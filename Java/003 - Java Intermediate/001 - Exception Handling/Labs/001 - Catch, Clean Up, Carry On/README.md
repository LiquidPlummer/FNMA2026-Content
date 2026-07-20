# Lab: Catch, Clean Up, Carry On

In this lab we harden a fragile file importer with Java's exception-handling toolkit: targeted `try`/`catch`, catch ordering, multi-catch, and try-with-resources. The starter program reads a deliberately dirty data file and dies on the first bad row — by the end it survives every bad row, reports them honestly, and closes its resources no matter what. There's also one bug already hiding in the starter; we hunt it in Part 5.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

From this lab's folder (the one containing `pom.xml` and `records.txt`):

```console
mvn -q compile exec:java
```

The data file lives at the project root, so always run from there. Open [src/main/java/com/curriculum/labs/ImporterLab.java](src/main/java/com/curriculum/labs/ImporterLab.java) and [records.txt](records.txt) side by side before starting.

---

## Part 1 — Run it and read the wreckage

Run the command above. The program prints two rows, then dies. Before fixing anything, we read the stack trace like professionals — top line first:

```
Exception in thread "main" java.lang.NumberFormatException: For input string: "three"
    at java.base/java.lang.Integer.parseInt(...)
    at com.curriculum.labs.ImporterLab.main(ImporterLab.java:31)
```

*The trace names the exception, the offending input, and the exact line in our code that triggered it.*

Cross-reference with `records.txt`: row 3 has `three` where a quantity belongs. One bad row killed the whole import — including the four good rows after it. That's the problem this lab fixes: **failure of one row shouldn't be failure of the job.**

---

## Part 2 — Catch the parse failure, skip the row

At **`[MARKER 1]`**, we'll wrap the parsing block in a `try`/`catch` so a bad row is *recorded and skipped* instead of fatal. Wrap everything from `String[] fields = ...` down to `total += lineValue(...)` like this, and add a `skipped` counter next to `imported`:

```java
try {
    String[] fields = line.split(",");
    String name = fields[0];
    int quantity = Integer.parseInt(fields[1]);
    String priceText = fields[2];
    System.out.printf("  %-10s x%-3d @ %s%n", name, quantity, priceText);
    imported++;
    total += lineValue(quantity, priceText);
} catch (NumberFormatException e) {
    skipped++;
    System.out.println("  SKIPPED (bad number): " + line);
}
```

*A targeted catch: the specific failure we expect, handled with a count and an honest message — the loop carries on to the next row.*

Also print `skipped` in the summary. Run again: `gizmo` and the blank line get skipped... and then the program still dies — on `doohickey`, with a different exception (`ArrayIndexOutOfBoundsException`: the row has no third field). Progress, and a lesson: **a catch block only catches what it names.**

Before fixing that properly, one experiment. Change the catch to `catch (Exception e)` temporarily, then add a *second* catch block below it for `NumberFormatException`. The compiler refuses:

```
error: exception NumberFormatException has already been caught
```

*Catch blocks are checked top to bottom — a general type above a specific one makes the specific one unreachable, and Java rejects it at compile time.*

Undo the experiment (back to the single `NumberFormatException` catch). Specific first, general last — the compiler just proved why the ordering rule exists.

---

## Part 3 — Multi-catch: same recovery, several failures

The missing-field failure deserves the same treatment as the bad number: skip and count. Since the *recovery is identical*, we don't need two blocks — we widen the catch with `|`:

```java
} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
    skipped++;
    System.out.println("  SKIPPED (" + e.getClass().getSimpleName() + "): " + line);
}
```

*Multi-catch: one handler for two exception types — the message now names which failure occurred.*

Run it. The importer now processes the whole file: good rows imported, bad rows skipped and labeled. Check the summary line — how many imported, how many skipped? (Keep that "Total inventory value" number in mind. We'll come back to it.)

---

## Part 4 — Try-with-resources: the file always closes

`Files.readAllLines` slurps the file in one call, so there's nothing to close — but real importers stream large files through a reader, and a reader is a **resource** that must be closed even when things go wrong. We'll convert to that shape. At **`[MARKER 2]`**, replace the `readAllLines` line and the for-loop header:

```java
try (var reader = Files.newBufferedReader(Path.of("records.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        // ... the existing try/catch parsing block moves inside, unchanged ...
    }
}
```

*Try-with-resources: the reader is declared in the header, and Java guarantees `close()` runs — normal exit or exception, no `finally` required.*

(The summary printing stays after the block; `imported`, `skipped`, and `total` are declared before it — scope rules doing their job.) Run it — same output as Part 3.

Now prove the guarantee. Temporarily add `if (line.contains("sprocket")) throw new IllegalStateException("mid-file failure!");` inside the loop, and a `System.out.println("closing reader")` — wait, we can't print from `close()`... but we can *observe* it another way: change the resource line temporarily to:

```java
try (var reader = Files.newBufferedReader(Path.of("records.txt"));
     var probe = new java.io.Closeable() {
         public void close() { System.out.println("[probe] resources closed"); }
     }) {
```

*A second throwaway resource whose only job is announcing when closing happens.*

Run with the mid-file throw in place: the exception flies, and `[probe] resources closed` prints anyway — *before* the stack trace. Cleanup ran during the failure. Remove both temporary additions once you've seen it.

---

## Part 5 — The bug that was always there

Since Part 3, every row is accounted for: imported or skipped, nothing silent. Except... check the math. With the file fully processing, the summary says 5 rows imported. Compute the total by hand from `records.txt` for those five rows — widget, gadget, sprocket, flange, cog:

`10×2.50 + 5×10.00 + 12×1.75 + 7×?? + 20×0.60`

The program says `108.00`. The hand math says flange contributed *nothing* — yet flange was **imported**, not skipped. Something ate a failure silently.

Find it. (Hint: only one method in the starter was written before our hardening campaign — read `lineValue` with Part 2's eyes, then look very closely at flange's price in `records.txt`.)

This is the **swallowed exception** anti-pattern from the notes: `catch (Exception e) { return 0.0; }` converted a loud, diagnosable failure into a silently wrong total. Fix it your way — the cleanest is letting `lineValue` throw (delete the try/catch inside it) so the row lands in the existing skip-handler, honestly counted. Re-run: flange now reports as skipped, and the total is correct for what was actually imported. Fix the data file's typo last, and watch everything reconcile.

---

## Exercises

1. **Three tries, then give up.** Write `readWithRetry(String path)`: it attempts the file read up to 3 times, catching `IOException`; on the third failure it rethrows. Prove both paths (temporarily point it at a nonexistent file for the failure path).

2. **Wrap and translate.** Write `loadRecords(String path)` that catches `IOException` and rethrows it as an unchecked `ImportException` (you define it — one constructor taking a message and a cause). Trigger it with a missing file and confirm the printed stack trace shows **both** exceptions, `Caused by:` included.

3. **Review set.** For each of the following, state what's wrong and what you'd change — one sentence each:
   a. `catch (Exception e) { }`
   b. `catch (Exception e) { e.printStackTrace(); }` followed by code that uses the half-initialized result anyway
   c. `catch (Exception e) { throw new RuntimeException("error"); }` (look carefully at what's *missing*)
   d. A `catch (IOException e)` block placed above `catch (FileNotFoundException e)`

4. **Audit trail.** Add a `finally` block (not try-with-resources this time — the point is to write one by hand) that prints the summary line *even when the file itself can't be opened*. Decide what the summary should honestly say in that case.
