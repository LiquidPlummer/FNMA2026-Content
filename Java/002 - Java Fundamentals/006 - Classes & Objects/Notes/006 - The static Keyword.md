# The static Keyword

Every member we've added to `BankAccount` so far is *per-object*: each account has its own `balance`, and `deposit` runs against one particular instance. But some things belong to the *concept*, not to any individual — the count of all accounts, the bank's routing number, a utility that validates account numbers. **`static`** marks a member as belonging to the **class itself**: one copy, shared everywhere, existing whether or not any instance does.

---

## Static Fields: One Copy, Shared

An instance field exists once per object; a **static field** exists once per class:

```java
public class BankAccount {
    private static int accountsOpened = 0;     // one counter for ALL accounts
    private static final double OVERDRAFT_FEE = 35.0;   // shared constant

    private final String id;                   // per-instance, as usual
    private double balance;

    public BankAccount(String owner) {
        accountsOpened++;                      // every construction bumps the shared counter
        this.id = "ACCT-" + accountsOpened;
    }

    public static int getAccountsOpened() {    // static getter for static state
        return accountsOpened;
    }
}
```

*`accountsOpened` is class-wide state — all constructors increment the same variable.*

Access goes through the class name — `BankAccount.getAccountsOpened()` — matching the static calls we've made all along (`Math.sqrt`, `Integer.parseInt`). Java tolerates reaching statics through an instance reference (`myAccount.getAccountsOpened()`), but it misleads readers into thinking something per-object is happening; always use the class name.

The `static final` pair is the **constant idiom** from Variables & Types (`MAX_RETRIES`, `Math.PI`): one immutable copy, `UPPER_SNAKE_CASE`, typically `public` when callers need it.

---

## Static Methods: No Instance Required

A **static method** runs without any object — and consequently, as the previous lesson established, has no `this` and cannot touch instance fields or call instance methods directly:

```java
public class BankAccount {
    private double balance;                              // instance

    public static boolean isValidId(String id) {         // static: pure input → output
        return id != null && id.matches("ACCT-\\d+");
    }

    public double getBalance() {                          // instance: needs a particular account
        return balance;
    }
}
```

*`isValidId` depends only on its parameter — no account needed. `getBalance` is meaningless without one.*

That's the decision rule: **if the method needs per-object state, it's an instance method; if it's a pure function of its inputs (or touches only static state), make it static.** The reverse direction is fine — instance methods use static members freely, since class-level things exist regardless.

`main` is static for exactly this reason: the JVM must call it *before any object exists*. It's also why `main` can't call the instance methods of its own class without first constructing an instance — the wall between static and instance context, met by every beginner as a confusing compile error, is just this rule.

---

## Utility Classes

Some classes are *nothing but* static methods — `Math`, `Arrays`, `Collections`. The pattern is called a **utility class**: stateless functions grouped by theme, never instantiated (the well-mannered version marks the constructor `private` so `new Math()` can't compile). Fine in moderation — every codebase has a `StringUtils` — but a growing utility class is often objects trying to be born: when several functions keep passing the same data around, that data plus those functions probably want to *be* a class.

---

## Static Initializers, Briefly

Simple static fields initialize at their declaration. When setup takes real code, a **static initializer block** runs once, at class loading:

```java
public class Config {
    private static final Map<String, String> DEFAULTS;
    static {
        DEFAULTS = new HashMap<>();
        DEFAULTS.put("timeout", "30");
        DEFAULTS.put("retries", "3");
    }
}
```

*A `static { }` block: one-time class setup for initialization too complex for a single expression.*

---

## The Caution Label

Static *methods* (stateless utilities) are wholesome. Static *mutable state* deserves suspicion: a non-final, non-private static field is shared by the entire program — effectively the global variable Java's design otherwise avoids (as noted back in the Scope lesson). Every corner of the code can read and write it, changes in one place surprise another, tests interfere through leftover state, and threads racing on it corrupt it (Concurrency, Java Advanced). Constants (`static final` immutables) and counters guarded by well-designed methods are legitimate; "a static field because passing parameters felt tedious" is future pain. When in doubt, keep state on instances — that's what they're for.
