# Lists and Tuples

Python has two built-in sequence types for holding a run of values: the **list**, which is mutable, and the **tuple**, which isn't. Between them they cover what Java splits across arrays, `ArrayList`, `List.of()`, and small record classes. The part that trips people up isn't the syntax — it's that a list is shared by reference, and nothing about assignment ever makes a copy.

---

## List Literals and Indexing

A list literal is a comma-separated run of values in square brackets:

```python
languages = ["Java", "TypeScript", "Python"]
empty = []

print(len(languages))   # 3
print(languages[0])     # Java
print(languages[-1])    # Python — negative indexes count from the end
```

*Creating a list, taking its length, and indexing from the front and from the back.*

Indexing past either end raises `IndexError`. Negative indexes run from `-1` (the last item) down to `-len(list)` (the first).

Slicing works exactly as it does on strings — `start:stop:step`, with the stop excluded — and always produces a **new list**:

```python
numbers = [10, 20, 30, 40, 50]

numbers[1:3]     # [20, 30]
numbers[:2]      # [10, 20]
numbers[-2:]     # [40, 50]
numbers[::-1]    # [50, 40, 30, 20, 10]
```

*Slices of a list, including a reversed copy.*

`+` concatenates two lists into a new one, and `*` repeats one: `[0] * 3` is `[0, 0, 0]`.

### Lists Are Heterogeneous

Nothing restricts what a list holds:

```python
record = ["Ada", 36, True, None, [1, 2]]
```

*A single list holding a string, an int, a bool, `None`, and another list.*

That's legal, but rarely a good idea. In practice a list holds one kind of thing, and a fixed group of different things is better expressed as a tuple, a dict, or a class.

**Contrast:** `List<String>` and `string[]` carry a type parameter the compiler polices. A Python list has no type parameter, so nothing stops a list of names from acquiring an integer. A hint like `list[str]` documents the intent, but — as with every annotation — the interpreter never enforces it. The discipline is ours.

---

## Modifying a List

Lists grow and shrink in place:

```python
tasks = ["build", "test"]

tasks.append("deploy")            # ['build', 'test', 'deploy']
tasks.insert(0, "lint")           # ['lint', 'build', 'test', 'deploy']
tasks.extend(["tag", "notify"])   # [..., 'deploy', 'tag', 'notify']

last = tasks.pop()                # removes and returns 'notify'
first = tasks.pop(0)              # removes and returns 'lint'
tasks.remove("test")              # removes the first 'test', by value

print(tasks)                      # ['build', 'deploy', 'tag']
```

*Adding at the end, at an index, and from another list; removing by position and by value.*

- **`append(x)`** adds one item to the end.
- **`extend(iterable)`** adds each item of another collection. Writing `tasks.append(["tag", "notify"])` instead adds a single nested list — a common slip.
- **`insert(i, x)`** adds at an index, shifting everything after it along.
- **`pop()`** removes and returns the last item; **`pop(i)`** removes and returns the item at index `i`.
- **`remove(x)`** deletes the first item equal to `x`, and raises `ValueError` if there isn't one.

Every one of these except `pop` returns `None`. They change the list; they don't hand back a new one.

---

## `sort()` vs `sorted()`

There are two ways to sort, and the difference is whether the original changes:

```python
scores = [72, 95, 88]

ordered = sorted(scores)    # new list [72, 88, 95]; scores is unchanged
scores.sort()               # rearranges scores itself; returns None

scores.sort(reverse=True)   # scores is now [95, 88, 72]
```

*`sorted()` returns a new list; `list.sort()` rearranges the existing list in place.*

`sorted()` accepts any iterable — a tuple, a string, a set — and always returns a list. `sort()` exists only on lists. Both take the same `key` and `reverse` arguments.

The classic bug is mixing the two styles:

```python
scores = scores.sort()      # scores is now None
```

*`sort()` returns `None`, so assigning its result throws the list away.*

This follows a general Python convention: methods that mutate in place return `None`, precisely so they can't be mistaken for methods that return a new object.

---

## Tuples

A **tuple** is an immutable sequence. It's written with commas, usually wrapped in parentheses:

```python
point = (3, 4)
rgb = 255, 128, 0          # parentheses are optional; the commas make the tuple

print(point[0])            # 3
print(len(rgb))            # 3

point[0] = 10              # TypeError: 'tuple' object does not support item assignment
```

*Creating tuples, reading from one, and failing to change one.*

Indexing, slicing, `len()`, `in`, and iteration all work exactly as they do on lists. What's missing is every method that would change the tuple — no `append`, no `remove`, no `sort`.

### The Single-Element Gotcha

Because the **comma** makes a tuple, not the parentheses, a one-item tuple needs a trailing comma:

```python
not_a_tuple = (5)     # just the int 5 inside grouping parentheses
one_tuple = (5,)      # a tuple containing 5
empty = ()            # the empty tuple is the one case with no comma

print(type(not_a_tuple))    # <class 'int'>
print(type(one_tuple))      # <class 'tuple'>
```

*Parentheses on their own only group an expression; the trailing comma is what creates a single-element tuple.*

---

## Unpacking

Any sequence can be **unpacked** into separate names, as we saw with multiple return values:

```python
point = (3, 4)
x, y = point

name, age, active = ["Ada", 36, True]    # works on lists too
```

*Assigning each element of a sequence to its own name. The counts on each side must match, or Python raises `ValueError`.*

Unpacking makes swapping two values a single line with no temporary variable:

