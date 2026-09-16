# Classes and Dunders

We'll grow a plain `Basket` class into one that works with Python's own syntax — `print`, `len`, `in`, `for`, `+`, and `with` — by uncommenting one driver block at a time and adding whatever each block needs. Along the way we'll cover `__repr__` vs `__str__`, `__len__`, `__contains__`, `__iter__`, `__eq__` paired with `__hash__`, `__add__`, `__enter__` and `__exit__` used through a `with` block, `@property` replacing a getter, `@dataclass` for the item type, `@staticmethod`, and the difference between class and instance attributes. The exercises finish with two blocks that come with no hints at all.

---

## Prerequisites

| Software | Required Version |
|---|---|
| Python | 3.11 or newer (current stable release: 3.14.7) |

This lab uses no third-party packages, so there's nothing to install and a virtual environment is optional.

### Getting Started

1. Open a terminal in this lab's folder — the one containing this `README.md`.
2. Run the driver (use `python3` on macOS/Linux if `python` isn't available):

   ```text
   python src/main.py
   ```

The starter runs as-is, but only block 0 is active:

```text
== 0. Starting point ==
ada
1848
```

*The basket's owner, and its total in cents from the `get_total()` method.*

### Project Layout

```text
001 - Classes and Dunders/
├── README.md
└── src/
    ├── basket.py    # the Item and Basket classes we'll extend
    └── main.py      # the driver: block 0 active, every other block commented out
```

### How the Lab Works

`basket.py` starts with two plain classes. `Item` holds a name and a price in cents. `Basket` has an owner, a private-by-convention `_items` list, two class attributes, a working `add()` method, and a `get_total()` getter.

For each block in `main.py`:

1. **Uncomment the block** — every line from its `##` header down to the blank line after it. In VS Code, select those lines and press **Ctrl+/** (**Cmd+/** on macOS). The `##` header loses one `#` and stays a comment.
2. **Run the file.** Most blocks fail in a specific way, or print something unhelpful. The failure tells us what Python was looking for.
3. **Change `basket.py`** until the block's output matches the expected output below.

Unless a section says otherwise, new methods go inside the `Basket` class, below the methods already there, indented four spaces.

---

## Guided Walkthrough

### Block 1: `@staticmethod`

After uncommenting block 1, running the file fails with `AttributeError: type object 'Basket' has no attribute 'format_cents'`.

A static method belongs to the class but receives neither the instance nor the class. We'll add one that formats cents as dollars:

```python
    @staticmethod
    def format_cents(cents):
        """Return an amount in cents as a dollar string, like "$18.48"."""
        return f"${cents / 100:.2f}"
```

*`@staticmethod` means there's no `self` parameter. The first `$` in the f-string is a literal dollar sign.*

Expected output:

```text
== 1. @staticmethod ==
$18.48
$0.99
```

Block 1 calls it both ways: through the class, `Basket.format_cents(1848)`, and through an instance, `basket.format_cents(99)`. Either works, and neither passes the basket in.

### Block 2: `__str__` and `__repr__`

Uncommenting block 2 doesn't crash, but it prints something like `<basket.Basket object at 0x0000027EF10F6A50>` three times — the default representation every class inherits, showing only the class name and a memory address.

We'll give `Basket` both string representations:

```python
    def __repr__(self):
        return f"Basket({self.owner!r})"

    def __str__(self):
        return f"{self.owner}'s basket: {len(self._items)} items, {Basket.format_cents(self.get_total())}"
```

*`__repr__` is for developers, so it looks like the code that would create the basket. `!r` inserts the owner's own repr, quotes included. `__str__` is the friendly version.*

Expected output:

```text
== 2. __str__ and __repr__ ==
ada's basket: 3 items, $18.48
Basket('ada')
[Basket('ada')]
```

`print(basket)` used `__str__`. `repr(basket)` used `__repr__` — and so did printing a *list* containing the basket, because containers always show their items' reprs.

### Block 3: `__len__`

Block 3 fails with `TypeError: object of type 'Basket' has no len()`. `len()` looks for a `__len__` method, so we'll add one:

```python
    def __len__(self):
        return len(self._items)
```

*The basket's length is the length of the list it wraps.*

Expected output:

```text
== 3. __len__ ==
3
empty is falsy: True
```

The second line comes for free. We never wrote anything about truthiness, but a class with `__len__` and no `__bool__` is falsy when its length is 0 — so `not empty` is `True`, just as it would be for an empty list.

### Block 4: `__contains__`

Block 4 fails with a `TypeError` saying a `Basket` isn't iterable. `in` looks for `__contains__` first, and falls back to looping over the object when there isn't one. We'll add `__contains__`, checking item *names*:

```python
    def __contains__(self, name):
        return any(item.name == name for item in self._items)
```

*`any()` with a generator expression stops at the first item whose name matches.*

Expected output:

```text
== 4. __contains__ ==
True
False
```

Our `__contains__` decides what "in" means for a basket. Here it's a name, so `"bread" in basket` works — even though the basket actually holds `Item` objects, not strings.

### Block 5: `__iter__`

Block 5 fails with `TypeError: 'Basket' object is not iterable`. A `for` loop calls `iter()` on the object, which looks for `__iter__`:

```python
    def __iter__(self):
        return iter(self._items)
```

*`__iter__` must return an iterator. Rather than build one ourselves, we borrow the list's.*

Expected output:

```text
== 5. __iter__ ==
bread $3.50
milk $1.99
coffee $12.99
```

> **Contrast — Java:** to use a class in an enhanced `for` loop, Java requires `implements Iterable<Item>`, checked by the compiler. `Basket` declares nothing — it simply has an `__iter__` method, and `for` uses it. This is **duck typing**: Python checks for the method at the moment it's needed, not for a declared interface.

### Block 6: `@property`

Block 6 fails with `AttributeError: 'Basket' object has no attribute 'total'` — it reads `basket.total` as an attribute, with no parentheses.

Python code doesn't use `get_x()` methods; a **property** lets a method be read like an attribute. We'll replace the whole `get_total` method with this:

```python
    @property
    def total(self):
        """Return the total price of every item in the basket, in cents."""
        return sum(item.price for item in self._items)
```

*`@property` turns `total` into a computed, read-only attribute. The body is unchanged; only the decorator and the name are new.*

Renaming the method means updating the two places that still call `get_total()`:

- In `__str__`, change `self.get_total()` to `self.total`.
- In block 0 of `main.py`, change `basket.get_total()` to `basket.total`.

Expected output:

```text
== 6. @property ==
$18.48
```

Block 0 still prints `1848`, and block 2 is unchanged. Had `Basket` exposed `total` as a property from the start, no caller would have needed to change — which is exactly why Python classes skip writing getters up front.

### Block 7: `__add__`

Block 7 fails with `TypeError: unsupported operand type(s) for +: 'Basket' and 'Basket'`. The `+` operator calls `__add__` on its left operand:

```python
    def __add__(self, other):
        if not isinstance(other, Basket):
            return NotImplemented
        combined = Basket(f"{self.owner}+{other.owner}")
        for item in self:
            combined.add(item)
        for item in other:
            combined.add(item)
        return combined
```

*Adding two baskets returns a new basket holding both sets of items. `for item in self` works because of the `__iter__` we wrote in block 5.*

Expected output:

```text
== 7. __add__ ==
ada+grace's basket: 5 items, $28.48
3 2 5
```

The second line shows that `+` built something new and left both original baskets unchanged. Returning `NotImplemented` for anything that isn't a `Basket` tells Python this method doesn't handle it, so an expression like `basket + 5` raises a clear `TypeError` instead of doing something strange.

### Block 8: `__eq__` and `__hash__`

This block works with `Item` instead of `Basket`, so its methods go inside the **`Item`** class.

Uncommented as-is, block 8 prints `False`, `False`, and `3`. Without `__eq__`, `==` compares identity, and two separately created items with identical data are different objects. The set keeps all three for the same reason.

We'll add value-based equality to `Item`:

```python
    def __eq__(self, other):
        if not isinstance(other, Item):
            return NotImplemented
        return self.name == other.name and self.price == other.price
```

*Two items are equal when their names and prices match.*

Running the file now prints `True` and `False`, then fails on the set line with `TypeError: unhashable type: 'Item'`. Defining `__eq__` removed `Item`'s ability to be hashed. Python does this on purpose: sets and dicts rely on equal objects having equal hashes, and the inherited hash no longer agrees with our new definition of equal.

So we'll add `__hash__`, computed from the same fields `__eq__` compares:

```python
    def __hash__(self):
        return hash((self.name, self.price))
```

*Hashing a tuple of the fields gives equal items equal hashes.*

Expected output:

```text
== 8. __eq__ and __hash__ ==
True
False
2
```

The set now recognizes the two identical breads as one value.

### Block 9: `@dataclass`

Uncommented, block 9 prints the default `<basket.Item object at 0x...>` representation for every item.

We could write `__repr__` for `Item` by hand, but `Item` is pure data — exactly what `@dataclass` exists for. We'll replace the **entire `Item` class**, including the `__init__`, `__eq__`, and `__hash__` we just wrote, with this:

```python
@dataclass(frozen=True)
class Item:
    """One product: a name and a price in cents."""

    name: str
    price: int
```

*The annotated names declare the fields. From them, `@dataclass` generates `__init__`, `__repr__`, and `__eq__`. `frozen=True` makes instances immutable, which lets it generate `__hash__` too.*

The decorator needs an import at the very top of `basket.py`:

```python
from dataclasses import dataclass
```

*`dataclass` lives in the standard library's `dataclasses` module.*

Expected output:

```text
== 9. @dataclass ==
Item(name='bread', price=350)
[Item(name='milk', price=199), Item(name='coffee', price=1299)]
```

Block 8 still prints `True`, `False`, and `2` — the generated `__eq__` and `__hash__` behave just like the ones we deleted. The annotations are still not enforced, though: `Item("bread", "cheap")` would be accepted without complaint.

### Block 10: Class and Instance Attributes

Block 10 needs no changes to `basket.py`; it runs as soon as it's uncommented. `currency` and `baskets_created` are **class attributes**, assigned in the class body and shared by every basket. `__init__` increments `Basket.baskets_created` each time a basket is created.

Expected output:

```text
== 10. Class and instance attributes ==
baskets created: 4
USD USD
USD EUR USD
```

Four baskets exist by this point: `basket`, `empty` from block 3, and `other` and `combined` from block 7 — `__add__` creates a basket too.

The last line is what block 10's **Explain:** prompt asks about. Before answering, it's worth adding `print(other.__dict__)` to the block to see which attributes `other` holds on its own.

### Block 11: `__enter__` and `__exit__`

Block 11 fails with a `TypeError` saying `Basket` doesn't support the context manager protocol. (Older Python versions report an error about `__enter__` instead.) A `with` statement calls `__enter__` at the start of the block and `__exit__` at the end.

We'll make a `with` block act as a checkout:

```python
    def __enter__(self):
        print(f"checking out {self.owner}")
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        print(f"{self.owner} paid {Basket.format_cents(self.total)} for {len(self)} items")
        self._items = []
        return False
```

*Whatever `__enter__` returns is bound by `as`, so `order` is the basket. `__exit__` runs when the block ends: it prints a receipt and empties the basket. Its three parameters describe any error that ended the block, and returning `False` lets such an error continue on its way.*

Expected output:

```text
== 11. __enter__ and __exit__ ==
checking out linus
items in order: 2
linus paid $8.49 for 2 items
items after checkout: 0
```

This is the same mechanism behind `with open(...)`. A file object's `__enter__` returns the file, and its `__exit__` closes it.

### Checking the Whole File

With blocks 0 through 11 uncommented and working, running `python src/main.py` prints:

```text
== 0. Starting point ==
ada
1848
== 1. @staticmethod ==
$18.48
$0.99
== 2. __str__ and __repr__ ==
ada's basket: 3 items, $18.48
Basket('ada')
[Basket('ada')]
== 3. __len__ ==
3
empty is falsy: True
== 4. __contains__ ==
True
False
== 5. __iter__ ==
bread $3.50
milk $1.99
coffee $12.99
== 6. @property ==
$18.48
== 7. __add__ ==
ada+grace's basket: 5 items, $28.48
3 2 5
== 8. __eq__ and __hash__ ==
True
False
2
== 9. @dataclass ==
Item(name='bread', price=350)
[Item(name='milk', price=199), Item(name='coffee', price=1299)]
== 10. Class and instance attributes ==
baskets created: 4
USD USD
USD EUR USD
== 11. __enter__ and __exit__ ==
checking out linus
items in order: 2
linus paid $8.49 for 2 items
items after checkout: 0
```

Before moving on, we'll make sure block 10's **Explain:** prompt has an answer.

---

## Exercises

The last two blocks in `main.py` have no hints. For each one, uncomment it, run the file, and use the error to work out which method `Basket` is missing. Then write it. Both blocks run after the rest of the file, so `basket` still holds bread, milk, and coffee.

### Exercise 1

When it works, the output is:

```text
== Exercise 1 ==
Item(name='bread', price=350)
coffee
```

### Exercise 2

When it works, the output is:

```text
== Exercise 2 ==
2 False
```

Once both exercises pass, confirm that blocks 0 through 11 still print exactly what they did before.
