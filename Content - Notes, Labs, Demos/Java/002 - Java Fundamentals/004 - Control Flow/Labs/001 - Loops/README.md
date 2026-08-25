# Lab: Loops — while, do-while, for, and for-each

In this lab we build a small interactive console app, the **Loop Lab**, and in the process write every loop Java has: a `while` loop for repeat-until-a-condition work, a `do-while` menu that must run at least once, a counted `for` loop, and a `for-each` loop over an array. By the end, the app has a working menu with three loop-powered features — and you'll have typed every loop in it yourself.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

Verify both with `java -version` and `mvn -version`. Then, from this lab's folder (the one containing `pom.xml`), compile and run:

```console
mvn -q compile exec:java
```

You should see `=== Loop Lab ===` followed by `Goodbye!`. That's the starter skeleton in [src/main/java/com/curriculum/labs/LoopLab.java](src/main/java/com/curriculum/labs/LoopLab.java) — open it now and find the numbered `[MARKER]` comments. Each part of the walkthrough below tells you which marker to work at. Re-run the command above after every part to see your progress.

---

## Part 1 — `while`: repeat until a condition changes

A `while` loop is for work where we *can't predict* how many repetitions we need — we only know the condition that ends them. Our first feature: given a starting number, how many times can it be halved before it reaches 1?

At **`[MARKER 1]`** in `main`, we'll add:

```java
System.out.print("Enter a starting number: ");
int number = Integer.parseInt(scanner.nextLine());

int halvings = 0;
while (number > 1) {
    number = number / 2;
    halvings++;
}
System.out.println("Reached " + number + " after " + halvings + " halvings.");
```

*Reads a number, then halves it repeatedly — the loop body runs as long as the condition `number > 1` holds.*

Run it and try `100` (6 halvings), then `1000000`. Now the interesting one: enter `1`. The answer is `0` halvings — the condition was false on the very first check, so the body never ran at all. **A `while` loop runs zero or more times.** Hold that thought; it's the exact property the next loop trades away.

Notice also what makes the loop *terminate*: the body changes `number`, moving it toward the condition failing. Delete the `number = number / 2;` line mentally — the loop would never end. Every `while` loop needs its body to make progress.

---

## Part 2 — `do-while`: run first, ask questions after

Right now the program does one thing and exits. Real console tools show a menu, do what you pick, and ask again — and a menu must be shown *at least once* before we can know whether the user wants to quit. That "at least once" is precisely what `do-while` guarantees: body first, condition check after.

At **`[MARKER 2]`**, we'll add the menu structure below — and then **move your Part 1 halving code** (everything from `System.out.print("Enter a starting number..."` down) into the spot marked `// option 1 code goes here`:

```java
String choice;
do {
    System.out.println();
    System.out.println("1) Halving counter");
    System.out.println("2) Countdown        (coming in Part 3)");
    System.out.println("3) Score average    (coming in Part 4)");
    System.out.println("q) Quit");
    System.out.print("> ");
    choice = scanner.nextLine().trim();

    if (choice.equals("1")) {
        // option 1 code goes here  (move your Part 1 halving code into this block)
    } else if (!choice.equals("q")) {
        System.out.println("Unknown option: " + choice);
    }
} while (!choice.equals("q"));
```

*A menu loop: print the options, read a choice, act on it — and only then decide whether to go around again.*

Run it. The menu appears immediately (the body ran before any condition was tested), option `1` runs the halving feature as many times as you like, and `q` falls through to `Goodbye!`. One structural detail worth noticing as you type: `choice` is declared *outside* the loop — the `while (...)` condition at the bottom needs it, and a variable declared inside the block would be out of scope there (the scope rules from Variables & Types, showing up in real code).

---

## Part 3 — `for`: counting, the compact way

When we *do* know how many iterations we want, the `for` loop packs the counter, the condition, and the update into one line. Feature two is a launch countdown.

At **`[MARKER 3]`** — below `main`, as a separate method — we'll add:

