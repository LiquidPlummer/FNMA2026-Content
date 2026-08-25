# Java Fundamentals — Labs Outline

Planning document for the `Labs/` modality across this unit. Each entry below is one lab: a self-contained Maven project (Java 21, quickstart scaffold, `README.md` with guided walkthrough + unguided exercises) living at `<Topic>/Labs/<XXX - Name>/`, per the labs skill conventions. Labs cover lesson-level *ideas*, not necessarily one lab per lesson — small related lessons share a lab; conceptual lessons fold into others rather than getting thin labs of their own.

**Status:** `004 - Control Flow/Labs/001 - Loops` already exists. Everything else here is planned, not yet produced.

---

## 001 - Variables & Types

### Lab 001 - Primitives & Casting
**Covers:** Primitives, Declaration & Initialization, Type Casting (Scope woven in throughout).
**Shape:** A "unit conversion calculator" console app. Guided: declare typed variables with literals (underscores, suffixes, hex), watch integer division and overflow bite, fix them with deliberate casts and `Math.round`; a `final` constants section; a `var` section where students judge when inference reads well. Exercises: temperature/currency conversions that force casting decisions, an overflow-detection challenge, a "predict the output" set on truncation vs. rounding.
**Flow note:** Scope gets no standalone lab — block/loop/method scope shows up naturally here and in every later lab; the walkthrough calls it out where it appears.

### Lab 002 - Wrappers & Parsing
**Covers:** Wrapper Classes.
**Shape:** A "score sheet reader" that parses user-typed numbers into wrappers. Guided: `Integer.parseInt` with bad input, autoboxing into use, the null-unboxing NPE reproduced *on purpose*, then the `==` cache trap demonstrated with 100 vs 1000 — students predict, run, and explain. Exercises: a lenient parser returning sensible defaults, a min/max/average over parsed values, a "find the bug" snippet where `==` passes in test and fails at scale.

---

## 002 - Strings

### Lab 001 - String Workout
**Covers:** Strings, String Pool.
**Shape:** A "text cleanup utility" — normalize messy user input (trim, case, split/join, substring, replace). Guided: build the cleaner method by method; a comparison section proving `==` vs `.equals()` behavior on literals vs. runtime strings (the pool, demonstrated rather than lectured); `String.format` for a report line. Exercises: initials extractor, filename sanitizer, a CSV field splitter that survives empty fields, palindrome check.

### Lab 002 - StringBuilder Performance
**Covers:** StringBuilder & StringBuffer.
**Shape:** Short and pointed. Guided: build a 100k-line report with `+=` concatenation, *time it* (`System.currentTimeMillis`), rebuild with `StringBuilder`, time again — the number gap is the lesson; then the fluent API (insert, reverse, setLength) on a small exercise. Exercises: a receipt builder, reverse-words, and a "which tool?" decision table the student fills in for five scenarios (concat vs builder vs format vs join).

---

## 003 - Arrays

