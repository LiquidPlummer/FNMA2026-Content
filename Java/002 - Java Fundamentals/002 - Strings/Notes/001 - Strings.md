# Strings

A **`String`** is Java's text type: a sequence of characters, written in double quotes. We've been using strings since Hello World, but they deserve a closer look, because two facts about them shape everything else: a `String` is an **object**, not a primitive — and it is **immutable**, meaning once created, its contents never change.

```java
String greeting = "Hello, world";
String empty = "";
String block = """
        Text blocks (Java 15+) span
        multiple lines without escapes.
        """;
```

*String literals: a plain string, an empty string, and a multi-line text block.*

---

## Immutability

No method on a `String` modifies it. Every operation that looks like a change — uppercasing, trimming, replacing — returns a **new** string and leaves the original untouched:

```java
String name = "ada lovelace";
name.toUpperCase();                      // result thrown away — name is unchanged!
String shout = name.toUpperCase();       // "ADA LOVELACE" — a new String

System.out.println(name);                // still "ada lovelace"
```

*Calling `toUpperCase()` doesn't change `name`; the result must be captured in a variable.*

Forgetting to capture the result is one of the most common beginner bugs in Java, and the compiler won't flag it. The upside of immutability is substantial: strings are safe to share between threads, safe to use as map keys, and eligible for the memory-saving **string pool** we'll cover in the next lesson. The cost — building strings piece by piece gets expensive — is what `StringBuilder` solves, two lessons from now.

---

## Comparing Strings

Because strings are objects, `==` compares references, not text. Content comparison is always `.equals()`:

```java
String a = scanner.nextLine();     // user typed "yes"
if (a == "yes") { ... }            // unreliable — compares references
if (a.equals("yes")) { ... }       // correct — compares characters
if (a.equalsIgnoreCase("YES")) { ... }   // correct, case-insensitive
```

*`==` may happen to work for literals but fails for runtime strings; `.equals()` is the rule.*

Why `==` *sometimes* works for literals is the subject of the String Pool lesson. The habit to build now: **`.equals()` for strings, every time.** When one side is a literal, writing it first — `"yes".equals(a)` — also protects against `a` being `null`.

For ordering (sorting, comparisons), `compareTo` returns a negative, zero, or positive value reflecting lexicographic order: `"apple".compareTo("banana") < 0`.

---

## The Everyday API

`String` has dozens of methods; this core handles most real work:

```java
String s = "  Order #1042: shipped  ";

s.length();                  // 25 — character count
s.charAt(2);                 // 'O' — character at index (zero-based)
s.strip();                   // "Order #1042: shipped" — whitespace removed
s.toUpperCase();             // "  ORDER #1042: SHIPPED  "
s.contains("shipped");       // true
s.indexOf("#");              // 8 — position of first match, -1 if absent
s.substring(8, 13);          // "#1042" — from index 8 up to (not including) 13
s.replace("shipped", "delivered");
s.startsWith("  Order");     // true
s.isBlank();                 // false — true for empty or whitespace-only
```

*The workhorse methods: measuring, extracting, searching, and transforming text.*

Two indexing details to lock in: positions are **zero-based** (`charAt(0)` is the first character), and `substring(begin, end)` includes `begin` but *excludes* `end` — so `end - begin` is the length of the result. Out-of-range indexes throw a `StringIndexOutOfBoundsException`.

Splitting and joining round out the set:

```java
String csv = "red,green,blue";
String[] parts = csv.split(",");            // ["red", "green", "blue"]
String joined = String.join(" | ", parts);  // "red | green | blue"
```

*`split` breaks a string into an array; `String.join` reassembles pieces with a separator.*

---

## Building Strings: Concatenation and Formatting

The `+` operator concatenates, and mixing in non-strings converts them automatically (as we saw in Hello World):

```java
String label = "Order " + 1042 + " total: $" + 19.99;
```

*Concatenation with `+` — anything joined to a string becomes a string.*

For anything with structure, **`String.format`** (or the instance form `formatted`) is cleaner than long `+` chains. It uses format specifiers: `%s` for strings, `%d` for integers, `%f` for decimals, `%n` for a newline:

```java
String receipt = String.format("Order %d: %s — $%.2f", 1042, "shipped", 19.99);
// "Order 1042: shipped — $19.99"
```

*`String.format` substitutes values into placeholders; `%.2f` limits the decimal to two places.*

The same specifiers work in `System.out.printf(...)` for direct printed output.

---

## Strings and `null`

A `String` variable is a reference, so it can be `null` — and calling any method on `null` throws a `NullPointerException`. Distinguish the three "nothing" states: `null` (no string at all), `""` (empty string), and `" "` (blank but not empty). `isEmpty()` and `isBlank()` test the last two; only an explicit `!= null` check handles the first. When a string might be `null`, check for that before calling methods on it — or design so it can't be (initialize to `""`).
