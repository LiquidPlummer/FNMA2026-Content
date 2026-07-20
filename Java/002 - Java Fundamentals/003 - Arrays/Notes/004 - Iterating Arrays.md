# Iterating Arrays

Most array work is a loop: visit every element and do something with it. Java offers two loop forms for the job — the classic indexed `for` and the **for-each** loop — and choosing between them comes down to one question: does the body need the *index*, or only the *value*?

---

## The Indexed `for` Loop

The pattern we met in Hello World, driven by `length`:

```java
double[] temps = {21.5, 23.1, 19.8, 24.6};

for (int i = 0; i < temps.length; i++) {
    System.out.println("Hour " + i + ": " + temps[i]);
}
```

*The canonical indexed sweep: `i` runs 0 to length-1, and `temps[i]` reads each element.*

The index makes this the fully general form. It's the right tool whenever the loop needs to:

- **Modify elements** — `temps[i] = temps[i] * 1.8 + 32;` (writing needs a slot, not a copy)
- **Know positions** — printing rank, finding *where* a match lives
- **Skip or reorder** — every second element (`i += 2`), reverse order (`for (int i = temps.length - 1; i >= 0; i--)`)
- **Walk two arrays in step** — `names[i]` paired with `scores[i]`

---

## The For-Each Loop

When the body only reads each value once, front to back, the **for-each** loop (officially the *enhanced for*) drops the index ceremony:

```java
double sum = 0;
for (double t : temps) {
    sum += t;
}
double average = sum / temps.length;
```

*Read it as "for each `t` in `temps`" — no index variable, no bounds condition to get wrong.*

The loop variable (`t`) receives a **copy** of each element in turn. That's the crucial limitation: assigning to it changes the copy, not the array —

```java
for (double t : temps) {
    t = 0;             // pointless — the array is untouched
}
```

*For-each hands out copies; "writing" to the loop variable modifies nothing.*

(One nuance for object arrays: the copy is a copy of the *reference*, so calling a mutating method on it — `sb.append("!")` — does affect the shared object. Replacing the element still requires an index.)

For-each also works on collections and anything `Iterable`, which makes it the single most-used loop form in Java code — the mechanics of *how* it iterates come up again with Iterators in the Collections topic.

**Choosing is mechanical:** read-only, whole array, don't care about positions → for-each. Anything else → indexed `for`.

---

## Printing an Array

`println(array)` prints a reference stamp, not contents — the fix is `Arrays.toString` (and `deepToString` for nested arrays, next lesson):

```java
int[] data = {4, 8, 15};
System.out.println(data);                    // [I@7ad041f3 — not useful
System.out.println(Arrays.toString(data));   // [4, 8, 15]
```

*`Arrays.toString` is the idiomatic way to see an array's contents.*

---

## A Glimpse Ahead: Streams

Java also has a functional style of iteration, where the "loop" is a method call:

```java
double average = Arrays.stream(temps).average().orElse(0);
int max = Arrays.stream(scores).max().getAsInt();
```

*Streams express the goal (average, max) rather than the mechanics of looping.*

Streams get full treatment in the Functional Programming topic — mentioned here only so this reads as familiar when it appears in real codebases. For now, the two `for` forms cover everything.

---

## Common Iteration Recipes

The handful of patterns that account for most array loops in practice:

```java
int[] scores = {88, 92, 79, 95, 84};

// Aggregate: sum, count, average
int sum = 0;
for (int s : scores) sum += s;

// Search: find the first index matching a condition
int found = -1;
for (int i = 0; i < scores.length; i++) {
    if (scores[i] > 90) { found = i; break; }
}

// Max/min: track the best so far
int max = scores[0];
for (int s : scores) {
    if (s > max) max = s;
}

// Transform in place
for (int i = 0; i < scores.length; i++) {
    scores[i] += 5;
}
```

*Aggregate, search, extremum, transform — four recipes that cover the daily workload.*

Note the shapes: the read-only recipes use for-each; the ones needing a position or a write use the index. The `break` in the search recipe exits the loop as soon as the answer is known — loop control statements get proper coverage in the Control Flow topic next.
