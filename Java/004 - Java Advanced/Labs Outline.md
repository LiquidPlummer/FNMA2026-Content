# Java Advanced — Labs Outline

Planning document for the `Labs/` modality across this unit. Same conventions as the previous units' labs outlines — one self-contained project per lab with a guided README walkthrough plus unguided exercises — with one structural difference: **this unit's stack varies by topic.** The first three topics stay on the plain Maven quickstart scaffold (with the test tree *kept* where noted); the Spring topics scaffold via Spring Initializr instead. Production notes at the bottom cover what that means for the labs skill.

**Status:** all planned, none produced yet. These labs assume the Intermediate labs — interfaces, collections, lambdas, and streams are used without ceremony.

---

## 001 - Concurrency

### Lab 001 - Threads & Shared State
**Covers:** Threads & the Runnable Interface, Synchronization.
**Shape:** The broken-counter arc from the notes, made physical. Guided: create and `start()` two threads (with the `run()`-instead-of-`start()` mistake made deliberately — same thread name printed, aha moment); `join()` to collect results; then the race — two threads incrementing a shared counter, run three times, three different wrong answers *observed*; fix it three escalating ways and re-verify each: `synchronized` method, `AtomicInteger`, and task-confinement (each thread counts privately, main sums — no shared state at all); a `volatile` stop-flag demo closes (the never-stopping loop, then fixed). Exercises: a two-lock deadlock to construct *and then fix* by lock ordering, a `ConcurrentHashMap` word-count with racing threads, predict-the-range questions for given interleavings.

### Lab 002 - Pools & Pipelines
**Covers:** Executors & Thread Pools, CompletableFuture.
**Shape:** A "price aggregator" hitting three simulated slow services (each a method with a `sleep` and occasional simulated failure). Guided: sequential baseline timed; fixed pool + `Callable`/`Future` with `invokeAll` — timed again, the speedup is the lesson; proper `shutdown` in a `finally`/try-with-resources; then the same aggregation rebuilt as `CompletableFuture` — `supplyAsync` on a named executor, `thenApply`, `thenCombine` for the merge, `orTimeout` plus `exceptionally` for the flaky service (run until the failure path actually triggers). Exercises: fan out over N SKUs with `allOf` and collect, add a fallback price on timeout, one thread-starvation scenario to diagnose from a description, and a "which tool: raw thread, pool, or CompletableFuture?" decision set.

---

## 002 - Reflection

### Lab 001 - Build a Mini-Framework
**Covers:** The Class Object, Inspecting Fields Methods & Constructors, Annotations at Runtime — one lab, full arc.
**Shape:** The notes' promise ("no framework is magic after this") delivered in code, in three acts. Guided: **Act 1** — interrogate classes: `getClass()` vs literals, walk `getDeclaredFields`/`Methods` on a provided `BankAccount`, print a class report; **Act 2** — the ten-line universal `toMap` serializer, including `setAccessible` on private state and boxing observed; **Act 3** — define `@Audited(reason=...)` with `RUNTIME` retention, then write the twenty-line runner that scans an object's methods, finds the annotation, and invokes flagged methods with logging — a working annotation framework, hand-built. Exercises: a `@Default`-value field injector for test objects, extend the runner to respect an `enabled=false` element, and a "why did the framework skip my method" debugging scenario (wrong retention — the classic).

---

## 003 - Unit Testing

*Scaffold note: these labs keep `src/test/java` and the JUnit dependencies the quickstart archetype generates — the usual trim step does not apply.*

### Lab 001 - Red, Green, Prove It
**Covers:** JUnit Basics, Assertions & Test Structure.
**Shape:** Testing a provided-but-untested `PromoCode` class (with two planted bugs). Guided: first `@Test` with Arrange-Act-Assert spacing; the assertion vocabulary applied — `assertEquals` (including the double-with-delta form), `assertThrows` with a message check, `assertAll` on a multi-field result; `@BeforeEach` extraction; a `@ParameterizedTest` over boundary values — which *finds planted bug #1*; behavior-named tests throughout, and a deliberately vague `test1()` renamed as an exercise in communication. Bug #2 survives until the student writes the boundary test the walkthrough only hints at. Exercises: full test class for a provided `DateRange` class to a behavior spec (min coverage: boundaries, invalid input, the empty case), critique-and-fix three over-specified tests, one test whose *expected value* is wrong — find it by hand-computation.

