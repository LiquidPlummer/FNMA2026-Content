# Wrapper Classes

Primitives are fast, but they aren't objects — and a lot of Java only works with objects. Generics and collections (`ArrayList<...>`, `HashMap<...>`) can only hold object types, and sometimes we genuinely need a value that can be *absent* (`null`), which a primitive can never be. Java's answer is the **wrapper classes**: for each primitive type there is a corresponding class in `java.lang` that wraps a single value of that type in an object.

| Primitive | Wrapper     |
|-----------|-------------|
| `byte`    | `Byte`      |
| `short`   | `Short`     |
| `int`     | `Integer`   |
| `long`    | `Long`      |
| `float`   | `Float`     |
| `double`  | `Double`    |
| `char`    | `Character` |
| `boolean` | `Boolean`   |

Most follow the "capitalize the primitive" pattern; the two to memorize are **`int` → `Integer`** and **`char` → `Character`**.

---

## Autoboxing and Unboxing

We rarely construct wrappers by hand. The compiler converts between a primitive and its wrapper automatically:

- **Autoboxing** — primitive → wrapper, applied when an object is expected.
- **Unboxing** — wrapper → primitive, applied when a primitive is expected.

```java
Integer boxed = 42;              // autoboxing: int → Integer
int unboxed = boxed;             // unboxing: Integer → int

List<Integer> scores = new ArrayList<>();
scores.add(95);                  // 95 is autoboxed into the list
int first = scores.get(0);       // unboxed back to an int
```

*The compiler inserts the conversions, so wrappers and primitives mix almost seamlessly.*

That "almost" carries two sharp edges, covered next.

---

## Pitfall 1: `null` Unboxing

A wrapper variable can be `null` — and unboxing `null` throws a `NullPointerException`, often far from where the `null` was introduced:

```java
Map<String, Integer> ages = new HashMap<>();
Integer age = ages.get("Dana");   // no entry — returns null
int years = age;                  // NullPointerException at runtime
```

*`ages.get` returns `null` for a missing key, and unboxing that `null` throws an NPE.*

The habit to build: whenever a wrapper comes from a source that might not have a value (map lookups, database results, deserialized JSON), check for `null` before letting it unbox.

---

## Pitfall 2: `==` Compares References

Wrappers are objects, so `==` compares *references* (are these the same object?), not values. Value comparison uses `.equals()`. What makes this pitfall nasty is the **integer cache**: Java pre-creates `Integer` objects for values from -128 to 127, so small boxed values are the *same object* and `==` happens to work — until the numbers get bigger:

```java
Integer a = 100, b = 100;
System.out.println(a == b);        // true  — both from the cache

Integer c = 1000, d = 1000;
System.out.println(c == d);        // false — two distinct objects!
System.out.println(c.equals(d));   // true  — compares values
```

*`==` on wrappers appears to work for small values (cached) and silently fails for large ones.*

The rule is simple: **compare wrappers with `.equals()`, always** — or unbox to primitives first. Code that passes tests with small numbers and breaks in production with large ones is the classic symptom of getting this wrong.

---

## Utility Methods and Constants

The wrappers double as the standard library's home for type-related utilities. The ones we reach for constantly:

```java
int n = Integer.parseInt("42");            // String → int (NumberFormatException if malformed)
double d = Double.parseDouble("3.14");     // String → double
String s = Integer.toString(255);          // int → String ("255")
String hex = Integer.toHexString(255);     // "ff"

int max = Integer.MAX_VALUE;               // 2,147,483,647
int min = Integer.MIN_VALUE;               // -2,147,483,648

int cmp = Integer.compare(3, 7);           // negative — useful in sorting
boolean digit = Character.isDigit('7');    // true
```

*Parsing, formatting, range constants, and character tests all live on the wrapper classes.*

`parseInt` and friends are the standard way to turn user input, file contents, or HTTP parameters into numbers. Note that the explicit constructors (`new Integer(42)`) are deprecated — use autoboxing or `Integer.valueOf(42)`, which can reuse cached objects.

---

## When to Use Which

Default to primitives; reach for wrappers when the situation demands an object:

- **Primitives** — local variables, arithmetic, loop counters, fields that always have a value. Faster, no `null` risk.
- **Wrappers** — collection elements and generic type arguments (`List<Integer>`), values that can legitimately be absent (`Integer` from a map lookup), and APIs that require objects.

Performance is part of the story too: a `long[]` with a million elements stores a million values in one contiguous block, while a `List<Long>` stores a million separate objects plus references to them. In hot loops and large data sets, staying primitive matters.
