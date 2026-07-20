# Lab: Threads & Shared State

In this lab we make the first two Concurrency lessons physical: we'll create and coordinate threads with `Runnable`, `start()`, and `join()`, then *watch* a race condition destroy a shared counter, and fix it three escalating ways — a `synchronized` method, an `AtomicInteger`, and no shared state at all. A `volatile` stop-flag closes the loop on visibility. By the end you'll have seen every wrong answer with your own eyes, which is the only way concurrency ever really sinks in.

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

You should see a usage-style banner for Part 1 and the name of the main thread. The starter skeleton is [src/main/java/com/curriculum/labs/ThreadLab.java](src/main/java/com/curriculum/labs/ThreadLab.java) — open it now and find the numbered `[MARKER]` comments. Each part of the walkthrough tells you which marker to work at, and the `-Dexec.args` number picks which part runs.

---

## Part 1 — Two workers (and the classic first mistake)

A `Runnable` is *what to do*; a `Thread` is *the worker that does it*. Our first goal is just to see two threads genuinely running at once — and to step on the most famous rake in Java concurrency on the way there, on purpose.

At **`[MARKER 1]`** inside `part1()`, we'll add two workers — but note carefully: the last two lines call **`run()`**, not `start()`. That's deliberate:

```java
Runnable task = () -> {
    String me = Thread.currentThread().getName();
    for (int i = 1; i <= 5; i++) {
        System.out.println(me + " working, step " + i);
    }
};

Thread alice = new Thread(task, "alice");
Thread bob = new Thread(task, "bob");

alice.run();   // WRONG on purpose — we'll fix this in a moment
bob.run();
```

*Two named threads sharing one task — but invoked the wrong way.*

