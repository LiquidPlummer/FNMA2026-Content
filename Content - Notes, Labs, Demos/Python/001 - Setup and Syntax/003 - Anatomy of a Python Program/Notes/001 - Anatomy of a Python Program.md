# Anatomy of a Python Program

A Java program has a required skeleton: a class, and inside it a `public static void main(String[] args)` the launcher looks for. A Python file has no required skeleton at all. That raises two questions a Java developer asks immediately — where does execution start, and what actually happens when one file imports another? The answers explain why nearly every Python script ends with the same odd-looking `if` statement.

---

## A File Is a Module

Every `.py` file is a **module**, and the module's name is the filename without the extension: `helpers.py` is the module `helpers`.

Any module can be used two ways:

- **Run directly:** `python helpers.py`
- **Imported by another file:** `import helpers`

Either way, Python executes the file from top to bottom. Running and importing are the same act; the only difference is which file serves as the program's entry point.

---

## No `main` Method, No Class Wrapper

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, world");
    }
}
```

*The traditional minimum runnable Java program: a class wrapping a `main` method with a fixed signature.*

```python
print("Hello, world")
```

*The minimum runnable Python program: one statement at the top level of a file.*

Statements that aren't indented inside a function or class are **top-level code**, and they run as soon as the interpreter reaches them. Execution starts at the first line of the file we run and proceeds downward. There's no method the interpreter searches for.

Even definitions are statements executed in order. A function doesn't exist until the interpreter reaches the line defining it, so calling a function *above* its definition fails.

Recent Java versions have relaxed the ceremony with a shorter launch form, but there is still a `main` method the launcher goes looking for. Python has nothing equivalent.

---

## Importing Runs the File

When a module is imported for the first time, Python executes it top to bottom, builds a module object from whatever names it defined, and caches that object. Every later import of the same module, from anywhere in the program, reuses the cached object without running the file again.

```python
# settings.py
print("Loading settings...")
TIMEOUT = 30
```

*A module with one side effect (the `print`) and one module-level name (`TIMEOUT`).*

```python
# app.py
import settings
import settings     # already loaded — nothing runs this time

print("Timeout is", settings.TIMEOUT)
```

*A script that imports `settings` twice and reads a name from it.*

```text
$ python app.py
Loading settings...
Timeout is 30
```

*The output: `settings.py` ran exactly once, on the first import.*

This has real consequences, not just trivia value:

- **Expensive module-level work happens at import time.** If a module opens a database connection or reads a large file at the top level, that cost is paid the moment anything imports it.
- **Stray top-level code fires in every importer.** A test `print` or a leftover function call at module level runs for every file that imports the module.
- **Module-level names are shared.** Since the module runs once and is cached, every importer sees the same objects.

One practical trap follows from how imports are found: Python searches the running script's own directory first. A file we name `random.py` or `math.py` will shadow the standard library module of the same name.

### Contrast with Java and TypeScript

A **Java** `import` runs nothing. It's a compile-time shortcut that lets us write `List` instead of `java.util.List`, and it vanishes from the compiled class.

**TypeScript** developers will find Python's behavior more familiar: ES modules also execute their top-level code once, on first import. The habit to unlearn is the Java one — in Python, an import statement is an instruction to execute a file.

---

## `__name__`

Before running a module, Python sets a built-in variable in it called **`__name__`**:

- When the file is **run directly**, `__name__` is the string `"__main__"`.
- When the file is **imported**, `__name__` is the module's name, such as `"greetings"`.

```python
# greetings.py
print("greetings.py sees __name__ as:", __name__)
```

*A module that reports its own `__name__`.*

```python
# main.py
import greetings

print("main.py sees __name__ as:", __name__)
```

*A script that imports `greetings`, then reports its own `__name__`.*

```text
$ python main.py
greetings.py sees __name__ as: greetings
main.py sees __name__ as: __main__

$ python greetings.py
greetings.py sees __name__ as: __main__
```

*The same `greetings.py` reports a different `__name__` depending on whether it was imported or run directly.*

Names wrapped in double underscores on both sides — "**dunder**" names — are reserved for meanings Python itself defines.

---

## `if __name__ == "__main__":`

Most modules are written to be imported for their definitions, but it's common to also want some code that runs only when the file is launched as a script. Without protection, that script code would also run every time another module imports the file. The **main guard** solves this:

```python
# temperature.py
FREEZING_F = 32

