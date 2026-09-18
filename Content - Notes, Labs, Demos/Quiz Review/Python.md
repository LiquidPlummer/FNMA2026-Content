# Python — Quiz Review

This page reviews the Python ideas the quiz checks: `for` loops and `range()`, lists and how they differ from Java arrays, dictionaries, class inheritance, exception handling, the standard library, and Flask. Each section has a short summary, a few questions to test ourselves with, and links to the notes that explain the idea in full.

This is a study guide, not an answer key. Several quiz questions ask us to pick the correct code. For those, the best preparation is to write the snippets in the check-yourself questions ourselves, then check them against the notes or run them.

---

## `for` Loops and `range()`

Python's `for` loop has only one form. It walks through the **items** of something iterable, one at a time. There's no C-style `for (int i = 0; i < n; i++)` loop.

```python
for language in ["Java", "TypeScript", "Python"]:
    print(language)
```

When we need a run of integers, `range()` produces them:

```python
list(range(5))            # [0, 1, 2, 3, 4]
list(range(2, 6))         # [2, 3, 4, 5]
list(range(10, 0, -3))    # [10, 7, 4, 1]
```

The rule to remember is that `range(start, stop, step)` **includes `start` and excludes `stop`**. If we leave them out, `start` defaults to `0` and `step` defaults to `1`.

There are no braces. Code belongs to a loop because it's indented under the loop's line, which ends in a colon.

**Check yourself**
- What does `range(1, 5)` produce? What does `range(5)` produce?
- Write a loop that prints the numbers 1 through 5, one per line. Check the `range()` arguments against the stop-is-excluded rule.
- Why is `for i in range(len(names)):` called a "Java accent," and what's the idiomatic way to write it?

