# Fields & Constructors

An object's fields hold its state, and something has to put *correct initial values* in them at the moment the object is born. That something is the **constructor** — a special method-like member that runs exactly once per object, during `new`. Between fields and constructors, a class controls both what its objects remember and how they come into existence.

---

## Fields

A **field** (or *instance variable*) is a variable declared directly in the class body — one copy per object, alive as long as the object is:

```java
public class BankAccount {
    private String owner;
    private double balance;
    private boolean frozen = false;     // explicit initial value
}
```

*Three fields: two relying on defaults (`null`, `0.0`), one initialized at declaration.*

Everything established about fields in earlier topics applies: they're auto-initialized to defaults (unlike locals), scoped to the whole class, and — per the last lesson — `private` as a matter of policy. An initializer at the declaration (`= false`) suits constants-per-instance and simple defaults; anything depending on outside input needs a constructor.

---

## Constructors

A constructor looks like a method with **the class's name and no return type** (not even `void`). It runs as part of `new`, receiving arguments to establish valid starting state:

```java
public class BankAccount {
    private String owner;
    private double balance;

    public BankAccount(String owner, double openingBalance) {
        if (owner == null || owner.isBlank())
            throw new IllegalArgumentException("owner is required");
        if (openingBalance < 0)
            throw new IllegalArgumentException("opening balance cannot be negative");

        this.owner = owner;
        this.balance = openingBalance;
    }
}

BankAccount acct = new BankAccount("Ada", 100.0);   // constructor runs here
```

*The constructor validates and assigns — after `new` completes, the object is guaranteed usable.*

That guarantee is the deep point. The raw class from two lessons ago could exist half-set-up (`owner` null, forgotten by the caller); with a validating constructor and private fields, **an invalid `BankAccount` cannot be constructed at all**. Fail-fast validation (the Methods topic's `throw`) belongs in constructors more than anywhere else.

The `this.owner = owner` idiom resolves parameter/field shadowing, previewed in the Scope lesson and given full treatment two lessons ahead.

---

## The Default Constructor — and Losing It

A class with *no* written constructor gets a free **default constructor**: no parameters, no body, fields left at their defaults — it's what let us write `new BankAccount()` before this lesson. The catch everyone hits: **writing any constructor removes the free one.** With the two-argument constructor above, `new BankAccount()` is now a compile error — which is usually exactly right (a no-argument account has no owner), but explains the sudden breakage when a constructor is added to an existing class. If both forms are wanted, both must be written.

---

## Overloaded Constructors and `this(...)`

Constructors overload like methods — several signatures for several ways to create the object. To keep validation in one place, a constructor can delegate to another with **`this(...)`**, which must be its first statement:

```java
public BankAccount(String owner) {
    this(owner, 0.0);                   // delegate: new account, empty balance
}

public BankAccount(String owner, double openingBalance) {
    // ... the validating constructor from above — the single source of truth
}
```

*Constructor chaining: the convenience form forwards to the full form, so rules live once.*

The chain pattern scales: one "real" constructor does all the work; the others are one-line conveniences. (When the combinations of optional values multiply beyond a few overloads, the Builder pattern — Java Advanced, Design Patterns — is the industrial answer.)

---

## Initialization Order, Briefly

When `new BankAccount("Ada", 100)` executes, the sequence is: memory allocated with all fields at **defaults** → field initializers run (`frozen = false`) → constructor body runs → `new` returns the reference. Superclass construction slots into this sequence too, once inheritance arrives. The practical takeaways: field initializers run *before* the constructor body (so the body may overwrite them), and a constructor should avoid handing out `this` or calling overridable methods before the object is fully built — the object isn't ready until the constructor finishes.

With birth under control, the next question is daily life: how outside code reads and updates private state on the class's terms — getters and setters.
