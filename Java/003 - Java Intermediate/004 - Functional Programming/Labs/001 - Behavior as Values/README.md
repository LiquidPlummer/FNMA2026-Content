# Lab: Behavior as Values

In this lab we compress a decade of Java history into one sitting: three anonymous classes become three lambdas, a home-grown functional interface retires in favor of the standard kit (`Predicate`, `Function`, `Consumer`, `Supplier`), behaviors compose with `and`/`negate`/`andThen`, forwarding lambdas become method references, and the effectively-final rule gets provoked into showing itself.

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

Starters: [BehaviorLab.java](src/main/java/com/curriculum/labs/BehaviorLab.java) (three anonymous classes, marked as the "before" picture), [Order.java](src/main/java/com/curriculum/labs/Order.java) (a record with sample data), and [OrderCheck.java](src/main/java/com/curriculum/labs/OrderCheck.java) (our own functional interface — briefly).

---

## Part 1 — Three rewrites, decreasing ceremony

Run the starter and skim the output, then look at what it cost: ~20 lines of anonymous-class scaffolding wrapping perhaps 3 lines of actual logic. Each anonymous class implements an interface with **one abstract method** — which is precisely the condition that lets a lambda replace it. Rewrite each in place, feeling the compression stages:

```java
// #1 — full lambda form first...
OrderCheck bigOrder = (Order order) -> { return order.amount() > 1000; };
// ...then let inference and the single-expression rule shrink it:
OrderCheck bigOrder = order -> order.amount() > 1000;

// #2 — two parameters keep their parentheses:
Comparator<Order> byAmount = (a, b) -> Double.compare(a.amount(), b.amount());

// #3 — no parameters, empty parens required:
Runnable banner = () -> System.out.println("=== Order Report ===");
```

*Same three objects, same behavior — the lambda is nothing but the abstract method's parameters and body. Types flow in from the target: `OrderCheck` tells the compiler what `order` is.*

Run again — identical output, a screen less code. While here, add `@FunctionalInterface` to `OrderCheck` — then try adding a second abstract method to it and read the error. The annotation turns "happens to be lambda-compatible" into an enforced promise.

---

## Part 2 — Retire the home-brew; behavior becomes a parameter

`OrderCheck` taught us the shape, but the JDK already ships it: `java.util.function.Predicate<Order>` — same contract, method named `test`, universally understood. The bigger move is *accepting behavior as a parameter*. At **`[MARKER 5]`**:

```java
static List<Order> select(List<Order> all, java.util.function.Predicate<Order> criterion) {
    List<Order> out = new ArrayList<>();
    for (Order o : all) {
        if (criterion.test(o)) {
            out.add(o);
        }
    }
    return out;
}
```

*One loop, written once — the varying logic arrives from the caller as a Predicate.*

At **`[MARKER 1]`**, put it through its paces:

```java
System.out.println(select(orders, o -> o.rush()));
System.out.println(select(orders, o -> o.customer().equals("Ada Corp")));
System.out.println(select(orders, o -> o.amount() > 1000 && !o.rush()));
```

*Three different selections, zero new loops — each call site reads as its own requirement.*

Convert `bigOrder` to a `Predicate<Order>` too, and delete `OrderCheck.java` entirely (fix the one usage in the report loop: `bigOrder.test(o)` — unchanged, conveniently). The lesson in the deletion: reach for the standard kit first; a custom functional interface needs to earn its name the way `Comparator` does.

---

## Part 3 — The rest of the kit, each shape doing its job

At **`[MARKER 2]`**, one specimen per shape:

```java
java.util.function.Consumer<Order> printer = o -> System.out.println("  -> " + o.id());
orders.forEach(printer);                                     // T -> void: side effects

java.util.function.Function<Order, String> label =
        o -> o.customer() + " owes " + o.amount();           // T -> R: transform
System.out.println(label.apply(orders.get(0)));

java.util.function.Supplier<List<Order>> fallback = () -> {
    System.out.println("(building fallback — expensive!)");
    return List.of(new Order("ORD-0", "House", 0.0, false));
};
List<Order> result = select(orders, o -> o.amount() > 5000);
List<Order> toShip = result.isEmpty() ? fallback.get() : result;
```

