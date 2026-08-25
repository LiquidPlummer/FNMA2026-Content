# The 4 Pillars

Object-oriented programming is usually summarized as four principles — **encapsulation, inheritance, polymorphism, abstraction** — and Java is built around all four. One of them we've already earned the hard way; the other three are this topic's work. This lesson is the aerial view: what each pillar means, how they interlock, and where each gets its full treatment.

---

## Encapsulation — State Behind Behavior

The pillar we know. An object owns its data, hides it (`private` fields), and exposes chosen operations that enforce its rules — the entire arc of the Classes & Objects topic, from access modifiers through defensive copies:

```java
public class BankAccount {
    private double balance;                      // hidden state

    public void withdraw(double amount) {        // the only doors in
        if (amount > balance) throw new IllegalStateException("insufficient funds");
        balance -= amount;
    }
}
```

*Encapsulation, as practiced in Unit 2: invariants live with the data they protect.*

Everything ahead builds on this. The remaining pillars are about relationships *between* classes — and they only work when each class is already a sealed, self-consistent unit.

---

## Inheritance — Is-A Reuse

**Inheritance** lets a class be defined as a *specialization* of another: a `SavingsAccount` **is a** `BankAccount`, automatically possessing its fields and methods, adding or adjusting what differs:

```java
public class SavingsAccount extends BankAccount {
    private double rate;

    public void applyInterest() {                // new capability
        deposit(getBalance() * rate);            // inherited behavior, reused
    }
}
```

*`extends` imports a whole class's contract and implementation as a starting point.*

The mechanics — `super`, overriding, `final`, and the `Object` class every Java type descends from — are the next lesson. So is the caution: inheritance is Java's most *overused* feature, and "is-a" is a claim that must be true behaviorally, not just grammatically.

---

## Polymorphism — One Call, Many Behaviors

**Polymorphism** ("many forms") is inheritance's payoff: code written against a general type runs correctly against any specialization, because each object answers method calls in *its own* way:

```java
BankAccount[] accounts = { new SavingsAccount(...), new CheckingAccount(...) };
for (BankAccount acct : accounts) {
    acct.applyMonthlyPolicy();     // each object runs ITS version — no if/else on type
}
```

*The same call on each element dispatches to different code, chosen by the object's actual class.*

That loop is the pillar in miniature: no `switch` on account type, no cast, and it keeps working when a `MoneyMarketAccount` is invented next year. How Java picks the method at runtime (dynamic dispatch), and what the *reference* type versus *object* type distinction means, is lesson 3.

---

## Abstraction — Program to the Contract

**Abstraction** is the deliberate act of separating *what* an operation does from *how* — defining pure contracts that hide implementation entirely. Java's dedicated tools are **interfaces** and **abstract classes** (lesson 4):

```java
public interface PaymentGateway {
    Receipt charge(String account, double amount);     // what — no how anywhere in sight
}
```

*An interface is a capability with no implementation: any class that can honor it may declare so.*

Code that depends on `PaymentGateway` works with Stripe today, a test fake in the unit tests, and whatever replaces Stripe later — the dependency points at the contract, never the concrete class. This "program to interfaces" discipline is the pillar modern Java leans on hardest: it's the backbone of the Collections Framework (next topic), unit testing with mocks, and Spring's entire dependency-injection model (Java Advanced).

---

## How They Interlock

The pillars aren't four separate features — they're one design story: **encapsulation** makes classes trustworthy in isolation; **abstraction** defines the contracts between them; **inheritance** lets implementations share structure; **polymorphism** makes the contracts *live*, dispatching each call to the right implementation at runtime. In practice, abstraction and polymorphism carry the most weight in professional code, with inheritance the sparingly-used sharp tool — a weighting the next three lessons will justify rather than assert.
