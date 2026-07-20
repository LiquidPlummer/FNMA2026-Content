# Iterable & Iterators

The for-each loop has carried us since the Arrays topic — over arrays, over `List.of(...)`, apparently over anything. Time to reveal the machinery: for-each is compiler shorthand around two small interfaces, **`Iterable`** and **`Iterator`**, and they're the first taste of the interface-first design the whole Collections Framework follows.

---

## Iterator: One Pass, Two Methods

An **`Iterator<E>`** is a cursor over a sequence — a stateful object that hands out elements one at a time:

```java
List<String> names = new ArrayList<>(List.of("Ada", "Grace", "Barbara"));

Iterator<String> it = names.iterator();
while (it.hasNext()) {                 // anything left?
    String name = it.next();           // take the next one, advance the cursor
    System.out.println(name);
}
```

*The manual iteration protocol: test with `hasNext()`, consume with `next()`, repeat.*

The contract is minimal by design: `hasNext()` peeks, `next()` returns the next element and advances (throwing `NoSuchElementException` if nothing remains — always test first). An iterator is one-shot and forward-only; iterating again means asking for a fresh one. What it buys over indexed access: it works for *any* sequence shape — linked lists where indexing is slow, sets that have no indexes at all, streams of unknown length.

---

## Iterable: "You May Loop Over Me"

**`Iterable<E>`** is the capability interface with a single method — `iterator()` — and it is exactly what the for-each loop requires. The compiler translates:

```java
for (String name : names) {
    System.out.println(name);
}
// ...into precisely the while-loop above: names.iterator(), hasNext(), next()
```

*For-each is sugar: any `Iterable` on the right of the colon, and the compiler writes the iterator plumbing.*

Every collection in the framework implements `Iterable` — which is why for-each "just works" on lists, sets, and queues alike (arrays get their own separate compiler treatment). And the interface is ours to implement too: any class with something sequence-like inside can join the club:

```java
public class Playlist implements Iterable<Song> {
    private final List<Song> songs = new ArrayList<>();

    @Override
    public Iterator<Song> iterator() {
        return List.copyOf(songs).iterator();     // delegate — and defensively, at that
    }
}

for (Song s : myPlaylist) { ... }                  // our own type, native loop syntax
```

*Implementing `Iterable` in one line by delegating to an internal collection's iterator.*

Delegation like this covers most real cases; hand-writing an `Iterator` (a small class tracking a position, implementing `hasNext`/`next`) is occasionally needed for computed or lazy sequences.

---

## Modifying While Iterating: The Classic Trap

Structurally changing a collection *while* a for-each is walking it — adding, removing via the collection itself — is a bug the framework detects and refuses:

```java
for (String name : names) {
    if (name.startsWith("B")) {
        names.remove(name);            // ConcurrentModificationException
    }
}
```

*The iterator notices the collection changed under it and fails fast rather than corrupt the traversal.*

The collection's iterators are **fail-fast**: they detect concurrent structural modification and throw `ConcurrentModificationException` — better a loud failure than a silently skipped element. Three correct alternatives, in rising order of elegance:

```java
// 1. The iterator's own remove — the one legal mid-loop mutation
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    if (it.next().startsWith("B")) it.remove();
}

// 2. removeIf — the same thing, packaged (uses a lambda: next topic's syntax)
names.removeIf(name -> name.startsWith("B"));

// 3. Collect-then-apply: build the result instead of mutating in place
List<String> keepers = new ArrayList<>();
for (String name : names) {
    if (!name.startsWith("B")) keepers.add(name);
}
```

*Three safe patterns: `Iterator.remove()`, `removeIf`, or building a new collection.*

`removeIf` is the everyday answer; the explicit iterator explains *why* it works. (True multi-thread modification is a different problem with different tools — Concurrency, Java Advanced.)

---

## Why Such Small Interfaces Matter

`Iterable` is abstraction (last topic) applied perfectly: one method, yet it lets the `for-each` loop, and any algorithm written against `Iterable`, serve every collection ever written — including ones that don't exist yet. The framework repeats this move at every level, as the next lessons show: `Comparable` (one method) plugs any type into every sorting algorithm; `List`/`Set`/`Map` (contracts) stand in front of interchangeable implementations. Small interface, universal leverage — the design lesson of the whole topic.