*Consumer consumes, Function transforms, Supplier defers — note the fallback's print line only appears when the Supplier is actually invoked. Laziness is the Supplier's whole point.*

Run twice: once as-is (no orders over 5000 — the fallback fires) and once with the threshold at 500 (it doesn't — no "expensive" line). That deferred-cost pattern returns as `orElseGet` in the next lab.

---

## Part 4 — Composition: building predicates from predicates

The kit interfaces carry `default` methods (Program to the Contract, paying off) that combine behaviors. At **`[MARKER 3]`**:

```java
java.util.function.Predicate<Order> big = o -> o.amount() > 1000;
java.util.function.Predicate<Order> rush = Order::rush;          // preview of Part 5

System.out.println(select(orders, big.and(rush)));
System.out.println(select(orders, big.negate()));
System.out.println(select(orders, big.or(rush).negate()));       // neither big nor rush

var loud = label.andThen(String::toUpperCase);                    // Function chaining
System.out.println(loud.apply(orders.get(3)));
```

*Named small predicates, combined declaratively — `big.and(rush)` reads like the requirement it implements, and each piece stays independently testable.*

---

## Part 5 — Method references: the forwarding lambda, named

A lambda that only calls one existing method can be written as the method itself. Convert these across the file, matching each to its kind:

- `o -> System.out.println(o)` → `System.out::println` — *instance method of a particular object*
- `o -> o.rush()` → `Order::rush` — *instance method of the first parameter* (the subtle kind)
- `(a, b) -> Double.compare(a, b)` inside the comparator → better: the whole comparator becomes `Comparator.comparingDouble(Order::amount)` — factory + reference, the Order Matters lab's style
- `() -> new ArrayList<Order>()` (write one somewhere) → `ArrayList::new` — *constructor reference*

And one that should **stay a lambda**: `o -> o.customer().equals("Ada Corp")` — it does two things (navigate, then compare), so no single method reference expresses it. Forcing references where logic lives is compression past the point of readability; the rule is *pure forwarding converts, anything more stays a lambda*.

---

## Part 6 — What lambdas may touch

At **`[MARKER 4]`**, the capture rule, provoked:

```java
int bigCount = 0;
orders.forEach(o -> {
    if (o.amount() > 1000) {
        bigCount++;                    // ← will not compile
    }
});
```

*"local variables referenced from a lambda expression must be final or effectively final" — the lambda captured `bigCount`, and captured locals are frozen.*

Read the error, then fix it the idiomatic way — don't smuggle state through an array; *ask for the answer instead of counting into a box*:

```java
long bigCount = orders.stream().filter(o -> o.amount() > 1000).count();
```

*A one-line preview of the next lab's subject — the counting loop replaced by a pipeline that owns its own accumulation.*

(Or, with only this lab's tools: `int bigCount = select(orders, big).size();` — same spirit.) The urge to mutate a local from inside a lambda is almost always the signal that a better-shaped operation exists.

---

## Exercises

1. **Name that interface.** For each lambda, write the standard-kit type it needs (no code — just the declaration): `s -> s.length()`, `() -> LocalDate.now()`, `(a, b) -> a + b` (two ways!), `x -> {}` with `x` an `Order`, `s -> s.isBlank()`.

2. **Rules engine.** Build `List<Predicate<Order>>` holding three named business rules (e.g., "rush orders under $50 need review"). Write `firstViolation(Order, List<Predicate<Order>>)` returning the index of the first rule that fires, or -1. Then add a rule to the list and confirm no other code changed — behavior stored in a data structure is the lab's idea at full strength.

3. **`removeIf` drills.** Using a mutable copy of the sample data, remove in three separate steps: all non-rush orders under $100; all Byte Barn orders; everything (one predicate, no cheating with `clear()`). Predict the list size before each run.

4. **Comparator, rebuilt.** Reconstruct these Order Matters patterns in this lab's domain, maximally compressed with references: orders by customer then amount descending; orders by id length. One sentence: which of the two deserved to stay more explicit, and why?
