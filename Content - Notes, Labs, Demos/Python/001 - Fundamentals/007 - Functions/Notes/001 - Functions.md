# Functions

Python functions don't live inside classes, don't declare return types, and can't be overloaded. What they offer instead — keyword arguments, default values, multiple return values, and functions as ordinary objects — covers most of what overloads, wrapper classes, and functional interfaces do in Java. One of those features, default values, also contains the most famous trap in the language.

---

## `def` and the Implicit `None` Return

A function is defined with `def`, a name, a parameter list, a colon, and an indented body:

```python
def greet(name):
    return "Hello, " + name

message = greet("Ada")
print(message)      # Hello, Ada
```

*A function with one parameter that returns a string.*

There's no return type, no access modifier, no `static`, and no enclosing class. Functions sit directly in a module.

`def` is a statement executed at runtime: it creates a function object and binds it to the name. That's why a function can't be called from a line above its `def` — at that moment, the name doesn't exist yet.

**Every function returns a value.** A function that never executes a `return` statement, or uses a bare `return`, returns `None`:

```python
def log(text):
    print("[LOG]", text)

result = log("starting")    # prints: [LOG] starting
print(result)               # None
```

*`log` has no `return`, so calling it produces `None`.*

**Contrast:** a Java `void` method produces nothing at all, and trying to assign its result is a compile error. In Python, assigning the result of any call is legal — which means forgetting a `return` gives us a silent `None` rather than an error.

---

## Returning Multiple Values

A function can return several values separated by commas, and the caller can **unpack** them into separate names:

```python
def min_max(numbers):
    return min(numbers), max(numbers)

low, high = min_max([4, 9, 1, 7])
print(low, high)            # 1 9
```

*Returning two values and unpacking them into two names at the call site.*

What's actually returned is a single **tuple** — an immutable, fixed-length sequence, written with optional parentheses. `min_max([4, 9, 1, 7])` on its own evaluates to `(1, 9)`. Unpacking assigns each element to a name, and the number of names must match the number of elements or Python raises `ValueError`. By convention, `_` names a value we don't need: `_, high = min_max(values)`.

**Contrast:** a Java developer returning two values reaches for a wrapper class or record, an array, or a mutable object passed in to be filled. A tuple replaces all of those for the common case. TypeScript developers will recognize the idea from returning `[low, high]` and destructuring it.

---

## Positional and Keyword Arguments

Arguments can be passed **by position** or **by name**:

```python
def create_account(owner, balance, currency):
    return f"{owner}: {balance} {currency}"

create_account("Ada", 100, "USD")                          # positional
create_account(owner="Ada", currency="USD", balance=100)   # keyword, any order
create_account("Ada", balance=100, currency="USD")         # mixed
```

*Three calls producing the same result. Keyword arguments can appear in any order, but must follow any positional ones.*

Keyword arguments make call sites readable, especially for numbers and booleans whose meaning isn't obvious from the value:

```python
resize(photo, 800, 600, True)
resize(photo, width=800, height=600, keep_aspect=True)
```

*The same call written positionally and with keywords. The second documents itself.*

Because callers can use parameter names, **parameter names are part of a function's public interface** — renaming one can break code that calls it by keyword.

**Contrast:** Java has no named arguments at all; builders and IDE parameter hints fill that gap. TypeScript typically passes a single options object to get the same readability.

---

## Default Parameter Values

A parameter can declare a default, used when the caller omits that argument:

```python
def connect(host, port=5432, timeout=30):
    print(f"Connecting to {host}:{port} (timeout {timeout}s)")

connect("db.local")                 # port 5432, timeout 30
connect("db.local", 6543)           # port 6543, timeout 30
connect("db.local", timeout=5)      # port 5432, timeout 5
```

*Defaults combined with keyword arguments let a caller override any subset of options, in any combination.*

Parameters with defaults must come after parameters without them.

---

## No Overloading

In Java, `connect(String host)` and `connect(String host, int port)` can coexist as overloads. **Python has one function per name.** Defining a second function with the same name simply rebinds the name, discarding the first:

