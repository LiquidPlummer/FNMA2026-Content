# Indentation and Control Flow

Python's control flow keywords will look familiar, but the structure around them won't. There are no braces: the indentation *is* the block structure. And some Java staples — the three-part `for` loop, `do`-`while`, block-scoped variables — simply don't exist. This lesson covers how blocks are formed, the branching and looping statements, and the habits worth leaving behind.

---

## Indentation Is Syntax

In Java and TypeScript, braces define blocks, and indentation is cosmetic. A formatter can re-indent an entire file without changing what it does. In Python, **indentation defines blocks**, so changing indentation changes the program.

```python
total = 0
for n in [10, 20, 30]:
    total += n
    print("running total:", total)
```

*The `print` is indented inside the loop, so it runs three times: 10, 30, and 60.*

```python
total = 0
for n in [10, 20, 30]:
    total += n
print("running total:", total)
```

*The only change is the `print` line's indentation. It's now outside the loop and runs once, printing 60.*

The rules:

- **A block** is a run of consecutive lines indented deeper than the line that opened it, all at the same level.
- **A block ends** when indentation returns to an outer level.
- **Four spaces per level** is the PEP 8 standard, and editors should be configured to insert spaces when Tab is pressed.
- **Mixing tabs and spaces** inconsistently raises `TabError`.
- **Unexpected or missing indentation** raises `IndentationError` before the program runs.

```python
if True:
print("hi")     # IndentationError: expected an indented block
```

*A block opener with nothing indented beneath it is a syntax error.*

---

## A Colon Opens a Block

Every statement that introduces a block ends its header line with a **colon**: `if`, `elif`, `else`, `for`, `while`, `def`, and others. The indented lines that follow are the block. A missing colon is the single most common syntax error when coming from a braces language.