### Lab 002 - Mock the World
**Covers:** Mockito & Mocking.
**Shape:** An `OrderService` with `OrderRepository` and `PaymentGateway` interfaces (in-memory fakes provided so the class *runs*, but tests shouldn't use them). Guided: `@Mock`/`@InjectMocks` setup; stub the happy path with `when/thenReturn` (Optional returns — Intermediate pays off); `verify` the save with `argThat`; the error path — `thenThrow` a gateway timeout and assert the service's translation behavior; `never()` for the must-not-charge case; an `ArgumentCaptor` for a rich assertion on what was persisted. Exercises: test a cancellation flow spec'd in prose (choose what to stub, what to verify, what to leave alone), refactor an over-mocked test that verifies every interaction, and one test that fails because a matcher was mixed with a literal — fix and explain.

---

## 004 - Spring Core

*Scaffold note: Spring topics scaffold with Spring Initializr (`spring boot` CLI or start.spring.io with fixed options) rather than the quickstart archetype — see Production Notes.*

### Lab 001 - The Container Takes Over
**Covers:** Inversion of Control & Dependency Injection, Beans & the Application Context.
**Shape:** A greeting/notification console app (via `CommandLineRunner` — no web layer yet) built twice. Guided: first, five minutes of manual wiring in `main` (the pain is the setup); then Boot: `@SpringBootApplication`, stereotypes on the service and two `NotificationSender` implementations, constructor injection with final fields; the two failure modes provoked on purpose and their startup errors read — zero candidates (missing annotation) and two candidates (fixed with `@Qualifier`, then `@Primary`); a `@Bean` method for a library type; `List<NotificationSender>` injection to fan out. Exercises: add a third sender selected by qualifier without touching the service, a `@PostConstruct` cache-warmer, and a component-scan mystery (class outside the base package — diagnose from the error).

### Lab 002 - Same Jar, Different Worlds
**Covers:** Configuration & Profiles, Spring Boot & Auto-Configuration.
**Shape:** The Lab 001 app grows environments. Guided: externalize the greeting and a retry count via `@Value`; group related settings into a `@ConfigurationProperties` record; `application-dev.properties` overriding the base; `@Profile("prod")` real sender vs `@Profile("!prod")` fake sender — run under both profiles and watch the wiring change without a code edit; override a property from the command line and from an environment variable (the precedence chain, observed); finish with auto-configuration made visible — run with `--debug` and find one matched and one unmatched auto-config in the condition report. Exercises: add a `test` profile wiring for `@SpringBootTest`, a properties-validation exercise (`@Positive` on the record + what happens at startup), and a scavenger hunt through `/actuator/beans` and `/actuator/env` (three questions answerable only from the endpoints).

---

## 005 - Spring Data

### Lab 001 - Rows Become Objects
**Covers:** JPA & Entities, Repositories.
**Shape:** An invoice persistence layer on H2 (in-memory — zero external setup). Guided: the `Invoice` entity annotated field by field with each decision narrated (`@Id`/`@GeneratedValue`, `BigDecimal` for money, `EnumType.STRING`, the protected no-arg constructor); the empty `InvoiceRepository` interface — then a breakpoint moment: *where's the implementation?*; CRUD via `CommandLineRunner` with `save`/`findById` (Optional again)/`findAll`; dirty checking demonstrated inside `@Transactional` (mutate, don't save, observe the UPDATE in `show-sql`); a `@DataJpaTest` with two tests. Exercises: add a `Customer` entity + repository, a paged `findAll` printing page metadata, and a schema-review exercise (spot three problems in a provided bad entity).

### Lab 002 - Queries & Joins
**Covers:** Query Methods & @Query, Entity Relationships.
**Shape:** The Lab 001 domain grows a `Customer` relationship and real questions. Guided: derived queries built up name by name (`findByStatus`, `findByCustomerAndTotalGreaterThan`, a `Top3...OrderBy`) with one deliberately misspelled field to see the startup failure; two `@Query` JPQL methods (an aggregate and a multi-condition search) with named parameters; then the relationship — `@ManyToOne(LAZY)` with `@JoinColumn`, the owning-side rule demonstrated; the N+1 problem *produced* (loop over invoices touching `getCustomer()`, count the queries in `show-sql`) and then killed with `JOIN FETCH`; `LazyInitializationException` triggered once outside a transaction and explained. Exercises: three prose queries to implement (choosing derived vs. `@Query` per the escalation ladder), add `LineItem` as a cascade-ALL child with orphan removal, and a query-review exercise on an injection-shaped string-concatenated native query.

---

## 006 - Spring Web

