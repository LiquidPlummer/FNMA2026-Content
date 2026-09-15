# Writing Functions

We'll implement a file of numbered function stubs, each with a docstring describing exactly what it should do, until the driver at the bottom of the file prints the expected output. Along the way we'll cover `def` and `return`, the implicit `None` return, returning a tuple and unpacking it, keyword arguments at the call site, default parameter values, the mutable default argument trap and its fix, `*args`, `**kwargs`, passing functions around as objects, `lambda` as a `sorted` key, and annotations that Python doesn't enforce. The exercises finish with writing functions and their docstrings from scratch.

---

## Prerequisites

| Software | Required Version |
|---|---|
| Python | 3.11 or newer (current stable release: 3.14.7) |

This lab uses no third-party packages, so there's nothing to install and a virtual environment is optional.

### Getting Started

1. Open a terminal in this lab's folder — the one containing this `README.md`.
2. Run the starter file (use `python3` on macOS/Linux if `python` isn't available):

   ```text
   python src/functions_lab.py
   ```

The starter runs as-is. Every stub's body is just `pass`, so most lines print `None` for now.

### Project Layout

```text
001 - Writing Functions/
├── README.md
└── src/
    └── functions_lab.py    # numbered stubs on top, driver block at the bottom
```

### How the File Is Organized

- **The stubs** at the top are numbered 1 through 10. Each docstring is the function's contract: what it receives, and what it returns or prints.
- **The driver** at the bottom sits under `if __name__ == "__main__":` and has a matching numbered header for each stub. Some driver sections are complete already; others have TODOs where the point of the exercise is how the function gets *called*.

For each number, we'll implement the stub, finish that number's driver TODOs, run the file, and compare with the expected output below.

---

## Guided Walkthrough

### 1. Returning a Value

We'll replace `format_price`'s `pass` with a `return`:

```python
def format_price(amount):
    """Return amount as a price string with a dollar sign and two decimals.

    format_price(3.5) returns "$3.50".
    """
    return f"${amount:.2f}"
```

*The `:.2f` format spec rounds to two decimal places. The `$` before the brace is just a literal dollar sign.*

Expected output:

```text
== 1. Returning a value ==
$3.50
$12.00
```

### 2. Returning Nothing

`announce` prints instead of returning. We'll implement it with no `return` statement at all:

```python
def announce(message):
    """Print message with three asterisks and a space on each side.

    announce("Hi") prints "*** Hi ***". Returns nothing.
    """
    print(f"*** {message} ***")
```

*A function body that prints and ends without a `return`.*

Expected output:

```text
== 2. Returning nothing ==
*** Now serving order 42 ***
announce returned: None
```

The driver stored the result of calling `announce` and printed it. A function with no `return` still hands back a value: `None`.

### 3. Returning Two Values

We'll return both prices at once, separated by a comma:

```python
def price_range(prices):
    """Return the lowest and the highest price in prices, in that order."""
    return min(prices), max(prices)
```

*The comma builds a tuple, so this returns a single `(lowest, highest)` pair.*

Then, in the driver, we'll replace the two `None` lines with one line that unpacks the tuple into two names:

```python
    lowest, highest = price_range(menu_prices)
```

*Tuple unpacking: the first element is bound to `lowest`, the second to `highest`. Keep the four-space indentation of the driver block.*

Expected output:

```text
== 3. Returning two values ==
cheapest: 2.75
priciest: 5.5
```

### 4. Default Values and Keyword Arguments

We'll give `size` and `shots` their defaults in the signature, then implement the body:

```python
def make_order(drink, size="medium", shots=1):
    """Return a one-line description of a coffee order.

    The format is "<size> <drink>, <shots> shot(s)", for example
    "medium latte, 1 shot(s)". size defaults to "medium" and shots
    defaults to 1.
    """
    return f"{size} {drink}, {shots} shot(s)"
```

*Parameters with defaults come after the required `drink` parameter.*

In the driver, we'll replace the three `None`s with three calls:

```python
    print(make_order("mocha"))
    print(make_order("latte", shots=2))
    print(make_order("americano", shots=3, size="large"))
```

*Both defaults, one keyword argument skipping over `size`, and two keyword arguments in the reverse of their order in the definition.*

Expected output:

```text
== 4. Default values ==
medium mocha, 1 shot(s)
medium latte, 2 shot(s)
large americano, 3 shot(s)
```

> **Contrast — Java:** covering these three calls in Java means writing overloads — `makeOrder(String drink)`, `makeOrder(String drink, int shots)`, and so on — or a builder. Python has no overloading: one function, with defaults and keyword arguments covering every combination.

### 5. The Mutable Default Trap

First we'll implement `add_topping_buggy` exactly as its docstring describes, without touching the signature:

```python
def add_topping_buggy(topping, toppings=[]):
    """Append topping to toppings and return toppings.

    If no toppings list is passed in, start from an empty list.
    """
    toppings.append(topping)
    return toppings
```

*`append` adds an item to the end of a list in place.*

Now we'll run the file and look at the two `buggy` lines:

```text
buggy, order 1: ['vanilla']
buggy, order 2: ['vanilla', 'caramel']
```

The second order never asked for vanilla. That's the trap, and it's what the **Explain:** comment in the driver asks about.

Next we'll write `add_topping_fixed` properly. We'll change its default to `None`, and create the new list inside the body:

```python
def add_topping_fixed(topping, toppings=None):
    """Append topping to toppings and return toppings.

    If no toppings list is passed in, start from a new empty list on
    every call.
    """
    if toppings is None:
        toppings = []
    toppings.append(topping)
    return toppings
```

*The body runs on every call, so each call that omits `toppings` gets its own new list.*

Expected output:

```text
== 5. The mutable default trap ==
buggy, order 1: ['vanilla']
buggy, order 2: ['vanilla', 'caramel']
fixed, order 1: ['vanilla']
fixed, order 2: ['caramel']
```

Before moving on, we'll answer the **Explain:** prompt in the driver.

### 6. Any Number of Positional Arguments

We'll add a `*prices` parameter so `order_total` accepts any number of prices:

```python
def order_total(*prices):
    """Return the sum of any number of prices passed as separate arguments.

    order_total(3.5, 2.25) returns 5.75, and order_total() returns 0.
    """
    return sum(prices)
```

*`*prices` collects every positional argument into a tuple, which may be empty.*

In the driver, we'll replace the two `None`s:

```python
    print("three items:", order_total(3.5, 2.25, 4.0))
    print("no items:", order_total())
```

*The same function called with three arguments, then with none.*

Expected output:

```text
== 6. Any number of positional arguments ==
three items: 9.75
no items: 0
```

### 7. Any Number of Keyword Arguments

We'll add a `**items` parameter and loop over what it collects:

```python
def print_receipt(customer, **items):
    """Print "Receipt for <customer>", then one line per extra keyword argument.

    Each extra line is two spaces, the argument's name, a colon, a space,
    and its value: print_receipt("Ada", latte=4.25) prints "Receipt for Ada"
    and then "  latte: 4.25". Returns nothing.
    """
    print(f"Receipt for {customer}")
    for name, price in items.items():
        print(f"  {name}: {price}")
```

*`**items` collects the extra keyword arguments into a dict that maps each argument's name to its value. `items.items()` produces each name–value pair, which the `for` unpacks into `name` and `price`.*

In the driver, under section 7's TODO, we'll add the call:

```python
    print_receipt("Ada", latte=4.25, muffin=3.0)
```

*`latte` and `muffin` aren't parameters in the signature, so both land in `items`.*

Expected output:

```text
== 7. Any number of keyword arguments ==
Receipt for Ada
  latte: 4.25
  muffin: 3.0
```

### 8. Functions as Objects

We'll implement both stubs. `apply_to_each` receives a function as its `transform` parameter and calls it:

```python
def shout(text):
    """Return text in uppercase, followed by an exclamation mark."""
    return text.upper() + "!"


def apply_to_each(transform, words):
    """Call transform on each word in words, printing each result on its own line."""
    for word in words:
        print(transform(word))
```

*`transform(word)` calls whichever function was passed in; `apply_to_each` doesn't need to know which one.*

Then we'll complete section 8 of the driver, replacing its `None`s and adding the final call:

```python
    loud = shout
    print(loud("order up"))
    apply_to_each(shout, ["latte", "mocha"])
```

*`shout` without parentheses is the function object itself. `loud` becomes a second name for it, and `apply_to_each` receives it as an argument.*

Expected output:

```text
== 8. Functions as objects ==
ORDER UP!
LATTE!
MOCHA!
```

It's worth trying `loud = shout()` once to see the difference: that *calls* `shout` with no argument instead of referring to it, and the file fails.

### 9. `lambda` as a Sort Key

We'll implement `sort_by_price` with `sorted` and a `lambda` that tells it what to sort on:

```python
def sort_by_price(menu):
    """Return a new list of (name, price) pairs sorted from cheapest to priciest."""
    return sorted(menu, key=lambda item: item[1])
```

*For each `(name, price)` pair, the lambda returns `item[1]` — the price — and `sorted` orders the pairs by that value.*

Expected output:

```text
== 9. lambda as a sort key ==
[('espresso', 2.75), ('latte', 4.25), ('mocha', 5.5)]
```

`sorted` returns a new list; the original `menu` is unchanged.

### 10. Annotations

We'll add annotations to the signature and implement the body:

```python
def scale_recipe(amount: float, servings: int) -> float:
    """Return amount multiplied by servings.

    Meant for numbers: scale_recipe(2.5, 4) returns 10.0.
    """
    return amount * servings
```

*Each parameter's type follows a colon, and the return type follows `->`.*

The driver's section 10 is already complete. Its second call deliberately passes a string where a `float` is annotated.

Expected output:

```text
== 10. Annotations ==
10.0
mochamocha
{'amount': <class 'float'>, 'servings': <class 'int'>, 'return': <class 'float'>}
```

Three things to observe:

- **The correct call** returns `10.0`, as the docstring promised.
- **The wrong-type call runs anyway.** `"mocha" * 2` is valid Python string repetition, so the annotations were simply ignored.
- **The annotations are still there.** `__annotations__` holds them at runtime — Python keeps them for tools to read, but never checks them.

### Checking the Whole File

With all ten sections done, running `python src/functions_lab.py` prints:

```text
== 1. Returning a value ==
$3.50
$12.00
== 2. Returning nothing ==
*** Now serving order 42 ***
announce returned: None
== 3. Returning two values ==
cheapest: 2.75
priciest: 5.5
== 4. Default values ==
medium mocha, 1 shot(s)
medium latte, 2 shot(s)
large americano, 3 shot(s)
== 5. The mutable default trap ==
buggy, order 1: ['vanilla']
buggy, order 2: ['vanilla', 'caramel']
fixed, order 1: ['vanilla']
fixed, order 2: ['caramel']
== 6. Any number of positional arguments ==
three items: 9.75
no items: 0
== 7. Any number of keyword arguments ==
Receipt for Ada
  latte: 4.25
  muffin: 3.0
== 8. Functions as objects ==
ORDER UP!
LATTE!
MOCHA!
== 9. lambda as a sort key ==
[('espresso', 2.75), ('latte', 4.25), ('mocha', 5.5)]
== 10. Annotations ==
10.0
mochamocha
{'amount': <class 'float'>, 'servings': <class 'int'>, 'return': <class 'float'>}
```

---

## Exercises

Create a new file, `src/exercises.py`, and write the three functions below from scratch. No stubs this time: each function needs its own `def`, a docstring stating its contract in your own words, and a body. Add an `if __name__ == "__main__":` block at the bottom that runs every example call listed and prints the result, then run the file with `python src/exercises.py` and confirm each result matches.

### 1. `split_bill`

Takes a bill total, a number of people, and a tip percentage that defaults to 18. Returns two values: the tip amount, and each person's share of the total plus the tip. Annotate its parameters.

| Call | Result |
|---|---|
| `split_bill(100, 4)` | `(18.0, 29.5)` |
| `split_bill(60, 3, tip_percent=20)` | `(12.0, 24.0)` |

In your driver, unpack at least one of the results into two separate names before printing them.

### 2. `longest_word`

Takes any number of words as separate arguments and returns the longest one. If two words tie for longest, return the one that was passed first. If no words are passed at all, return `None`.

| Call | Result |
|---|---|
| `longest_word("tea", "latte", "mocha")` | `'latte'` |
| `longest_word("espresso")` | `'espresso'` |
| `longest_word()` | `None` |

### 3. `count_matching`

Takes a function and a list of values. Returns how many of the values the function gives a truthy result for.

| Call | Result |
|---|---|
| `count_matching(lambda price: price > 4, [4.25, 2.75, 5.5, 3.0])` | `2` |
| `count_matching(str.isupper, ["LATTE", "mocha", "TEA"])` | `2` |
| `count_matching(lambda word: word, ["", "tea", ""])` | `1` |
