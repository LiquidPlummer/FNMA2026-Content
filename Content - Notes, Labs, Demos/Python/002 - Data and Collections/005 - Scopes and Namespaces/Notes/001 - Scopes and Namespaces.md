# Scopes and Namespaces

Java and TypeScript scope names to blocks: a variable declared inside braces disappears at the closing brace. Python draws scope boundaries in different places — at functions and modules, never at `if` or `for` — and it decides whether a name is local by looking for assignments anywhere in the function. Most of the surprising errors in this lesson come from that one rule.

---

## Namespaces

A **namespace** is a mapping from names to objects — conceptually, a dict. Python keeps several of them at once:

- **Built-in:** names available everywhere without importing, like `print`, `len`, `list`, and `ValueError`.
- **Global (module):** names bound at the top level of a module — its imports, functions, classes, and module-level variables.
- **Local:** names bound inside a function call. A fresh local namespace is created each time the function runs, and discarded when it returns.

Namespaces are why two modules can each define a function called `load` without conflict: they live in different module namespaces, reached as `config.load` and `data.load`. The dot is a lookup in that module's namespace.

A **scope** is the region of code where a namespace's names can be used without qualification. The question this lesson answers is: when code mentions a bare name, which namespace does Python look in?

---

## LEGB Resolution

Python searches for a name in four scopes, in a fixed order, and uses the first match:

1. **L — Local:** names bound in the current function.
2. **E — Enclosing:** names bound in any function that *contains* the current one, from the nearest outward.
3. **G — Global:** names bound at the top level of the current module.
4. **B — Built-in:** Python's built-in names.

If the search reaches the end without a match, Python raises `NameError`.

```python
tax_rate = 0.08                             # global

def make_pricer(markup):                    # markup is local to make_pricer
    def price(cost):                        # cost is local to price
        return round(cost * (1 + markup) * (1 + tax_rate), 2)
    return price

retail = make_pricer(0.25)
print(retail(100))                          # 135.0
```

*Inside `price`, `cost` is found in Local, `markup` in Enclosing, `tax_rate` in Global, and `round` in Built-in.*

Note that "global" in Python means **module-level**. There's no program-wide global namespace; another module's globals are reached through an import.

---

## No Block Scope

As covered with control flow, `if`, `for`, `while`, and `with` don't create scopes. A name bound inside them belongs to the enclosing function or module:

```python
def find_first_negative(values):
    for value in values:
        if value < 0:
            found = value
            break
    print(value)        # the loop variable is still bound after the loop
    return found
```

*Both `value` and `found` are locals of the function, not of the loop or the `if` block.*

Only four constructs create a new scope: **modules**, **functions** (including `lambda`), **classes**, and **comprehensions**. That last one is worth noticing — the loop variable of a comprehension does *not* leak:

```python
squares = [n * n for n in range(3)]
print(n)                # NameError: name 'n' is not defined
```

*A comprehension's loop variable is local to the comprehension, unlike a `for` loop's.*

**Contrast:** `let` and `const` in TypeScript, and every local variable in Java, are scoped to their enclosing block. Python binds at the function level, so a name created deep inside nested blocks is visible throughout the rest of the function.

---

## Assignment Makes a Name Local

Here is the rule behind most scope surprises: **if a function assigns to a name anywhere in its body, that name is local for the entire function** — including the lines *before* the assignment.

Reading a global from inside a function works fine:

```python
count = 0

def show():
    print(count)        # no assignment to count here, so it's found in Global

show()                  # 0
```

*A function that only reads a module-level name.*

Add an assignment, and the same read fails:

```python
count = 0

def increment():
    count += 1          # UnboundLocalError

increment()
```

*`count += 1` assigns to `count`, so `count` is local throughout `increment` — and the local hasn't been given a value yet when `+=` tries to read it.*

The assignment doesn't even need to come first:

```python
total = 10

def report():
    print(total)        # UnboundLocalError — the assignment below makes total local
    total = 20
```

*Python decides which names are local when it compiles the function, by scanning the whole body for assignments.*

This is decided when the function is **compiled to bytecode**, before any of it runs — the same compilation step covered in How Python Runs. That's why an assignment further down still affects a line above it.

Two related points prevent confusion:

- **Mutating isn't assigning.** `items.append(x)` inside a function changes a global list without making `items` local, because the name `items` is never rebound.
- **Assignment includes more than `=`.** `+=`, a `for` loop variable, an `import`, a `def`, and `except ... as e` all bind names, and all make those names local.

---

## `global` and `nonlocal`

When a function genuinely needs to **rebind** a name in an outer scope, it has to say so.

**`global`** declares that a name refers to the module-level binding:

```python
count = 0

def increment():
    global count
    count += 1

increment()
increment()
print(count)        # 2
```

*`global count` makes the assignment rebind the module-level name instead of creating a local.*