### Lab 001 - Endpoints
**Covers:** REST Controllers, Request & Response Handling.
**Shape:** A REST API over the Spring Data labs' invoice domain. Guided: `@RestController` with the CRUD verb mappings; each binding annotation exercised (`@PathVariable`, `@RequestParam` with defaults, `@RequestBody`); every endpoint hit with `curl` as it's built (the API is *felt*, not just compiled); Bean Validation on the request record with `@Valid` — send bad JSON, read the 400; the domain exception mapped to 404 via `@RestControllerAdvice` with a uniform `ApiError` body; correct status codes throughout (201 + Location, 204). Exercises: a search endpoint with three optional filters + pagination params, a second advice mapping (409 for a duplicate), and an HTTP-conventions review of a provided badly-designed controller (verbs in URLs, 200-with-error-body — find all six sins).

### Lab 002 - The Full Stack, Layered
**Covers:** DTOs & Model Mapping, Service & Repository Layers.
**Shape:** Refactoring Lab 001's working-but-entangled API into the three-layer architecture. Guided: the entity-serialization problems demonstrated live (lazy-loading blowup on the relationship, an internal field leaking into JSON); request/response records introduced with `from(...)` mapping — the asymmetry between them narrated (what a client may send vs. what the server owns); business rules relocated from controller into a `@Transactional` service; the dependency-direction rule enforced (controller loses its repository injection); slice tests at two altitudes — `@WebMvcTest` with a mocked service for the HTTP contract, plain Mockito for the service rules. Exercises: add an update endpoint end-to-end through all three layers, a mass-assignment attack attempted against the DTO (and why it bounces), and a layering review of a provided God-controller.

---

## 007 - Design Patterns

### Lab 001 - Constructing Better
**Covers:** Creational Patterns (Singleton, Factory, Builder).
**Shape:** Refactoring lab on a small report-generation module with three planted pains. Guided: a telescoping four-constructor class rebuilt with a hand-written Builder (fluent chaining, defaults, `build()` validation); an `instanceof`-chain parser selection centralized into a static factory method; the module's hand-rolled `getInstance()` singleton examined, its testing problem demonstrated, and both resolutions shown — enum singleton for plain Java, and the note that in Spring it's just a bean. Exercises: builder for a provided config class, extend the factory with a new format (measuring what didn't change), and a code-review exercise identifying which creational pattern three snippets are crying out for.

### Lab 002 - Wrap & Swap
**Covers:** Structural Patterns (Adapter, Decorator), Behavioral Patterns (Strategy, Observer) — with Patterns in Spring woven throughout.
**Shape:** A payment-processing module evolved through four patterns in sequence, each motivated by a new requirement. Guided: a vendor SDK with the wrong interface → **adapter** (translate, quarantine); "add logging and retry without touching the gateway" → two **decorators**, stacked, order discussed; "support three fee strategies chosen at runtime" → **strategy** via a `Map<String, FeeStrategy>` (Spring's map injection noted as the production form); "notify inventory and email on payment" → **observer** with a small listener list, plus the Spring `@EventListener` equivalent shown as the woven-in Spring note. Exercises: add a caching decorator and choose its position in the stack (defend the order), adapt a second vendor SDK, convert an if/else fee block into a new strategy, and one design question answered in prose: which of this lab's four patterns does `@Transactional` implement, and how do we know?
**Flow note:** Patterns in Spring gets no standalone lab — it's a recognition lesson; each pattern's Spring incarnation is noted at the moment the hand-built version works, which lands harder than a separate tour.

---

## Summary

| Topic | Labs planned | Lessons folded into other labs |
|---|---|---|
| 001 - Concurrency | 2 | — |
| 002 - Reflection | 1 (three-act) | — |
| 003 - Unit Testing | 2 | — |
| 004 - Spring Core | 2 | — |
| 005 - Spring Data | 2 | — |
| 006 - Spring Web | 2 | — |
| 007 - Design Patterns | 2 | Patterns in Spring (woven into both) |

**Total: 13 labs.**

## Production Notes

- **Order matters more here than anywhere:** Spring Core → Spring Data → Spring Web form one continuous domain (the invoice app grows across five labs), and Unit Testing Lab 002's interfaces reappear in Spring Core. Produce top-to-bottom.
- **Scaffolding changes:** Unit Testing labs keep the archetype's test tree and JUnit deps (skip that trim step). Spring labs (topics 004–006) need a Spring Initializr scaffold — fixed options: Maven, Java 21, Boot latest stable at production time; starters `web`, `data-jpa`, `h2`, `validation`, `actuator` added per lab's needs. The labs skill has no `references/java-spring.md` yet — worth writing one before producing topic 004, so all five Spring labs scaffold identically.
- **Verification:** every lab's final assembled state should be built and run before shipping, as with the Loops lab; for Spring Web labs that includes scripted `curl` calls against the running app.
