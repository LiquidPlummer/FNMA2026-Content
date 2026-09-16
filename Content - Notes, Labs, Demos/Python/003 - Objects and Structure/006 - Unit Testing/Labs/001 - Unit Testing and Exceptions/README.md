# Unit Testing and Exceptions

We'll write a pytest suite for a small meeting-room booking module that runs, looks finished, and isn't: several of its functions are subtly wrong. The tests will pin down what the code is supposed to do, expose each bug, and then keep us safe while we fix them. Along the way we'll cover test file and function naming, plain `assert` for equality and membership, asserting that a call raises the module's custom `BookingError`, a fixture for shared setup, running the suite from the command line, `try`/`except`/`else`/`finally`, and an EAFP rewrite of an LBYL check. The exercise finishes with a function of our own, written together with its tests.

---

## Prerequisites

| Software | Required Version |
|---|---|
| Python | 3.11 or newer (current stable release: 3.14.7) |
| pytest | 9.1.1 — installed during setup from `requirements.txt` (requires Python ≥ 3.10) |

### Getting Started

1. Open a terminal in this lab's folder — the one containing this `README.md`. Every command in this lab runs from here.
2. Create and activate a virtual environment:

   ```text
   python -m venv .venv

   # macOS / Linux (bash, zsh)
   source .venv/bin/activate

   # Windows PowerShell
   .venv\Scripts\Activate.ps1

   # Windows Command Prompt
   .venv\Scripts\activate.bat
   ```

   *On macOS and Linux, use `python3` for the first command if `python` isn't available. Once the environment is active, `python` works everywhere.*

3. Install the pinned test runner:

   ```text
   python -m pip install -r requirements.txt
   ```

4. Confirm it's installed:

   ```text
   pytest --version
   ```

   Expected output:

   ```text
   pytest 9.1.1
   ```

5. Run the (empty) test suite:

   ```text
   pytest
   ```

   The last line of the output reads **`no tests ran`**. pytest searched the lab folder, found `src/test_booking.py`, and collected nothing from it, because the file holds only a comment.

### Project Layout

```text
001 - Unit Testing and Exceptions/
├── README.md
├── requirements.txt       # pins pytest
└── src/
    ├── booking.py         # the module under test: it runs, but not all of it is right
    └── test_booking.py    # empty for now; we'll write every test here
```

### The Module Under Test

`src/booking.py` manages bookings for three meeting rooms. Its pieces:

- **`ROOMS`** maps each room's name to how many people it holds, and **`HOURLY_RATE`** is the price per hour.
- **`BookingError`** is the module's own exception class. Every rule a booking breaks is reported by raising it.
- **`parse_time`** turns `"09:30"` into minutes after midnight.
- **`capacity_of`** looks up a room's size.
- **`overlaps`** decides whether two time ranges collide.
- **`book`** validates a request and adds it to a list of bookings.
- **`price`** calculates what a booking costs.
- **`process_requests`** books a batch of requests and reports the results.

We won't go hunting through the code for mistakes. Instead, we'll treat each function's **docstring as its contract** and write tests that check the code against it. When a test fails, the docstring decides whether the test or the code is wrong.

---

## Guided Walkthrough

All of our tests go in `src/test_booking.py`, added below its comment. After each section we'll run `pytest` from the lab folder and check the summary on the last line of its output. Timings in that line will differ, and on Windows, paths print with backslashes.

### 1. A First Test

We'll start with the imports every test in the file will need, followed by one test:

```python
import pytest

from booking import BookingError, book, capacity_of, parse_time, price, process_requests


def test_parse_time_converts_to_minutes():
    assert parse_time("09:30") == 570
    assert parse_time("00:00") == 0
```

*A test is a plain function whose name starts with `test`, in a file whose name starts with `test_`. Its checks are ordinary `assert` statements.*

Running `pytest` now:

```text
src/test_booking.py .                                                    [100%]

============================== 1 passed in 0.01s ==============================
```

*Each `.` is one passing test.*

pytest found the test purely by its name — there's no annotation and nothing to register. To see that, we'll rename the function to `check_parse_time_converts_to_minutes` and run `pytest` again: it's back to `no tests ran`. Then we'll change the name back.

