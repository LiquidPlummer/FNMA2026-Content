# Lab: Array Fundamentals — declaring, indexing, length, and iteration

In this lab we build a small console app, **Quiz Statistics**, that picks up where the Loops lab left off — same roster, same `-1`-means-absent convention, now stored properly in arrays. Along the way we declare and literal-initialize arrays, read and write elements by index (and deliberately blow past the end of one, so we can read the exception Java gives us), lean on `length` to write code that survives any array size, and then run through the four iteration recipes — aggregate, search, max, and transform — choosing between indexed `for` and `for-each` on purpose each time.

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

You should see `=== Quiz Statistics ===` followed by `Done.`. That's the starter skeleton in [src/main/java/com/curriculum/labs/ArrayFundamentalsLab.java](src/main/java/com/curriculum/labs/ArrayFundamentalsLab.java) — open it now and find the numbered `[MARKER]` comments. Each part below tells you which marker to work at. Re-run the command above after every part to see your progress.

---

## Part 1 — Declaring & initializing: a parallel array

The class already has `QUIZ_SCORES`, an `int[]` declared with an array literal. We'll give it a partner: a `String[]` of the students those scores belong to, declared the same way.

At **`[MARKER 1]`**, we'll add:

```java
String[] studentNames = {"Ava", "Ben", "Cleo", "Dmitri", "Elena", "Farid", "Grace", "Hana"};
System.out.println("Roster: " + Arrays.toString(studentNames));
System.out.println("Scores: " + Arrays.toString(QUIZ_SCORES));
```

*An array literal declares, sizes, and fills `studentNames` in one line — no `new`, no explicit length. `Arrays.toString` gives us readable output; plain `println(studentNames)` would print a reference stamp like `[Ljava.lang.String;@1b6d3586`.*

Run it. Two eight-element arrays, printed side by side. This is a **parallel array** setup: `studentNames[i]` and `QUIZ_SCORES[i]` describe the same student — index 2 is Cleo, and `QUIZ_SCORES[2]` is `-1`, her absence. Nothing enforces that link for us; it's on the two arrays being built and indexed together, consistently.

---

## Part 2 — Accessing elements: reads, writes, and a crash

Now we'll index into both arrays — and deliberately make a mistake, so we can see exactly what Java does about it.

At **`[MARKER 2]`**, we'll add:

```java
System.out.println("First student: " + studentNames[0]);
System.out.println("Last score: " + QUIZ_SCORES[8]);
```

*Reading `studentNames[0]` gets the first element. `QUIZ_SCORES[8]` is meant to get the last one — except the array only has 8 elements, at indexes 0 through 7.*

Run it. The first line prints fine, then the program dies:

```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 8 out of bounds for length 8
	at com.curriculum.labs.ArrayFundamentalsLab.main(ArrayFundamentalsLab.java:...)
```

Read that message carefully — it names the exact bad index (`8`) and the array's actual length (`8`), so you never have to guess where the overrun is. This is the classic off-by-one: the last valid index of a length-8 array is `7`, not `8`. Fix the line now:

```java
System.out.println("Last score: " + QUIZ_SCORES[7]);
```

Run it again — no crash. Now let's also do an index *write*. Cleo (index 2) was absent for the original quiz but made one up later. Add this beneath the fixed line:

```java
QUIZ_SCORES[2] = 68;
System.out.println("Updated scores: " + Arrays.toString(QUIZ_SCORES));
```

*Same bracket syntax, other side of the assignment — this replaces the `-1` at index 2 with her makeup score of 68. `QUIZ_SCORES` is declared `final`, but `final` locks the reference, not the contents; the array's elements are still writable.*

Run it once more and confirm index 2 now reads `68` instead of `-1`.

---

## Part 3 — Array length: idioms that survive any size

The `QUIZ_SCORES[7]` we just wrote works, but only because we happen to know the array has 8 elements today. `length` lets us stop hardcoding that number.

At **`[MARKER 3]`**, we'll add:

```java
int lastIndex = QUIZ_SCORES.length - 1;
System.out.println("Last score (by length): " + QUIZ_SCORES[lastIndex]);

int checkIndex = 8;
if (checkIndex >= 0 && checkIndex < QUIZ_SCORES.length) {
    System.out.println("Score at " + checkIndex + ": " + QUIZ_SCORES[checkIndex]);
} else {
    System.out.println(checkIndex + " is out of bounds for length " + QUIZ_SCORES.length);
}
```

*`length - 1` is the "last element" idiom — it's correct no matter how many scores end up in the array. The `if` is the guard-check idiom: testing `checkIndex` against `0` and `length` *before* touching the array turns a would-be crash into a clean, handled message instead.*

