# Primitives

Java is a **statically typed** language: every variable has a type, the type is known at compile time, and it never changes. The foundation of that type system is a small, fixed set of **primitive types** — eight of them — that hold simple values directly in memory. Everything else in Java is an object, but primitives are not objects: they have no methods, no fields, and very little overhead. That's exactly why they exist — they're the fast, cheap building blocks the rest of the language is built on.

---

## The Eight Primitive Types

Four integer types, two floating-point types, one character type, and one boolean:

| Type      | Category       | Size    | Range                                       | Default    |
|-----------|----------------|---------|---------------------------------------------|------------|
| `byte`    | Integer        | 8-bit   | -128 to 127                                 | `0`        |
| `short`   | Integer        | 16-bit  | -32,768 to 32,767                           | `0`        |
| `int`     | Integer        | 32-bit  | about ±2.1 billion                          | `0`        |
| `long`    | Integer        | 64-bit  | about ±9.2 quintillion                      | `0L`       |
| `float`   | Floating-point | 32-bit  | ~6–7 significant decimal digits             | `0.0f`     |
| `double`  | Floating-point | 64-bit  | ~15 significant decimal digits              | `0.0d`     |
| `char`    | Character      | 16-bit  | a single UTF-16 code unit (0 to 65,535)     | `'\u0000'` |
| `boolean` | Boolean        | JVM-dependent | `true` or `false`                     | `false`    |

In day-to-day code, two of these do most of the work: **`int`** is the default choice for whole numbers, and **`double`** is the default for decimal numbers. The others earn their place in specific situations — `long` when values can exceed ±2.1 billion (timestamps, IDs, byte counts), `byte` and `short` mostly in arrays, file I/O, and network protocols where memory layout matters.

```java
int count = 42;
long worldPopulation = 8_100_000_000L;
double price = 19.99;
boolean isActive = true;
char grade = 'A';
```

*Declaring variables of the most common primitive types with literal values.*

---

## Literals

A **literal** is a value written directly in source code, and each primitive family has its own literal rules.

Integer literals are `int` by default. To write a `long` literal, append `L` (always use uppercase — a lowercase `l` looks like the digit `1`). For readability, underscores can be placed between digits:

```java
int million = 1_000_000;
long bigNumber = 9_000_000_000L;   // won't compile without the L — too big for int
```

*Underscores group digits for readability; the `L` suffix makes the literal a `long`.*

Floating-point literals are `double` by default. A `float` literal needs an `f` suffix:

```java
double ratio = 0.75;        // fine — literal is a double
float ratioF = 0.75f;       // f suffix required
// float broken = 0.75;     // compile error: possible lossy conversion
```

*Decimal literals default to `double`; assigning one to a `float` requires the `f` suffix.*

Integers can also be written in hexadecimal (`0x1F`), binary (`0b1010`), and octal (`017`) — hex and binary show up regularly in code that works with flags, colors, or bit masks.

`char` literals use single quotes and hold exactly one character (double quotes are for `String`, which is *not* a primitive):

```java
char letter = 'J';
char newline = '\n';       // escape sequence
char unicode = '\u00E9';   // é, by Unicode escape
```

*Three ways to write a `char` literal: plain character, escape sequence, and Unicode escape.*

---

## Floating-Point Caution

`float` and `double` store values in binary (IEEE 754), and many decimal fractions can't be represented exactly — the same way 1/3 can't be written exactly in decimal. The classic demonstration:

```java
System.out.println(0.1 + 0.2);   // 0.30000000000000004
```

*Binary floating-point can't represent 0.1 or 0.2 exactly, so the sum is very slightly off.*

For scientific calculations, graphics, and statistics, this tiny imprecision is acceptable and `double` is the right tool. For **money and other exact decimal quantities, never use `float` or `double`** — use `java.math.BigDecimal`, or represent the amount as a `long` count of cents. This comes up in code review constantly, so it's worth internalizing now.

---

## Default Values — Fields Only

The "Default" column in the table above applies to **fields** (variables declared in a class), which the JVM automatically initializes. **Local variables** — those declared inside a method — get no default value. Reading a local variable before assigning it is a compile error, not a runtime surprise:

```java
public class Example {
    int instanceCount;              // field: automatically 0

    void run() {
        int localCount;
        // System.out.println(localCount);   // compile error: might not be initialized
        localCount = 1;
        System.out.println(localCount);      // fine now
    }
}
```

*Fields are auto-initialized to their default; local variables must be assigned before use.*

We'll look at declaration and initialization patterns in more detail in a later lesson — for now, the rule of thumb: the compiler protects local variables, and the JVM zero-fills fields.

---

## Why Primitives Matter

Primitives exist for performance. A primitive `int` is 32 bits of stack or in-object storage; arithmetic on it is a single CPU instruction. An object carrying the same value costs a heap allocation, a reference, and header overhead — and creates garbage collection work. In a loop crunching millions of numbers, that difference is enormous.

The trade-off is that primitives can't do anything object-like: they can't be `null`, can't go into collections like `ArrayList` directly, and have no methods. Java bridges that gap with **wrapper classes** (`Integer`, `Double`, and friends) — the subject of the next lesson.
