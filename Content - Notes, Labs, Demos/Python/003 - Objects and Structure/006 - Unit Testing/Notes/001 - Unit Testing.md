# Unit Testing

Python has two main ways to write tests. The built-in `unittest` module is modeled on JUnit, with test classes and `assertEqual` methods. **pytest**, the de facto standard in the wider Python community, strips nearly all of that away: tests are plain functions, assertions use the plain `assert` statement, and the runner finds everything by naming convention. These notes use pytest.

---

## Installing pytest

pytest is a third-party package, so it's installed into the project's virtual environment like any other:

```bash
python -m venv .venv
.venv\Scripts\activate          # Windows; macOS/Linux: source .venv/bin/activate
pip install pytest
```

*Creating and activating a virtual environment, then installing pytest into it.*

Because it's a development dependency, it belongs in `requirements.txt` alongside everything else the project needs.

`unittest` needs no install and remains perfectly usable — pytest can even run existing `unittest` tests. Its class-based style will simply look more familiar to Java developers than it does idiomatic to Python ones.

---

## Test Discovery and Naming

pytest finds tests by **name**, so no configuration or registration is needed. Starting from the current directory, it collects:

- **Files** named `test_*.py` or `*_test.py`, searched recursively through subdirectories.
- **Functions** in those files whose names start with `test`.
- **Classes** whose names start with `Test` (and have no `__init__`), when we choose to group tests that way — classes are optional.

A typical small project keeps each test file next to, or in a folder alongside, the code it tests:

```text
pricing/
├── pricing.py
└── test_pricing.py
```

*A module and its test file. The `test_` prefix is what makes pytest collect it.*

```python
# pricing.py
def apply_discount(price, percent):
    if not 0 <= percent <= 100:
        raise ValueError("percent must be between 0 and 100")
    return round(price * (1 - percent / 100), 2)
```

*The module under test: a function that applies a percentage discount and rejects out-of-range percentages.*

```python
# test_pricing.py
from pricing import apply_discount

def test_ten_percent_off():
    assert apply_discount(50.0, 10) == 45.0

def test_zero_percent_changes_nothing():
    assert apply_discount(19.99, 0) == 19.99
```

*Two tests: ordinary functions with `test_` names, importing the module under test like any other code.*

Test names should describe the behavior being checked, since they appear in the output when a test fails. A long, specific name like `test_zero_percent_changes_nothing` is normal.

**Contrast:** JUnit tests need a `@Test` annotation on each method, inside a test class. Jest also discovers test files by naming pattern (such as `*.test.ts`), but registers each test through a `test()` or `it()` call. pytest needs neither: a function named `test_...` in a file named `test_...` is a test.

---

## Plain `assert`

`assert` is a built-in Python statement: `assert condition` does nothing if the condition is truthy, and raises `AssertionError` if it isn't. pytest uses it as its only assertion mechanism:

```python
def test_assertion_forms():
    skills = ["python", "sql"]
    result = {"status": "ok", "count": 2}

    assert len(skills) == 2
    assert "sql" in skills
    assert "java" not in skills
    assert result["status"] == "ok"
    assert result == {"status": "ok", "count": 2}
```

*Equality, membership, and whole-dict comparison, all written as ordinary Python expressions.*

The clever part is what happens on failure. pytest rewrites `assert` statements in test files so it can report the values involved:

```python
def test_ten_percent_off():
    assert apply_discount(50.0, 10) == 40.0
```

*A deliberately wrong expectation.*

```text
    def test_ten_percent_off():
>       assert apply_discount(50.0, 10) == 40.0
E       assert 45.0 == 40.0
E        +  where 45.0 = apply_discount(50.0, 10)
```

*pytest's failure report shows the actual value, the expected value, and the call that produced it.*

For lists and dicts, the failure report goes further and highlights exactly which items differ.

Because floating-point arithmetic is inexact, comparing computed floats with `==` can fail on rounding. `pytest.approx` compares within a small tolerance: `assert 0.1 + 0.2 == pytest.approx(0.3)`.

**Contrast:** JUnit has a family of methods — `assertEquals`, `assertTrue`, `assertNotNull`, `assertIterableEquals` — and a failure message is whatever that method was given. pytest has one statement, and the failure message is **reconstructed** from the expression itself.

---

## Arranging Shared Setup

When several tests need the same starting objects, the simplest option is a helper function each test calls. pytest's own tool for this is a **fixture**: a function marked with `@pytest.fixture`, whose return value is passed to any test that names it as a parameter.

