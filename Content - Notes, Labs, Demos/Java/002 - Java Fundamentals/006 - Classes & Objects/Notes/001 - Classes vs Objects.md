# Classes vs Objects

Everything so far — variables, arrays, control flow, methods — could exist in a purely procedural language. Java's organizing idea is one level up: bundle **state** (data) and the **behavior** that operates on it into a single unit. The unit's definition is a **class**; each concrete thing built from it is an **object**. Getting the class/object distinction crisp now makes the rest of the language fall into place.

---

## Blueprint and Building

A class *describes*; an object *is*. The class states what fields every instance will have and what methods it will respond to. An object is one actual occurrence, with its own values in those fields:

```java
public class BankAccount {
    // state — every account HAS these
    String owner;
    double balance;

    // behavior — every account CAN DO these
    void deposit(double amount) {
        balance += amount;
    }

    void printStatement() {
        System.out.println(owner + ": $" + balance);
    }
}
```

*A class definition: two fields for state, two methods for behavior. No account exists yet.*

The **`new`** operator turns a blueprint into a building — an **instance**, allocated on the heap:

```java
BankAccount checking = new BankAccount();
checking.owner = "Ada";
checking.balance = 100.0;

BankAccount savings = new BankAccount();
savings.owner = "Ada";
savings.balance = 5000.0;

checking.deposit(50.0);
checking.printStatement();     // Ada: $150.0
savings.printStatement();      // Ada: $5000.0 — untouched
```

*Two independent objects from one class — each with its own field values.*

That independence is the whole point: one class, any number of instances, each carrying its own state. When `checking.deposit(50)` runs, `balance += amount` means *checking's* balance — an instance method always executes against the specific object it was called on (the `reference.method(...)` syntax from the Methods topic, now from the inside).

---

## We've Been Using This All Along

Nothing here is actually new — it's the model behind every library type we've touched:

- `String` is a class; `"hello"` is an object of it, and `name.length()` asks *that* string for *its* length.
- `StringBuilder` is a class; each `new StringBuilder()` is a separate buffer.
- Arrays are objects; `scores.length` reads a field on one particular array.

What changes in this topic is our seat: from *users* of classes to *authors* of them. The `Billing`-style classes we wrote earlier were just containers for `static` methods; a real class models a **concept** — an account, an order, a sensor — and its instances model the individual accounts, orders, and sensors a running program juggles.

---

## References, Revisited One Last Time

A variable of a class type holds a **reference**, and every behavior we established for strings and arrays applies to our own classes automatically — this is the payoff for having drilled it:

```java
BankAccount a = new BankAccount();
BankAccount b = a;                 // same object, second name
b.balance = 999.0;
System.out.println(a.balance);     // 999.0

BankAccount c = null;              // a reference to nothing
// c.deposit(10);                  // NullPointerException

System.out.println(a == b);        // true  — same object
```

*Assignment copies references; two variables can share one object; `null` means no object at all.*

`==` on objects asks "same instance?" — for a "same contents?" question, classes define an `equals` method (as `String` does). Writing a proper `equals` involves inheritance rules covered in the OOP topic; until then, our own classes compare by identity only.

---

## The Road Through This Topic

Our `BankAccount` works, but it's *raw*: any code can reach in and set `balance` to `-50` directly, there's no controlled way to create a properly initialized account, and nothing distinguishes the fields every account has from things all accounts share. The rest of this topic supplies the machinery, one lesson each: **access modifiers** to wall off state, **fields and constructors** to initialize it correctly, **getters and setters** to expose it deliberately, **`this`** and **`static`** to sharpen what belongs to the instance versus the class, and **enums** and **annotations** — two special kinds of class-level construct Java gives us for free.