```java
static void countdown(int start) {
    for (int i = start; i >= 1; i--) {
        System.out.println(i + "...");
    }
    System.out.println("Liftoff!");
}
```

*A counted loop, downward: initialize `i` at `start`, run while `i >= 1`, subtract 1 each pass.*

Then wire it into the menu — in Part 2's `if`/`else if` chain, add this branch between option 1 and the unknown-option case:

```java
} else if (choice.equals("2")) {
    System.out.print("Count down from: ");
    int start = Integer.parseInt(scanner.nextLine());
    countdown(start);
```

*Menu option 2: read the starting number and hand it to our new method.*

Run it and count down from `5`. Note the loop variable `i` exists *only inside* the loop — after `Liftoff!`, `i` is gone, which is why the same name can be reused by every loop in the program. And compare with Part 1: the halving loop couldn't have been a clean `for`, because nothing there counts by a fixed step. **`for` when you know the trip count or need the index; `while` when you only know the stopping condition.**

---

## Part 4 — for-each: every element, no index bookkeeping

The last loop is for the most common job in real code: *do something with every element of a collection*. The starter file has been carrying a field for exactly this moment — `QUIZ_SCORES`, an array of quiz results where `-1` marks a student who was absent.

At **`[MARKER 4]`**, we'll add:

```java
static void averageScores(int[] scores) {
    int sum = 0;
    int counted = 0;
    for (int score : scores) {
        if (score == -1) {
            continue;                    // absent — skip this element, keep looping
        }
        sum += score;
        counted++;
    }
    System.out.println("Scores counted: " + counted);
    System.out.println("Average: " + (sum / (double) counted));
}
```

*For each `score` in `scores`: skip the `-1` sentinels with `continue`, accumulate the rest.*

Read the header aloud — "for each `score` in `scores`" — and notice everything that *isn't* there: no counter, no `scores.length`, no `scores[i]`, no off-by-one risk. The loop hands us each element's value in order, and `continue` (from the notes) abandons just the current pass. The cast to `double` in the average is the integer-division rule from Type Casting — without it, `sum / counted` would silently truncate.

Wire it into the menu with one more branch:

```java
} else if (choice.equals("3")) {
    averageScores(QUIZ_SCORES);
```

*Menu option 3: run the average over the built-in score array.*

Run option `3`: six scores counted (two absences skipped), average `83.0`.

One forward pointer, in passing: `for-each` isn't just for arrays — it works on `List`, `Set`, and anything else implementing the `Iterable` interface, which supplies the *iterator* that walks the elements behind the scenes. We'll meet that machinery properly in the Collections Framework topic; for now, it's enough to know the same loop syntax will carry over unchanged.

---

## Exercises

The training wheels come off — same concepts, new problems, no step-by-step. Add each as a new menu option (or a separate method you call from one). For each exercise, decide *first* which loop fits and why.

1. **Three strikes.** Prompt for a password that must be at least 8 characters, re-prompting until the input qualifies — but allow at most 3 attempts, then print `Locked out.` and give up. (Think about which part is "at least once" and which part is a countable limit.)

2. **Times table.** Prompt for a number `n` and print an `n × n` multiplication table, rows and columns labeled 1 through `n`. One loop won't be enough.

3. **Longest passing streak.** Using `QUIZ_SCORES` (treat `-1` as a break in the streak), find the length of the longest *consecutive* run of passing scores (70 or higher) and print it. All the data flows in one direction — a `for-each` can do it, if you track the right things between iterations.

4. **Collatz counter.** Prompt for a positive integer. Repeatedly transform it — halve it if it's even, replace it with `3n + 1` if it's odd — until it reaches 1, then print how many steps that took. Nobody can tell you in advance how many iterations this needs, which should tell you which loop to reach for.

5. **Rewind.** Print `QUIZ_SCORES` in reverse order, separated by spaces. A `for-each` can't do this one — be ready to say why.
