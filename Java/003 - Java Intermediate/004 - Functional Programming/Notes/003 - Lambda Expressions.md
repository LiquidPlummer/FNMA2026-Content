# Lambda Expressions

Lambdas have been appearing since the Iterators lesson — `name -> name.startsWith("B")` and friends — with syntax deferred. Time to settle it. A **lambda expression** is an anonymous function: parameters, arrow, body. Nothing more — no name, no class ceremony, just behavior written inline at the point it's needed, typed by whatever functional interface it lands in.

---

## The Syntax, All of It

```java
// Full form: parenthesized, typed parameters; braced body with return
Comparator<Invoice> cmp = (Invoice a, Invoice b) -> {
    return Double.compare(a.getAmount(), b.getAmount());
};

// Types inferred from the target interface
Comparator<Invoice> cmp2 = (a, b) -> Double.compare(a.getAmount(), b.getAmount());

// Single parameter: parentheses optional
Predicate<String> blank = s -> s.isBlank();

// No parameters: empty parentheses required
Supplier<Instant> now = () -> Instant.now();

// Multi-statement body: braces, semicolons, explicit return
Function<Order, Receipt> checkout = order -> {
    validate(order);
    charge(order);
    return receiptFor(order);
};
```

*Every syntactic variation: the progression from fully explicit to maximally inferred.*

The compression rules: parameter types may be omitted (inferred from the target); a *single* untyped parameter drops its parentheses; a *single-expression* body drops braces and `return` — the expression's value is the result. Style follows readability: default to the shortest legal form, but a body pushing past two or three lines usually reads better extracted into a named method (with a method reference pointing at it — below).

How does `(a, b) -> ...` know its parameter types at all? **Target typing**, the mechanism from the Functional Interfaces lesson: the lambda adopts the shape of the functional interface it's assigned to, passed as, or returned as. Same lambda text, different types in different contexts — and a lambda without any target context doesn't compile.

---

## Capturing Variables

A lambda body can read variables from the enclosing method — it **captures** them — with one famous restriction: captured locals must be **effectively final** (never reassigned after initialization):

```java
double threshold = 10_000;
Predicate<Invoice> large = inv -> inv.getAmount() > threshold;    // capture: fine

// threshold = 20_000;        // uncomment, and the LAMBDA line stops compiling
```

*Capturing an effectively final local — the lambda closes over the value it had.*

The restriction exists because the lambda may run long after the method's stack frame is gone (recall the call-stack lifetime rules) — Java copies the value in, and forbidding reassignment keeps that copy honest. The workaround-shaped urge to mutate a counter inside a lambda is almost always a signal to use the right stream operation instead (next lesson). Instance fields, note, are *not* restricted — `this` is captured instead, fields stay live — and `this` inside a lambda means the enclosing object, not the lambda (one of several reasons lambdas beat anonymous classes).

---

## Method References

When a lambda does nothing but call one existing method, the **method reference** operator `::` names the method directly — the forms seen in `Comparator.comparing(Invoice::getAmount)` and `Optional.map(User::getEmail)`:

| Form | Example | Equivalent lambda |
|---|---|---|
| Static method | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Instance method of a *particular* object | `log::accept`, `System.out::println` | `x -> System.out.println(x)` |
| Instance method of the *first parameter* | `String::isBlank` | `s -> s.isBlank()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

*The four kinds — the third is the subtle one: the receiver comes from the argument itself.*

```java
names.forEach(System.out::println);                     // particular object's method
names.removeIf(String::isBlank);                        // first-parameter form
invoices.sort(Comparator.comparing(Invoice::getId));    // getter as sort key
Supplier<List<String>> maker = ArrayList::new;          // constructor reference
```

*Method references in their natural habitats — each replaces a lambda that merely forwards.*

Use them when they're a pure drop-in for a forwarding lambda; the moment logic is added (arguments juggled, conditions checked), it's a lambda again. Both compile to the same thing — this is readability, not performance.

---

## What Lambdas Replaced, and What They Enable

The before-times comparison, one last time — because legacy codebases keep both:

```java
button.addAction(new Runnable() {          // 2004
    @Override
    public void run() { save(); }
});

button.addAction(() -> save());            // today
button.addAction(this::save);              // or tighter still
```

*Five lines of anonymous-class ritual, or the behavior itself.*

The deeper shift isn't line count — it's that passing behavior became *cheap enough to design around*. APIs stopped asking for data to act on and started asking for the action: `removeIf(Predicate)`, `computeIfAbsent(key, Function)`, `Comparator.comparing(keyExtractor)`. With lambdas fluent and the functional interfaces named, the last piece is the API that composes them at scale over whole collections — the Streams API, next.
