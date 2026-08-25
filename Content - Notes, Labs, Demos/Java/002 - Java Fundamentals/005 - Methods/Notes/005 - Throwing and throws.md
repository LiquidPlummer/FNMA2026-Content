# Throwing and throws

Sometimes a method cannot do its job: the input is invalid, the file is missing, the divisor is zero. Returning a made-up value would poison every calculation downstream. Java's mechanism for honest failure is the **exception** — an object describing what went wrong, *thrown* out of the method instead of returned. Full exception handling (catching, recovering) opens the Java Intermediate unit; this lesson covers the method-side half: how a method **throws**, and what the **`throws`** clause in a signature means.

---

## `throw` — Failing on Purpose

The `throw` statement hurls an exception object, ending the method on the spot — no return value, no further statements. Its everyday use is **input validation**, the fail-fast door check promised in the Parameters lesson:

```java
static int applyDiscount(int price, int percent) {
    if (percent < 0 || percent > 100) {
        throw new IllegalArgumentException("percent must be 0-100, got: " + percent);
    }
    return price - (price * percent / 100);
}
```

*Reject bad input immediately, with a message that names the problem and the offending value.*

Anatomy: `throw`, then an exception object — almost always built on the spot with `new`, passing a message the eventual reader will thank us for. When a throw executes, the JVM begins **unwinding the call stack** (the abnormal jump from the Jumps lesson): the method aborts, its caller aborts, and so on until something catches the exception — or the stack runs out and the program dies printing the stack trace.

The two standard exceptions for validation, both from `java.lang`:

- **`IllegalArgumentException`** — a parameter value is unacceptable (`percent` of 150).
- **`IllegalStateException`** — arguments are fine, but the object isn't in a state for this call (writing to a closed file).

We've also *met* several thrown by the runtime itself: `ArrayIndexOutOfBoundsException`, `NullPointerException`, `NumberFormatException`, `ArithmeticException`. Same mechanism — the JVM's own code executes a `throw`.

---

## `throws` — Declaring the Possibility

`throw` is an action; **`throws`** is a *declaration*: a clause in the method signature announcing "this method may fail this way." Whether it's required depends on the exception's category — the split that organizes all of Java's exceptions:

- **Unchecked exceptions** (`RuntimeException` and subclasses — everything named so far) represent *bugs and bad calls*: invalid arguments, null references, out-of-range indexes. They may be thrown freely; declaring them is optional documentation. The expected fix is correct code, not a handler.
- **Checked exceptions** (everything else, famously `IOException`) represent *foreseeable failures of the outside world*: missing files, broken connections. These the compiler polices — a method that can raise one **must** declare it, and callers must deal with it.

```java
import java.io.IOException;
import java.nio.file.*;

static String loadConfig(String path) throws IOException {
    return Files.readString(Path.of(path));      // can fail: file missing, unreadable...
}
```

*`Files.readString` declares `IOException`, so `loadConfig` either handles it or — as here — declares it onward.*

That's the **handle-or-declare** rule: facing a checked exception, a method has exactly two legal moves — catch it (next unit) or add it to its own `throws` and pass the problem up. Skipping both is a compile error. A `throws` clause can list several types, comma-separated, and belongs after the parameter list exactly as shown.

One spelling clarification, because it trips everyone once: **`throw`** (no s) is the statement that fails *now*; **`throws`** (with s) is the signature clause that warns it *might*.

---

## Failure Is Part of the Contract

The previous lesson called a method's return "the contract"; exceptions are the contract's fine print — and worth designing with the same care:

- **Fail fast, fail loud.** Validate inputs at the top and throw immediately. An early `IllegalArgumentException` at the true source beats a `NullPointerException` three calls later in someone else's code.
- **Make messages actionable.** Include what was expected and what arrived. `"percent must be 0-100, got: 150"` diagnoses itself; `"invalid input"` starts a debugging session.
- **Throw, don't mumble.** Returning `null` or `-1` where the caller might not check is how errors travel silently. A throw cannot be ignored.
- **Don't overuse the escape hatch.** Exceptions are for *exceptional* situations, not expected outcomes — "user not found" from a search is a normal result (empty `Optional`), while "database unreachable" is an exception.

With methods able to define, call, parameterize, return, and fail honestly, the toolkit for procedural Java is complete. What remains is organizing state and behavior together — classes and objects, the next topic.
