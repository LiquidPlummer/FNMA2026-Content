# Lab: Methods — Extract & Compose

Most labs start from an empty skeleton. This one starts from a **working program that badly needs a refactor** — an 80-line `main` that splits restaurant bills, computes tax and tip, and prints a receipt, all inline with plenty of copy-pasted arithmetic. We'll pull it apart piece by piece: naming each behavior, choosing parameters and return types along the way, until `main` is a handful of readable calls instead of a wall of math. Along the way we'll cover **defining methods** (the five-part anatomy, one-thing-per-method, overloading), **calling methods** (same-class calls, methods calling methods), and **returning** (return types, and the guard-clause early return). The diff between where we start and where we end up *is* the lesson.

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

You should see a "Dinner for four" receipt followed by a "Solo lunch" receipt. That's the starter in [src/main/java/com/curriculum/labs/BillSplitLab.java](src/main/java/com/curriculum/labs/BillSplitLab.java) — open it now. `main` is one long block with two `[MARKER]` regions, each computing and printing one bill the exact same way. Re-run the command above after every part to confirm the output hasn't changed — a refactor that changes behavior isn't a refactor.

---

## Part 1 — Extract `roundToCents`

Look through `MARKER 1` and `MARKER 2`: the line `x = Math.round(x * 100) / 100.0;` appears **eight times** — once for tax, tip, total, and per-person split, in both scenarios. That repetition is exactly what a method is for: write the behavior once, give it a name, call it from everywhere it's needed.

Below `main`, in place of the comment `// [MARKER 3] Exercise-only...` — no, above it, right after the closing brace of `main` — we'll add:

```java
static double roundToCents(double amount) {
    return Math.round(amount * 100) / 100.0;
}
```

*Rounds any dollar amount to the nearest cent — the one calculation copy-pasted eight times in `main`.*

Now replace all eight rounding lines with a call. For example, this pair in `MARKER 1`:

```java
double tax = subtotal * taxRate / 100.0;
tax = Math.round(tax * 100) / 100.0;
```

becomes:

```java
double tax = subtotal * taxRate / 100.0;
tax = roundToCents(tax);
```

Do the same for `tip`, `total`, and `perPersonTip`/`perPersonTotal` in **both** marker regions — eight call sites in place of eight copies of the same formula. Run the program again: the output should be byte-for-byte identical to before. That's the point — we changed the code's shape, not its behavior.

---

## Part 2 — Extract `percentOf`

Next, look at how tax and tip are computed: `subtotal * taxRate / 100.0` and `subtotal * tipRate / 100.0` — same formula, different percentage. We'll name that formula too:

```java
static double percentOf(double amount, double percent) {
    return amount * percent / 100.0;
}
```

*An amount scaled by a percentage — the shared formula behind both tax and tip.*

Add this below `roundToCents`. Then replace the tax and tip calculations in both marker regions. For example:

```java
double tax = roundToCents(percentOf(subtotal, taxRate));
double tip = roundToCents(percentOf(subtotal, tipRate));
```

*One method's result feeding straight into another — `percentOf` computes the raw percentage, `roundToCents` cleans it up. This is composition: small methods calling each other instead of one method doing everything.*

Delete the now-unused intermediate reassignment lines (`tax = roundToCents(tax);` no longer applies — `tax` is assigned once, correctly, on declaration). Run again — same output as always.

---

## Part 3 — Extract `splitEvenly`

The per-person lines divide an amount by a headcount, then round:

```java
double perPersonTotal = total / diners;
perPersonTotal = Math.round(perPersonTotal * 100) / 100.0;
```

One more method, reusing one we already built:

```java
static double splitEvenly(double amount, int numPeople) {
    return roundToCents(amount / numPeople);
}
```

*Divides an amount among `numPeople` and rounds the result — calling `roundToCents` instead of repeating that formula a ninth and tenth time.*

Replace both per-person calculations, in both marker regions:

```java
double perPersonTotal = splitEvenly(total, diners);
double perPersonTip = splitEvenly(tip, diners);
```

Run it. Still identical output — but look at how much shorter each marker region has gotten. Everything left inside them now is either a `println` or a call to one of our three new methods.

---

## Part 4 — Extract `printReceipt` and compose the whole thing

Each marker region still repeats the same shape: compute four values, print five lines. We'll pull the *entire* per-bill process into one method that takes the inputs and does everything:

