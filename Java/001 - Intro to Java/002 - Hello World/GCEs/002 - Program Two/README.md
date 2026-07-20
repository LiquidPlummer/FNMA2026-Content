# JAV-GCE-CMDARGS
JAV-GCE-CMDARGS is a small Java program that accepts **command-line arguments** and prints each one to the console. Along the way it introduces something new: this program is made up of *two* classes in two separate files, showing how Java programs can be composed of multiple parts that work together. We'll also meet the **for loop** and see how strings are built with the `+` operator.

The only prerequisite is familiarity with a basic Hello World program — a single class with a `main()` method. Everything else is explained here.

To compile and run the example from the `src/` directory:

```bash
javac Main.java Printer.java
java Main apple banana cherry
```

### Version Information
| Software | Version    |
|----------|------------|
| Java     | 8 or later |

## The Printer Class
Let's start with the smaller of our two files, `Printer.java`:

```java
public class Printer {

    public static void printString(String s) {
        System.out.println(s);
    }
}
```

`Printer` is a **utility class** — a class whose sole job is to provide a helpful behavior, in this case printing. All it really does is wrap `System.out.println()`, so on its own it doesn't accomplish anything new. What it demonstrates is the idea of **organizing code by responsibility**: rather than putting every behavior into one file, we separate concerns into their own classes. Here, anything related to printing belongs to `Printer`. This is a small example of a principle that becomes very important as programs grow, and one we'll explore in depth when we get to object-oriented programming.

The class contains a single **method**, `printString()`. Let's break down its declaration piece by piece:

- `public` — the method is visible and callable from other classes, which matters because we'll be calling it from `Main`.
- `static` — the method belongs to the class itself rather than to any particular object. For now, the practical takeaway is that we can call it directly on the class without any extra setup.
- `void` — the **return type**. This method doesn't hand any value back to its caller; it just performs an action.
- `String s` — the **parameter**. Whoever calls this method must pass in a `String`, and inside the method that string is available under the name `s`.

The method body passes `s` along to `System.out.println()`, which prints it to the console followed by a line break.

## The Main Class
Now for `Main.java`, where the program actually begins:

```java
public class Main {

    public static void main(String[] args) {

        Printer.printString("Command line args: ");

        for (int i = 0; i < args.length; i++) {
            Printer.printString(i + ": " + args[i]);
        }
    }
}
```

The structure here is the same as Hello World: a public class containing a `main()` method. `main()` is the **entry point** of a Java program — when we run the program, the JVM looks for this exact method signature and calls it, and execution proceeds from there.

The first statement inside `main()` is something new:

```java
Printer.printString("Command line args: ");
```

This is a call to a **static method on another class**. Because `printString()` is static, we call it using the class name followed by a dot: `Printer.printString(...)`. We don't need to create a `Printer` object first — the class name alone is enough. The string we pass in, `"Command line args: "`, becomes the parameter `s` inside `printString()`, which prints it as a label before we list the arguments. Calling methods on other classes like this is the core of how multi-file Java programs fit together.

## Command-Line Arguments
Every `main()` method declares the same parameter: `String[] args`. We've been writing it since Hello World, and now we finally get to use it. `args` is an **array of strings** holding the **command-line arguments** — extra values typed after the class name when the program is run. The JVM collects those values and passes them into `main()` for us.

Arguments are supplied at launch, separated by spaces:

```bash
java Main apple banana cherry
```

Here `args` will contain three elements: `"apple"`, `"banana"`, and `"cherry"`. If we run `java Main` with nothing after it, `args` is simply an empty array — it still exists, it just has zero elements.

Every array in Java knows its own size through its `length` property. `args.length` gives us the number of arguments that were provided — `3` in the example above, `0` if none were given. We'll use that count to drive our loop.

Running the command above produces:

```
Command line args:
0: apple
1: banana
2: cherry
```

## The For Loop
To print each argument, we need to repeat the same action once per element. That's exactly what a **for loop** is for:

```java
for (int i = 0; i < args.length; i++) {
    Printer.printString(i + ": " + args[i]);
}
```

The loop header inside the parentheses has three parts, separated by semicolons:

```java
for (int i = 0; i < args.length; i++)
```

The first part, `int i = 0`, is the **initialization**. It runs once, before the loop starts, and creates a counter variable `i` starting at 0. Arrays in Java are **zero-indexed** — the first element sits at position 0 — so 0 is the natural starting point.

The second part, `i < args.length`, is the **condition**. It's checked before every iteration, and the loop keeps running as long as it's true. Once `i` reaches `args.length`, the condition is false and the loop ends. Note that the comparison is `<` and not `<=`: with three arguments, the valid positions are 0, 1, and 2, so the loop must stop before `i` reaches 3.

The third part, `i++`, is the **increment**. It runs after each iteration and adds 1 to `i`, moving the counter forward one position.

Put together, the loop starts at the beginning of the array, runs the body once for each element, and stops at the end. Inside the body, `args[i]` accesses the element at position `i` — on the first pass that's `args[0]`, then `args[1]`, and so on.

## String Concatenation
There's one more thing happening in the loop body worth a closer look:

```java
Printer.printString(i + ": " + args[i]);
```

When the `+` operator is used with strings, it performs **concatenation** — joining values together into a single string. Here we join three pieces: the counter `i`, the literal `": "`, and the current argument `args[i]`.

Notice that `i` is an `int`, not a `String`. When we mix an `int` and a `String` with `+`, Java automatically converts the number to its text form before joining. So on the first iteration with our earlier example, `i + ": " + args[i]` becomes the single string `"0: apple"`, which is then passed to `printString()` and printed. This automatic conversion is what makes it so convenient to build output messages from a mix of text and values.

## Conclusion
We've walked through a complete two-class Java program. The `Printer` class showed how we organize code by responsibility and how a `public static` method can be called from another class using `ClassName.methodName()`. In `Main`, we finally put `String[] args` to work, learning that it holds whatever values are typed after the class name at launch, and that `args.length` tells us how many there are. The for loop's three-part header — initialization, condition, increment — let us visit every element in the array, and string concatenation with `+` let us combine an `int` index and a `String` argument into one printed line. These pieces — multiple classes, arrays, loops, and concatenation — are foundations we'll build on constantly going forward.
