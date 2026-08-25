# Getters & Setters

Private fields raise the obvious question: how does the rest of the program *read* an account's balance, or *update* an owner's address? Java's answer is a naming idiom so entrenched it's effectively part of the language: **getters** expose a field's value, **setters** accept a new one — each an ordinary method, each an opportunity to enforce the class's rules.

---

## The Idiom

```java
public class BankAccount {
    private String owner;
    private double balance;
    private boolean frozen;

    public String getOwner() {          // getter: getX for a field x
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isFrozen() {         // boolean getters use "is"
        return frozen;
    }

    public void setOwner(String owner) {          // setter: setX, void, one parameter
        if (owner == null || owner.isBlank())
            throw new IllegalArgumentException("owner is required");
        this.owner = owner;
    }
}
```

*The conventions: `getX()` returns the field, `isX()` for booleans, `setX(value)` validates and assigns.*

The naming isn't just taste — frameworks throughout the Java ecosystem (Spring, JSON mappers, JPA in the Java Advanced unit) *discover* properties by looking for exactly these method shapes, a convention called the JavaBeans standard. Deviating from `get`/`is`/`set` names breaks that machinery.

Notice what's *absent*: no setter for `balance`. Money moves through `deposit` and `withdraw` — methods with rules — never by direct assignment. And that's the design lens for this whole lesson: **a getter or setter per field is not a requirement; it's a decision, field by field.**

---

## Why Not Just Public Fields?

The pair `getBalance()`/`setBalance(x)` looks like ceremony around `balance` — until the day requirements change, which is the entire argument:

- **Validation** — a setter can reject (`setOwner` above); a public field can't.
- **Side effects** — logging, auditing, notifying listeners can be added inside the method later, *without changing any caller*.
- **Read-only exposure** — getter without setter (like `balance`) makes a field publicly readable but privately writable. Public fields are all-or-nothing.
- **Representation freedom** — `getBalance()` could tomorrow compute from a transaction list instead of a field; callers never know.

The asymmetry is the key: converting a public field to a method later breaks every caller; a method whose insides change breaks no one. The idiom buys permanent flexibility with a little verbosity — and IDEs generate the boilerplate anyway (Alt+Insert in IntelliJ).

---

## Setters Are Optional; Immutability Is a Feature

Every setter written is a promise that the field *may change at any time*, which is a real cost — mutable objects are harder to reason about, share, and debug (recall why `String`'s immutability was so valuable). Modern Java style leans toward fewer setters:

- Set once in the **constructor**, expose a getter, skip the setter — many "setters" turn out never needed.
- For pure data carriers, a **record** does this in one line, generating constructor, accessors (named `owner()`, not `getOwner()` — the one idiom exception), `equals`, and `toString`:

```java
public record AccountSummary(String owner, double balance) {}

AccountSummary s = new AccountSummary("Ada", 150.0);
s.owner();       // "Ada" — record accessor
```

*A record: the all-getters-no-setters class, written for us. Immutable by construction.*

---

## The Reference-Leak Trap

One subtlety turns a getter into a hole in the wall. Returning a field that is itself a **mutable object** hands the caller live access to internal state — the leak previewed in the Returning lesson, now with its proper fix:

```java
public class Account {
    private final List<String> transactions = new ArrayList<>();

    public List<String> getTransactions() {
        return transactions;                              // LEAK: caller can .add() / .clear()
    }

    public List<String> getTransactionsSafely() {
        return List.copyOf(transactions);                 // defensive copy — read-only snapshot
    }
}
```

*Returning the list itself outsources our invariants to strangers; returning a copy keeps the wall intact.*

The rule of thumb: getters returning primitives, `String`s, or other immutables are safe as-is; getters returning collections, arrays, or mutable objects should return a **defensive copy** or unmodifiable view. Setters have the mirror-image concern (storing a caller's mutable object directly) — same fix, copy on the way in.

Both the getter idiom and the leak fix rely on `this` being wielded precisely — and `this` has a few more talents than the shadowing fix we've seen. Next lesson.
