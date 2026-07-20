# Functional Interfaces

Java's functional style rests on a single structural idea: an interface with **exactly one abstract method** can stand for a *function* — and any expression that provides that one method's behavior can be treated as a value of that interface. Such interfaces are called **functional interfaces**, and they're the type system underneath everything in this topic: lambdas target them, `Optional` and Streams consume them, and the JDK ships a standard kit of them ready to use.

---

## One Abstract Method = One Function Shape

We've already met several without the label. `Comparator<T>` has one abstract method (`compare`); `Runnable` has one (`run`); `Iterable` has one (`iterator`). Each nails down a *shape of behavior* — takes what, returns what — while saying nothing about the logic:

```java
@FunctionalInterface
public interface Validator {
    boolean check(String input);          // the single abstract method (SAM)
}
```

*A functional interface: one abstract method defining one function shape — String in, boolean out.*

The **`@FunctionalInterface`** annotation (from the Annotations lesson) makes the intent compiler-enforced: add a second abstract method and compilation fails. It's optional — any single-abstract-method interface qualifies structurally — but standard practice on purpose-built ones. (`default` and `static` methods don't count against the limit; `Comparator` carries a dozen and remains functional.)

Why this matters: with the shape pinned down, an *implementation* doesn't need a whole class. Historically it took an **anonymous class**; since Java 8, a **lambda expression** supplies just the body:

```java
Validator notBlank = new Validator() {            // the old way: anonymous class
    @Override
    public boolean check(String input) {
        return input != null && !input.isBlank();
    }
};

Validator notBlank2 = input -> input != null && !input.isBlank();   // the lambda way
```

*Same object, two spellings — the lambda is nothing but the abstract method's parameters and body.*

Lambda syntax gets its own lesson (003); for now, read `x -> expression` as "the function that maps `x` to that expression." What deserves attention here is the *type system* half: a lambda has no type of its own — it takes on whatever functional interface it's assigned to. That's why these interfaces exist.

---

## The Standard Kit: java.util.function

Rather than every library inventing its own `Validator`, the JDK standardizes the common shapes in **`java.util.function`**. Six cover nearly everything:

| Interface | Method | Shape | Typical use |
|---|---|---|---|
| `Predicate<T>` | `test` | T → boolean | filtering, matching |
| `Function<T,R>` | `apply` | T → R | transforming |
| `Consumer<T>` | `accept` | T → void | side effects: print, save |
| `Supplier<T>` | `get` | () → T | lazy/deferred creation |
| `UnaryOperator<T>` | `apply` | T → T | in-kind transforms |
| `BiFunction<T,U,R>` | `apply` | (T,U) → R | two-argument transforms |

*The core six — memorize `Predicate`, `Function`, `Consumer`, `Supplier` cold; the rest are variations.*

```java
Predicate<String> isEmail = s -> s.contains("@");
Function<Invoice, Double> amount = inv -> inv.getAmount();
Consumer<String> log = msg -> System.out.println("[LOG] " + msg);
Supplier<List<String>> freshList = () -> new ArrayList<>();

isEmail.test("ada@example.com");     // true
log.accept("starting");              // prints
```

*Each kit interface in action: declare the shape, assign a lambda, invoke the method.*

Most shapes also have primitive specializations (`IntPredicate`, `ToDoubleFunction`...) that skip boxing — they'll surface in Streams. And our custom `Validator` above? Real code would just use `Predicate<String>` — define a new functional interface only when the domain name earns its keep (as `Comparator` does).

---

## Behavior as a Parameter

The design revolution hiding in all this plumbing: **methods can now take behavior as an argument**, cleanly. Compare a method that filters actives, another that filters overdue, another that filters by customer — versus one method that takes the criterion itself:

```java
static List<Invoice> select(List<Invoice> all, Predicate<Invoice> criterion) {
    List<Invoice> out = new ArrayList<>();
    for (Invoice inv : all) {
        if (criterion.test(inv)) out.add(inv);       // caller-supplied logic, invoked here
    }
    return out;
}

List<Invoice> overdue = select(invoices, inv -> inv.isOverdue());
List<Invoice> large = select(invoices, inv -> inv.getAmount() > 10_000);
```

*One loop, infinite criteria: the varying logic arrives as a `Predicate` argument.*

We've been consuming this pattern all along — `removeIf(Predicate)`, `sort(Comparator)` — and the standard kit's interfaces even *compose*: `isEmail.negate()`, `p1.and(p2)`, `f1.andThen(f2)` build new functions from old (those are the default methods earning their keep). The rest of the topic is this idea industrialized: `Optional` (next) threads `Function`s and `Supplier`s through absent-value handling, and Streams (lesson 4) chain `Predicate`–`Function`–`Consumer` pipelines across whole collections.
