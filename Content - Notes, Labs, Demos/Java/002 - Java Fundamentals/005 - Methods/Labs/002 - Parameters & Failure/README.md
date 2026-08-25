# Lab: Methods — Parameters & Failure

We'll build `Validator`, a small utility class we exercise straight from `main`, assert-style: call a method, print what we expected right next to what we actually got. Along the way we'll cover **pass-by-value** (what really gets copied when you call a method with a primitive vs. an array), **varargs** (a method that accepts any number of arguments), and **fail-fast validation** (rejecting bad input with `throw`, the `throw`/`throws` distinction, and reading a stack trace).

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

Verify both with `java -version` and `mvn -version`. Then, from this lab's folder (the one containing `pom.xml`), compile and run:

```console
mvn -q compile exec:java
```

You should see `=== Validator Lab ===` followed immediately by `Done.`. That's the starter skeleton in [src/main/java/com/curriculum/labs/Validator.java](src/main/java/com/curriculum/labs/Validator.java) — open it now and find the numbered `[MARKER]` comments. Each part below tells you which marker to work at. Re-run the command above after every part to check your progress.

---

## Part 1 — Pass-by-value: primitives vs. arrays

Java only ever passes **copies**. For a primitive, that copy is the value itself, so reassigning it inside a method can never reach back to the caller. For an array, the copy is a **reference** — both method and caller end up pointing at the *same* array object, so mutating an element is visible to the caller, while reassigning the parameter to a brand-new array is not.

At **`[MARKER 4]`**, below `main`, we'll add three methods:

```java
static void bumpPrimitive(int n) {
    n = n + 1;
}

static void bumpFirstElement(int[] arr) {
    arr[0] = arr[0] + 1;
}

static void replaceArray(int[] arr) {
    arr = new int[] { 99, 99, 99 };
}
```

*`bumpPrimitive` reassigns its own local copy of the `int` — nothing outside the method can see it. `bumpFirstElement` reaches through the copied reference and mutates the shared array's first slot — the caller sees that. `replaceArray` reassigns its local copy of the reference to point at a brand-new array — the caller's variable still points at the original.*

Also add this import at the top of the file, just below `package com.curriculum.labs;`, so we can print arrays readably:

```java
import java.util.Arrays;
```

Now, at **`[MARKER 1]`** in `main`, we'll call all three and compare expected vs. actual:

```java
int count = 10;
bumpPrimitive(count);
System.out.println("Expected 10, got " + count);

int[] scores = { 1, 2, 3 };
bumpFirstElement(scores);
System.out.println("Expected [2, 2, 3], got " + Arrays.toString(scores));

replaceArray(scores);
System.out.println("Expected [2, 2, 3] (unchanged), got " + Arrays.toString(scores));
```

Run it. `count` is still `10` — `bumpPrimitive` only ever touched a copy. `scores` becomes `[2, 2, 3]` after `bumpFirstElement` — that method mutated the one array both it and `main` were looking at. And `scores` is **still** `[2, 2, 3]` after `replaceArray` — that method built a whole new array and pointed its own local `arr` at it, but `main`'s `scores` variable was never touched. Same mechanism (pass-by-value) explains all three lines; the difference is entirely in what gets *copied* — a value, versus a reference to a shared object.

---

## Part 2 — Varargs: a method that takes any number of arguments

Sometimes we don't know in advance how many arguments a caller will have on hand. A method's *last* parameter can be declared with `...` to accept zero or more values of that type — inside the method, it's just an array.

At **`[MARKER 5]`**, we'll add:

```java
static int sum(int... numbers) {
    int total = 0;
    for (int n : numbers) {
        total += n;
    }
    return total;
}
```

*`numbers` behaves like an `int[]` inside the method; callers can pass any number of `int` arguments, including zero, or hand in an existing array directly.*

At **`[MARKER 2]`** in `main`, we'll call it a few different ways:

```java
System.out.println("Expected 0, got " + sum());
System.out.println("Expected 3, got " + sum(1, 2));
System.out.println("Expected 15, got " + sum(1, 2, 3, 4, 5));

int[] existing = { 10, 20, 30 };
System.out.println("Expected 60, got " + sum(existing));
```

