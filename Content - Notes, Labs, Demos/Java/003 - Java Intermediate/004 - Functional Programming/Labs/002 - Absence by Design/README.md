# Lab: Absence by Design

In this lab we take a user directory whose finders return `null` — and whose callers crash three lines away from the actual mistake — and refactor absence into the type system with `Optional`: honest finder signatures, the full consumption vocabulary (`orElse`, `orElseGet`, `orElseThrow`, `ifPresentOrElse`), and pipelines with `map`/`filter`/`flatMap` that handle a whole chain of maybes in one readable expression.

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

Starters: [User.java](src/main/java/com/curriculum/labs/User.java) (note the nullable `manager` field — real, legitimate absence), [UserDirectory.java](src/main/java/com/curriculum/labs/UserDirectory.java), and [AbsenceLab.java](src/main/java/com/curriculum/labs/AbsenceLab.java).

---

## Part 1 — The crash, and where it *isn't*

Run the starter:

```
grace reports to ada
looked up: null
emailing ...NullPointerException
```

Read the trace and note the line it blames — the `getEmail()` call. But the *mistake* happened earlier: `findByName("casper")` returned null, and the code sailed on. In this toy the gap is two lines; in production it's two modules — a null born in the data layer detonates in a template renderer, and the trace points at the victim, not the culprit. Notice also that the middle line printed `looked up: null` — null even *survived a println*. Nothing about `User findByName(String)` warns that absence is possible. That signature is the bug.

---

## Part 2 — The honest signature

Change the finder to say what it means:

```java
public java.util.Optional<User> findByName(String name) {
    return java.util.Optional.ofNullable(byName.get(name));
}
```

*`ofNullable` converts the map's null-for-missing into an empty Optional — the null is contained at its source, and the return type now announces "may be absent."*

Compile — and enjoy the wreckage: every call site in `AbsenceLab` is now a compile error. This is the refactor working as designed: **the compiler is walking us to every place that was ignoring the possibility of absence.** Fix the two starter lookups minimally for now:

```java
User grace = dir.findByName("grace").orElseThrow();
System.out.println(grace.getName() + " reports to " + grace.getManager().getName());

System.out.println("casper in directory? " + dir.findByName("casper").isPresent());
```

*`orElseThrow()` with no arguments: "I assert this exists — fail here, loudly, if not." The right call when absence would be a data bug, and infinitely better than the drifting NPE: the failure now happens at the lookup, named after it.*

Delete the two dead `ghost` lines. Run — no crash, and the program *says* casper is absent instead of tripping over him.

---

## Part 3 — Four ways out

At **`[MARKER 1]`**, the consumption vocabulary, one intent per idiom:

```java
// 1. Substitute a default
String email = dir.findByName("casper").map(User::getEmail).orElse("nobody@example.com");

// 2. Lazy default — built only when needed
User onCall = dir.findByName("casper").orElseGet(() -> {
    System.out.println("(paging the fallback roster — expensive!)");
    return new User("fallback", "ops@example.com", null);
});

// 3. Escalate with a domain exception
User lead = dir.findByName("grace")
        .orElseThrow(() -> new UserNotFoundException("grace"));

// 4. Act on presence/absence without extracting
dir.findByName("edsger").ifPresentOrElse(
        u -> System.out.println("found " + u),
        () -> System.out.println("no such user"));
```

*Default, lazy default, throw, act — each names its absence policy at the call site. The Supplier in #2 is Behavior as Values paying off: run #2 with a name that exists and the "expensive" line never prints.*

For #3, create `UserNotFoundException` — the three-ingredient style from Design the Failure, one topic over: extends `RuntimeException`, message built in the constructor (`"no user named: " + name`), the name kept as a field. Same pattern, new domain — that's the point of patterns.

---

## Part 4 — Pipelines over maybes

The starter's `grace.getManager().getName()` only worked because grace *has* a manager — run it for `ada` (whose manager is null) and the NPE returns. The fix starts in `User`: absence should be typed at its source. Replace `getManager` with:

```java
public java.util.Optional<User> manager() {
    return java.util.Optional.ofNullable(manager);
}
```

*The nullable field stays (fields shouldn't be Optional — the notes' rule); the accessor translates at the boundary.*

Now, at **`[MARKER 2]`**, the question "who is X's manager's manager?" — first, the shape we're escaping, as a comment:

```java
// User u = dir.findByName("edsger").orElse(null);
// String answer = "(none)";
// if (u != null) {
//     User m = u.getManager();
//     if (m != null) {
//         User mm = m.getManager();
//         if (mm != null) { answer = mm.getName(); }
//     }
// }
```

Then the pipeline that replaces it:

```java
String skipLevel = dir.findByName("edsger")
        .flatMap(User::manager)             // Optional<User> → flatMap, not map
        .flatMap(User::manager)
        .map(User::getName)                 // plain value → map
        .orElse("(none)");
System.out.println("edsger's skip-level: " + skipLevel);
```

*Each step transforms only if something's there; absence anywhere short-circuits to the `orElse`. `flatMap` because `manager()` itself returns an Optional — `map` would nest them.*

Run for `edsger` (→ `ada`), then for `grace` (chain ends early → `(none)`), then for `casper` (absent from step one → `(none)`). Three absence scenarios, zero branches, one policy at the end. Add a `.filter(m -> !m.getName().equals("ada"))` step and predict each result before running.

---

## Part 5 — The anti-pattern, retired

At **`[MARKER 3]`**, type the shape everyone writes in their first Optional week:

```java
var found = dir.findByName("barbara");
if (found.isPresent()) {
    System.out.println("email: " + found.get().getEmail());
} else {
    System.out.println("no email on file");
}
```

*Legal — and it's the old null-check wearing a costume, with `get()` as a loaded gun for whoever edits the block later.*

Refactor it to `ifPresentOrElse` (or a `map(...).orElse(...)` chain — either removes the `get()`). The greppable rule for code review: **`isPresent()` followed by `get()` is always rewritable, and the rewrite is always safer.**

---

## Exercises

1. **Nested to chain.** Convert this (write it out first, then refactor): a method that takes a name and returns the *email domain of that user's manager*, defaulting to `"none"` — the null-checked version needs three nested ifs and a `substring`; the pipeline needs none.

2. **API design call.** For each signature, keep it or change it, one sentence each:
   a. `Optional<User> findByEmail(String email)`
   b. `List<Optional<User>> findAllByTeam(String team)`
   c. `void notify(Optional<String> message)`
   d. a `private Optional<User> manager` field in `User`
   e. `Optional<List<User>> directReports(User u)` (empty list vs. empty Optional — which absence is this?)

3. **Safe parse.** Write `OptionalInt parsePort(String s)` — empty for null/blank/non-numeric/out-of-range(1–65535) input, present otherwise. Use it to read a port with a default of 8080. No exceptions escape, no sentinels return.

4. **Find the lie.** This compiles and "works": `Optional.of(dir.findByName(name).orElse(null))` — explain what it does, why it defeats the entire purpose, and what one-token change fixes it.
