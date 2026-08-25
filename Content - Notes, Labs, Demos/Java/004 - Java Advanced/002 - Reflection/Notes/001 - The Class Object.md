# The Class Object

**Reflection** is Java examining itself at runtime: code that asks "what class is this object? what methods does it have? what annotations?" — and then acts on the answers, calling methods and reading fields it never knew at compile time. It's how JUnit finds `@Test` methods, how Spring wires beans, how JSON libraries map objects — the enabling machinery of this whole unit. Its gateway is the **`Class`** object.

---

## Every Type Has a Class Object

For every type loaded in the JVM — every class, interface, enum, array type, even primitives — the JVM maintains exactly one **`Class<T>`** instance describing it: name, superclass, interfaces, members, annotations. Three roads lead to it:

```java
// 1. The class literal — compile-time known type
Class<String> c1 = String.class;

// 2. getClass() — from a live object (inherited from Object)
Object payload = fetchNext();
Class<?> c2 = payload.getClass();            // the object's ACTUAL runtime class

// 3. Class.forName — from a string, at runtime
Class<?> c3 = Class.forName("com.example.billing.Invoice");
```

*Three routes to the same kind of object: literal for known types, `getClass()` for live objects, `forName` for names from configuration.*

Each has its moment. The **literal** (`String.class`) is how we pass types as values — the tokens handed to frameworks (`ObjectMapper.readValue(json, Invoice.class)`). **`getClass()`** answers "what is this *really*?" — by dynamic dispatch logic, it returns the actual class, so a `Vehicle` reference holding an `ElectricTruck` reports `ElectricTruck`. **`forName`** loads a class by name — the trick behind plugin systems and drivers configured in text files: behavior chosen by string, at runtime. (`Class<?>` is the generics wildcard from Unit 3 — "a Class of some unknown type.")

---

## Interrogating a Class

The `Class` object answers structural questions:

```java
Class<?> c = order.getClass();

c.getName();                  // "com.example.shop.RushOrder" — fully qualified
c.getSimpleName();            // "RushOrder"
c.getSuperclass();            // Class for Order
c.getInterfaces();            // e.g., [Comparable]
c.isInterface();  c.isEnum();  c.isArray();
c.isInstance(order);          // the instanceof check, as a method
Modifier.isFinal(c.getModifiers());       // class-level modifiers, decoded
```

*The metadata API: names, hierarchy, kind checks — the compile-time facts, available at runtime.*

This is also our first look at how frameworks "see" our code: to a library that's never heard of `RushOrder`, the class arrives as pure metadata — a name, a parent, a bag of members to enumerate (next lesson), and annotations to look up (lesson 3).

---

## Creating Instances Reflectively

With a `Class` object, code can construct instances of types it doesn't know at compile time:

```java
Class<?> pluginClass = Class.forName(config.get("exporter.class"));
Object exporter = pluginClass.getDeclaredConstructor().newInstance();

if (exporter instanceof Exporter e) {         // cross back into typed-land ASAP
    e.export(report);
}
```

*Load by configured name, invoke the no-arg constructor, cast to a known interface — the plugin pattern.*

The shape is idiomatic: reflection creates the object, an *interface* (abstraction, pulling its weight again) gives us a typed handle immediately after. Untyped `Object`-and-`Method` juggling should be a thin layer, not a style. This snippet is essentially how Spring instantiates beans and JDBC loads drivers — configuration in, objects out.

---

## Costs and Cautions

Reflection trades away what Java normally guarantees, so the trade must be worth it:

- **No compile-time safety** — a mistyped name in `forName` or a signature mismatch fails at *runtime* (`ClassNotFoundException`, etc.), moving whole error classes from the compiler to production. Wrong types surface as exceptions, not red squiggles.
- **Slower** — metadata lookups and reflective calls cost multiples of direct ones. Irrelevant at startup-time wiring frequency (the framework pattern); ruinous in hot loops.
- **Encapsulation can be bypassed** — as previewed in Access Modifiers: reflection can force private members open (`setAccessible(true)`), within limits the module system now enforces. Power for frameworks and tests; design smell in application logic.

The professional posture: *understand* reflection deeply, *write* it rarely. Application code uses frameworks that use reflection; knowing what's under the hood is what makes framework behavior — and its error messages — legible. With the `Class` object in hand, the next lesson does something with it: enumerating and invoking fields, methods, and constructors.