**`nonlocal`** does the same for the nearest **enclosing function's** name:

```python
def make_counter():
    count = 0
    def increment():
        nonlocal count
        count += 1
        return count
    return increment

counter = make_counter()
print(counter(), counter(), counter())    # 1 2 3
```

*`nonlocal count` lets the inner function rebind the `count` belonging to `make_counter`.*

Both are legitimate but best used sparingly. A function that rebinds globals is hard to test and reason about; returning a value and letting the caller assign it is almost always cleaner.

---

## Closures

The counter above works because of a **closure**: an inner function keeps access to the enclosing function's names even after the enclosing function has returned.

```python
def make_multiplier(factor):
    def multiply(x):
        return x * factor
    return multiply

double = make_multiplier(2)
triple = make_multiplier(3)
print(double(10), triple(10))       # 20 30
```

*Each call to `make_multiplier` creates a new `multiply` that remembers its own `factor`.*

### Closures Capture Variables, Not Values

A closure remembers the **variable**, and looks up its value when the inner function is *called*, not when it's created:

```python
multipliers = []
for factor in [1, 2, 3]:
    def multiply(x):
        return x * factor
    multipliers.append(multiply)

print([m(10) for m in multipliers])   # [30, 30, 30]
```

*All three functions share the single variable `factor`, which holds `3` by the time any of them runs.*

One fix uses a default argument, since defaults are evaluated once, at definition — the same behavior behind the mutable default trap, used deliberately:

```python
for factor in [1, 2, 3]:
    def multiply(x, factor=factor):
        return x * factor
    multipliers.append(multiply)
```

*Each function captures the current value of `factor` as its own default.*

**Contrast:** Java prevents this bug outright — lambdas may only capture local variables that are effectively final. In TypeScript, a `for (let ...)` loop creates a fresh binding on each iteration, so the equivalent code gives `[10, 20, 30]`; only the old `var` behaves like Python.

---

## `globals()`, `locals()`, and `dir()`

Three built-ins let us look inside namespaces directly:

```python
def show_scope(a):
    b = a * 2
    print(locals())             # {'a': 5, 'b': 10}

show_scope(5)

print("show_scope" in globals())   # True — the function is a module-level name
print(dir())                       # sorted list of names in the current scope
print(dir("hello")[-5:])           # the last few attributes of a str object
```

*`locals()` and `globals()` return dicts of the current local and module namespaces; `dir()` lists names in the current scope or on an object.*

- **`locals()`** is a snapshot for inspection. Changing the dict it returns doesn't change the function's variables.
- **`globals()`** returns the module's actual namespace dict.
- **`dir(obj)`** lists an object's attributes, which makes it one of the fastest ways to explore an unfamiliar object in the REPL.

---

## Shadowing Built-ins

Because Global is searched before Built-in, a module-level name can **shadow** a built-in of the same name. Nothing warns us, and the damage often shows up far from the cause:

```python
list = ["apples", "bread"]        # near the top of a module

# ... many lines later ...
ids = list(range(5))              # TypeError: 'list' object is not callable
```

*Binding `list` at module level hides the built-in `list` type for the rest of the module.*

LEGB explains exactly what happened: the call finds `list` in Global and never reaches Built-in. Names that are especially easy to shadow include `list`, `dict`, `str`, `id`, `type`, `input`, `sum`, `min`, `max`, and `filter`. The fix is simply a more specific name — `groceries`, `user_id`. In the REPL, `del list` removes the shadowing global and restores access to the built-in.

---

**Contrast:** Java's compiler rejects code that might read a local variable before assigning it — `variable result might not have been initialized`. Python raises at runtime, and only on the path that actually executes:

```python
def describe(score):
    if score >= 50:
        result = "pass"
    return result

print(describe(80))     # pass
print(describe(30))     # UnboundLocalError
```

*`result` is local because it's assigned in the function, but the branch that assigns it only runs for some inputs.*

A test that happens to use only passing scores never reveals the bug.

---

## Key Takeaways

- A namespace maps names to objects. Python keeps built-in, module-level, and per-call local namespaces.
- Bare names are resolved in LEGB order — Local, Enclosing, Global, Built-in — and the first match wins.
- "Global" means module-level. Only modules, functions, classes, and comprehensions create scopes; `if`, `for`, and `while` don't.
- Any assignment to a name anywhere in a function makes that name local for the whole function, which causes `UnboundLocalError` when it's read first.
- Mutating an object isn't assignment; rebinding is.
- `global` rebinds a module-level name; `nonlocal` rebinds an enclosing function's name. Prefer returning values.
- Closures keep enclosing names alive after the outer function returns, and capture variables rather than values.
- `globals()`, `locals()`, and `dir()` expose namespaces for inspection.
- Module-level names like `list` or `id` shadow built-ins silently.
- Use-before-assignment errors appear at runtime, only on the path that runs.
