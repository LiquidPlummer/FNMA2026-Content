# Annotations

Java code is full of `@`-prefixed markers — `@Override`, `@Deprecated`, `@Test`, `@Autowired`. These are **annotations**: structured metadata attached to code. An annotation doesn't *do* anything by itself — it's a label. What makes labels powerful is that tools read them: the compiler, testing frameworks, and eventually Spring, which configures entire applications from them. This lesson covers the concept and the built-ins; the frameworks that live on annotations fill the Java Advanced unit.

---

## What an Annotation Is

An annotation is applied by writing `@Name` immediately before a declaration — a class, method, field, parameter, or local variable:

```java
public class LegacyBilling {

    @Deprecated
    public double taxFor(double amount) {         // annotated method
        return amount * 0.08;
    }

    @SuppressWarnings("unchecked")
    void migrate(@Deprecated Object oldData) {    // annotations on parameters, too
        ...
    }
}
```

*Annotations decorate declarations — metadata sitting beside the code it describes.*

Some annotations take **elements** (named values) in parentheses — `@SuppressWarnings("unchecked")` — and a single element named `value` can omit its name, which is why both `@SuppressWarnings(value = "unchecked")` and the short form work. An annotation with no elements is a *marker* (`@Deprecated`, `@Override`).

---

## The Built-ins Worth Knowing Now

**`@Override`** — declares that a method is meant to *replace* one inherited from a parent type. Full meaning arrives with inheritance in the OOP topic, but the value is graspable now: it turns a silent mistake into a compile error. Without it, a typo like `tostring()` quietly defines a *new* method; with `@Override`, the compiler checks "does this really override something?" and fails loudly when it doesn't. The rule: **always write it when overriding.**

**`@Deprecated`** — marks an API as obsolete: it still works, but new code shouldn't call it. Callers get a compiler warning (and strikethrough in the IDE). Pair it with a Javadoc note pointing at the replacement; `@Deprecated(since = "2.1", forRemoval = true)` sharpens the message.

**`@SuppressWarnings`** — asks the compiler to mute a named warning category for the annotated element. Legitimate when a warning is examined and truly unavoidable; scope it as narrowly as possible (one method, not the class). A file peppered with suppressions is hiding problems, not solving them.

**`@FunctionalInterface`** — marks an interface as intended for lambda use (Functional Programming topic); like `@Override`, it makes the compiler enforce the intent.

```java
public class Invoice {

    @Override
    public String toString() {                    // compiler verifies this overrides Object.toString
        return "Invoice #" + id;
    }

    @Deprecated
    public double getTotalWithTax() {             // still compiles for old callers, warns new ones
        return total * 1.08;
    }
}
```

*The two everyday built-ins: `@Override` for safety, `@Deprecated` for graceful API evolution.*

---

## Where the Real Power Is: Tools Reading Metadata

The built-ins talk to the compiler. The broader ecosystem talks through annotations at every stage:

- **Testing** — JUnit runs every method marked `@Test` (Unit Testing topic).
- **Dependency injection** — Spring wires objects based on `@Component`, `@Autowired`, `@Configuration` (Spring Core).
- **Persistence** — JPA maps classes to tables via `@Entity`, `@Id`, `@Column` (Spring Data).
- **Web** — `@RestController` and `@GetMapping` turn methods into HTTP endpoints (Spring Web).

The common shape: instead of writing registration code ("add this class to the test suite", "bind this URL to that method"), we *label* the code and the framework discovers it. Declarative over imperative — half of modern Java development is knowing which label goes where.

How frameworks physically find the labels is **reflection** — runtime inspection of classes, a Java Advanced topic. Each annotation declares how long it survives (its *retention*): source-only (compiler hints like `@Override`), or all the way to runtime (framework annotations, readable while the program runs).

---

## Defining Our Own, in One Glance

Annotations are declared with `@interface` — worth recognizing even though authoring them is rare outside library code:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {
    String reason() default "";
}

@Audited(reason = "financial regulation")
public void transfer(double amount) { ... }
```

*A custom annotation: retention and target declared via meta-annotations, then applied like any other.*

Nothing happens until some code looks for `@Audited` via reflection and acts on it — the label-plus-reader pattern again, now from the authoring side.

---

## Closing the Unit

Annotations round out the Classes & Objects topic — and with it, Java Fundamentals. We can now define classes that guard their state, construct instances correctly, distinguish instance from class membership, model fixed value sets with enums, and read the metadata layer that the professional ecosystem is built on. The next unit turns to what happens *between* classes: exceptions crossing method boundaries, inheritance and polymorphism, collections, and functional style.
