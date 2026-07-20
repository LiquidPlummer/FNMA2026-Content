# Declaring & Initializing Arrays

An **array** is Java's most basic container: a fixed-length, ordered sequence of elements, all of the same type. Arrays are objects — they live on the heap, and a variable of array type holds a *reference* to one. Two properties define them and drive every design decision around them: the **length is fixed at creation**, and access by position is as fast as memory access gets.

---

## Declaring

An array type is the element type plus square brackets:

```java
int[] scores;          // array of int
String[] names;        // array of String
double[][] grid;       // array of arrays of double (more on these later)
```

*Array declarations — the brackets belong with the type.*

Java also permits the C-style `int scores[]`, but the convention is firm: brackets go on the **type**, because "`int[]`" *is* the type of the variable. Declaring creates only the reference variable — no array exists yet.

---

## Creating with `new`

The `new` operator allocates an array of a given length, with every element set to the type's default value (`0`, `0.0`, `false`, or `null` — the same defaults fields get):

```java
int[] scores = new int[5];         // [0, 0, 0, 0, 0]
String[] names = new String[3];    // [null, null, null]

scores[0] = 97;                    // fill individual slots
names[0] = "Ada";
```

*`new type[length]` allocates a zero-filled (or null-filled) array of exactly that size.*

The `null` fill for object arrays deserves emphasis: `new String[3]` creates an array of three *references to nothing*. Touching `names[1].length()` before assigning something there throws a `NullPointerException`. Creating the array does not create its elements.

The length can be any non-negative `int` expression — a variable, a computed value, zero. But once created, it's permanent. "Growing" an array actually means allocating a bigger one and copying (`Arrays.copyOf(old, newLength)` does exactly that) — which is why collections like `ArrayList`, which do this bookkeeping automatically, take over once sizes become dynamic. That story continues in the Collections Framework topic.

---

## Array Literals

When the values are known up front, an **array initializer** declares, sizes, and fills in one stroke:

```java
int[] primes = {2, 3, 5, 7, 11};
String[] weekdays = {"Mon", "Tue", "Wed", "Thu", "Fri"};
```

*The initializer counts the elements itself — no explicit length, no `new`.*

The brace form is only legal in a declaration. To pass a fresh array to a method or assign one later, add the type explicitly with `new`:

```java
int[] primes;
// primes = {2, 3, 5};                 // compile error: initializer only allowed in declarations
primes = new int[]{2, 3, 5};           // fine — "anonymous" array creation
printAll(new String[]{"a", "b"});      // fine as an argument, too
```

*Outside a declaration, the element list needs the full `new type[]{...}` form.*

---

## Arrays Are References

Assigning an array variable copies the *reference*, not the contents — both variables then point at the same array:

```java
int[] a = {1, 2, 3};
int[] b = a;             // b is the SAME array
b[0] = 99;
System.out.println(a[0]);          // 99 — visible through both names
int[] c = a.clone();               // an actual copy (independent contents)
```

*One array, two references — mutations through either are seen by both; `clone()` makes a real copy.*

The same logic explains comparison and printing: `a == b` asks "same array object?", `a.equals(b)` on arrays is *also* just a reference check, and `System.out.println(a)` prints a cryptic type-and-hash like `[I@1b6d3586`. The useful versions all live in the **`java.util.Arrays`** utility class — `Arrays.equals(a, b)` for content comparison and `Arrays.toString(a)` for readable output. We'll lean on that class throughout this topic.
