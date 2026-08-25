# JAV-GCE-HELLO

HelloWorld is the simplest possible Java program — it prints a single line of text to the console. Small as it is, it contains the essential anatomy shared by every Java program we will ever write: a class, a `main` method, and a statement to execute. In this example we will walk through the source file top to bottom and get familiar with each of these pieces.

If you want to run this program yourself, run it from the command line (no need to compile this one!):

```bash
java HelloWorld.java
```

## The Class

Let's start at the top of the file:

```java
public class HelloWorld {
```

In Java, all code lives inside a **class**. There are no free-floating functions or statements like in some other languages — every method and every line of executable code must belong to a class. A class is declared with the `class` keyword followed by its name, and everything between its opening and closing braces is the body of that class.

One important rule to notice right away: the file name must match the class name. Our class is named `HelloWorld`, so the file must be named `HelloWorld.java`. This is not just a convention — the Java compiler enforces it for public classes, and getting it wrong is a common early stumbling block. Note that the match is case-sensitive: `helloworld.java` would not work.

## The Main Method

Inside the class we find a single method:

```java
public static void main(String[] args) {
```

This is the **main method**, the entry point of a Java program. When we run the program, the **JVM** (Java Virtual Machine) looks for a method with exactly this signature and calls it. Execution starts at the first line inside `main` and proceeds from there. No `main`, no program — a class without one can still be compiled and used by other code, but it cannot be run on its own.

The signature is dense with keywords, and for now we only need to recognize them, not master them:

- `public` is an access modifier — it makes the method visible from outside the class, which the JVM requires in order to call it.
- `static` means the method belongs to the class itself rather than to any particular object. The JVM can call it without creating a `HelloWorld` object first.
- `void` is the return type — this method returns nothing.
- `String[] args` is the method's one parameter: an array of strings holding any command-line arguments passed when the program was launched.

Each of these is a topic we will explore in depth later. For now, it's enough to treat `public static void main(String[] args)` as the fixed incantation that marks where a Java program begins.

## Printing to the Console

The body of `main` contains exactly one statement:

```java
System.out.println("Hello, World!");
```

This line prints text to the console, and it's worth unpacking the pieces. `System` is a built-in Java class that provides access to things in the program's environment. `out` is a member of that class representing the standard output stream — the console. `println` is a method on that stream which prints whatever we pass it, followed by a newline, so anything printed afterward starts on a fresh line.

What we pass to `println` is `"Hello, World!"`, a **string literal** — a fixed sequence of characters written directly in the source code, enclosed in double quotes. The quotes are not part of the output; they simply mark where the text begins and ends.

Notice also that the statement ends with a semicolon. In Java, every statement must be terminated with a `;` — forgetting one is probably the most common compiler error there is.

When we run the program, the result is one line of output:

```
Hello, World!
```

## Conclusion

We've seen the three structural pieces that make up this program: a class named `HelloWorld` in a file of the same name, the `main` method where the JVM begins execution, and a call to `System.out.println()` that writes a string literal to the console. Every Java program we encounter from here on will be built on this same skeleton — a class containing methods, with `main` as the starting point. In future examples we will dig into the keywords we glossed over here, like `public`, `static`, and what it really means for code to belong to a class.
