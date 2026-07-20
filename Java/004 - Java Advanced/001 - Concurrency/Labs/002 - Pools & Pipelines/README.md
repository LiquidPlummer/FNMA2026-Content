# Lab: Pools & Pipelines — Executors, Callable/Future, and CompletableFuture

In this lab we build a **best-price aggregator** three times over: first sequentially (the timed baseline), then on a fixed thread pool with `Callable`, `Future`, and `invokeAll`, and finally as a `CompletableFuture` pipeline with `supplyAsync`, `thenApply`, `thenCombine`, and a timeout defense for a flaky service. The services are simulated (a `sleep` and some jitter each), but the numbers on your screen are real — every rebuild gets measurably faster or tougher, and that's the lesson.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

Verify both with `java -version` and `mvn -version`. Then, from this lab's folder (the one containing `pom.xml`), run:

```console
mvn -q compile exec:java -Dexec.args="1"
```

You should see the Part 1 banner and nothing else — the parts are empty until you build them. Two files matter: [src/main/java/com/curriculum/labs/PriceLab.java](src/main/java/com/curriculum/labs/PriceLab.java) is where you'll work (find the `[MARKER]` comments), and [src/main/java/com/curriculum/labs/Services.java](src/main/java/com/curriculum/labs/Services.java) is **provided — read it, don't edit it**. It simulates three vendor pricing services: `warehousePrice` (~800 ms), `retailPrice` (~1000 ms), and `partnerPrice` — usually ~1200 ms and the cheapest, but roughly **one call in three it stalls for 3 seconds**. That flakiness is not a bug in the lab; it's the villain of Part 3.

The starter also gives you two small helpers, already written: `cheaper(a, b)` picks the lower-priced of two `Quote` records, and `elapsedMs(start)` turns a `System.nanoTime()` reading into milliseconds.

---

## Part 1 — Sequential: the honest baseline

No concurrency yet — we need a number to beat. At **`[MARKER 1]`** in `part1()`, we'll call the three services back to back and time the whole thing:

```java
long start = System.nanoTime();

Quote warehouse = new Quote("warehouse", Services.warehousePrice(SKU));
Quote retail = new Quote("retail", Services.retailPrice(SKU));
Quote partner = new Quote("partner", Services.partnerPrice(SKU));

Quote best = cheaper(cheaper(warehouse, retail), partner);
System.out.printf("best: %s at $%.2f  (%d ms)%n", best.vendor(), best.price(), elapsedMs(start));
```

*Three slow calls in single file, then the one-rule aggregation: cheapest wins.*