```python
def area(side):
    return side * side

def area(width, height):
    return width * height

area(4, 5)      # 20
area(4)         # TypeError: area() missing 1 required positional argument: 'height'
```

*The second `def` replaces the first. Only the two-parameter version exists afterward.*

Default values and keyword arguments do the job overloads do in Java:

```python
def area(width, height=None):
    if height is None:
        height = width
    return width * height

area(4)         # 16
area(4, 5)      # 20
```

*One function covering both the square and rectangle cases through a default value.*

---

## The Mutable Default Argument Trap

```python
def add_item(item, cart=[]):
    cart.append(item)
    return cart

print(add_item("apple"))    # ['apple']
print(add_item("bread"))    # ['apple', 'bread'] — the same list, again
```

*Each call that omits `cart` should start with an empty list. Instead, items accumulate across calls.*

### Why It Happens

**A default value is evaluated once, when the `def` statement runs** — not each time the function is called. The resulting object is stored on the function and reused by every call that omits that argument. We can see it sitting there:

```python
print(add_item.__defaults__)    # (['apple', 'bread'],)
```

*The function's stored defaults, showing the single shared list that every call has been appending to.*

For immutable defaults like numbers, strings, and `None`, sharing one object is harmless, since nothing can change it. For a mutable default like a list, every mutation persists into the next call.

### The Fix

Use `None` as the default and create the mutable object inside the function body, which runs on every call:

```python
def add_item(item, cart=None):
    if cart is None:
        cart = []
    cart.append(item)
    return cart

print(add_item("apple"))    # ['apple']
print(add_item("bread"))    # ['bread']
```

*A fresh list is created on each call that doesn't supply one.*

**Contrast:** TypeScript evaluates a default expression every time the function is called without that argument, so `function addItem(item, cart = [])` gets a new array per call. Java has no default parameters; the overload that stands in for one typically calls `new ArrayList<>()` each time it runs. Only Python evaluates the default once, at definition — and that single difference is the whole problem.

---

## `*args` and `**kwargs`

A parameter prefixed with `*` collects any **extra positional arguments** into a tuple:

```python
def total(*amounts):
    return sum(amounts)

total(5, 10, 20)    # 35
total()             # 0
```

*`amounts` is a tuple of every positional argument passed, possibly empty.*

This is Python's version of Java's varargs (`int... amounts`).

A parameter prefixed with `**` collects any **extra keyword arguments** into a **dict** — Python's built-in map of keys to values:

```python
def describe(**details):
    for key, value in details.items():
        print(f"{key} = {value}")

describe(name="Ada", role="admin")
# name = Ada
# role = admin
```

*`details` is a dict mapping each keyword argument's name to its value.*

Java has no equivalent to `**kwargs`; a TypeScript options object is the closest analogue.

They can be combined with regular parameters, in this order: regular parameters, then `*args`, then `**kwargs`:

```python
def log_event(event, *tags, **fields):
    print(event, tags, fields)

log_event("login", "auth", "web", user="ada", success=True)
# login ('auth', 'web') {'user': 'ada', 'success': True}
```

*One required parameter, extra positional arguments collected into `tags`, and extra keyword arguments collected into `fields`.*

The names `args` and `kwargs` are only conventions. The `*` and `**` are what matter.

---

## Functions as First-Class Objects

A function is an object like any other. Its name without parentheses refers to the function itself; adding parentheses calls it. That means functions can be bound to other names and passed as arguments:

```python
def shout(text):
    return text.upper() + "!"

speak = shout
print(speak("hello"))               # HELLO!

def apply_twice(func, value):
    return func(func(value))

print(apply_twice(shout, "hi"))     # HI!!
```

*Binding a function to a second name, then passing it into another function that calls it.*

The standard library leans on this constantly. `sorted()` accepts a **`key`** function, called once per item to produce the value to sort by:

