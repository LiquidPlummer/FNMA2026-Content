# Java Intermediate — Labs Outline

Planning document for the `Labs/` modality across this unit. Same conventions as the Java Fundamentals labs outline: each lab is a self-contained Maven project (Java 21, quickstart scaffold, `README.md` with guided walkthrough + unguided exercises) at `<Topic>/Labs/<XXX - Name>/`. Labs cover lesson-level ideas; conceptual lessons fold into the labs where their skills actually get exercised.

**Status:** all planned, none produced yet. Labs in this unit assume the Fundamentals labs (or equivalent comfort) — several starters deliberately reuse that unit's domains.

---

## 001 - Exception Handling

### Lab 001 - Catch, Clean Up, Carry On
**Covers:** Try-catch & Resources (Checked vs. Unchecked woven in).
**Shape:** A "data file importer" that reads a records file with deliberately dirty rows. Guided: first run crashes raw — read the trace; add a targeted `catch` for the parse failure (skip the bad row, count it); provoke the unreachable-catch compile error by misordering specific/general; multi-catch two equivalent failures; convert the file handling to try-with-resources and *prove* the close happens by breaking mid-file; finish with the swallowed-exception anti-pattern presented as a planted bug the student must find and fix. Exercises: a retry-3-times wrapper for a flaky operation, wrap a checked `IOException` into an unchecked exception with the cause attached (verify the full chain prints), and a "which catch block is wrong and why" review set.
**Flow note:** Checked vs. Unchecked gets no standalone lab — its content is decision-making, exercised here (handle-or-declare mechanics) and in Lab 002 (choosing a parent class).

### Lab 002 - Design the Failure
**Covers:** Custom Exceptions.
**Shape:** A small inventory-reservation module with stringly errors (`return null` and `-1` sentinels) as the starter. Guided: introduce a domain family — `InventoryException` root plus two concrete types with message-building constructors, cause overloads, and a machine-readable field (the SKU); replace the sentinels with throws; write the boundary translation (a fake low-level `StorageFailure` wrapped into domain terms); demonstrate catching at two precisions (one concrete type vs. the family root). Exercises: design the exception set for a given ticketing spec (justify checked vs. unchecked per type), add a handler that *uses* the carried field, and a critique exercise on an over-engineered six-level exception hierarchy.

---

## 002 - Object-Oriented Programming

### Lab 001 - Hierarchy & Dispatch
**Covers:** Inheritance, Polymorphism (The 4 Pillars woven in).
**Shape:** A payroll system: `Employee` base, `SalariedEmployee`/`HourlyEmployee`/`ContractEmployee` subclasses. Guided: build the base with protected-vs-private decisions made explicit; `extends` with `super(...)` constructor chaining; override `monthlyPay()` and `toString()` with `@Override` (and one deliberate typo'd override caught by the annotation); the payroll loop over `Employee[]` dispatching polymorphically; an `instanceof`-chain version shown *first*, then deleted in favor of dispatch — the diff is the lesson; downcasting done once, legitimately, with pattern matching. Exercises: add a fourth employee type without touching the payroll loop (open/closed in practice), a composition-over-inheritance judgment case (`AuditedEmployee` — wrap, don't extend), and equals/hashCode for one class.
**Flow note:** The 4 Pillars is an overview lesson — no lab; its vocabulary gets used throughout both OOP labs.

### Lab 002 - Program to the Contract
**Covers:** Interfaces & Abstract Classes.
**Shape:** An export subsystem: reports must go to CSV, JSON, and (later) XML. Guided: define an `Exporter` interface; two implementations; the client method typed against the interface — then swap implementations with a one-line change; add a `default` method built on the abstract ones; then the abstract-class half: a `Report` skeleton with a `final` template method and an abstract `body()` step, subclassed twice. Ends with the choosing table from the notes, filled in against this lab's own classes. Exercises: retrofit a `Describable` interface onto two unrelated existing classes, implement a third exporter against the contract *without reading the other two*, and three design scenarios where the student picks interface vs. abstract class and defends it.

---

## 003 - Collections Framework

### Lab 001 - Generic Containers
**Covers:** Generics, Iterable & Iterators.
**Shape:** Build a small type-safe `Playlist<T>` container. Guided: start from a raw-`Object` version that compiles-but-casts (recreate the pre-generics world and its runtime failure); genericize it — `<T>`, typed `add`/`get`, the diamond; a bounded generic method (`<T extends Comparable<T>> T max(...)`); implement `Iterable<T>` by delegation so for-each works on our own type; provoke `ConcurrentModificationException` with a mid-loop remove, fix with `removeIf` and with an explicit iterator. Exercises: a generic `Pair<A,B>`, a bounded `sum` over `Number` lists, and a wildcard-signature fix-up exercise (`List<Truck>` into a `List<? extends Vehicle>` parameter).

