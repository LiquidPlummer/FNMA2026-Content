# Lab: Wrappers & Parsing — turning typed text into numbers, safely

In this lab we build a small console app, the **Score Sheet Reader**, that turns scores a user types in as text into real numbers. Along the way we'll parse with `Integer.parseInt` (and watch it reject bad input), let autoboxing carry `int`s into a `List<Integer>` without us writing a single wrapper constructor, reproduce the null-unboxing `NullPointerException` *on purpose* and then fix it, and finish with the sharpest pitfall in the wrapper classes: `==` that appears to work and then doesn't. By the end you'll have typed every part of the reader yourself.

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

You should see `=== Score Sheet Reader ===` followed immediately by `Done.`. That's the starter skeleton in [src/main/java/com/curriculum/labs/ScoreSheetLab.java](src/main/java/com/curriculum/labs/ScoreSheetLab.java) — open it now and find the numbered `[MARKER]` comments. Each part of the walkthrough below tells you which marker to work at. Re-run the command above after every part to see your progress — this app reads from the console, so have a number ready to type each time it prompts you.

---

## Part 1 — `Integer.parseInt`, and what happens with bad input

Every value the user types at a console is text — a `String` — even when it looks like a number. `Integer.parseInt` is how we turn that text into an actual `int`. At **`[MARKER 1]`**, we'll add:

```java
System.out.print("Enter the first score: ");
int firstScore = Integer.parseInt(scanner.nextLine().trim());
System.out.println("Parsed score: " + firstScore);
```

*Read a line of text, parse it into an `int`, and print the result.*

Run it and type `88` — you'll see `Parsed score: 88`. Now run it a second time and type something that isn't a number, like `abc`. The program crashes: `Integer.parseInt` throws a `NumberFormatException` — *For input string: "abc"* — and because nothing catches it, it propagates all the way up and takes the program down with it. Read the error Maven prints; the message names the exact string that failed to parse.

Don't fix this yet — leave it as-is and re-run one more time with a real number so we can keep building. The habit of validating input *before* parsing it is exactly what the first exercise below asks you to build. For now, the lesson is simpler: **`parseInt` throws on anything that isn't a valid integer, unconditionally.**

---

## Part 2 — Autoboxing: primitives flowing into a collection

`ArrayList<Integer>` can only hold objects — a raw `int` can't go in directly. At **`[MARKER 2]`**, we'll add:

```java
List<Integer> scores = new ArrayList<>();
System.out.print("Enter a score to add to the sheet: ");
int nextScore = Integer.parseInt(scanner.nextLine().trim());
scores.add(nextScore);
System.out.print("Enter another score to add: ");
int anotherScore = Integer.parseInt(scanner.nextLine().trim());
scores.add(anotherScore);
System.out.println("Score sheet so far: " + scores);
```

*Two more parsed scores, added to a `List<Integer>`.*

Run it and enter two numbers, say `95` and `76` — you'll see `Score sheet so far: [95, 76]`. Look at `scores.add(nextScore)`: `nextScore` is a plain `int`, but `add` expects an `Integer`. We never wrote a wrapper conversion — the compiler inserted one for us. That's **autoboxing**: a primitive silently becomes its wrapper the moment an object is expected. It happens so smoothly that most Java code never mentions it explicitly, which is exactly why the two pitfalls below are worth knowing on purpose.

---

## Part 3 — Reproducing the null-unboxing crash, then fixing it

A wrapper variable, unlike a primitive, can be `null` — and a map lookup for a key that isn't there is the classic way one shows up. At **`[MARKER 3]`**, we'll add:

```java
Map<String, Integer> scoreByStudent = new HashMap<>();
scoreByStudent.put("Riley", 92);
scoreByStudent.put("Sam", 78);

Integer danaScore = scoreByStudent.get("Dana");   // no entry for Dana — returns null
int danaScoreValue = danaScore;                   // unboxing null
System.out.println("Dana's score: " + danaScoreValue);
```

*A lookup that misses, and an unboxing that doesn't check for it.*

