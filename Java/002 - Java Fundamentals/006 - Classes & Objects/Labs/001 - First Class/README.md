# Lab: First Class — fields, constructors, getters/setters, and `this`

In this lab we build `BankAccount` from nothing: a raw bag of public fields first, so we can watch it break, then a proper class with a validating constructor, getters, one deliberately-chosen setter, `this` for shadowed parameters, and `deposit`/`withdraw` methods that actually enforce rules. By the end you'll have typed every line of a small but *real* class, and you'll carry this exact `BankAccount` forward into the next lab.

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

You should see `=== Bank Account Lab ===` followed by `Done.`. That's the starter skeleton: [src/main/java/com/curriculum/labs/BankAccount.java](src/main/java/com/curriculum/labs/BankAccount.java) (the class we're building) and [src/main/java/com/curriculum/labs/BankAccountLab.java](src/main/java/com/curriculum/labs/BankAccountLab.java) (the driver that uses it). Open both now and find the numbered `[MARKER]` comments — the walkthrough below tells you which marker to work at, in which file. Re-run the command above after every part.

---

## Part 1 — Classes vs Objects: a raw struct, and breaking it

A class is a blueprint; `new` builds an actual object — an instance — from it. Our first `BankAccount` is as raw as a class gets: just the state every account has, with nothing guarding it yet.

At **`[MARKER 1]`** in `BankAccount.java`, we'll add:

```java
public String owner;
public double balance;
```

*Two public fields — the state every account HAS. No behavior yet, no rules.*

At **`[MARKER 7]`** in `BankAccountLab.java`, we'll add:

```java
BankAccount checking = new BankAccount();
checking.owner = "Ada";
checking.balance = 100.0;

BankAccount savings = new BankAccount();
savings.owner = "Ada";
savings.balance = 5000.0;

checking.balance += 50.0;
System.out.println(checking.owner + " checking: $" + checking.balance);
System.out.println(savings.owner + " savings: $" + savings.balance);
```

*Two independent objects from one class — depositing into `checking` never touches `savings`, even though both belong to Ada.*

Run it. `checking` and `savings` are separate instances, each with its own copy of `owner` and `balance` — that independence is the entire point of instances. Now let's see the problem with a raw struct. Add one more line right after:

```java
checking.balance = -1000.0;   // nothing in the class stops this
System.out.println(checking.owner + " checking: $" + checking.balance);
```

Run it again: `checking` prints a balance of `-1000.0` and the program is perfectly happy about it. Nothing in `BankAccount` says a balance can't be negative — any code, anywhere, can reach in and set whatever it wants. That's the raw-struct problem, and the rest of this lab fixes it one piece at a time.

---

## Part 2 — Fields & Constructors: validating on the way in

A **constructor** runs once, during `new`, and is our chance to guarantee an object starts life valid. At **`[MARKER 2]`** in `BankAccount.java`, we'll add:

```java
public BankAccount(String owner, double openingBalance) {
    if (owner == null || owner.isBlank()) {
        throw new IllegalArgumentException("owner is required");
    }
    if (openingBalance < 0) {
        throw new IllegalArgumentException("opening balance cannot be negative");
    }
    this.owner = owner;
    this.balance = openingBalance;
}

public BankAccount(String owner) {
    this(owner, 0.0);
}
```

*The main constructor validates, then assigns — `this.owner` is the field, `owner` is the parameter shadowing it, and `this.` tells them apart. The one-argument constructor delegates to it with `this(...)`, so the validation rule lives in exactly one place.*

Writing either constructor removes the free no-argument one Java was giving us — `new BankAccount()` no longer compiles, which is exactly right: an account with no owner shouldn't be constructible. That means Part 1's code at `[MARKER 7]` has to change. Replace it with:

```java
BankAccount checking = new BankAccount("Ada", 100.0);
BankAccount savings = new BankAccount("Ada", 5000.0);

checking.balance += 50.0;
System.out.println(checking.owner + " checking: $" + checking.balance);
System.out.println(savings.owner + " savings: $" + savings.balance);
```

*Same two independent accounts, now built through a constructor instead of three bare assignments each.*

Then at **`[MARKER 8]`**, we'll add a look at what validation buys us:

```java
try {
    BankAccount bad = new BankAccount("Ada", -1000.0);
} catch (IllegalArgumentException e) {
    System.out.println("Rejected: " + e.getMessage());
}

BankAccount fresh = new BankAccount("Grace");     // the one-argument convenience constructor
System.out.println(fresh.owner + " opened with $" + fresh.balance);
```

*A negative opening balance is now rejected before the object even exists — compare this to Part 1, where the same bad value was cheerfully accepted. `fresh` shows the convenience constructor at work: one argument, `this(...)` fills in the rest.*

Run it. The `-1000.0` attempt prints `Rejected: ...` instead of silently producing a broken account, and `fresh` opens at `$0.0` without us having to type `0.0` at the call site.

---

## Part 3 — Getters & Setters: a deliberate decision

With a class actually shaping how accounts get created, let's add the standard way outside code reads (and, sometimes, writes) private... well, for now, still-public state — the getter/setter idiom. At **`[MARKER 3]`** in `BankAccount.java`, we'll add:

```java
public String getOwner() {
    return owner;
}

public double getBalance() {
    return balance;
}
```

*`getX()` returns the field named `x` — the JavaBeans convention every Java tool recognizes.*

Now the setter — and the decision. At **`[MARKER 4]`**, we'll add:

```java
public void setOwner(String owner) {
    if (owner == null || owner.isBlank()) {
        throw new IllegalArgumentException("owner is required");
    }
    this.owner = owner;
}
```

*A setter for `owner` — validated, just like the constructor. `this.owner = owner` disambiguates the field from the parameter, the same trick from Part 2.*

Notice what we're **not** writing: `setBalance(double)`. That's not an oversight — it's a decision. A balance shouldn't jump to any value a caller hands it; it should only move through rules (`deposit`, `withdraw` — next part). A getter/setter pair per field isn't a requirement, it's a choice made field by field, and "no setter" is a legitimate choice when the field's changes need to go through something more specific.

At **`[MARKER 9]`** in `BankAccountLab.java`, we'll add:

```java
System.out.println(checking.getOwner() + " checking: $" + checking.getBalance());
System.out.println(savings.getOwner() + " savings: $" + savings.getBalance());

checking.setOwner("Ada Lovelace");
System.out.println("Renamed to: " + checking.getOwner());
```

*Reading through getters instead of the fields directly, then using the one setter we did write.*

Run it and confirm the rename shows up. The fields are still technically public at this point (that changes next lab) — but we're now *choosing* to go through the getters and setter, the way real code should.

---

## Part 4 — Rules for changing the balance

`deposit` and `withdraw` are where the balance is actually allowed to change — and where we enforce that it changes sensibly. At **`[MARKER 5]`** in `BankAccount.java`, we'll add:

```java
public void deposit(double amount) {
    if (amount <= 0) {
        throw new IllegalArgumentException("deposit amount must be positive");
    }
    balance += amount;
}

public void withdraw(double amount) {
    if (amount <= 0) {
        throw new IllegalArgumentException("withdrawal amount must be positive");
    }
    if (amount > balance) {
        throw new IllegalArgumentException("insufficient funds");
    }
    balance -= amount;
}
```

*Both methods validate before touching `balance` — no setter could enforce "can't overdraw," but a method with real logic can.*

At **`[MARKER 10]`** in `BankAccountLab.java`, we'll add:

```java
checking.deposit(50.0);
checking.withdraw(30.0);
System.out.println(checking.getOwner() + " checking: $" + checking.getBalance());

try {
    checking.withdraw(1_000_000.0);
} catch (IllegalArgumentException e) {
    System.out.println("Rejected: " + e.getMessage());
}
```

*A deposit, a withdrawal, then an oversized withdrawal that gets rejected — the rule `withdraw` enforces that no setter ever could.*

Run it. `checking`'s balance moves by exactly the deposit and withdrawal amounts, and the million-dollar withdrawal is refused with a clear message instead of driving the balance negative.

---

## Part 5 — Printing an account, and a loose end

One convenience before we close out: printing a `BankAccount` directly should show something useful, not the default `BankAccount@1a2b3c4`. At **`[MARKER 6]`** in `BankAccount.java`, we'll add:

```java
@Override
public String toString() {
    return owner + ": $" + balance;
}
```

*`@Override` tells the compiler this is meant to replace `Object`'s `toString` — a typo here (like `tostring`) becomes a compile error instead of a silent no-op, which is why we always write `@Override` when overriding.*

At **`[MARKER 11]`** in `BankAccountLab.java`, we'll add:

```java
System.out.println(checking);

// A "manual correction" — straight to the field, bypassing every rule
// deposit/withdraw enforce. Nothing stops this, because the fields are
// still public. That's exactly what next lab's access modifiers fix.
checking.balance = checking.balance - 10000.0;
System.out.println(checking);
```

*`System.out.println(checking)` calls `toString()` automatically. The line after it is the loose end: even with a validating constructor, getters, a careful setter, and rule-enforcing `deposit`/`withdraw`, a public field is still a field — anyone can still assign straight into it and skip every rule we just wrote.*

Run it. The first print looks clean (`toString` at work); the second shows a balance no `withdraw` call would ever have allowed. That gap — public fields undermining everything else the class does — is exactly what the next lab (Access Modifiers) closes, by making `owner` and `balance` `private` and watching which lines of `main` stop compiling.

---

## Exercises

The training wheels come off — same ideas, new problems, no step-by-step.

1. **`LibraryItem`.** Design and build a `LibraryItem` (or `Book`) class from scratch, to this behavior spec: a constructor requiring a non-blank `title` and a non-blank `isbn`; a `checkedOut` field that starts `false`; a `checkOut()` method that fails (throws) if the item is already checked out, otherwise marks it checked out; a `returnItem()` method that fails if the item isn't checked out, otherwise marks it available; getters for every field; no public setters. Build two `LibraryItem`s and prove they track their checked-out status independently.

2. **Count the transactions.** Add a field to `BankAccount` that counts how many times `deposit` or `withdraw` has successfully run on *that* account (a failed/rejected call shouldn't count). Expose it with a getter. Prove — with two accounts — that each account's count only reflects transactions run on it.

3. **A getter that leaks.** Add a `java.util.List<String> history` field to `BankAccount` (a running log of what happened — e.g. `"deposit 50.0"` each time `deposit` runs), and a `getHistory()` getter that simply `return`s the list. Then, from `main`, call `getHistory()` and `.clear()` the list you get back — and watch it wipe out the account's own history. Fix it: change `getHistory()` to return a defensive copy (`List.copyOf(history)`) instead, and confirm the same `.clear()` call no longer affects the account.