### Lab 002 - Order Matters
**Covers:** Comparable & Comparator.
**Shape:** Sorting a product catalog. Guided: implement `Comparable` (natural order by SKU) and use `Collections.sort`/`list.sort(null)`; then the comparator factories — `comparing`, `thenComparing`, `reversed`, `nullsLast` — building three different catalog views over the same data; `Collections.max` and `binarySearch` with the sorted-input requirement demonstrated (search an unsorted list, get garbage, explain); a `TreeSet` with a comparator as the closer. Exercises: multi-key sort from a prose spec ("by category, then price descending, ties by name"), a comparator that would violate consistency-with-equals and what breaks, sort stability observed with a two-pass sort.

### Lab 003 - Choose Your Structure
**Covers:** Collections API.
**Shape:** A word/frequency and dedupe toolkit over a text file — tasks chosen so each structure is the *obviously right* one in turn. Guided: `ArrayList` accumulation; `HashSet` dedupe with the same data (size difference tells the story); word-frequency `HashMap` with `merge`/`getOrDefault`; iteration order chaos with `HashMap` vs `LinkedHashMap` vs `TreeMap` printed side by side; the equals/hashCode trap — a small class used as a map key *without* the pair, elements vanishing, then fixed; immutable factories (`List.of`, `Map.copyOf`) and the `UnsupportedOperationException` proof. Exercises: an inventory system's five operations each mapped to a structure (with justification), top-N most frequent words, a "which collection?" decision drill of eight scenarios.

---

## 004 - Functional Programming

### Lab 001 - Behavior as Values
**Covers:** Functional Interfaces, Lambda Expressions.
**Shape:** Refactor an anonymous-class-riddled event/filter module into lambdas — the historical progression compressed into one sitting. Guided: rewrite three anonymous classes as lambdas step by step (full form → inferred → single-expression); tour the standard kit by *using* each shape — `Predicate` into a `select(...)` method, `Consumer` into `forEach`, `Supplier` for lazy defaults, `Function` for a transform; compose with `and`/`negate`/`andThen`; convert eligible lambdas to method references (all four kinds, with one that *shouldn't* convert); close with effectively-final demonstrated by provoking the capture compile error. Exercises: write the target functional interface from a lambda's shape, a mini rules-engine where rules are `Predicate<Order>` values in a list, replace-this-loop-with-removeIf drills.
**Flow note:** Covers outline lessons 001 and 003 together — the interface and the syntax are one idea in practice. Optionals (lesson 002) follows as Lab 002 without needing full lambda fluency first.

### Lab 002 - Absence by Design
**Covers:** Optionals.
**Shape:** A user-directory service whose starter returns `null`s and throws NPEs three calls downstream (reproduce the crash first — the motivation is the stack trace). Guided: convert finders to `Optional<User>` returns via `ofNullable`; consume with `orElse`, `orElseGet` (lazy default demonstrated with a print in the supplier), `orElseThrow` with a domain exception, `ifPresentOrElse`; then the pipeline — `map`/`filter`/`flatMap` over a `findUser(id).flatMap(User::manager)` chain vs. its nested-null-check equivalent shown side by side; the `isPresent()/get()` anti-pattern refactored away. Exercises: convert a three-level nested null-check method to one chain, an API-design exercise (which of five signatures should return Optional — including the field/parameter cases where the answer is no), a safe-parse `OptionalInt` utility.

### Lab 003 - Pipelines
**Covers:** Streams API.
**Shape:** An orders dataset (a provided list of ~50 records — seeded with edge cases) queried eight ways. Guided: the first pipeline built operator by operator with a `peek` to watch laziness; `filter`/`map`/`sorted`/`distinct`/`limit`; terminals in families — `toList`, `count`, `anyMatch`, `findFirst` (returning Optional — Lab 002 pays off), `mapToDouble().sum()` and `summaryStatistics()`; `Collectors.groupingBy` and `joining` for a report; one loop deliberately *kept* as a loop with the reasoning written out; the single-use stream error provoked once. Exercises: five prose queries to implement as pipelines ("total revenue per customer, top three"), one pipeline to rewrite as a loop and one loop as a pipeline (judgment both directions), a mutation-inside-a-lambda bug to find and fix.

---

## Summary

| Topic | Labs planned | Lessons folded into other labs |
|---|---|---|
| 001 - Exception Handling | 2 | Checked vs. Unchecked (woven into both) |
| 002 - Object-Oriented Programming | 2 | The 4 Pillars (vocabulary throughout) |
| 003 - Collections Framework | 3 | — |
| 004 - Functional Programming | 3 | — |

**Total: 10 labs.** Suggested production order: top-to-bottom. Cross-lab threads to preserve when producing: the exception family from Exception Handling Lab 002 reappears as the thrown type in Optionals Lab 002 (`orElseThrow`); the catalog/orders domains recur from Collections into Streams so students query data they already understand. All labs stay on the plain Maven quickstart scaffold — no new dependencies needed anywhere in this unit.
