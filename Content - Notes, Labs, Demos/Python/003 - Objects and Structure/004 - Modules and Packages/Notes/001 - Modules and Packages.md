# Modules and Packages

Unit 001 established that a `.py` file is a module and that importing it runs it. This lesson scales that up: how modules are grouped into **packages**, how Python finds what we import, and why imports behave differently depending on where a program is launched from. Java developers should expect less structure to declare and more to understand, because in Python the file system *is* the package system.

---

## A Module Is a File; a Package Is a Directory

A **package** is a directory of modules, marked by an `__init__.py` file. Packages can contain other packages:

```text
shop/                       ← project root; we run commands from here
├── main.py
└── inventory/              ← package
    ├── __init__.py
    ├── models.py           ← module: inventory.models
    ├── pricing.py          ← module: inventory.pricing
    └── reports/            ← subpackage
        ├── __init__.py
        └── daily.py        ← module: inventory.reports.daily
```

*A project with one package containing two modules and a subpackage. Dotted module names mirror the directory path.*

Every module has a dotted name built from its location: `inventory/reports/daily.py` is `inventory.reports.daily`. Inside that module, `__name__` holds that dotted string — unless it's being run directly, when it's `"__main__"`.

**Contrast:** a Java source file begins with `package com.shop.inventory;`, and the declaration must agree with the directory. Python has no `package` statement. A module's location on disk is the only thing that defines its package.

---

## Importing from Packages

The import forms from unit 001 all work with dotted names:

```python
import inventory.models
product = inventory.models.Product("Lamp", 39.99)

from inventory import models
product = models.Product("Lamp", 39.99)

from inventory.models import Product
product = Product("Lamp", 39.99)

import inventory.reports.daily as daily
daily.run()
```

*Four ways to reach the same module or class, each binding a different name in the importing file.*

`import inventory.models` binds only the top-level name `inventory`, so everything must be spelled out in full. `from ... import` binds the final name directly. Most code uses `from package import module` or `from package.module import Name`, keeping call sites short while still showing where names came from.

---

## How Imports Resolve

When Python executes `import inventory.models`, it works through these steps:

1. **Check the cache.** Every module imported so far is stored in the dict `sys.modules`. If the module is already there, Python reuses it — this is why a module's top-level code runs only once.
2. **Search `sys.path`.** Otherwise, Python walks the directories listed in `sys.path`, in order, looking for `inventory/` or `inventory.py`. The first match wins.
3. **Execute and cache.** Python runs the module's code, stores the module object in `sys.modules`, and binds the requested name.

If no directory on `sys.path` contains a match, the import fails with `ModuleNotFoundError`.

### `sys.path`

`sys.path` is an ordinary list of directory paths, built at startup:

```python
import sys

for entry in sys.path:
    print(entry)
```

*Printing the directories Python searches for imports, in search order.*

Its entries, in order, are roughly:

1. **The directory of the script being run** — or the current directory, when using the REPL or `python -m`.
2. Any directories listed in the `PYTHONPATH` environment variable.
3. **The standard library.**
4. **`site-packages`** — the active virtual environment's installed packages.

Two consequences follow:

- **The entry point determines what's importable.** Running `python main.py` from `shop/` puts `shop/` on the path, so `import inventory` works everywhere in the program. Running `python inventory/reports/daily.py` puts `inventory/reports/` on the path instead, and `import inventory` inside it fails.
- **Local files shadow installed ones.** The script's directory is searched before the standard library and `site-packages`, which is why a file named `random.py` or `json.py` breaks imports of the real module.

**Contrast:** a TypeScript import like `./models` is resolved relative to the importing file. A Python absolute import like `inventory.models` is resolved against `sys.path`, which depends on how the program was launched — not on where the importing file sits.

---

## `__init__.py`

An **`__init__.py`** file marks a directory as a regular package, and it runs when the package is first imported — before any of its modules. It's often empty.

It can also shape the package's public interface by importing names from its modules:

```python
# inventory/__init__.py
from inventory.models import Product
from inventory.pricing import apply_discount
```

*Re-exporting the package's most-used names from its `__init__.py`.*

```python
# main.py
from inventory import Product, apply_discount
```

*Callers can now import directly from the package, without knowing which module defines each name.*

Keep `__init__.py` light. Since it runs on every import of the package or any module inside it, expensive work there slows down everything.

