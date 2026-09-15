# Names, Types, and Identifiers

In Java, `int count = 5;` is a declaration: it reserves a slot that can only ever hold an `int`, and the compiler polices that for the life of the variable. Python has no declarations. `count = 5` doesn't reserve anything and doesn't promise anything about what `count` will hold later. Getting this model right — names bound to objects, rather than typed boxes holding values — is the foundation for nearly everything that behaves differently in Python.

---

## Names Are Bindings, Not Boxes

In Python, **objects** have types, and **names** simply refer to objects. An assignment creates (or finds) an object and **binds** a name to it:

```python
count = 5
other = count          # both names now refer to the same object
count = count + 1      # count is rebound to a new object, 6

print(count, other)    # 6 5
```

*`other` still refers to the original `5`. Rebinding `count` points that name at a new object without affecting any other name.*

A few consequences follow directly:

- **There's no declaration keyword.** No `int`, `var`, `let`, or `const`. The first assignment creates the name.
- **Using a name before it's bound fails at runtime** with `NameError`.
- **The name itself has no type.** Asking "what type is `count`?" really means "what type is the object `count` currently refers to?"

---

## Rebinding to a Different Type

Because names carry no type, rebinding a name to a completely different kind of object is legal and unremarkable:

```python
value = 42
value = "forty-two"
value = None
```

*The name `value` is bound in turn to an `int`, a `str`, and `None`. Python raises no error at any point.*

This is **dynamic typing**. It is *not* the same as loose typing. Python is **strongly typed**: objects keep their types, and Python refuses to silently convert between incompatible ones.

```python
"Total: " + 42          # TypeError: can only concatenate str (not "int") to str
"Total: " + str(42)     # 'Total: 42'
```

*Python won't coerce an `int` into a string. The conversion has to be explicit.*

Here Python is *stricter* than both languages we know: Java and JavaScript each turn `"Total: " + 42` into `"Total: 42"` without complaint.

---

## Everything Is an Object

Python has no primitive types. The number `42` is an object with a type and methods, exactly like a string or a list. So are functions, and so are modules.

```python
import math

(255).bit_length()      # 8
type(42)                # <class 'int'>
type(print)             # <class 'builtin_function_or_method'>
type(math)              # <class 'module'>
```

*An integer with a method of its own, and the types of an integer, a built-in function, and a module.*

Because functions and modules are ordinary objects, they can be bound to names like any other value:

```python
say = print
say("hello")            # hello
```

*The name `say` is bound to the same function object as `print`; calling either one does the same thing.*

**Contrast:** Java splits the world into primitives (`int`, `boolean`) and objects (`Integer`, `Boolean`), with boxing to move between them. Python has only one kind of value.

---

## Built-in Types

These five cover most everyday values:

| Type | Example literals | Nearest Java / TS | Notable difference |
|---|---|---|---|
| `int` | `42`, `-7`, `1_000_000`, `0xFF` | `long`, `BigInteger` / `number`, `bigint` | Arbitrary precision — never overflows |
| `float` | `3.14`, `2.5e-3` | `double` / `number` | 64-bit IEEE 754, like both |
| `bool` | `True`, `False` | `boolean` | Capitalized; a subtype of `int` |
| `str` | `"hi"`, `'hi'` | `String` / `string` | No separate `char` type |
| `None` | `None` | `null` / `null`, `undefined` | One "no value" object |

### `int`

Python integers grow as large as memory allows, so there is no overflow and no separate `long`:

```python
2 ** 100      # 1267650600228229401496703205376
```

*An integer far beyond the range of Java's `long`, computed exactly.*

Underscores in numeric literals (`1_000_000`) are ignored and exist only for readability, just as in Java.

### `float`

`float` is a 64-bit double, with the familiar rounding behavior of every language that uses IEEE 754: `0.1 + 0.2` is `0.30000000000000004`. There's no separate single-precision type.

### `bool`

`True` and `False` are capitalized; lowercase `true` is just an undefined name and raises `NameError`. `bool` is technically a subtype of `int` (`True + True` is `2`), which occasionally shows up in type checks.

### `str`

Single and double quotes are interchangeable — `'a'` and `"a"` are the same one-character string. There is no `char` type, so the Java distinction between `'a'` and `"a"` doesn't exist. Strings are immutable.

### `None`

`None` is Python's single "no value" object, used where Java uses `null`. TypeScript's split between `null` and `undefined` has no counterpart; Python has just the one.

---

## Converting Between Types

Each built-in type's name can be called to convert a value:

```python
int("42")         # 42
int(4.9)          # 4 — truncates toward zero
float("3.5")      # 3.5
str(3.5)          # '3.5'
int("4.5")        # ValueError: invalid literal for int() with base 10: '4.5'
```

*Explicit conversions between strings and numbers. Converting a string that doesn't look like the target type raises `ValueError`.*

---

## `type()` and `isinstance()`

