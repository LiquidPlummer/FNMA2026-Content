# Iteration and Comprehensions

Nearly everything in Python can be looped over, and the language builds a lot on that single idea. **Comprehensions** turn a loop that builds a collection into one expression, and **generators** produce values only as they're asked for. Java developers will be tempted to map these onto streams, and the mapping is close — except that in Python this is the ordinary way to build a list, not a deliberate stylistic choice.

---

## Iterables and Iterators

Two related terms describe how looping works:

- An **iterable** is anything that can be looped over: lists, tuples, strings, dicts, sets, ranges, files. Formally, it's anything the built-in `iter()` accepts.
- An **iterator** is the object that actually hands out the values, one at a time, each time `next()` is called on it. When it runs out, it signals the end by raising `StopIteration`.

```python
colors = ["red", "green"]
it = iter(colors)

print(next(it))     # red
print(next(it))     # green
print(next(it))     # StopIteration
```

*Asking a list for an iterator, then pulling values out of it by hand until it's exhausted.*

### What a `for` Loop Actually Does

`for color in colors:` performs exactly those steps for us:

1. Calls `iter(colors)` once to get a fresh iterator.
2. Calls `next()` on it before each pass, binding the result to `color`.
3. Stops quietly when `next()` raises `StopIteration`.

That's the entire protocol, and it's why a `for` loop works identically over a list, a string, a file, or a dict.

### Iterators Are One-Shot

A list is an iterable, not an iterator: every `for` loop over it gets a new iterator starting from the beginning. But some objects *are* iterators, and once consumed, they stay empty:

```python
pairs = zip(["a", "b"], [1, 2])

print(list(pairs))    # [('a', 1), ('b', 2)]
print(list(pairs))    # [] — the iterator was used up by the first call
```

*`zip` returns an iterator, so the second pass over it produces nothing.*

`zip`, `enumerate`, `map`, `filter`, file objects, and generators are all one-shot. When we need to walk the results more than once, we store them in a list first.

---

## `enumerate`, `zip`, and `sorted(key=...)`

Three built-ins handle the most common looping needs.

**`enumerate`** pairs each item with a counter, as we saw with control flow:

```python
for position, name in enumerate(["Ada", "Grace"], start=1):
    print(position, name)       # 1 Ada, then 2 Grace
```

*Numbering items without managing an index variable.*

**`zip`** walks several iterables in step, producing a tuple from each position:

```python
names = ["Ada", "Grace", "Linus"]
languages = ["Analytical Engine", "COBOL", "C"]

for name, language in zip(names, languages):
    print(f"{name} → {language}")

lookup = dict(zip(names, languages))    # {'Ada': 'Analytical Engine', ...}
```

*Pairing two parallel lists in a loop, then turning the pairs into a dict.*

`zip` stops at the end of the **shortest** input, silently dropping any leftovers from longer ones.

**`sorted(key=...)`** sorts by whatever the key function returns for each item:

```python
employees = [
    {"name": "Ada", "dept": "eng", "salary": 120},
    {"name": "Grace", "dept": "eng", "salary": 135},
    {"name": "Linus", "dept": "ops", "salary": 110},
]

by_salary = sorted(employees, key=lambda e: e["salary"], reverse=True)
by_dept_then_pay = sorted(employees, key=lambda e: (e["dept"], -e["salary"]))
```

*Sorting records by one field, then by two: department ascending, salary descending.*

Returning a tuple from the key sorts by several fields at once, because tuples compare element by element. Negating a number flips its direction within that tuple. `min()` and `max()` accept the same `key` argument.

---

## List Comprehensions

A **list comprehension** builds a list from an iterable in a single expression. The output expression comes **first**, followed by the loop:

```python
prices = [19.99, 5.00, 42.50, 3.25]

with_tax = [round(p * 1.08, 2) for p in prices]
print(with_tax)     # [21.59, 5.4, 45.9, 3.51]
```

*Producing a new list with tax applied to every price.*

It's exactly equivalent to this loop:

```python
with_tax = []
for p in prices:
    with_tax.append(round(p * 1.08, 2))
```

*The loop a list comprehension replaces.*

An `if` at the end filters which items are included:

```python
cheap = [p for p in prices if p < 10]
print(cheap)        # [5.0, 3.25]
```

*Keeping only the prices under 10.*

The way to read one aloud is: "*this expression*, for each item in *that iterable*, if *this condition*."

Position matters for conditionals. An `if` **after** the loop filters items out. A conditional expression **before** the loop transforms every item and keeps them all:

```python
labels = ["cheap" if p < 10 else "pricey" for p in prices]
print(labels)       # ['pricey', 'cheap', 'pricey', 'cheap']
```

*A conditional expression in the output position chooses a value for each item; nothing is dropped.*

---

## Dict and Set Comprehensions

The same syntax in braces builds a dict or a set. A colon in the output makes it a dict:

```python
names = ["Ada", "Grace", "Linus"]

name_lengths = {name: len(name) for name in names}
print(name_lengths)     # {'Ada': 3, 'Grace': 5, 'Linus': 5}

emails = ["ada@eng.io", "grace@ops.io", "linus@eng.io"]
domains = {email.split("@")[1] for email in emails}
print(domains)          # {'eng.io', 'ops.io'}
```

*A dict comprehension mapping each name to its length, and a set comprehension collecting unique domains.*

