# Dunder Methods

`len(x)`, `x + y`, `x == y`, `for item in x`, `with x:` — all of this syntax works by calling specially named methods on the objects involved. These are **dunder methods**, short for "double underscore," because every one is spelled `__name__`. Implementing them lets our own classes plug into the language exactly like built-in types do, without declaring an interface anywhere.

---

## The Protocol Idea

When Python evaluates `len(playlist)`, it calls `playlist.__len__()`. When it evaluates `a + b`, it calls `a.__add__(b)`. Each piece of syntax or built-in function dispatches to a matching dunder method:

| We write | Python calls |
|----------|--------------|
| `len(x)` | `x.__len__()` |
| `str(x)`, `print(x)` | `x.__str__()` |
| `repr(x)` | `x.__repr__()` |
| `x == y` | `x.__eq__(y)` |
| `hash(x)` | `x.__hash__()` |
| `item in x` | `x.__contains__(item)` |
| `for item in x` | `x.__iter__()` |
| `x[key]` | `x.__getitem__(key)` |
| `x + y` | `x.__add__(y)` |
| `with x:` | `x.__enter__()` and `x.__exit__(...)` |

A group of related dunders is called a **protocol** — the iteration protocol, the container protocol, the context manager protocol. We implement dunder methods; we almost never call them directly. Code calls `len(x)`, not `x.__len__()`.

---

## `__str__` vs `__repr__`

Python has two string representations, aimed at two different readers:

- **`__repr__`** is for **developers**: unambiguous, and ideally something that looks like the code to recreate the object. It's what the REPL echoes and what appears inside containers and error messages.
- **`__str__`** is for **end users**: readable and friendly. It's what `print()` and `str()` use. If a class doesn't define it, Python falls back to `__repr__`.

Without either, we get the unhelpful default inherited from `object`:

```python
class Playlist:
    def __init__(self, name, songs):
        self.name = name
        self.songs = list(songs)

print(Playlist("Focus", ["Weightless"]))
# <__main__.Playlist object at 0x0000021F4A7C3D90>
```

*The default representation shows only the class and a memory address.*

Adding both:

```python
class Playlist:
    def __init__(self, name, songs):
        self.name = name
        self.songs = list(songs)

    def __repr__(self):
        return f"Playlist({self.name!r}, {self.songs!r})"

    def __str__(self):
        return f"{self.name} ({len(self.songs)} songs)"

focus = Playlist("Focus", ["Weightless", "Clair de Lune"])

print(focus)        # Focus (2 songs)                                — __str__
print([focus])      # [Playlist('Focus', ['Weightless', 'Clair de Lune'])]  — __repr__
print(f"{focus!r}") # Playlist('Focus', ['Weightless', 'Clair de Lune'])    — __repr__
```

*`print` uses `__str__`, but a list prints its items with `__repr__`. The `!r` suffix in an f-string requests the repr instead of the str.*

If we only write one, write `__repr__` — it covers both cases, and it's the one we'll see while debugging.

**Contrast:** Java has one `toString()` serving both audiences. Python splits the job, which is why printing a list of objects can look completely different from printing one of them.

---

## `__eq__` and `__hash__`

By default, `==` on instances of our own classes compares **identity**, just like `is`:

```python
class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y

print(Point(1, 2) == Point(1, 2))   # False — two different objects
```

*Without `__eq__`, two points with the same coordinates aren't equal.*

Defining **`__eq__`** gives value-based equality:

```python
class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __eq__(self, other):
        if not isinstance(other, Point):
            return NotImplemented
        return self.x == other.x and self.y == other.y

print(Point(1, 2) == Point(1, 2))   # True
print({Point(1, 2)})                # TypeError: unhashable type: 'Point'
```

*`__eq__` makes points compare by value — and makes them unhashable.*

Returning **`NotImplemented`** (a built-in constant, not an error) tells Python this method doesn't know how to compare with that type, so Python can try the other operand's method before falling back to `False`.

### Why They Travel Together

The last line failed because of a rule dicts and sets depend on: **objects that compare equal must have equal hashes.** If two equal points hashed differently, a set could hold both and a dict lookup could miss. So when a class defines `__eq__` without `__hash__`, Python sets its `__hash__` to `None`, making instances unhashable rather than silently wrong.

To restore hashing, `__hash__` must be computed from the same fields `__eq__` compares. Hashing a tuple of those fields is the standard approach:

```python
    def __hash__(self):
        return hash((self.x, self.y))
```

*Added to `Point`, this makes equal points produce equal hashes, so they work as set members and dict keys.*

The fields involved should never change after the object is created. If a point used as a dict key had its `x` reassigned, its hash would change and the dict could no longer find it. This is exactly why a non-frozen `@dataclass` is unhashable, and a `frozen=True` one isn't.

**Contrast:** the pairing rule is identical to Java's `equals`/`hashCode` contract; only the names and the enforcement differ. Java compiles a class that overrides `equals` without `hashCode` and lets `HashMap` misbehave at runtime. Python removes the hash entirely, so the mistake fails loudly the first time an instance goes into a set.

---

## Container Protocols

Four dunders make an object behave like a built-in collection:

```python
class Playlist:
    def __init__(self, name, songs):
        self.name = name
        self.songs = list(songs)

    def __len__(self):
        return len(self.songs)

    def __contains__(self, song):
        return song in self.songs

    def __iter__(self):
        return iter(self.songs)

    def __getitem__(self, index):
        return self.songs[index]

focus = Playlist("Focus", ["Weightless", "Clair de Lune", "Gymnopédie"])

print(len(focus))                   # 3
print("Weightless" in focus)        # True
for song in focus:                  # iterates the songs
    print(song)
print(focus[0], focus[-1])          # Weightless Gymnopédie
print(focus[1:])                    # ['Clair de Lune', 'Gymnopédie']
```

