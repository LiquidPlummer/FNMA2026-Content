# StringBuilder & StringBuffer

Immutability makes strings safe and poolable, but it has a price: "modifying" a string really means allocating a new one and copying every character over. Do that once, no problem. Do it in a loop, and the cost compounds badly. **`StringBuilder`** is the mutable companion class that fixes this — a resizable character buffer we can append to freely, converting to a real `String` only at the end.

---

## The Problem: Concatenation in Loops

```java
String report = "";
for (String line : lines) {
    report = report + line + "\n";     // new String created EVERY iteration
}
```

*Each `+` builds a brand-new string, copying everything accumulated so far.*

With each pass copying all previous characters, building an n-character result costs on the order of n² character copies. For 10 lines it's invisible; for 100,000 lines it's a program that mysteriously hangs. Any loop that grows a string with `+` or `+=` is the signal to switch tools.

(One `+` expression on a single line — `"Order " + id + ": " + status` — is fine. The compiler turns it into `StringBuilder` calls or better on its own. The problem is *repeated* concatenation across loop iterations.)

---

## StringBuilder

```java
StringBuilder sb = new StringBuilder();
for (String line : lines) {
    sb.append(line).append("\n");      // appends into an internal buffer — no copying spree
}
String report = sb.toString();         // one final String at the end
```

*The builder accumulates characters in place; `toString()` produces the immutable result once.*

`append` accepts every type — strings, primitives, chars, objects — and returns the builder itself, which is why calls chain. The rest of the API edits the buffer in place:

```java
StringBuilder sb = new StringBuilder("Hello world");

sb.append("!");                  // "Hello world!"
sb.insert(5, ",");               // "Hello, world!"
sb.replace(7, 12, "Java");       // "Hello, Java!"
sb.deleteCharAt(sb.length() - 1);// "Hello, Java"
sb.reverse();                    // "avaJ ,olleH"
sb.setLength(0);                 // "" — quick way to clear and reuse
int len = sb.length();           // works like String's length()
```

*In-place editing: insert, replace, delete, reverse — none of these allocate new strings.*

A `StringBuilder` is **not** a `String` — it won't work where a `String` is required, and its `equals` doesn't compare contents. Finish with `toString()` and hand that around.

Internally the builder keeps a char array with spare **capacity**, growing it as needed. When the final size is roughly known, pre-sizing skips the intermediate growth: `new StringBuilder(expectedLength)`. That's an optimization to reach for only when profiling says so.

---

## StringBuffer: The Legacy Twin

**`StringBuffer`** is `StringBuilder`'s older sibling — the same API, but every method is `synchronized` (thread-safe). It dates from Java 1.0; `StringBuilder` was added in Java 5 specifically as the faster, unsynchronized replacement for the overwhelmingly common case of building a string inside one thread.

The guidance is unambiguous:

- **Use `StringBuilder`** — effectively always.
- **`StringBuffer`** appears in legacy code and interview questions. Even for concurrent code it's rarely the right answer: synchronizing individual `append` calls doesn't make a multi-step build correct, so real concurrent designs coordinate at a higher level anyway.

Recognize `StringBuffer` on sight, don't reach for it.

---

## Choosing at a Glance

| Situation | Tool |
|---|---|
| Fixed text, single expressions, readability first | `String` and `+` |
| Building text in a loop or across many steps | `StringBuilder` |
| Structured formatting (padding, decimals, alignment) | `String.format` / `printf` |
| Joining a known collection with a separator | `String.join` |
| Legacy code that already uses it | `StringBuffer` (leave it be) |

The pattern worth internalizing: **immutable by default, mutable buffer for construction, back to immutable for the result.** It shows up all over Java — build with something mutable, publish something immutable.
