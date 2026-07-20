# Lab: Generic Containers

In this lab we modernize a raw-`Object` container into a fully generic one: type parameters and the diamond, a bounded generic method, `Iterable` so for-each works on our own type, and the `ConcurrentModificationException` — provoked, understood, and fixed two ways. The starting point deliberately recreates pre-generics Java, because feeling that world's failure mode is the fastest way to understand what `<T>` buys.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

From this lab's folder:

```console
mvn -q compile exec:java
```

Starters: [Playlist.java](src/main/java/com/curriculum/labs/Playlist.java) (the raw container) and [ContainerLab.java](src/main/java/com/curriculum/labs/ContainerLab.java) (the driver).

---

## Part 1 — The year 2003, reenacted

Compile and notice Maven's grumbling before anything runs: *"uses unchecked or unsafe operations"* — the compiler already distrusts the raw types. Run it:

```
BLUE IN GREEN
Exception in thread "main" java.lang.ClassCastException:
    class java.lang.Integer cannot be cast to class java.lang.String
```

*The type error existed at the `mix.add(42)` line — but it detonated two lines later, at runtime, at the cast.*

Read the sequence in `main` the way the compiler saw it: `add(Object)` happily accepted an `Integer` into a playlist of songs; the cast on read is where reality caught up. In a real program those two lines might be in different files, written months apart — and the crash lands on whoever *reads*, not whoever wrote the bug. This gap — errors deferred from compile time to runtime — is the entire problem generics exist to close.

---

## Part 2 — Genericize the container

We'll parameterize `Playlist` by its element type. Change the class to:

```java
public class Playlist<T> {
    private final List<T> songs = new ArrayList<>();

    public void add(T song) {
        songs.add(song);
    }

    public T get(int index) {
        return songs.get(index);
    }

    public int size() {
        return songs.size();
    }
}
```

*One type parameter `T`, used everywhere `Object` used to be — the class is now a container of some caller-chosen type.*

In `main`, update the construction to `Playlist<String> mix = new Playlist<>();` (the diamond infers the argument). Compile — and the compiler now rejects the crime scene itself:

```
error: incompatible types: int cannot be converted to String
```

*`mix.add(42)` doesn't compile anymore. The runtime crash from Part 1 became a red squiggle at the true source.*

Delete the `add(42)` line, then delete both `(String)` casts — `get` returns `T`, and `T` is `String` here; the compiler *knows*. Run: both titles print, no casts, no crash possible. The warnings are gone too. That's the trade generics made: a little angle-bracket ceremony for a whole error class moved to compile time.

---

## Part 3 — A bounded generic method

Generic *methods* carry their own type parameter — and a **bound** makes the parameter useful. At **`[MARKER 4]`**:

```java
static <T extends Comparable<T>> T max(Playlist<T> list) {
    T best = list.get(0);
    for (int i = 1; i < list.size(); i++) {
        if (list.get(i).compareTo(best) > 0) {
            best = list.get(i);
        }
    }
    return best;
}
```

*`<T extends Comparable<T>>`: this works for ANY element type — as long as that type knows how to order itself. The bound is what makes `compareTo` legal in the body.*

At **`[MARKER 1]`**, try it twice: `System.out.println(max(mix));` (Strings compare alphabetically), and then build a `Playlist<Integer>`, add `3`, `41`, `7` (autoboxing — the wrapper classes earning their keep, since `Playlist<int>` won't compile; try it and read the error), and print its max. One method, two element types, zero casts. Then a negative test worth ten minutes of reading: define a tiny `class Note {}` and try `max` on a `Playlist<Note>` — the compile error tells you exactly what the bound demands.

---

## Part 4 — Iterable: joining the for-each club

`for (String s : mix)` doesn't compile yet — for-each requires `Iterable`. One declaration and one method fix that. Change the class header and add:

```java
public class Playlist<T> implements Iterable<T> {

    @Override
    public java.util.Iterator<T> iterator() {
        return songs.iterator();               // delegate to the backing list's iterator
    }
```

*Implementing `Iterable` by delegation — the backing `ArrayList` already knows how to iterate; we hand out its iterator.*

At **`[MARKER 2]`**, write the loop that just became legal:

```java
for (String title : mix) {
    System.out.println("♪ " + title);
}
```

*Our own class, native loop syntax — the compiler desugars this into `mix.iterator()` / `hasNext()` / `next()` calls.*

One method bought us membership in every for-each loop ever written. That's the small-interface leverage the notes promised.

---

## Part 5 — Breaking the iteration (and fixing it twice)

First give `Playlist` a removal method: `public void remove(T song) { songs.remove(song); }`. Then at **`[MARKER 3]`**, the classic mistake — removing *through the playlist* while for-each is walking it:

```java
mix.add("Freddie Freeloader");
for (String title : mix) {
    if (title.startsWith("So")) {
        mix.remove(title);                     // modifying mid-iteration...
    }
}
```

*Run it: `ConcurrentModificationException`. The iterator noticed the list changed under it and failed fast rather than corrupt the traversal.*

Fix one — the iterator's own removal is the sanctioned mid-loop mutation. Replace the loop with:

```java
var it = mix.iterator();
while (it.hasNext()) {
    if (it.next().startsWith("So")) {
        it.remove();
    }
}
```

*`Iterator.remove()` deletes the last-returned element with the iterator's knowledge — no exception.*

Fix two — the packaged version. Add to `Playlist`: `public void removeIf(java.util.function.Predicate<T> test) { songs.removeIf(test); }` and call `mix.removeIf(title -> title.startsWith("So"));`. (That arrow is a lambda — the Functional Programming topic gives it full treatment; for now, read it as "the test, written inline.") Same effect, one line, and it explains what `removeIf` was doing under the hood in the notes: exactly fix one.

---

## Exercises

1. **`Pair<A, B>`.** Write a generic class holding two values of independent types, with getters and a `swapped()` method returning a `Pair<B, A>`. Use it twice with different type combinations. No casts anywhere — if you need one, the design is wrong.

2. **Bounded sum.** Write `static double sum(List<? extends Number> values)` and call it with both a `List<Integer>` and a `List<Double>`. Then try changing the parameter to `List<Number>` and passing `List<Integer>` — read the error, and explain in a comment why the wildcard version accepts what the plain version refuses (the invariance rule from the notes).

3. **Fix the signature.** This method compiles but is needlessly rigid: `static void printAll(Playlist<String> list)`. Generalize it so it prints a playlist of *anything* — decide between `<T>` and `?`, and justify the choice in one comment line.

4. **Raw-type cleanup drill.** Recreate a small raw-type bug on purpose: a raw `List` passed between two methods, an object of the wrong type added in one, a cast crash in the other. Then fix it with generics and note (one sentence) where the error *moved* — that motion is the whole story of this lab.