Run it. Zero arguments is legal and gives `0`; any number of `int` arguments works without a single overload being written for each count; and an existing array passes straight through unchanged, because a varargs parameter *is* an array once inside the method.

---

## Part 3 — Fail-fast validation: `throw`, `throws`, and stack traces

A method that's handed input it can't work with shouldn't guess or return garbage — it should fail immediately and say why. That's `throw`: it hurls an exception object and ends the method on the spot.

At **`[MARKER 6]`**, we'll add:

```java
static double average(int[] numbers) {
    if (numbers.length == 0) {
        throw new IllegalArgumentException(
            "average requires at least one number, got an empty array");
    }
    int total = 0;
    for (int n : numbers) {
        total += n;
    }
    return total / (double) numbers.length;
}
```

*Guard clause first: an empty array has no average, so we `throw` an `IllegalArgumentException` with a message naming the problem, rather than returning something misleading like `0`. `IllegalArgumentException` is unchecked — nothing in the method's signature has to announce it.*

Not every failure is the caller's fault, though. `Thread.sleep(long)`, part of the JDK, can be interrupted by another thread while it's paused — a *checked* exception, `InterruptedException`, that the compiler forces every caller to either catch or **declare** with a `throws` clause. At **`[MARKER 7]`**, we'll add:

```java
static void pauseBriefly() throws InterruptedException {
    Thread.sleep(200);
}
```

*`throws InterruptedException` is a declaration, not an action — it warns callers "this might fail this way," so they're forced to deal with it too, one level up. Compare the spelling: `throw` (no `s`) is the statement that fails right now, inside `average`; `throws` (with `s`) is the signature clause `pauseBriefly` uses to pass the possibility of failure up to its caller.*

Because `pauseBriefly` declares a checked exception, `main` must handle it or declare it onward too. Update `main`'s signature, right at the top of the file:

```java
public static void main(String[] args) throws InterruptedException {
```

Now, at **`[MARKER 3]`** in `main`, we'll call all of this, ending with the call that's meant to crash the program:

```java
int[] passing = { 70, 80, 90 };
System.out.println("Expected 80.0, got " + average(passing));

pauseBriefly();
System.out.println("Paused for a moment without crashing.");

int[] empty = {};
System.out.println("Calling average(empty) — expect this to end the program:");
average(empty);
```

Run it. The first three lines behave as expected — a correct average, then a couple hundred milliseconds later, confirmation that the pause didn't blow anything up. Then `average(empty)` throws, and the program dies without ever printing `"Done."`. Read the stack trace that gets printed **top to bottom**: the first line names the exception class and our message, and the lines under `at ...` trace the call backward — `Validator.average`, then `Validator.main`, showing exactly which line in which method threw and exactly which line called it. That trace *is* the debugging information the fail-fast throw was designed to hand you.

---

## Exercises

Same ideas, new problems, no markers to follow.

1. **A defensive `average` that rejects the empty case, cleanly.** You've already built this in the guided part — now write a second one from scratch: `medianOf(int[] numbers)`, which returns the middle value of a *sorted copy* of the array (for an even-length array, average the two middle values). Guard it exactly like `average`: an empty array should `throw new IllegalArgumentException(...)` with a message that names the problem, not a `NullPointerException` or `ArrayIndexOutOfBoundsException` three lines into some sorting logic.

2. **A method that must not mutate its argument.** Write `int[] doubled(int[] numbers)` that returns a **new** array containing each element multiplied by two, leaving the array the caller passed in completely untouched. Prove it in `main`: build an array, call `doubled` on it, and print *both* arrays afterward to show the original is unchanged. (Watch the trap `replaceArray` from Part 1 illustrated — it's easy to accidentally mutate the input in place instead of building a new one.)

3. **Write the error message.** Write `int factorial(int n)`, which should reject negative input. Don't just throw `new IllegalArgumentException()` with no message, and don't write something generic like `"bad input"` — write a message that would let a teammate who has never seen this code fix their bug in under ten seconds without opening a debugger, using the actionable-message examples from the notes as your bar (name what was expected *and* what arrived). Call it with a negative number and read your own stack trace as if you were that teammate — does it actually tell you enough?
