# Lab: Pipelines

In this lab we put the Streams API through a real workload: source → intermediate operations (`filter`, `map`, `sorted`, `distinct`, `limit`) → terminals (`toList`, `count`, matches and finds, numeric aggregation, `groupingBy`, `joining`) over a two-dozen-order dataset. We also watch laziness happen with `peek`, keep one loop *as* a loop on purpose, and trip the single-use rule so it's never a surprise again.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

From this lab's folder:

```console
mvn -q compile exec:java
```

Starters: [Order.java](src/main/java/com/curriculum/labs/Order.java) (record + nested `Status` enum), [OrderData.java](src/main/java/com/curriculum/labs/OrderData.java) (read its comment — the edge cases are planted), and [PipelineLab.java](src/main/java/com/curriculum/labs/PipelineLab.java).

---

## Part 1 — One pipeline, built slowly, watched closely

At **`[MARKER 1]`**, the lab's first pipeline — typed in stages, with a spy attached. Start with just the source and a `peek`, and a small terminal:

```java
var bigPaidCustomers = orders.stream()
        .peek(o -> System.out.println("  flowing: " + o.id()))
        .filter(o -> o.status() == Order.Status.PAID)
        .filter(o -> o.amount() >= 1000)
        .map(Order::customer)
        .distinct()
        .sorted()
        .toList();
System.out.println("big paid customers: " + bigPaidCustomers);
```

*Filter twice, transform to names, dedupe, sort, materialize — the requirement ("which customers have a paid order of $1000+?") reads straight down the page.*

Run it and study the `peek` output *before* the result: all 24 ids flow — but move the `peek` to *after* the two filters and run again: only the survivors print. Elements travel the pipeline one at a time, each going as far as it qualifies; nothing happens in "phases." Then the sharper demonstration — replace `.toList()` with nothing (delete the terminal, keep a semicolon after `sorted()`, assigning to a `var pipeline =`): run, and **no `peek` lines print at all.** Intermediate operations are a plan, not an action; the terminal is what turns the key. Restore the `toList()`.

One data detail: Ada Corp appears once despite having *two* qualifying orders (O-01 and O-12, the planted twins) — `distinct()` on the mapped names did its job.

---

## Part 2 — The intermediate vocabulary

At **`[MARKER 2]`**, quick reps with the rest of the everyday operators:

```java
var topThree = orders.stream()
        .filter(o -> o.status() != Order.Status.CANCELLED)
        .sorted(java.util.Comparator.comparingDouble(Order::amount).reversed())
        .limit(3)
        .toList();
topThree.forEach(o -> System.out.println("top: " + o));

var idsAfterTen = orders.stream().skip(10).map(Order::id).toList();
System.out.println("from O-11 on: " + idsAfterTen);
```

*`sorted` with a comparator (Order Matters, third appearance), `limit`/`skip` for top-N and paging shapes.*

Note what `sorted(...)` consumed: the exact comparator style from the Collections labs. The functional topic didn't replace that machinery — it's the delivery mechanism for it.

---

## Part 3 — Terminals: every family, one specimen

At **`[MARKER 3]`**:

```java
long pendingCount = orders.stream().filter(o -> o.status() == Order.Status.PENDING).count();

boolean anyHuge = orders.stream().anyMatch(o -> o.amount() > 2500);

var firstCancelled = orders.stream()
        .filter(o -> o.status() == Order.Status.CANCELLED)
        .findFirst();                                    // Optional<Order> — of course
System.out.println("first cancelled: " + firstCancelled.map(Order::id).orElse("none"));

double paidRevenue = orders.stream()
        .filter(o -> o.status() == Order.Status.PAID)
        .mapToDouble(Order::amount)
        .sum();

var stats = orders.stream().mapToDouble(Order::amount).summaryStatistics();
System.out.printf("count=%d min=%.2f avg=%.2f max=%.2f%n",
        stats.getCount(), stats.getMin(), stats.getAverage(), stats.getMax());
```

*Count, match (short-circuiting — it stops at the first hit), find (returning `Optional`, the last lab cashing in), and the primitive-stream aggregations via `mapToDouble`.*

