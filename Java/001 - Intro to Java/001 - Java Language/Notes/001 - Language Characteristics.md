# Language Characteristics

Before writing any Java, it helps to know what kind of language we're dealing with. Java makes a handful of foundational design choices — static typing, object orientation, automatic memory management — and nearly everything about how Java code looks and behaves traces back to them. We'll take each in turn.

---

## What Java Is

**Java** is a general-purpose programming language created at Sun Microsystems and first released in 1995. James Gosling led the original design; Oracle has owned and stewarded the language since acquiring Sun in 2010. Java was designed for a world of networked devices, with portability as a headline goal — the famous promise was *"write once, run anywhere."* (How that promise is actually delivered is the subject of the next set of notes, on the Java platform.)

Three decades on, Java is one of the most widely used languages in the industry, and it dominates a particular niche: large, long-lived, server-side business applications. Banking systems, insurance platforms, e-commerce backends, and the Android ecosystem all run on enormous amounts of Java. New versions now ship on a predictable six-month cadence, with a **Long-Term Support (LTS)** release every two years (Java 8, 11, 17, 21, and 25 are LTS versions). Enterprises generally standardize on an LTS release.

The language's reputation is "verbose but dependable." Java asks us to spell things out — types, class structures, exceptions — and in exchange gives us code that the compiler can check thoroughly, tools can analyze deeply, and large teams can maintain for decades.

---

## Strongly and Statically Typed

Java is both **statically typed** and **strongly typed**. The two terms sound similar but answer different questions.

**Static typing** means every variable has a type that is fixed at compile time. We declare the type, and the compiler verifies — before the program ever runs — that we only use the variable in ways that type allows:

```java
int count = 10;
String name = "Alice";

count = 25;        // fine — still an int
count = "Bob";     // compile error: incompatible types
```

Compare this with a dynamically typed language like Python or JavaScript, where a variable can hold a number one moment and a string the next, and type mismatches only surface when the offending line actually executes. In Java, a whole class of bugs is caught at compile time, before the code ships. This is also what powers the rich autocomplete and refactoring in Java IDEs — the tools always know exactly what type everything is.

**Strong typing** means the language doesn't silently bend types to make an operation work. There's no JavaScript-style `"5" + 1` producing `"51"`. If we want to treat one type as another, we must convert it explicitly:

```java
String input = "42";
int value = Integer.parseInt(input);   // explicit conversion — we asked for it

double price = 9.99;
int dollars = (int) price;             // explicit cast — truncates to 9
```