```python
type(3.0)                     # <class 'float'>
type("3") == str              # True
isinstance(3, int)            # True
isinstance(3, (int, float))   # True — matches any of the listed types
isinstance(True, int)         # True — bool is a subtype of int
```

*`type()` reports an object's exact class; `isinstance()` checks whether an object is an instance of a type or any of its subtypes.*

- **`type(x)`** answers "what exactly is this?" and is most useful when exploring in the REPL.
- **`isinstance(x, T)`** answers "can this be treated as a `T`?" and is the right choice for checks in code, since it respects subtypes.

These map roughly to Java's `getClass()` and `instanceof`, and to TypeScript's runtime `typeof` and `instanceof` checks.

---

## Identifier Rules

- Identifiers use letters, digits, and underscores, and **cannot start with a digit**.
- They're **case-sensitive**: `total` and `Total` are different names.
- They **can't be keywords**.
- **`$` is not allowed**, unlike in Java and JavaScript.

### Keywords

The full list of reserved words for the running version is available from the standard library:

```python
import keyword

print(keyword.kwlist)
```

*Prints every reserved keyword for the installed Python version.*

A few stand out to Java and TypeScript developers:

- **Capitalized constants are keywords:** `True`, `False`, `None`.
- **Logic is spelled out:** `and`, `or`, `not`, `is`, `in`.
- **Different spellings:** `elif`, `def`, `pass`, `lambda`, `del`.
- **Missing entirely:** `new`, `public`, `private`, `static`, `final`, `const`, `var`, `let`.

### Built-in Names Are Not Keywords

Names like `print`, `type`, `str`, `list`, `id`, `input`, and `sum` are built-in functions and types, not keywords. That means they can be rebound — which silently hides the built-in:

```python
str = "hello"      # legal, and a mistake
str(42)            # TypeError: 'str' object is not callable
```

*Binding `str` to a string shadows the built-in `str` type for the rest of the module.*

---

## Naming Conventions (PEP 8)

**PEP 8** is Python's official style guide, and its naming conventions are followed almost universally:

| Convention | Used for | Example |
|---|---|---|
| `snake_case` | Variables, functions, modules | `total_price`, `load_config`, `order_utils.py` |
| `PascalCase` | Classes | `OrderProcessor` |
| `UPPER_SNAKE` | Constants | `MAX_RETRIES` |

**Contrast:** Java and TypeScript use `camelCase` for variables and methods. `totalPrice` is valid Python, but it reads as a Java accent.

**Constants aren't enforced.** Python has no `final` or `const`. `MAX_RETRIES = 3` can be reassigned like any other name; the capital letters are a promise between developers, not a rule the language checks.

---

## Leading Underscores

Underscores carry meaning by convention:

- **`_name`** — "internal; not part of the public interface." Nothing prevents access, but readers and tools treat it as private, and `from module import *` skips it. Compare Java's `private`, which the compiler enforces.
- **`__name__`** (double underscores on both sides) — reserved for names with special meaning to Python, like the `__name__` variable every module has. We use these; we don't invent new ones.
- **`_`** on its own — a throwaway name for a value we don't care about. In the REPL, `_` also holds the result of the last expression.

---

## Type Hints

Python supports optional **type hints** (also called **annotations**) using a colon after the name:

```python
count: int = 0
name: str = "Ada"
ratio: float = 0.75
```

*Variables annotated with their intended types. TypeScript developers will recognize the `name: type` order.*

**Type hints are not enforced at runtime.** The interpreter reads them and moves on:

```python
count: int = "zero"     # no error
print(count)            # zero
```

*A hint contradicted by its own assignment. The program runs without complaint.*

Hints are information for readers, editors, and separate **type checkers** such as **mypy** and **pyright** (the engine behind VS Code's Pylance). Running a checker over the file above reports the problem — mypy says `Incompatible types in assignment (expression has type "str", variable has type "int")` — but that's an optional tool we choose to run, not part of executing the program.

### Three Languages, Three Models

- **Java:** the type is part of the declaration, and the compiler guarantees it. Code that violates it never runs.
- **TypeScript:** annotations are checked by `tsc`, then **erased**. The JavaScript that actually runs contains no trace of them.
- **Python:** annotations are **never checked** by the interpreter and **never erased**. Annotations on functions, classes, and module-level variables stay attached to the code at runtime, where tools and libraries can read them.

---

## Key Takeaways

- A name is a binding to an object, not a typed storage slot; `x = 5` declares nothing.
- Rebinding a name to a different type is legal, but Python is strongly typed and won't silently convert between types.
- Everything is an object, including numbers, functions, and modules — there are no primitives.
- The core built-in types are `int` (arbitrary precision), `float`, `bool`, `str`, and `None`.
- Use `type()` to inspect and `isinstance()` to check; call a type's name to convert.
- PEP 8 conventions: `snake_case` for variables and functions, `PascalCase` for classes, `UPPER_SNAKE` for constants — none enforced.
- A leading `_` marks something internal by convention only.
- Type hints are annotations for tools and readers; the interpreter neither checks nor erases them.
