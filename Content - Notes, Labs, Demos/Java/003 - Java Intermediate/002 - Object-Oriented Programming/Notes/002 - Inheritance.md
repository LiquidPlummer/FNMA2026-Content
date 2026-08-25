# Inheritance

**Inheritance** creates a parent–child relationship between classes: the child (**subclass**) automatically has every field and method of the parent (**superclass**), then extends or refines them. We've used the keyword already — every custom exception `extends` something. Now the machinery in full: `super`, overriding, and the root class every Java object shares.

---

## extends

```java
public class Vehicle {
    private final String id;
    protected double fuelLevel;                  // protected: subclasses may touch

    public Vehicle(String id) { this.id = id; }

    public void refuel() { fuelLevel = 100.0; }
    public String getId() { return id; }
}

public class Truck extends Vehicle {
    private double cargoTons;

    public Truck(String id, double cargoTons) {
        super(id);                               // MUST run the parent's constructor first
        this.cargoTons = cargoTons;
    }

    public void load(double tons) {              // new behavior, Truck-only
        cargoTons += tons;
    }
}

Truck t = new Truck("TRK-7", 2.5);
t.refuel();                                      // inherited — defined in Vehicle
t.load(1.0);                                     // own — defined in Truck
```

*A `Truck` is a `Vehicle`: it inherits `refuel` and `getId`, adds `load`, and constructs its parent part via `super(...)`.*

Rules packed into that example: a class extends **exactly one** class (Java has no multiple class inheritance — interfaces, lesson 4, fill that role); constructors are *not* inherited, and a subclass constructor must invoke a superclass constructor — explicitly with **`super(args)`** as its first statement, or implicitly (the compiler inserts `super()` when the parent has a no-arg constructor). Construction therefore runs top-down: `Vehicle`'s constructor completes before `Truck`'s body starts — extending the initialization-order story from Unit 2.

`private` members are inherited but inaccessible to the subclass; **`protected`** (the modifier parked in the Access Modifiers lesson) opens them to subclasses. Even so, prefer private-plus-getters — `protected` fields are a smaller wall, not a better one.

---

## Overriding

A subclass may **override** an inherited method — redefine it with the same signature, replacing the parent's behavior for its instances:

```java
public class ElectricTruck extends Truck {

    @Override
    public void refuel() {                       // replaces Vehicle's version
        chargeBattery();
    }

    @Override
    public String toString() {
        return "ElectricTruck " + getId() + " (" + super.toString() + ")";
    }                                            // super.method(): the overridden version, still callable
}
```

*Overriding swaps implementations; `super.` reaches the parent's version when the child builds on it rather than replacing it.*

Always annotate with **`@Override`** — as covered in Annotations, it converts a signature typo from a silent new method into a compile error. Overriding is *not* overloading (same name, different parameters, resolved at compile time — Unit 2); overriding is same signature, different class, resolved **at runtime**, which is precisely the polymorphism of the next lesson. Restrictions worth knowing: an override can't *reduce* visibility (`public` can't become `protected`), can't throw broader checked exceptions, and `final` methods can't be overridden at all — while a `final` **class** (`String` is one) can't be extended, period.

---

## Everything Extends Object

A class with no `extends` clause implicitly extends **`java.lang.Object`** — making every class, including all of ours, part of one tree with `Object` at the root. Three inherited methods matter daily, and all three exist to be overridden:

- **`toString()`** — the printable form. `Object`'s version is the unhelpful `ClassName@hash` we saw printing arrays; overriding it (as `ElectricTruck` does above) is why `println(order)` can be useful.
- **`equals(Object o)`** — "same contents?" `Object`'s version is `==` (same instance), which is why our Unit 2 classes compared by identity only. Value classes override it.
- **`hashCode()`** — a numeric digest that **must** be overridden whenever `equals` is, keeping the pair consistent (equal objects, equal hash codes). Hash-based collections in the next topic silently malfunction otherwise.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Invoice other)) return false;
    return id.equals(other.id);
}

@Override
public int hashCode() { return id.hashCode(); }
```

*The standard `equals`/`hashCode` pair, based on the same fields — IDEs and records generate exactly this.*

(Records, from Unit 2, generate correct versions automatically — one more reason they're the default for data carriers.)

---

## The Warning Label: Composition Over Inheritance

Inheritance is powerful and *binding*: a subclass is coupled to its parent's implementation details forever — parents can't change internals without auditing children (the *fragile base class* problem), and the is-a claim is public API. Much of what beginners reach for inheritance to do — reuse a class's functionality — is served better by **composition**: holding a reference and delegating:

```java
public class AuditedAccount {                    // HAS-A account — no extends
    private final BankAccount account;
    private final AuditLog log;

    public void withdraw(double amount) {
        log.record("withdraw " + amount);
        account.withdraw(amount);                // delegation: reuse without coupling
    }
}
```

*Composition: reuse `BankAccount`'s behavior by owning one, exposing only what makes sense.*

The test is behavioral: inherit only when the child is **truly substitutable** wherever the parent is expected — every promise the parent's methods make, the child keeps (the *Liskov substitution* principle, informally). "A `Truck` is a `Vehicle`" passes; "a `Stack` is a `Vector`" (a real JDK mistake) did not. When the relationship is "uses" or "is made of," compose. Modern Java code inherits rarely, implements interfaces constantly — and the next two lessons show why that trade is so favorable.