Run it. The first line reproduces Part 2's fix, but robustly this time. The guard check reports `8 is out of bounds for length 8` — the exact same fact the exception trace told us, except now the program keeps running instead of dying. Remember: `length` is a field, not a method — `QUIZ_SCORES.length`, no parentheses.

---

## Part 4 — Iterating: four recipes, one deliberate choice each

Most array work boils down to four jobs: sum something up, find something, find the best something, or change every element. Each has its natural loop — some want `for-each`'s "just give me the values," others want an indexed `for` because they need a position or a write. We'll write all four as separate methods below `main`, then call each one from `main` as we go.

### 4a — Aggregate: average score

At **`[MARKER 5]`**, below `main`, we'll add:

```java
static double averageScore(int[] scores) {
    int total = 0;
    int present = 0;
    for (int score : scores) {
        if (score != -1) {
            total += score;
            present++;
        }
    }
    return present == 0 ? 0 : total / (double) present;
}
```

*Summing every element and counting as we go is purely read-only, front-to-back work — exactly what `for-each` is for. We skip `-1` sentinels instead of counting them, and the cast to `double` avoids integer-division truncation.*

Back at **`[MARKER 4]`** in `main`, add the first call:

```java
System.out.println("Average score: " + averageScore(QUIZ_SCORES));
```

Run it and confirm the average.

### 4b — Search: first failing score

At **`[MARKER 6]`**, we'll add:

```java
static int indexOfFirstFailing(int[] scores) {
    for (int i = 0; i < scores.length; i++) {
        if (scores[i] != -1 && scores[i] < 70) {
            return i;
        }
    }
    return -1;
}
```

*A search needs to report a **position**, not just a value — that's the indexed `for`'s job. Returning as soon as we find a match (rather than a `break` plus a flag) is the same "stop looking, we have the answer" idea in method form.*

Add the second line at **`[MARKER 4]`**:

```java
int failingIndex = indexOfFirstFailing(QUIZ_SCORES);
if (failingIndex == -1) {
    System.out.println("No failing scores.");
} else {
    System.out.println("First failing score at index " + failingIndex + ": " + QUIZ_SCORES[failingIndex]);
}
```

Run it — with this roster, someone is below 70 (a passing grade), and you'll see which index.

### 4c — Max: highest score

At **`[MARKER 7]`**, we'll add:

```java
static int highestScore(int[] scores) {
    int max = scores[0];
    for (int score : scores) {
        if (score > max) {
            max = score;
        }
    }
    return max;
}
```

*Tracking "the best value seen so far" is read-only, front-to-back — `for-each` again. Note `-1` never wins this comparison against a real score, so absences don't need special-casing here the way they did in the average.*

Add the third line at **`[MARKER 4]`**:

```java
System.out.println("Highest score: " + highestScore(QUIZ_SCORES));
```

Run it and confirm the top score.

### 4d — Transform: curve every score

At **`[MARKER 8]`**, we'll add:

```java
static void curve(int[] scores, int points) {
    for (int i = 0; i < scores.length; i++) {
        if (scores[i] == -1) {
            continue;
        }
        scores[i] = scores[i] + points;
    }
}
```

*Writing a new value into each slot needs the index — a `for-each` loop variable is a **copy** of the element, so assigning to it changes nothing in the real array. `scores[i] = ...` is the only way to mutate in place.*

Add the last line at **`[MARKER 4]`**:

```java
curve(QUIZ_SCORES, 5);
System.out.println("After a 5-point curve: " + Arrays.toString(QUIZ_SCORES));
```

Run the whole program one more time. You should see the roster, the fixed index reads, the guarded bounds check, the average, the first failing score, the highest score, and finally every real score bumped up by 5 (absences left alone).

---

## Exercises

Same ideas, new problems, no markers to follow. Add each as its own method, call it from `main`, and decide the loop shape yourself before you start typing.

1. **Reverse in place.** Write a method that reverses an `int[]` by swapping elements from the outside in — no second array. Run it on a copy of `QUIZ_SCORES` and print the result with `Arrays.toString`. (A `for-each` can't do this one — be ready to say why.)

2. **Second-largest, without sorting.** Write a method that finds the second-highest value in an `int[]` in a single pass — no `Arrays.sort`. Think about what two things you need to track as you go, and what happens when the current element beats both of them.

3. **Histogram bucketing.** Bucket `QUIZ_SCORES` into five ranges — `0–59`, `60–69`, `70–79`, `80–89`, `90–100` — using a five-element `int[]` as your bucket counts, and print how many scores landed in each (skip absences). The bucket index for a score is computable with arithmetic; you shouldn't need five `if`/`else` branches.

4. **Append helper.** Arrays can't grow, but `Arrays.copyOf` can hand you a bigger one. Write `static int[] append(int[] arr, int value)` that returns a *new* array one element longer than `arr`, with `value` in the last slot. Confirm the original array passed in is unchanged after the call.