Swapping a dict's keys and values is a common one-liner: `{value: key for key, value in original.items()}`.

---

## Generator Expressions and Laziness

Replacing the square brackets with parentheses gives a **generator expression**. It doesn't build a collection at all — it produces each value only when something asks for the next one:

```python
squares = (n * n for n in range(10_000_000))    # instant; nothing computed yet

print(next(squares))    # 0
print(next(squares))    # 1
```

*A generator expression over ten million numbers costs almost no memory, because values are computed one at a time on demand.*

This **laziness** is ideal when the values are fed straight into a function that consumes an iterable. When the generator expression is the only argument, it doesn't need its own parentheses:

```python
total = sum(p * 1.08 for p in prices)
has_expensive = any(p > 40 for p in prices)
most = max(len(name) for name in names)
```

*Feeding generator expressions directly into `sum`, `any`, and `max` without ever building an intermediate list.*

`any()` also stops at the first `True` it sees, so with a generator the remaining values are never computed. Like every iterator, a generator expression is one-shot.

---

## `yield` and Generator Functions

A function containing **`yield`** is a **generator function**. Calling it doesn't run its body — it returns a generator object. Each `next()` runs the body until the next `yield`, hands that value out, and pauses there with all its local variables intact:

```python
def countdown(start):
    while start > 0:
        yield start
        start -= 1

for n in countdown(3):
    print(n)        # 3, 2, 1
```

*Each `yield` produces one value and suspends the function until the loop asks for the next.*

Generator functions shine when producing values needs state or several statements — more than a single expression can express:

```python
def batches(items, size):
    batch = []
    for item in items:
        batch.append(item)
        if len(batch) == size:
            yield batch
            batch = []
    if batch:
        yield batch

print(list(batches(range(7), 3)))    # [[0, 1, 2], [3, 4, 5], [6]]
```

*A generator that groups any iterable into fixed-size chunks, yielding a final partial chunk if one remains.*

**Contrast:** a custom Java iterator means writing a class that implements `Iterator<T>`, with state held in fields and logic split across `hasNext()` and `next()`. A generator function keeps that state in ordinary local variables. TypeScript's `function*` generators are nearly the same feature; the difference is how routinely Python code uses them.

---

## When a Comprehension Has Gotten Unreadable

Comprehensions are for simple transformations and filters. They get hard to read quickly:

```python
report = [f"{u['name']}: {o['total']}" for u in users if u["active"] for o in orders if o["user_id"] == u["id"] and o["total"] > 100]
```

*Two loops and three conditions in one expression — technically valid, and hard to verify.*

The same logic as a plain loop is longer, but each step can be read on its own:

```python
report = []
for user in users:
    if not user["active"]:
        continue
    for order in orders:
        if order["user_id"] == user["id"] and order["total"] > 100:
            report.append(f"{user['name']}: {order['total']}")
```

*The unreadable comprehension rewritten as nested loops.*

Reasonable signals that it's time for a loop:

- More than one `for` clause.
- A condition complex enough to need `and`, `or`, or a comment.
- The line no longer fits comfortably on screen.
- The comprehension exists for a side effect. `[print(x) for x in items]` builds a throwaway list of `None` values — a loop is the correct tool.

---

## No Fluent Chain

Java developers build collections by chaining stream stages:

```java
List<String> names = employees.stream()
    .filter(e -> e.salary() > 115)
    .map(Employee::name)
    .collect(Collectors.toList());
```

*A Java stream that filters employees and maps them to names.*

In Python, a filter plus a map is a single comprehension:

```python
names = [e["name"] for e in employees if e["salary"] > 115]
```

*The same filter-then-map as one list comprehension.*

**Contrast:** a Java stream is an opt-in style layered on top of loops; a comprehension is simply how Python code builds a list. And there's no `.stream().filter().map().sorted().limit()` chain to extend — each extra stage becomes an intermediate variable:

```python
well_paid = [e for e in employees if e["salary"] > 100]
ranked = sorted(well_paid, key=lambda e: e["salary"], reverse=True)
top_two = [e["name"] for e in ranked[:2]]
```

*Filter, sort, and take the top two, written as three named steps.*

Python does have `map()` and `filter()` built-ins, but they nest inside-out rather than chaining left to right, so a comprehension is almost always the more readable choice.

---

## Key Takeaways

- An iterable can be looped over; an iterator hands out values through `next()` and signals the end with `StopIteration`. A `for` loop does this automatically.
- `zip`, `enumerate`, `map`, `filter`, files, and generators are one-shot iterators — store results in a list to reuse them.
- `enumerate` adds a counter, `zip` walks iterables in step and stops at the shortest, and `sorted(key=...)` sorts by a computed value, with tuple keys for multiple fields.
- A comprehension puts the output expression first: `[expr for item in iterable if condition]`. Braces with `key: value` make a dict; braces alone make a set.
- An `if` after the loop filters; `a if cond else b` before the loop transforms every item.
- A generator expression `(expr for ...)` computes values lazily and can be passed straight to `sum`, `any`, `all`, `min`, or `max`.
- A function containing `yield` returns a generator that pauses at each `yield` with its local state preserved.
- When a comprehension needs multiple loops, complex conditions, or side effects, write a loop.
- There's no fluent chain: a filter and map is one comprehension, and further stages become intermediate variables.
