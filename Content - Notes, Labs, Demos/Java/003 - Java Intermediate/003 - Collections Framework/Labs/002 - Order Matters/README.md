# Lab: Order Matters

In this lab we give a product catalog every kind of ordering Java offers: a natural order via `Comparable`, à-la-carte orderings via `Comparator` factories (`comparing`, `thenComparing`, `reversed`), the algorithms that consume them (`sort`, `max`, `binarySearch` — including what happens when we feed `binarySearch` unsorted data), and a `TreeSet` that stays permanently sorted — and quietly *eats an element*, teaching the comparator-as-membership rule the hard way.

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

Starters: [Product.java](src/main/java/com/curriculum/labs/Product.java), [Catalog.java](src/main/java/com/curriculum/labs/Catalog.java) (note its comment about a shared price — that's a Part 4 landmine, planted), and [SortLab.java](src/main/java/com/curriculum/labs/SortLab.java). The driver's `show` helper uses `forEach(System.out::println)` — a method reference; read it as "print each one," with the full syntax story coming in Functional Programming.

---

## Part 1 — Natural ordering: Comparable

At **`[MARKER 1]`**, try to sort the obvious way:

```java
java.util.Collections.sort(products);
```

*It doesn't compile: "no suitable method found" — the compiler has no idea what order products go in, and refuses to guess.*

The fix is teaching `Product` its **natural ordering**. SKUs are unique and stable — the right canonical key. In `Product.java`, change the class header to `implements Comparable<Product>` and add:

```java
@Override
public int compareTo(Product other) {
    return this.sku.compareTo(other.sku);
}
```

*Natural order delegated to String's own ordering — negative/zero/positive, exactly the contract `sort` needs.*

Now the sort compiles. Follow it with `show("by SKU (natural)", products);` and run: EL-045 to TL-300, alphabetical by SKU. One method opted `Product` into every ordering-aware API in the JDK — which the rest of this lab tours.

---

## Part 2 — Comparator: every other order

Natural order answers "the one canonical way." Everything else is a `Comparator`, built with the factory methods. At **`[MARKER 2]`**, three views of the same data:

```java
var byPriceDesc = java.util.Comparator.comparing(Product::getPrice).reversed();
var byCategoryThenPrice = java.util.Comparator.comparing(Product::getCategory)
                                              .thenComparing(Product::getPrice);
var byName = java.util.Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);

products.sort(byPriceDesc);          show("price, high to low", products);
products.sort(byCategoryThenPrice);  show("category, then price", products);
products.sort(byName);               show("name, A to Z", products);
```

*The factory chain: `comparing` extracts the key, `reversed` flips, `thenComparing` breaks ties — declarative orderings, no arithmetic to get wrong.*

Run and inspect the middle view closely: within `tools`, prices ascend — the tie-break doing its job. Also worth noticing what we *didn't* write: the notes' warning about hand-rolled `(int) (a.price - b.price)` subtraction bugs doesn't apply, because `comparing` handles the comparison arithmetic itself. The habit: **factories, not subtraction.**

---

## Part 3 — Ordering's consumers: max and binarySearch

Sorting is the visible consumer; two more at **`[MARKER 3]`**:

```java
Product priciest = java.util.Collections.max(products, java.util.Comparator.comparing(Product::getPrice));
System.out.println("priciest: " + priciest + "\n");
```

*`max` with an explicit comparator — no sort required; it scans once.*

Then `binarySearch`, which comes with a contract written in fine print: **the list must already be sorted in the same order the search assumes.** Honor it first:

```java
java.util.Collections.sort(products);                       // natural order restored
Product probe = new Product("PL-220", "", "", 0);           // only the SKU matters to compareTo
int at = java.util.Collections.binarySearch(products, probe);
System.out.println("PL-220 found at index " + at);
```

*Binary search against natural order: the probe object exists only to carry the key our `compareTo` reads.*

Now break the contract on purpose: re-sort with `byPriceDesc`, run the same natural-order search again, and print the result. Sometimes a negative "not found," sometimes a *wrong index* — silently. No exception, no warning: `binarySearch` trusted the ordering and halved its way to garbage. That's why the sorted-input requirement is a contract, not a suggestion — and why tests for search code always include an unsorted-input case.

---

## Part 4 — TreeSet: permanently sorted, and strict about it

A `TreeSet` keeps elements sorted *at all times* — insert in any order, iterate in comparator order. At **`[MARKER 4]`**:

```java
var shelf = new java.util.TreeSet<Product>(byCategoryThenPrice);
shelf.addAll(Catalog.sample());
System.out.println("--- TreeSet, category then price ---");
shelf.forEach(System.out::println);
System.out.println("list had " + Catalog.sample().size() + ", set has " + shelf.size());
```

*A sorted set: no sort calls anywhere, yet iteration comes out ordered.*

Run it — and read that last line. **Seven in, six out.** A product vanished. Which one? Compare the printout against the catalog: the Torque Wrench and Socket Set are both `tools` at `89.00` — identical under `byCategoryThenPrice` — and a `TreeSet` uses its comparator, not `equals`, to decide membership. Compare-equal means *same element*; the second arrival was "already present," silently dropped.

The fix is making the comparator total — break the final tie with the unique key:

```java
var shelfOrder = byCategoryThenPrice.thenComparing(Product::getSku);
```

*Every distinct product now compares distinct — rebuild the TreeSet with this and all seven survive.*

Do it and confirm the count. The rule this burns in: **an ordering used for membership must distinguish everything `equals` distinguishes** — the consistency-with-equals warning from the notes, experienced instead of memorized.

---

## Exercises

1. **From prose to chain.** Implement this spec as one comparator and sort with it: "electrical products first, then plumbing, then tools; within a category, cheapest first; equal prices resolved by SKU." (The category clause is *not* alphabetical — think about what `comparing` needs to extract.)

2. **The broken comparator.** Write `Comparator.comparing(p -> p.getName().length())`, sort with it, and answer in comments: which products can swap positions between runs and why? Then state what extra clause makes it deterministic, and add it.

3. **Stability, observed.** Sort the catalog by price ascending, *then* sort by category. Look at the tools' internal order in the final result and explain what the second sort preserved and why (the notes call this property by name). Then produce the same final ordering with a single `thenComparing` chain and say which approach a reader should prefer.

4. **Reverse-engineer the ordering.** Given this output order — `Socket Set, Torque Wrench, Multimeter, Pipe Cutter, Claw Hammer, Basin Wrench, Wire Stripper` — reconstruct the comparator that produced it, implement it, and verify. (Hint: look at prices before you look at names.)