Worth pausing on `findFirst`: an empty stream has no first element, and the API says so in its return type instead of returning null — the Streams API is `Optional`'s biggest customer, which is why the outline sequenced these labs this way.

---

## Part 4 — Collect: from stream to structure

At **`[MARKER 4]`**, the collector everyone remembers:

```java
var revenueByRegion = orders.stream()
        .filter(o -> o.status() == Order.Status.PAID)
        .collect(java.util.stream.Collectors.groupingBy(
                Order::region,
                java.util.stream.Collectors.summingDouble(Order::amount)));
System.out.println("paid revenue by region: " + revenueByRegion);

String pendingReport = orders.stream()
        .filter(o -> o.status() == Order.Status.PENDING)
        .map(o -> o.id() + " (" + o.customer() + ")")
        .collect(java.util.stream.Collectors.joining(", ", "PENDING: [", "]"));
System.out.println(pendingReport);
```

*`groupingBy` with a downstream collector: a categorized totals map in one expression — the Part 3-of-Choose-Your-Structure word count, generalized. `joining` builds delimited report lines with prefix/suffix.*

Cross-check one region's total by hand against the data file — thirty seconds of arithmetic that converts "I ran it" into "I verified it," the habit every lab in this curriculum keeps pushing.

---

## Part 5 — The loop that stays a loop

At **`[MARKER 5]`**, a task that *could* be streamed — print a numbered receipt line for each PAID order, stopping entirely if a data error (negative amount) is found:

```java
int line = 1;
for (Order o : orders) {
    if (o.status() != Order.Status.PAID) continue;
    if (o.amount() < 0) {
        System.out.println("DATA ERROR at " + o.id() + " — aborting report");
        break;
    }
    System.out.printf("%2d. %-6s %-10s %8.2f%n", line++, o.id(), o.customer(), o.amount());
}
```

*An index that increments per-print, and an early abort — both awkward to express in a pipeline (the counter fights effectively-final; the abort fights the no-early-exit shape).*

Write it as a loop, deliberately, and leave a one-line comment saying why. The judgment being trained: streams for *filter–transform–aggregate*, loops for stateful iteration and early exits. Fluency means choosing, not defaulting — in either direction.

---

## Part 6 — Streams are single-use

At **`[MARKER 6]`**, the rule that bites everyone exactly once:

```java
var paidStream = orders.stream().filter(o -> o.status() == Order.Status.PAID);
System.out.println("count: " + paidStream.count());
System.out.println("any huge: " + paidStream.anyMatch(o -> o.amount() > 2500));
```

*Run it: `IllegalStateException: stream has already been operated upon or closed` — the first terminal consumed the stream; the second found a husk.*

The fix is never storing streams — store the *source* (the list) and open a fresh stream per question, or store the *result* (`toList()`) if the intermediate work is worth reusing. Fix it both ways, pick the one this case deserves, and comment the difference. Streams are cheap views; treat them as disposable.

---

## Exercises

1. **Five queries, prose to pipeline.** Implement each: (a) total revenue per customer, highest first, top three only; (b) ids of all zero-amount orders that are *not* cancelled (data-quality report); (c) the average PAID order amount per region, but only for regions with 3+ paid orders; (d) one string: all distinct customers, alphabetical, semicolon-separated; (e) does *every* region have at least one PAID order? (one terminal answers it).

2. **Translate both directions.** (a) Rewrite Part 5's receipt loop as a stream *as far as is honest* and write two sentences on what got worse. (b) Rewrite this pipeline as a loop and say which version a maintainer should get: `orders.stream().filter(o -> o.customer().equals("Ada Corp")).mapToDouble(Order::amount).max().orElse(0)`.

3. **The mutation bug.** This compiles and usually "works" — find the two things wrong with it, then fix it into proper pipeline shape: `var results = new ArrayList<String>(); orders.stream().filter(o -> o.amount() > 500).forEach(o -> results.add(o.id()));`

4. **Grouping, deeper.** Produce a `Map<String, Map<Order.Status, Long>>` — per region, how many orders in each status — with one pipeline (hint: `groupingBy` nests). Then answer: what collection type are the inner maps, and what would you change if the report needed statuses in declaration order? (Two labs' worth of knowledge meet in that answer.)
