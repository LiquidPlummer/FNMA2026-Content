# Control Flow — For Loops

Programs frequently need to repeat an action: print each command-line argument, process each item in a list, count from one number to another. A **for loop** is Java's primary tool for repetition when we know, or can compute, how many times to repeat.

---

## What a For Loop Is

A **for loop** runs a block of code repeatedly, managing a counter as it goes. Here's the shape, printing the numbers 0 through 4:

```java
for (int i = 0; i < 5; i++) {
    System.out.println("i is " + i);
}
```

Output:

```
i is 0
i is 1
i is 2
i is 3
i is 4
```

The loop keeps a counter variable (`i`), checks a condition before each pass, runs the body if the condition holds, updates the counter, and checks again — until the condition becomes false, at which point execution continues with whatever comes after the loop.

---

## The Three Parts of the For Loop Header

Everything that controls the loop sits in the header, inside the parentheses, separated by semicolons:

```java
for (initialization; condition; increment)
```

- **Initialization** — runs *once*, before the loop starts. This is where we declare and set the counter: `int i = 0`. A variable declared here exists only inside the loop.
- **Condition** — checked *before every pass*, including the first. The body runs only while this is `true`: `i < 5`. When it's `false`, the loop ends. If it's false on the very first check, the body never runs at all.
- **Increment** — runs *after each pass* of the body, typically advancing the counter: `i++` (shorthand for `i = i + 1`).

So the exact order of events is: initialize → check condition → run body → increment → check condition → run body → increment → ... → condition false → done.

The counter doesn't have to start at 0 or step by 1 — we can count down, skip values, or use any condition:

```java
for (int i = 10; i > 0; i--) {       // countdown: 10, 9, 8, ... 1
    System.out.println(i);
}

for (int i = 0; i <= 100; i += 10) { // 0, 10, 20, ... 100
    System.out.println(i);
}
```

Two classic mistakes to watch for. An **off-by-one error** — using `<=` where we meant `<` (or vice versa) — makes the loop run one time too many or too few. And an **infinite loop** happens when the increment never moves the condition toward false (for example, writing `i--` in a loop that needs `i++` to terminate).

---

## Iterating Over an Array

The most common use of a for loop in our early programs is visiting every element of an array. The pattern: start the counter at `0` (the first index), continue while it's *less than* the array's `length`, and use the counter as the index:

```java
String[] fruits = {"apple", "banana", "cherry"};

for (int i = 0; i < fruits.length; i++) {
    System.out.println("Fruit " + i + ": " + fruits[i]);
}
```

Output:

```
Fruit 0: apple
Fruit 1: banana
Fruit 2: cherry
```

The condition is `i < fruits.length`, not `<=`. Array indexes run from `0` to `length - 1`, so a three-element array has valid indexes 0, 1, and 2. Using `<=` would attempt `fruits[3]` and crash with an `ArrayIndexOutOfBoundsException`.

This is exactly the pattern the command-line args printer uses — `args` is a `String[]` like any other:

```java
public class ArgsPrinter {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println("Argument " + i + ": " + args[i]);
        }
    }
}
```

If no arguments are passed, `args.length` is 0, the condition `0 < 0` is false immediately, and the body simply never runs — no crash, no output. The loop handles the empty case for free.

---

## The Loop Body

The **loop body** is the block between the braces — the code that runs on every pass. It can contain any number of statements, and they all execute, in order, each time through:

```java
int[] scores = {88, 92, 75, 96};
int total = 0;

for (int i = 0; i < scores.length; i++) {
    total += scores[i];
    System.out.println("Running total after score " + i + ": " + total);
}

System.out.println("Final total: " + total);   // 351
```

A few details about the body worth knowing:

- Variables declared *inside* the body (or in the header) vanish when the loop ends. To accumulate a result like `total` above, declare it *before* the loop.
- The braces are technically optional when the body is a single statement, but we should always write them — omitting braces makes it easy to add a second line later and have it silently execute outside the loop.
- Loops can be **nested**: a loop body can contain another loop, which completes all of its passes for each pass of the outer loop. We'll use that pattern later for working with grids and tables.