### Lab 001 - Array Fundamentals
**Covers:** Declaring & Initializing, Accessing Elements, Array Length, Iterating Arrays.
**Shape:** A "quiz statistics" app (deliberate continuity with the Loops lab's `QUIZ_SCORES` theme). Guided: declare and literal-initialize arrays, index reads/writes with a deliberately triggered `ArrayIndexOutOfBoundsException` (read the trace, fix the bound), `length` idioms (`length - 1`, guard checks), then the four iteration recipes — aggregate, search, max, transform — choosing for vs. for-each each time. Exercises: reverse in place, second-largest without sorting, histogram bucketing, `Arrays.copyOf`-based "append" helper.

### Lab 002 - Grids
**Covers:** Multi-dimensional Arrays.
**Shape:** A "theater seating chart" console app. Guided: declare a 2D `char` grid, nested-loop render with row/seat labels, book a seat by coordinates (bounds-checked), count free seats per row (`grid[row].length` as the inner bound). Exercises: a jagged "per-month readings" structure, full-row detection, and a rotate-90° stretch goal.

---

## 004 - Control Flow

### Lab 001 - Loops ✅ (exists)
**Covers:** Loops - while, do-while, for, for-each. Menu-driven Loop Lab; iterators mentioned only in passing.

### Lab 002 - Branches
**Covers:** Branches - if-else, switch.
**Shape:** A "support ticket router" — classify tickets by fields. Guided: an if/else-if ladder for priority bands (ranges — where switch can't go), ternary for a two-value label, then a traditional `switch` on category *with a deliberately missing `break`* to watch fall-through corrupt output, then the same logic as an arrow-form switch expression with exhaustiveness. Exercises: grade-band calculator, a leap-year rule (compound boolean logic), rewrite-this-ladder-as-a-switch (and one that legitimately can't be), a nested-condition flattening challenge using guard clauses.
**Flow note:** Jumps - Method Calls gets no standalone lab — it's conceptual (call stack, stack traces). Its practical skills are embedded: stack-trace reading happens in Arrays Lab 001 and Methods Lab 002; guard-clause style appears here and in Methods.

---

## 005 - Methods

### Lab 001 - Extract & Compose
**Covers:** Defining Methods, Calling Methods, Returning.
**Shape:** Refactoring lab — the starter is a deliberately monolithic 80-line `main` (a working tip/bill splitter with everything inline). Guided: extract it method by method — name each behavior, choose parameters and return types, replace duplication with calls, add an overload, finish with a guard-clause early return. The diff between start and end *is* the lesson. Exercises: extract methods from a second monolith (different domain, e.g. a stats reporter), design a method signature from a prose description, add a record-returning "two values" method.

### Lab 002 - Parameters & Failure
**Covers:** Parameters, Throwing and throws.
**Shape:** A small `Validator` utility class built test-first-ish (assert-style checks in `main`). Guided: pass-by-value demonstrated with a primitive and an array (mutate both, observe the asymmetry, explain it); varargs for a `sum(...)`; then fail-fast validation — `IllegalArgumentException` with actionable messages, the `throw` vs `throws` distinction, and reading the resulting stack traces top to bottom. Exercises: an `average` that rejects empty input with a clear message, a defensive method that must NOT mutate its array argument, a "write the error message" exercise judged on diagnosability.

---

## 006 - Classes & Objects

### Lab 001 - First Class
**Covers:** Classes vs Objects, Fields & Constructors, Getters & Setters, The this Keyword.
**Shape:** The unit's centerpiece: build `BankAccount` from scratch (mirroring the notes' running example). Guided: raw public-field struct first — break it deliberately from `main` (negative balance) — then constructor with validation, `this` for shadowed parameters, getters, a deliberate *decision* about which setters not to write, `deposit`/`withdraw` with rules, constructor overloading via `this(...)`. Two-instance independence demonstrated throughout. Exercises: build a `Book`/`LibraryItem` class to a behavior spec, add a transaction-count field, a defensive-copy getter for a mutable field.

### Lab 002 - Access & static
**Covers:** Access Modifiers, The static Keyword.
**Shape:** Continues from Lab 001's `BankAccount` (starter ships the finished Lab 001 state). Guided: lock fields down to `private` and watch exactly which lines of `main` stop compiling (the compiler as encapsulation tutor); add a `static` account counter and auto-generated IDs; a `static final` constants section; a utility method (`isValidId`) that *should* be static — and one that shouldn't, with the "no `this` in static context" error provoked and explained. Exercises: design decisions on a provided class (mark every member public/private/static with justification), a `Temperature` utility class, find-the-shared-state-bug.

### Lab 003 - Enums
**Covers:** Enums.
**Shape:** Refactor a provided string-constant mess: an order tracker using `"PENDING"`/`"PAID"` literals (with a shipped typo bug the strings hide). Guided: replace with an `OrderStatus` enum — the typo becomes a compile error; `values()`/`valueOf` for input; switch-on-enum with exhaustiveness; then enum-with-fields (a display label and a `isTerminal()` method). Exercises: a `Planet`-style constants-with-data enum for another domain (HTTP status codes or T-shirt sizes with measurements), an allowed-transitions method (`canMoveTo(OrderStatus)`).
**Flow note:** Annotations gets no standalone lab — `@Override` appears organically the first time `toString()` is overridden (Lab 001 or 003), and the framework annotations story has no runnable payoff until JUnit/Spring in Java Advanced. The notes lesson carries it alone.

---

## Summary

| Topic | Labs planned | Lessons folded into other labs |
|---|---|---|
| 001 - Variables & Types | 2 | Scope (woven throughout) |
| 002 - Strings | 2 | — |
| 003 - Arrays | 2 | — |
| 004 - Control Flow | 1 (+1 exists) | Jumps - Method Calls (embedded skills) |
| 005 - Methods | 2 | — |
| 006 - Classes & Objects | 3 | Annotations (deferred to Java Advanced labs) |

**Total: 12 new labs** (13 with the existing Loops lab). Suggested production order: follow the outline top-to-bottom, since later labs deliberately reference earlier ones (quiz-scores continuity in Arrays, the `BankAccount` arc across Classes & Objects Labs 001→002). Each lab should stay in the Loops lab's proven format: version table verified against the local toolchain, marker-based guided typing, run-after-every-part, and exercises that force a genuine decision rather than pattern-matching.
