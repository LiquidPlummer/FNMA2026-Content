# Interfaces & Abstract Classes

Polymorphism needs a shared type to dispatch through, but that type doesn't have to be a full class. Java offers two ways to define *partially or entirely abstract* types: the **interface** — a pure capability contract — and the **abstract class** — a half-built class that only subclasses can complete. Between them they carry the abstraction pillar, and choosing correctly between them is a recurring design decision.

---

## Interfaces

An **interface** declares what an implementor can *do*, with no say in how. Methods are implicitly `public` and, classically, body-less:

```java
public interface Payable {
    Receipt pay(double amount);                  // abstract: signature only
    double outstandingBalance();
}

public class Invoice implements Payable {
    @Override
    public Receipt pay(double amount) { ... }    // the class supplies the how

    @Override
    public double outstandingBalance() { ... }
}
```

*`implements` is a promise: `Invoice` provides real bodies for every method `Payable` declares.*

The compiler enforces the promise — implement all methods or don't compile. An interface is a genuine type: variables, parameters, and collections can be declared `Payable`, and dynamic dispatch works exactly as in the last lesson.

Two properties make interfaces the workhorse of Java design. First, a class **implements any number** of them (`class Invoice implements Payable, Printable, Comparable<Invoice>`) — capabilities compose, sidestepping single-inheritance entirely. Second, they're *retrofittable*: unrelated classes from unrelated hierarchies can each implement `Payable`, and code written against `Payable` serves them all. Interfaces also hold no instance state — fields in an interface are implicitly `public static final` constants — which is precisely what keeps multiple "inheritance" of interfaces safe.

Since Java 8, interfaces may carry **`default` methods** — bodies that implementors inherit but may override:

```java
public interface Payable {
    Receipt pay(double amount);
    double outstandingBalance();

    default boolean isSettled() {                // derived behavior, defined once
        return outstandingBalance() == 0;
    }
}
```

*A default method: convenience logic built from the abstract methods, free to every implementor.*

Defaults exist mainly so interfaces can *evolve* — adding a method to a published interface without breaking every implementor — and to ship helpers (the Collections API uses them heavily). They don't change the essence: interfaces define contracts, not state.

---

## Abstract Classes

An **abstract class** sits between interface and concrete class: it may hold fields, constructors, and implemented methods, but declares some methods `abstract` — bodiless, mandatory for subclasses — and consequently **cannot be instantiated** itself:

```java
public abstract class Report {
    private final String title;                       // real state, real constructor

    protected Report(String title) { this.title = title; }

    public final String render() {                    // the TEMPLATE: fixed skeleton
        return header() + body() + "\n-- end of " + title;
    }

    private String header() { return "== " + title + " ==\n"; }

    protected abstract String body();                 // the HOLE: each subclass fills it
}

public class SalesReport extends Report {
    public SalesReport() { super("Sales"); }

    @Override
    protected String body() { return "revenue table..."; }
}
```

*An abstract class as a skeleton: shared structure implemented once, the varying step left abstract.*

`new Report("x")` is a compile error; `new SalesReport()` works, and `render()` runs the shared skeleton around the subclass's `body()`. This shape — invariant algorithm in the parent, variant steps as abstract methods — is the *template method* pattern (formally revisited in Design Patterns), and it's the strongest case for abstract classes: **shared implementation and state** across a closed family of subclasses.

---

## Choosing Between Them

The two answer different questions:

| | Interface | Abstract class |
|---|---|---|
| Models | a **capability** ("can be paid") | a **partial thing** ("is a report, half-built") |
| Instance state | none | fields + constructors |
| A class can have | many | exactly one parent |
| Relationship to implementors | often unrelated classes | a tight family |

The modern default is firm: **start with an interface.** It costs nothing, couples nobody, and composes. Reach for an abstract class only when implementations genuinely share state or code — and even then, a common trick is both: an interface for the contract, an abstract class as an optional convenience base (`List` and `AbstractList` in the JDK do exactly this).

The deeper habit both serve is **programming to abstractions**: declare variables, parameters, and return types as the interface, keep concrete classes behind constructors and factories. Client code that says `Payable` instead of `Invoice` survives every change of implementation — which is the property unit tests (mock the interface), Spring (inject by interface), and the topic we turn to next all exploit: the Collections Framework is one grand exercise in interface-first design, `List`, `Set`, and `Map` being contracts with swappable implementations behind them.
