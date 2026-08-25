# Entity Relationships

Rows relate: an invoice has line items, a customer has invoices, students attend courses. Relational databases express this with foreign keys and join tables; JPA maps those onto **object references between entities** — annotated with cardinality, direction, and loading behavior. Relationships are where ORM earns its keep and where its sharpest edges live; this lesson covers both.

---

## The Cardinalities

**`@ManyToOne`** — the workhorse: many invoices belong to one customer. It maps directly onto the foreign key column:

```java
@Entity
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)              // LAZY: explained below — always set it
    @JoinColumn(name = "customer_id")               // the FK column on the invoices table
    private Customer customer;
}
```

*The many side holds the foreign key, so `@ManyToOne` + `@JoinColumn` lives here — one reference, one column.*

**`@OneToMany`** — the same relationship seen from the customer: one customer, many invoices. Mapped as the *inverse* side, pointing back at the owning field:

```java
@Entity
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "customer")               // "the Invoice.customer field owns this"
    private List<Invoice> invoices = new ArrayList<>();
}
```

*The inverse side: `mappedBy` names the owning field — no new column, just the object-graph view of the same FK.*

The **owning side** concept trips everyone once, so plainly: the side *with the foreign key* (`@ManyToOne`) is the owning side, and **only changes to the owning side hit the database**. Adding an invoice to `customer.getInvoices()` alone persists nothing; setting `invoice.setCustomer(c)` is what writes. Bidirectional mappings must keep both sides consistent in memory (a small `addInvoice` helper that sets both is the standard fix) — or better, ask whether the `@OneToMany` side is needed at all: a unidirectional `@ManyToOne` plus a repository query (`findByCustomer`) is simpler and often enough.

**`@OneToOne`** (a user and its profile) works like `@ManyToOne` with a uniqueness constraint. **`@ManyToMany`** (students ↔ courses) maps a join table via `@JoinTable` — correct for pure link tables, but the moment the relationship itself carries data (enrollment date, grade), model the join table as its *own entity* with two `@ManyToOne`s. Most mature schemas end up there.

---

## Fetching: LAZY vs. EAGER

The million-dollar setting. **`FetchType.LAZY`** loads the related entity only when first accessed; **`EAGER`** joins it in immediately, every time, needed or not. The professional defaults: **declare every relationship LAZY** (`@ManyToOne` is EAGER by default — override it; `@OneToMany` is already LAZY), then *opt in* to loading per query. Two failure modes explain why this needs managing:

**The `LazyInitializationException`** — touching a lazy relationship after the transaction closed (the detached-snapshot warning from the JPA lesson, come due). The fix is never "make it EAGER"; it's loading what the use case needs *within* the transaction.

**The N+1 problem** — the ORM performance classic. Load 100 invoices, then touch each `.getCustomer()`: one query becomes 101. Diagnosis: `spring.jpa.show-sql=true` and a scroll of near-identical SELECTs. The cure is asking for the join explicitly:

```java
@Query("SELECT i FROM Invoice i JOIN FETCH i.customer WHERE i.status = :status")
List<Invoice> findWithCustomerByStatus(@Param("status") InvoiceStatus status);
```

*`JOIN FETCH`: one query, invoices and customers together — per use case, not baked into the mapping.*

(`@EntityGraph(attributePaths = "customer")` on a derived method is the annotation-flavored equivalent.)

---

## Cascading and Orphans

**Cascade** propagates operations across a relationship; **orphan removal** deletes children dropped from the collection. Both belong on true parent–child (composition) relationships only:

```java
@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
private List<LineItem> items = new ArrayList<>();     // items live and die with their invoice
```

*Full lifecycle coupling: saving the invoice saves its items; removing an item from the list deletes its row.*

The judgment call is the Unit 3 composition-vs-association distinction wearing persistence clothes: line items are *part of* an invoice — cascade everything. A customer is merely *referenced by* invoices — cascading from invoice to customer (especially `REMOVE`!) deletes shared data other rows still point at. When in doubt, no cascade; explicit repository calls are boring and safe.

---

## Spring Data, Closed Out

The topic in four sentences: entities map classes to tables; repositories put all data access behind generated interfaces; query methods and JPQL express the questions; relationships map the joins, with LAZY loading and explicit fetching keeping performance honest. The recurring meta-lesson — conventions with escape hatches, behind interfaces — is Spring's entire personality. What's left is the front door: exposing all of this over HTTP as a REST API, which is Spring Web, next.
