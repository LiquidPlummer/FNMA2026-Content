# Exceptions

Python's `try`/`except` works much like Java's `try`/`catch`, and the exception hierarchy will feel familiar. The differences are in posture. There are no checked exceptions, so the compiler never reminds us what might fail, and Python code uses exceptions for ordinary control flow far more freely than Java style allows — to the point that every `for` loop ends by catching one.

---

## `try` and `except`

Code that might fail goes in a `try` block, and an `except` clause names the exception type to handle:

```python
def parse_quantity(text):
    try:
        return int(text)
    except ValueError:
        print(f"Not a whole number: {text!r}")
        return 0

print(parse_quantity("12"))     # 12
print(parse_quantity("twelve")) # Not a whole number: 'twelve', then 0
```

*`int()` raises `ValueError` for text it can't convert; the `except` clause catches it and returns a fallback.*

**`as`** binds the exception object to a name, so we can inspect its message:

```python
try:
    int("twelve")
except ValueError as error:
    print(error)    # invalid literal for int() with base 10: 'twelve'
```

*Binding the caught exception to `error` and printing its message.*

A `try` can have several `except` clauses, and one clause can catch several types by listing them in a tuple:

```python
try:
    unit_price = prices[product] / quantity
except KeyError:
    print(f"Unknown product: {product}")
except ZeroDivisionError:
    print("Quantity cannot be zero")
except (TypeError, ValueError) as error:
    print(f"Bad input: {error}")
```

*Separate handling for a missing key and a zero divisor, plus one clause covering two related error types.*

---

## `else` and `finally`

Two more optional clauses complete the statement, always in this order: `try`, `except`, `else`, `finally`.

```python
try:
    amount = int(user_input)
except ValueError:
    print("Please enter a whole number")
else:
    print(f"Charging ${amount}")     # runs only if the try block raised nothing
finally:
    print("Request handled")         # runs no matter what
```

*`else` holds the success path; `finally` holds cleanup that must always happen.*

- **`else`** runs only when the `try` block completed without an exception. Its purpose is to keep the `try` block as small as possible. If the charging code sat inside `try` and raised its own `ValueError`, it would be misreported as bad input.
- **`finally`** runs whether the `try` succeeded, an exception was caught, an exception escaped uncaught, or the function returned early. Java developers know it already; for files, `with` usually replaces it.

---

## The Exception Hierarchy

Exceptions are classes arranged in an inheritance tree. An `except` clause catches the named class **and all of its subclasses**. The part of the tree we'll meet most often:

```text
BaseException
├── SystemExit
├── KeyboardInterrupt
└── Exception
    ├── ArithmeticError
    │   └── ZeroDivisionError
    ├── LookupError
    │   ├── IndexError
    │   └── KeyError
    ├── OSError
    │   └── FileNotFoundError
    ├── AttributeError
    ├── NameError
    ├── TypeError
    └── ValueError
```

*A slice of the built-in exception hierarchy. `except LookupError` would catch both `IndexError` and `KeyError`.*

**`Exception`** is the base for every ordinary error. `SystemExit` (raised by `sys.exit()`) and `KeyboardInterrupt` (raised by Ctrl+C) sit outside it deliberately, so that broad error handling doesn't stop a program from exiting.

`except` clauses are checked **top to bottom, and the first match wins**. A general clause placed before a specific one swallows everything:

```python
try:
    value = settings["timeout"]
except LookupError:
    print("lookup failed")
except KeyError:              # never runs — KeyError is a LookupError
    print("missing setting")
```

*The `KeyError` clause is unreachable, because the `LookupError` clause above it already matches.*

**Contrast:** Java refuses to compile a `catch` block that an earlier one makes unreachable. Python accepts it silently, so specific clauses must be listed first.

### Reading a Traceback

An uncaught exception prints a **traceback**. It lists the chain of calls with the most recent **last**, so we read it from the bottom up:

```text
Traceback (most recent call last):
  File "orders.py", line 12, in <module>
    total = order_total(order)
  File "orders.py", line 5, in order_total
    return order["price"] * order["qty"]
KeyError: 'qty'
```

*The final line names the exception and its message; the frame directly above it shows the exact line that raised it.*

---

## Raising Exceptions

**`raise`** signals an error, usually with a message describing what went wrong:

```python
def withdraw(balance, amount):
    if amount <= 0:
        raise ValueError(f"amount must be positive, got {amount}")
    if amount > balance:
        raise ValueError(f"cannot withdraw {amount}; balance is {balance}")
    return balance - amount
```

*Rejecting invalid arguments by raising `ValueError` with a specific message.*

A **bare `raise`** inside an `except` block re-raises the exception currently being handled — useful when we want to act on an error without actually handling it:

```python
try:
    process(order)
except KeyError:
    print(f"order {order['id']} is missing a field")
    raise
```

*Logging the failure, then letting the original exception continue up the call stack unchanged.*

### `raise ... from ...`

When catching one exception and raising a different, more meaningful one, **`from`** records the original as the cause:

```python
def load_port(settings):
    try:
        return int(settings["port"])
    except (KeyError, ValueError) as error:
        raise ConfigError("port setting is missing or invalid") from error
```

