# The Standard Library

Python's standard library is famously large — the phrase is "**batteries included**." Parsing JSON, handling dates, counting and grouping data, reading environment variables, and generating random numbers all work with nothing installed. This lesson covers the modules we'll reach for most, and how to find the rest on our own.

---

## Batteries Included

Every module in this lesson ships with Python itself. There's nothing to add to `requirements.txt` and nothing to `pip install`; an `import` is all it takes.

**Contrast:** Java has no JSON parser in the JDK, so nearly every Java project adds Jackson or Gson as a dependency. Python projects skip that entire category of dependency, which is part of why their dependency lists tend to be shorter than a Java developer expects.

---

## `json`

The **`json`** module converts between JSON text and ordinary Python objects. There are four functions, and the naming rule is simple: the ones ending in **`s`** work with **strings**; the others work with **files**.

| Function | Converts |
|----------|----------|
| `json.loads(text)` | JSON string → Python object |
| `json.dumps(obj)` | Python object → JSON string |
| `json.load(file)` | Open JSON file → Python object |
| `json.dump(obj, file)` | Python object → written to an open file |

```python
import json

text = '{"name": "Ada", "languages": ["Python", "Java"], "active": true, "manager": null}'

employee = json.loads(text)
print(employee["languages"][0])         # Python
print(employee["active"], employee["manager"])    # True None

print(json.dumps(employee, indent=2))
```

*Parsing a JSON string into a dict, reading from it, and serializing it back with indentation.*

```text
{
  "name": "Ada",
  "languages": [
    "Python",
    "Java"
  ],
  "active": true,
  "manager": null
}
```

*The output of `json.dumps` with `indent=2`. Python's `True` and `None` become JSON's `true` and `null`.*

Working with files uses `with open(...)`, as in File I/O:

```python
with open("employees.json", encoding="utf-8") as file:
    employees = json.load(file)

summary = {"count": len(employees), "names": [e["name"] for e in employees]}

with open("summary.json", "w", encoding="utf-8") as file:
    json.dump(summary, file, indent=2)
```

*Loading a JSON array from one file, building a summary dict, and writing it to another file.*

### The Dict-to-JSON Mapping

There are no data-binding classes or annotations. JSON maps directly onto built-in types:

| JSON | Python |
|------|--------|
| object | `dict` |
| array | `list` |
| string | `str` |
| number (integer) | `int` |
| number (real) | `float` |
| `true` / `false` | `True` / `False` |
| `null` | `None` |

A few conversions don't round-trip cleanly:

- **Dict keys become strings.** `json.dumps({1: "a"})` produces `'{"1": "a"}'`, which loads back with the string key `"1"`.
- **Tuples become arrays**, and load back as lists.
- **Other types aren't serializable at all.** Sets, `datetime` objects, and instances of our own classes raise `TypeError: Object of type set is not JSON serializable`. We convert them first — a set to a list, a datetime to a string with `.isoformat()`, an object to a dict.

---

## `datetime`

The **`datetime`** module provides `date` (a calendar date), `datetime` (a date and time), and `timedelta` (a duration):

```python
from datetime import date, datetime, timedelta

today = date.today()
now = datetime.now()

release = date(2026, 3, 14)
print(release.isoformat())                  # 2026-03-14
print(date.fromisoformat("2026-03-14"))     # 2026-03-14
```

*Getting the current date and time, constructing a specific date, and converting to and from ISO 8601 strings.*

### Parsing and Formatting

For formats other than ISO 8601, **`strptime`** ("string parse time") parses text into a `datetime`, and **`strftime`** ("string format time") formats one as text. Both use `%` codes:

```python
from datetime import datetime

deployed = datetime.strptime("14/03/2026 09:30", "%d/%m/%Y %H:%M")
print(deployed)                                     # 2026-03-14 09:30:00
print(deployed.strftime("%A, %d %B %Y at %H:%M"))   # Saturday, 14 March 2026 at 09:30
```

*Parsing a day-first date string, then formatting it in a readable long form.*

| Code | Meaning | Example |
|------|---------|---------|
| `%Y` | Four-digit year | `2026` |
| `%m` | Month number, zero-padded | `03` |
| `%d` | Day of month, zero-padded | `14` |
| `%H` | Hour, 24-hour clock | `09` |
| `%M` | Minute | `30` |
| `%S` | Second | `00` |
| `%B` / `%b` | Month name, full / abbreviated | `March` / `Mar` |
| `%A` / `%a` | Weekday name, full / abbreviated | `Saturday` / `Sat` |

The classic mistake is case: **`%m` is month and `%M` is minute.** A string that doesn't match its format raises `ValueError`.

### Date Arithmetic

Subtracting dates produces a `timedelta`, and adding a `timedelta` to a date moves it:

```python
from datetime import date, timedelta

due = date(2026, 3, 14) + timedelta(days=30)
print(due)                  # 2026-04-13

elapsed = date(2026, 3, 14) - date(2025, 12, 25)
print(elapsed.days)         # 79
```

*Calculating a due date thirty days out, and the number of days between two dates.*

`datetime.now()` returns a **naive** datetime, with no time zone attached. Time zone handling exists in the standard library but is beyond this lesson.

**Contrast:** these types line up with `java.time` — `LocalDate`, `LocalDateTime`, and `Duration`. The format languages differ: Java's `DateTimeFormatter` patterns look like `yyyy-MM-dd`, while Python's look like `%Y-%m-%d`.

---

## `collections`

