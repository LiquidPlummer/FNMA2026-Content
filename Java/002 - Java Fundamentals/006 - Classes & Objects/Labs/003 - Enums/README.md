# Lab: Enums — refactoring a string-constant mess into `OrderStatus`

This lab hands you a small order tracker that's already "working" — and already carrying a bug you can't see by reading the output. We'll refactor its `String`-based status (`Order.PENDING`, `Order.PAID`, ...) into a proper `OrderStatus` enum, watch the hidden bug turn into a compile error, replace an if/else chain with an exhaustive `switch`, and finish by giving the enum its own data and behavior. This lab is a standalone domain — it doesn't touch the `BankAccount` from the last two labs.

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

You should see:

```
=== Order Tracker ===
ORD-1: Unknown status: SHIPPPED
ORD-2: In progress
Done.
```

That first line is the bug. Open [src/main/java/com/curriculum/labs/Order.java](src/main/java/com/curriculum/labs/Order.java), [src/main/java/com/curriculum/labs/OrderStatus.java](src/main/java/com/curriculum/labs/OrderStatus.java), and [src/main/java/com/curriculum/labs/OrderTrackerLab.java](src/main/java/com/curriculum/labs/OrderTrackerLab.java) now — find the numbered `[MARKER]` comments. The walkthrough below tells you which marker to work at, in which file. Re-run the command above after every part.

---

## Part 1 — Finding (and fixing) the typo, with an enum

Look at `OrderTrackerLab.java`, around **`[MARKER 7]`**:

```java
order1.setStatus(Order.PAID);
order1.setStatus("SHIPPPED");   // typo: three P's — compiles fine, silently wrong
order2.setStatus(Order.PAID);
```

`"SHIPPPED"` doesn't match any of `Order`'s status constants — but it's a `String`, so the compiler has no way to know that. It compiles, it runs, and `order1` ends up with a status that doesn't match anything `printStatus` checks for, which is exactly why the output above says `Unknown status: SHIPPPED` instead of the shipping message you'd expect. **A typo in a string is invisible until something goes looking for it at runtime** — and by then, it's a bug report, not a compile error.

Let's fix the root problem: a status shouldn't be "any string," it should be one of a small, fixed set. At **`[MARKER 1]`** in `OrderStatus.java`, we'll add:

```java
PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
```

*Five constants, and — for now — nothing else. `OrderStatus` has exactly these five values; nothing else compiles as one.*

Now migrate `Order.java` to use it. At **`[MARKER 2]`**, delete the five `String` constants entirely — `OrderStatus` replaces them:

```java
public static final String PENDING = "PENDING";
public static final String PAID = "PAID";
public static final String SHIPPED = "SHIPPED";
public static final String DELIVERED = "DELIVERED";
public static final String CANCELLED = "CANCELLED";
```

At **`[MARKER 3]`**, change the field's type:

```java
private OrderStatus status;
```

At **`[MARKER 4]`**, update the constructor to assign the enum constant instead of the (now-deleted) `String` one:

```java
this.status = OrderStatus.PENDING;
```

At **`[MARKER 5]`** and **`[MARKER 6]`**, change the getter's return type and the setter's parameter type from `String` to `OrderStatus`:

```java
public OrderStatus getStatus() {
    return status;
}

public void setStatus(OrderStatus status) {
    this.status = status;
}
```

*Four small type changes — `Order` now speaks `OrderStatus`, not `String`, everywhere.*

Back in `OrderTrackerLab.java`, `Order.PAID` no longer exists (we deleted it), so at `[MARKER 7]` replace the whole block with:

```java
order1.setStatus(OrderStatus.PAID);
order1.setStatus(OrderStatus.SHIPPPED);   // same typo — now written as an enum constant
order2.setStatus(OrderStatus.PAID);
```

Notice we kept the *typo* on purpose. Try to compile:

```console
mvn -q compile
```

It fails:

```
error: cannot find symbol
        order1.setStatus(OrderStatus.SHIPPPED);
                                    ^
  symbol:   variable SHIPPPED
  location: class OrderStatus
```

**That's the whole lesson in one error message.** The exact same typo that silently produced garbage output as a `String` is now a compile error the moment you type it, because `OrderStatus` only has the values it declares — `SHIPPPED` simply isn't one of them. Fix it:

```java
order1.setStatus(OrderStatus.SHIPPED);
```

You'll also need to update `printStatus` further down so it compiles against the new type — for now, at the bottom of `main`'s existing calls, just change `order.getStatus()` comparisons to compare against `OrderStatus` constants instead of `Order`'s (we'll properly rewrite this method in Part 3; for now, change every `Order.PENDING`/`Order.PAID`/etc. inside `printStatus` to `OrderStatus.PENDING`/`OrderStatus.PAID`/etc.). Run `mvn -q compile exec:java`. Both orders now print a correct, sensible status — the bug is gone, and it's structurally impossible to reintroduce by typo.

---

## Part 2 — `values()` and `valueOf()`: enums meet the outside world

Real status values often arrive as text — a CSV column, a form field, an HTTP body — and need converting into our enum. At **`[MARKER 8]`** in `OrderTrackerLab.java`, we'll add:

```java
String[] rawStatuses = {"DELIVERED", "REFUNDED", "PENDING"};
for (String raw : rawStatuses) {
    try {
        OrderStatus parsed = OrderStatus.valueOf(raw);
        System.out.println(raw + " -> " + parsed);
    } catch (IllegalArgumentException e) {
        System.out.println(raw + " -> not a real status");
    }
}
```

*`OrderStatus.valueOf(String)` converts text to a constant — exact match only — and throws `IllegalArgumentException` for anything that isn't one of the five names, which we catch and report instead of crashing.*

