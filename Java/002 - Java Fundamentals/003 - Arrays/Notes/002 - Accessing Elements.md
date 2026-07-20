# Accessing Elements

Every element in an array has a position — its **index** — and the bracket operator reads or writes the element at that position. Indexing is the whole point of arrays: getting element 40,000 costs exactly the same as getting element 0.

---

## Zero-Based Indexing

Array indexes start at **0**, so an array of length n has valid indexes 0 through n-1:

```java
String[] crew = {"Ada", "Grace", "Edsger", "Barbara"};

String first = crew[0];       // "Ada"
String last = crew[3];        // "Barbara" — index 3, though it's the 4th element
crew[1] = "Katherine";        // assignment: replace "Grace"
crew[2] = crew[2].toUpperCase();   // read, transform, write back
```

*Reading with `crew[i]` and writing with `crew[i] = ...` — same syntax, either side of the assignment.*

The mental shift that prevents most indexing bugs: an index isn't "which element" so much as "how far from the start." The first element is 0 elements from the start. The last element of a length-n array is at n-1 — a formula that comes up constantly (`crew[crew.length - 1]`).

The index can be any `int` expression — a literal, a variable, arithmetic, a method call. This is what makes arrays programmable: `data[i]`, `data[i + 1]`, `data[indexOf(target)]`.

---

## Out of Bounds

Java checks every array access at runtime. An index outside 0..length-1 — negative, or length and beyond — throws an **`ArrayIndexOutOfBoundsException`** naming the bad index and the array's length:

```java
int[] data = new int[3];      // valid indexes: 0, 1, 2
data[3] = 42;                 // throws: Index 3 out of bounds for length 3
```

*Index 3 in a length-3 array is one past the end — the classic off-by-one.*

That exception is a feature. In lower-level languages, the same mistake silently reads or corrupts neighboring memory; Java stops the program at the exact line with the exact numbers. The two habitual causes are worth naming:

- **Off-by-one**: using `length` instead of `length - 1` for the last element, or `<=` instead of `<` in a loop condition.
- **Stale index**: an index computed from other data (user input, a search result like `indexOf` returning -1) used without validation.

---

## Object Arrays: Accessing References

For an array of objects, `arr[i]` retrieves a *reference*. Two consequences follow. First, an element that was never assigned is `null`, and calling a method on it throws a `NullPointerException` — the array access itself succeeds; the failure happens a step later. Second, mutating the object an element points to is not the same as replacing the element:

```java
StringBuilder[] cells = { new StringBuilder("a"), new StringBuilder("b") };

cells[0].append("!");                  // mutates the object cells[0] refers to → "a!"
cells[1] = new StringBuilder("B");     // replaces the reference in slot 1
```

*Two different operations: changing the object at a slot vs. pointing the slot at a new object.*

---

## Bulk Access: `java.util.Arrays`

A few utility methods cover the everyday "touch many elements at once" chores:

```java
int[] data = new int[5];
Arrays.fill(data, -1);                     // [-1, -1, -1, -1, -1]

int[] copy = Arrays.copyOf(data, 8);       // longer copy, extra slots zero-filled
int[] slice = Arrays.copyOfRange(data, 1, 4);   // indexes 1,2,3 — end exclusive

int[] sorted = {3, 1, 4, 1, 5};
Arrays.sort(sorted);                       // [1, 1, 3, 4, 5] — sorts in place
int pos = Arrays.binarySearch(sorted, 4);  // fast lookup — requires sorted input
```

*Fill, copy, slice, sort, and search without writing the loops by hand.*

Note the range convention repeating from `substring`: `copyOfRange(from, to)` includes `from`, excludes `to`. Java uses half-open ranges everywhere, and internalizing that eliminates a whole family of off-by-one errors.