Python can also import a directory that has no `__init__.py`, treating it as a **namespace package**. That feature exists for splitting one package across several locations; for ordinary projects, including `__init__.py` avoids surprises.

---

## Absolute vs Relative Imports

An **absolute import** names the full path from a directory on `sys.path`:

```python
# inventory/reports/daily.py
from inventory.models import Product
from inventory.pricing import apply_discount
```

*Absolute imports spell out the complete dotted path.*

A **relative import** starts with dots, and is resolved relative to the current module's package:

```python
# inventory/reports/daily.py
from ..models import Product        # .. = the parent package, inventory
from ..pricing import apply_discount
from . import formatting            # .  = this package, inventory.reports
```

*One dot means the current package; each additional dot moves up one level.*

- Relative imports only work inside a package, and only in modules that were **imported as part of that package**.
- Only the `from ... import` form can be relative. `import .models` is a syntax error.
- PEP 8 recommends absolute imports as the default, since they're clearer about where names come from. Relative imports are acceptable inside large packages, to avoid repeating a long package name.

Running a file that contains relative imports directly fails:

```text
$ python inventory/reports/daily.py
ImportError: attempted relative import with no known parent package
```

*A file run as a script is `__main__`, not part of a package, so there's nothing for the dots to be relative to.*

---

## `if __name__ == "__main__":` with Packages

The main guard from unit 001 works the same inside a package, but launching the file needs care. Running a module by its **file path** breaks both relative imports and absolute imports of its own package, as shown above. Running it by its **module name** with **`-m`**, from the project root, works:

```text
$ cd shop
$ python -m inventory.reports.daily
```

*`-m` runs a module by dotted name. The current directory goes on `sys.path`, the package is imported normally, and the module itself runs with `__name__` set to `"__main__"`.*

With `-m`, the module keeps its place in the package, so every import form works, and its main guard still fires. This is the same `-m` behind `python -m venv` and `python -m pip`: both run a module from the standard library or `site-packages` by name.

A package can also contain a **`__main__.py`** file, which runs when the package itself is executed: `python -m inventory`.

---

## Circular Imports

A **circular import** happens when two modules import each other, directly or through a chain:

```python
# store/orders.py
from store.customers import Customer

class Order:
    def __init__(self, customer):
        self.customer = customer
```

```python
# store/customers.py
from store.orders import Order

class Customer:
    def new_order(self):
        return Order(self)
```

*Two modules in a `store` package, each importing a name from the other at the top of the file.*

```text
$ python main.py
ImportError: cannot import name 'Order' from partially initialized module 'store.orders' (most likely due to a circular import)
```

*`main.py` contains `import store.orders`, and the import fails before either class is defined. The full message also ends with the module's file path.*

Tracing it through shows why:

1. Importing `store.orders` starts running `orders.py`, which immediately imports `store.customers`.
2. `customers.py` starts running, and immediately asks for `Order` from `store.orders`.
3. `store.orders` is already in `sys.modules` — but only its first line has run, so `Order` doesn't exist yet. The chain unwinds in failure.

When the two modules sit directly beside the script rather than in a package, recent Python versions may word the error differently, even suggesting the file is shadowing a library. The cause is the same cycle.

The fixes, from best to worst:

- **Restructure.** Move whatever both modules need into a third module that imports neither. A cycle usually signals that responsibilities are tangled.
- **Import the module, not the name.** `import store.orders`, with `store.orders.Order(self)` used inside the method, defers the lookup until the method runs, by which time both modules have finished loading.
- **Move the import into the function** that needs it. This works, but hides the dependency.

**Contrast:** Java classes can reference each other freely, because a Java import is a compile-time alias with no side effects. A Python import *executes* the other file, in order, right now — so a cycle means one module can observe another before it has finished running.

---

## Key Takeaways

- A module is a `.py` file; a package is a directory with an `__init__.py`. Dotted module names mirror the directory structure.
- There's no `package` declaration — location on disk is the package.
- Imports check the `sys.modules` cache, then search `sys.path` in order; the first match wins.
- `sys.path` starts with the entry script's directory, so how a program is launched determines what it can import, and local files can shadow the standard library.
- `__init__.py` runs on first import of its package; it can re-export names, and it should stay light.
- Absolute imports name the full dotted path and are the PEP 8 default. Relative imports use leading dots and only work inside an imported package.
- Run modules inside a package with `python -m package.module` from the project root, not by file path.
- Circular imports fail because importing executes code in order. Restructure first; importing the module instead of the name is the usual fallback.