Run it a few times. Two observations to bank: every service line prints the **same thread name** (nobody but the launcher's main thread is working), and the total is the **sum** of the three latencies — about **3000 ms** on a good run, about **4800 ms** when the partner stalls. Each call sits idle waiting on simulated network while the other two haven't even started. That idleness is the entire opportunity.

---

## Part 2 — A fixed pool: submit, invokeAll, harvest

The executor framework's move: stop managing threads, start submitting *tasks*. Each vendor call becomes a **`Callable<Quote>`** — like `Runnable`, but it *returns a value* — and a pool of three workers runs them simultaneously.

At **`[MARKER 2]`** in `part2()`:

```java
long start = System.nanoTime();

List<Callable<Quote>> tasks = List.of(
        () -> new Quote("warehouse", Services.warehousePrice(SKU)),
        () -> new Quote("retail", Services.retailPrice(SKU)),
        () -> new Quote("partner", Services.partnerPrice(SKU)));

try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
    List<Future<Quote>> futures = pool.invokeAll(tasks);

    Quote best = null;
    for (Future<Quote> f : futures) {
        Quote q = f.get();
        best = (best == null) ? q : cheaper(best, q);
    }
    System.out.printf("best: %s at $%.2f  (%d ms)%n", best.vendor(), best.price(), elapsedMs(start));
}
```

*Three `Callable` tasks, one `invokeAll` batch: run all, wait for all, then harvest the `Future`s.*

You'll need imports at the top of the file: `java.util.List` and `java.util.concurrent.*`.

Run it a few times and compare against Part 1. The service lines now print **three different pool thread names** (`pool-1-thread-1` and friends), all appearing at once — and the elapsed time collapses to roughly the *slowest single call*, about **1200 ms**, not the sum. That's the whole speedup argument in one number: parallel fan-out costs max, not sum.

Three details in that snippet deserve a closer look:

- **`invokeAll` waits for the whole batch**, so by the time we loop over the futures, every `f.get()` returns immediately — the harvesting loop isn't where the waiting happens.
- **Shutdown is not optional** — pool threads would keep the JVM alive forever. We used the modern form: `ExecutorService` is `AutoCloseable`, and try-with-resources shuts the pool down and waits for it on the way out. The pre-Java-19 equivalent you'll still see everywhere is `pool.shutdown()` in a `finally` block (followed by `awaitTermination`). Same obligation, older spelling.
- **No shared state, no locks.** Compare with the previous lab's counters: each task computed its own `Quote` and *returned* it; `main` combined results after the fact. Task-based decomposition is synchronization avoidance, systematized.

The wrinkle: run Part 2 until the partner stalls (a run in three, on average). The elapsed time jumps to ~3000 ms — `invokeAll` waits for *all* tasks, so the whole batch is hostage to its slowest member, and `Future` gives us no graceful way to say "two out of three is fine, use a fallback for the laggard." For that we need composition.

---

## Part 3 — CompletableFuture: the pipeline with defenses

`CompletableFuture` turns "a result that isn't ready yet" into something we can *transform, combine, and guard* — a stream pipeline over time instead of elements. We'll rebuild the aggregation so the flaky partner gets a two-second budget and a fallback, while the other two vendors proceed untouched.

At **`[MARKER 3]`** in `part3()`, first the pool — this time with **named threads**, because `pool-1-thread-1` tells you nothing in a stack trace:

```java
try (ExecutorService quotePool = Executors.newFixedThreadPool(3,
        Thread.ofPlatform().name("quote-", 1).factory())) {

    long start = System.nanoTime();

    CompletableFuture<Quote> warehouseF = CompletableFuture
            .supplyAsync(() -> Services.warehousePrice(SKU), quotePool)
            .thenApply(price -> new Quote("warehouse", price));

    CompletableFuture<Quote> retailF = CompletableFuture
            .supplyAsync(() -> Services.retailPrice(SKU), quotePool)
            .thenApply(price -> new Quote("retail", price));

    CompletableFuture<Quote> partnerF = CompletableFuture
            .supplyAsync(() -> Services.partnerPrice(SKU), quotePool)
            .thenApply(price -> new Quote("partner", price))
            .orTimeout(2, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                System.out.println("    partner quote failed (" + ex.getClass().getSimpleName()
                        + ") — using last-known price");
                return new Quote("partner (cached)", 36.00);
            });

    CompletableFuture<Quote> bestF = warehouseF
            .thenCombine(retailF, PriceLab::cheaper)
            .thenCombine(partnerF, PriceLab::cheaper);

    Quote best = bestF.join();
    System.out.printf("best: %s at $%.2f  (%d ms)%n", best.vendor(), best.price(), elapsedMs(start));
}
```

*Three async legs launched with `supplyAsync` on a named executor; `thenApply` shapes each result; `orTimeout` + `exceptionally` armor the flaky leg; `thenCombine` merges everything into one best quote.*

Add `java.util.concurrent.CompletableFuture` and `java.util.concurrent.TimeUnit` to your imports if they aren't covered by the wildcard already.

Walk the pipeline before you run it. `supplyAsync(supplier, executor)` launches each fetch on our named pool — passing the executor explicitly is mandatory practice for I/O-shaped work (the default common pool is sized for CPU work). Each `thenApply` transforms a result *when it arrives* — a `Function`, exactly like `map` on a stream. `thenCombine` takes two independent futures and merges their results when **both** are done — and since `cheaper` is just a two-argument function, a method reference drops straight in. Nothing anywhere blocks until the final `join()`, which is `get()`'s unchecked-exception sibling.

Now the defenses on the partner leg, in order: **`orTimeout(2, SECONDS)`** fails that future if no result arrives in time; **`exceptionally`** is the async catch block — it catches the timeout (or any upstream failure), logs, and substitutes a last-known price, letting the pipeline *continue as if the partner had answered*. Order matters here: the `exceptionally` must come after the `orTimeout` to catch it.

Run it several times — keep going **until you see the failure path actually fire**; with a one-in-three stall you rarely need more than a few runs. On a clean run: ~1200 ms, partner usually wins (it's the cheapest). On a stall run: the "partner quote failed (TimeoutException) — using last-known price" line appears at almost exactly the 2-second mark, the aggregation completes at **~2000 ms with a sensible answer anyway**, and the best quote falls back to whichever real vendor was cheaper (or the cached partner price). Compare that with Part 2's hostage situation: same flaky service, but now the *pipeline* absorbed the failure.

One last observation for the road: on a stall run the JVM lingers a moment after printing the result. That's try-with-resources waiting out the abandoned partner call — the *future* timed out, but the underlying task is still sleeping on its pool thread. Timeouts abandon results; they don't reach in and stop work. True cancellation is a deeper contract (the interruption machinery from the first lab), and knowing the difference is worth a lot in production post-mortems.

---

## Exercises

The training wheels come off — same concepts, new problems, no step-by-step. Add parts `4`, `5`, ... to the switch as needed.

1. **The full catalog.** Quote *five* SKUs (`"WIDGET-7"`, `"GADGET-3"`, `"GIZMO-9"`, `"DOOHICKEY-1"`, `"SPROCKET-5"`), each through the complete Part 3 pipeline (extract it into a method `CompletableFuture<Quote> bestPriceF(String sku, ExecutorService pool)`). Launch all five, use `CompletableFuture.allOf(...)` to await the batch, then print a five-line price table and one total elapsed time. Before running, predict the time: five aggregations, three calls each, on a pool of — how many threads should it be, and what happens to your total if you leave it at 3?

2. **Quieter fallback.** Rework the partner leg to use `completeOnTimeout(new Quote("partner (cached)", 36.00), 2, TimeUnit.SECONDS)` instead of the `orTimeout` + `exceptionally` pair. Verify it behaves the same on a stall run — then answer in a comment: what did the pipeline *lose* in the trade? (Hint: run it until a stall and read the output carefully. What no longer happens? Would you know the partner service was degrading?)

3. **The frozen pool.** No code for this one — diagnose from the description, in a comment. *A teammate's service uses `Executors.newFixedThreadPool(2)`. Each incoming request submits a task; that task splits its work by submitting two sub-tasks to the same pool and calling `get()` on both. Under light testing it worked. On launch day, two requests arrived at once and the service froze forever — no errors, no CPU usage, every thread state `WAITING`.* Explain precisely what each of the two pool threads is doing, why no sub-task can ever run, and give two different fixes (there are at least three).

4. **Choose your weapon.** For each scenario, name the right tool — raw `Thread`, fixed thread pool, single-thread executor, or `CompletableFuture` (possibly on virtual threads) — and defend it in a sentence, in comments:
   a. Parse 10,000 CSV files on an 8-core batch server, CPU-bound, results collected at the end.
   b. A desktop app needs one background thread so file saves never freeze the UI, and saves must happen in the order requested.
   c. A checkout page needs shipping options from three carriers' APIs; show whatever arrives within 800 ms, with a flat-rate fallback for the rest.
   d. A one-off admin script that pings a health endpoint every 10 seconds while the main flow does an interactive restore, then exits with it.
