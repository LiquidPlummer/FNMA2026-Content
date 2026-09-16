# Dictionaries and Sets

The **dict** is Python's built-in mapping from keys to values, and it's everywhere: configuration, JSON data, keyword arguments, and the attributes of objects themselves. Where Java reaches for a `HashMap` or a small class, Python code usually reaches for a dict. Its sibling, the **set**, is an unordered collection of unique values built on the same hashing machinery.

---

## Dict Literals and Key Access

A dict literal is a comma-separated run of `key: value` pairs inside braces:

```python
user = {"name": "Ada", "role": "admin", "active": True}

print(user["name"])              # Ada
user["role"] = "owner"           # update an existing key
user["email"] = "ada@example.com"  # add a new key
del user["active"]               # remove a key

print("email" in user)           # True — `in` checks keys, not values
print(len(user))                 # 3

user["phone"]                    # KeyError: 'phone'
```

*Reading, updating, adding, and deleting entries. Square-bracket access to a missing key raises `KeyError`.*

There's no separate `put` method: assigning to a key either updates it or creates it.

### `get()` with a Default

When a key may be missing, **`get()`** returns `None` — or a default we supply — instead of raising:

```python
print(user.get("phone"))          # None
print(user.get("phone", "n/a"))   # n/a
```

*Reading a possibly-missing key without risking a `KeyError`.*

A common idiom combines `get()` with a default of zero to count things:

```python
counts = {}
for word in ["red", "blue", "red"]:
    counts[word] = counts.get(word, 0) + 1

print(counts)     # {'red': 2, 'blue': 1}
```

*Each word's count starts at zero the first time it's seen.*

**Contrast:** Java's `map.get(key)` quietly returns `null` for a missing key, and `getOrDefault` supplies a fallback. In Python the square-bracket form is the strict one that raises, and `get()` is the lenient one.

### `setdefault()`

**`setdefault(key, default)`** returns the value for `key` if it exists; otherwise it inserts `default` under that key and returns it. That makes grouping a one-liner:

```python
orders = [("ada", "book"), ("grace", "pen"), ("ada", "lamp")]

by_customer = {}
for customer, item in orders:
    by_customer.setdefault(customer, []).append(item)

print(by_customer)    # {'ada': ['book', 'lamp'], 'grace': ['pen']}
```

*The first time a customer appears, an empty list is inserted for them; after that, the existing list is returned and appended to.*

---

## Insertion Order Is Guaranteed

A dict remembers the order keys were first inserted, and iterating it follows that order. This is a language guarantee, not an implementation accident:

```python
steps = {}
steps["checkout"] = 1
steps["build"] = 2
steps["deploy"] = 3

print(list(steps))    # ['checkout', 'build', 'deploy']
```

*Keys come back in the order they were added.*

Updating an existing key keeps its position. Deleting a key and adding it again moves it to the end.

**Contrast:** Java's `HashMap` makes no ordering promise, and code that needs one reaches for `LinkedHashMap`. Python's everyday dict already behaves like `LinkedHashMap`, as does a JavaScript `Map`.

---

## Keys Must Be Hashable

A dict finds a key by computing its **hash** — an integer derived from the key's value — and jumping straight to the matching slot. If a key's value could change after insertion, its hash would change and the dict could never find it again. So keys must be **hashable**, which in practice means immutable:

- **Hashable:** `int`, `float`, `str`, `bool`, `None`, and tuples whose items are all hashable.
- **Not hashable:** `list`, `dict`, `set`, and anything containing one of those.

```python
locations = {(40.7, -74.0): "New York", (51.5, -0.1): "London"}
print(locations[(40.7, -74.0)])     # New York

bad = {["a", "b"]: 1}               # TypeError: unhashable type: 'list'
also_bad = {(1, [2, 3]): "x"}       # TypeError: unhashable type: 'list'
```

*Tuples of numbers work as keys; a list, or a tuple containing a list, doesn't.*

Values have no such restriction — a dict's values can be anything at all.

---

## `keys()`, `values()`, and `items()`

Three methods expose a dict's contents:

```python
prices = {"apple": 1.25, "bread": 3.50, "milk": 2.10}

print(list(prices.keys()))      # ['apple', 'bread', 'milk']
print(list(prices.values()))    # [1.25, 3.5, 2.1]
print(list(prices.items()))     # [('apple', 1.25), ('bread', 3.5), ('milk', 2.1)]

for item, price in prices.items():
    print(f"{item}: ${price:.2f}")
```

*Listing keys, values, and key-value pairs, then unpacking each pair in a loop.*

Iterating a dict directly (`for item in prices:`) walks its keys, so `.keys()` is rarely needed in a loop. When we want both halves, `.items()` with unpacking is the idiom.

These methods return **views**, not copies: they reflect later changes to the dict. That liveness has a consequence — adding or removing keys while looping over a dict raises `RuntimeError: dictionary changed size during iteration`. Looping over a snapshot such as `list(prices)` avoids it.

---

## The Dict as a General-Purpose Record

Because a dict holds named fields of any type, it's Python's default shape for a record — the same shape JSON data has:

```python
employee = {
    "id": 1042,
    "name": "Grace",
    "skills": ["COBOL", "compilers"],
    "manager": None,
}

print(employee["skills"][0])    # COBOL
```

