# Enums

Some values come from a small, fixed menu: days of the week, card suits, order statuses. Modeling those as `String`s or `int` codes invites every failure mode at once — typos compile fine, invalid values flow freely, and meaning lives in comments. An **enum** (enumerated type) fixes all of it: a type whose complete set of possible values is declared up front, with the compiler rejecting everything else.

---

## The Basic Form

```java
public enum OrderStatus {
    PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
}
```

*An enum declaration: the type `OrderStatus` has exactly these five values, forever.*

Each name is a **constant** — implicitly `public static final`, written in `UPPER_SNAKE_CASE`, and referenced through the type:

```java
OrderStatus status = OrderStatus.PAID;

if (status == OrderStatus.SHIPPED) { ... }     // == is CORRECT for enums
// status = OrderStatus.valueOf("REFUNDED");   // IllegalArgumentException — not a constant
// status = "PAID";                            // compile error — a String is not an OrderStatus
```

*Type safety in action: only the five declared values exist, and comparison is by identity.*

Two welcome reversals of earlier rules. First, each constant is a **singleton** — exactly one `PAID` object exists in the JVM — so `==` is not only safe for enums but preferred (it's null-safe, where `.equals` would throw). Second, there's no way to construct new values at runtime: `new OrderStatus(...)` doesn't compile. What's declared is all there is.

---

## Built-in Machinery

Every enum automatically provides:

```java
OrderStatus.values()                 // OrderStatus[] — all constants, declaration order
OrderStatus.valueOf("SHIPPED")       // String → constant (exact match, or throws)
status.name()                        // "SHIPPED" — the constant's declared name
status.ordinal()                     // 2 — position in declaration order
```

*The free API: enumerate, parse, and inspect without writing a line.*

`values()` powers "loop over all options" (`for (OrderStatus s : OrderStatus.values())`), and `valueOf` converts external input — with the usual fail-fast exception for garbage. Treat `ordinal()` as trivia, not data: persisting it (to a database, a file) breaks the moment someone reorders or inserts a constant. Store `name()` instead.

Enums also pair perfectly with `switch` — and modern switch expressions (Control Flow topic) know an enum's constants, so the compiler enforces **exhaustiveness** without a `default`:

```java
String label = switch (status) {
    case PENDING            -> "Awaiting payment";
    case PAID, SHIPPED      -> "In progress";
    case DELIVERED          -> "Complete";
    case CANCELLED          -> "Cancelled";
};
```

*Cover every constant and no `default` is needed — add a sixth constant later and this switch becomes a compile error until updated. That's a feature.*

---

## Enums Are Classes

The headline feature beyond other languages' enums: a Java enum is a full class. Constants can carry **fields**, set through a **constructor**, exposed through **methods** — turning "a list of names" into "a fixed set of rich objects":

```java
public enum Planet {
    MERCURY(3.30e23, 2.44e6),
    EARTH  (5.97e24, 6.37e6),
    JUPITER(1.90e27, 7.15e7);

    private final double mass;       // per-constant state
    private final double radius;

    Planet(double mass, double radius) {   // runs once per constant, implicitly private
        this.mass = mass;
        this.radius = radius;
    }

    public double surfaceGravity() {
        return 6.67e-11 * mass / (radius * radius);
    }
}

double g = Planet.EARTH.surfaceGravity();   // ~9.8
```

*Each constant is constructed with its own data; methods compute from it like any class.*

The constructor is implicitly private — only the constant declarations may call it, preserving the fixed-set guarantee. Fields should be `final`: a mutable enum is shared mutable static state wearing a costume (see the previous lesson's caution label).

This pattern absorbs an enormous amount of real-world modeling: statuses with display names and allowed transitions, units with conversion factors, error codes with messages. When a `switch` on an enum recurs across the codebase, moving that logic *into* the enum as a method is usually the cleaner home.

---

## When to Reach for an Enum

Any time a variable's legal values are a **known, fixed, small set** — statuses, types, categories, modes, days, directions. The moment requirements say "it can be A, B, or C," that's an enum announcing itself. Two more things worth knowing exist: `EnumSet`/`EnumMap` (collections tuned for enum keys — Collections topic), and the singleton-by-enum trick (Design Patterns). The habit to break starting today: `String` constants pretending to be types — `"PAID"` — with their invisible typos and unenforceable menus.
