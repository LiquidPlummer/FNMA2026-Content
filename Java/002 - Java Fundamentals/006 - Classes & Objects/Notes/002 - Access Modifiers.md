# Access Modifiers

The raw `BankAccount` from the last lesson has a problem: any code anywhere can write `account.balance = -50;` and no rule in the class can stop it. **Access modifiers** are Java's visibility controls — keywords that declare, for every class and member, *who is allowed to touch this*. They're the enforcement mechanism behind **encapsulation**: an object's data is its own business, reachable only through the methods it chooses to expose.

---

## The Four Levels

From most open to most closed:

| Modifier | Accessible from |
|---|---|
| `public` | everywhere |
| `protected` | same package + subclasses |
| *(none — "package-private")* | same package only |
| `private` | same class only |

Two of these do most of the work right now. **`public`** means "part of my offered surface — call freely." **`private`** means "internal — hands off." The middle two involve packages and inheritance: *package-private* (the default when no modifier is written) limits access to the same package, and **`protected`** adds subclasses — it becomes meaningful with inheritance in the OOP topic.

```java
public class BankAccount {
    private String owner;          // internal state — no outside access
    private double balance;        // internal state

    public void deposit(double amount) {          // public surface
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        balance += amount;
    }

    private void audit(String action) {           // internal helper
        System.out.println(owner + ": " + action);
    }
}
```

*Fields locked down, behavior exposed: the standard shape of a well-encapsulated class.*

With `balance` private, `account.balance = -50;` is now a **compile error** anywhere outside `BankAccount`. The only way in is `deposit`, and `deposit` validates. That's encapsulation in one sentence: *the class's rules travel with the class, because bypassing them doesn't compile.*

---

## Why Hide Anything?

The question every newcomer reasonably asks. The payoffs compound with codebase size:

- **Invariants hold.** Rules like "balance changes are validated and audited" are enforced in one place — the methods — instead of hoped-for at every call site in the program.
- **Change stays cheap.** Private members can be renamed, restructured, or deleted knowing *nothing outside the class can possibly depend on them*. Public members are promises — every one has unknown callers, forever. Small public surface = freedom to refactor.
- **Debugging narrows.** When a private field holds a bad value, the code that did it is *in this file*. When a public field holds a bad value, the suspect list is the whole application.

Hence the working default: **fields `private`, always; methods `public` only if genuinely part of the class's job description; everything else `private`** (or package-private for collaboration within a package). Start closed — opening up later is easy and breaks nothing; closing down later breaks every caller.

---

## Modifiers on Classes

Top-level classes take only two options: `public` (usable from any package) or package-private (no modifier — internal to its package). A `public` class must live in a file matching its name (`BankAccount.java`), which is why the rule "one public class per file" exists. Making helper classes package-private keeps a library's public face small — the same start-closed instinct, one level up.

---

## What Access Control Is — and Isn't

Access modifiers are enforced at **compile time**, on the *type* of the reference, everywhere — there's no "same object" exemption (one `BankAccount` can touch another `BankAccount`'s privates, since that code lives in the same class; this surprises people). They are a *design* tool, not a security boundary — reflection (a Java Advanced topic) can pry open private members at runtime with enough permissions. The point isn't to stop attackers; it's to make the honest path the only easy path, so large codebases stay changeable.

The obvious follow-up — "if fields are private, how does anyone *read* the balance?" — has a dedicated idiom, two lessons ahead (Getters & Setters). First, though: with direct field access gone, objects need a proper way to start life with valid values. That's constructors, next.