*A record with mixed value types, including a nested list.*

This is convenient, but it's not free. A misspelled key like `employee["nmae"]` is only caught when that line runs, and nothing documents which keys a record is supposed to have. Once a record needs guaranteed fields or behavior of its own, a class is the better fit — that's unit 003.

**Contrast:** a dict does the work of both a `Map` and a small POJO or TypeScript interface. This is why Python codebases contain far fewer tiny data classes than Java ones: a function can simply return a dict.

---

## Merging Dicts

The `|` operator merges two dicts into a new one. When both contain the same key, the right-hand value wins:

```python
defaults = {"host": "localhost", "port": 5432, "debug": False}
overrides = {"port": 6543, "debug": True}

config = defaults | overrides
print(config)       # {'host': 'localhost', 'port': 6543, 'debug': True}

defaults |= overrides   # merge into defaults in place
```

*Layering overrides on top of defaults. The merged dict keeps the key order of the left-hand side.*

The older spelling unpacks both dicts into a new literal with `**`, and is still common:

```python
config = {**defaults, **overrides}
```

*Equivalent to `defaults | overrides`, and the direct analogue of JavaScript's `{...defaults, ...overrides}`.*

`**` also unpacks a dict into keyword arguments at a call site — `connect(**config)` passes `host=`, `port=`, and `debug=` — the mirror image of `**kwargs` collecting them.

---

## Sets

A **set** is an unordered collection of unique, hashable values:

```python
tags = {"python", "java", "python"}
print(tags)             # {'java', 'python'} — duplicates collapse; order not guaranteed

tags.add("go")
tags.remove("java")     # raises KeyError if the value isn't present
tags.discard("rust")    # removes if present; no error if not

unique_ids = set([3, 1, 3, 2, 1])
print(unique_ids)       # {1, 2, 3}
```

*Creating a set, adding and removing values, and deduplicating a list.*

Sets have no positions, so `tags[0]` is a `TypeError`. And because they're unordered, deduplicating with `set()` doesn't preserve the original order.

### The Empty-Set Gotcha

Braces with nothing in them make an empty **dict**, not an empty set:

```python
empty_dict = {}
empty_set = set()

print(type({}))         # <class 'dict'>
print(type(set()))      # <class 'set'>
```

*Dicts claimed `{}` first, so an empty set has to be written `set()`.*

### `frozenset`

A **`frozenset`** is an immutable set. Because it can't change, it's hashable, so it can be a dict key or a member of another set:

```python
roles = {
    frozenset({"read"}): "viewer",
    frozenset({"read", "write"}): "editor",
}

print(roles[frozenset({"write", "read"})])    # editor — element order doesn't matter
```

*Using frozensets of permissions as dict keys.*

---

## Set Operations

Sets support the operations of mathematical sets, written as operators:

```python
backend = {"python", "java", "sql"}
frontend = {"typescript", "css", "sql"}

backend | frontend      # union: all six skills
backend & frontend      # intersection: {'sql'}
backend - frontend      # difference: {'python', 'java'}
backend ^ frontend      # symmetric difference: in exactly one of the two

{"sql"} <= backend      # True — subset test
```

*Combining and comparing two sets of skills.*

Each operator also has a method form — `union()`, `intersection()`, `difference()` — which accepts any iterable rather than only another set: `backend.union(["go", "rust"])`.

### Membership Cost

`in` reads the same on a list and a set, but the work behind it is very different. A list is checked **item by item**, so a lookup gets slower as the list grows. A set (like a dict) hashes the value and checks one slot, so a lookup takes roughly the same time at any size:

```python
blocked_list = load_blocked_ids()    # imagine 100,000 ids
blocked = set(blocked_list)          # convert once

for request in requests:
    if request.user_id in blocked:   # fast, no matter how many ids there are
        ...
```

*Converting a large list to a set once, then running many membership checks against it.*

When a collection exists mainly to answer "is this in here?", it should be a set.

---

**Contrast:** JavaScript object keys are always strings — `{1: "a", "1": "b"}` produces a single key, and using an array as a key silently turns it into the string `"0,0"`. A Python dict key is any hashable object, compared by value: `1` and `"1"` are different keys, and the tuple `(0, 0)` stays a tuple.

---

## Key Takeaways

- A dict maps hashable keys to arbitrary values. `d[key]` raises `KeyError` when the key is missing; `d.get(key, default)` doesn't.
- `setdefault()` returns an existing value or inserts and returns a default, which makes grouping concise.
- Dicts preserve insertion order as a language guarantee.
- Keys must be hashable — immutable built-ins and tuples of them. Lists, dicts, and sets can't be keys.
- `keys()`, `values()`, and `items()` return live views; iterating a dict walks its keys. Don't add or remove keys while looping over one.
- A dict is Python's default record type, doing the work of both a `Map` and a small data class.
- `a | b` and `{**a, **b}` merge dicts, with the right-hand side winning.
- A set holds unique hashable values with no order; `{}` is an empty dict, so an empty set is `set()`. A `frozenset` is the immutable, hashable variant.
- `|`, `&`, `-`, and `^` give union, intersection, difference, and symmetric difference.
- `in` on a set or dict is fast at any size; on a list it scans every item.
