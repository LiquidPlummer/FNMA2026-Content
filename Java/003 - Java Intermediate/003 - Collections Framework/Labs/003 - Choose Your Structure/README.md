# Lab: Choose Your Structure

In this lab we work the core Collections API — `ArrayList`, `HashSet`, `HashMap` and its ordered cousins `LinkedHashMap`/`TreeMap`, plus the immutable factories — on tasks where each structure is the obviously right tool in turn. Along the way we spring the framework's most famous trap: a hash collection silently losing elements because a class skipped `equals`/`hashCode`.

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

The starter is [StructureLab.java](src/main/java/com/curriculum/labs/StructureLab.java) — a text block of warehouse chatter and six markers.

---

## Part 1 — A List: ordered, duplicated, indexed

At **`[MARKER 1]`**, split the text into words and collect them:

```java
java.util.List<String> words = new java.util.ArrayList<>();
for (String w : TEXT.split("\\W+")) {
    if (!w.isBlank()) {
        words.add(w.toLowerCase());
    }
}
System.out.println("total words: " + words.size());
System.out.println("first: " + words.get(0) + ", last: " + words.get(words.size() - 1));
```

*The default collection: grows as we add, keeps insertion order, duplicates welcome, instant access by index — note `size()` and `get(i)`, the List spellings of the array idioms.*

Run it: 29 words. The list faithfully keeps every repetition of "the" and "stock" — which is correct behavior *for a list*, and exactly what the next two structures exist to change.

---

## Part 2 — A Set: uniqueness is the whole product

At **`[MARKER 2]`**, pour the same words into a set:

```java
java.util.Set<String> vocabulary = new java.util.HashSet<>(words);
System.out.println("distinct words: " + vocabulary.size());
System.out.println("contains 'audit'? " + vocabulary.contains("audit"));
boolean added = vocabulary.add("stock");
System.out.println("adding 'stock' again changed the set: " + added);
```

*One constructor call dedupes the entire list — and `add` on a duplicate returns `false`, the set's polite refusal.*

29 in, 14 distinct. The gap between those two numbers *is* the set's report. Note also `contains` — on a `HashSet` that check is effectively instant regardless of size, which is the real reason sets appear everywhere membership is asked repeatedly.

---

## Part 3 — A Map: from "did it appear" to "how often"

At **`[MARKER 3]`**, the classic word count:

```java
java.util.Map<String, Integer> freq = new java.util.HashMap<>();
for (String w : words) {
    freq.merge(w, 1, Integer::sum);
}
System.out.println("'stock' appears " + freq.getOrDefault("stock", 0) + " times");
System.out.println("'gizmo' appears " + freq.getOrDefault("gizmo", 0) + " times");
```

*`merge`: insert 1 for a new key, add 1 for an existing one — the whole check-then-act dance in one atomic-feeling call. `getOrDefault` reads without the null trap.*

(`Integer::sum` is another method reference — "add the two.") Run it: `stock` 4, `gizmo` 0 — and note the `gizmo` line *didn't* NPE, because `getOrDefault` supplied the 0 that a bare `get` would have made our problem.

---

## Part 4 — Three maps, three iteration orders

The map *interface* says nothing about order; the implementations disagree loudly. At **`[MARKER 4]`**, build all three from the same data and print them side by side:

```java
var hash = new java.util.HashMap<>(freq);
var linked = new java.util.LinkedHashMap<String, Integer>();
for (String w : words) linked.merge(w, 1, Integer::sum);     // built in encounter order
var tree = new java.util.TreeMap<>(freq);

System.out.println("HashMap:       " + hash.keySet());
System.out.println("LinkedHashMap: " + linked.keySet());
System.out.println("TreeMap:       " + tree.keySet());
```

*Same 14 keys, three orders: HashMap's is arbitrary (and can differ between runs/JVMs), LinkedHashMap remembers first-insertion order, TreeMap keeps keys sorted — Comparable, from the last lab, powering it.*

The selection rule falls out of the printout: default to `HashMap`; reach for `LinkedHashMap` when output should mirror input order (reports, JSON); reach for `TreeMap` when consumers need sorted keys continuously. Never *rely* on `HashMap` order — code that accidentally does works for months and breaks on a JVM upgrade.

---

## Part 5 — The vanishing element

Now the trap. Create a small class in a new file, deliberately bare:

```java
package com.curriculum.labs;

public class Tag {
    private final String name;
    public Tag(String name) { this.name = name; }
    public String getName() { return name; }
    @Override public String toString() { return "#" + name; }
}
```

*No `equals`, no `hashCode` — it inherits Object's identity-based versions. Looks harmless.*

At **`[MARKER 5]`**, use it as hash material:

```java
var tags = new java.util.HashSet<Tag>();
tags.add(new Tag("urgent"));
tags.add(new Tag("urgent"));
System.out.println("tags in set: " + tags.size());
System.out.println("contains #urgent? " + tags.contains(new Tag("urgent")));
```

*Run it: size 2 — the "duplicate" got in. Contains: false — the set can't find what's plainly inside it.*

Both results are the same disease: without `equals`/`hashCode`, two `Tag("urgent")` objects are strangers — different hash buckets, never compared equal. The set is functioning perfectly on broken inputs. The fix, and the modern idiom for it, is one line — replace the whole class body:

```java
public record Tag(String name) { }
```

*A record generates correct `equals`, `hashCode`, and `toString` from its components — the hash-collection contract satisfied by construction.*

(Call sites change from `getName()` to `name()`; adjust if you used it.) Re-run: size 1, contains true. The rule this stamps in: **anything used as a hash key or set element needs a real `equals`/`hashCode` pair — and records give it for free, which is one more reason they're the default for value types.**

---

## Part 6 — Immutable collections

At **`[MARKER 6]`**, the factories and their teeth:

```java
var stopWords = java.util.List.of("the", "and", "a");
var limits = java.util.Map.of("maxSkus", 500, "maxTags", 20);
System.out.println(stopWords + " " + limits);
stopWords.add("or");
```

*Run it: `UnsupportedOperationException` on the `add` — these collections are deeply unmodifiable, and they say so at runtime.*

Wrap the `add` in a comment once you've seen the exception. `List.of`/`Map.of` are for fixed data (constants, config, test fixtures) and `List.copyOf` is the defensive-copy tool from the Fundamentals getters lesson — the same immutable-by-default instinct, at collection scale. Finish by using one: filter the frequency printout to skip `stopWords` entries.

---

## Exercises

1. **Inventory operations, structure per job.** An inventory module needs: (a) SKU → quantity lookups, (b) an ordered receiving log of SKUs as they arrive (duplicates meaningful), (c) the set of SKUs ever flagged by audit, (d) SKUs in alphabetical order for a nightly report, (e) a fixed list of the four warehouse zone names. Declare all five (interface types on the left!), justify each choice in a one-line comment, and write a few operations against each.

2. **Top three.** From the frequency map, print the three most common non-stop-words with their counts. No streams yet — do it with collections tools (hint: entries can go into a list, and lists sort with a comparator... which you built in the last lab).

3. **Decision drill.** For each scenario, name the structure and the one property that decides it: unique visitor IDs; undo history; leaderboard by score; country-code → dialing-prefix; recently-used files (most recent first, no duplicates); words of a sentence, in order; task queue processed first-come-first-served; the DNA bases {A,C,G,T}.

4. **Break it like Part 5, differently.** Demonstrate the *mutable key* version of the hash trap: use a mutable object as a `HashMap` key, mutate the field after insertion, then show the lookup failing. One comment: why did the fix from Part 5 (equals/hashCode) not save us here, and what design rule does?
