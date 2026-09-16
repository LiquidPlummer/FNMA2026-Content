# Strings

Python strings are immutable sequences of Unicode characters, just like Java's `String`, and they come with a rich set of methods. Three things will feel new: there's no `char` type at all, slicing replaces `substring()` with a far more flexible syntax, and there's no `StringBuilder` — building a large string efficiently is a job for `join()`.

---

## Quote Forms

Single and double quotes create exactly the same kind of string. The usual reason to pick one over the other is to avoid escaping a quote inside:

```python
single = 'Ada'
double = "Ada"
print(single == double)         # True

contraction = "It's ready"
speech = 'She said "ship it"'
```

*Single- and double-quoted strings are identical; choosing one lets the other appear inside without escaping.*

**Triple-quoted strings** — `"""..."""` or `'''...'''` — can span multiple lines, and every newline and space inside them is kept:

```python
message = """Build #412 passed.
  Duration: 3m 12s
  Warnings: 0"""

print(message)
```

*A multi-line string. The two-space indentation on the second and third lines is part of the string's content.*

Triple quotes are also the convention for docstrings. Because indentation inside them is literal, a triple-quoted string written inside an indented function body carries that indentation along.

### There Is No `char`

A single character is just a string of length one. Indexing a string gives back another string:

```python
first = "Python"[0]
print(first, type(first))       # P <class 'str'>
```

*Indexing a string returns a one-character `str`, not a separate character type.*

**Contrast:** Java distinguishes `'a'` (a `char`, a numeric type) from `"a"` (a `String`). Python has no such split, which is why either quote style works for any length.

---

## f-Strings and Format Specs

f-strings, introduced earlier, embed expressions in `{braces}`. After a colon inside the braces, a **format spec** controls how the value is laid out. The general shape is `{value:[fill][align][width][,][.precision][type]}`:

```python
name = "Widget"
qty = 7
price = 3.5

print(f"|{name:<10}|")      # |Widget    |  — left-aligned in 10 characters
print(f"|{name:>10}|")      # |    Widget|  — right-aligned
print(f"|{name:^10}|")      # |  Widget  |  — centered
print(f"|{name:*^10}|")     # |**Widget**|  — centered, padded with *
print(f"|{qty:>5}|")        # |    7|
print(f"|{price:>8.2f}|")   # |    3.50|  — two decimals, right-aligned in 8
```

*Alignment characters `<`, `>`, and `^`, an optional fill character, a width, and a precision for floats.*

A few more specs come up constantly:

```python
print(f"{1234567.891:,.2f}")    # 1,234,567.89 — thousands separators
print(f"{0.256:.1%}")           # 25.6% — multiply by 100 and add %
print(f"{42:05}")               # 00042 — zero-padded to width 5

width = 12
print(f"|{name:<{width}}|")     # |Widget      | — width taken from a variable
print(f"{{literal braces}}")    # {literal braces}
```

*Thousands separators, percentages, zero padding, a width supplied by a variable, and doubled braces for literal `{` and `}`.*

Without an explicit alignment, strings default to left-aligned and numbers to right-aligned. Combining widths lets us produce aligned columns:

```python
rows = [("Widget", 7, 3.5), ("Gadget", 12, 19.99), ("Doohickey", 3, 105.0)]

print(f"{'Item':<12}{'Qty':>5}{'Price':>10}")
for item, qty, price in rows:
    print(f"{item:<12}{qty:>5}{price:>10.2f}")
```

*Printing a header and rows with fixed column widths.*

```text
Item          Qty     Price
Widget          7      3.50
Gadget         12     19.99
Doohickey       3    105.00
```

*The output: every column lines up because each field is padded to the same width.*

---

## Escape Sequences and Raw Strings

Inside ordinary string literals, a backslash begins an **escape sequence**:

| Escape | Meaning |
|--------|---------|
| `\n` | Newline |
| `\t` | Tab |
| `\\` | A literal backslash |
| `\'` and `\"` | A literal quote |
| `\u00e9` | The Unicode character with that code (here, `é`) |

This causes trouble with Windows paths, where backslashes are the separator:

```python
path = "C:\new\table.txt"
print(path)
# C:
# ew	able.txt
```

*`\n` and `\t` were interpreted as a newline and a tab, mangling the path.*

