# Lab: Primitives & Casting — declaring, converting, and taming the bits

In this lab we build a small console app, the **Unit Converter**, that reports on a backup file's size and a road-trip distance. Along the way we'll declare variables of several primitive types using every common literal form (underscores, suffixes, hex), watch integer division and integer overflow quietly produce wrong answers, fix the wrong answers with deliberate casts and `Math.round`, pull the magic numbers out into named `final` constants, and finish by deciding where `var` helps and where it hurts. By the end you'll have typed every part of the calculator yourself.

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

You should see `=== Unit Conversion Calculator ===` followed by `Done.`. That's the starter skeleton in [src/main/java/com/curriculum/labs/UnitConverterLab.java](src/main/java/com/curriculum/labs/UnitConverterLab.java) — open it now and find the numbered `[MARKER]` comments. Each part of the walkthrough below tells you which marker to work at. Re-run the command above after every part to see your progress.

---

## Part 1 — Declaring variables with literals

We'll start by describing the file we're reporting on. At **`[MARKER 1]`** in `main`, we'll add:

```java
long fileSizeBytes = 3_500_000_777L;      // ~3.26 GiB backup, underscores group the digits
int bytesPerKilobyte = 1_024;             // grouped too, for consistency
int sectorSize = 0x200;                   // 512 bytes per disk sector, written in hex

System.out.println("File size: " + fileSizeBytes + " bytes");
System.out.println("Sector size: " + sectorSize + " bytes (0x200 in hex)");
```

*Three ways of writing literals we'll lean on all lab: underscores for readability, an `L` suffix, and hex.*

Run it. Two details are worth stopping on:

- The underscores in `3_500_000_777L` and `1_024` are purely for us — the compiler strips them out. They're legal anywhere between digits.
- The `L` isn't optional here. `3_500_000_777` is bigger than `int` can hold (int tops out around 2.1 billion), so *the literal itself* needs the suffix before it can even be assigned to a `long` — try deleting the `L` and recompiling to see `integer number too large` for yourself, then put it back.
- `0x200` is the same value as `512`, just spelled in hexadecimal — common wherever code talks about byte layouts, flags, or memory addresses.

One more thing before we move on: `fileSizeBytes`, `bytesPerKilobyte`, and `sectorSize` are **local variables** — declared inside `main`, so by the scope rule (visible from declaration to the end of the enclosing block) they exist for the rest of `main`'s body. Every part we add below lives in that same block, so they'll all still be able to see these three variables.

---

## Part 2 — Integer division and overflow (the bite)

Now we'll convert that byte count to kilobytes two different ways, and then do something we shouldn't. At **`[MARKER 2]`**, we'll add:

```java
long kilobytes = fileSizeBytes / bytesPerKilobyte;
System.out.println("Kilobytes (integer division): " + kilobytes);

double kilobytesExact = fileSizeBytes / (double) bytesPerKilobyte;
System.out.println("Kilobytes (exact): " + kilobytesExact);

int fileSizeBytesAsInt = (int) fileSizeBytes;
System.out.println("Same size squeezed into an int: " + fileSizeBytesAsInt);
```

*Two divisions of the same numbers, and one deliberately reckless narrowing cast.*

Run it and look closely at the three new lines:

- `kilobytes` divides a `long` by an `int` — both whole-number types, so the division is **integer division**: the fractional kilobyte is simply thrown away. You'll see `3417969`.
- `kilobytesExact` casts `bytesPerKilobyte` to `double` *before* dividing — casting the operand, not the result, exactly the rule from Type Casting: once one operand is floating-point, the other is promoted to match, and the division keeps its fraction. You'll see `3417969.5087890625` — proof that `kilobytes` above quietly dropped real information.
- `fileSizeBytesAsInt` narrows a `long` that doesn't fit into an `int` **on purpose**. Nothing throws, nothing warns — the value silently wraps to whatever the low 32 bits happen to spell, and you'll see a nonsense *negative* number where a ~3.3 billion byte count used to be. This is the overflow rule from Type Casting made real: narrowing a value that doesn't fit isn't an error, it's a silent wrong answer.

That last line is worth sitting with. If we didn't already know `fileSizeBytes` was too big for an `int`, we'd have no way to tell from this output alone that anything went wrong.

---

## Part 3 — Casting for real numbers: truncation vs. `Math.round`

Next we'll convert a distance. At **`[MARKER 3]`** — below `main`, as a separate method — we'll add:

```java
static double milesToKilometers(double miles) {
    return miles * 1.60934;
}
```

*A small conversion method: multiply by the miles-per-kilometer rate.*

Notice `miles` is a **parameter** — method-scoped, meaning it exists only for the lifetime of this method call and is gone the instant `milesToKilometers` returns. It can't collide with anything else in the file named `miles`, and nothing outside this method can read it.

Now, back at **`[MARKER 4]`** in `main`, we'll call it and look at two different ways of turning the result into a whole number:

```java
double milesDriven = 31.5;
double kilometersDriven = milesToKilometers(milesDriven);
System.out.println("Kilometers driven (exact): " + kilometersDriven);

int truncatedKm = (int) kilometersDriven;
long roundedKm = Math.round(kilometersDriven);
System.out.println("Truncated: " + truncatedKm + ", Rounded: " + roundedKm);
```

*Same double, two ways of making it a whole number — a cast and `Math.round`.*