Run it. `scoreByStudent.get("Dana")` returns `null` — there's no entry for Dana — and the very next line tries to unbox that `null` into a primitive `int`. That's a `NullPointerException`, and Java's error message is specific about it: it names the exact call that failed on a `null` reference. This is the pitfall from the Wrapper Classes notes made real: unboxing doesn't ask whether the wrapper is `null` first, it just tries, and `null` has no `int` value to hand back.

Now we'll build the habit the notes recommend — check before you unbox. Replace the last two lines (from `int danaScoreValue = danaScore;` down) with:

```java
if (danaScore == null) {
    System.out.println("Dana has no recorded score.");
} else {
    int danaScoreValue = danaScore;
    System.out.println("Dana's score: " + danaScoreValue);
}
```

*Check for `null` before unboxing — the crash becomes a handled case instead.*

Run it again: `Dana has no recorded score.`, no crash. Same missing data, but now the program decides what to do about it instead of the JVM deciding for us.

---

## Part 4 — The `==` cache trap: predict, run, explain

This is the sharpest pitfall in the wrapper classes, because it *looks* like it works. At **`[MARKER 4]`**, we'll add:

```java
Integer a = 100;
Integer b = 100;
System.out.println("100 == 100 (boxed): " + (a == b));
```

*Two `Integer`s, both boxing the value `100`, compared with `==`.*

Before you run it — predict the answer. `Integer` is a class, so `==` compares *references*, not values — does that mean this prints `false`? Run it. It prints `true`. Now add the rest of Part 4, right below:

```java
Integer c = 1000;
Integer d = 1000;
System.out.println("1000 == 1000 (boxed): " + (c == d));
System.out.println("1000 .equals 1000 (boxed): " + c.equals(d));
```

*The same comparison, with bigger numbers — plus the correct way to compare them.*

Predict again before running. Same pattern as `a`/`b`, so shouldn't `c == d` also be `true`? Run it: it's `false`. `c.equals(d)` — printed right after — is `true`.

Here's the explanation the notes give: Java pre-creates `Integer` objects for every value from `-128` to `127` and hands out the *same object* whenever code boxes a value in that range — which is why `a == b` was `true`, by coincidence of both being `100`. Once the value leaves that cached range, boxing creates two genuinely separate objects, and `==` correctly reports they're different objects, even though they hold the same value. **The rule that never fails: compare wrappers with `.equals()`, not `==`.** Code that only ever gets tested with small numbers can pass every test and still carry this bug straight into production.

---

## Exercises

Same underlying ideas, new problems, no markers this time.

1. **A lenient parser.** Write a method `parseScoreOrDefault(String raw, int fallback)` that returns the parsed number when `raw` is a valid non-negative integer, and returns `fallback` otherwise — without ever letting the program crash. Don't reach for exception handling (that's a later topic); instead, validate the string yourself before parsing, character by character, using `Character.isDigit(...)`. Decide what counts as "not a valid integer" (an empty string? one with a letter in it? one that's just a minus sign?) and make sure your method handles each case you decide on.

2. **Min, max, average.** Given a fixed list of raw score strings — some clean, and deliberately include one or two that your `parseScoreOrDefault` from exercise 1 would treat as invalid — parse them all, then find the minimum, the maximum, and the average of the valid scores. (You'll need to loop over the parsed values to do this; a `for-each` loop, one line per element, is the natural fit — we'll cover loops properly in Control Flow, but `for (int score : scores)` reads almost like English: "for each score in scores.")

3. **Find the bug.** You're handed this snippet from a teammate's code, along with the claim "it works, I tested it":

   ```java
   Integer previousAttempts = getPreviousAttempts(studentId);   // returns an Integer
   Integer currentAttempts = getCurrentAttempts(studentId);     // returns an Integer

   if (previousAttempts == currentAttempts) {
       System.out.println("No new attempts recorded.");
   }
   ```

   Explain, in your own words, exactly which values would make this bug invisible in a small test and exactly which values would make it fail in production — then rewrite the condition so it's correct regardless of the numbers involved.