The practical consequence: Java code declares its intent everywhere. Method signatures say exactly what they accept and return, and reading unfamiliar code is easier because the types document the data flowing through it. The cost is some ceremony — more to type, and occasional explicit conversions — which Java has softened over the years (the `var` keyword, introduced in Java 10, lets the compiler infer a local variable's type from the right-hand side, but the variable is still statically typed; `var` is inference, not dynamic typing).

---

## An Object-Oriented Language

Java is an **object-oriented** language, and it commits to the paradigm more thoroughly than most: in Java, *all code lives inside a class*. There are no free-floating functions or top-level statements. Even the simplest program is a class with a method in it:

```java
public class Greeter {
    public static void main(String[] args) {
        System.out.println("Hello!");
    }
}
```

The core idea of object orientation is bundling **state** (data) and **behavior** (the methods that operate on that data) together into **objects**. A **class** is the blueprint; an object is a live instance of that blueprint. Encapsulation, inheritance, polymorphism, and abstraction are the four pillars — we'll work with each in depth later. For now, the takeaway is that Java's whole structure is organized around classes and the objects made from them.

One honest footnote: Java is not *purely* object-oriented. The eight **primitive types** (`int`, `double`, `boolean`, `char`, and friends) are simple values, not objects, kept that way for performance. And since Java 8, the language has absorbed functional-style features like lambdas and streams. But the object-oriented model remains the backbone everything else hangs on.

---

## Classes and Objects

The distinction between a **class** and an **object** is one of the most important in Java, and it's worth being precise about it.

A class is a blueprint — a definition. It describes what fields (state) something has and what methods (behavior) it exposes. It doesn't hold any live data by itself; it's just a template:

```java
public class BankAccount {
    private double balance;

    public void deposit(double amount) {
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}
```

An object is a live instance of that blueprint, created at runtime with `new`. Each instance gets its own copy of the fields:

```java
BankAccount alice = new BankAccount();
BankAccount bob = new BankAccount();

alice.deposit(100.0);

System.out.println(alice.getBalance());  // 100.0
System.out.println(bob.getBalance());    // 0.0
```

`alice` and `bob` are both `BankAccount` objects, but they are entirely independent. Depositing into one has no effect on the other. The class defines the shape; the objects are the actual data.

A variable like `alice` doesn't contain the object itself — it holds a **reference** to it, an address pointing to where the object lives in memory. That distinction matters, and it comes up directly in the next section.

---

## Pass by Value and Pass by Reference

When we call a method and pass an argument, the language has to decide what exactly gets handed to the method. There are two common models:

**Pass by value** means the method receives a *copy* of the value. The method can do whatever it likes with that copy — it has no effect on the original:

```java
void doubleIt(int x) {
    x = x * 2;   // modifies the local copy only
}

int n = 5;
doubleIt(n);
System.out.println(n);   // still 5
```

**Pass by reference** means the method receives a direct reference to the original variable. Modifying it inside the method changes the original. Java does not do this.

**Java is strictly pass by value — always.** For primitives this is straightforward: a copy of the value is passed, and the original is untouched.

For objects, the picture looks more complicated but the rule is the same. What gets passed is a copy of the *reference* — not the object itself, and not a reference to the variable. This means the method can use that reference to call methods or modify fields on the object (which will be visible to the caller), but it cannot make the caller's variable point to a different object:

```java
void addFunds(BankAccount account, double amount) {
    account.deposit(amount);   // modifies the object — visible to the caller
}

void replaceAccount(BankAccount account) {
    account = new BankAccount();   // only reassigns the local copy of the reference
                                   // the caller's variable is unchanged
}

BankAccount myAccount = new BankAccount();
addFunds(myAccount, 50.0);
System.out.println(myAccount.getBalance());   // 50.0 — object was modified

replaceAccount(myAccount);
System.out.println(myAccount.getBalance());   // still 50.0 — caller's reference unchanged
```

The common point of confusion: because we can mutate an object through a passed reference, it *feels* like pass by reference. But true pass by reference would allow `replaceAccount` to change which object `myAccount` points to. It can't. Java passed a copy of the reference, and reassigning that copy has no effect on the original variable.

---

## Garbage Collection and Automatic Memory Management

In languages like C and C++, the programmer manages memory by hand: allocate it when needed, release it when done. Forget to release, and the program slowly leaks memory; release too early, and it crashes or corrupts data. These memory bugs are notoriously difficult to find.

Java removes the entire category. Memory is managed automatically by the **garbage collector (GC)**, a component of the Java runtime that periodically finds objects no longer reachable by the program and reclaims their memory:

```java
public void process() {
    Report report = new Report();   // memory allocated automatically
    report.generate();
}                                    // 'report' goes out of scope here —
                                     // no reference remains, so the object
                                     // becomes eligible for garbage collection
```

There is no `free()` or `delete` in Java. We create objects with `new` and simply stop referring to them when we're done; the GC handles the rest. The rule it follows is **reachability**: an object stays alive as long as something in the running program can still reach it through a chain of references. Once nothing can, it's garbage.

What this buys us:

- **No manual deallocation bugs.** Use-after-free and double-free errors can't be written in Java.
- **Leaks are rare and tamer.** A Java program can still "leak" by accidentally keeping references alive (say, objects piling up in a long-lived collection), but it can't lose track of memory the way C can.
- **Simpler code.** We think about object lifetimes far less, and our APIs don't need ownership conventions.

The trade-off is control: the GC decides when to run, and a collection pause briefly takes time away from the application. Modern collectors have made these pauses small enough that most applications never think about them — but it's why performance-critical Java tuning often centers on GC behavior, and it's a fair price for eliminating an entire class of the most painful bugs in systems programming.

---

## Key Takeaways
- Java is statically typed (types are fixed and checked at compile time) and strongly typed (no silent type coercion) — the compiler catches type errors before the program runs.
- Java is object-oriented to its core: all code lives in classes, and programs are built from objects that encapsulate state and behavior.
- A **class** is a blueprint; an **object** is a live instance of that blueprint. Each object has its own copy of the fields defined by its class.
- Java is strictly **pass by value**. Primitives pass a copy of the value; objects pass a copy of the reference. A method can mutate an object through its reference, but cannot replace the caller's reference.
- Memory is managed automatically by the garbage collector — we create objects with `new` and never explicitly free them; unreachable objects are reclaimed for us.