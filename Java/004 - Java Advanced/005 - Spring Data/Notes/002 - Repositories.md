# Repositories

The **repository** pattern gives data access a home: one component per entity type, owning every query and update for it, presenting a collection-like interface to the rest of the application ("find this invoice", "save that one") while hiding the persistence machinery. Spring Data JPA's twist is startling the first time: **we write only the interface — the implementation is generated at runtime.**

---

## An Interface Is Enough

```java
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
                                        //              entity ↑     ↑ ID type
}
```

*A complete, working repository. No implementation class exists anywhere in our code.*

At startup, Spring Data detects interfaces extending `JpaRepository`, generates an implementation (a dynamic proxy — the reflection topic's machinery in production), and registers it as a bean. Injecting and using it follows every rule from Spring Core:

```java
@Service
public class BillingService {
    private final InvoiceRepository invoices;

    public BillingService(InvoiceRepository invoices) {    // constructor injection, as always
        this.invoices = invoices;
    }

    @Transactional
    public Invoice open(String customer, BigDecimal total) {
        return invoices.save(new Invoice(customer, total)); // INSERT; returns with generated id
    }
}
```

*The repository injected like any bean and used like a collection — the SQL happens behind `save`.*

---

## The Inherited Toolkit

`JpaRepository<T, ID>` brings a full CRUD API:

```java
Invoice saved = invoices.save(invoice);              // insert OR update (id decides)
Optional<Invoice> found = invoices.findById(42L);    // Optional! — Unit 3's lesson, honored
List<Invoice> all = invoices.findAll();
long n = invoices.count();
boolean exists = invoices.existsById(42L);
invoices.deleteById(42L);
invoices.saveAll(batch);
```

*The free methods: create, read, update, delete, count — typed to the entity, no strings anywhere.*

Details worth pausing on. **`findById` returns `Optional<Invoice>`** — the entire Optionals lesson operationalized; the `orElseThrow` idiom appears at virtually every call site. **`save` is upsert-shaped**: a null-id entity inserts, an existing-id entity merges — and per the persistence-context lesson, updates to managed entities inside a transaction don't even need `save`. **`findAll` is a loaded weapon** on large tables — which is why pagination is built into the base interface:

```java
Page<Invoice> page = invoices.findAll(
        PageRequest.of(0, 20, Sort.by("createdAt").descending()));

List<Invoice> content = page.getContent();           // the 20 rows
long totalRows = page.getTotalElements();            // plus count metadata
```

*`Pageable` in, `Page` out: LIMIT/OFFSET queries and the count, standardized — the raw material of every paged API endpoint.*

---

## Testing the Slice

Repositories get their own test support — `@DataJpaTest` boots a JPA-only slice of the context against an embedded database (H2 as a test dependency), wrapping each test in a rolled-back transaction:

```java
@DataJpaTest
class InvoiceRepositoryTest {

    @Autowired InvoiceRepository invoices;

    @Test
    void savedInvoiceGetsAnId() {
        Invoice saved = invoices.save(new Invoice("Ada Corp", new BigDecimal("150.00")));

        assertNotNull(saved.getId());
        assertEquals(1, invoices.count());
    }
}
```

*A repository test: real JPA, real (in-memory) SQL, isolated per test — the Unit Testing discipline extended one layer down.*

(In service-layer unit tests, meanwhile, the repository is exactly what Mockito mocks — the interface-shaped boundary the testing topic promised.)

---

## Why the Pattern Matters Beyond the Convenience

The generated CRUD is the headline, but the architectural value is the *boundary*: every query for `Invoice` lives behind `InvoiceRepository`, so the rest of the codebase contains zero SQL, zero JPA, zero persistence detail — swap PostgreSQL, add caching, or restructure tables and the blast radius is one interface's implementation. It's the program-to-abstractions principle applied to the messiest dependency most applications have. The `@Repository` stereotype's exception translation (converting JDBC/JPA exceptions to Spring's unchecked `DataAccessException` family) reinforces the same seam.

What the free methods can't do is *domain-specific* lookups — invoices by customer, overdue past a date, top ten by amount. Spring Data's answer keeps the no-implementation trick going: queries derived from method names, and annotated JPQL for everything else. Next lesson.
