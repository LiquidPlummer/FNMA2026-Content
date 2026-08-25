# Synchronization

The broken counter from last lesson — two threads, twenty thousand increments, a wrong and different answer every run — is the emblem of shared mutable state. **Synchronization** is Java's toolkit for making such code correct: mechanisms that impose order on interleaving threads. This lesson covers the two core problems (atomicity and visibility), the `synchronized` keyword, and the modern alternatives that often beat it.

---

## The Two Problems

**Atomicity.** `counter++` compiles to read–modify–write. If thread B reads between A's read and write, both compute from the same stale value and one increment evaporates — a **race condition**: correctness depending on scheduling luck. Any *check-then-act* (`if (!map.containsKey(k)) map.put(k, v)`) or *read-modify-write* sequence on shared data races the same way.

**Visibility.** Even single writes aren't automatically seen. Threads may cache values in registers and CPU caches; without a *happens-before* relationship, a write by one thread can remain invisible to another indefinitely — the infamous `while (!stopped) {}` loop that never notices `stopped = true`. Correct concurrency needs both: operations that don't interleave destructively *and* writes that publish.

---

## synchronized: Mutual Exclusion

The **`synchronized`** keyword guards code with a **lock** (every Java object has one built in). Only one thread may hold a given lock at a time; others block at the boundary until it's released:

```java
public class Counter {
    private int count = 0;

    public synchronized void increment() {     // lock: this — one thread inside at a time
        count++;
    }

    public synchronized int get() {            // reads of shared state need the lock too
        return count;
    }
}
```

*A synchronized method: acquire the object's lock, run, release — increments can no longer interleave.*

With both threads calling `increment()` on one `Counter`, the answer is 20,000, every run. `synchronized` solves *both* problems at once: mutual exclusion gives atomicity, and the lock hand-off publishes all writes made inside the block (releasing a lock happens-before the next acquisition of it).

The block form narrows the critical section and chooses the lock object explicitly — good practice, since it hides the lock from outside code:

```java
private final Object lock = new Object();

public void record(Sale sale) {
    validate(sale);                            // no shared state — outside the lock
    synchronized (lock) {
        total += sale.amount();                // only the shared mutation is guarded
        count++;
    }
}
```

*A private lock object and a minimal critical section: hold locks briefly, over the shared state only.*

The rules that make locking correct: **every access** (writes *and* reads) to the guarded state goes through **the same lock** — one unsynchronized read ruins the guarantee; keep critical sections **small** (locks serialize threads — coarse locking is a scalability tax); and acquire multiple locks in a **consistent global order**, because two threads acquiring two locks in opposite orders is a **deadlock** — each holding what the other awaits, both frozen forever.

---

## volatile: Visibility Only

A **`volatile`** field guarantees visibility — every read sees the latest write — with no locking and *no atomicity*:

```java
private volatile boolean stopped = false;      // flag written by one thread, read by another

public void run() {
    while (!stopped) { doWork(); }             // guaranteed to notice the change
}
public void stop() { stopped = true; }
```

*The legitimate `volatile` use case: a status flag with one writer and simple reads.*

`volatile` fixes the never-stopping loop; it does **not** fix the counter (`count++` still isn't atomic, volatile or not). Its niche is narrow — flags and safely-published references. When in doubt, it's the wrong tool.

---

## The java.util.concurrent Toolkit

Modern Java code mostly *avoids writing* `synchronized` by using classes that package the correctness:

```java
// Atomic variables: lock-free read-modify-write
AtomicInteger counter = new AtomicInteger();
counter.incrementAndGet();                     // atomic ++ — the counter problem, solved in one line

// Concurrent collections: internally synchronized, highly scalable
Map<String, Integer> hits = new ConcurrentHashMap<>();
hits.merge("/home", 1, Integer::sum);          // atomic check-then-act, no external lock
```

*The `java.util.concurrent` answer: `AtomicInteger` for counters, `ConcurrentHashMap` for shared maps — correctness built in.*

The working hierarchy for shared state, best option first:

1. **Don't share** — thread-local data, or values passed between threads via executors' results (next lesson).
2. **Share immutably** — `final` fields, records, `List.copyOf` snapshots: immutable objects (the Strings lesson's theme) are thread-safe *by definition*, no locks required.
3. **Share through concurrent utilities** — atomics, `ConcurrentHashMap`, `BlockingQueue`.
4. **Lock** — `synchronized` (or `ReentrantLock` for advanced needs: timeouts, fairness), correctly and minimally, as the last resort.

That ordering is the real lesson: synchronization is the tool we escalate *to*, not the default. Most well-designed concurrent code has remarkably few locks in it — largely because task-based designs, up next with executors, keep data confined to tasks and pass results instead of sharing variables.
