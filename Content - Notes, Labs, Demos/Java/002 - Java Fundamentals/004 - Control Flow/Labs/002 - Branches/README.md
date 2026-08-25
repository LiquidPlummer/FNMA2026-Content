# Lab: Branches — if/else, the ternary operator, and switch

In this lab we build a small interactive console app, the **Ticket Router**, that classifies a support ticket from a few pieces of input. Along the way we write an `if`/`else if` ladder for a range-based decision, a ternary for a two-value label, a traditional `switch` — where we'll deliberately leave out a `break` and watch it quietly corrupt the output — and finally the modern arrow-form `switch` expression that makes that whole bug class impossible. By the end, the app reads a ticket's severity, timing, and category, and prints a full routing decision — and you'll have typed every branch in it yourself.

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

You should see `=== Ticket Router ===` followed by `Done.`. That's the starter skeleton in [src/main/java/com/curriculum/labs/TicketRouter.java](src/main/java/com/curriculum/labs/TicketRouter.java) — open it now and find the numbered `[MARKER]` comments. Each part of the walkthrough below tells you which marker to work at. Re-run the command above after every part to see your progress.

---

## Part 1 — `if` / `else if`: priority bands from a range

A ticket's severity arrives as a number, `1` to `100`, and we need to sort it into one of four priority bands. That's a decision over *ranges* — `switch` has no way to say "any value from 70 to 89," so this is exactly the job an `if`/`else if` ladder is for.

At **`[MARKER 1]`** in `main`, we'll add:

```java
System.out.print("Enter ticket severity (1-100): ");
int severity = Integer.parseInt(scanner.nextLine());

String priority;
if (severity >= 90) {
    priority = "P1 - Critical";
} else if (severity >= 70) {
    priority = "P2 - High";
} else if (severity >= 40) {
    priority = "P3 - Medium";
} else {
    priority = "P4 - Low";
}
System.out.println("Priority: " + priority);
```

*Reads the severity, then tests each band top to bottom — the first condition that's true wins, and everything below it is skipped.*

Run it and try `95` (P1), `75` (P2), `50` (P3), and `10` (P4). Notice why order matters: `severity >= 70` only gets reached once `severity >= 90` has already failed, which is what makes it safely mean "70 to 89" rather than "70 and up." Swap the first two branches and `95` would wrongly report P2 — try it, see the bug, then put the order back.

---

## Part 2 — the ternary operator: a two-value label

Next we record *when* the ticket came in. We don't need a whole decision tree for this — just one of two labels, which is exactly what the conditional operator `? :` is for: an `if`/`else` that produces a value instead of running a block.

At **`[MARKER 2]`**, we'll add:

```java
System.out.print("Was this reported after business hours? (y/n): ");
boolean afterHours = scanner.nextLine().trim().equalsIgnoreCase("y");

String window = afterHours ? "after-hours" : "business-hours";
System.out.println("Reported: " + window);
```

*Condition, `?`, value-if-true, `:`, value-if-false — `afterHours` picks which string `window` gets.*

Run it and answer `y`, then `n`. One ternary like this is idiomatic and easy to read at a glance; if you ever find yourself nesting a second `? :` inside one of the branches to squeeze in a third outcome, that's the signal to stop and write an `if`/`else if` ladder instead.

---

## Part 3 — `switch`: matching a category (and a bug on purpose)

The last piece of information is the ticket's category — one of a fixed, known set of strings, which is precisely the case `switch` handles more directly than a chain of `.equals()` checks in an `if` ladder. We're going to type this one exactly as shown, including a bug, so we can watch what it does before we fix it.

At **`[MARKER 3]`**, we'll add:

```java
System.out.print("Enter category (BUG, FEATURE, OUTAGE, QUESTION): ");
String category = scanner.nextLine().trim().toUpperCase();

String team;
switch (category) {
    case "BUG":
        team = "Engineering";
    case "FEATURE":
        team = "Product";
        break;
    case "OUTAGE":
        team = "Infrastructure";
        break;
    default:
        team = "Support";
        break;
}
System.out.println("Routed to (traditional switch): " + team);
```

*A traditional switch: each `case` is an entry point, and `break` is what stops execution from continuing into the next one.*