*Translating a low-level `KeyError` or `ValueError` into a domain-specific `ConfigError`, while keeping the original attached.*

The traceback then shows both exceptions, joined by *"The above exception was the direct cause of the following exception."* Raising inside an `except` block without `from` still shows both, but with the vaguer *"During handling of the above exception, another exception occurred"* — which reads like a second, accidental failure. `from` states that the translation was deliberate.

**Contrast:** this is the equivalent of Java's `throw new ConfigException("...", e)`, which wraps the cause in the constructor.

---

## Custom Exception Classes

A custom exception is a class that inherits from `Exception`. Often the class needs no body beyond a docstring:

```python
class BankError(Exception):
    """Base class for errors raised by the bank module."""

class InsufficientFundsError(BankError):
    def __init__(self, balance, amount):
        super().__init__(f"cannot withdraw {amount}; balance is {balance}")
        self.balance = balance
        self.amount = amount

try:
    raise InsufficientFundsError(balance=40, amount=100)
except BankError as error:
    print(error)            # cannot withdraw 100; balance is 40
    print(error.amount)     # 100
```

*A module-level base exception and a specific subclass that carries extra data as attributes.*

- Names end in **`Error`** by convention.
- Passing the message to `super().__init__()` is what makes `print(error)` show it.
- A shared base class like `BankError` lets callers catch everything the module raises with one clause, or handle specific subclasses individually.

---

## Why a Bare `except:` Is Wrong

An `except` with no type catches **everything**:

```python
try:
    total = calculate_totl(order)   # typo in the function name
except:
    total = 0
```

*The typo raises `NameError`, which the bare `except` swallows. The program carries on with a silently wrong total.*

A bare `except:` catches programming mistakes like `NameError` and `AttributeError`, hiding bugs that should crash loudly. It also catches `KeyboardInterrupt` and `SystemExit`, so the program may refuse to stop. `except Exception:` avoids the last two problems but still hides bugs.

The rules:

- **Catch the narrowest type** that describes the failure we actually expect.
- **Keep the `try` block small**, ideally around the single line that can raise it.
- **Only catch what we can meaningfully handle.** Otherwise, let it propagate.

---

## EAFP vs LBYL

There are two ways to deal with something that might not work:

- **LBYL** — "Look Before You Leap": check that the operation is safe, then do it.
- **EAFP** — "Easier to Ask Forgiveness than Permission": do it, and handle the exception if it fails.

```python
# LBYL
if "port" in settings and settings["port"].isdigit():
    port = int(settings["port"])
else:
    port = 5432

# EAFP
try:
    port = int(settings["port"])
except (KeyError, ValueError):
    port = 5432
```

*The same fallback logic written both ways. The EAFP version doesn't have to anticipate every way the value could be bad.*

**EAFP is Python's default posture**, for three reasons:

- **The check and the action can't drift apart.** LBYL checks have to predict every failure mode — here, `isdigit()` rejects a negative number that `int()` would accept.
- **It avoids race conditions.** Checking `path.exists()` and then opening the file leaves a gap in which another process can delete it. Opening directly and catching `FileNotFoundError` has no gap.
- **It's often simpler**, because the happy path reads straight through.

LBYL is still the better choice when the check is cheap and obvious, or when failing partway through would leave things in a half-finished state.

---

## Exceptions as Control Flow

Python treats exceptions as a normal way to signal outcomes, not only as signs of disaster:

- Every `for` loop ends when the iterator raises `StopIteration`, which the loop catches for us.
- `int("abc")` raises rather than offering a `tryParse` that returns a flag.
- `dict[key]` raises `KeyError` for a missing key, and catching it is an accepted idiom.

**Contrast:** conventional Java style reserves exceptions for exceptional situations and warns against using them for flow control. Python code uses them freely for expected cases, and exceptions are cheap enough in Python that this is normal practice.

---

**Contrast:** Python has **no checked exceptions and no `throws` clause**. A function signature says nothing about what it might raise, so nothing warns us that a call to `open()` or `int()` can fail — that belongs in the docstring, and anticipating it is on us. JavaScript and TypeScript developers are used to that part, but a JS `catch` receives every error in a single clause with no type filter. Python catches by type, with as many clauses as we need.

---

## Key Takeaways

- `try`/`except` catches exceptions by type; `as` binds the exception object. A tuple of types catches several at once.
- `else` runs only when the `try` block succeeded, keeping the `try` narrow; `finally` always runs.
- Exceptions form a class hierarchy. An `except` catches subclasses too, clauses are checked top to bottom, and the first match wins — so list specific types first.
- Read tracebacks from the bottom up.
- `raise` signals an error; a bare `raise` re-raises the current one; `raise New(...) from error` translates an exception while preserving its cause.
- Custom exceptions subclass `Exception`, end in `Error`, and often share a module-level base class.
- Never use a bare `except:` — it hides bugs and blocks Ctrl+C. Catch narrowly and keep `try` blocks small.
- EAFP — try it and handle failure — is the idiomatic default over LBYL checks.
- There are no checked exceptions or `throws` clauses; nothing warns us what a call can raise.