There are also **no statement-terminating semicolons**. A newline ends a statement. (A semicolon *can* separate two statements on one line, but it's unidiomatic.) Conditions don't need parentheses either — `if (x > 0):` is legal, but `if x > 0:` is how Python is written.

When a statement needs to span several lines, wrapping it in parentheses (or brackets) lets it continue freely:

```python
annual_total = (first_quarter
                + second_quarter
                + third_quarter
                + fourth_quarter)
```

*Inside parentheses, a statement can span multiple lines, and the continuation lines' indentation doesn't matter.*

---

## `if` / `elif` / `else`

```python
score = 82

if score >= 90:
    grade = "A"
elif score >= 80:
    grade = "B"
elif score >= 70:
    grade = "C"
else:
    grade = "F"

print(grade)    # B
```

*A multi-way branch. Python spells "else if" as the single keyword `elif`.*

- `elif` replaces `else if`. Writing `else if` on one line is a syntax error.
- Conditions use truthiness, so any value can be tested directly.
- `elif` and `else` are both optional, and there can be any number of `elif` clauses.

---

## `while`

```python
attempts = 0

while attempts < 3:
    print("attempt", attempts)
    attempts += 1
```

*Loops while the condition is truthy, printing attempts 0, 1, and 2.*

Python has **no `do`-`while` loop**. When a body must run at least once, the pattern is an infinite loop with an explicit exit:

```python
while True:
    answer = input("Continue? (y/n) ")
    if answer == "y" or answer == "n":
        break
```

*The body always runs at least once, and `break` exits once a valid answer arrives.*

---

## `for ... in ...`

Python's `for` loop has exactly one form: it walks through the **items** of something iterable, binding each item to the loop variable in turn.

```python
languages = ["Java", "TypeScript", "Python"]

for language in languages:
    print(language)

for letter in "abc":
    print(letter)
```

*Iterating directly over the items of a list, then over the characters of a string.*

This is Java's enhanced `for (String language : languages)` and TypeScript's `for...of`. The C-style three-part loop doesn't exist in Python.

**Contrast for TypeScript developers:** JavaScript's `for...in` iterates *keys* (for an array, the indexes as strings). Python's `for ... in` iterates *values*. Same keywords, different behavior.

---

## `range()`

When we need a sequence of integers, `range()` produces one:

```python
list(range(5))            # [0, 1, 2, 3, 4]
list(range(2, 6))         # [2, 3, 4, 5]
list(range(10, 0, -3))    # [10, 7, 4, 1]
```

*`range(stop)`, `range(start, stop)`, and `range(start, stop, step)`. The stop value is always excluded. `list()` is used here only so we can see the values.*

A `range` is lazy: it produces numbers as the loop asks for them rather than building a list, so `range(1_000_000)` costs almost nothing. It's the right tool when we need the numbers themselves — "do this five times," or counting through a numeric range.

```python
for attempt in range(3):
    print("attempt", attempt)
```

*Repeating a block a fixed number of times.*

### The Java Habit

`for (int i = 0; i < n; i++)` has no direct Python equivalent, and the most common tell of a Java developer writing Python is recreating it with `range(len(...))`:

```python
names = ["Ada", "Grace", "Linus"]

# Java accent: loop over indexes, then look up each item
for i in range(len(names)):
    print(names[i])

# Idiomatic: loop over the items
for name in names:
    print(name)
```

*Both loops print the same three names. The second says what it means without the index bookkeeping.*

When the index genuinely is needed alongside the item, `enumerate()` supplies both:

```python
for position, name in enumerate(names, start=1):
    print(position, name)
```

*Prints `1 Ada`, `2 Grace`, `3 Linus`. `enumerate` pairs each item with a counter, starting from `start`.*

---

## `break`, `continue`, and Loop `else`

`break` exits the loop immediately and `continue` skips to the next iteration, exactly as in Java:

```python
readings = [12, 7, -3, 9]

for reading in readings:
    if reading < 0:
        continue
    print(reading)
```

*Skips the negative reading and prints 12, 7, and 9.*

Python has **no labeled `break`**. Java's `break outer;` for escaping nested loops has no counterpart.

### The `else` Clause on a Loop

Both `for` and `while` loops accept an `else` clause, which runs **only if the loop finished without hitting `break`**:

```python
readings = [12, 7, -3, 9]

for reading in readings:
    if reading < 0:
        print("Found a negative reading:", reading)
        break
else:
    print("All readings valid")
```

*Prints "Found a negative reading: -3". If no reading were negative, the loop would run to completion and the `else` block would print "All readings valid".*

Reading the `else` as "**no break**" makes it click. It replaces the `boolean found = false;` flag variable that search loops need in Java and TypeScript, neither of which has anything equivalent.

---

## `pass`

A block must contain at least one statement, and Python has no empty braces to fall back on. **`pass`** is a statement that does nothing:

```python
for reading in readings:
    if reading < 0:
        pass        # TODO: decide how to handle negative readings
    else:
        print(reading)
```

*`pass` holds the place of a block whose logic hasn't been written yet.*

A comment alone doesn't count as a statement, so a block containing only a comment is an `IndentationError`. `pass` is the explicit way to say "intentionally empty," and it's most common in placeholders and stubs.

---

## No Block Scope

In Java, a variable declared inside an `if` block or a `for` loop disappears at the closing brace. **Python's `if`, `for`, and `while` do not create a new scope.** A name bound inside one of those blocks remains available afterward, in the surrounding function or module:

```python
if True:
    message = "set inside the if"

print(message)      # set inside the if

for i in range(3):
    pass

print(i)            # 2 — the loop variable survives the loop
```

*Names bound inside an `if` block and by a `for` loop are still bound after the block ends.*

The flip side: if the block never runs, the name is never bound at all.

```python
for item in []:
    last = item

print(last)         # NameError: name 'last' is not defined
```

*The loop body never executes over an empty list, so `last` is never created.*

Functions *do* create their own scope — that's where Python draws scope boundaries, rather than at every indented block.

---

## Key Takeaways

- Indentation is syntax: blocks are defined by indentation, and changing it changes what the program does.
- A colon opens every block. There are no braces and no statement-terminating semicolons.
- Branching uses `if` / `elif` / `else`; there's no `else if`.
- `while` loops on a truthy condition; there's no `do`-`while`, so use `while True:` with `break`.
- `for ... in ...` iterates the items themselves, not indexes. Reaching for `range(len(...))` is a Java habit; use `enumerate()` when an index is truly needed.
- `range()` lazily produces integers, with the stop value excluded.
- `break` and `continue` work as in Java, without labels. A loop's `else` runs only when no `break` occurred.
- `pass` is an explicit empty block.
- `if`, `for`, and `while` don't create scope — names bound inside them outlive the block.