The **`collections`** module adds specialized versions of the built-in containers.

### `Counter`

A **`Counter`** is a dict that counts things:

```python
from collections import Counter

words = "the cat and the hat and the bat".split()
counts = Counter(words)

print(counts["the"])            # 3
print(counts["dog"])            # 0 — missing items count as zero, no KeyError
print(counts.most_common(2))    # [('the', 3), ('and', 2)]
```

*Counting word frequencies in one call, then finding the most common.*

It replaces the manual `counts[word] = counts.get(word, 0) + 1` loop from the Dictionaries lesson.

### `defaultdict`

A **`defaultdict`** creates a value automatically the first time a missing key is accessed. Its argument is a **function** that produces that value — often a type like `list`, `int`, or `set`:

```python
from collections import defaultdict

orders = [("ada", "book"), ("grace", "pen"), ("ada", "lamp")]

by_customer = defaultdict(list)
for customer, item in orders:
    by_customer[customer].append(item)

print(dict(by_customer))        # {'ada': ['book', 'lamp'], 'grace': ['pen']}
```

*Grouping items by customer. A missing customer gets a fresh empty list from calling `list()`.*

This is the same grouping `setdefault()` handled earlier, without repeating the default at every access. One side effect: merely *reading* a missing key inserts it.

### `namedtuple`

A **`namedtuple`** creates a tuple type whose positions also have names:

```python
from collections import namedtuple

Point = namedtuple("Point", ["x", "y"])
p = Point(3, 4)

print(p.x, p[1])        # 3 4 — access by name or by position
print(p)                # Point(x=3, y=4)
x, y = p                # still unpacks like any tuple
```

*A lightweight, immutable record that remains a real tuple.*

For new code, a `@dataclass` is usually the more flexible choice. `namedtuple` still turns up often, especially where a function already returns tuples and names would make them clearer.

---

## `os` and `sys`

**`os`** is the interface to the operating system; **`sys`** is the interface to the running Python interpreter.

```python
import os
import sys

db_url = os.environ.get("DATABASE_URL", "sqlite:///local.db")
print(os.getcwd())          # the current working directory

print(sys.argv)             # ['report.py', '--verbose'] for: python report.py --verbose
print(sys.executable)       # path to the interpreter running this program
print(sys.version)          # the Python version string

sys.exit(1)                 # end the program with exit status 1
```

*Reading an environment variable with a fallback, checking the working directory, inspecting the command line and interpreter, and exiting with a status code.*

- **`os.environ`** behaves like a dict of environment variables, so `.get()` with a default is the safe way to read one.
- **`sys.argv`** is the list of command-line arguments, with the script name first.
- **`sys.path`**, from the Modules and Packages lesson, lives here too, as does `sys.prefix` from the virtual environments lesson.
- The `os` module also has many path functions under `os.path`. They predate `pathlib`, and `pathlib` is preferred in new code.

**Contrast:** these replace Java's `System.getenv()`, `System.exit()`, and `main`'s `args` array, and TypeScript's `process.env`, `process.exit()`, and `process.argv`.

---

## `random` and `math`

Two more modules round out everyday needs:

```python
import math
import random

random.randint(1, 6)                    # a die roll — both ends included
random.choice(["red", "green", "blue"]) # one random item
random.shuffle(deck)                    # shuffles a list in place; returns None

math.sqrt(16)                           # 4.0
math.floor(2.7), math.ceil(2.1)         # (2, 3)
math.pi                                 # 3.141592653589793
math.isclose(0.1 + 0.2, 0.3)            # True — safe float comparison
```

*Common random-number and math operations.*

Note that `random.randint(1, 6)` **includes** both ends, unlike Java's `nextInt(6)`, which excludes its bound. The `random` module isn't suitable for passwords or tokens; the standard library's `secrets` module is.

---

## Reading the Docs

The standard library is too large to memorize, so the useful skill is finding things quickly:

- **The library reference** at docs.python.org/3/library lists every module, grouped by purpose — text processing, data types, file formats, operating system services, and so on. Scanning the group headings is often the fastest way to discover that a module exists.
- **Each module page** opens with a summary and, for many modules, a link to its source code. Functions are documented with their full signatures, and notes like "Added in version 3.x" show what requires a newer Python.
- **In the REPL,** `help(json)` shows a module's documentation and `help(json.dumps)` shows one function's. `dir(json)` lists everything a module contains.
- **Trying it immediately** in the REPL is the fastest way to confirm what a function returns.

A reasonable habit before reaching for a third-party package: search the standard library first. The answer is often already installed.

---

## Key Takeaways

- The standard library covers JSON, dates, specialized collections, the operating system, and more, with no installation.
- `json.loads`/`json.dumps` work with strings and `json.load`/`json.dump` with files. JSON objects become dicts, arrays become lists, and `null` becomes `None`.
- JSON can't represent sets, datetimes, or custom objects, and dict keys come back as strings.
- `datetime` provides `date`, `datetime`, and `timedelta`. `strptime` parses and `strftime` formats, using `%` codes — `%m` is month, `%M` is minute.
- `Counter` counts, `defaultdict` supplies defaults for missing keys, and `namedtuple` names tuple positions.
- `os.environ` reads environment variables; `sys.argv`, `sys.path`, and `sys.exit` expose the interpreter.
- `random.randint` includes both ends; `math` supplies the usual functions and `isclose` for float comparison.
- Search docs.python.org and use `help()` and `dir()` before adding a dependency.