`pytest -v` lists each test by name instead of printing dots, which is handy as the suite grows.

### 2. Asserting That a Call Raises

`parse_time`'s docstring says it raises `BookingError` for text that isn't a valid time. `pytest.raises` is a `with` block that passes only if the code inside raises the named exception:

```python
def test_parse_time_rejects_missing_colon():
    with pytest.raises(BookingError, match="invalid time"):
        parse_time("0930")


def test_parse_time_rejects_impossible_hour():
    with pytest.raises(BookingError, match="invalid time"):
        parse_time("25:00")
```

*`match` also checks the exception's message, so a `BookingError` raised for some other reason wouldn't pass.*

Expected summary: **`3 passed`**.

The two tests reach `BookingError` by different routes, and `parse_time` is worth reading to see both:

- **`"0930"`** has no colon, so `text.split(":")` returns a single item and the unpacking inside the `try` block raises `ValueError`. The `except ValueError as error:` clause catches it and raises `BookingError` **`from error`** instead, translating a low-level failure into the module's own exception while keeping the original attached as its cause.
- **`"25:00"`** parses fine, so no exception occurs. The range check after the `try` statement raises `BookingError` directly.

It's worth changing one test to expect `ValueError` instead of `BookingError`, just once. It fails — `BookingError` isn't a subclass of `ValueError`, so `pytest.raises(ValueError)` doesn't recognize it. Then we'll change it back.

### 3. Equality and an Unknown Room

`capacity_of` has one normal case and one failure case, so it gets one test of each kind:

```python
def test_capacity_of_known_room():
    assert capacity_of("boreal") == 4


def test_capacity_of_unknown_room():
    with pytest.raises(BookingError, match="unknown room"):
        capacity_of("dorado")
```

*There's no room called `"dorado"` in `ROOMS`.*

Expected summary: **`5 passed`**.

### 4. Shared Setup with a Fixture

Most of the remaining tests need a list of bookings to work against. Rather than build one inside every test, we'll write a **fixture** — a function marked with `@pytest.fixture` whose return value pytest hands to any test that names it as a parameter:

```python
@pytest.fixture
def bookings():
    return [{"id": 1, "room": "atlas", "people": 6, "start": 540, "end": 600}]
```

*One existing booking: six people in `atlas` from 09:00 (540 minutes after midnight) until 10:00 (600).*

Now three tests for `book` that use it:

```python
def test_book_returns_next_id(bookings):
    assert book(bookings, "cirrus", 10, "09:00", "10:00") == 2
    assert len(bookings) == 2


def test_book_adds_the_booking(bookings):
    book(bookings, "cirrus", 10, "09:00", "10:00")
    assert {"id": 2, "room": "cirrus", "people": 10, "start": 540, "end": 600} in bookings


def test_book_rejects_overlap(bookings):
    with pytest.raises(BookingError, match="already booked"):
        book(bookings, "atlas", 2, "09:30", "10:30")
```

*Each test declares a `bookings` parameter, and pytest matches it to the fixture by name. The second test uses `in` to check membership: the list must contain a dict equal to the one we expect.*

Expected summary: **`8 passed`**.

The first two tests each add a booking and expect it to get id `2`. That only works because pytest calls the fixture **fresh for every test**, so neither test sees the other's booking.

### 5. The First Bug

`book`'s docstring says to reject a group that's *bigger* than the room holds. A group that exactly fills the room should be accepted:

```python
def test_room_can_be_filled_exactly(bookings):
    assert book(bookings, "boreal", 4, "13:00", "14:00") == 2
```

*`boreal` holds 4 people, and 4 people want it at a time no one else has booked.*

Running `pytest`, the summary now reads **`1 failed, 8 passed`**. The failure report walks down into `booking.py` and ends here:

```text
>           raise BookingError(f"{room} holds {capacity} people, not {people}")
E           booking.BookingError: boreal holds 4 people, not 4
```

*The `>` marks the line that raised, and the `E` line shows the exception.*

The message itself is the giveaway: a room that holds 4 rejected 4 people. Checking the contract, the test is right and the code is wrong. In `booking.py`, inside `book`, we'll fix the comparison:

