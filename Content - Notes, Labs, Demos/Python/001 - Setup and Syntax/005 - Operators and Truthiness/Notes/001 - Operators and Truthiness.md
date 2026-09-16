# Operators and Truthiness

Most of Python's operators look like the ones we already use, which is exactly what makes the differences dangerous. Division doesn't truncate, `is` isn't `===`, `or` doesn't return a boolean, and a condition doesn't need to be a boolean at all. This lesson walks through the operators with those differences front and center.

---

## Arithmetic

| Operator | Meaning | Example | Result |
|---|---|---|---|
| `+`, `-`, `*` | Add, subtract, multiply | `7 * 2` | `14` |
| `/` | True division — always a float | `7 / 2` | `3.5` |
| `//` | Floor division | `7 // 2` | `3` |
| `%` | Modulo (remainder) | `7 % 2` | `1` |
| `**` | Exponentiation | `2 ** 10` | `1024` |

Mixing an `int` and a `float` in any arithmetic produces a `float`.

### `/` vs `//`

In Java, dividing two `int`s truncates: `7 / 2` is `3`. In Python, **`/` always produces a float**, even when the division is exact:

```python
7 / 2      # 3.5
6 / 2      # 3.0
7 // 2     # 3
7.0 // 2   # 3.0 — floor division of a float still gives a float
```

*`/` performs true division; `//` performs floor division when we want a whole-number result.*

TypeScript developers get the `/` behavior they expect, since JavaScript's `/` never truncates either. `//` is new to both groups — it replaces `Math.floor(a / b)` or Java's integer division.

### Floor Means Toward Negative Infinity

`//` rounds *down*, not toward zero. For negative numbers that gives a different answer than Java:

```python
-7 // 2    # -4    (Java: -7 / 2 is -3)
-7 % 2     #  1    (Java: -7 % 2 is -1)
```

*Python's floor division rounds toward negative infinity, and `%` takes the sign of the divisor. Both keep `(a // b) * b + a % b == a` true.*

### `**`

Python has an exponent operator, so there's no need for `Math.pow`. One precedence detail catches people: `**` binds tighter than a unary minus.

```python
2 ** 10     # 1024
2 ** -1     # 0.5
-2 ** 2     # -4 — parsed as -(2 ** 2)
(-2) ** 2   # 4
```

*Exponentiation, including a negative exponent and the unary-minus precedence trap.*

---

## `==` vs `is`

Python has two different equality questions, and they map onto Java — not JavaScript:

- **`==` compares values.** Are these two objects equal? (Like Java's `.equals()`.)
- **`is` compares identity.** Are these the same object? (Like Java's `==` on references.)

```python
a = [1, 2, 3]
b = [1, 2, 3]
c = a

a == b     # True  — equal contents
a is b     # False — two separate list objects
a is c     # True  — two names bound to one object
```

*Two lists with the same contents are equal but not identical. A list is Python's growable array, written with square brackets.*

The good news for Java developers: **`==` on strings compares contents**, so the classic Java bug of comparing strings with `==` doesn't exist in Python.

### Why `is` Is Almost Never What We Want

Whether two equal immutable values happen to be the same object is an implementation detail. CPython reuses some small integers and short strings, so `is` can appear to work in a quick REPL test and then fail on other values or in other contexts. Recent versions emit a `SyntaxWarning` when `is` is used against a literal, precisely because it's almost always a mistake.

**The one standard exception is `None`.** There's exactly one `None` object, so identity is the correct test, and PEP 8 recommends it:

```python
result = None

if result is None:
    print("No result yet")
```

*Checking for `None` with `is` — the idiomatic, recommended form.*

### Contrast: This Is Not `==` vs `===`

JavaScript's `==` vs `===` split is about **type coercion**: `===` compares without converting types. Python's `==` already refuses to coerce between strings and numbers — `1 == "1"` is `False` — so it behaves like `===` in that respect. `is` answers a different question entirely: identity. Reading `is` as "strict equals" leads straight to bugs.

(Numeric types do compare by value across types: `1 == 1.0` is `True`, as is `True == 1`.)

---

## Comparisons and Chaining

The comparison operators are `<`, `<=`, `>`, `>=`, `==`, and `!=`. There is no `===`.

Python lets comparisons **chain**:

```python
x = 5

0 < x < 10       # True
0 < x < 3        # False
1 <= x <= 5      # True
```

*A chained comparison reads like the mathematical notation for a range check.*

`0 < x < 10` means `0 < x and x < 10`, with `x` evaluated only once.

**Contrast:** in Java, `0 < x < 10` doesn't compile — it would compare a `boolean` to an `int`. In JavaScript it compiles and is silently wrong: `(0 < 50) < 10` becomes `true < 10`, which becomes `1 < 10`, which is `true`.

---

## `and`, `or`, `not`

Python's logical operators are words. `&&`, `||`, and `!` don't exist for logic (`!` is a syntax error, and `&` and `|` are bitwise operators).

Like Java, `and` and `or` **short-circuit**: they stop evaluating as soon as the result is known. Unlike Java, **they return one of their operands, not a boolean**:

- `a or b` returns `a` if `a` is truthy; otherwise it returns `b`.
- `a and b` returns `a` if `a` is falsy; otherwise it returns `b`.
- `not a` always returns a real `True` or `False`.

```python
"" or "anonymous"        # 'anonymous'
"Ada" or "anonymous"     # 'Ada'
0 and 10                 # 0
5 and 10                 # 10
not ""                   # True
```

*`and` and `or` hand back whichever operand decided the result; only `not` produces a boolean.*

That makes `or` a compact way to supply a fallback:

```python
username = ""
display_name = username or "anonymous"
print(display_name)      # anonymous
```

*Using `or` to fall back to a default when the first value is empty.*

TypeScript developers know this pattern from `||`. The same caveat applies: `count or 10` replaces a legitimate `0` with `10`, because `0` is falsy. Python has no `??` operator, so when `0` or `""` are valid values, an explicit `is None` check is the safe choice.

Precedence runs `not`, then `and`, then `or`.

---

## Truthiness

In Java, an `if` condition must be an actual `boolean`; `if (list)` won't compile. Python accepts **any object** in a condition and decides whether it counts as true using **truthiness** rules.

These built-in values are **falsy**:

- `False` and `None`
- Zero of any numeric type: `0`, `0.0`
- Empty strings and empty collections: `""`, `[]`, `()`, `{}`, `set()`, `range(0)`

**Everything else is truthy** — including values that look false, like `"0"`, `"False"`, and `[0]`.

Calling `bool()` shows how any value will be treated:

```python
bool(0)        # False
bool("")       # False
bool([])       # False
bool(None)     # False
bool("0")      # True — a non-empty string
bool([0])      # True — a list with one item in it
bool(-1)       # True — any non-zero number
```

*`bool()` reveals the truthiness Python applies when a value appears in a condition.*

**Contrast with JavaScript:** the idea is the same, but the rules differ. The one that bites most often is the empty array — `[]` is truthy in JavaScript and falsy in Python.

### Idiomatic Emptiness Checks

Python style tests emptiness through truthiness rather than by measuring length:

```python
items = []

if not items:
    print("Nothing to process")
```

*The idiomatic way to check for an empty collection. PEP 8 prefers this over `if len(items) == 0:`.*

The same goes for strings (`if name:` rather than `if name != "":`).

Truthiness does blur "empty" and "missing" together. When `0`, `""`, or an empty list is a legitimate value and we only want to catch an absent one, `if value is None:` says exactly what we mean.

---

## Membership: `in` and `not in`

`in` tests whether a value is contained in a collection or string:

```python
3 in [1, 2, 3]           # True
"py" in "python"         # True — substring test for strings
"z" not in "python"      # True
```

*Membership tests on a list and on strings; `not in` is the negated form.*

This replaces Java's `list.contains(x)` and `str.contains("py")`, and TypeScript's `includes`. We write `x not in items` rather than `not x in items`.

**Contrast for TypeScript developers:** JavaScript's `in` checks for a *property key* — `0 in [5]` is `true` because index `0` exists. Python's `in` checks for a *value* in the sequence.

---

## Augmented Assignment, and No `++`

The augmented assignment operators work as expected:

```python
count = 0
count += 1       # 1
count -= 2       # -1
count *= 10      # -10
count //= 3      # -4
count **= 2      # 16
```

*Each augmented operator combines an arithmetic operation with rebinding the name.*

**There is no `++` or `--`.** `count++` is a syntax error. The trap is the prefix form: `++count` is *legal* and does nothing — it's two unary plus signs applied to `count`. Likewise `--count` is just double negation, returning the same value. Neither changes `count`. We write `count += 1`.

Augmented assignments are statements, not expressions, so they can't be embedded inside a condition or another expression the way `i++` often is in Java.

---

## Conditional Expressions

Python's version of the ternary operator reads value-first:

```python
age = 20
status = "adult" if age >= 18 else "minor"
print(status)    # adult
```

*A conditional expression choosing between two values. The equivalent in Java and TypeScript is `age >= 18 ? "adult" : "minor"`.*

The form is always `value_if_true if condition else value_if_false`. There's no `? :` operator. Nesting conditional expressions is legal but quickly becomes unreadable; an `if` statement is clearer.

---

## Key Takeaways

- `/` always produces a float; `//` is floor division and rounds toward negative infinity; `**` is exponentiation.
- `==` compares values and `is` compares identity. Use `==` for equality and reserve `is` for `None`.
- Python's `==`/`is` split is value vs identity, not JavaScript's coercion vs strict equality.
- Comparisons chain: `0 < x < 10` works as it reads.
- `and` and `or` short-circuit and return one of their operands; `not` returns a boolean.
- Any value can be a condition. `False`, `None`, zero, and empty strings and collections are falsy; everything else is truthy.
- `if not items:` is the idiomatic emptiness check.
- `in` tests membership by value; `not in` negates it.
- There's no `++` or `--` — and `++x` silently does nothing.
- The conditional expression is `a if condition else b`.