```python
a, b = 1, 2
a, b = b, a
print(a, b)     # 2 1
```

*The right side builds the tuple `(2, 1)` first, then unpacks it into `a` and `b`.*

**Starred unpacking** collects "everything else" into a list:

```python
first, *rest = [10, 20, 30, 40]
print(first, rest)      # 10 [20, 30, 40]

*history, latest = ["v1", "v2", "v3"]
print(latest)           # v3

head, *middle, tail = (1, 2, 3, 4, 5)
print(middle)           # [2, 3, 4] — always a list, even when unpacking a tuple
```

*One starred name per assignment absorbs however many items the other names don't take.*

Unpacking also works directly in a `for` loop, which is how a sequence of pairs is usually walked:

```python
products = [("laptop", 1200), ("mouse", 25)]

for name, price in products:
    print(f"{name}: ${price}")
```

*Each tuple is unpacked into `name` and `price` on every pass through the loop.*

---

## When a Tuple Is the Right Choice

A list and a tuple can hold the same values, so the choice is mostly about what we're communicating:

- **Fixed records.** A tuple suits a small group of values whose positions carry meaning — a coordinate `(x, y)`, an RGB color, a `(name, price)` pair. A list suits a variable-length run of similar items.
- **Dictionary keys and set members.** Only immutable objects can be dict keys, so `(row, col)` works as a key where `[row, col]` can't. The next lesson covers why.
- **Multiple return values.** `return low, high` returns a tuple, and the caller unpacks it.

A useful rule of thumb: if we'd ever `append` to it, it's a list. If its length is part of what it means, it's a tuple.

**Contrast:** one `list` type replaces `T[]`, `ArrayList<T>`, and `List.of()`, and the tuple covers much of what a Java `record` does for small groupings. TypeScript has tuple *types* like `[string, number]`, but those describe an ordinary mutable array at compile time and disappear afterward. A Python tuple is genuinely immutable at runtime.

---

## Reference Semantics

Assignment never copies a list. It binds a second name to the **same** list object, so a change made through either name is visible through both. This is **aliasing**:

```python
original = ["a", "b"]
alias = original

alias.append("c")
print(original)             # ['a', 'b', 'c']
print(alias is original)    # True — one object, two names
```

*Both names refer to a single list, so appending through `alias` changes what `original` sees.*

The same thing happens when a list is passed to a function. The parameter is just another name for the caller's list:

```python
def add_default_tag(tags):
    tags.append("untagged")

my_tags = []
add_default_tag(my_tags)
print(my_tags)      # ['untagged']
```

*The function mutates the caller's list, because its parameter is bound to the same object.*

Java and TypeScript behave identically here — object references are passed by value in all three languages. What catches people out is that Python's lightweight syntax makes a list *feel* like a value.

### Shallow Copies

To get an independent list, we copy it explicitly. All three of these make a **shallow copy** — a new outer list holding the same inner objects:

```python
original = [1, 2, 3]

copy_a = original.copy()
copy_b = list(original)
copy_c = original[:]

copy_a.append(4)
print(original)     # [1, 2, 3] — unaffected
```

*Three equivalent ways to make a new list with the same items.*

A shallow copy is enough when the items are immutable, like numbers and strings. When the items are themselves lists, the copies share them:

```python
grid = [[1, 2], [3, 4]]
shallow = grid.copy()

shallow[0].append(99)
print(grid)         # [[1, 2, 99], [3, 4]] — the inner list is shared
```

*The outer list was copied, but both outer lists still point at the same inner lists.*

### Deep Copies

`copy.deepcopy()` from the standard library copies recursively, all the way down:

```python
import copy

grid = [[1, 2], [3, 4]]
deep = copy.deepcopy(grid)

deep[0].append(99)
print(grid)         # [[1, 2], [3, 4]] — fully independent
```

*A deep copy duplicates every nested list, so no mutation can leak back to the original.*

### The Repeated-List Trap

Aliasing also hides inside `*`. Repeating a list that contains a list repeats the **reference**, not the inner list:

```python
board = [[0] * 3] * 3
board[0][0] = 1
print(board)        # [[1, 0, 0], [1, 0, 0], [1, 0, 0]]
```

*All three rows are the same list object, so setting one cell sets it in every row.*

Building each row separately avoids the problem:

```python
board = []
for _ in range(3):
    board.append([0] * 3)
```

*Each pass through the loop creates a brand-new row.*

`[0] * 3` on its own is safe: the repeated items are integers, which can't be mutated, so sharing them is harmless.

---

## Key Takeaways

- A list is a mutable, ordered, heterogeneous sequence; indexing supports negative positions and slicing returns a new list.
- `append`, `extend`, `insert`, `pop`, and `remove` change a list in place. Mutating methods return `None`.
- `sorted()` returns a new list from any iterable; `list.sort()` sorts in place and returns `None`.
- A tuple is an immutable sequence. The comma makes it, so a single-element tuple is `(x,)`.
- Unpacking assigns sequence elements to names, makes swaps a one-liner, and `*name` collects the leftovers into a list.
- Prefer tuples for fixed records, dict keys, and multiple return values; lists for variable-length collections.
- Assignment and argument passing share the same list object. Copy explicitly: `copy()`, `list()`, or `[:]` for shallow, `copy.deepcopy()` for nested data.
- One `list` type replaces arrays, `ArrayList`, and `List.of()`, with no type parameter to keep its contents consistent.