*A class supporting `len()`, `in`, `for`, indexing, and slicing, each by delegating to the list it wraps.*

- **`__len__`** powers `len()`. It also drives truthiness: an object with a length of zero is falsy, so `if not focus:` works too.
- **`__contains__`** powers `in`.
- **`__iter__`** powers `for` loops and anything else that iterates. It must return an iterator; `iter(self.songs)` borrows the list's. A generator works as well — writing `__iter__` with `for song in self.songs: yield song` is equally valid.
- **`__getitem__`** powers `obj[key]`. Slices arrive as a `slice` object, which the list here handles for us.

### Duck Typing

Nothing in `Playlist` says "I am a collection." `len()` works because `__len__` exists — and that's the entire requirement. This is **duck typing**: if it walks like a duck and quacks like a duck, Python treats it as a duck. The check happens at the moment of use, not at declaration.

**Contrast:** in Java, a class usable in an enhanced `for` loop must declare `implements Iterable<T>`, and the compiler verifies it. Python has nothing to declare and nothing to implement formally — define `__iter__` and `for` works. TypeScript's structural typing is similar in spirit, but it's checked at compile time; Python's is discovered when the line runs.

---

## `__enter__` and `__exit__`

Unit 002 used `with open(...)` without explaining how it closes the file. The mechanism is two dunder methods — the **context manager protocol**:

- **`__enter__(self)`** runs when the `with` block starts. Its return value is what `as` binds.
- **`__exit__(self, exc_type, exc_value, traceback)`** runs when the block ends — **always**, whether it finished normally or an error escaped. The three arguments describe that error, or are all `None` if there wasn't one.

A file object's `__enter__` returns the file itself, and its `__exit__` closes it. Our own classes can do the same:

```python
import time

class Timer:
    def __init__(self, label):
        self.label = label

    def __enter__(self):
        self.start = time.perf_counter()
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        elapsed = time.perf_counter() - self.start
        print(f"{self.label}: {elapsed:.3f}s")
        return False

with Timer("summing") as timer:
    total = sum(range(10_000_000))
# summing: 0.214s
```

*A context manager that measures and reports how long its block took, even if the block fails.*

The sequence is: `Timer("summing")` creates the object, `__enter__` runs and its result is bound to `timer`, the block runs, then `__exit__` runs. Returning `False` (or nothing) from `__exit__` lets any error continue on its way. Returning `True` would swallow it, which is almost never what we want.

**Contrast:** try-with-resources calls `close()` on an `AutoCloseable` at the end of the block. Python's version has a hook at the start as well, and it isn't tied to closing — anything that needs a guaranteed "before and after" can be a context manager.

---

## Operator Overloading

Arithmetic and comparison operators dispatch to dunders too, so our classes can support them:

```python
class Money:
    def __init__(self, cents):
        self.cents = cents

    def __add__(self, other):
        if not isinstance(other, Money):
            return NotImplemented
        return Money(self.cents + other.cents)

    def __lt__(self, other):
        if not isinstance(other, Money):
            return NotImplemented
        return self.cents < other.cents

    def __repr__(self):
        return f"Money({self.cents})"

print(Money(250) + Money(199))      # Money(449)
print(Money(250) < Money(199))      # False
```

*Supporting `+` and `<` between `Money` values. Returning `NotImplemented` makes `Money(250) + 5` raise a clear `TypeError`.*

The common operator dunders:

| Operator | Dunder | Operator | Dunder |
|----------|--------|----------|--------|
| `+` | `__add__` | `<` | `__lt__` |
| `-` | `__sub__` | `<=` | `__le__` |
| `*` | `__mul__` | `>` | `__gt__` |
| `/` | `__truediv__` | `>=` | `__ge__` |

One practical catch: `sum()` starts from the integer `0`, so `sum([Money(1), Money(2)])` tries `0 + Money(1)` and fails. Passing a starting value — `sum(wallet, Money(0))` — keeps every addition between `Money` objects.

Overloading is best kept to types where the operator's meaning is obvious, like money, vectors, or durations.

**Contrast:** Java has no user-defined operator overloading — `BigDecimal` needs `.add()` and `.compareTo()` — and neither does TypeScript. Python's own `+` on strings and lists is just `str.__add__` and `list.__add__`.

---

## Key Takeaways

- Python syntax and built-ins dispatch to dunder methods: `len(x)` calls `x.__len__()`, `a + b` calls `a.__add__(b)`. We implement them, but rarely call them directly.
- `__repr__` is the developer-facing representation used by the REPL and containers; `__str__` is the user-facing one used by `print`. Write `__repr__` first.
- Default `==` compares identity. Defining `__eq__` sets `__hash__` to `None`; define `__hash__` from the same immutable fields to restore hashing.
- Equal objects must have equal hashes — the same contract as Java's `equals`/`hashCode`, but Python enforces half of it.
- `__len__`, `__contains__`, `__iter__`, and `__getitem__` make a class work with `len`, `in`, `for`, and indexing. `__len__` also controls truthiness.
- Duck typing replaces interfaces: implement the methods, and the language uses them, with nothing declared.
- `__enter__` and `__exit__` are the mechanism behind `with`; `__exit__` always runs, even when the block fails.
- Operator dunders like `__add__` and `__lt__` enable operator overloading. Return `NotImplemented` for unsupported types.
