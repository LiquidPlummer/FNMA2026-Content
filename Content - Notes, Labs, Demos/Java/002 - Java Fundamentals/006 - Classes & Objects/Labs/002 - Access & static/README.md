# Lab: Access & static — locking down `BankAccount`, and class-wide state

This lab picks up exactly where **First Class** left off: the same `BankAccount`, public fields and all, still carrying that one loose end from the last lab — a manual correction that reached straight past every rule `deposit`/`withdraw` enforce. Here we close that gap with `private`, then add the class-wide machinery that belongs to `BankAccount` itself rather than to any one account: a shared counter, generated ids, constants, and a static utility method.

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

You should see the same output First Class ended on — including that questionable `$-9830.0` balance from the manual correction. That's [src/main/java/com/curriculum/labs/BankAccount.java](src/main/java/com/curriculum/labs/BankAccount.java) and [src/main/java/com/curriculum/labs/BankAccountLab.java](src/main/java/com/curriculum/labs/BankAccountLab.java), carried forward unchanged. Open both now and find the numbered `[MARKER]` comments — the walkthrough below tells you which marker to work at, in which file. Re-run the command above after every part.

---

## Part 1 — Access Modifiers: the compiler as encapsulation tutor

At **`[MARKER 1]`** in `BankAccount.java`, change the two fields from `public` to `private`:

```java
private String owner;
private double balance;
```

*Same two fields, one word changed each — now nothing outside `BankAccount` can touch them directly.*

Try to compile:

```console
mvn -q compile
```

It fails — with **9 errors**, all shaped like `owner has private access in BankAccount` or `balance has private access in BankAccount`, pointing at five lines in `BankAccountLab.java` (at **`[MARKER 6]`** and **`[MARKER 7]`**). That's the compiler doing exactly what access modifiers are for: every line that reached past the class's own methods straight into its fields just became impossible to write. Let's fix each one.

The first three (the ones at `[MARKER 6]`) all read or write a field directly where a method already exists to do the job properly. Replace:

```java
checking.balance += 50.0;
System.out.println(checking.owner + " checking: $" + checking.balance);
System.out.println(savings.owner + " savings: $" + savings.balance);
```

with:

```java
checking.deposit(50.0);
System.out.println(checking.getOwner() + " checking: $" + checking.getBalance());
System.out.println(savings.getOwner() + " savings: $" + savings.getBalance());
```

*The same deposit and the same two prints — now through `deposit` (which validates) and the getters (which First Class already gave us), instead of the raw fields.*

A little further down, still at `[MARKER 6]`, do the same for:

```java
System.out.println(fresh.owner + " opened with $" + fresh.balance);   // [MARKER 6] this one too
```

Replace it with:

```java
System.out.println(fresh.getOwner() + " opened with $" + fresh.getBalance());
```

Last one: at **`[MARKER 7]`**, the manual correction from the end of First Class:

```java
// [MARKER 7] Part 1: this manual correction won't even compile once fields are private — delete it.
checking.balance = checking.balance - 10000.0;
System.out.println(checking);
```

Delete the marker comment and the `checking.balance = ...` line entirely (leave the `System.out.println(checking);` above it, which was already there). There's no getter-based replacement for this one — it doesn't get fixed, it gets *removed*, because it was never a legitimate operation. That's the whole point of the lesson: a `-1000.0`-in-a-constructor mistake gets caught by validation, but a `checking.balance = anything` mistake used to not even need a mistake — it was just always available. It no longer is.

Run `mvn -q compile exec:java` again. It compiles clean and runs — minus that one bogus balance at the end. **Every remaining line in `main` goes through a method `BankAccount` chose to expose.** That's encapsulation, enforced by the compiler rather than by good intentions.

---

## Part 2 — static: one counter, shared by every account

Some state belongs to the *concept* of a bank account, not to any one instance — how many accounts have ever been opened, for instance. At **`[MARKER 2]`** in `BankAccount.java`, we'll add:

```java
private static int accountsOpened = 0;

private final String id;
```

*`accountsOpened` is `static` — one copy for the whole class, not one per object. `id` stays a normal instance field — every account has its own.*

Now wire both into the constructor. In the main constructor (just above `this.owner = owner;`), add:

```java
accountsOpened++;
this.id = "ACCT-" + accountsOpened;
```

*Every call to `new BankAccount(...)` bumps the one shared counter, then stamps that account with an id built from it.* Because the one-argument constructor delegates to this one with `this(...)`, it gets an id too, automatically — one place generates ids, no matter which constructor a caller used.

Add a getter for each — a static one for the static field, an instance one for the instance field:

```java
public static int getAccountsOpened() {
    return accountsOpened;
}

public String getId() {
    return id;
}
```

*`getAccountsOpened()` is `static` because it reports on the class, not on any one account — call it as `BankAccount.getAccountsOpened()`, never through an instance.*

At **`[MARKER 8]`** in `BankAccountLab.java`, we'll add:

```java
System.out.println("Accounts opened: " + BankAccount.getAccountsOpened());
System.out.println(checking.getId() + ", " + savings.getId() + ", " + fresh.getId());
```

*Reading the shared counter through the class name, and each account's own generated id through the instance.*

