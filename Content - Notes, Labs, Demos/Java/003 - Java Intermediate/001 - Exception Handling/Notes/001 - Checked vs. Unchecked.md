# Checked vs. Unchecked

The Methods topic ended with methods that *throw*; this unit opens with the other side — understanding, catching, and designing exceptions. First, the map. Every exception in Java belongs to a class hierarchy, and one fork in that hierarchy — **checked** versus **unchecked** — decides whether the compiler forces callers to deal with it. Knowing which side an exception lives on, and which side a *new* failure belongs on, is the foundational skill of Java error handling.

---

## The Throwable Hierarchy

Everything that can be thrown descends from `java.lang.Throwable`, which splits three ways:

```
Throwable
├── Error                    — JVM-level disasters (unchecked)
│     OutOfMemoryError, StackOverflowError...
└── Exception                — application-level problems (CHECKED by default)
      ├── IOException, SQLException...          (checked)
      └── RuntimeException                      — and everything below it: UNCHECKED
            NullPointerException, IllegalArgumentException,
            IndexOutOfBoundsException, NumberFormatException...
```

*The family tree: `Error` and `RuntimeException` branches are unchecked; the rest of `Exception` is checked.*

**`Error`s** signal the JVM itself is in trouble — memory exhausted, stack overflowed. We don't catch these; there's rarely anything sane to do. They exist in the hierarchy so the *language* can throw them; application code never should.

**Checked exceptions** — `Exception` and subclasses, *except* the `RuntimeException` branch — represent foreseeable failures of the outside world: files missing (`IOException`), databases unreachable (`SQLException`), interrupted waits (`InterruptedException`). The compiler enforces the **handle-or-declare** rule from the Methods topic: code that can raise one must catch it or declare it in `throws`.

**Unchecked exceptions** — `RuntimeException` and below — represent *defects*: null references, bad indexes, invalid arguments. No declaration required, no catching enforced, because the intended fix is correcting the bug, not handling it at runtime. Every exception we met in Unit 2 lives here.

---

## The Compiler's Enforcement, Concretely

```java
// CHECKED: won't compile until the IOException is addressed
static String load(String path) {
    return Files.readString(Path.of(path));       // error: unhandled exception: IOException
}

// Option 1 — declare it onward
static String load(String path) throws IOException {
    return Files.readString(Path.of(path));
}

// UNCHECKED: compiles freely; failure is the caller's bug to fix
static int parsePort(String s) {
    return Integer.parseInt(s);                   // may throw NumberFormatException — no clause needed
}
```

*The compiler blocks unhandled checked exceptions; unchecked ones pass through silently.*

(Option 2 — catching — is the next lesson.) Note that `throws NumberFormatException` *may* be written for documentation; it just isn't required and changes nothing for callers.

---

## Which Kind Should a Failure Be?

The design question, since custom exceptions (lesson 3) must pick a parent. The classic guidance:

- **Checked** when the caller can *reasonably be expected to recover* — the failure is a normal, anticipatable part of using the operation. A missing config file has a story: prompt the user, fall back to defaults, try another path.
- **Unchecked** when the failure indicates a *programming error* — recovering makes no sense because correct code wouldn't be here. `IllegalArgumentException` for a percent of 150 shouldn't be caught; the calling code should be fixed.

The modern footnote, worth giving trainees honestly: the industry has cooled on checked exceptions. Forced handling sounds disciplined, but in practice it breeds empty `catch` blocks, `throws Exception` on everything, and friction with lambdas and streams (later this unit) — so most modern frameworks, Spring included, favor unchecked exceptions almost exclusively, reserving checked ones for genuinely recoverable, local situations. We'll follow that lean when designing our own; what's non-negotiable is *reading* both kinds fluently, because `IOException` and friends aren't going anywhere.

---

## Wrapping: Crossing the Boundary

The two worlds meet through **wrapping** — catching a checked exception and rethrowing it inside an unchecked one, preserving the original as the **cause**:

```java
static Config loadConfig(String path) {
    try {
        return Config.parse(Files.readString(Path.of(path)));
    } catch (IOException e) {
        throw new UncheckedIOException("cannot load config: " + path, e);   // cause attached
    }
}
```

*Translation at the boundary: a low-level checked failure becomes an unchecked one, cause preserved for the stack trace.*

This pattern — deal with checked exceptions near their source, let unchecked ones carry the news upward — keeps `throws IOException` from contaminating every signature between the file system and the top-level error handler. The mechanics of `try`/`catch` that make it work are exactly where we go next.