```python
# cart.py
class Cart:
    def __init__(self):
        self.items = {}

    def add(self, name, price):
        self.items[name] = price

    def total(self):
        return sum(self.items.values())

    def __len__(self):
        return len(self.items)
```

*A small class to test.*

```python
# test_cart.py
import pytest
from cart import Cart

@pytest.fixture
def cart():
    c = Cart()
    c.add("lamp", 40)
    c.add("bulb", 5)
    return c

def test_item_count(cart):
    assert len(cart) == 2

def test_total(cart):
    assert cart.total() == 45

def test_adding_an_item(cart):
    cart.add("shade", 15)
    assert len(cart) == 3
```

*A fixture builds a two-item cart, and each test receives it by declaring a parameter named `cart`.*

pytest matches the parameter name to the fixture name and calls the fixture **fresh for every test**. `test_adding_an_item` modifies its cart, but no other test sees that change, so tests can run in any order.

**Contrast:** a fixture fills the role of JUnit's `@BeforeEach` or Jest's `beforeEach`. Instead of assigning to a shared field that every test in a class reads, each test asks for exactly the objects it needs.

---

## Asserting That Something Raises

**`pytest.raises`** is a context manager — a `with` block — that passes only if the code inside raises the expected exception:

```python
import pytest
from pricing import apply_discount

def test_rejects_discount_over_100():
    with pytest.raises(ValueError):
        apply_discount(50.0, 150)

def test_error_message_names_the_range():
    with pytest.raises(ValueError, match="between 0 and 100"):
        apply_discount(50.0, -5)
```

*Checking that an invalid percentage raises `ValueError`, and that its message contains the expected text.*

- If the block raises the expected type, the exception is caught and the test passes.
- If it raises nothing, the test fails with `DID NOT RAISE ValueError`.
- If it raises a different type, that exception propagates and the test fails.
- **`match`** is a regular expression searched for in the exception's message.

Adding `as error_info` gives access to the caught exception afterward, through `error_info.value`, for checking custom attributes.

**Contrast:** this replaces JUnit's `assertThrows(ValueError.class, () -> ...)` and Jest's `expect(() => ...).toThrow()`. The `with` block holds the code directly, so no lambda is needed.

---

## Running the Suite

From the project root, with the virtual environment active:

```bash
pytest                                      # run every test pytest can find
pytest test_pricing.py                      # run one file
pytest test_pricing.py::test_ten_percent_off   # run one test
pytest -v                                   # verbose: list every test by name
pytest -x                                   # stop at the first failure
pytest -k discount                          # run tests whose names contain "discount"
python -m pytest                            # run pytest through the active interpreter
```

*Common ways to run all, some, or one of the tests.*

Default output prints one character per test as it runs — `.` for a pass, `F` for a failure — followed by a detailed report for each failure and a one-line summary:

```text
test_cart.py ...                                                  [ 60%]
test_pricing.py .F                                                [100%]

=================================== FAILURES ===================================
...
========================= 1 failed, 4 passed in 0.03s ==========================
```

*Five collected tests, one failure, with the failure detail shown between the progress line and the summary.*

`python -m pytest` behaves like `pytest`, with one useful difference: `-m` adds the current directory to `sys.path`, exactly as described in the Modules and Packages lesson. When a test fails to import the module it's testing with `ModuleNotFoundError`, running through `python -m pytest` from the project root usually fixes it.

pytest exits with status `0` when every test passes and a non-zero status otherwise, which is what build scripts and CI systems check.

---

**Contrast:** there are no annotations and no runner class. JUnit needs `@Test`, often `@BeforeEach`, and a runner or engine configured through the build tool. pytest discovers files and functions by name, injects setup by parameter name, and turns plain `assert` into detailed failure reports.

---

## Key Takeaways

- pytest is the de facto standard test runner; install it into the virtual environment. `unittest` ships with Python but uses JUnit-style classes.
- Test discovery is by name: files named `test_*.py` or `*_test.py`, functions whose names start with `test`.
- Plain `assert` is the only assertion mechanism; pytest reconstructs failure messages from the expression. Use `pytest.approx` for floats.
- Fixtures, marked with `@pytest.fixture`, provide shared setup. Tests request them by parameter name, and each test gets a fresh one.
- `with pytest.raises(SomeError, match="..."):` asserts that code raises an exception with a matching message.
- Run with `pytest`, narrow with a file, `::test_name`, or `-k`, and use `python -m pytest` to fix import-path problems.
- There's no `@Test`, no `assertEquals` family, and no runner class — naming conventions do the work.