A **raw string**, prefixed with `r`, treats backslashes as ordinary characters:

```python
path = r"C:\new\table.txt"
print(path)                     # C:\new\table.txt
```

*The `r` prefix turns off escape processing, so the backslashes survive.*

Raw strings are also standard for regular expressions, which use backslashes heavily. One quirk: a raw string can't end with a single backslash — `r"C:\folder\"` is a syntax error, because the final `\"` still escapes the closing quote. For file paths, `pathlib` (covered in File I/O) and forward slashes, which Windows also accepts, sidestep the problem entirely.

---

## Immutability and Building Strings

Strings can't be changed in place. Every operation that looks like a modification returns a **new** string:

```python
word = "hello"
word[0] = "H"               # TypeError: 'str' object does not support item assignment

capitalized = "H" + word[1:]
print(word, capitalized)    # hello Hello
```

*Assigning into a string fails; building a new string from pieces works.*

That includes every string method. A frequent bug is calling one and discarding the result:

```python
name = "  Ada  "
name.strip()                # returns 'Ada', which is thrown away
print(repr(name))           # '  Ada  ' — unchanged

name = name.strip()         # rebind the name to the new string
```

*String methods never modify the original; the result must be assigned.*

### The Cost of `+=` in a Loop

Because strings are immutable, `report += line` can't append to the existing string. It builds a new string containing everything so far plus the new piece:

```python
report = ""
for line in lines:
    report += line + "\n"   # copies the whole report so far, every pass
```

*Growing a string one piece at a time. As `report` gets longer, each copy gets more expensive.*

For a handful of pieces this doesn't matter. For thousands, the repeated copying adds up — the total work grows with the *square* of the number of pieces. CPython sometimes optimizes this pattern, but that's an implementation detail rather than something to rely on.

### `str.join()`

The idiomatic approach collects the pieces in a list, then joins them once:

```python
parts = []
for line in lines:
    parts.append(line)

report = "\n".join(parts)
```

*Collecting pieces cheaply in a list, then producing the final string in a single step.*

`join` is called on the **separator**, and its argument is any iterable of strings:

```python
", ".join(["Java", "TypeScript", "Python"])   # 'Java, TypeScript, Python'
"".join(["a", "b", "c"])                       # 'abc'
", ".join([1, 2, 3])                           # TypeError: sequence item 0: expected str instance, int found
```

*Joining with a separator, joining with none, and failing because the items aren't strings.*

Every item must already be a string; numbers have to be converted with `str()` first.

**Contrast:** Java's `String` is immutable too, and the answer there is `StringBuilder`. Python has no builder class — a list plus `join` does that job. TypeScript developers will know `join` from arrays, but notice the reversal: JavaScript writes `parts.join(", ")`, while Python writes `", ".join(parts)`.

---

## Slicing

A **slice** extracts part of a sequence using `s[start:stop:step]`. Positions can be counted from the front starting at `0`, or from the back starting at `-1`:

```text
  P   y   t   h   o   n
  0   1   2   3   4   5
 -6  -5  -4  -3  -2  -1
```

*Positive and negative indexes for each character of `"Python"`.*

```python
code = "ORD-2026-00042"

code[0]         # 'O'
code[-1]        # '2' — the last character
code[0:3]       # 'ORD'
code[4:8]       # '2026'
code[-5:]       # '00042' — the last five characters
code[:-6]       # 'ORD-2026' — everything except the last six
code[::2]       # 'OD22-04' — every second character
code[::-1]      # '24000-6202-DRO' — reversed
```

*Indexing and slicing a string with positive, negative, omitted, and stepped positions.*

The rules:

- **`stop` is excluded.** `code[0:3]` returns indexes 0, 1, and 2.
- **Omitting `start` or `stop`** means "from the beginning" or "to the end."
- **A negative `step`** walks backward, so `[::-1]` reverses.
- **Slices never raise.** `code[10:100]` quietly returns `'0042'`, while indexing `code[100]` raises `IndexError`.

The same slicing syntax works on lists and tuples, as the next lesson shows.

