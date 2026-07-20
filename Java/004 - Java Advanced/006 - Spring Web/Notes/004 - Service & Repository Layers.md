# Service & Repository Layers

Every piece is on the table — controllers, services, repositories, entities, DTOs. This lesson assembles them into the **layered architecture** that structures virtually every Spring application, and states the rules that keep the layers doing their jobs. None of it is new machinery; it's the discipline of arranging the machinery we have.

---

## The Three Layers

Requests flow down, data flows back up, and each layer has one responsibility:

```
HTTP  →  Controller (web layer)      — translate HTTP ⇄ DTOs; nothing else
             ↓
         Service (business layer)    — use cases, rules, transactions; the application's brain
             ↓
         Repository (data layer)     — persistence behind an interface
             ↓
         Database
```

*The dependency direction is one-way: each layer knows the one below, never the one above.*

```java
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {                       // WEB: thin by design
    private final InvoiceService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse create(@Valid @RequestBody CreateInvoiceRequest req) {
        return service.create(req);                    // bind, validate, delegate — that's all
    }
}

@Service
public class InvoiceService {                          // BUSINESS: rules and orchestration
    private final InvoiceRepository invoices;
    private final CustomerRepository customers;

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest req) {
        Customer customer = customers.findByName(req.customer())
                .orElseThrow(() -> new CustomerNotFoundException(req.customer()));
        if (customer.isDelinquent()) {
            throw new CustomerDelinquentException(customer.getId());     // the business rule
        }
        Invoice saved = invoices.save(new Invoice(customer, req.total()));
        return InvoiceResponse.from(saved);            // entities in, DTO out
    }
}

public interface InvoiceRepository extends JpaRepository<Invoice, Long> { }   // DATA: an interface
```

*One use case, three layers: HTTP handled at the top, policy in the middle, persistence at the bottom.*

---

## What Each Layer Owns — and Must Not

**Controllers stay thin.** Binding, validation triggering, delegation, DTO returns. The test: a controller method should read as *routing*, not logic. Business rules in controllers can't be reused (a scheduled job can't call an endpoint), can't be tested without HTTP scaffolding, and multiply when a second interface (CLI, queue consumer, GraphQL) arrives — every rule would need writing twice.

**Services own the use cases.** Each public service method is one business operation, named in domain language (`create`, `applyLateFee`, `writeOff`) — and it owns the **transaction boundary**: `@Transactional` belongs here, wrapping the whole use case so its steps commit or roll back as a unit (the persistence-context lesson's machinery, placed at its correct altitude — repositories are too fine-grained, controllers too coarse and web-coupled). Services speak entities and DTOs, throw domain exceptions, and know nothing of HTTP: no `ResponseEntity`, no status codes — that's the advice handler's translation job.

**Repositories stay dumb.** Queries and persistence, one entity aggregate each, zero business policy. A repository method named `findInvoicesEligibleForLateFee` has quietly swallowed a business rule ("eligible" is policy); `findByStatusAndDueDateBefore` keeps the rule in the service where it's visible and testable.

The one-way dependency rule has a corollary worth stating: entities and repositories never reference services or controllers; nothing imports "upward." Spring's constructor injection makes the graph explicit — a controller constructor demanding a repository is the smell of a layer being skipped.

---

## The Payoff: Testing at Every Altitude

The layering maps one-to-one onto the testing topic's toolkit — each layer testable in isolation because its neighbors are interfaces or mocks:

- **Service tests** — plain JUnit + Mockito: mock the repositories, drive the use case, assert outcomes and thrown exceptions. This is where business-rule coverage lives, fast and DB-free.
- **Repository tests** — `@DataJpaTest`: real queries against the embedded database.
- **Controller tests** — `@WebMvcTest` + `MockMvc`: the HTTP contract (paths, statuses, JSON shapes, validation), service mocked.
- **A few end-to-end tests** — `@SpringBootTest` boots the whole context to prove the layers actually wire together; kept few because they're slow and every rule already has a fast test at its home layer.

*The test pyramid, realized: many fast unit tests at the service layer, targeted slices around them, a thin cap of full-stack checks.*

---

## The Unit's Architecture, Complete

Trace one POST through everything this unit built: Tomcat accepts it on a pooled thread (Concurrency) → the DispatcherServlet routes by reflectively-discovered annotations (Reflection) → binding and validation shape it into a DTO → the injected service (Spring Core) runs the use case in a transaction, consulting repositories that are generated proxies over JPA entities (Spring Data) → domain exceptions translate to status codes at the advice boundary → the DTO returns as JSON. Every arrow tested at its own layer (Unit Testing). One topic remains — Design Patterns — which names the recurring shapes we've been using all along and adds the last few worth carrying deliberately.
