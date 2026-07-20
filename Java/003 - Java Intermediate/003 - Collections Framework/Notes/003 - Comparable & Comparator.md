# Comparable & Comparator

Sorting requires an answer to one question, asked repeatedly: *of these two elements, which comes first?* Java splits the answer across two interfaces. **`Comparable`** lets a class declare its own **natural ordering** — the one obvious way its instances line up. **`Comparator`** packages an ordering as a separate object — for types we can't modify, or for the second, third, and tenth ways to sort the same data. Between them, every sorted structure in the framework gets its ordering.

---

## Comparable: The Natural Ordering

A class implements **`Comparable<T>`** by providing `compareTo`, returning the sign convention we met with `String.compareTo` in Unit 2 — negative for "this first," zero for "tie," positive for "other first":

```java
public class Invoice implements Comparable<Invoice> {
    private final String id;
    private final double amount;

    @Override
    public int compareTo(Invoice other) {
        return Double.compare(this.amount, other.amount);    // natural order: by amount, ascending
    }
}

List<Invoice> invoices = new ArrayList<>(loadInvoices());
Collections.sort(invoices);                    // no ordering passed — uses compareTo
```

*Implementing `Comparable` plugs `Invoice` into every sort, search, and sorted collection in the JDK.*

Delegate the arithmetic to the static helpers — `Integer.compare`, `Double.compare`, `String`'s own `compareTo` — rather than the tempting `(int) (this.amount - other.amount)`, which overflows and truncates its way to wrong answers. Multi-field orderings chain: compare the primary field, and only on a tie (`result == 0`) compare the next.

`String`, all the numeric wrappers, enums (by declaration order), and dates all ship `Comparable` — why `Collections.sort` works on them out of the box. Reserve `Comparable` for orderings that are genuinely *the* canonical one (amounts, timestamps, IDs); a debatable choice baked into the class misleads everyone. And keep `compareTo` **consistent with `equals`** (zero exactly when `equals` is true) — sorted collections misbehave subtly otherwise.

---

## Comparator: Orderings à la Carte

A **`Comparator<T>`** externalizes the same two-argument question into its own object — definable anywhere, passable to anything:

```java
Comparator<Invoice> byId = Comparator.comparing(Invoice::getId);
Comparator<Invoice> byAmountDesc = Comparator.comparing(Invoice::getAmount).reversed();
Comparator<Invoice> byCustomerThenAmount =
        Comparator.comparing(Invoice::getCustomer)
                  .thenComparing(Invoice::getAmount);

invoices.sort(byAmountDesc);                       // List.sort takes a Comparator
invoices.sort(byCustomerThenAmount);               // same data, different day, different order
```

*The factory-method style: `comparing` extracts a sort key, `reversed` flips, `thenComparing` breaks ties.*

The `Invoice::getAmount` tokens are **method references** — functional-programming syntax formally introduced next topic; read them for now as "the getAmount getter, as a value." The old style — anonymous classes implementing `compare(a, b)` by hand — survives in legacy code, but the factory chain above is standard today: declarative, composable, and hard to get wrong. Useful extras: `Comparator.naturalOrder()`, `nullsFirst(...)`/`nullsLast(...)` for null-tolerant sorts, and `comparingInt`/`comparingDouble` to skip boxing.

**Choosing is simple:** the one canonical order → `Comparable`, on the class. Everything else — alternate orders, orders over library types, orders that depend on context — → a `Comparator` at the call site.

---

## Where Orderings Get Used

Sorting a list is the visible case, with two spellings (`invoices.sort(cmp)` preferred; `Collections.sort(list)` for natural order). The same two interfaces also power:

```java
Invoice biggest = Collections.max(invoices, byAmountDesc.reversed());
int pos = Collections.binarySearch(sortedInvoices, target);        // needs natural-order sorted input

TreeSet<Invoice> ledger = new TreeSet<>(byCustomerThenAmount);     // stays sorted as elements arrive
```

*`max`/`min`, binary search, and the sorted collections all consume the same ordering contracts.*

`TreeSet` and `TreeMap` — properly introduced next lesson — keep elements *permanently* sorted, and take their ordering the same two ways: elements are `Comparable`, or a `Comparator` is handed to the constructor. One warning worth pre-issuing: those trees use the ordering *instead of* `equals` for membership, which is where an inconsistent `compareTo` turns into elements that vanish or duplicate mysteriously.

Sorting stability, as a closing detail: Java's list sort is **stable** — equal elements keep their relative order — which is why sorting by amount *then* by customer produces customer-grouped, amount-ordered data. Deliberate multi-key sorts still belong in one `thenComparing` chain, where the intent is visible.

With ordering handled, the framework's main event: the collections themselves.
