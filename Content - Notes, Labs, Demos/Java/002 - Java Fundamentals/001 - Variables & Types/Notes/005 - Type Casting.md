# Type Casting

Java's static typing means a value of one type can't just be dropped into a variable of another — the types have to be compatible, and sometimes we have to convert. **Type casting** is that conversion between primitive types. It comes in two flavors with very different safety profiles: conversions Java performs automatically because nothing can go wrong, and conversions we must write explicitly because something can.

---

## Widening: Automatic and Safe

A **widening conversion** moves a value to a "larger" type — one that can represent every value of the original. No information can be lost, so the compiler does it silently:

```
byte → short → int → long → float → double
              char → int → ...
```

```java
int count = 42;
long bigCount = count;      // int → long, automatic
double ratio = count;       // int → double, automatic (42.0)
```

*Widening happens implicitly — a smaller type flows into a larger one without ceremony.*

One subtlety hides in that chain: `long` → `float` and `long`/`int` → `double` are "widening" by range but a 64-bit `long` has more *precision* than a 64-bit `double` (which spends bits on the exponent). Very large `long` values can lose their least-significant digits when converted. It's a corner case, but a real one in code that handles IDs or nanosecond timestamps as floating point.

---

## Narrowing: Explicit and Risky

A **narrowing conversion** goes the other way — toward a type that *can't* hold every possible value of the original. Java refuses to do it implicitly; we must write a **cast**, the target type in parentheses:

```java
double price = 19.99;
// int dollars = price;        // compile error: possible lossy conversion
int dollars = (int) price;     // explicit cast: dollars == 19
```

*A cast tells the compiler "I accept the risk" — here the fraction is simply dropped.*

The cast is a statement of intent, not a safety mechanism. Whatever doesn't fit is lost, in two distinct ways:

- **Floating point → integer: truncation.** The fractional part is cut off, not rounded — `(int) 9.99` is `9`, and `(int) -9.99` is `-9` (truncation goes toward zero). For actual rounding, use `Math.round(...)`.
- **Larger integer → smaller integer: overflow.** Java keeps only the low-order bits, which can produce wildly unrelated numbers, including sign flips:

```java
int big = 130;
byte b = (byte) big;           // b == -126, not 130
long huge = 4_000_000_000L;
int i = (int) huge;            // i == -294967296
```

*Narrowing an out-of-range value doesn't error — it silently wraps to whatever the low bits spell.*

Nothing at runtime warns us. Before narrowing, either be certain the value fits or check it — `Math.toIntExact(longValue)` does the check for us and throws if the `long` doesn't fit in an `int`.

---

## Casting in Expressions: Integer Division

Casting matters most where it's least visible — inside arithmetic. Operators look at their *operands*, not at the variable receiving the result. Division of two integers is integer division, and converting the result afterward is too late:

```java
int completed = 7, total = 10;

double bad = completed / total;            // 0.0 — int division happened first
double good = (double) completed / total;  // 0.7 — cast one operand first
```

*Casting one operand before the division forces floating-point arithmetic; casting the result after would preserve the wrong answer.*

Once one operand is a `double`, the other is automatically widened to match — a rule called **numeric promotion**. Promotion has a second surprise: in arithmetic, everything smaller than `int` is promoted *to* `int` first, so the sum of two `byte`s is an `int` and needs a cast to go back:

```java
byte x = 10, y = 20;
// byte sum = x + y;           // compile error: x + y is an int
byte sum = (byte) (x + y);     // fine
```

*Arithmetic on `byte` and `short` produces `int` results — narrowing back requires a cast.*

The same applies to `char`: it's a numeric type, so `char` arithmetic promotes to `int` — which is occasionally useful (`'b' - 'a'` is `1`) and occasionally baffling (`'a' + 'b'` is `195`, not `"ab"`).

---

## Converting to and from `String`

A cast converts between *numeric* types — `String` is an object, and casting can't turn text into numbers or back. That job belongs to the wrapper classes' parse methods and `String.valueOf`:

```java
int n = Integer.parseInt("42");        // String → int
double d = Double.parseDouble("3.5");  // String → double
String s = String.valueOf(42);         // int → String
// int broken = (int) "42";            // compile error: not a cast situation
```

*Text↔number conversion is parsing and formatting, not casting.*

`parseInt` throws a `NumberFormatException` on malformed input, so input from users or files gets validated or wrapped in exception handling — covered properly in the Exception Handling topic.

---

## Rules of Thumb

- Widening is free — let the compiler do it.
- Narrowing needs a cast, and the cast is a promise that the value fits. When unsure, check (`Math.toIntExact`) rather than hope.
- Truncation is not rounding — reach for `Math.round` when rounding is what's meant.
- In mixed arithmetic, cast an *operand*, not the result — especially for division.
- Casts convert primitives; parsing converts strings. (Casting between *object* types exists too, and we'll meet it with inheritance in the OOP topic.)
