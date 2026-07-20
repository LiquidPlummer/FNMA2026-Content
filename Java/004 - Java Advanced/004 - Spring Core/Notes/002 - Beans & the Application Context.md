# Beans & the Application Context

Spring's vocabulary for what the last lesson described: an object the container constructs and manages is a **bean**; the container itself is the **ApplicationContext**. This lesson covers how classes become beans, how injection actually resolves, and the lifecycle and scoping rules that govern beans once they exist.

---

## Becoming a Bean: Stereotypes and Scanning

At startup Spring performs **component scanning** — walking the classpath under the application's base package, reflectively looking for stereotype annotations:

```java
@Component                  // generic: "manage me"
public class PdfRenderer { ... }

@Service                    // a business-logic component
public class OrderService { ... }

@Repository                 // a data-access component (adds exception translation)
public class JdbcOrderRepository implements OrderRepository { ... }

@RestController             // a web component (Spring Web topic)
public class OrderController { ... }
```

*The stereotype family: all mean "make a bean of this class" — the specializations add intent and small extras.*

`@Service`, `@Repository`, and `@RestController` are `@Component` with role labels — they document architecture (the layers formalized in the Spring Web topic) and enable layer-specific behavior. One instance of each scanned class is constructed and registered in the context under its type (and a default name).

For classes we *can't* annotate — library types, third-party clients — a **`@Bean` method** inside a `@Configuration` class constructs the bean manually:

```java
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {              // a Jackson class — not ours to annotate
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public PaymentGateway paymentGateway(AppProps props) {   // parameters are injected too
        return new StripeGateway(props.stripeKey());
    }
}
```

*The factory-method route: our code builds the object, Spring manages the result exactly like a scanned bean.*

Two routes, one registry: scanning for our classes, `@Bean` methods for everyone else's.

---

## Injection: How Needs Get Met

The container satisfies a bean's constructor parameters **by type**: one parameter of type `OrderRepository`, one bean implementing it → injected. With a single constructor, modern Spring needs no annotation at all (`@Autowired` on constructors is legacy visual noise; on *fields* it's actively worse — field injection defeats the final-fields, testable-constructor design the whole unit has built):

```java
@Service
public class OrderService {
    private final OrderRepository repo;               // final: set once, in the constructor

    public OrderService(OrderRepository repo) {       // no annotation needed — Spring uses this
        this.repo = repo;
    }
}
```

*Constructor injection, the recommended style: immutable dependencies, mock-friendly, no framework imports in the class.*

By-type resolution has two failure modes, both startup errors (fail-fast — the container refuses to boot rather than run half-wired). **No candidate**: no bean implements the type — typically a missing annotation or unscanned package. **Too many candidates**: two beans implement `PaymentGateway` — resolved by naming a choice:

```java
@Component @Qualifier("stripe")   public class StripeGateway implements PaymentGateway { ... }
@Component @Qualifier("mock")     public class MockGateway   implements PaymentGateway { ... }

public OrderService(@Qualifier("stripe") PaymentGateway gateway) { ... }   // pick by name
```

*`@Qualifier` disambiguates; `@Primary` on one candidate is the "default unless asked" alternative.*

(Injecting `List<PaymentGateway>` collects *all* implementations — a tidy plugin pattern.)

---

## Scope and Lifecycle

By default every bean is a **singleton** — one instance in the context, injected everywhere it's needed. Consequences worth internalizing: singletons are shared across all threads (a web app's requests included), so **beans should be stateless** — configuration and dependencies in final fields, no mutable per-request state — or the Concurrency topic's race conditions arrive uninvited. Other scopes exist (`@Scope("prototype")` for new-instance-per-injection; request/session scopes in web apps) but stateless singletons are the overwhelming norm.

The container also manages lifecycle moments:

```java
@Service
public class CacheWarmer {

    @PostConstruct
    void warm() { ... }          // after construction + injection — initialization goes here

    @PreDestroy
    void flush() { ... }         // at context shutdown — cleanup goes here
}
```

*Lifecycle hooks: post-wiring initialization and graceful-shutdown cleanup, container-invoked.*

`@PostConstruct` exists because constructors run *before* the surrounding context is fully assembled — startup work touching other beans belongs in the hook, not the constructor.

The `ApplicationContext` itself is rarely touched directly (`context.getBean(OrderService.class)` — useful in tests and edge cases; a smell in application code, where injection should do the work). What remains before Spring feels complete: where do values like `props.stripeKey()` come from, and how does the same codebase wire differently for dev, test, and prod? Configuration and profiles — next.