Run it. `"DELIVERED"` and `"PENDING"` convert cleanly; `"REFUNDED"` — a status that doesn't exist (yet — we'll see it again in the exercises) — is caught and reported instead of blowing up the program. This is the fail-fast idiom from earlier topics, now guarding the boundary between "text from outside" and "a real `OrderStatus}` inside."

---

## Part 3 — switch on an enum: the compiler checks your coverage

`printStatus`'s if/else chain works, but it's exactly the kind of code enums make redundant. At **`[MARKER 10]`**, replace the whole `printStatus` method body with a switch expression:

```java
private static void printStatus(Order order) {
    String label = switch (order.getStatus()) {
        case PENDING            -> "Awaiting payment";
        case PAID, SHIPPED      -> "In progress";
        case DELIVERED          -> "Complete";
        case CANCELLED          -> "Cancelled";
    };
    System.out.println(order.getId() + ": " + label);
}
```

*One case per constant (`PAID` and `SHIPPED` share a label), no `default`, and no `.equals(...)` calls anywhere.*

Run `mvn -q compile exec:java` — same output as before, far less code. Now prove the safety net is real: temporarily add a sixth constant to `OrderStatus` (in `[MARKER 1]`'s list), like `REFUNDED`, and run `mvn -q compile` again. It fails:

```
error: the switch expression does not cover all possible input values
```

**That's exhaustiveness.** The switch above has no `default`, so the compiler checked every constant `OrderStatus` declares against every `case` in the switch — and the moment a constant has no matching case, it's a compile error, not a silent fall-through. Remove the `REFUNDED` constant you just added (we'll add it properly, with a case, in the exercises) and confirm it compiles again.

---

## Part 4 — Enums are classes: giving `OrderStatus` its own data

A Java enum isn't just a list of names — it can carry fields and methods, computed per constant. Let's move the label logic Part 3 hardcoded in `printStatus` into the enum itself, where it belongs. At **`[MARKER 11]`** in `OrderStatus.java`, change the constant list to pass a label to each, and add the field and constructor that receive it:

```java
PENDING("Awaiting payment"),
PAID("In progress"),
SHIPPED("In progress"),
DELIVERED("Complete"),
CANCELLED("Cancelled");

private final String label;

OrderStatus(String label) {
    this.label = label;
}

public String getLabel() {
    return label;
}
```

*Each constant now runs the constructor with its own label — a constant isn't just a name anymore, it's a name plus data. Notice the `;` after `CANCELLED("Cancelled")` — required once anything (fields, constructor, methods) follows the constant list.*

Now `printStatus` can ask each status for its own label instead of hardcoding one per case:

```java
private static void printStatus(Order order) {
    System.out.println(order.getId() + ": " + order.getStatus().getLabel());
}
```

*The switch is gone entirely — `getLabel()` already knows what to say for its own constant.*

One more piece of behavior: some statuses are "final" — an order in that state won't change again. At **`[MARKER 12]`**, we'll add:

```java
public boolean isTerminal() {
    return this == DELIVERED || this == CANCELLED;
}
```

*`this` inside an enum constant's method refers to that specific constant — `DELIVERED.isTerminal()` compares `this` (which is `DELIVERED`) against `DELIVERED` and `CANCELLED`. `==` is correct and preferred here, since every enum constant is a singleton.*

At **`[MARKER 9]`** in `OrderTrackerLab.java`, we'll add:

```java
System.out.println(order1.getId() + " terminal? " + order1.getStatus().isTerminal());
order1.setStatus(OrderStatus.DELIVERED);
System.out.println(order1.getId() + " terminal? " + order1.getStatus().isTerminal());
```

*Same order, same method call, two different answers — because `isTerminal()` is asking the status, and the status just changed.*

Run `mvn -q compile exec:java`. `order1` reports `false` while `SHIPPED`, then `true` once moved to `DELIVERED` — one small enum method now expresses a rule that used to require checking two string literals by hand, everywhere it mattered.

---

## Exercises

The training wheels come off — same ideas, new problems, no step-by-step.

1. **A constants-with-data enum, your domain.** Pick one: HTTP status codes (`OK(200)`, `NOT_FOUND(404)`, `SERVER_ERROR(500)`, at least five total) or T-shirt sizes (`SMALL`, `MEDIUM`, `LARGE`, each carrying a chest-width measurement in inches). Build it as a proper enum with per-constant data (like `OrderStatus`'s label, or `Planet`'s mass/radius from the notes) and at least one method that computes something from that data (e.g. a `Size` method that says whether it fits a given measurement, or an HTTP status method that returns `isSuccess()`/`isError()`).

2. **`canMoveTo`.** Add a method to `OrderStatus`: `public boolean canMoveTo(OrderStatus next)`, encoding real business rules for what transitions are legal — for example: `PENDING` can move to `PAID` or `CANCELLED`; `PAID` can move to `SHIPPED` or `CANCELLED`; `SHIPPED` can move to `DELIVERED`; `DELIVERED` and `CANCELLED` can't move anywhere (use `isTerminal()`). Use a `switch` on `this` inside the method. Then update `Order.setStatus` to call `canMoveTo` and throw an `IllegalStateException` if the requested move isn't legal — and prove it, both with a legal transition that succeeds and an illegal one (e.g. `DELIVERED` back to `PENDING`) that's rejected.

3. **Add `REFUNDED` properly.** Add a sixth constant, `REFUNDED`, with its own label and a place in `isTerminal()` and `canMoveTo()` (decide: can only `DELIVERED` orders be refunded? Is a refunded order terminal?). Confirm the project fails to compile the moment you add the constant — every switch that isn't updated to cover it should break, by design — then update each one until it compiles clean again.
