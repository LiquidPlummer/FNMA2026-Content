# Custom Exceptions

`IllegalArgumentException` and `IOException` describe failures in the language's terms. Real systems fail in *domain* terms — a payment declines, an account is frozen, an inventory reservation expires — and a **custom exception** gives each of those a name that code can catch precisely and stack traces can announce clearly. Defining one is small; designing the *set* of them well is the actual skill.

---

## Defining One

An exception is a class extending `Exception` (checked) or `RuntimeException` (unchecked) — that parent choice being exactly the design decision from lesson 1. Convention: the name ends in `Exception` and says what went wrong, not where:

```java
public class PaymentDeclinedException extends RuntimeException {

    private final String transactionId;               // domain data rides along

    public PaymentDeclinedException(String transactionId, String reason) {
        super("payment " + transactionId + " declined: " + reason);
        this.transactionId = transactionId;
    }

    public PaymentDeclinedException(String transactionId, String reason, Throwable cause) {
        super("payment " + transactionId + " declined: " + reason, cause);
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
```

*A complete custom exception: meaningful message built in `super(...)`, a cause-accepting constructor, and structured data for handlers.*

Three ingredients worth copying every time:

- **Message assembled in the constructor** — callers pass facts (`transactionId`, `reason`); the exception formats them consistently. Every throw site gets a good message for free.
- **A `Throwable cause` overload** — so the wrapping pattern works (`throw new PaymentDeclinedException(id, "gateway timeout", e)`), keeping the original failure in the trace. Forgetting this constructor is the most common custom-exception defect; the underlying cause gets dropped exactly when it's needed most.
- **Fields for machine-readable context** — a handler that must *act* on the failure (refund flow needs the transaction ID) shouldn't parse the message string to get it.

---

## Catching in Domain Terms

The payoff arrives at the catch site. Compare handling built on language-level types with handling built on domain types:

```java
try {
    checkout.process(cart);
} catch (PaymentDeclinedException e) {
    offerAlternatePayment(e.getTransactionId());      // precise, recoverable, informed
} catch (OutOfStockException e) {
    suggestSubstitutes(e.getMissingItems());
}
```

*Handlers read like business policy — each named failure gets its named response.*

Without custom types, this is one `catch (RuntimeException e)` and string-matching on messages — brittle, unreadable, and broken by the first reworded message. Custom exceptions are the difference between *handling failures* and *parsing them*.

---

## Exception Hierarchies

Related failures can share a parent, letting callers choose their precision — catch one specific problem, or the whole family:

```java
public class BillingException extends RuntimeException { ... }          // the family root

public class PaymentDeclinedException extends BillingException { ... }
public class InvoiceNotFoundException extends BillingException { ... }
public class CurrencyMismatchException extends BillingException { ... }
```

*A shallow hierarchy: one abstract-ish root per module, concrete exceptions one level below.*

`catch (BillingException e)` now means "any billing failure," while `catch (PaymentDeclinedException e)` stays available for targeted recovery. Keep such hierarchies **shallow** — one root per module or layer, one level of concrete types beneath. Deep taxonomies of exceptions age badly; nobody catches the middle layers.

This is also the natural home for the **layer-boundary translation** from the last two lessons: the persistence layer catches `SQLException` and throws `RepositoryException`; the service layer above knows nothing of SQL. Each layer speaks its own exception vocabulary and translates at the door, cause always attached.

---

## Restraint: When *Not* to Create One

Custom exceptions are cheap to write and expensive to maintain — every one is public API. The checklist before minting a new type:

- **Would a standard exception do?** Bad argument → `IllegalArgumentException`. Wrong state → `IllegalStateException`. Unsupported operation → `UnsupportedOperationException`. Reusing these is instantly understood by every Java developer and every framework.
- **Will anyone catch it specifically?** If every conceivable handler treats it identically to its parent, the distinct type adds ceremony, not information. A distinct *message* may be all that's needed.
- **Is it actually exceptional?** The reminder from the Methods topic: "not found" from a lookup is usually a normal outcome — an empty `Optional` (this unit, Functional Programming) — not a throw.

A well-designed module typically ends up with a *handful* of exception types, not dozens: one family root, two to five concrete failures that callers demonstrably handle differently, standard exceptions for everything else. That's the whole discipline — and with throwing, catching, and designing covered, exception handling is complete. Next topic: object-oriented programming proper, where the `extends` keyword we just used gets its full story.
