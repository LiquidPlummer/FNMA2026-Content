# Declaration & Initialization

Every variable in Java goes through two moments: **declaration**, where we tell the compiler its type and name, and **initialization**, where it receives its first value. The two can happen together or apart, and the compiler enforces rules about the gap between them. This lesson collects the syntax and the conventions around both.

---

## Declaration

A declaration is a type followed by a name:

```java
int count;
String name;
double[] readings;
```

*Three declarations: a primitive, an object reference, and an array reference — none initialized yet.*

The type on the left decides what the variable can hold. For primitives, that's a value of that type. For everything else — `String`, arrays, our own classes — the variable holds a **reference** (a pointer to an object on the heap), and can also hold `null`, meaning "refers to nothing."

Several variables of the same type can be declared in one statement, though convention favors one declaration per line for readability:

```java
int x, y, z;           // legal
int width = 800, height = 600;   // legal, with initializers
```

*Multiple declarations in one statement — allowed, but one per line is the common convention.*

---

## Initialization

Initialization is the first assignment. It usually rides along with the declaration:

```java
int count = 0;
String name = "Ada";
double[] readings = new double[24];
List<String> tags = new ArrayList<>();
```

*Declaration and initialization combined — the most common pattern by far.*

Declaring first and assigning later is fine too, and occasionally necessary — for example when the initial value depends on a condition:

```java
int fee;
if (isMember) {
    fee = 0;
} else {
    fee = 25;
}
```

*Deferred initialization: the compiler accepts this because every path assigns `fee` before use.*

### Definite Assignment

The compiler tracks whether a **local variable** has been assigned on every possible path before it's read — a rule called *definite assignment*. Violating it is a compile error, which catches a whole class of bugs before the program ever runs:

```java
int fee;
if (isMember) {
    fee = 0;
}
// System.out.println(fee);   // compile error: fee may not have been initialized
```

*One branch assigns, the other doesn't — the compiler rejects the read.*

**Fields** play by a different rule: if we don't initialize them, the JVM does — numeric types to `0`, `boolean` to `false`, `char` to the zero character, and references to `null` (as covered in the Primitives lesson).

---

## `final` — Assign Exactly Once

The **`final`** keyword makes a variable assignable exactly once. After that, any reassignment is a compile error:

```java
final int maxRetries = 3;
// maxRetries = 5;            // compile error: cannot assign a value to final variable
```

*A `final` local variable — one assignment, enforced by the compiler.*

Two clarifications that matter in practice:

- `final` on a reference means the *reference* can't change — the object it points to can still be mutated. A `final List` can still have elements added to it.
- A `final` variable doesn't have to be initialized at the declaration, as long as every path assigns it exactly once.

Class-level constants combine `static final` with an `UPPER_SNAKE_CASE` name:

```java
public static final int MAX_CONNECTIONS = 10;
public static final String API_VERSION = "v2";
```

*The standard constant idiom: `static final` fields with uppercase names.*

Many teams also mark local variables and parameters `final` liberally, as a signal that a value is fixed once computed. That's a style choice; the constant idiom above is universal.

---

## `var` — Local Variable Type Inference

Since Java 10, local variables can be declared with **`var`**, letting the compiler infer the type from the initializer:

```java
var count = 42;                          // inferred: int
var name = "Ada";                        // inferred: String
var scores = new HashMap<String, List<Integer>>();   // inferred: the full map type
```

*`var` asks the compiler to fill in the type from the right-hand side.*

This is still static typing — the type is fixed at compile time; we just didn't write it out. Because inference needs something to infer from, `var` comes with hard limits: it requires an initializer on the same line, can't be `null`-initialized, and only works for *local* variables — never fields, parameters, or return types.

```java
// var broken;              // compile error: cannot infer type
// var nothing = null;      // compile error: cannot infer type
```

*`var` without a usable initializer has nothing to infer, so it won't compile.*

Style guidance: use `var` when the type is obvious from the right-hand side (as in the `HashMap` example, where it removes duplication) and spell the type out when the initializer doesn't make it clear — `var result = service.process(input);` tells the reader nothing about what `result` is.

---

## Naming Conventions

Java's naming rules are enforced socially rather than by the compiler, but they're near-universal:

- **Variables and methods**: `camelCase` — `orderCount`, `firstName`, `isActive`.
- **Constants** (`static final`): `UPPER_SNAKE_CASE` — `MAX_RETRIES`.
- Names should say what the value *is*, not its type: `customers`, not `customerArrayList`.
- Single letters are reserved for tight scopes: `i`, `j` as loop indices, `e` for a caught exception.

Identifiers must start with a letter, `$`, or `_` (by convention, always a letter), may contain digits after that, are case-sensitive, and can't be a reserved word like `class` or `int`.
