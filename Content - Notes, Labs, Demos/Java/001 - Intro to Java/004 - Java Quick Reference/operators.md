# Java Quick Reference

## Mathematical Operators

| Operator | Description | Example |
|---|---|---|
| `+` | Addition | `a + b` |
| `-` | Subtraction | `a - b` |
| `*` | Multiplication | `a * b` |
| `/` | Division | `a / b` |
| `%` | Modulus (remainder) | `a % b` |

---

## Compound Assignment Operators

| Operator | Description | Example |
|---|---|---|
| `+=` | Add and assign | `a += b` → `a = a + b` |
| `-=` | Subtract and assign | `a -= b` → `a = a - b` |
| `*=` | Multiply and assign | `a *= b` → `a = a * b` |
| `/=` | Divide and assign | `a /= b` → `a = a / b` |
| `%=` | Modulus and assign | `a %= b` → `a = a % b` |

---

## Unary Operators

| Operator | Description | Example |
|---|---|---|
| `+` | Unary plus (positive value) | `+a` |
| `-` | Unary minus (negate value) | `-a` |
| `++` (prefix) | Increment before use | `++a` |
| `++` (postfix) | Increment after use | `a++` |
| `--` (prefix) | Decrement before use | `--a` |
| `--` (postfix) | Decrement after use | `a--` |
| `!` | Logical NOT | `!flag` |

> **Prefix vs Postfix:** `++a` increments `a` and returns the new value. `a++` returns the current value, then increments.

---

## Comparison Operators

| Operator | Description | Example |
|---|---|---|
| `==` | Equal to | `a == b` |
| `!=` | Not equal to | `a != b` |
| `>` | Greater than | `a > b` |
| `<` | Less than | `a < b` |
| `>=` | Greater than or equal to | `a >= b` |
| `<=` | Less than or equal to | `a <= b` |

---

## Logical Operators

| Operator | Description | Example |
|---|---|---|
| `&&` | AND — true if both are true | `a > 0 && b > 0` |
| `\|\|` | OR — true if either is true | `a > 0 \|\| b > 0` |
| `!` | NOT — inverts the value | `!flag` |

---

## Ternary Operator

```java
condition ? valueIfTrue : valueIfFalse
```

```java
int max = (a > b) ? a : b;
```

A shorthand for simple if/else assignments.

---

## String Concatenation

`+` is also used to join strings. If either operand is a String, Java converts the other to a String automatically.

| Example | Result |
|---|---|
| `"Hello" + " World"` | `"Hello World"` |
| `"Value: " + 42` | `"Value: 42"` |
| `"Result: " + (a + b)` | Parentheses ensure math happens first |

`+=` also works: `str += " more text";`

---

## Bitwise Operators

| Operator | Description | Example |
|---|---|---|
| `&` | Bitwise AND | `a & b` |
| `\|` | Bitwise OR | `a \| b` |
| `^` | Bitwise XOR | `a ^ b` |
| `~` | Bitwise complement (NOT) | `~a` |
| `<<` | Left shift | `a << 2` |
| `>>` | Right shift (signed) | `a >> 2` |
| `>>>` | Right shift (unsigned) | `a >>> 2` |

> Bitwise operators work on individual bits of integer values. Less common in day-to-day Java but important to recognize.