```python
    if people > capacity:
```

*Only a group strictly larger than the room's capacity is rejected.*

Expected summary: **`9 passed`**.

### 6. Touching Isn't Overlapping

`overlaps`'s docstring says that ranges which only touch — one ending exactly when the other begins — don't overlap. So a second meeting starting at 10:00 should be able to follow the fixture's 09:00–10:00 booking in the same room:

```python
def test_back_to_back_bookings_are_allowed(bookings):
    assert book(bookings, "atlas", 6, "10:00", "11:00") == 2
```

*The new booking starts at exactly the minute the existing one ends.*

The summary reads **`1 failed, 9 passed`**, and the report ends with:

```text
E               booking.BookingError: atlas is already booked at that time
```

This time the report points at the loop inside `book`, but that loop only *acts* on what `overlaps` tells it — the wrong answer comes from `overlaps` itself. Its comparisons use `<=`, so two ranges sharing a single boundary minute count as overlapping. In `booking.py`, we'll change `overlaps` to compare strictly:

```python
    return start < other_end and other_start < end
```

*Each range must start strictly before the other one ends.*

Expected summary: **`10 passed`**.

To run just one test while working on it, `pytest -k back_to_back` selects tests whose names contain that text.

### 7. Billing Started Hours

`price`'s docstring says every started hour is billed in full, so 90 minutes costs two hours. We'll test an exact hour and a partial one:

```python
def test_price_of_one_hour(bookings):
    assert price(bookings[0]) == 25


def test_started_hour_is_billed_in_full():
    assert price({"id": 1, "room": "atlas", "people": 2, "start": 540, "end": 630}) == 50
```

*The fixture's booking is exactly 60 minutes. The second test builds its own 90-minute booking, since it doesn't need the fixture.*

The summary reads **`1 failed, 11 passed`**. This failure comes from an `assert` rather than an exception, and pytest shows what each side evaluated to:

```text
E       AssertionError: assert 25 == 50
E        +  where 25 = price({'id': 1, 'room': 'atlas', 'people': 2, 'start': 540, ...})
```

*The actual value, the expected value, and the call that produced the actual value.*

`minutes // 60` rounds *down*, so 90 minutes becomes 1 hour. In `booking.py`, we'll replace the last line of `price` with two lines that round up:

```python
    hours = (minutes + 59) // 60
    return hours * HOURLY_RATE
```

*Adding 59 before floor division pushes any partial hour over into the next whole hour, while an exact hour like 60 minutes stays at 1.*

Expected summary: **`12 passed`**.

> **Contrast — Java:** JUnit writes this check as `assertEquals(50, price(booking))`, and a family of methods — `assertTrue`, `assertNotNull`, `assertThrows` — covers the other cases. pytest has only the plain `assert` statement, and it *reconstructs* the failure message from the expression itself, which is how it knew to show both 25 and the `price(...)` call that produced it.

### 8. `else` vs `finally`

`process_requests` promises that `"processed"` counts every request handled, *whether it was booked or not*. We'll send it one request that should succeed and one for a room that doesn't exist:

```python
def test_process_requests_counts_every_request(bookings):
    requests = [
        {"room": "boreal", "people": 3, "start": "11:00", "end": "12:00"},
        {"room": "dorado", "people": 3, "start": "11:00", "end": "12:00"},
    ]
    report = process_requests(bookings, requests)
    assert report["booked"] == [2]
    assert "unknown room: dorado" in report["rejected"]
    assert report["processed"] == 2
```

*Three asserts on one report: an equality check on the booked ids, a membership check on the rejection messages, and the count.*

The summary reads **`1 failed, 12 passed`**, with:

```text
E       assert 1 == 2
```

The first two asserts passed, so booking and rejecting both work — only the count is off. In `process_requests`, the `try` statement has three parts:

- **`try`** attempts the booking.
- **`except BookingError`** runs only when the booking failed.
- **`else`** runs only when the booking *succeeded*.

The count is incremented inside `else`, so rejected requests are never counted. Code that must run in every case belongs in a **`finally`** clause, which runs whether the `try` succeeded, failed and was caught, or failed with an exception nobody caught. In `booking.py`, we'll rewrite the `try` statement inside the loop:

