# Behavioral Patterns (Strategy, Observer)

**Behavioral patterns** organize how objects collaborate and how algorithms vary. The two that carry the most weight in modern Java: **Strategy** — swap the algorithm without touching the code that uses it — and **Observer** — let interested parties react to events without the event source knowing who they are. Both will feel familiar; functional programming (Unit 3) made lightweight strategies an everyday syntax, and Spring's event system is Observer at framework scale.

---

## Strategy — The Interchangeable Algorithm

**Problem:** one step of a process varies — shipping cost by carrier, pricing by customer tier, compression by format — and `if`/`else` chains over a "type" field spread copies of that decision everywhere. **Solution:** extract the varying step behind an interface; make each variant a class; inject the one that applies:

```java
public interface ShippingStrategy {
    BigDecimal costFor(Order order);
}

@Component("standard")
public class StandardShipping implements ShippingStrategy {
    public BigDecimal costFor(Order order) { return flatRate(order.weight()); }
}

@Component("express")
public class ExpressShipping implements ShippingStrategy {
    public BigDecimal costFor(Order order) { return flatRate(order.weight()).multiply(TWO).add(SURCHARGE); }
}

@Service
public class CheckoutService {
    private final Map<String, ShippingStrategy> strategies;      // Spring injects ALL of them,
                                                                 // keyed by bean name
    public CheckoutService(Map<String, ShippingStrategy> strategies) {
        this.strategies = strategies;
    }

    public BigDecimal shippingFor(Order order, String method) {
        ShippingStrategy s = strategies.get(method);
        if (s == null) throw new IllegalArgumentException("unknown method: " + method);
        return s.costFor(order);                                  // polymorphic dispatch — no if/else
    }
}
```

*Strategy with Spring's map injection: each variant is a bean; adding "overnight" means adding one class — no existing code changes.*

This is the pattern already living inside Unit 3's furniture: a `Comparator` is a sorting strategy, the `Predicate` handed to `removeIf` a filtering strategy — which points at the modern simplification: **when a strategy is one method with no dependencies, it's just a functional interface, and variants are lambdas** (`Map.of("standard", o -> flatRate(o.weight()), ...)`). Full classes earn their keep when strategies carry injected dependencies or real state; lambdas cover the rest. Either way the test is the same: an `if`/`else` (or `switch`) over the *same discriminator* appearing in more than one place is Strategy asking to happen. (Its sibling, **Template Method** — the abstract-class skeleton from Unit 3 — solves the same problem with inheritance; Strategy's composition is usually the better default.)

---

## Observer — Reacting Without Coupling

**Problem:** when something happens — an order ships, a threshold trips — several unrelated things must react (email, inventory, analytics), and hard-wiring them into the event source makes it depend on everyone (`OrderService` importing the mailer, the warehouse, the metrics client — and growing a new dependency per reaction). **Solution:** invert it — the source announces to an abstract audience; listeners subscribe:

```java
public interface OrderShippedListener {                    // the observer contract
    void onShipped(Order order);
}

@Service
public class ShippingService {
    private final List<OrderShippedListener> listeners;    // ALL implementors, injected

    public ShippingService(List<OrderShippedListener> listeners) {
        this.listeners = listeners;
    }

    public void ship(Order order) {
        dispatch(order);                                    // the actual work
        for (OrderShippedListener l : listeners) {
            l.onShipped(order);                             // announce; who reacts is not our business
        }
    }
}

@Component
public class ConfirmationEmailer implements OrderShippedListener {
    public void onShipped(Order order) { mail(order.customerEmail(), ...); }
}

@Component
public class InventoryAdjuster implements OrderShippedListener {
    public void onShipped(Order order) { decrement(order.items()); }
}
```

*Observer via list injection: the source knows a contract; reactions register themselves by existing. New listener = new class, source untouched.*

The dependency arrows reverse — that's the whole trick: `ShippingService` no longer knows mailers exist. The classic pitfalls travel with the pattern: a *throwing* listener can break the source's operation (decide: isolate each call in try/catch, or let failures propagate — deliberately); a *slow* listener delays it (async dispatch is the fix, with the Concurrency topic's tools); and ordering between listeners should be a non-requirement (needing it is a sign those two belong together).

Spring packages all of this as **application events** — `publisher.publishEvent(new OrderShippedEvent(order))` on one side, `@EventListener` (optionally `@Async`) methods on the other, no interface or list management at all — the form real Spring codebases use, detailed next lesson. The GUI world (`addActionListener`) and reactive streams are the same pattern at other scales.

---

## The Pair, Summarized

Both patterns are polymorphism aimed at coupling: Strategy decouples code from *which algorithm runs*; Observer decouples an event from *who cares about it*. Both replace conditionals-and-imports with interfaces-and-registration, and both get first-class support from the DI container (collection and map injection) — which is no accident. The final lesson turns that observation around and tours Spring itself as a patterns showcase: where each pattern from this topic lives inside the framework we've been using all unit.
