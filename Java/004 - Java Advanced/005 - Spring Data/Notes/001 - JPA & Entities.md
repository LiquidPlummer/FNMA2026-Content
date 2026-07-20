# JPA & Entities

Applications keep their real state in relational databases; Java keeps its state in objects. Bridging the two by hand — SQL strings, `ResultSet` loops, copying columns into fields — is repetitive and error-prone at scale. **JPA** (Jakarta Persistence API) is Java's standard **object-relational mapping (ORM)** specification: classes map to tables, instances to rows, fields to columns, with the mapping declared in annotations. **Hibernate** is the implementation Spring Boot ships; **Spring Data JPA** (via `spring-boot-starter-data-jpa`) is the layer that makes using it nearly effortless.

---

## An Entity

An **entity** is a class mapped to a table:

```java
import jakarta.persistence.*;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // DB assigns the key on insert
    private Long id;

    @Column(nullable = false, length = 40)
    private String customer;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal total;                              // money: BigDecimal — never double!

    @Enumerated(EnumType.STRING)                           // store the enum's NAME, not ordinal
    private InvoiceStatus status;

    private Instant createdAt;                             // unannotated: maps by convention

    protected Invoice() {}                                 // JPA requires a no-arg constructor

    public Invoice(String customer, BigDecimal total) {
        this.customer = customer;
        this.total = total;
        this.status = InvoiceStatus.PENDING;
        this.createdAt = Instant.now();
    }
    // getters; setters only where mutation is legitimate
}
```

*A complete entity: identity, column mappings, and the conventions that prevent real-world grief.*

The annotations in order of importance. **`@Entity`** marks the class for mapping (with `@Table` naming the table when it differs from the class). **`@Id`** marks the **primary key** — every entity must have one — with `@GeneratedValue` delegating key assignment to the database. **`@Column`** customizes name and constraints; unannotated fields map by name convention (`createdAt` → `created_at`). The details that pay rent later: `EnumType.STRING` (the default, `ORDINAL`, breaks when constants reorder — the exact `ordinal()` warning from the Enums lesson), `BigDecimal` for money (the Primitives lesson's rule, now with a database), and the **no-arg constructor** — JPA instantiates entities reflectively and populates private fields the way the Reflection topic showed, so it needs that constructor (`protected` keeps it out of everyone else's way).

Entity design guidance: entities are *mutable by nature* (their job is representing changeable rows) but deserve the Unit 2 discipline anyway — meaningful constructors for creation, targeted setters or intention-named methods (`markPaid()`) over blanket setters, validation at the door.

---

## The Persistence Context: Managed Objects

JPA's central runtime idea: entities loaded or saved within a transaction become **managed** — tracked by the **persistence context**, which notices changes and writes them back automatically:

```java
@Transactional                                  // one transaction = one unit of work
public void applyLateFee(Long invoiceId) {
    Invoice inv = repository.findById(invoiceId).orElseThrow();
    inv.setTotal(inv.getTotal().add(LATE_FEE)); // just mutate the object...
}                                               // ...commit flushes an UPDATE — no save() call
```

*Dirty checking: within a transaction, modifying a managed entity IS the database update.*

**`@Transactional`** (on service methods — the layering formalized in the Spring Web topic) wraps the method in a database transaction: everything commits together or rolls back together, and a `RuntimeException` triggers the rollback (the unchecked-exception convention from Unit 3, bearing infrastructure weight). Within it, the persistence context also deduplicates — loading the same row twice yields the *same object* — and defers SQL until flush, batching work.

The model to hold: **inside a transaction, entities are live views of rows; outside, they're detached snapshots.** A large share of JPA confusion (the notorious `LazyInitializationException`, arriving with relationships in lesson 4) is code touching entity state after the context that managed it has closed.

---

## What JPA Buys, and What It Costs

The wins are large: no hand-written SQL for the standard 90% (generated inserts, updates, lookups), database portability (PostgreSQL locally, whatever production runs — the dialects are Hibernate's problem), type-safe queries against fields rather than strings-with-column-names, and schema conventions kept in one place. The costs are real too: SQL doesn't disappear — it goes *behind* an abstraction, and diagnosing performance requires looking through it (`spring.jpa.show-sql=true` in dev; reading what Hibernate actually emits is a professional skill, not an admission of failure). ORM rewards developers who know SQL and punishes those who hoped to avoid it.

With entities mapped and the persistence context understood, the striking part is what we *haven't* written: any data-access code. The interface that makes CRUD nearly free — Spring Data's repositories — is the next lesson.
