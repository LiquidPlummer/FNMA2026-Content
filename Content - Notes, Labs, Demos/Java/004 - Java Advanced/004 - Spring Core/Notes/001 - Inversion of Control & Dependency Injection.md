# Inversion of Control & Dependency Injection

Every thread of this unit — interfaces as contracts, constructor-fed dependencies, annotation scanning, mock-friendly design — converges on **Spring**, the framework that runs most of professional Java. Before any Spring API, the two ideas it's built on, which are design principles first and framework features second: **Inversion of Control** and **Dependency Injection**.

---

## The Problem: Objects Building Their Own World

Consider wiring done the direct way:

```java
public class OrderService {
    private final OrderRepository repo = new JdbcOrderRepository(     // concrete class, chosen here
            new ConnectionPool("jdbc:postgresql://prod-db:5432/shop", "sa", "hunter2"));
    private final PaymentGateway gateway = new StripeGateway(System.getenv("STRIPE_KEY"));
}
```

*Self-assembling object: `OrderService` chooses, configures, and constructs its own collaborators.*

Everything wrong with this we can now name precisely. It's welded to concrete classes — swapping Stripe out means editing `OrderService` (abstraction, violated). It's untestable — Mockito can't intercept `new` buried in a field initializer; last lesson's whole approach is impossible. Configuration is smeared through business code. And each of its dependencies builds *its* dependencies, so the whole graph hardens into one unswappable lump.

**Dependency Injection** is the counter-move we've already been practicing: an object *declares* what it needs — as interfaces, via its constructor — and something outside provides the implementations:

```java
public class OrderService {
    private final OrderRepository repo;
    private final PaymentGateway gateway;

    public OrderService(OrderRepository repo, PaymentGateway gateway) {   // needs declared at the door
        this.repo = repo;
        this.gateway = gateway;
    }
}
```

*The injectable version — identical to the testable version from the Mockito lesson, because they're the same idea.*

**Inversion of Control** names the shift in responsibility: objects no longer control the construction and wiring of their world; that control moves *out* — to whoever calls the constructor. Tests inject mocks. Production injects the real graph. The class can't tell the difference, which is exactly the point.

---

## The Container: Wiring at Scale

DI without help means a `main` method hand-building the object graph:

```java
ConnectionPool pool = new ConnectionPool(config.dbUrl(), config.dbUser(), config.dbPass());
OrderRepository repo = new JdbcOrderRepository(pool);
PaymentGateway gateway = new StripeGateway(config.stripeKey());
OrderService orders = new OrderService(repo, gateway);
InvoiceService invoices = new InvoiceService(repo);                  // shared dependency — same instance
// ... × two hundred more classes
```

*Manual wiring: correct, explicit, and unmaintainable past a few dozen components.*

Fine at small scale — genuinely — but real applications have hundreds of components with shared instances, initialization order constraints, and environment-dependent choices. Spring's core is an **IoC container** that does this assembly automatically: it discovers the application's components, understands each one's declared needs from its constructor, constructs everything in dependency order, and injects the right instances — the whole graph, built at startup from metadata.

The discovery mechanism is zero surprise by now: **annotations, read reflectively**. Mark a class as a component, and the container finds it, constructs it, and feeds it to whoever declares a parameter of its type:

```java
@Component
public class StripeGateway implements PaymentGateway { ... }

@Component
public class JdbcOrderRepository implements OrderRepository { ... }

@Component
public class OrderService {
    public OrderService(OrderRepository repo, PaymentGateway gateway) {  // Spring calls this,
        ...                                                              // passing the two above
    }
}
```

*Three labels replace the wiring code: the container scans, matches constructor parameters to components by type, and assembles.*

Note what `OrderService` depends on: the *interfaces*. Spring finds that exactly one component implements `OrderRepository` and injects it — swap `JdbcOrderRepository` for a `MongoOrderRepository` component and `OrderService` neither knows nor changes. Program-to-abstractions, now with automated payoff.

---

## Inversion Beyond Construction

"IoC" covers more than wiring. In a Spring application, the framework also owns the *run loop*: it receives the HTTP request and calls our controller (Spring Web); it manages the transaction around our repository call (Spring Data); it applies our `@Scheduled` method on its timer — the same "don't call us, we'll call you" flow as the mini-scheduler we built in the Reflection topic, and as JUnit calling our `@Test` methods. Our code becomes a set of well-labeled parts; the framework is the machine that hosts them.

What Spring calls those managed parts — **beans** — and the container that holds them — the **ApplicationContext** — are the vocabulary of the next lesson, along with the injection mechanics: qualifiers, scopes, and what happens when the container can't figure out what to inject.
