# Collections API

The **Collections Framework** is the JDK's library of data structures — and the payoff of everything this topic assembled: generic interfaces (`List<E>`, `Set<E>`, `Map<K,V>`) in front, interchangeable implementations behind, iteration and ordering supplied by the small contracts we've covered. Arrays with their fixed lengths and manual bookkeeping now retire to the background; this is what production Java actually uses.

---

## The Map of the Framework

Three root abstractions cover nearly everything:

- **`List<E>`** — ordered, indexed, duplicates welcome. The default collection.
- **`Set<E>`** — no duplicates; membership is the point. Ordering varies by implementation.
- **`Map<K,V>`** — key → value associations; one value per key. (Technically beside, not under, the `Collection` interface — but always discussed together.)

Each is an interface with two or three implementations that matter:

| Interface | Implementation | Character |
|---|---|---|
| `List` | `ArrayList` | resizable array — fast index access, the default |
| | `LinkedList` | chain of nodes — rarely the right choice; measure first |
| `Set` | `HashSet` | hash-based — fastest, no order guarantee |
| | `LinkedHashSet` | hash + remembers insertion order |
| | `TreeSet` | sorted (via `Comparable`/`Comparator`), slower |
| `Map` | `HashMap` | hash-based — the default map |
| | `LinkedHashMap` | + insertion order |
| | `TreeMap` | keys kept sorted |
| `Queue`/`Deque` | `ArrayDeque` | ends-only access: stacks and queues |

*The 80/20 of the framework: `ArrayList`, `HashSet`, and `HashMap` are the defaults; the others earn their place with ordering or end-access needs.*

And the discipline from the OOP topic applies verbatim — **declare the interface, construct the implementation**: `List<String> names = new ArrayList<>();`. Callers depend on the contract; the concrete class stays swappable.

---

## The Working Vocabulary

```java
List<String> names = new ArrayList<>();
names.add("Ada");                          // append
names.add(0, "Grace");                     // insert at index
names.get(1);                              // "Ada"
names.set(1, "Barbara");                   // replace
names.remove("Grace");                     // by value (or by index)
names.contains("Ada");                     // false now
names.size();  names.isEmpty();

Set<String> tags = new HashSet<>();
tags.add("urgent");
tags.add("urgent");                        // returns false — already present, set unchanged

Map<String, Integer> stock = new HashMap<>();
stock.put("widget", 12);                   // add or overwrite
stock.get("bolt");                         // null — absent key (the NPE trap from Unit 2!)
stock.getOrDefault("bolt", 0);             // 0 — the safer read
stock.containsKey("widget");
stock.merge("widget", 5, Integer::sum);    // update-in-place: 12 → 17

for (Map.Entry<String, Integer> e : stock.entrySet()) {
    System.out.println(e.getKey() + " x" + e.getValue());
}
```

*The daily API across all three abstractions — note `size()` (not `length`), and maps iterated via `entrySet()`.*

A `Map` isn't `Iterable` itself; iteration goes through its three views — `keySet()`, `values()`, `entrySet()`. All collections speak the protocols already covered: `Iterable` (for-each, `removeIf`), sorting for lists, and everything here is generic — primitives ride via wrappers and autoboxing.

Two rules keep hash-based structures honest. **Hash collections require correct `equals`/`hashCode`** — the pair contract from the Inheritance lesson is exactly what `HashSet`/`HashMap` rely on; break it and elements genuinely vanish (stored under one hash, sought under another). And **never mutate an object while it's a hash key** — same vanishing act, self-inflicted.

---

## Immutable Collections

The factory methods create fixed collections in one expression — and they are *deeply* unmodifiable, throwing `UnsupportedOperationException` on any mutation:

```java
List<String> colors = List.of("red", "green", "blue");
Set<Integer> primes = Set.of(2, 3, 5, 7);
Map<String, Integer> limits = Map.of("retries", 3, "timeout", 30);

List<String> snapshot = List.copyOf(mutableList);      // defensive copy, immutable
```

*`List.of` / `Set.of` / `Map.of` for literals; `copyOf` for immutable snapshots of existing data.*

These are the right tool for constants, fixed configuration, and the defensive copies the Getters & Setters lesson prescribed. (One quirk: `List.of` rejects `null` elements outright.) Mutable-when-needed, immutable-by-default is the same instinct the Strings topic taught, now at collection scale.

---

## Choosing, and What's Next

The selection algorithm that covers most decisions: *need key→value lookup?* `HashMap`. *Need uniqueness?* `HashSet`. *Otherwise* `ArrayList` — reaching for `TreeSet`/`TreeMap` only when data must stay sorted continuously (one-time ordering is just `list.sort(...)`), `LinkedHash*` when iteration must mirror insertion, `ArrayDeque` for stack/queue access patterns. Rough costs to carry: hash operations are effectively constant time; tree operations logarithmic; `ArrayList` indexing constant but mid-list insertion linear.

The `Collections` utility class rounds things out (`shuffle`, `reverse`, `frequency`, `unmodifiableList`), and one legacy note: `Vector` and `Hashtable` are the pre-framework ancestors — recognize, don't use (the same verdict `StringBuffer` received).

What the framework still lacks is an elegant way to *process* collections — filter this list, transform that one, group the other. Loops do it; the next topic does it declaratively: functional programming, from lambdas to the Streams API, all of it built on collections as the source.
