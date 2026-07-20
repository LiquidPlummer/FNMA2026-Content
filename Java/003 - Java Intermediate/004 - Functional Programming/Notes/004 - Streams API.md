# Streams API

The **Streams API** is where the topic's pieces assemble: a pipeline style for processing collections, where each step is a functional interface filled by a lambda. Instead of loops describing *how* to traverse, filter, and accumulate, a stream declares *what* the result should be — filter by this, transform to that, collect thus — and reads like the requirement it implements.

---

## The Pipeline Shape

Every stream computation has three parts: a **source**, zero or more **intermediate operations** (each returning a new stream), and one **terminal operation** that produces the result and ends the pipeline:

```java
List<String> topCustomers = invoices.stream()               // source
        .filter(inv -> inv.getAmount() > 10_000)            // intermediate: keep matching
        .map(Invoice::getCustomer)                          // intermediate: transform each
        .distinct()                                         // intermediate: drop duplicates
        .sorted()                                           // intermediate: natural order
        .toList();                                          // terminal: materialize
```

*The canonical pipeline: source → filter → map → collect, reading top to bottom like a spec.*

The loop equivalent is a dozen lines of accumulator lists and nested conditions; the stream states the policy. Sources beyond `collection.stream()`: `Arrays.stream(array)` (met briefly in the Arrays topic), `Stream.of(a, b, c)`, `IntStream.range(0, n)` (a functional counting loop), and `Files.lines(path)` (a lazy stream of file lines — try-with-resources applies).

Two properties define stream behavior. **Laziness**: intermediate operations do nothing until a terminal operation pulls — elements flow through the whole pipeline one at a time, so `filter` sees only what it needs and short-circuiting terminals can stop early. **Single-use**: a stream is consumed by its terminal operation; reusing it throws `IllegalStateException` — build a fresh one (they're cheap views, not copies; the source collection is untouched throughout).

---

## The Intermediate Vocabulary

A small set of operations covers nearly all pipelines — each taking exactly the functional interface its job implies:

- **`filter(Predicate)`** — keep elements passing the test
- **`map(Function)`** — replace each element with a transformation of it
- **`flatMap(Function)`** — when the transform yields a stream *per element* (order → its line items), flatten the streams-of-streams into one
- **`sorted()` / `sorted(Comparator)`** — natural or supplied order (Comparable & Comparator, paying off again)
- **`distinct()`** — deduplicate via `equals` (the hash-collection contract, once more)
- **`limit(n)` / `skip(n)`** — truncation and paging
- **`peek(Consumer)`** — observe elements mid-flight; debugging only

*The core eight — `filter`, `map`, and `flatMap` carry the overwhelming share of real pipelines.*

---

## Terminal Operations: Getting Answers Out

```java
// Collect into containers
List<String> list = stream.toList();                              // (Java 16+)
Set<String> set = stream.collect(Collectors.toSet());
String joined = stream.collect(Collectors.joining(", "));
Map<String, List<Invoice>> byCustomer =
        invoices.stream().collect(Collectors.groupingBy(Invoice::getCustomer));

// Reduce to a single value
double total = invoices.stream().mapToDouble(Invoice::getAmount).sum();
Optional<Invoice> biggest = invoices.stream().max(Comparator.comparing(Invoice::getAmount));
long overdueCount = invoices.stream().filter(Invoice::isOverdue).count();

// Search and test — short-circuiting
boolean anyHuge = invoices.stream().anyMatch(inv -> inv.getAmount() > 1_000_000);
Optional<Invoice> firstOverdue = invoices.stream().filter(Invoice::isOverdue).findFirst();

// Side effects, when the goal IS the effect
invoices.stream().filter(Invoice::isOverdue).forEach(mailer::sendReminder);
```

*The terminal families: collect, reduce, match/find, and forEach — every pipeline ends in exactly one.*

Threads worth pulling: `max`, `min`, and `findFirst` return **`Optional`** — the API practicing what lesson 2 preached, since an empty stream has no max. **`Collectors.groupingBy`** is the workhorse nobody forgets after the first use — a categorized `Map` in one line. The `mapToDouble`/`IntStream` family are the primitive specializations promised in the Functional Interfaces lesson — numeric pipelines without boxing, carrying `sum()`, `average()` (an `OptionalDouble`), and `summaryStatistics()`. Behind them all stands general **`reduce`** — `stream.reduce(0, Integer::sum)` — worth understanding as the primitive that `sum`, `max`, and `count` specialize.

---

## Stream Discipline

The API rewards a functional style and punishes hybrids:

- **Don't mutate external state from inside a pipeline.** A lambda adding to an outside list defeats laziness guarantees and breaks under parallelism — *collect*, don't accumulate by hand. (The effectively-final rule from last lesson is nudging the same direction.)
- **Keep lambdas in pipelines small.** A ten-line `map` lambda is a named method wanting a method reference.
- **Streams complement loops; they don't outlaw them.** A plain for-each remains clearer for simple iteration with side effects, early-exit searches with complex state, or index-driven logic. The stream wins when the computation is naturally *filter–transform–aggregate*.
- **`parallelStream()` exists and is not free candy** — parallelism helps large, CPU-heavy, independent workloads and actively hurts small or I/O-bound ones. Treat it as an optimization to measure, with full context arriving in Concurrency (Java Advanced).

---

## Closing the Unit

Functional programming completes Java Intermediate. The through-line of the unit: **exceptions** gave failures a disciplined channel, **OOP** organized types around contracts, **collections** put interface-first design to work at scale, and **functional style** made behavior itself a first-class value flowing through those contracts — `Predicate`s into `filter`, `Comparator`s into `sorted`, `Function`s into `map`. Java Advanced builds on all of it at once: threads running `Runnable` lambdas, JUnit asserting over collections, and Spring wiring interfaces to implementations behind annotations.