Run it and enter `BUG`. Read the output carefully — it says `Routed to (traditional switch): Product`, not `Engineering`. Nothing crashed; the program just quietly did the wrong thing. That's **fall-through**: the `BUG` case has no `break`, so after `team = "Engineering";` runs, execution falls straight into the `FEATURE` case's code and overwrites `team` with `"Product"` before finally hitting *that* `break`. This is the exact bug the notes warned about — a forgotten `break` doesn't announce itself, it just silently corrupts a value, which is why it's shipped in real software more than once.

Fix it now — add a `break;` right after `team = "Engineering";` in the `"BUG"` case. Re-run with `BUG` and confirm you now get `Engineering`. Keep this corrected version; Part 4 rewrites it.

---

## Part 4 — switch expressions: the same routing, fall-through-proof

Modern Java (14+) adds an arrow form of `switch` that is an *expression* — it produces a value directly, every arm is self-contained, and fall-through simply isn't part of the syntax. We'll rewrite Part 3's logic with it.

At **`[MARKER 4]`**, we'll add:

```java
String teamExpr = switch (category) {
    case "BUG" -> "Engineering";
    case "FEATURE" -> "Product";
    case "OUTAGE" -> "Infrastructure";
    default -> "Support";
};
System.out.println("Routed to (switch expression): " + teamExpr);
```

*Each arm runs on its own — no `break` needed, and none of them can leak into the next.*

Run it with each category and confirm `BUG` now correctly and unambiguously prints `Engineering`, with no way to get that wrong the way Part 3 did. Notice too that `default` is still required here: the compiler demands a switch *expression* be **exhaustive** — every possible input covered — and since `category` is a `String`, there's no fixed list of values the compiler can check against, so `default` is the only way to promise "everything else is handled." (Switching on an `enum` is the one case where the compiler can verify exhaustiveness itself, with a case per constant and no `default` required — a preview of a pairing we'll return to in Classes & Objects.)

---

## Exercises

The training wheels come off — same concepts, new problems, no step-by-step.

1. **Grade-band calculator.** Write a method (or add code inline) that reads a numeric score, `0`-`100`, and prints a letter grade: `A` for 90+, `B` for 80-89, `C` for 70-79, `D` for 60-69, `F` below that. Same shape as Part 1 — decide for yourself why this has to be a ladder and can't be a `switch`.

2. **Leap-year rule.** Prompt for a year and print whether it's a leap year. The real rule is compound boolean logic, not a single comparison: a year is a leap year if it's divisible by 4, *except* century years (divisible by 100), *unless* they're also divisible by 400. (`1900` was not a leap year; `2000` was.) Write this as one boolean expression, or build it up with nested `if`s — either way, test all three cases.

3. **Ladder-to-switch, and one that can't.** Below is a shipping-method ladder. Rewrite it as a `switch` (traditional or arrow-form, your choice):

   ```java
   String label;
   if (method.equals("STANDARD")) {
       label = "5-7 business days";
   } else if (method.equals("EXPRESS")) {
       label = "2-3 business days";
   } else if (method.equals("OVERNIGHT")) {
       label = "next business day";
   } else {
       label = "unknown method";
   }
   ```

   Then look back at Part 1's priority-band ladder and explain, in a comment, *why* that one can't be rewritten as a `switch` no matter how it's arranged — what's fundamentally different about what it's testing.

4. **Flatten the nesting.** The eligibility check below decides whether a ticket qualifies for auto-close, but it's nested three `if`s deep and hard to follow. Rewrite it using **guard clauses** (early `return`s that reject the bad cases first, leaving the success path unindented and last) — the technique from the Jumps notes:

   ```java
   static boolean isAutoCloseEligible(String category, int severity, boolean afterHours) {
       boolean eligible;
       if (category != null) {
           if (category.equals("QUESTION")) {
               if (severity < 20) {
                   if (!afterHours) {
                       eligible = true;
                   } else {
                       eligible = false;
                   }
               } else {
                   eligible = false;
               }
           } else {
               eligible = false;
           }
       } else {
           eligible = false;
       }
       return eligible;
   }
   ```

   Your rewrite should behave identically for every input but read as a short list of rejections followed by one success line.
