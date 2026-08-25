# Structural Patterns (Adapter, Decorator)

**Structural patterns** solve shape problems: the class exists and works, but it has the wrong interface, or lacks one responsibility we need layered on — and modifying it is impossible (third-party) or unwise (shared, stable, single-responsibility). Both patterns here answer with the same move — *wrap it* — differing in why: the **Adapter** changes an object's interface; the **Decorator** keeps the interface and adds behavior.

---

## Adapter — Making the Wrong Interface Right

**Problem:** our code is written against an interface (as Unit 3 taught); the class we must plug in — a vendor SDK, a legacy component — does the right job through the wrong methods. **Solution:** a thin class that implements *our* interface by translating calls onto *theirs*:

```java
// Ours: the port our application depends on
public interface PaymentGateway {
    Receipt charge(String account, BigDecimal amount);
}

// Theirs: the vendor SDK — different names, different types, not ours to edit
public class LegacyPayClient {
    public PayResult submitPayment(long cents, String acctNo) { ... }
}

// The adapter: ours on the outside, theirs on the inside
@Component
public class LegacyPayAdapter implements PaymentGateway {
    private final LegacyPayClient client;

    public LegacyPayAdapter(LegacyPayClient client) { this.client = client; }

    @Override
    public Receipt charge(String account, BigDecimal amount) {
        long cents = amount.movePointRight(2).longValueExact();   // translate the types...
        PayResult result = client.submitPayment(cents, account);  // ...delegate the work...
        return new Receipt(result.confirmationId());              // ...translate the result
    }
}
```

*An adapter: implement the target interface, hold the adaptee, translate in both directions — no logic of its own.*

The application still injects `PaymentGateway`; the vendor's vocabulary is quarantined inside one class. That containment is the real value: when the vendor changes (or gets replaced), the diff is the adapter, not the codebase. Adapters should stay *dumb* — pure translation; business rules that sneak in become invisible policy. We've met the pattern in the wild repeatedly: `Arrays.asList` (array → `List`), `InputStreamReader` (byte stream → character reader), and Spring MVC's own handler adapters. It's also the standard boundary discipline: wrap every third-party client in an interface we own — the testing payoff (mock the interface, not the SDK) comes free.

---

## Decorator — Same Interface, Added Behavior

**Problem:** an implementation does its one job well; a use site needs that job *plus* caching, or logging, or retry — and stuffing those into the class violates single-responsibility (and multiplies: cached-and-logged? logged-and-retrying?). **Solution:** a wrapper that implements the *same* interface, holds another implementation, adds its concern around the delegated call:

```java
public class CachingGateway implements PaymentGateway {          // same interface!
    private final PaymentGateway inner;                          // wraps another one
    private final Map<String, Receipt> recent = new ConcurrentHashMap<>();

    public CachingGateway(PaymentGateway inner) { this.inner = inner; }

    @Override
    public Receipt charge(String account, BigDecimal amount) {
        String key = account + ":" + amount;
        return recent.computeIfAbsent(key, k -> inner.charge(account, amount));
    }
}

public class RetryingGateway implements PaymentGateway {
    private final PaymentGateway inner;
    ...
    @Override
    public Receipt charge(String account, BigDecimal amount) {
        try {
            return inner.charge(account, amount);
        } catch (TransientGatewayException e) {
            return inner.charge(account, amount);                // one retry, then propagate
        }
    }
}

// Composition at wiring time — decorators STACK:
PaymentGateway gateway =
        new RetryingGateway(
            new CachingGateway(
                new LegacyPayAdapter(client)));
```

*Decorators: each adds one concern and delegates; stacking composes concerns in chosen order — and callers can't tell the difference.*

Because wrapper and wrapped share the type, any consumer of `PaymentGateway` accepts the decorated chain unmodified — polymorphism doing the heavy lifting. Each concern lives once, testable alone (wrap a Mockito mock, assert the retry happened), combinable at will. Order is part of the design: retry-around-cache and cache-around-retry behave differently, and the nesting spells it out.

The JDK's I/O streams are the canonical decorator stack — `new BufferedReader(new InputStreamReader(socket.getInputStream()))` wraps buffering around decoding around bytes; `Collections.unmodifiableList(list)` decorates with immutability. The Strings topic's advice about builders and the collections topic's wrappers were decorators all along.

---

## Adapter vs. Decorator, in One Breath

Both hold a wrapped object and delegate. The discriminator is the interface: **different interface out → adapter** (translation); **same interface out → decorator** (augmentation). A codebase boundary often uses both, as the stack above shows: adapt the foreign thing once, then decorate the resulting clean interface with operational concerns.

The pattern's industrial form is worth one forward pointer: Spring applies decorators *automatically* — `@Transactional` (Spring Data) and `@Cacheable` work by generating proxy decorators around beans at startup, wrapping our methods with the transaction or cache logic. That mechanism gets unpacked in the Patterns in Spring lesson. First, the third family: patterns for varying *behavior* — Strategy and Observer.