**Go deeper**
- Indentation and Control Flow: [Indentation Is Syntax](../Python/001%20-%20Setup%20and%20Syntax/006%20-%20Indentation%20and%20Control%20Flow/Notes/001%20-%20Indentation%20and%20Control%20Flow.md#indentation-is-syntax), [range()](../Python/001%20-%20Setup%20and%20Syntax/006%20-%20Indentation%20and%20Control%20Flow/Notes/001%20-%20Indentation%20and%20Control%20Flow.md#range), [The Java Habit](../Python/001%20-%20Setup%20and%20Syntax/006%20-%20Indentation%20and%20Control%20Flow/Notes/001%20-%20Indentation%20and%20Control%20Flow.md#the-java-habit)

---

## Lists, and How They Differ from Java Arrays

A **list** is Python's ordered, mutable sequence, written with square brackets. The notes call it "Python's growable array." A single list type does the work that Java splits across `T[]`, `ArrayList<T>`, and `List.of()`.

The properties to compare with Java:

- **A list changes size in place.** Methods add and remove items after the list is created.
- **A list holds any mix of types.** There's no type parameter. A hint like `list[str]` documents intent, but Python never enforces it.
- **Assignment shares the list; it doesn't copy it.** Two names can point to the same list object.

The methods that change a list, taken from the notes' example:

```python
tasks = ["build", "test"]

tasks.append("deploy")            # ['build', 'test', 'deploy']
tasks.insert(0, "lint")           # ['lint', 'build', 'test', 'deploy']
tasks.extend(["tag", "notify"])   # [..., 'deploy', 'tag', 'notify']
tasks.pop()                       # removes and returns 'notify'
tasks.remove("test")              # removes the first 'test', by value
```

All of these except `pop` return `None`. They change the list itself rather than returning a new one.

**Check yourself**
- A Java `String[]` is created with `new String[3]`. Name two things we can do with a Python list that we can't do with that array.
- What does `tasks.append(["tag", "notify"])` add to the list? How would `extend` behave differently?
- Why does `result = tasks.append("x")` leave `result` set to `None`?

**Go deeper**
- Lists and Tuples: [List Literals and Indexing](../Python/002%20-%20Data%20and%20Collections/002%20-%20Lists%20and%20Tuples/Notes/001%20-%20Lists%20and%20Tuples.md#list-literals-and-indexing), [Lists Are Heterogeneous](../Python/002%20-%20Data%20and%20Collections/002%20-%20Lists%20and%20Tuples/Notes/001%20-%20Lists%20and%20Tuples.md#lists-are-heterogeneous), [Modifying a List](../Python/002%20-%20Data%20and%20Collections/002%20-%20Lists%20and%20Tuples/Notes/001%20-%20Lists%20and%20Tuples.md#modifying-a-list), [Reference Semantics](../Python/002%20-%20Data%20and%20Collections/002%20-%20Lists%20and%20Tuples/Notes/001%20-%20Lists%20and%20Tuples.md#reference-semantics)

---

## Dictionaries

A **dict** maps **keys to values**. Where Java would use a `HashMap`, Python code uses a dict. A dict literal is a set of `key: value` pairs inside braces:

```python
user = {"name": "Ada", "role": "admin", "active": True}
```

- `user["name"]` reads a value and raises `KeyError` if the key is missing. `user.get("name", default)` returns a fallback value instead.
- Assigning to a key adds it if it's new and updates it if it exists. There's no `put` method.
- `in` checks keys, not values.
- Keys must be **hashable**, meaning immutable values like strings, numbers, or tuples of them. A list can't be a key.
- A dict keeps its keys in the order they were inserted.

A dict does the job of both a `Map` and a small data class. It's Python's default shape for a record, and it's the same shape as JSON data.

**Check yourself**
- What's the difference between `user["phone"]` and `user.get("phone")` when the dict has no `phone` key?
- Why can a tuple be a dict key when a list can't?
- How does looking up a value in a dict differ from looking one up in a list?

**Go deeper**
- Dictionaries and Sets: [Dict Literals and Key Access](../Python/002%20-%20Data%20and%20Collections/003%20-%20Dictionaries%20and%20Sets/Notes/001%20-%20Dictionaries%20and%20Sets.md#dict-literals-and-key-access), [Keys Must Be Hashable](../Python/002%20-%20Data%20and%20Collections/003%20-%20Dictionaries%20and%20Sets/Notes/001%20-%20Dictionaries%20and%20Sets.md#keys-must-be-hashable), [The Dict as a General-Purpose Record](../Python/002%20-%20Data%20and%20Collections/003%20-%20Dictionaries%20and%20Sets/Notes/001%20-%20Dictionaries%20and%20Sets.md#the-dict-as-a-general-purpose-record)

---

## Classes and Inheritance

A class is defined with `class`, and `__init__` sets up each new instance. Every method takes `self` explicitly as its first parameter. To inherit, a subclass **names its parent in parentheses** after its own name. There's no `extends` keyword:

```python
class Account:
    def __init__(self, owner, balance=0):
        self.owner = owner
        self.balance = balance

class SavingsAccount(Account):
    def __init__(self, owner, balance=0, rate=0.02):
        super().__init__(owner, balance)
        self.rate = rate
```

- To override a method, define a method with the same name. There's no `@Override`.
- `super()` reaches the parent class's version of a method.
- Unlike Java, **`super().__init__()` is never called automatically.** If we forget it, the parent's attributes are never created.
- Every class ultimately inherits from the built-in `object`.

**Check yourself**
- Write the first line of a `Dog` class that inherits from `Animal`.
- `Dog` defines its own `__init__`. What must that `__init__` do so the attributes set in `Animal.__init__` still exist?
- Why does creating an object never use the `new` keyword in Python?

**Go deeper**
- Classes and Objects: [Defining a Class](../Python/003%20-%20Objects%20and%20Structure/001%20-%20Classes%20and%20Objects/Notes/001%20-%20Classes%20and%20Objects.md#defining-a-class), [self Is Explicit](../Python/003%20-%20Objects%20and%20Structure/001%20-%20Classes%20and%20Objects/Notes/001%20-%20Classes%20and%20Objects.md#self-is-explicit), [Inheritance and super()](../Python/003%20-%20Objects%20and%20Structure/001%20-%20Classes%20and%20Objects/Notes/001%20-%20Classes%20and%20Objects.md#inheritance-and-super)

---

## Handling Exceptions

Python's `try`/`except` is the counterpart of Java's `try`/`catch`, and **the keyword is `except`, not `catch`.** Code that might fail goes in the `try` block. Each `except` clause names the exception type it handles:

```python
def parse_quantity(text):
    try:
        return int(text)
    except ValueError:
        print(f"Not a whole number: {text!r}")
        return 0
```

- `except SomeError as error:` binds the exception object to a name, so we can inspect its message.
- `else` runs only if the `try` block raised nothing. `finally` always runs.
- Exceptions form a class hierarchy, and an `except` clause also catches every subclass of the type it names. Clauses are checked top to bottom and the first match wins, so specific types go first.
- Catch the narrowest type that fits. A bare `except:` with no type hides real bugs and even blocks Ctrl+C.
- Python has no checked exceptions and no `throws` clause.

**Check yourself**
- Which built-in exception does dividing by zero raise, and what's its parent class? The hierarchy diagram in the notes shows both.
- Write a `try`/`except` that divides two numbers and prints a message, instead of crashing, when the divisor is zero.
- A bare `except:` would also "handle" the division error. Why is it still the wrong choice?

**Go deeper**
- Exceptions: [try and except](../Python/003%20-%20Objects%20and%20Structure/003%20-%20Exceptions/Notes/001%20-%20Exceptions.md#try-and-except), [else and finally](../Python/003%20-%20Objects%20and%20Structure/003%20-%20Exceptions/Notes/001%20-%20Exceptions.md#else-and-finally), [The Exception Hierarchy](../Python/003%20-%20Objects%20and%20Structure/003%20-%20Exceptions/Notes/001%20-%20Exceptions.md#the-exception-hierarchy), [Why a Bare except: Is Wrong](../Python/003%20-%20Objects%20and%20Structure/003%20-%20Exceptions/Notes/001%20-%20Exceptions.md#why-a-bare-except-is-wrong)

---

## Modules and the Standard Library

Python ships with a large standard library, which is why it's described as "batteries included." Nothing in it needs a `pip install`; an `import` is enough. These are the modules the notes cover:

| Module | Used for |
|---|---|
| `json` | Reading and writing JSON |
| `datetime` | Dates and times: parsing, formatting, and date arithmetic |
| `collections` | Specialized containers: `Counter`, `defaultdict`, `namedtuple` |
| `os`, `sys` | Environment variables, command-line arguments, exiting the program |
| `random` | Random numbers and random choices |
| `math` | Numeric functions and constants, such as square roots, floor and ceiling, and `pi` |

The notes don't list every function in each module. Their "Reading the Docs" section shows how to find the rest.

How we import a module decides which name we use afterward:

| Statement | How we call it |
|---|---|
| `import math` | `math.sqrt(16)` |
| `from math import sqrt` | `sqrt(16)` |
| `import math as m` | `m.sqrt(16)` |

**Check yourself**
- After `from math import sqrt`, why does `math.pi` raise a `NameError`?
- Which standard-library module would we import to (a) parse a JSON string, (b) roll a die, and (c) calculate a factorial?
- Why do Python projects need fewer third-party dependencies for everyday jobs like JSON than Java projects do?

**Go deeper**
- The Standard Library: [Batteries Included](../Python/003%20-%20Objects%20and%20Structure/005%20-%20The%20Standard%20Library/Notes/001%20-%20The%20Standard%20Library.md#batteries-included), [random and math](../Python/003%20-%20Objects%20and%20Structure/005%20-%20The%20Standard%20Library/Notes/001%20-%20The%20Standard%20Library.md#random-and-math), [Reading the Docs](../Python/003%20-%20Objects%20and%20Structure/005%20-%20The%20Standard%20Library/Notes/001%20-%20The%20Standard%20Library.md#reading-the-docs)
- Anatomy of a Python Program: [Import Forms and the Names They Bind](../Python/001%20-%20Setup%20and%20Syntax/003%20-%20Anatomy%20of%20a%20Python%20Program/Notes/001%20-%20Anatomy%20of%20a%20Python%20Program.md#import-forms-and-the-names-they-bind)
- Python documentation: [the `math` module](https://docs.python.org/3/library/math.html), which lists every function it provides

---

## Flask

> **Not yet in the course notes.** This summary covers the idea until a notes file does. The links below point to the closest related notes and to Flask's own documentation.

**Flask** is a popular third-party Python **micro-framework for building web applications**, including REST APIs. "Micro" means its core is deliberately small. It maps URLs to Python functions and handles requests and responses. Everything else, such as database access, forms, and authentication, comes from extensions we choose to add.

Flask isn't part of the standard library. We install it from PyPI with `pip`, ideally inside a virtual environment, and record it in `requirements.txt`. The pip notes cover that process. When a Flask app runs inside a Docker container, it has to listen on `0.0.0.0` to be reachable, as the Docker notes explain.

**Check yourself**
- Flask isn't in the standard library. How do we add it to a project, and where do we record that dependency?
- What does "micro" in "micro-framework" tell us about what Flask includes out of the box?

**Go deeper**
- Virtual Environments, PyPI, and pip: [Installing Packages with pip](../Python/001%20-%20Setup%20and%20Syntax/002%20-%20Virtual%20Environments,%20PyPI,%20and%20pip/Notes/001%20-%20Virtual%20Environments,%20PyPI,%20and%20pip.md#installing-packages-with-pip), [requirements.txt](../Python/001%20-%20Setup%20and%20Syntax/002%20-%20Virtual%20Environments,%20PyPI,%20and%20pip/Notes/001%20-%20Virtual%20Environments,%20PyPI,%20and%20pip.md#requirementstxt)
- Docker, Running Containers: [The Application Must Listen on 0.0.0.0](../Docker/001%20-%20Docker%20Fundamentals/004%20-%20Running%20Containers/Notes/001%20-%20Running%20Containers.md#the-application-must-listen-on-0000)
- Flask documentation: [flask.palletsprojects.com](https://flask.palletsprojects.com/)
