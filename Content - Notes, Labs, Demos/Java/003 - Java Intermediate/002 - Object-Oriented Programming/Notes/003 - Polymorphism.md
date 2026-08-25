# Polymorphism

**Polymorphism** is the rule that makes inheritance worth having: an object of a subclass can stand anywhere its superclass is expected, and method calls on it run the *subclass's* versions. One block of code, written once against the general type, behaves differently — correctly — for every specialization. This lesson pins down the mechanics: reference types versus object types, dynamic dispatch, and casting.

---

## Reference Type vs. Object Type

The insight everything else hangs on: a variable has a declared **reference type**, but the object it points at has its own **actual class** — and they need only be compatible, not identical:

```java
Vehicle v = new ElectricTruck("ET-1", 2.0);     // reference type: Vehicle; object: ElectricTruck
```

*Perfectly legal: an `ElectricTruck` IS a `Vehicle`, so a `Vehicle` variable may hold one.*

The two types answer different questions:

- **What can we *call*?** — decided by the **reference type**, at compile time. Through `v`, only `Vehicle`'s methods are visible: `v.refuel()` compiles, `v.load(1.0)` does not — even though the object underneath genuinely has a `load` method.
- **What actually *runs*?** — decided by the **object's class**, at runtime. `v.refuel()` executes `ElectricTruck`'s battery-charging override, not `Vehicle`'s version.

That second rule is **dynamic dispatch**: every instance method call is resolved against the real object at the moment of the call. The reference type is a lens; the object is what it is.

---

## Why This Is the Point of OOP

Dynamic dispatch is what deletes type-checking conditionals from client code. The pillar lesson's loop, now with full understanding:

```java
List<Vehicle> fleet = List.of(new Truck("T1", 3.0), new ElectricTruck("E1", 2.0), new Van("V1"));

for (Vehicle v : fleet) {
    v.refuel();                    // three iterations, three different refuel() bodies
}
```

*Heterogeneous collection, uniform code: each object supplies its own behavior.*

The alternative universe — `if (v instanceof ElectricTruck) { ... } else if (...)` — must be edited every time a vehicle type is added, in every place that pattern appears. With polymorphism, a new `HydrogenTruck` slots into `fleet` and every existing loop, method, and framework callback just works. **Adding a type requires no changes to code that uses the supertype** — that's the open/closed principle, and it's why parameters, fields, and collections are declared with the *most general* type that suffices (`List<Vehicle>`, not `List<Truck>` — and `List` itself, not `ArrayList`, foreshadowing the next topic).

Static methods, note, don't participate — they're resolved by class at compile time (no object, no dispatch; part of why the `static` lesson called them class-level). Fields don't dispatch either; only instance methods are polymorphic.

---

## Casting Objects: Up and Down

Moving a reference *up* the hierarchy — **upcasting** — is implicit and always safe (`Vehicle v = eTruck`: every `ElectricTruck` is a `Vehicle`). Moving *down* — **downcasting**, to regain the subclass's specific methods — requires an explicit cast, and unlike the primitive casts of Unit 2, a wrong one fails at runtime with a **`ClassCastException`**:

```java
Vehicle v = fleet.get(0);
// v.load(2.0);                        // compile error — Vehicle has no load
Truck t = (Truck) v;                   // downcast: works only if v really IS a Truck
t.load(2.0);
```

*The cast asserts "this Vehicle is actually a Truck" — the JVM checks, and throws if it's a Van.*

Guarding the cast is the **`instanceof`** test, and modern Java (16+) fuses test and cast into one **pattern matching** step:

```java
if (v instanceof Truck t) {            // test AND bind: t is in scope, already a Truck
    t.load(2.0);
}
```

*Pattern matching for `instanceof`: no separate cast line, no repeated type name.*

Design instinct, though: a downcast means the code *lost* type information and is buying it back — occasionally necessary (framework boundaries, `equals` implementations like last lesson's), but a codebase full of `instanceof` chains is polymorphism refused. The first question when reaching for a cast: could this be a method the supertype declares and each subclass overrides?

---

## The Vocabulary, Settled

For precision in code review and interviews alike:

- **Compile-time (static) polymorphism** — method *overloading*: same name, different signatures, resolved by the compiler from argument types (Unit 2).
- **Runtime (dynamic) polymorphism** — method *overriding* plus dynamic dispatch: same signature, chosen by the object's actual class. This lesson — and when unqualified, "polymorphism" means this one.

The remaining gap: `Vehicle` still carries implementation. What if the supertype should be *pure contract* — no fields, no bodies, just the promise that `refuel()` exists? That's abstraction's dedicated machinery: interfaces and abstract classes, next.