**Contrast:** slicing replaces Java's `substring(begin, end)`, and Java has no negative indexes at all — the last character is `s.charAt(s.length() - 1)`. JavaScript's `.slice()` does accept negative positions, but ordinary indexing like `s[-1]` returns `undefined`, and there's no step. Python applies negative positions everywhere and adds the step.

---

## Everyday Methods

A handful of methods handle most text cleanup:

```python
raw = "  Ada Lovelace, ENGINEER  "

clean = raw.strip()                     # 'Ada Lovelace, ENGINEER'
name, role = clean.split(", ")          # ['Ada Lovelace', 'ENGINEER'], unpacked

print(role.lower())                     # engineer
print(name.upper())                     # ADA LOVELACE
print(name.replace("Ada", "Augusta"))   # Augusta Lovelace
print(name.startswith("Ada"))           # True
print("report.csv".endswith(".csv"))    # True
```

*Trimming whitespace, splitting on a separator, changing case, replacing text, and checking prefixes and suffixes.*

Some details worth knowing:

- **`strip()`** removes whitespace from both ends; `lstrip()` and `rstrip()` handle one end. Given an argument, they strip those characters instead: `"--draft--".strip("-")` is `'draft'`.
- **`split()` with no argument** splits on any run of whitespace and discards empty pieces: `"a  b\tc".split()` is `['a', 'b', 'c']`.
- **`split(sep)` with an argument** splits on exactly that separator and keeps empty pieces: `"a,,b".split(",")` is `['a', '', 'b']`.
- **`replace(old, new)`** replaces every occurrence, not just the first.
- **`startswith` and `endswith`** accept a tuple to test several options at once: `filename.endswith((".csv", ".tsv"))`.

### `in` for Substrings

The membership operator tests whether one string appears inside another:

```python
print("Love" in "Ada Lovelace")     # True
print("love" in "Ada Lovelace")     # False — case-sensitive
print("@" not in "ada.example.com") # True
```

*Substring tests with `in` and `not in`.*

**Contrast:** `in` replaces Java's `contains()` and JavaScript's `includes()`, using the same operator that tests membership in lists, sets, and dicts.

---

## `str` vs `bytes`

A `str` holds **text** — a sequence of Unicode characters. A **`bytes`** object holds raw **8-bit values**, which is what files, network sockets, and hash functions actually deal in. Converting between them requires an **encoding**:

```python
text = "café"
data = text.encode("utf-8")

print(data)                     # b'caf\xc3\xa9'
print(len(text), len(data))     # 4 5 — é takes two bytes in UTF-8
print(data.decode("utf-8"))     # café
```

*Encoding text into UTF-8 bytes and decoding it back.*

- **`encode()`** turns `str` into `bytes`; **`decode()`** turns `bytes` into `str`.
- A `bytes` literal is written with a `b` prefix: `b"abc"`.
- Indexing `bytes` gives an integer, not a character: `b"abc"[0]` is `97`.
- The two types never mix silently: `"abc" + b"def"` raises `TypeError`.

Most of the time we work purely in `str`, and conversion happens at the edges of a program — reading a binary file, or receiving data over a network.

**Contrast:** Java's `getBytes(StandardCharsets.UTF_8)` and `new String(bytes, StandardCharsets.UTF_8)` do the same job. One practical difference: a Java or JavaScript string's length counts UTF-16 code units, so an emoji like `"😀"` has length 2. A Python `str` is a sequence of whole Unicode characters, so `len("😀")` is 1.

---

## Key Takeaways

- Single and double quotes are interchangeable; triple quotes allow multi-line strings with their whitespace intact. There's no `char` type.
- f-string format specs follow a colon: `<`, `>`, `^` align; a number sets width; `.2f` sets precision; `,` adds separators; `%` formats percentages.
- Backslashes start escape sequences; an `r` prefix makes a raw string that leaves them alone.
- Strings are immutable. Methods return new strings, so their results must be assigned.
- `+=` in a loop copies the whole string every time; collect pieces in a list and use `separator.join(pieces)`.
- Slicing uses `[start:stop:step]` with the stop excluded, negative indexes counting from the end, and no errors for out-of-range slices.
- `split`, `strip`, `replace`, `startswith`, `lower`, and `upper` handle most cleanup; `in` tests for substrings.
- `str` is text and `bytes` is raw data; `encode()` and `decode()` convert between them using a named encoding.
