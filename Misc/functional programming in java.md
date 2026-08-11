# Functional Programming in Java — Lesson Outline

**Scope:** Functional interfaces and lambda expressions. Introductory depth — enough to read, write, and reason about lambdas.

---

## 1. Framing: Why This Exists

- Java is object-oriented: to pass *behavior* to a method, you historically had to pass an *object*.
- Anonymous inner classes were the workaround — verbose, noisy, mostly boilerplate.
- Java 8 added lambdas to express "just the behavior" concisely.
- Mental model: **a lambda is a shorthand for an object that implements a one-method interface.**

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("working");
    }
};
```

*Five lines of ceremony to deliver one line of behavior — this is the problem lambdas solve.*

## 2. Functional Interfaces

- Definition: an interface with exactly one abstract method (SAM — Single Abstract Method).
- `@FunctionalInterface` annotation — optional, but enforces the rule at compile time.
- Default and static methods don't count against the one-abstract-method rule.
- The interface defines the **contract** (parameters, return type); the lambda supplies the **implementation**.

```java
@FunctionalInterface
interface Greeter {
    String greet(String name);
}
```

*The minimum shape of a functional interface: one abstract method, so one lambda can satisfy it.*

```java
Runnable r;     // void run()
Comparator<String> c;  // int compare(String, String)
```

*Types they already use are functional interfaces — this isn't a new category of thing.*

## 3. From Anonymous Class to Lambda

- Show the same behavior in both forms, back to back.
- Emphasize: identical result, less ceremony.
- Point out what the compiler infers — the type, the method name, the `@Override`.

```java
Greeter oldWay = new Greeter() {
    @Override
    public String greet(String name) {
        return "Hello, " + name;
    }
};

Greeter newWay = name -> "Hello, " + name;
```

*Same object, same behavior — the lambda just drops everything the compiler can figure out on its own.*

## 4. Lambda Syntax

- Anatomy: `(parameters) -> body`
- Type inference: the compiler determines parameter types from the target functional interface.

```java
() -> System.out.println("hi")        // no parameters
name -> name.length()                 // one parameter, parens optional
(a, b) -> a + b                       // multiple parameters
(String a, String b) -> a + b         // explicit types, rarely needed
```

*Parameter forms — the only rule worth memorizing is that parens are optional only for a single parameter.*

```java
Greeter expression = name -> "Hi, " + name;

Greeter block = name -> {
    String trimmed = name.trim();
    return "Hi, " + trimmed;
};
```

*Expression bodies return implicitly; block bodies need braces and an explicit `return`.*

## 5. Built-In Functional Interfaces (`java.util.function`)

Introduce the four workhorses; don't exhaust the package.

| Interface | Method | Takes | Returns | Use for |
|---|---|---|---|---|
| `Predicate<T>` | `test` | T | `boolean` | Filtering / conditions |
| `Function<T,R>` | `apply` | T | R | Transforming |
| `Consumer<T>` | `accept` | T | `void` | Side effects (printing, saving) |
| `Supplier<T>` | `get` | — | T | Producing / deferred creation |

```java
Predicate<String> isEmpty = s -> s.isEmpty();
Function<String, Integer> length = s -> s.length();
Consumer<String> print = s -> System.out.println(s);
Supplier<String> make = () -> "new value";

System.out.println(isEmpty.test(""));      // true
System.out.println(length.apply("java"));  // 4
print.accept("done");                      // done
System.out.println(make.get());            // new value
```

*One lambda per interface, then the call that runs it — reinforces that these are ordinary interfaces with ordinary methods.*

- Mention that variants exist (`BiFunction`, `UnaryOperator`, primitive specializations) without covering them.

## 6. Method References (Light Touch)

- `::` as shorthand when the lambda does nothing but call an existing method.
- Note the forms exist (static, instance, constructor) — flag as "recognize it, we'll return to it."

```java
Consumer<String> lambda = s -> System.out.println(s);
Consumer<String> reference = System.out::println;
```

*When the lambda's entire body is one existing method call, the method reference says the same thing with less.*

## 7. Common Gotchas

```java
int count = 0;
Runnable r = () -> System.out.println(count);
count++;  // compile error: count must be effectively final
```

*Captured local variables can't be reassigned — the most common surprise when a lambda won't compile.*

```java
Greeter bad = name -> { "Hi, " + name; };   // compile error: not a statement
Greeter good = name -> { return "Hi, " + name; };
```

*Once you add braces, you own the `return` — mixing the two forms is the second most common error.*

- Lambdas only work where a functional interface is expected — no free-floating lambdas.
- `this` inside a lambda refers to the enclosing instance, not the lambda itself (differs from anonymous classes).

## 8. Practice

- Write a custom functional interface and implement it with a lambda.
- Rewrite a supplied anonymous inner class as a lambda.
- Given a target interface, identify valid vs. invalid lambda syntax.
- Match a scenario to the correct built-in interface (`Predicate` / `Function` / `Consumer` / `Supplier`).

## 9. Wrap-Up

- Recap: functional interface = contract, lambda = implementation, syntax = `(params) -> body`.
- Preview: Streams — where lambdas actually pay off.