```python
words = ["banana", "fig", "Cherry"]

sorted(words)                  # ['Cherry', 'banana', 'fig'] — uppercase sorts first
sorted(words, key=len)         # ['fig', 'banana', 'Cherry']
sorted(words, key=str.lower)   # ['banana', 'Cherry', 'fig']
```

*Passing existing functions as sort keys: sorting by length, then case-insensitively.*

**Contrast:** Java needs a functional interface such as `Function<String, String>` or a method reference like `String::length` to pass behavior around. Java comparators also compare two items at once, while a Python `key` function transforms one item into a sortable value. TypeScript developers already treat functions as values, so this part will feel natural.

---

## `lambda`

A **`lambda`** creates a small anonymous function inline:

```python
products = [("laptop", 1200), ("mouse", 25), ("monitor", 300)]

sorted(products, key=lambda product: product[1])
# [('mouse', 25), ('monitor', 300), ('laptop', 1200)]
```

*Sorting a list of (name, price) tuples by price, using a lambda that returns each tuple's second element.*

A lambda's body is **a single expression**, whose value is returned automatically. It can't contain statements — no assignments, loops, or multi-line logic.

That makes Python's lambda far narrower than Java's `p -> { ... }` or TypeScript's `p => { ... }`. It belongs in one place: a short function passed inline as an argument, like a sort key. Anything more complex should be a named `def`, and binding a lambda to a name (`double = lambda x: x * 2`) is discouraged by PEP 8 — write `def double(x):` instead.

---

## Docstrings

A string literal placed as the first statement in a function body becomes that function's **docstring**:

```python
def convert(amount, rate):
    """Convert an amount into another currency.

    Multiplies amount by the exchange rate and rounds to two decimal places.
    """
    return round(amount * rate, 2)

help(convert)            # displays the signature and docstring
print(convert.__doc__)   # the raw docstring text
```

*A docstring with a one-line summary, a blank line, and further detail, then two ways to read it back.*

The convention is triple double quotes, a one-line summary, and a blank line before any detail.

**Contrast:** Javadoc and JSDoc are comments, read from source by external tools. A Python docstring is a real string stored on the function object at runtime, which is why `help()` in the REPL can display it for any function — including ones from libraries we've never opened.

---

## Parameter and Return Annotations

Parameters can carry type hints after a colon, and the return type follows `->`:

```python
def connect(host: str, port: int = 5432) -> None:
    print(f"Connecting to {host}:{port}")
```

*An annotated function. When a parameter has both an annotation and a default, PEP 8 puts spaces around the `=`.*

As with variable annotations, **nothing enforces them**:

```python
def repeat(text: str, times: int) -> str:
    return text * times

repeat("ab", 3)     # 'ababab'
repeat(3, 4)        # 12 — the annotations are ignored; int * int works fine
```

*Calling an annotated function with the wrong types. Python runs it without complaint and returns an `int`.*

The annotations aren't discarded, though. They remain on the function object, where editors, type checkers, and libraries can read them:

```python
print(repeat.__annotations__)
# {'text': <class 'str'>, 'times': <class 'int'>, 'return': <class 'str'>}
```

*The annotations of `repeat`, still available at runtime.*

Running a type checker such as mypy over the file would flag `repeat(3, 4)`. The interpreter never will.

---

## Key Takeaways

- `def` creates a function object at runtime; a function without a `return` returns `None`.
- Returning comma-separated values returns a tuple, which the caller unpacks — replacing Java's wrapper classes and out-parameters.
- Arguments can be passed by position or keyword; keywords make calls self-documenting and make parameter names part of the interface.
- There's no overloading: a second `def` with the same name replaces the first. Defaults and keyword arguments fill that role.
- Default values are evaluated once, at definition. Mutable defaults are shared across calls; use `None` and create the object inside the function.
- `*args` collects extra positional arguments into a tuple; `**kwargs` collects extra keyword arguments into a dict.
- Functions are objects that can be bound to names and passed as arguments, as with `sorted(key=...)`.
- `lambda` is a single-expression function, best kept to short inline arguments.
- Docstrings are runtime strings readable through `help()`.
- Parameter and return annotations are kept at runtime but never enforced.