```python
        try:
            booking_id = book(bookings, request["room"], request["people"], request["start"], request["end"])
        except BookingError as error:
            report["rejected"].append(str(error))
        else:
            report["booked"].append(booking_id)
        finally:
            report["processed"] += 1
```

*`else` keeps only the success-only work, and the count moves to `finally`.*

Expected summary: **`13 passed`**.

For these two requests, a plain line after the whole `try` statement would count correctly too. `finally` is the right home because it's the one place guaranteed to run no matter how the `try` block ends — including when an exception that nothing catches is on its way out of the function.

### 9. An EAFP Rewrite

Every bug is fixed, and the suite now does a second job: it lets us change code with confidence. `capacity_of` currently **looks before it leaps** (LBYL) — it checks whether the room exists, then looks it up:

```python
    if room not in ROOMS:
        raise BookingError(f"unknown room: {room}")
    return ROOMS[room]
```

*The LBYL version: check first, then act.*

Python's usual style is **EAFP** — easier to ask forgiveness than permission. We'll replace the whole `capacity_of` function in `booking.py` with a version that just tries the lookup, and handles the failure if it comes:

```python
def capacity_of(room):
    """Return how many people room holds. Raise BookingError if there's no such room."""
    try:
        return ROOMS[room]
    except KeyError as error:
        raise BookingError(f"unknown room: {room}") from error
```

*The dict lookup raises `KeyError` for a missing room, and the `except` clause translates it into the module's own exception — the same pattern `parse_time` uses for `ValueError`.*

Expected summary: **`13 passed`**.

Nothing about `capacity_of`'s behavior changed, and the unchanged test results are what prove it. The EAFP version looks the room up once instead of twice, and it can't drift out of step with its own check.

### Checking the Whole Suite

With all nine sections done, running `pytest -v` prints every test by name:

```text
src/test_booking.py::test_parse_time_converts_to_minutes PASSED          [  7%]
src/test_booking.py::test_parse_time_rejects_missing_colon PASSED        [ 15%]
src/test_booking.py::test_parse_time_rejects_impossible_hour PASSED      [ 23%]
src/test_booking.py::test_capacity_of_known_room PASSED                  [ 30%]
src/test_booking.py::test_capacity_of_unknown_room PASSED                [ 38%]
src/test_booking.py::test_book_returns_next_id PASSED                    [ 46%]
src/test_booking.py::test_book_adds_the_booking PASSED                   [ 53%]
src/test_booking.py::test_book_rejects_overlap PASSED                    [ 61%]
src/test_booking.py::test_room_can_be_filled_exactly PASSED              [ 69%]
src/test_booking.py::test_back_to_back_bookings_are_allowed PASSED       [ 76%]
src/test_booking.py::test_price_of_one_hour PASSED                       [ 84%]
src/test_booking.py::test_started_hour_is_billed_in_full PASSED          [ 92%]
src/test_booking.py::test_process_requests_counts_every_request PASSED   [100%]

============================= 13 passed in 0.04s ==============================
```

---

## Exercises

Add a function of your own to `booking.py`, and write its tests **first**.

Pick one of these, or invent your own that fits the booking model:

- **`cancel(bookings, booking_id)`** removes the booking with that id from the list and returns it.
- **`free_rooms(bookings, people, start_text, end_text)`** returns the sorted names of every room big enough for the group and free for the whole time.
- **`bookings_for(bookings, room)`** returns that room's bookings, earliest first.

Whichever you choose:

1. **Write the docstring first.** It's the contract, so it must say exactly what the function returns and when it raises `BookingError`. It must raise `BookingError` for at least one kind of invalid input, such as an unknown id, an unknown room, or an invalid time.
2. **Write the tests next, before the function body.** Include at least one equality check, one membership check, and one test that the function raises `BookingError`. Use the `bookings` fixture in at least one of them.
3. **Run `pytest` and watch the new tests fail.** Confirm they fail for the reason you expect, not because of a typo in the test.
4. **Implement the function** until the whole suite passes — the original 13 tests included.
