# Threads & the Runnable Interface

Everything we've written so far runs on one **thread** — a single line of execution stepping through one call stack. Real applications run many: a web server handles hundreds of requests at once, a UI stays responsive while saving, a batch job fans work across CPU cores. **Concurrency** is the discipline of multiple threads sharing one program — Java's deepest topic, opened here with the basic unit: creating and coordinating threads.

---

## What a Thread Is

A thread is an independent path of execution with **its own call stack**, scheduled onto CPU cores by the operating system. Every Java program starts with one — the **main thread**, running `main` — and may spawn more. Crucially, all threads in a process share the same **heap**: the same objects, the same static fields. Separate stacks, shared objects — that single sentence generates both the power (threads communicate through shared data) and the peril (next lesson) of everything in this topic.

---

## Runnable: The Task

Java splits the concept cleanly: a **`Runnable`** is *what to do* — a functional interface with one method, `void run()` — while a `Thread` is *the worker that does it*:

```java
Runnable task = () -> {
    String worker = Thread.currentThread().getName();
    System.out.println(worker + " processing...");
};

Thread t = new Thread(task, "worker-1");
t.start();                                        // begins running task CONCURRENTLY
System.out.println("main continues immediately");
```

*A `Runnable` (as a lambda — it's a functional interface) handed to a `Thread`, which runs it in parallel with `main`.*

Two mechanics deserve underlining. **Call `start()`, not `run()`** — `start()` creates the new thread of execution which then invokes `run()`; calling `run()` directly just executes the code in the *current* thread like any method call, the classic first concurrency bug. And **output order is not guaranteed** — "main continues" may print before or after the worker's line, differing run to run. That nondeterminism isn't a flaw to fix but the fundamental property to design around: once started, threads interleave however the scheduler pleases.

(The alternative style — subclassing `Thread` and overriding `run` — appears in old tutorials. Prefer `Runnable`: it separates task from machinery, works with lambdas, and hands off directly to the executors of lesson 3.)

---

## Coordinating: sleep, join, interrupt

A few static and instance methods cover basic coordination:

```java
Thread worker = new Thread(() -> {
    try {
        Thread.sleep(2000);                       // pause THIS thread 2 seconds
        System.out.println("done");
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();       // restore the flag; exit gracefully
        System.out.println("cancelled");
    }
});

worker.start();
worker.join();                                    // main BLOCKS here until worker finishes
System.out.println("worker has finished");
```

*`sleep` pauses, `join` waits for another thread's completion, and interruption is the polite cancellation channel.*

**`sleep(millis)`** pauses the current thread. **`join()`** makes the calling thread wait until the target thread terminates — the simplest way `main` can collect results after workers finish. Both throw **`InterruptedException`**, a checked exception (Unit 3's rules apply) that is Java's cancellation mechanism: calling `worker.interrupt()` asks a thread to stop; a sleeping/waiting thread wakes with this exception, and well-behaved code treats it as "wrap up now" — never an empty catch. A **daemon thread** (`setDaemon(true)` before start) is the background variant that won't keep the JVM alive when all normal threads finish.

---

## Why Threads Are Hard: A Preview

The shared heap turns innocent code treacherous. Two threads incrementing one counter:

```java
static int counter = 0;                           // shared static state — the warning label from Unit 2!

Runnable increment = () -> {
    for (int i = 0; i < 10_000; i++) counter++;   // counter++ is NOT one step
};

Thread a = new Thread(increment), b = new Thread(increment);
a.start(); b.start();
a.join(); b.join();
System.out.println(counter);                      // 20000? Almost never. 13212. 17845. Varies.
```

*Two threads racing on one variable: increments interleave and overwrite each other — a **race condition**.*

`counter++` is three operations (read, add, write), and when threads interleave between them, updates vanish. This — plus the subtler question of when one thread's writes become *visible* to another — is exactly the subject of the next lesson, Synchronization.

---

## The Modern Landscape, Honestly

Raw `new Thread(...)` is the right way to *learn* threads and the wrong way to *ship* them — production code uses **executors and thread pools** (lesson 3) to manage workers and **`CompletableFuture`** (lesson 4) to compose results, and Java 21's **virtual threads** (`Thread.ofVirtual()`) make blocking-style code scale to millions of concurrent tasks. All of them still run `Runnable`s (or its value-returning sibling `Callable`), still interleave nondeterministically, and still share the heap — so the fundamentals in these first two lessons are the part that never goes out of date.