Run it: `Kilometers driven (exact): 50.69421`, then `Truncated: 50, Rounded: 51`. Same input, two different whole numbers, and neither is a bug — they're doing different things. `(int) kilometersDriven` **truncates**: it chops off the `.69421` and keeps whatever's left, always moving toward zero. `Math.round(...)` looks at that fraction and rounds to the *nearest* whole number — `.69421` is closer to `51` than `50`, so that's what we get. Reach for a cast when you specifically want truncation; reach for `Math.round` when you want the mathematically nearest value.

In passing: if we wanted to print this conversion for a whole list of trips, we'd loop over them with a `for` loop — and that loop's counter would be scoped only to the loop itself, invisible before or after it. We haven't written a `for` loop yet (that's coming properly in Control Flow), but the scope rule you just saw for `miles` above is the exact same rule that will apply there.

---

## Part 4 — Locking in constants with `final`

We've now used the literals `1_024` and `1.60934` inline, in two different places. Real code names values like that instead of repeating the magic number. At **`[MARKER 5]`** — at the top of the class, before `main` — we'll add:

```java
static final int BYTES_PER_KILOBYTE = 1_024;
static final double KM_PER_MILE = 1.60934;
```

*Two class-level constants: `static final` fields, named in `UPPER_SNAKE_CASE` — the standard constant idiom.*

Now we'll wire them in. Delete the local `int bytesPerKilobyte = 1_024;` line from Part 1 entirely, and replace every remaining use of `bytesPerKilobyte` in Part 2 with `BYTES_PER_KILOBYTE`. Then update the method from Part 3 to use the new constant too:

```java
static double milesToKilometers(double miles) {
    return miles * KM_PER_MILE;
}
```

Run it again — the output should be *identical* to before. That's the point of this refactor: behavior unchanged, readability improved, and the values now live in exactly one place each.

This is also a good moment to compare scopes side by side. `BYTES_PER_KILOBYTE` and `KM_PER_MILE` are **fields** — class scope, visible to every method in the class regardless of where it's declared, and they'll live for the entire run of the program. That's different from Part 1's `fileSizeBytes`, a **local** variable that only exists inside `main` and disappears the moment it returns. Same language, two very different lifetimes. And because both constants are `final`, the compiler now guarantees nobody can reassign them by accident — try adding `BYTES_PER_KILOBYTE = 2048;` anywhere and watch the compiler refuse it.

---

## Part 5 — `var`: when inference reads well

Since Java 10, a local variable can be declared with `var` instead of spelling out its type, and the compiler infers the type from whatever's on the right. At **`[MARKER 6]`**, we'll add:

```java
var backupLabel = "Photo Library Backup";     // inferred: String
var kilobytesPerMegabyte = BYTES_PER_KILOBYTE;  // inferred: int
System.out.println(backupLabel + " (" + kilobytesPerMegabyte + " KB/MB)");
```

*Two `var` declarations — the compiler works out `String` and `int` from the initializers.*

Run it. Both reads fine, because the type is obvious from the right-hand side without any digging — that's exactly when `var` earns its keep, trimming repetition the reader doesn't need.

Now compare that with Part 3's `double kilometersDriven = milesToKilometers(milesDriven);`. Could that have been `var kilometersDriven = ...`? Syntactically, yes — but pause and ask whether a reader can tell it's a `double` from that line alone. `milesToKilometers` returning a distance is a reasonable guess, but nothing in the line itself says so the way `"Photo Library Backup"` obviously says `String`. That's the judgment call `var` always demands: use it when the initializer already makes the type obvious, and spell the type out when it doesn't. There's no compiler rule here — only readability.

---

## Exercises

Same ideas, new problems, no markers this time. Decide for yourself where a cast belongs and where it doesn't.

1. **Temperature converter.** Add a feature that converts a Celsius reading to Fahrenheit (`F = C * 9/5 + 32`) and back. Use `double` throughout, but think carefully about `9/5` — written as two `int` literals it means something different than you'd expect. Prove it to yourself before you decide how to write the formula.

2. **Currency, the safe way.** Add a feature that converts a dollar amount to another currency using a `double` exchange rate (e.g., `1.08`). Then re-read the Floating-Point Caution from the Primitives notes and reconsider: is representing the dollar amount itself as a `double` a good idea? Rewrite the dollar amount as a `long` count of cents instead, and decide where — if anywhere — a cast or `Math.round` still needs to appear in the currency conversion.

3. **Overflow detection.** Pick a `long` value that doesn't fit in an `int` (anything bigger than `Integer.MAX_VALUE` works). First confirm that `(int)` casting it wraps to nonsense, the way `fileSizeBytesAsInt` did in Part 2. Then call `Math.toIntExact(...)` on the same value instead of casting, run it, and read the stack trace it produces top to bottom. What does `Math.toIntExact` do differently from a plain cast, and why might that difference matter in real code?

4. **Predict the output.** Before running anything, write down what you expect each of these to print, and *why*:

   ```java
   System.out.println((int) 9.99);
   System.out.println((int) -9.99);
   System.out.println(Math.round(9.5));
   System.out.println(Math.round(-9.5));
   System.out.println((byte) 130);
   System.out.println(7 / 2);
   System.out.println(7.0 / 2);
   ```

   Only after you've written every prediction down, drop them into a `scratch` method and run it. For any you got wrong, go back to the Type Casting notes and find the rule you missed.