Run it. `Accounts opened: 3` — every account so far — and three different ids (`ACCT-1`, `ACCT-2`, `ACCT-3`), one per account, generated automatically. No account had to build its own id; the class handled it centrally.

---

## Part 3 — static final: naming the constants

`deposit` and `withdraw` both hardcode `0` as "the smallest allowed amount" — a magic number with no name. At **`[MARKER 3]`**, we'll add:

```java
public static final String ID_PREFIX = "ACCT-";
public static final double MINIMUM_DEPOSIT = 0.01;
```

*`static final` — one shared, unchangeable copy per constant, `UPPER_SNAKE_CASE` by convention. `public` because callers legitimately need to know the minimum.*

Now use `ID_PREFIX` in the constructor instead of the literal string — change:

```java
this.id = "ACCT-" + accountsOpened;
```

to:

```java
this.id = ID_PREFIX + accountsOpened;
```

And in `deposit` and `withdraw`, replace the `amount <= 0` checks with the named constant. `deposit` becomes:

```java
public void deposit(double amount) {
    if (amount < MINIMUM_DEPOSIT) {
        throw new IllegalArgumentException("deposit amount must be at least " + MINIMUM_DEPOSIT);
    }
    balance += amount;
}
```

*Same rule, but `MINIMUM_DEPOSIT` in one place beats `0` scattered across every method that cares about it — change the rule once, both methods pick it up.*

Make the equivalent change in `withdraw` (its own `amount <= 0` check becomes `amount < MINIMUM_DEPOSIT`, with a matching message). Run `mvn -q compile exec:java` — everything still behaves the same; we've only given names to values that were already there.

---

## Part 4 — Static or instance? A method that should be, and one that shouldn't

Not every helper method needs an object to run against. At **`[MARKER 4]`**, we'll add:

```java
public static boolean isValidId(String id) {
    return id != null && id.matches(ID_PREFIX + "\\d+");
}
```

*`isValidId` only ever looks at its parameter — it needs no particular account's `owner` or `balance` to do its job, so it's `static`: a pure function of its input, called as `BankAccount.isValidId(...)`.*

Now the contrast. Suppose we want a one-line audit summary for an account — id, owner, and balance together. Try writing it as `static` first, at **`[MARKER 5]`**:

```java
public static String auditLabel() {
    return id + " (" + owner + "): $" + balance;
}
```

Run `mvn -q compile`. It fails: `non-static variable id cannot be referenced from a static context` (and the same for `owner` and `balance`). This is the error from the `this` lesson, back again — a `static` method has no `this`, so it has no particular account's `id`, `owner`, or `balance` to read. `auditLabel()` is fundamentally about *one account's* state, which means it can't be static. Fix it by deleting `static`:

```java
public String auditLabel() {
    return id + " (" + owner + "): $" + balance;
}
```

*Same body, no `static` — now it's an instance method, `this` exists again, and `id`/`owner`/`balance` resolve to whichever account it's called on.*

At **`[MARKER 9]`** in `BankAccountLab.java`, we'll add:

```java
System.out.println("Is 'ACCT-2' valid? " + BankAccount.isValidId("ACCT-2"));
System.out.println("Is 'ACCT-x' valid? " + BankAccount.isValidId("ACCT-x"));
System.out.println(checking.auditLabel());
```

*`isValidId` called on the class (no account involved); `auditLabel` called on `checking` (needs one).*

Run it: `isValidId` correctly separates a well-formed id from garbage, and `auditLabel()` prints `checking`'s own id, owner, and balance together. The rule that separates these two methods is the one to keep: **needs an object's own state → instance method. Depends only on its inputs → static.**

---

## Exercises

The training wheels come off — same ideas, new problems, no step-by-step.

1. **Public, private, or static — justify each.** You're handed a `ShoppingCart` class with these members: `items` (a `List<String>`), `taxRate` (same for every cart in the store), `addItem(String item)`, `applyDiscount(double percent)` (a helper only `addItem` should ever call), `totalItemsSoldStoreWide` (a running count across every cart that's ever existed), and `formatCurrency(double amount)` (turns a `double` into a `"$12.34"`-style string, using nothing but its argument). For each member, decide: `public` or `private`? `static` or instance? Write one sentence of justification per member before you write a line of code — then implement the class.

2. **A `Temperature` utility class.** Build a `Temperature` class that is nothing but `static` methods: `celsiusToFahrenheit(double c)`, `fahrenheitToCelsius(double f)`, and `isFreezing(double celsius)` (true at or below 0°C). Give it a `private` constructor so `new Temperature()` doesn't compile — nobody should ever need an instance of it.

3. **Find the shared-state bug.** This class has a bug living in its `static` field:

   ```java
   public class TicketCounter {
       private static int nextTicketNumber = 1;

       public int issueTicket() {
           int ticket = nextTicketNumber;
           nextTicketNumber++;
           return ticket;
       }

       public static int peekNext() {
           return nextTicketNumber;
       }
   }
   ```

   Create two `TicketCounter` instances and call `issueTicket()` on each, alternating between them, several times. Print each ticket number as it's issued. Explain — in a comment — what you observe about the sequence of numbers *across* the two instances, and why. Then decide: is `nextTicketNumber` being `static` a bug, or is it correct for what a "ticket counter" should do? Justify your answer.
