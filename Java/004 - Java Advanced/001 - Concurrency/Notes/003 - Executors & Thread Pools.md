# Executors & Thread Pools

Creating a `Thread` per task doesn't survive contact with production: threads are expensive (each reserves stack memory and OS resources), unbounded creation under load is a self-inflicted denial of service, and hand-managed workers mean hand-managed failure, shutdown, and result collection. The **executor framework** (`java.util.concurrent`) fixes the model: we submit *tasks*; a managed **thread pool** runs them on reusable workers.

---

## ExecutorService and Thread Pools

An **`ExecutorService`** is a task-running service backed by a pool of threads, created via factory methods on `Executors`:

```java
ExecutorService pool = Executors.newFixedThreadPool(4);       // 4 reusable workers

for (Path file : reportFiles) {
    pool.submit(() -> process(file));       // queue a task; a free worker picks it up
}

pool.shutdown();                            // no new tasks; finish what's queued
pool.awaitTermination(1, TimeUnit.MINUTES); // block until done (or timeout)
```

*The task-based model: submit work, let the pool schedule it across a fixed set of threads.*

Submitted tasks queue up; the four workers pull from the queue, run tasks to completion, and pull again — threads amortized across thousands of tasks. The main factory choices:

- **`newFixedThreadPool(n)`** — n workers, unbounded queue. The workhorse for CPU-bound parallelism; size ≈ number of cores.
- **`newCachedThreadPool()`** — grows on demand, reuses idle threads. Suited to many short bursty tasks; unbounded growth is its risk.
- **`newSingleThreadExecutor()`** — one worker: tasks run *sequentially* but asynchronously — a free ordering guarantee.
- **`newVirtualThreadPerTaskExecutor()`** (Java 21+) — a **virtual thread** per task: JVM-managed featherweight threads, cheap in the millions. The modern default for I/O-bound work (each task may block freely; blocked virtual threads cost almost nothing).

**Shutdown is not optional** — pool threads keep the JVM alive. `shutdown()` drains gracefully; `shutdownNow()` interrupts in-flight tasks (delivering the `InterruptedException` from lesson 1); `ExecutorService` is `AutoCloseable` in modern Java, so try-with-resources works for scoped usage.

---

## Callable and Future: Tasks with Results

`Runnable` returns nothing. Its sibling **`Callable<V>`** returns a value (and may throw checked exceptions) — and submitting one yields a **`Future<V>`**: a handle to a result that doesn't exist yet:

```java
Callable<Double> pricing = () -> fetchPrice("WIDGET-7");      // slow remote call

Future<Double> future = pool.submit(pricing);                 // returns immediately

doOtherWorkMeanwhile();

Double price = future.get();                                  // BLOCKS until the result is ready
```

*`submit` hands back a promise; `get()` collects it — waiting only if the task hasn't finished.*

`get()` rethrows a failed task's exception wrapped in `ExecutionException` (the task's failure, delivered at collection time — check the cause), and the timed form `get(5, TimeUnit.SECONDS)` refuses to wait forever, which production code generally should insist on. Fan-out/fan-in falls out naturally:

```java
List<Callable<Double>> tasks = symbols.stream()
        .map(s -> (Callable<Double>) () -> fetchPrice(s))
        .toList();

List<Future<Double>> futures = pool.invokeAll(tasks);         // run all, wait for all
double total = 0;
for (Future<Double> f : futures) total += f.get();            // all complete — no blocking now
```

*`invokeAll`: submit a batch, wait for completion, then harvest — parallel fetch in a dozen lines.*

Notice what this design did to the last lesson's problems: each task works on its *own* data and communicates by **returning a value** — no shared mutable state, no locks. Task-based decomposition is synchronization avoidance, systematized.

---

## Sizing and Hygiene

The two-line theory of pool sizing: **CPU-bound** work (parsing, computation) gains nothing past the core count — `Runtime.getRuntime().availableProcessors()` threads. **I/O-bound** work (HTTP calls, database queries) spends its life blocked, so either size platform-thread pools much larger — or use virtual threads and stop counting. Beyond sizing: name pool threads via a thread factory (stack traces full of `pool-3-thread-7` are misery to debug), catch and log within tasks (an uncaught exception silently kills that task — check the `Future`), and never block inside a task waiting on *another task in the same bounded pool* (thread starvation deadlock).

`Future.get()` still has an ergonomic problem, though: it *blocks*. Composing stages — fetch, then transform, then combine with another fetch — turns into threads waiting on threads. The non-blocking composition of asynchronous stages is exactly what **`CompletableFuture`** adds, next.