```java
static void printReceipt(String label, double subtotal, double taxRate, double tipRate, int diners) {
    double tax = roundToCents(percentOf(subtotal, taxRate));
    double tip = roundToCents(percentOf(subtotal, tipRate));
    double total = roundToCents(subtotal + tax + tip);
    double perPersonTotal = splitEvenly(total, diners);
    double perPersonTip = splitEvenly(tip, diners);

    System.out.println();
    System.out.println(label);
    System.out.println("  Subtotal:   $" + subtotal);
    System.out.println("  Tax:        $" + tax);
    System.out.println("  Tip:        $" + tip);
    System.out.println("  Total:      $" + total);
    System.out.println("  Per person: $" + perPersonTotal + " (includes $" + perPersonTip + " tip)");
}
```

*Everything one bill needs, in one place — this method calls the three helpers above instead of repeating their logic.*

Add it below `splitEvenly`. Now delete **everything** inside `MARKER 1` and `MARKER 2` — all the computation and printing — and replace the two regions with:

```java
printReceipt("Dinner for four", 84.50, 8.25, 18, 4);
printReceipt("Solo lunch", 13.75, 8.25, 20, 1);
```

Run it. Same two receipts, still byte-for-byte identical — except `main` just dropped from roughly 80 lines to 2. That collapse is what "extract and compose" means: small methods, each doing one thing, assembled into a bigger one that reads like a sentence.

---

## Part 5 — Overload `printReceipt` with a default tip

Most parties tip a standard 18%, and typing that number at every call site is one more thing to get wrong. We'll add a second `printReceipt` with **one fewer parameter** — an overload:

```java
static void printReceipt(String label, double subtotal, double taxRate, int diners) {
    printReceipt(label, subtotal, taxRate, 18, diners);
}
```

*Same name, a shorter parameter list — this version just calls the 5-argument version with a default 18% tip rate. Overloading is picking the version whose signature matches the call, not writing the logic twice.*

Add a third bill to `main` using this shorter form:

```java
printReceipt("Coffee run", 6.25, 8.25, 2);
```

Run it: a third receipt appears — `$0.52` tax, `$1.13` tip, `$7.9` total, split `$3.95` a person — computed with the 18% default even though we never mentioned a tip rate at the call site.

---

## Part 6 — Guard clause: reject an impossible split

Nothing currently stops `diners` from being `0`. Add a fourth call to `main`:

```java
printReceipt("Office party (bad headcount)", 250.00, 8.25, 18, 0);
```

Run it now, before fixing anything — Java prints `Infinity` for the per-person amounts, because dividing by zero on `double`s doesn't crash, it just produces garbage. That's worse than a crash: a receipt that *looks* legitimate but lies.

Fix it with a **guard clause** at the very top of the 5-argument `printReceipt` — the early-return pattern from the Returning lesson: check for the invalid case first, handle it, and leave immediately.

```java
if (diners <= 0) {
    System.out.println();
    System.out.println(label + ": can't split a bill among " + diners + " people.");
    return;
}
```

*A bare `return;` in a `void` method — pure control flow, no value to hand back, just "stop here."*

Add it as the first line inside the 5-argument `printReceipt`, before `tax` is computed. Run it one more time: the first three receipts print exactly as before, and the fourth now prints a clear one-line message instead of `Infinity` everywhere. Notice that the 4-argument overload didn't need its own guard — it just forwards to the 5-argument version, which already protects everyone.

---

## Exercises

Same ideas, a new problem, no markers to follow.

1. **A second monolith.** `reportStats(int[] scores)`, near the bottom of the file, has never been touched — it finds the minimum, maximum, and average of an array of test scores, all in one method, and prints a report. Wire it into `main` yourself (call `reportStats(TEST_SCORES)`), then extract at least two well-named helper methods from it (a natural split: one method per statistic, or one for the min/max scan and one for the average). `reportStats` should end up calling your new methods instead of computing everything itself — the same transformation Parts 1–4 just walked you through, on a different domain.

2. **Design a signature first.** A loyalty program needs a method that takes a customer's list of purchase amounts (as a `double[]`) and a discount percentage, and returns the total after the discount is applied — capped so the discount never exceeds $50 regardless of percentage or purchase total. Before writing a single line of body, decide: what should the method be *named*? What are its parameter types and order? What does it return? Write the signature, then implement it.

3. **Two values, one return.** `reportStats` currently prints the min and max separately — but a caller who wants both values back (rather than printed) is stuck, since `return` only hands back one thing. Define a small record — something like `record ScoreRange(int min, int max) {}` — and a method `scoreRange(int[] scores)` that returns one, computing both bounds in a single pass. Call it from `main` and print the result.
