# Comprehensions

We'll build lists, sets, and dicts from a morning of coffee-cart orders, one comprehension per line. Every line of the starter file prints a label next to a blanked-out expression; we fill in a comprehension and run the file until the output matches. Along the way we'll cover list comprehensions that extract a field, set comprehensions that drop duplicates, `if` filters, f-strings as the output expression, dict comprehensions with `key: value`, and `sorted()` for turning a set into output that's the same on every run. The file finishes with six lines that come with an expected output and nothing else.

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
   python src/comprehensions_lab.py
   ```

The starter runs as-is. Every line prints its label followed by `None`.

### Project Layout

```text
001 - Comprehensions/
├── README.md
└── src/
    └── comprehensions_lab.py    # ORDERS at the top, then 12 numbered print() lines
```

### The Data

`ORDERS` is a list of seven dicts, one per order. Each looks like this:

```python
{"id": 1001, "customer": "ada", "product": "espresso", "qty": 2, "price": 3.25, "region": "north"}
```

*An order's id, who placed it, what they bought, how many, the price of one, and where it was delivered.*

For every numbered line, we'll replace `None` with a comprehension over `ORDERS`, leaving the label alone.

---

## Guided Walkthrough

### 1. Extracting a Field

We'll replace line 1's `None` with a list comprehension:

```python
print("1. every order's customer:", [order["customer"] for order in ORDERS])
```

*The output expression comes first, then the `for` clause. It reads as "the order's customer, for each order in `ORDERS`."*

Expected output:

```text
1. every order's customer: ['ada', 'grace', 'ada', 'linus', 'margaret', 'grace', 'alan']
```

The list has one item per order, in order — duplicates and all.

### 2. The Same Thing with `{}`

Swapping the square brackets for braces builds a **set** instead of a list:

```python
print("2. customers, each once:", {order["customer"] for order in ORDERS})
```

*Same output expression, same `for` clause, different brackets.*

Expected output:

```text
2. customers, each once: {'ada', 'grace', 'alan', 'margaret', 'linus'}
```

The duplicates are gone: seven orders, but only five customers. The names will probably print in a different order than shown here — run the file a few times, and the order changes from one run to the next. A set has no order, so this line matches when it holds the same five names, whatever order they print in. Step 6 fixes that.

### 3. Adding an `if` Filter

An `if` after the `for` clause keeps only the items that pass it:

```python
print("3. ids of orders with a quantity of 3 or more:", [order["id"] for order in ORDERS if order["qty"] >= 3])
```

*Only orders with a `qty` of 3 or more reach the output expression.*

Expected output:

```text
3. ids of orders with a quantity of 3 or more: [1003, 1006, 1007]
```

### 4. f-Strings as the Output

The output expression doesn't have to be a bare field — it can be any expression, including an f-string:

```python
print("4. order labels:", [f"#{order['id']} {order['product']}" for order in ORDERS])
```

*Inside the double-quoted f-string, the dict keys use single quotes.*

Expected output:

```text
4. order labels: ['#1001 espresso', '#1002 latte', '#1003 muffin', '#1004 espresso', '#1005 latte', '#1006 tea', '#1007 espresso']
```

### 5. Dict Comprehensions

Braces with a `key: value` output expression build a **dict**:

```python
print("5. customer by order id:", {order["id"]: order["customer"] for order in ORDERS})
```

*The colon is what makes this a dict comprehension rather than a set comprehension.*

Expected output:

```text
5. customer by order id: {1001: 'ada', 1002: 'grace', 1003: 'ada', 1004: 'linus', 1005: 'margaret', 1006: 'grace', 1007: 'alan'}
```

### 6. `sorted()` for Stable Output

`sorted()` accepts any iterable — a set included — and returns a new, sorted list:

```python
print("6. customers, each once, alphabetically:", sorted({order["customer"] for order in ORDERS}))
```

*The set comprehension removes the duplicates, and `sorted()` puts what's left in a fixed order.*

Expected output:

```text
6. customers, each once, alphabetically: ['ada', 'alan', 'grace', 'linus', 'margaret']
```

Unlike line 2, this prints identically on every run.

> **Contrast — Java:** collecting unique customers in Java means `orders.stream().map(Order::customer).collect(Collectors.toSet())`. In Python it's the same comprehension as a list, with different brackets — `[...]` for a list, `{...}` for a set, `{k: v ...}` for a dict.

### Checking the Guided Lines

With lines 1 through 6 filled in, running `python src/comprehensions_lab.py` prints the following, with lines 7–12 still showing `None`. Remember that line 2 may list its names in a different order.

```text
1. every order's customer: ['ada', 'grace', 'ada', 'linus', 'margaret', 'grace', 'alan']
2. customers, each once: {'ada', 'grace', 'alan', 'margaret', 'linus'}
3. ids of orders with a quantity of 3 or more: [1003, 1006, 1007]
4. order labels: ['#1001 espresso', '#1002 latte', '#1003 muffin', '#1004 espresso', '#1005 latte', '#1006 tea', '#1007 espresso']
5. customer by order id: {1001: 'ada', 1002: 'grace', 1003: 'ada', 1004: 'linus', 1005: 'margaret', 1006: 'grace', 1007: 'alan'}
6. customers, each once, alphabetically: ['ada', 'alan', 'grace', 'linus', 'margaret']
```

---

## Exercises

Fill in lines 7 through 12. There are no hints this time: the expected output is all there is, and the brackets in it are part of the answer.

```text
7. every order's product: ['espresso', 'latte', 'muffin', 'espresso', 'latte', 'tea', 'espresso']
8. quantities ordered, each once: {1, 2, 3, 4}
9. customers of orders in the south: ['grace', 'margaret', 'grace']
10. line totals for the north: ['espresso: $6.50', 'muffin: $8.25', 'espresso: $9.75']
11. price by product: {'espresso': 3.25, 'latte': 4.5, 'muffin': 2.75, 'tea': 2.0}
12. regions, each once, alphabetically: ['east', 'north', 'south']
```

Every line must match exactly, and each one should print the same way every time.
