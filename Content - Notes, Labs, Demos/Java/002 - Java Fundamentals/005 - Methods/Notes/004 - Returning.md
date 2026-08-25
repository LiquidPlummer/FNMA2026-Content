# Returning

A method's **return value** is its output — the single value it hands back to the call site. The `return` statement does two jobs at once: it supplies that value, and it ends the method immediately (the jump we traced in Control Flow). This lesson covers both jobs, plus the design questions around them.

---

## Returning a Value

A method with a non-`void` return type **must** return a value of that type on every possible path — the compiler verifies this the same way it verifies definite assignment:

```java
static String categorize(int temp) {
    if (temp < 0) return "freezing";
    if (temp < 20) return "cold";
    if (temp < 30) return "warm";
    return "hot";                       // without this line: compile error
}
```

*Every path ends in a `return`; drop the last line and the compiler rejects the method.*

The returned expression must be assignable to the declared type — the same rules as assignment, widening included (`return 42;` is fine from a method declared `double`). What happens to the value is the caller's business; the method neither knows nor cares whether it's assigned, tested, or discarded.

---

## `void` and Early Return

A **`void`** method returns nothing, and simply reaching the end of the body ends it. A bare `return;` (no value) exits early, which is the guard-clause pattern applied to `void`:

```java
static void notify(User user, String message) {
    if (user == null || user.optedOut()) return;   // nothing to do — leave
    send(user.email(), message);
}
```

*In `void` methods, `return;` is pure control flow — exit now, hand back nothing.*

Style note carried over from Jumps: early returns for edge cases at the *top*, one main path below, is widely preferred over deep `else` nesting. Multiple returns scattered through a long method body, though, make it hard to know what the method produces — if the returns don't fit on one screen, the method wants splitting.

---

## Returning Objects and Arrays

Returning an object returns a *reference* (pass-by-value's mirror image) — the caller receives access to the same object the method had:

```java
static int[] firstN(int n) {
    int[] result = new int[n];
    for (int i = 0; i < n; i++) result[i] = i + 1;
    return result;                       // the array outlives the method
}
```

*Locals die with the stack frame, but the heap object a method built survives — the returned reference keeps it alive.*

This is the standard factory shape: build something inside, return it. The subtle design point is returning references to **internal state**: a method that returns its object's own mutable array or list hands callers the keys to that state (a leak we'll name properly when encapsulation arrives in Classes & Objects). Returning a copy, or an immutable view, keeps the boundary intact.

Two return-value conventions to recognize on sight:

- **Sentinel values** — `indexOf` returns `-1` for "not found"; `Map.get` returns `null`. Callers must check, and forgetting is a classic NPE source.
- **`Optional<T>`** — the modern alternative that makes "may be absent" part of the type. It gets full coverage in Functional Programming; for now, recognize `Optional` in signatures as a promise-with-teeth that the result can be empty.

---

## Exactly One Value — and Workarounds

`return` hands back exactly one value. When a method genuinely produces two — a quotient *and* a remainder, a result *and* a status — Java's answer is to return one object that carries both. A **record** (Java 16+) makes that nearly free:

```java
record DivResult(int quotient, int remainder) {}

static DivResult divide(int a, int b) {
    return new DivResult(a / b, a % b);
}

DivResult r = divide(17, 5);
System.out.println(r.quotient() + " r" + r.remainder());   // 3 r2
```

*A one-line record type turns "two return values" into one meaningful object.*

The anti-pattern to avoid is "returning" through a parameter — having callers pass in a mutable object or array for the method to fill. It works (mutation through a shared reference, as the Parameters lesson showed) but hides the data flow; reserve it for performance-critical code that has proven the need.

---

## Returns Are the Contract

A method's signature promises a type; its behavior promises a *meaning* — `applyDiscount` returns "the price after discount, never negative," not just "an int." Good methods keep that meaning stable across every path, document it when it's not obvious (`/** returns -1 if absent */`), and never return surprises like `null` from something named `getAll...` (an empty list is the kind answer). When a method *can't* keep its promise — bad input, missing file — returning a fake value is worse than admitting failure, and that admission is the next lesson: throwing.
