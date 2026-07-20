# Patterns in Spring

The best way to cement the patterns vocabulary is to notice it in software we already use daily. Spring is a patterns showcase — not because its authors collected them for sport, but because framework-scale problems are exactly the recurring problems patterns name. This lesson walks the framework we've spent four topics with and points at the patterns load-bearing inside it — turning "Spring magic" into "oh, that's a decorator."

---

## The Container Is Creational

**Singleton** — the default bean scope: one instance per context, shared everywhere. Spring delivers what the hand-rolled pattern promised, minus its costs — no `getInstance()` coupling, no untestable static state, because instances arrive by *injection*: the same class, injected with mocks in tests and real beans in production. Singleton the *scope* replaced singleton the *implementation*.

**Factory** — everywhere construction lives. `@Bean` methods are literally factory methods (our code deciding what to build, the container managing the result); the `ApplicationContext` itself is one giant abstract factory (`getBean(type)` — "give me an implementation, I don't care which class"); and `FactoryBean` is the extension point for beans whose construction is a project in itself.

**Builder** — the fluent APIs at Spring's edges: `ResponseEntity.created(uri).header(...).body(dto)`, `UriComponentsBuilder`, `MockMvcRequestBuilders`, WebClient's request spec. Complex optional-heavy construction, labeled and chainable — the pattern exactly as lesson 1 built it.

---

## The Proxies Are Structural

The deepest one. Annotate a method `@Transactional` and Spring does *not* modify the class — at startup it generates a **proxy**: a dynamically created subclass (or interface implementation) that wraps the real bean, and it's the proxy that gets injected everywhere. The proxy's method opens the transaction, delegates to our code, commits or rolls back — **a decorator, generated at runtime** (Proxy is the pattern's name when the wrapper is about *controlling access*; the mechanics are the same wrap-and-delegate):

```
caller ──▶ [generated proxy: open tx ▸ delegate ▸ commit/rollback] ──▶ our InvoiceService
```

*What `@Transactional` actually builds: our decorator stack from lesson 2, written by the framework from an annotation.*

`@Cacheable`, `@Async`, `@Retryable`, security's `@PreAuthorize` — same machinery, different concern. And the machinery explains the pattern's one famous gotcha: a bean calling *its own* `@Transactional` method (`this.otherMethod()`) bypasses the proxy — `this` is the real object, not the wrapper — so the annotation silently does nothing. Knowing the pattern is what makes that behavior predictable instead of mystifying. (The generation technique — JDK dynamic proxies for interfaces, bytecode subclassing otherwise — is the Reflection topic, weaponized.)

**Adapter** — the integration tissue: `HandlerAdapter` letting the DispatcherServlet drive any style of controller through one interface; message converters adapting JSON, XML, and form data to one binding model; `spring-boot-starter-*` integrations adapting third-party libraries into Spring idioms. The wrap-the-foreign-thing discipline from lesson 2, applied at every seam.

---

## The Collaboration Is Behavioral

**Strategy** — Spring's default answer to variation: any injected interface with swappable implementations is a strategy, and the container is the selector (`@Profile` choosing per environment, `@Qualifier`/`@Primary` choosing explicitly, `Map<String, T>` injection choosing at runtime — the exact shape of lesson 3's shipping example). Inside the framework: `PlatformTransactionManager` (JPA? JTA? per environment), view resolvers, converter registries.

**Observer** — packaged as application events:

```java
// Publish — the source announces and moves on
public void ship(Order order) {
    dispatch(order);
    events.publishEvent(new OrderShippedEvent(order.id()));    // ApplicationEventPublisher, injected
}

// Subscribe — reactions register by annotation
@Component
public class ConfirmationEmailer {
    @Async                                        // optionally off the caller's thread
    @EventListener
    public void on(OrderShippedEvent event) { ... }
}

// Or: only after the surrounding transaction commits — the production-grade subtlety
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onCommitted(OrderShippedEvent event) { ... }
```

*Lesson 3's observer, framework edition: no listener lists to manage, async for free, and transaction-aware delivery for the ship-then-rollback edge case.*

**Template Method** — the older Spring idiom, alive in the `*Template` classes: `JdbcTemplate`, `RestTemplate`, `TransactionTemplate`. Each owns an invariant skeleton (acquire resources, loop, handle errors, release) with our variation supplied as a callback — the abstract-class pattern from Unit 3, modernized with lambdas instead of subclasses.

---

## Reading Frameworks — and Designing — with Pattern Eyes

Two takeaways close the topic and the unit. First, patterns are a *reading* skill as much as a writing one: framework behavior, framework gotchas, and framework documentation all become legible once the underlying pattern is visible — the proxy self-invocation rule being the flagship example. Second, patterns are vocabulary, not virtue: the goal was never to install patterns into code (pattern-for-its-own-sake is its own anti-pattern) but to recognize a recurring problem and reach for its named, well-worn solution — then say so in four words at the design review.

That closes Java Advanced — and the curriculum's arc: language fundamentals, object design, the intermediate machinery of exceptions, collections, and functions, then the professional stack of concurrency, testing, Spring, and the design language that ties it together. From here, it's project work: applying the whole stack to something real.
