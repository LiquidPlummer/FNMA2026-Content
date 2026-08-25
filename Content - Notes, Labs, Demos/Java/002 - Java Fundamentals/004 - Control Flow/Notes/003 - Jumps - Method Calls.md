# Jumps - Method Calls

Loops repeat and branches choose, but both keep execution *inside* the current method. The third kind of control flow **jumps**: execution leaves the current location entirely, runs code somewhere else, and (usually) comes back. In Java, the everyday jump is the **method call** — and understanding it as control flow, not just syntax, explains the call stack, stack traces, and eventually exceptions.

---

## A Call Is a Round Trip

When execution reaches a method call, the current method pauses mid-statement, control transfers to the called method's first line, the body runs to a `return` (or its end), and control lands back exactly where it left — with the return value dropped into the paused expression:

```java
int total = subtotal + calculateTax(subtotal);   // pause here...

static int calculateTax(int amount) {            // ...jump to here
    return amount * 8 / 100;                     // ...and jump back with a value
}
```

*One statement, one round trip: out to `calculateTax`, back with a result, then the addition finishes.*

Calls nest arbitrarily — `main` calls `processOrder`, which calls `calculateTax`, which calls `Math.round`. Each call suspends its caller, so at any instant there's a chain of half-finished methods waiting, each one remembering where it paused.

---

## The Call Stack

That chain has a name and a data structure: the **call stack**. Every call pushes a **stack frame** holding the method's parameters, its local variables, and the return address (where to resume in the caller). Every return pops the frame, and the memory is instantly gone — this is why locals live and die with their method (the Scope lesson's rule, now with a mechanism).

```
main()                         main()                     main()
  └─ processOrder()      →       └─ processOrder()   →      (returned)
       └─ calculateTax()              (returned)
```

*The stack grows as calls nest and shrinks as they return — last called, first finished.*

Two practical consequences:

**Stack traces read bottom-up.** When something goes wrong, Java prints the stack — a snapshot of exactly this chain. The top line is where the failure happened; the lines below are the callers that led there:

```
Exception in thread "main" java.lang.ArithmeticException: / by zero
    at Billing.calculateTax(Billing.java:42)
    at Billing.processOrder(Billing.java:17)
    at Billing.main(Billing.java:8)
```

*A stack trace is the call stack, printed: `main` called `processOrder` called `calculateTax`, which failed at line 42.*

Reading these fluently is a daily skill — start at the top, find the first line in *our* code, and the trail below explains how execution got there.

**The stack is finite.** A method that calls itself — **recursion** — pushes a frame per call. Legitimate recursion always moves toward a stopping condition (a *base case*); recursion that doesn't, or a mutual-call cycle, exhausts stack space and throws `StackOverflowError`:

```java
static long factorial(int n) {
    if (n <= 1) return 1;            // base case: stop recursing
    return n * factorial(n - 1);     // each call is one frame until n reaches 1
}
```

*Well-founded recursion: the stack grows n frames deep, then unwinds. Remove the base case, and the stack — typically a few thousand frames — overflows.*

---

## `return` Is a Jump, Too

`return` transfers control unconditionally out of the current method, from anywhere in the body. Used early, it's a branch and a jump combined — and the basis of the **guard clause** style, which handles edge cases up front instead of nesting the whole method inside `if`s:

```java
static double average(int[] data) {
    if (data == null || data.length == 0) return 0;   // guard: reject bad input, exit now

    int sum = 0;
    for (int n : data) sum += n;
    return (double) sum / data.length;                // main path, unindented
}
```

*Guard clauses jump out early so the main logic reads straight down.*

`break` and `continue` (from the Loops lesson) are the other small jumps — local ones, within a method. Java pointedly has no `goto` (the keyword is reserved but unusable): every jump is *structured*, tied to a loop, a method boundary, or —

---

## The Jump That Doesn't Come Back Normally

One more control transfer completes the picture: a **thrown exception**. Instead of returning to its caller, a failing method aborts, and the JVM unwinds the call stack frame by frame, looking for a handler — potentially jumping across many methods at once. That stack trace above is the flight recording of exactly such a jump. The full mechanics — `throw`, `try`/`catch`, and designing with exceptions — open the Java Intermediate unit; the groundwork (methods declaring `throws`) appears in the Methods topic next.