Run Part 1 and read the output closely. Every line names the **same thread** — the one the banner already identified as the main thread (under `mvn exec:java` it carries the launcher's long name, `com.curriculum.labs.ThreadLab.main()`, rather than `main`) — not `alice`, not `bob` — and the steps come out in perfect 1-to-5 order, twice. Nothing concurrent happened. Calling `run()` directly is just an ordinary method call *in the current thread*; the two `Thread` objects sat unused. This is the classic first concurrency bug, and now you've seen its signature: everything works, sequentially, on `main`.

Now change the two calls to **`alice.start(); bob.start();`** and run again — several times. The lines now carry the names `alice` and `bob`, and their steps *interleave*: differently on every run. `start()` is what actually creates a new thread of execution, which then calls `run()` for us. And that run-to-run variation isn't a bug to fix — it's the scheduler doing as it pleases, the fundamental property everything else in this lab is about.

One more piece: notice `part1()` finishes while output may still be printing — `main` doesn't wait for anyone. Add this after the two `start()` calls:

```java
alice.join();
bob.join();
System.out.println("main: both workers finished");
```

*`join()` blocks the calling thread until the target thread terminates.*

Run again: the closing line is now reliably last. `join()` is how `main` collects its workers before moving on — we'll lean on it in every part that follows.

---

## Part 2 — The race, observed

The notes claim that two threads incrementing one counter lose updates. We're not going to take the notes' word for it.

First, at **`[MARKER 2a]`**, the shared state — a counter with no protection whatsoever, implementing the little `Counter` interface the starter file provides:

```java
static class BrokenCounter implements Counter {
    private int count = 0;
    public void increment() { count++; }
    public int value() { return count; }
}
```

*Innocent-looking code: `count++` is secretly read → add → write, three steps a scheduler can slice between.*

Next, at **`[MARKER 2b]`**, a reusable race harness — since Parts 2 and 3 will race four different counters, we write the experiment once against the interface:

```java
static void race(String label, Counter counter) throws InterruptedException {
    Runnable work = () -> {
        for (int i = 0; i < INCREMENTS; i++) {
            counter.increment();
        }
    };
    Thread a = new Thread(work);
    Thread b = new Thread(work);
    a.start(); b.start();
    a.join();  b.join();
    System.out.println(label + ": expected " + (2 * INCREMENTS) + ", got " + counter.value());
}
```

*Two threads, 100,000 increments each, `join` both, report — the same experiment for every counter we'll build.*

Finally, at **`[MARKER 2c]`** inside `part2()`, three trials:

```java
for (int trial = 1; trial <= 3; trial++) {
    race("trial " + trial, new BrokenCounter());
}
```

*Three fresh counters, three races.*

Run Part 2. Expected 200000 — and you'll get three *different* wrong answers, something like `117482`, `143909`, `152114`. Run it again: three new wrong answers. Every gap is thousands of increments that executed and simply vanished — thread B read `count` between A's read and write, both computed from the same stale value, and one update overwrote the other. This is a **race condition**: correctness that depends on scheduling luck. (If a trial occasionally lands on exactly 200000 — luck happens — the other trials give it away.)

---

## Part 3 — Three fixes, escalating

Same harness, three correct counters. Each fix embodies one rung of the notes' hierarchy — and we verify each one by racing it.

**Fix 1: `synchronized`.** At **`[MARKER 3a]`**:

```java
static class SyncCounter implements Counter {
    private int count = 0;
    public synchronized void increment() { count++; }
    public synchronized int value() { return count; }
}
```

*Both methods take the object's lock — increments can no longer interleave, and reads see published writes.*

Note that `value()` is synchronized too, not just `increment()` — *every* access to guarded state goes through the same lock, reads included. One unsynchronized read ruins the guarantee.

**Fix 2: `AtomicInteger`.** At **`[MARKER 3b]`**:

```java
static class AtomicCounter implements Counter {
    private final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
    public void increment() { count.incrementAndGet(); }
    public int value() { return count.get(); }
}
```

*The read-modify-write packaged as one atomic hardware operation — no lock, no blocking.*

(Feel free to move that import to the top of the file — written inline here so the snippet is self-contained.)

**Fix 3: don't share at all.** The best fix on the hierarchy isn't a better lock — it's removing the shared variable. At **`[MARKER 3c]`**:

```java
static class CountTask implements Runnable {
    private int count = 0;
    public void run() {
        for (int i = 0; i < INCREMENTS; i++) {
            count++;
        }
    }
    int count() { return count; }
}
```

*Each task counts in its own private field — there is nothing to race on.*

Now wire all three up at **`[MARKER 3d]`** inside `part3()`. The first two reuse the harness; the confinement version needs its own few lines since each thread gets its *own* task object:

```java
race("synchronized ", new SyncCounter());
race("atomic       ", new AtomicCounter());

CountTask taskA = new CountTask();
CountTask taskB = new CountTask();
Thread a = new Thread(taskA);
Thread b = new Thread(taskB);
a.start(); b.start();
a.join();  b.join();
System.out.println("confinement  : expected " + (2 * INCREMENTS) + ", got " + (taskA.count() + taskB.count()));
```

*Two private counters, summed by `main` after `join` — communication by results, not by shared variables.*

Run Part 3 several times: 200000, every line, every run. One subtlety worth saying out loud: the confinement version is only safe because `main` reads each task's `count` *after* `join()` — joining a thread publishes everything it wrote. And notice the ranking you just demonstrated: the cleverest fix had the least synchronization in it. Most well-designed concurrent code shares as little as possible and passes results instead — which is exactly where thread pools take us in the next lab.

---

## Part 4 — The flag nobody saw

Atomicity was Part 2's problem. The second problem from the notes is **visibility**: a write one thread makes that another thread simply never sees. We'll now build the infamous never-stopping loop.

At **`[MARKER 4a]`**:

```java
static class Worker implements Runnable {
    boolean stopped = false;             // no volatile — yet
    long iterations = 0;

    public void run() {
        while (!stopped) {
            iterations++;
        }
        System.out.println("worker stopped after " + iterations + " iterations");
    }
}
```

*A worker that spins until someone sets its flag — or so we hope.*

And at **`[MARKER 4b]`** inside `part4()`:

```java
Worker worker = new Worker();
Thread t = new Thread(worker);
t.start();

Thread.sleep(100);
System.out.println("main: setting stopped = true");
worker.stopped = true;

t.join(2000);
if (t.isAlive()) {
    System.out.println("main: two seconds later the worker is STILL spinning — it never saw the flag.");
    System.out.println("main: (exiting the JVM forcibly; a real program would hang here forever)");
    System.exit(0);
}
```

*Start the worker, let it warm up, flip the flag — then wait up to two seconds for a stop that may never come.*

Run Part 4. On almost every machine the worker never stops: the JIT compiler, seeing a field no one in *this thread* changes, hoists the read out of the loop — the worker is spinning on a stale copy, and `main`'s write might as well not exist. Without a happens-before relationship, there is no guarantee a write is *ever* seen.

Now the one-word fix — change the field declaration to:

```java
volatile boolean stopped = false;
```

*`volatile`: every read of this field sees the latest write, guaranteed.*

Run again: the worker announces it stopped (after some hundreds of millions of iterations), `join` returns immediately, no forced exit. That's `volatile`'s legitimate niche — a status flag with one writer and simple reads. What it does **not** do is fix Part 2: `count++` on a volatile field is still three steps, still a race. Visibility and atomicity are different problems; today you've watched both, separately.

---

## Exercises

The training wheels come off — same concepts, new problems, no step-by-step. Add each as a new part in `ThreadLab` (`case "5" -> ...` and so on), or as separate classes if a problem outgrows one method.

1. **Deadlock, constructed then cured.** Create two lock objects, `lockA` and `lockB`, and two threads: the first synchronizes on `lockA` then (inside that block) on `lockB`; the second takes them in the *opposite* order. Put a `Thread.sleep(50)` between the first and second acquisition in each thread so they reliably collide, and print a line before each acquisition attempt. Run it — the program freezes with each thread holding what the other needs (Ctrl+C to escape). Then fix it *without removing any locks*: make both threads acquire in the same global order, and prove the program now finishes. Keep both versions in your code, selectable somehow, so you can demo the disease and the cure.

2. **Racing word count.** Take a `String[]` of a few hundred words (generate one, or repeat a small sentence array a hundred times). Split the array into two halves and give each half to its own thread; each thread tallies its words into one *shared* `ConcurrentHashMap<String, Integer>` using `merge(word, 1, Integer::sum)`. After joining both threads, verify the counts: the values should sum to exactly the array's length, every run. Then — briefly, in a comment — explain why plain `HashMap` with `map.put(word, map.getOrDefault(word, 0) + 1)` would fail here, and *which* of the two problems from this lab (atomicity or visibility) that failure is.

3. **Predict the range.** Answer in a comment before you test anything. Two threads each perform 10 increments on one `BrokenCounter`. (a) What is the *maximum* possible final value, and what schedule produces it? (b) Is 15 a possible result? Describe an interleaving or rule it out. (c) The minimum possible value is surprisingly low — it is 2, not 10. Don't worry about constructing the full pathological schedule, but answer this: what must a thread be holding for an increment from "the past" to wipe out later ones? (d) Now replace `BrokenCounter` with `SyncCounter`, same question: what values are possible?

4. **The polite worker.** Rewrite Part 4's `Worker` to stop via *interruption* instead of a flag: the loop condition checks `Thread.currentThread().isInterrupted()`, and `main` calls `t.interrupt()` instead of setting a field. Then extend the worker so each iteration also sleeps 10 ms — the interrupt now lands as an `InterruptedException` mid-sleep. Handle it the well-behaved way (restore the flag, exit the loop, no empty catch) and print how the worker learned it was time to stop in each of the two cases.