def to_celsius(fahrenheit):
    return (fahrenheit - FREEZING_F) * 5 / 9

if __name__ == "__main__":
    print(to_celsius(212))
```

*Running `python temperature.py` prints `100.0`. Another module can `import temperature` and call `to_celsius` without anything printing.*

It's worth being precise about what the guard protects:

- **Definitions above the guard run either way.** `FREEZING_F` and `to_celsius` are created on import and on direct execution alike.
- **Only the indented block is guarded**, and it runs only when this file is the entry point.
- **Nothing about it is special syntax.** It's an ordinary `if` comparing a string. The interpreter doesn't look for it.

A common arrangement puts the script logic in a function and keeps the guard to one line:

```python
def main():
    print("Running the report...")

if __name__ == "__main__":
    main()
```

*A conventional script layout. The function named `main` is just a name we chose; the guard is what makes it run.*

**Contrast:** Java's `main` method is required ceremony — without it, there's nothing to launch. Python's guard is a convention we opt into, and a file without one is still a perfectly runnable program.

---

## Import Forms and the Names They Bind

Every form of `import` executes (or reuses) the entire module. What differs is which **name** gets bound in the importing file:

| Statement | Name created in our module | How we use it |
|---|---|---|
| `import math` | `math` | `math.sqrt(16)` |
| `from math import sqrt` | `sqrt` | `sqrt(16)` |
| `import math as m` | `m` | `m.sqrt(16)` |
| `from math import sqrt as root` | `root` | `root(16)` |

The table's middle column is the important one. `from math import sqrt` binds only `sqrt` — the name `math` doesn't exist afterward:

```python
from math import sqrt

print(sqrt(16))     # 4.0
print(math.pi)      # NameError: name 'math' is not defined
```

*`from ... import` binds the imported name, not the module name, so `math` is undefined here.*

Aliasing with `as` is common for long module names, and some libraries have community-standard aliases (`import numpy as np`, for example). There's also `from math import *`, which dumps every public name into our module; it makes it impossible to tell where a name came from, so it's best avoided.

**Contrast:** a Java import only shortens how we refer to a class. A Python import creates a variable in our module, bound to a module object or to something inside one.

---

## `print()`

`print` accepts any number of arguments, converts each to text, separates them with spaces, and ends with a newline. Two named options adjust that:

```python
print("Total:", 42, True)          # Total: 42 True
print("a", "b", "c", sep="-")      # a-b-c
print("Loading", end="")           # no newline at the end
print("...done")                   # continues the same line: Loading...done
```

*`print` with multiple arguments, a custom separator, and a suppressed newline.*

Unlike Java, building output with `+` doesn't auto-convert numbers: `"Total: " + 42` is a `TypeError` in Python. Passing values as separate arguments to `print`, or using an f-string, avoids the issue.

---

## f-strings

An **f-string** is a string literal prefixed with `f`, where anything in `{braces}` is evaluated and inserted:

```python
name = "Ada"
items = 3
price = 4.5

print(f"{name} bought {items} items")     # Ada bought 3 items
print(f"Total: {items * price}")          # Total: 13.5
print(f"Price: ${price:.2f}")             # Price: $4.50
print(f"{items=}")                        # items=3
```

*f-strings inserting a variable, an expression, a value formatted to two decimal places, and a self-labeling debug value.*

- **Any expression** can go inside the braces, not just a variable name.
- **A format spec** follows a colon: `:.2f` means a float with two decimal places.
- **A trailing `=`** prints the expression text alongside its value, which is handy for quick debugging.

TypeScript developers will recognize template literals: `` `${name} bought ${items} items` ``. The f-string equivalent drops the backticks and the `$`, and adds the `f` prefix. For Java developers, it replaces `String.format` and string concatenation.

---

## Key Takeaways

- Every `.py` file is a module; running it and importing it both execute it top to bottom.
- There's no required `main` method or class — top-level code runs as the interpreter reaches it.
- A module's code runs once, on first import, and the result is cached; module-level side effects fire for every importer.
- `__name__` is `"__main__"` when a file is run directly and the module's name when it's imported.
- `if __name__ == "__main__":` guards only its own block, and it's an opt-in convention rather than required ceremony.
- `import x`, `from x import y`, and `as` differ only in which name they bind in our module.
- `print` takes multiple arguments with `sep` and `end` options; f-strings embed expressions and format specs directly in strings.
