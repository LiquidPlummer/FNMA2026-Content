# Generics

Before meeting the collections themselves, we need the type machinery they're written in. **Generics** let a class or method be parameterized by type — written once, type-safe for any element type. Every angle-bracketed `List<String>` we've casually used is generics at work; this lesson makes the notation precise, because the Collections Framework's documentation and compiler errors are unreadable without it.

---

## The Problem Generics Solve

Pre-generics Java (before 2004) had collections of `Object` — anything could go in, and everything came out needing a cast and a prayer:

```java
List names = new ArrayList();          // raw type: elements are just Object
names.add("Ada");
names.add(42);                         // nothing stops this
String s = (String) names.get(1);      // compiles fine — ClassCastException at RUNTIME
```

*The old world: type errors deferred to runtime, casts everywhere, honest mistakes exploding in production.*

Generics move that failure to compile time by letting the collection declare its element type:

```java
List<String> names = new ArrayList<>();
names.add("Ada");
// names.add(42);                      // compile error — caught before it can hurt
String s = names.get(0);               // no cast: the compiler KNOWS it's a String
```

*The generic world: wrong types don't compile, and reads need no casts.*

The `<>` on the right is the **diamond operator** — the compiler infers the type argument from the left side, sparing us the repetition. Raw types (bare `List`) still compile for backward compatibility, with warnings; in new code they're always a mistake.

---

## Type Parameters: Writing Generic Classes

A generic class declares **type parameters** in angle brackets — placeholder names, conventionally single capitals (`T` type, `E` element, `K`/`V` key/value) — and uses them like ordinary types:

```java
public class Box<T> {
    private T contents;

    public void put(T item) { contents = item; }
    public T take() { return contents; }
}

Box<String> b = new Box<>();
b.put("hello");
String s = b.take();                   // typed end to end
```

*One class, any payload type: `T` is filled in per use site — `Box<String>`, `Box<Invoice>`, `Box<List<Integer>>`.*

Type arguments must be **reference types** — `Box<int>` doesn't compile, which is the real reason wrapper classes (Unit 2) matter: `List<Integer>` with autoboxing is how primitives ride in collections.

**Generic methods** declare their own parameter before the return type, usually inferred at the call site:

```java
public static <T> T firstOrDefault(List<T> list, T fallback) {
    return list.isEmpty() ? fallback : list.get(0);
}

String name = firstOrDefault(names, "unknown");    // T inferred as String
```

*A generic method: the `<T>` before the return type scopes the parameter to this one method.*

---

## Bounds: Constraining the Parameter

An unconstrained `T` supports only `Object`'s operations. **Bounded** type parameters — `<T extends SomeType>` — restrict the argument and unlock the bound's methods:

```java
public static <T extends Number> double sum(List<T> values) {
    double total = 0;
    for (T v : values) total += v.doubleValue();     // legal: every T IS a Number
    return total;
}

sum(List.of(1, 2, 3));           // List<Integer> — fine
sum(List.of(1.5, 2.5));          // List<Double> — fine
// sum(List.of("a", "b"));       // compile error: String is not a Number
```

*A bound makes the parameter useful: `T` is guaranteed to be a `Number`, so `Number`'s API is available.*

`extends` in bounds covers both superclasses and interfaces (`<T extends Comparable<T>>` — a bound we'll use in the sorting lesson). Multiple bounds join with `&`.

---

## Wildcards and Invariance, Briefly

The counterintuitive fact that eventually bites everyone: `List<Truck>` is **not** a `List<Vehicle>`, even though `Truck` extends `Vehicle`. (If it were, a method holding it as `List<Vehicle>` could add a `Van` to what is really a truck list.) Generics are *invariant* — and the escape hatch is the **wildcard** `?`:

```java
static double totalFuel(List<? extends Vehicle> fleet) {   // accepts List<Truck>, List<Van>...
    double sum = 0;
    for (Vehicle v : fleet) sum += v.getFuelLevel();       // reading as Vehicle: safe
    return sum;
}
```

*`? extends Vehicle` means "a list of some subtype of Vehicle" — readable as vehicles, but nothing can be added to it.*

The full discipline (`? super T` for write-side flexibility, the *PECS* mnemonic) belongs to advanced API design; for this unit, recognizing `? extends` in signatures — "any subtype's collection is welcome, read-only" — covers what the Collections and Streams APIs will show us.

One last mechanical fact with practical echoes: generics are compile-time only. The compiler checks, then *erases* — at runtime a `List<String>` and `List<Integer>` are the same class (**type erasure**). Hence no `new T[]`, no `instanceof List<String>`, and the occasional unavoidable `@SuppressWarnings("unchecked")` in library code. For daily work, erasure changes little — the compile-time guarantees are the product, and the collections we now turn to are built entirely on them.
