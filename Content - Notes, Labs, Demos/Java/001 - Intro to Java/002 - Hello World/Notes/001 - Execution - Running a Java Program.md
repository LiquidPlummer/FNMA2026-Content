# Execution — Running a Java Program

Every Java program starts in the same place: the `main` method. When we run a program, the **JVM** (Java Virtual Machine) loads our class, finds `main`, and executes it from top to bottom. Understanding that one method is the key to reading our first programs.

---

## The `main` Method as the Entry Point

A Java program can contain many classes and many methods, but execution always begins at exactly one spot — the **entry point**. In Java, that entry point is a method named `main`:

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

When we run this program, the JVM calls `main`, the statements inside it execute in order, and when the last statement finishes, the program ends. That's the entire lifecycle of a simple program: start at the top of `main`, stop at the bottom.

---

## The `main` Method Signature

The line `public static void main(String[] args)` is the **method signature** — the declaration that describes the method. The JVM is strict about it: every part has to be exactly right, or the JVM won't recognize the method as an entry point.

```java
public static void main(String[] args)
```

Breaking it down piece by piece:

- **`public`** — the method is visible from anywhere. The JVM lives outside our class, so it needs public access to call `main`.
- **`static`** — the method belongs to the class itself rather than to an object. This lets the JVM call `main` without first creating an instance of our class. We'll look at `static` more closely in the notes on classes.
- **`void`** — the method returns nothing. The JVM doesn't expect a value back from `main`.
- **`main`** — the name the JVM looks for. It must be spelled exactly this way, all lowercase.
- **`String[] args`** — a single parameter: an array of strings holding the command-line arguments. The name `args` is convention; we could call it anything, but the type must be `String[]`.

A common gotcha: if we get any part wrong — say, forgetting `static` or misspelling `main` — the program still *compiles*, but the JVM fails at runtime with an error like `Main method not found`. The compiler doesn't care about the signature; only the JVM does.

---

## Command-Line Arguments and `String[] args`

**Command-line arguments** are extra values we pass to a program when we launch it. They let the same program behave differently from run to run without changing the code.

When we run a program from the terminal, anything we type after the class name becomes an argument:

```bash
java ArgsPrinter apple banana cherry
```

The JVM collects those words into a `String[]` array and passes it to `main` as `args`. In this example:

- `args[0]` is `"apple"`
- `args[1]` is `"banana"`
- `args[2]` is `"cherry"`
- `args.length` is `3`

Every argument arrives as a `String`, even if it looks like a number — `java Calc 42` gives us the string `"42"`, not the integer `42`.

If we pass no arguments at all, `args` is not `null` — it's an empty array with `length` of `0`. A program that accesses `args[0]` without checking will crash with an `ArrayIndexOutOfBoundsException` when run with no arguments:

```java
public class ArgsPrinter {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments provided.");
        } else {
            System.out.println("First argument: " + args[0]);
        }
    }
}
```

---

## How the JVM Starts and Stops a Program

Java source code doesn't run directly on our machine. The path from source file to running program has two steps:

1. **Compile.** The `javac` compiler translates our `.java` source file into a `.class` file containing **bytecode** — a portable instruction format the JVM understands.
2. **Run.** The `java` command starts a JVM, which loads the `.class` file, finds the `main` method, and begins executing it.

```bash
javac HelloWorld.java    # produces HelloWorld.class
java HelloWorld          # starts the JVM and runs main
```

This two-step design is what makes Java portable: the same `.class` file runs on Windows, macOS, or Linux, because each platform has its own JVM that handles the platform-specific details.

The program stops when one of these happens:

- **`main` finishes.** Execution reaches the end of the `main` method — the normal case.
- **An uncaught exception occurs.** The JVM prints an error (a stack trace) and exits.
- **The code explicitly exits.** A call to `System.exit(0)` ends the program immediately, wherever it appears.

When the program stops, the JVM shuts down and returns an **exit code** to the operating system — `0` means success, and any non-zero value signals an error. For the simple programs we're starting with, the flow is just: JVM starts, `main` runs top to bottom, JVM stops.
