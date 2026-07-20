# Loops - while, do-while, for, for-each

A **loop** repeats a block of code while some condition holds. Java has four loop forms, and they're all interchangeable in principle — anything one can do, the others can too. What differs is what each form makes *easy to read*: they signal to the next developer whether the iteration count is known, unknown, or driven by a collection.

---

## `while` — Repeat While a Condition Holds

The fundamental loop: test first, then run the body, repeat until the test fails.

```java
int attempts = 0;
while (attempts < 3 && !connected) {
    connected = tryConnect();
    attempts++;
}
```

*A `while` loop runs zero or more times — if the condition is false at the start, the body never executes.*

`while` is the natural choice when we can't predict how many iterations are needed — retrying operations, reading input until it runs out, converging on a value. The body must make progress toward the condition becoming false; a condition that never changes is an **infinite loop** (`while (true)` is the deliberate version, paired with `break` to exit from the middle).

---

## `do-while` — Test at the Bottom

Identical to `while` except the condition is checked *after* the body — guaranteeing the body runs **at least once**:

```java
String input;
do {
    input = prompt("Enter yes or no: ");
} while (!input.equals("yes") && !input.equals("no"));
```

*Ask first, validate after — re-asking until the answer is acceptable.*

That "run first, ask questions later" shape fits input validation and menu loops, and little else — `do-while` is by far the rarest of the four. Note the semicolon after the closing condition; it's required and easy to forget.

---

## `for` — Counted Iteration

The classic `for` packs a loop's bookkeeping — initialize, test, update — into one header:

```java
for (int i = 0; i < 10; i++) {
    System.out.println("iteration " + i);
}
```

*The three-part header: run once before the loop; test before each pass; run after each pass.*

A `for` loop is exactly a `while` loop with the bookkeeping moved up front, and that's its message to the reader: *this loop counts*. The loop variable is scoped to the loop (as covered in Variables & Types → Scope), the three header parts are each optional (`for (;;)` is an infinite loop), and the update can be anything — `i += 2`, `i--`, even two variables (`for (int i = 0, j = max; i < j; i++, j--)`).

---

## For-Each — Iterate a Collection

The fourth form we've already used with arrays: no index, no condition, just "each element in turn":

```java
for (String name : names) {
    System.out.println(name);
}
```

*For-each: read-only, front-to-back iteration over an array or any `Iterable`.*

Its limitations were covered in the Arrays topic (copies, no index, no element replacement) — the summary is that for-each is the *default*, and the indexed `for` is the fallback when positions or writes are needed.

---

## `break` and `continue`

Two statements adjust a loop's flow from inside the body. **`break`** exits the loop immediately; **`continue`** abandons the current pass and jumps to the next test:

```java
for (String line : lines) {
    if (line.isBlank()) continue;      // skip blanks, keep looping
    if (line.equals("END")) break;     // stop the whole loop
    process(line);
}
```

*`continue` skips one iteration; `break` ends the loop entirely.*

Both make loops harder to reason about when overused — one clear `break` for early exit (like the search recipe from Iterating Arrays) is idiomatic; several scattered through a long body is a signal to restructure. In nested loops, `break` and `continue` affect only the **innermost** loop; a labeled form (`outer: for (...) { ... break outer; }`) exists for escaping multiple levels, and its rarity in real code is a hint about how often deeply nested control flow should be redesigned instead.

---

## Choosing a Loop

| We know... | Use |
|---|---|
| A collection/array to visit, values only | for-each |
| How many times (or need an index) | `for` |
| Only the stopping condition | `while` |
| Must run once before testing | `do-while` |

The compiler doesn't care which we pick — the reader does. Matching the loop form to the intent is a small act of documentation that costs nothing.
