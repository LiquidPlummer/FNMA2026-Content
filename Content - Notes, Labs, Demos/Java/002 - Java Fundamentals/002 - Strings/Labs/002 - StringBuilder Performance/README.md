# Lab: StringBuilder Performance — why mutable buffers exist

In this lab we build the same 100,000-line report two ways — once with plain `String` concatenation, once with `StringBuilder` — and time both. The gap between the two numbers *is* the lesson: no amount of explanation makes the point as well as watching it happen on your own machine. We close with `StringBuilder`'s fluent, in-place API: `insert`, `reverse`, and `setLength`.

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

You should see `=== Builder Performance Lab ===` followed by `Done.`. That's the starter skeleton in [src/main/java/com/curriculum/labs/BuilderPerformanceLab.java](src/main/java/com/curriculum/labs/BuilderPerformanceLab.java) — open it now and find the numbered `[MARKER]` comments. Each part below tells you which marker to work at. Re-run the command above after every part.

One heads-up: once Part 1 is in place, `exec:java` will visibly pause for a moment before printing — that's 100,000 loop iterations of string concatenation actually happening, not a hang. That pause is the whole point of this lab.

---

## Part 1 — Concatenation in a loop, timed

`LINE_COUNT` (100,000) is already declared as a field at the top of the class. We'll build a report by appending one line at a time with `+=`, and wrap the work in a stopwatch made from `System.currentTimeMillis()`.

At **`[MARKER 1]`** in `main`, we'll add:

```java
long startConcat = System.currentTimeMillis();
String report = "";
for (int i = 1; i <= LINE_COUNT; i++) {
    report += "Line " + i + "\n";
}
long concatMillis = System.currentTimeMillis() - startConcat;
System.out.println("Concatenation: " + report.length() + " characters in " + concatMillis + " ms");
```

*`System.currentTimeMillis()` returns the current time in milliseconds; we read it before and after the loop and subtract to get elapsed time. Each `+=` inside the loop discards the old `report` and allocates a brand-new, slightly-longer `String`.*

Run it. Note the character count (should be a bit over 700,000) and, more importantly, the millisecond figure. On most machines 100,000 lines is already slow enough to notice — and it gets worse than linearly with size, because every `+=` recopies everything accumulated *so far*. That's the n² behavior from the notes: each of the 100,000 iterations pays for copying an ever-growing string, not just adding one line.

---

## Part 2 — The same report, with `StringBuilder`

Now we rebuild the identical report using `StringBuilder`'s `append`, which writes into an internal buffer instead of allocating a new `String` every time.

At **`[MARKER 2]`** in `main`, we'll add:

```java
long startBuilder = System.currentTimeMillis();
StringBuilder builder = new StringBuilder();
for (int i = 1; i <= LINE_COUNT; i++) {
    builder.append("Line ").append(i).append("\n");
}
String builtReport = builder.toString();
long builderMillis = System.currentTimeMillis() - startBuilder;
System.out.println("StringBuilder: " + builtReport.length() + " characters in " + builderMillis + " ms");
```

*Same loop, same 100,000 lines, same final content — but `append` chains (it returns the builder itself) and edits one buffer in place. `toString()` produces the one and only `String` at the very end.*

Run it and put the two millisecond numbers side by side. They should be close in character count (both reports are the same content) and wildly apart in time — the `StringBuilder` version should be many times faster, often dramatically so. That difference, at this size, is the entire argument for `StringBuilder`: not "slightly nicer syntax," but "the other one doesn't scale." If you double `LINE_COUNT` to 200,000, watch what happens to each number — the concatenation time won't just double.

---

## Part 3 — The fluent, in-place API

`StringBuilder` isn't only for loops — its methods edit the buffer directly and chain together, which is worth getting a feel for on a small example before the exercises.

At **`[MARKER 3]`** in `main`, we'll add:

```java
StringBuilder sb = new StringBuilder("Hello world");
sb.append("!");
sb.insert(5, ",");
System.out.println(sb);

sb.reverse();
System.out.println(sb);

sb.reverse();
sb.setLength(5);
System.out.println(sb);
```

*`append` adds to the end, `insert(5, ",")` slides a comma in at index 5, `reverse()` flips the whole buffer in place, and `setLength(5)` truncates the buffer down to its first 5 characters — all without allocating a new `String` at each step.*

Run it and read the three printed lines: `Hello, world!`, then its mirror image reversed, then back to normal and cut down to just `Hello`. Every one of those is the *same* `StringBuilder` object being mutated — contrast that with Part 1's `report`, which was a new `String` object every single iteration. That contrast is the whole lesson of this lab in miniature: mutable buffer while building, `toString()` when you're done.

---

## Exercises

The training wheels come off — same concepts, new problems, no step-by-step.

1. **Receipt builder.** Given a small array of `{name, price}` line items, use a single `StringBuilder` to build a multi-line receipt (one line per item, a blank separator line, then a total). Use `append` throughout — no `+` concatenation allowed in the loop.

2. **Reverse the words, not the letters.** `StringBuilder.reverse()` flips *characters*, so `reverse()` on `"one two three"` gives `"eerht owt eno"` — not what we want. Given a sentence, produce it with the *words* in reverse order but each word's letters intact: `"three two one"`. (Think about what has to happen between splitting the sentence apart and gluing it back together.)

3. **Which tool?** Fill in the tool you'd reach for for each scenario below, and write one sentence justifying each choice: `String` + `+`, `StringBuilder`, `String.format`, or `String.join`.

   | Scenario | Tool | Why |
   |---|---|---|
   | Printing one log line combining a timestamp, a level, and a message | | |
   | Building an HTML page body from 5,000 row fragments in a loop | | |
   | Combining a first and last name into a display name, once | | |
   | Assembling a comma-separated list from a known array of category names | | |
   | Accumulating a running transcript inside a chat loop that could run for hours | | |
