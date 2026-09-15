# Operators and Flow

We'll work through Python's operators and control flow by completing a file of small numbered sections until every one prints exactly what it should. Along the way we'll hit the behaviors that break Java and TypeScript expectations: `/` vs `//` vs `%`, `**`, augmented assignment, `==` vs `is`, chained comparisons, `and`/`or` returning an operand, truthiness and `if not items:`, `in`, conditional expressions, `if`/`elif`/`else`, `while`, `for` over a sequence (and the `range(len(...))` habit to avoid), `break`, `continue`, loop `else`, and `pass`.

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
   python src/flow_lab.py
   ```

The starter runs as-is. It prints every section header, with `None` wherever a TODO is still waiting. As we complete each section, its lines turn into the expected output.

### Project Layout

```text
001 - Operators and Flow/
├── README.md
└── src/
    ├── flow_lab.py     # the numbered sections we complete in the walkthrough
    └── predict.py      # predict-the-output questions for the exercises
```

---

## Guided Walkthrough

We'll work top to bottom through `src/flow_lab.py`. After each section, we run the file and compare that section's output with the expected output shown here (the same expected output is also written in the comment above each section). Sections with an **Explain:** prompt get a short comment answer, written directly under the prompt.

### Operators

#### 1. Division

We'll replace each `None` with the expression written in its label:

```python
print("7 / 2 =", 7 / 2)
print("7 // 2 =", 7 // 2)
print("-7 // 2 =", -7 // 2)
print("-7 % 2 =", -7 % 2)
```

*True division, floor division, and remainder, including a negative operand.*

Expected output:

```text
== 1. Division ==
7 / 2 = 3.5
7 // 2 = 3
-7 // 2 = -4
-7 % 2 = 1
```

`7 / 2` gives `3.5` even though both operands are integers. Notice also that `-7 // 2` is not the `-3` Java would produce — that's this section's **Explain:** prompt.

#### 2. Exponents

```python
print("2 ** 10 =", 2 ** 10)
print("-2 ** 2 =", -2 ** 2)
print("(-2) ** 2 =", (-2) ** 2)
```

*The `**` exponent operator, with and without parentheses around a negative base.*

Expected output:

```text
== 2. Exponents ==
2 ** 10 = 1024
-2 ** 2 = -4
(-2) ** 2 = 4
```

The only difference between the last two lines is the parentheses, and they produce opposite signs.

#### 3. Augmented Assignment

Each TODO sits directly above the `print` that shows its result. We'll add one augmented assignment per TODO:

```python
stock += 5
```

*Goes under the first TODO, above `print("after adding 5:", stock)`.*

```python
stock -= 3
```

*Goes under the second TODO.*

```python
stock *= 2
```

*Goes under the third TODO.*

Expected output:

```text
== 3. Augmented assignment ==
after adding 5: 15
after subtracting 3: 12
after doubling: 24
```

For the **Explain:** prompt, we'll start a REPL with `python`, run `stock = 10`, then `++stock`, and watch what comes back.

#### 4. `==` vs `is`

```python
print("first_order == second_order:", first_order == second_order)
print("first_order is second_order:", first_order is second_order)
print("first_order is same_order:", first_order is same_order)
```

*Comparing two lists by value with `==` and by identity with `is`.*

Expected output:

```text
== 4. == vs is ==
first_order == second_order: True
first_order is second_order: False
first_order is same_order: True
```

Look back at how `second_order` and `same_order` were each created at the top of the section before answering the prompt.

#### 5. Chained Comparisons

We'll write the range check once as a single chained comparison, then repeat it after `temperature` changes:

```python
print("72 is comfortable:", 68 <= temperature <= 76)
```

*One chained comparison checks both bounds, just as it would be written in math.*

```python
print("90 is comfortable:", 68 <= temperature <= 76)
```

*The same expression, now evaluated with `temperature` rebound to `90`.*

Expected output:

```text
== 5. Chained comparisons ==
72 is comfortable: True
90 is comfortable: False
```

#### 6. `and` / `or`

```python
print("display name:", nickname or username)
print("0 and 99 ->", 0 and 99)
print("5 and 99 ->", 5 and 99)
print("[] or 'empty' ->", [] or 'empty')
```

*`or` supplying a fallback, then `and` and `or` applied directly to non-boolean values.*

Expected output:

```text
== 6. and / or ==
display name: ada_l
0 and 99 -> 0
5 and 99 -> 99
[] or 'empty' -> empty
```

None of the results after the first line is `True` or `False`. Each is one of the two values on either side of the operator.

#### 7. Truthiness

```python
print("bool(0):", bool(0))
print("bool(''):", bool(''))
print("bool([]):", bool([]))
print("bool(None):", bool(None))
print("bool('0'):", bool('0'))
print("bool([0]):", bool([0]))
```

*`bool()` shows how Python treats each value when it appears in a condition.*

Expected output:

```text
== 7. Truthiness ==
bool(0): False
bool(''): False
bool([]): False
bool(None): False
bool('0'): True
bool([0]): True
```

#### 8. Checking for Empty

This is our first TODO that needs a block. We'll add the check under the first TODO — note the colons and the four-space indentation:

```python
if not cart:
    print("cart is empty")
else:
    print("cart has items")
```

*Testing an empty list directly through truthiness rather than comparing its length to zero.*

Then we'll paste the identical check under the second TODO, after `cart` is rebound to `["coffee"]`.

Expected output:

```text
== 8. Checking for empty ==
cart is empty
cart has items
```

#### 9. Membership

```python
print("'M' in sizes:", 'M' in sizes)
print("'XL' in sizes:", 'XL' in sizes)
print("'XL' not in sizes:", 'XL' not in sizes)
print("'tea' in 'steam':", 'tea' in 'steam')
```

*`in` and `not in` checking a list for a value, then checking a string for a substring.*

Expected output:

```text
== 9. Membership ==
'M' in sizes: True
'XL' in sizes: False
'XL' not in sizes: True
'tea' in 'steam': True
```

#### 10. Conditional Expressions

We'll replace each `label = None` line with the same conditional expression:

```python
label = "item" if quantity == 1 else "items"
```

*Python's version of `quantity == 1 ? "item" : "items"`: the value if true comes first, then the condition, then the value if false.*

Expected output:

```text
== 10. Conditional expressions ==
1 item
4 items
```

### Control Flow

#### 11. `if` / `elif` / `else`

We'll replace the `grade = None` line with a full chain, leaving the `print` below it unchanged:

```python
if score >= 90:
    grade = "A"
elif score >= 80:
    grade = "B"
elif score >= 70:
    grade = "C"
else:
    grade = "F"
```

*Python spells "else if" as the single keyword `elif`. Only the first matching branch runs.*

Expected output:

```text
== 11. if / elif / else ==
grade: B
```

#### 12. `while`

We'll add the loop under the TODO, above the existing `print("liftoff")`:

```python
while countdown > 0:
    print(countdown)
    countdown -= 1
```

*Both indented lines form the loop body. `print("liftoff")` stays unindented, so it runs once, after the loop ends.*

Expected output:

```text
== 12. while ==
3
2
1
liftoff
```

Try indenting `print("liftoff")` to line up with the loop body and running the file again — it prints after every number instead of once. Then put it back.

#### 13. `for` Loops

Under the first TODO, we'll loop over the list itself:

```python
for name in crew:
    print(name)
```

*Each pass binds `name` directly to the next item in `crew`. There's no index anywhere.*

Under the second TODO, we'll write the same loop the way a Java habit would:

```python
for i in range(len(crew)):
    print(crew[i])
```

*`range(len(crew))` produces the indexes 0, 1, and 2, and each pass looks the name up by index.*

Expected output:

```text
== 13. for loops ==
Ada
Grace
Linus
Ada
Grace
Linus
```

> **Contrast — Java:** `for (int i = 0; i < crew.size(); i++)` is how many of us learned to loop. Python has no three-part `for` at all; `for name in crew:` is the idiomatic form, and `range(len(...))` is recreating the Java loop by hand.

#### 14. `break` and `continue`

```python
for reading in readings:
    if reading < 0:
        continue
    if reading > 25:
        break
    print("reading:", reading)
```

*`continue` skips straight to the next reading; `break` ends the loop entirely.*

Expected output:

```text
== 14. break and continue ==
reading: 12
reading: 7
```

Tracing it through: `-1` is skipped by `continue`, `30` triggers `break`, and the final `8` is never reached.

#### 15. Loop `else`

Under the first TODO:

```python
for reading in first_batch:
    if reading > limit:
        print("over limit:", reading)
        break
else:
    print("all readings within limit")
```

*The `else` lines up with `for`, not with `if` — it belongs to the loop.*

Under the second TODO, we'll write the same loop with `second_batch` in place of `first_batch`.

Expected output:

```text
== 15. Loop else ==
over limit: 30
all readings within limit
```

Getting the `else` indentation wrong changes the meaning: indented under the `for` to line up with `if`, it would become part of the `if` and run for every reading that's within the limit.

#### 16. `pass`

We'll uncomment the loop at the bottom of the file (in VS Code, select the lines and press **Ctrl+/**, or **Cmd+/** on macOS) and run the file. It fails with an **`IndentationError`**, and nothing prints — not even section 1. The `if` block contains only a comment, and a comment isn't a statement.

We'll add `pass` under the comment to fix it:

```python
for reading in [5, -2, 9]:
    if reading < 0:
        # negative readings are ignored for now
        pass
    else:
        print("kept:", reading)
```

*`pass` is a statement that does nothing, giving the empty branch the body Python requires.*

Expected output:

```text
== 16. pass ==
kept: 5
kept: 9
```

### Checking the Whole File

With every section done, running `python src/flow_lab.py` prints this, top to bottom:

```text
== 1. Division ==
7 / 2 = 3.5
7 // 2 = 3
-7 // 2 = -4
-7 % 2 = 1
== 2. Exponents ==
2 ** 10 = 1024
-2 ** 2 = -4
(-2) ** 2 = 4
== 3. Augmented assignment ==
after adding 5: 15
after subtracting 3: 12
after doubling: 24
== 4. == vs is ==
first_order == second_order: True
first_order is second_order: False
first_order is same_order: True
== 5. Chained comparisons ==
72 is comfortable: True
90 is comfortable: False
== 6. and / or ==
display name: ada_l
0 and 99 -> 0
5 and 99 -> 99
[] or 'empty' -> empty
== 7. Truthiness ==
bool(0): False
bool(''): False
bool([]): False
bool(None): False
bool('0'): True
bool([0]): True
== 8. Checking for empty ==
cart is empty
cart has items
== 9. Membership ==
'M' in sizes: True
'XL' in sizes: False
'XL' not in sizes: True
'tea' in 'steam': True
== 10. Conditional expressions ==
1 item
4 items
== 11. if / elif / else ==
grade: B
== 12. while ==
3
2
1
liftoff
== 13. for loops ==
Ada
Grace
Linus
Ada
Grace
Linus
== 14. break and continue ==
reading: 12
reading: 7
== 15. Loop else ==
over limit: 30
all readings within limit
== 16. pass ==
kept: 5
kept: 9
```

Before moving on, we'll make sure every **Explain:** prompt in the file has an answer under it.

---

## Exercises

**Predict the output.** Open `src/predict.py`, but **don't run it yet**. It holds ten short questions, each a few lines of code built from the operators and control flow above.

1. For each question, read the code and write the exact output you expect on its `# Prediction:` line.
2. Only once all ten predictions are written, run the file:

   ```text
   python src/predict.py
   ```

3. Compare each `Q` line of real output against your prediction.
4. For every prediction that missed, add a comment under it explaining why Python produced what it did.
