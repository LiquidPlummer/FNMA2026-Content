# String Pool

Strings are everywhere in a running program — class names, config keys, log messages, user data — and many of them are duplicates. Java exploits string immutability to deduplicate them: the JVM keeps a special region called the **string pool** (or *intern pool*), and every string **literal** in the source code is stored there exactly once. Two literals with the same characters aren't two objects — they're two references to the same pooled object.

---

## Literals Share One Object

```java
String a = "hello";
String b = "hello";

System.out.println(a == b);        // true — same pooled object
System.out.println(a.equals(b));   // true — same characters, of course
```

*Both literals resolve to a single pooled `String`, so even the reference comparison passes.*

This only works because strings are immutable. If strings could change, one variable mutating "its" string would silently change it for every other reference sharing the pooled object. Immutability makes sharing safe; sharing makes strings cheap.

---

## `new String(...)` Bypasses the Pool

Constructing a string with `new` forces a fresh object on the regular heap, even when an identical string sits in the pool:

```java
String a = "hello";
String b = new String("hello");

System.out.println(a == b);        // false — different objects
System.out.println(a.equals(b));   // true  — same characters
```

*`new` guarantees a distinct object, so `==` fails while `.equals()` succeeds.*

The same applies to strings built at **runtime** — concatenation of variables, `substring`, user input, file contents. None of those are literals, so none are automatically pooled:

```java
String name = "he" + "llo";        // compile-time constant → pooled → name == "hello" is true
String suffix = "llo";
String built = "he" + suffix;      // runtime concatenation → NOT pooled
System.out.println(built == "hello");        // false
System.out.println(built.equals("hello"));   // true
```

*The compiler folds constant expressions into pooled literals, but anything computed at runtime is a new object.*

This is exactly why `==` on strings is treacherous: it passes in quick tests full of literals, then fails in production where strings come from real input. It's the string-flavored version of the `Integer` cache pitfall from the Variables & Types topic, and the conclusion is the same — **compare with `.equals()`, always.**

---

## `intern()` — Manual Pooling

Every string has an **`intern()`** method that returns the pooled version of its contents, adding it to the pool if needed:

```java
String built = ("he" + getSuffix());   // runtime string, not pooled
String canon = built.intern();         // pooled copy

System.out.println(canon == "hello");  // true — canon is the pool's object
```

*`intern()` exchanges any string for its canonical pooled equivalent.*

In practice, explicit interning is rare — a niche memory optimization for programs holding millions of duplicate strings (and modern JVMs can deduplicate string memory during garbage collection anyway). It's worth knowing mainly because it explains what the pool *is*: a lookup table of canonical strings, which literals use automatically.

---

## What to Remember

- Every string **literal** lives in the pool exactly once; identical literals share one object.
- Strings created with `new` or computed at **runtime** are separate heap objects, outside the pool.
- Therefore `==` on strings is a reference check with misleading behavior — it can be `true` for pooled literals and `false` for equal runtime strings. Use `.equals()`.
- The pool exists because strings are **immutable** — sharing is safe when nothing can change.
