# Methods and Access Modifiers

A **method** is a named block of code that performs a task. We define it once and call it whenever we need that behavior. Even our simplest programs use methods — `main` is one, and `System.out.println` is a call to another. Here we'll look at how methods are declared, how they're called, and what the keywords in front of them mean.

---

## What a Method Signature Is

A **method signature** is the line that declares a method — its name, its parameters, its return type, and its modifiers. The signature tells us everything we need to know to call the method correctly:

```java
public static int add(int a, int b) {
    return a + b;
}
```

Reading the signature left to right:

- `public` — an access modifier (covered below)
- `static` — the method belongs to the class, not an instance (covered in the next notes)
- `int` — the return type: this method gives back an `int`
- `add` — the method name
- `(int a, int b)` — the parameter list: two `int` values must be passed in

Everything between the braces `{ }` is the **method body** — the code that runs when the method is called.

---

## Parameters and Parameter Types

**Parameters** are the inputs a method declares. Each parameter has a type and a name, and callers must supply a value (an **argument**) of the matching type for each one:

```java
public static void greet(String name, int times) {
    for (int i = 0; i < times; i++) {
        System.out.println("Hello, " + name + "!");
    }
}
```

This method requires exactly two arguments: a `String` first, then an `int`. The order matters, and the types must match — calling `greet(3, "Alice")` won't compile. A method can also take no parameters at all, in which case the parentheses are empty:

```java
public static void printBanner() {
    System.out.println("=== Welcome ===");
}
```

---

## Return Types and `void`

Every method declares a **return type** — the type of value it hands back to its caller. A method with a return type must use a `return` statement to produce that value:

```java
public static double average(double a, double b) {
    return (a + b) / 2;
}
```

When a method performs an action but has no value to give back, its return type is **`void`**. A `void` method simply finishes when it reaches the end of its body (or hits a bare `return;`):

```java
public static void printResult(double value) {
    System.out.println("Result: " + value);
}
```

This is why `main` is declared `void` — it does its work but returns nothing to the JVM.

---

## Calling a Method

We call a method by writing its name followed by parentheses containing the arguments. If the method returns a value, we can capture it in a variable or use it directly in an expression:

```java
double avg = average(4.0, 10.0);   // capture the return value
printResult(avg);                  // void method — nothing to capture
printResult(average(2.0, 8.0));    // use a return value directly
```

When a call happens, execution jumps into the method body, runs it, and then returns to the line where the call was made. Calls can ignore a return value (`average(1.0, 2.0);` alone is legal, just usually pointless), but we can't capture a value from a `void` method — `double x = printResult(5.0);` won't compile.

---

## What Access Modifiers Are

An **access modifier** is a keyword that controls *who is allowed to use* a class, method, or field. They're how Java enforces boundaries: some things are part of a class's public face, and some are internal details that outside code shouldn't touch.

Java has three access modifier keywords — `public`, `private`, and `protected` — plus a fourth level that applies when we write no modifier at all.

---

## `public`, `private`, `protected`, and Package-Private

From most open to most restricted:

- **`public`** — accessible from anywhere. Any class in any package can use it. `main` must be public so the JVM can call it.
- **`protected`** — accessible within the same package, plus from subclasses in other packages. This becomes relevant when we cover inheritance; for now it's enough to recognize the keyword.
- **package-private** (no keyword) — accessible only within the same package. This is the **default access level**: writing `void helper()` with no modifier gives package-private access. Note there is no `package` keyword for this — it's what we get by writing nothing.
- **`private`** — accessible only within the same class. Nothing outside the class can see it, not even classes in the same package.

```java
public class Account {
    private double balance;              // only Account can touch this

    public double getBalance() {         // anyone can call this
        return balance;
    }

    private void audit() {               // internal helper, hidden
        System.out.println("audited");
    }
}
```

The general habit to build: make things as restricted as possible. Fields are usually `private`, with `public` methods providing controlled access. We'll see why this matters when we get to encapsulation in the Java Fundamentals module.

---

## Where Access Modifiers Apply

Access modifiers appear on three kinds of declarations:

- **Classes.** A top-level class can be `public` or package-private. A `public` class is usable from anywhere; a package-private class is invisible outside its package. (Top-level classes can't be `private` or `protected`.)
- **Methods.** Any of the four levels. This controls who can call the method.
- **Fields.** Any of the four levels. This controls who can read or assign the variable directly.

One rule worth remembering early: a source file can contain at most one `public` top-level class, and the file name must match that class — `public class HelloWorld` must live in `HelloWorld.java`.
