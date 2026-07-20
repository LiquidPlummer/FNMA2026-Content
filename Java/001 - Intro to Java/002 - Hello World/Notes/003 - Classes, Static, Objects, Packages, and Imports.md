# Classes, Static, Objects, Packages, and Imports

In Java, all code lives inside classes — there are no free-floating functions or statements. Even Hello World needs a class to hold its `main` method. Here we'll look at what classes are, what the `static` keyword on `main` actually means, and how packages and imports organize classes into a usable structure.

---

## What a Class Is

A **class** is the basic unit of code organization in Java. Every method and every field belongs to some class. At minimum, a class is a named container declared with the `class` keyword:

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

But a class is more than a container — it's a *blueprint*. A class describes a kind of thing: what data it holds (fields) and what it can do (methods). From one class we can create many concrete things built from that blueprint.

```java
public class Dog {
    String name;                 // a field — data each dog has

    void bark() {                // a method — something each dog can do
        System.out.println(name + " says woof!");
    }
}
```

---

## Instances and Objects

An **object** is a concrete thing created from a class — also called an **instance** of that class. The class `Dog` is the blueprint; each actual dog in memory is an instance. We create instances with the **`new`** keyword:

```java
Dog rex = new Dog();
rex.name = "Rex";
rex.bark();              // Rex says woof!

Dog fido = new Dog();
fido.name = "Fido";
fido.bark();             // Fido says woof!
```

Each instance has its own copy of the fields — `rex.name` and `fido.name` are independent. The dot operator (`.`) is how we reach into an object to access its fields or call its methods.

For our first programs we won't create objects ourselves, but we'll use ones that already exist — `System.out` is an object, and `println` is a method we call on it.

---

## The `static` Keyword

The **`static`** keyword marks a method or field as belonging to the *class itself* rather than to any instance. A static member exists once, shared, regardless of how many objects we create — or whether we create any at all.

```java
public class MathHelper {
    static int square(int x) {       // static — belongs to the class
        return x * x;
    }

    int instanceCounter;             // non-static — each object has its own
}
```

The practical difference is what we need before we can use it:

- A **non-static** (instance) method or field requires an object: we must call `new` first, then use the object.
- A **static** method or field needs no object — the class name alone is enough.

This is exactly why `main` is static. When the JVM starts our program, no objects exist yet — nothing has run that could create one. By making `main` static, the JVM can call it directly on the class without constructing anything first.

One gotcha follows from this: code inside a static method can't directly use the non-static fields or methods of its own class, because there's no instance to read them from. If we try, the compiler reports `non-static method cannot be referenced from a static context`. The fix is either to make the member static too, or to create an instance and call it through that.

---

## Calling Static Methods on a Class

We call a static method using the class name, a dot, and the method name:

```java
int result = MathHelper.square(7);          // 49

double root = Math.sqrt(16.0);              // 4.0 — Math is a standard library class
int bigger = Math.max(3, 9);                // 9
```

The pattern `ClassName.methodName(arguments)` is one we'll see constantly. The standard library's `Math` class is a good example: it's a collection of static utility methods, and we never write `new Math()` — we just call methods on the class.

---

## What Packages Are and Why They Exist

A **package** is a named group of related classes — Java's equivalent of a folder for code. Packages exist for two reasons:

- **Organization.** Real projects have hundreds of classes. Packages group them by purpose: `com.example.billing`, `com.example.reports`, and so on.
- **Avoiding name collisions.** Two classes can both be named `List` as long as they live in different packages. The package gives each class a unique **fully qualified name**, like `java.util.List`.

Package names are lowercase by convention, with dots separating levels. To avoid collisions across organizations, the convention is a reversed domain name: a company at `example.com` names its packages `com.example.something`. The package structure must match the directory structure on disk — a class in package `com.example.app` lives in the folder `com/example/app/`.

---

## How to Declare a Package

A class joins a package with a **`package` declaration** — the very first statement in the source file, before anything else:

```java
package com.example.app;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("Launching...");
    }
}
```

If a file has no package declaration, its class lands in the **default package** (no name). That's fine for tiny practice programs, but real code always declares a package.

---

## Importing Classes from Other Packages

A class can use another class in the *same* package by name, with no extra steps. To use a class from a *different* package, we either spell out its fully qualified name everywhere or — far more commonly — add an **`import`** statement once at the top of the file:

```java
package com.example.app;

import java.util.Scanner;        // now "Scanner" alone refers to java.util.Scanner

public class Greeter {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("What's your name?");
        String name = input.nextLine();
        System.out.println("Hello, " + name + "!");
    }
}
```

Imports go after the package declaration and before the class. An import doesn't copy any code into our file — it just tells the compiler which class a short name refers to. There's also a wildcard form, `import java.util.*;`, which makes every class in that package available by short name, though importing specific classes is generally preferred for clarity.

---

## The Standard Library and Built-In Packages

Java ships with a large **standard library** — thousands of ready-made classes organized into packages, all available without installing anything. Some packages we'll meet early:

- **`java.lang`** — the core: `String`, `Math`, `System`, `Integer`, and more. This package is special: it's imported automatically, which is why we can write `String` and `System.out.println` with no import statement.
- **`java.util`** — utilities and collections: `Scanner`, `ArrayList`, `HashMap`, `Random`.
- **`java.io`** — input and output: reading and writing files and streams.
- **`java.time`** — dates and times: `LocalDate`, `LocalDateTime`, `Duration`.

Knowing the standard library exists is half the skill — before writing something from scratch, it's worth checking whether `java.util` or another built-in package already provides it.
