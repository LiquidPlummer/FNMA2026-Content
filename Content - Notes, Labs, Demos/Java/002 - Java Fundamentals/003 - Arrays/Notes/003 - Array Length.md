# Array Length

Every array knows its own size, exposed as the **`length`** field. It's set when the array is created and never changes — reading it is the single most common thing done with an array besides indexing it.

```java
int[] scores = new int[10];
String[] names = {"Ada", "Grace", "Barbara"};

scores.length      // 10
names.length       // 3
```

*`length` reports the allocated size — how many slots exist, regardless of what's in them.*

---

## Field, Not Method — and the length() Confusion

Array `length` is a `final` **field**: no parentheses. Java's other size-reporting types made different choices, and mixing them up is a rite of passage:

| Type | Size syntax |
|---|---|
| Array | `arr.length` — field |
| `String` | `str.length()` — method |
| Collections (`List`, `Set`, `Map`...) | `list.size()` — method |

```java
int[] arr = {1, 2, 3};
String s = "abc";
List<Integer> list = List.of(1, 2, 3);

arr.length;      // 3
s.length();      // 3
list.size();     // 3
// arr.length(); // compile error — length is not a method on arrays
```

*Three sizes, three spellings — the compiler catches the wrong one, but knowing which is which saves the round trip.*

---

## Length Counts Slots, Not Contents

`length` is capacity, not "how many meaningful values are in there." A `new String[10]` has length 10 and contains ten `null`s. Arrays have no notion of "used" vs. "unused" slots — code that fills an array gradually must track its own count:

```java
String[] buffer = new String[100];
int count = 0;                       // our bookkeeping, not the array's

buffer[count++] = "first";
buffer[count++] = "second";
// buffer.length is still 100; count is 2
```

*The array reports 100 either way — tracking how much is actually filled is on us.*

This gap — fixed capacity plus manual count — is precisely the bookkeeping `ArrayList` automates, which is why it replaces raw arrays in most application code.

Also note that `length` can legitimately be **0**: empty arrays (`new int[0]`) are common as "no results" return values, and any loop written against `length` handles them for free. Watch the `null` distinction, though — calling `.length` on a `null` array reference throws a `NullPointerException`; an empty array is a real object whose length is 0.

---

## The Patterns Built on `length`

Nearly every use of `length` is one of these:

```java
int[] data = {4, 8, 15, 16, 23, 42};

// Loop bound: < length, never <= length
for (int i = 0; i < data.length; i++) { ... }

// Last element, and counting from the end
int last = data[data.length - 1];            // 42
int secondToLast = data[data.length - 2];    // 23

// Guard before indexing
if (i >= 0 && i < data.length) {
    // safe to touch data[i]
}

// Middle element
int mid = data[data.length / 2];
```

*The recurring idioms: exclusive loop bounds, `length - 1` for the end, and range guards.*

The first two lines are the antidote to the off-by-one errors from the previous lesson: loop conditions use `< length`, and the last valid index is `length - 1`. Written that way, the code stays correct no matter what size the array actually is — which is the entire point of asking the array instead of hard-coding a number.
