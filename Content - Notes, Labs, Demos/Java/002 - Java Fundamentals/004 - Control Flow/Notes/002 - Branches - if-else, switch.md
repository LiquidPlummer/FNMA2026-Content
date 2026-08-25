# Branches - if / else, switch

**Branching** chooses between paths: run this code or that code, depending on a condition. Java provides `if`/`else` for decisions expressed as boolean logic, and `switch` for decisions that match one value against a set of alternatives.

---

## `if`, `else if`, `else`

An `if` runs its block when a boolean expression is true; `else` catches the false case; chains of `else if` test alternatives in order until one matches:

```java
if (score >= 90) {
    grade = "A";
} else if (score >= 80) {
    grade = "B";
} else if (score >= 70) {
    grade = "C";
} else {
    grade = "F";
}
```

*A classic decision ladder — conditions are tested top to bottom, and exactly one block runs.*

Order matters in a ladder: each condition is only reached if everything above it was false, which is why `score >= 80` safely means "80 to 89" here. The condition must be a real `boolean` — Java has no truthiness, so `if (count)` doesn't compile; it's `if (count != 0)`.

Braces are technically optional for a single statement, but the convention — strongly held — is to always write them. The famous failure mode is an indented second line that *looks* inside the branch but isn't:

```java
if (isValid)
    process(order);
    audit(order);        // runs unconditionally — indentation lies
```

*Without braces, only the first statement belongs to the `if`; always use the braces.*

### The Ternary Operator

For choosing between two *values* (not two blocks of behavior), the conditional operator `? :` compresses an if/else into an expression:

```java
String label = (count == 1) ? "item" : "items";
int max = (a > b) ? a : b;
```

*Condition, `?`, value-if-true, `:`, value-if-false — an if/else that produces a value.*

One level is idiomatic; nested ternaries are a readability crime — use the ladder.

---

## `switch` — Matching One Value

When the decision is "which of these known values is it?", `switch` says so more directly than a ladder of `==` tests. The traditional statement form:

```java
switch (dayOfWeek) {
    case "SAT":
    case "SUN":
        rate = weekendRate;
        break;
    case "FRI":
        rate = peakRate;
        break;
    default:
        rate = baseRate;
        break;
}
```

*A traditional switch: cases are entry points, `break` exits, stacked cases share a body.*

The critical mechanic is **fall-through**: a `case` label is an entry point, not a compartment. Without `break`, execution *continues into the next case's code*. Stacked labels (the SAT/SUN pair) use fall-through deliberately; a forgotten `break` uses it accidentally, and that bug shipped enough software that the language eventually grew a replacement (below). `default` handles anything unmatched, and the selector can be an `int`/`short`/`byte`/`char`, a `String`, or an enum — enums and `switch` are a particularly good pairing, covered in the Classes & Objects topic.

### Switch Expressions (Java 14+)

Modern Java adds an arrow form that fixes fall-through and produces a value:

```java
int rate = switch (dayOfWeek) {
    case "SAT", "SUN" -> weekendRate;
    case "FRI"        -> peakRate;
    default           -> baseRate;
};
```

*The arrow form: multiple labels per case, no fall-through, no `break`, and the switch itself is the value.*

Each arm is self-contained — exactly one runs. When an arm needs multiple statements, braces plus **`yield`** supply the value (`case "FRI" -> { audit(); yield peakRate; }`). Switch expressions must be **exhaustive** — every possible input covered, via `default` or (for enums) a case per constant, checked by the compiler. In codebases on modern Java, this form is the default choice; the statement form persists in older code, so both must be readable on sight.

---

## `if` or `switch`?

- **Ranges, compound conditions, boolean logic** (`score >= 80 && hasBonus`) → `if`/`else`. Switch can't express these.
- **One value against fixed alternatives** (command names, status codes, enum constants) → `switch`, preferably the arrow form.
- **Two outcomes** → plain `if`/`else`, or a ternary when it's just two values.

Both branch forms also nest inside loops and each other — in practice, `if` inside `for` is most of the control flow ever written.
