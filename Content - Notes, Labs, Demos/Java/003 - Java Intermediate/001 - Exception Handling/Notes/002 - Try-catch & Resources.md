# Try-catch & Resources

Throwing gets a failure *out* of a method; **catching** is where some method finally does something about it. The `try` statement marks a region of code whose exceptions we're prepared to handle, and its variants — multi-catch, `finally`, and try-with-resources — cover the real-world shapes of cleanup and recovery.

---

## try / catch

```java
try {
    int port = Integer.parseInt(args[0]);
    startServer(port);
} catch (NumberFormatException e) {
    System.err.println("Port must be a number, got: " + args[0]);
}
```

*Code that might fail goes in `try`; the matching `catch` receives the exception object if it does.*

Mechanics: statements in the `try` block run normally until an exception occurs; execution then jumps (the stack-unwinding jump from Control Flow) to the first `catch` whose type matches — the exception itself or any subclass. The block runs with `e` bound to the exception object — message (`e.getMessage()`), stack trace, and cause all available. If nothing in the `try` throws, every `catch` is skipped. If no catch matches, the exception continues up the stack as if the `try` weren't there.

Multiple catches are tried top to bottom, so **specific types must come first** — a `catch (Exception e)` above a `catch (IOException e)` makes the second unreachable, and the compiler says so. When several exception types deserve identical handling, **multi-catch** joins them with `|`:

```java
try {
    process(request);
} catch (JsonException | IllegalArgumentException e) {     // one block, two types
    log.warn("bad request: " + e.getMessage());
} catch (IOException e) {                                   // different failure, different response
    retryLater(request);
}
```

*Multi-catch collapses duplicate handlers; distinct recoveries keep distinct blocks.*

---

## What a catch Block Should Do

Catching is a claim: *"this method can do something meaningful about this failure."* Meaningful looks like: retrying, substituting a fallback value, translating to a caller-appropriate exception (the wrapping pattern from the previous lesson), or logging **at the top level** of the application where nothing else can. If none of those apply — *don't catch*; let it rise to a method that can.

The anti-patterns are worth naming because they're epidemic:

```java
try {
    saveOrder(order);
} catch (Exception e) { }                 // 1. swallowed — the failure now never happened

try {
    saveOrder(order);
} catch (Exception e) {
    e.printStackTrace();                  // 2. print-and-continue — logged noise, broken state marches on
}
```

*Two ways to make failures worse: silence them, or acknowledge them and carry on anyway.*

An empty catch block converts a loud, diagnosable failure into silent data corruption discovered weeks later. If a failure is truly ignorable (it almost never is), a comment in the block saying *why* is the minimum tax. And catch the narrowest type that fits — `catch (Exception e)` intended for one failure also mutes every unrelated bug in the block.

---

## finally — Runs No Matter What

A **`finally`** block executes whether the `try` completed, threw, or returned early — the language's guarantee for cleanup:

```java
Connection conn = pool.acquire();
try {
    return conn.query(sql);
} finally {
    pool.release(conn);          // runs on success, on exception, even after the return
}
```

*`finally` is for must-happen cleanup: releasing what was acquired, whatever the outcome.*

`finally` may appear with or without `catch` blocks. Two cautions: don't `return` from inside `finally` (it silently swallows any in-flight exception), and don't do failable work there. For its historically biggest use case — closing resources — modern Java has something better.

---

## try-with-resources

Anything that must be closed — files, streams, connections, anything implementing **`AutoCloseable`** — can be declared in the `try` header, and Java closes it automatically, in reverse order, exception or not:

```java
try (BufferedReader reader = Files.newBufferedReader(Path.of("data.csv"));
     PrintWriter out = new PrintWriter("report.txt")) {

    reader.lines().forEach(line -> out.println(transform(line)));

} catch (IOException e) {
    throw new ReportException("report generation failed", e);
}
```

*Resources declared in the header are guaranteed closed — no `finally`, no leak, no boilerplate.*

This is the mandatory idiom for resources — not a stylistic option. The manual `try`/`finally` version, done *correctly* (close can itself throw, mid-cleanup), takes a dozen lines per resource and everyone gets it wrong; try-with-resources also handles the ugly edge where both the body and the close throw, keeping the body's exception primary and attaching the close failure as a **suppressed exception** (visible in the stack trace, retrievable via `e.getSuppressed()`).

The declared resource variables are effectively final and scoped to the block — another case of Java pushing toward the smallest possible scope.

---

## The Shape of Good Handling

Putting the topic so far together: most methods should contain **no** try/catch at all — they validate, throw, and declare, letting failures travel. Catching concentrates in a few deliberate places: at *boundaries* (wrap low-level exceptions into domain terms), at *retry points* (transient failures with a second-chance strategy), and at the *top level* (log, respond gracefully, keep the application alive). When those domain terms don't exist yet — what does a billing system throw when a payment fails? — we create them: custom exceptions, next.
