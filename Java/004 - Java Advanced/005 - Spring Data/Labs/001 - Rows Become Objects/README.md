# Lab: Rows Become Objects — JPA Entities & Repositories

In this lab we build an invoice persistence layer on an in-memory H2 database — zero external setup, full JPA machinery. We'll annotate an `Invoice` entity field by field (narrating every decision: identity, money, enums, the no-arg constructor), declare a repository *interface* and confront the fact that nobody ever writes its implementation, run CRUD through a `CommandLineRunner` with `show-sql` proving what happens underneath, watch dirty checking issue an `UPDATE` we never asked for, and pin the behavior down with a `@DataJpaTest`.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |
| Spring Boot (via `pom.xml`) | 4.1.x |
| H2 (via `pom.xml`, in-memory) | runtime dependency |

The scaffold has the `data-jpa` and `h2` starters. From this lab's folder:

```console
mvn spring-boot:run
```

It starts, connects to an H2 database conjured out of RAM, finds nothing to do, and exits. Before writing code, add two lines to [src/main/resources/application.properties](src/main/resources/application.properties) — we want to *see* the SQL this lab is secretly about:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

## Part 1 — The entity, one decision at a time

Create `Invoice.java`. We'll build it deliberately, because every annotation answers a question a database schema forces:

```java
package com.curriculum.labs;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private LocalDate issuedOn;

    protected Invoice() { }                    // for JPA, not for you

    public Invoice(String customerName, BigDecimal total, LocalDate issuedOn) {
        this.customerName = customerName;
        this.total = total;
        this.issuedOn = issuedOn;
        this.status = InvoiceStatus.DRAFT;
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotal() { return total; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDate getIssuedOn() { return issuedOn; }

    public void setStatus(InvoiceStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Invoice[" + id + ", " + customerName + ", " + total + ", " + status + ", " + issuedOn + "]";
    }
}
```

And the status enum, `InvoiceStatus.java`:

```java
package com.curriculum.labs;

public enum InvoiceStatus { DRAFT, SENT, PAID }
```

The decisions, narrated:

- **`@Entity`** — "this class maps to a table." Hibernate will derive table `invoice` and a column per field.
- **`@Id` + `@GeneratedValue(IDENTITY)`** — every row needs identity; we delegate numbering to the database's auto-increment. The field is `Long` (the wrapper — it must be *nullable*, because an unsaved invoice has no id yet; watch for exactly that in Part 3).
- **`BigDecimal` for money** — never `double`. Binary floating point cannot represent 0.10; invoices that drift by fractions of a cent are a career-limiting bug. `BigDecimal` maps to an exact `numeric` column.
- **`@Enumerated(EnumType.STRING)`** — stores `"PAID"`, not `2`. The default (`ORDINAL`) stores the enum's *position*, which silently corrupts every row the day someone reorders or inserts a constant. STRING costs bytes and survives refactors; take that trade every time.
- **The `protected` no-arg constructor** — JPA materializes rows by instantiating the class reflectively (last topic's `newInstance`, industrialized) and then populating fields. It needs a no-arg path; `protected` keeps it out of everyone else's way while satisfying the spec. The *public* constructor is the one with business rules: new invoices start as `DRAFT`.

---

## Part 2 — The repository: an interface with no implementation, anywhere

Create `InvoiceRepository.java` — in its entirety:

```java
package com.curriculum.labs;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
```

*Entity type and id type in the generics — and that's the whole file.*

Sit with the oddity for a second: this is an interface. It has no implementing class in this project — search if you like; you won't find one, and you never will. Yet in the next part we'll inject it and call `save`, `findById`, `findAll`, `count`, `delete`... At startup, Spring Data inspects the interface, and **generates the implementation** — a proxy assembled with, once again, the reflection machinery from two topics ago. The framework writes the DAO; we write the *contract*. (Next lab this gets better: methods it implements from nothing but their *names*.)

---

## Part 3 — CRUD, with the SQL showing

Wire a runner into `InvoicesApplication` (constructor-inject the repository, same pattern as every bean so far):

```java
@SpringBootApplication
public class InvoicesApplication implements CommandLineRunner {

    private final InvoiceRepository invoices;

    public InvoicesApplication(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public static void main(String[] args) {
        SpringApplication.run(InvoicesApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Invoice draft = new Invoice("Globex Corp", new BigDecimal("1200.00"), LocalDate.now());
        System.out.println("before save, id = " + draft.getId());

        Invoice saved = invoices.save(draft);
        System.out.println("after save,  id = " + saved.getId());

        Optional<Invoice> found = invoices.findById(saved.getId());
        System.out.println("findById -> " + found.orElseThrow());

        System.out.println("missing id -> " + invoices.findById(999L));

        invoices.save(new Invoice("Initech", new BigDecimal("450.50"), LocalDate.now()));
        invoices.findAll().forEach(System.out::println);
        System.out.println("count = " + invoices.count());
    }
}
```

*(Imports: `java.math.BigDecimal`, `java.time.LocalDate`, `java.util.Optional`, plus the Boot ones already there.)*

Run it and read the output as two interleaved stories. The Java story: id is `null` before save and `1` after (the database numbered it); `findById` returns `Optional<Invoice>` — present for id 1, `Optional.empty` for 999, the Intermediate unit's null-discipline holding the line at the database boundary. The SQL story, courtesy of `show-sql`: a Hibernate `create table` at startup (schema derived from the entity — default behavior for an embedded database), one `insert` per save, a `select ... where id=?` per find. Rows became objects; the mapping is real and visible.

---

## Part 4 — Dirty checking: the UPDATE nobody wrote

JPA's most magical-looking behavior, provoked deliberately. Create a small service, `InvoiceAdmin.java`:

```java
package com.curriculum.labs;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceAdmin {

    private final InvoiceRepository invoices;

    public InvoiceAdmin(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    @Transactional
    public void markPaid(Long id) {
        Invoice invoice = invoices.findById(id).orElseThrow();
        invoice.setStatus(InvoiceStatus.PAID);
        // no save(...) — that's the point
    }
}
```

*A `@Transactional` method that loads an entity, mutates it, and conspicuously never calls save.*

Inject `InvoiceAdmin` into the runner alongside the repository and append to `run(...)`:

```java
        admin.markPaid(saved.getId());
        System.out.println("after markPaid -> " + invoices.findById(saved.getId()).orElseThrow());
```

Run and watch the SQL: a `select`, then — with no `save` anywhere — an **`update`** setting the status. Inside a transaction, JPA's persistence context *tracks every entity it loaded*; at commit it diffs them against their loaded state and flushes changes automatically. That's **dirty checking**: within a transaction, *a managed entity's setters are effectively writes to the database*. It's why `@Transactional` service methods (next labs) mostly just mutate objects and let commit do the rest — and why mutating an entity you didn't mean to change is a real bug class. Try removing `@Transactional` and rerunning: no update — the entity was loaded, modified, and abandoned, because nobody was tracking. Put the annotation back.

---

## Part 5 — Proof under test: @DataJpaTest

Create `src/test/java/com/curriculum/labs/InvoiceRepositoryTest.java`:

```java
package com.curriculum.labs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class InvoiceRepositoryTest {

    @Autowired
    InvoiceRepository invoices;

    @Test
    void savedInvoiceRoundTripsWithGeneratedIdAndDefaultStatus() {
        Invoice saved = invoices.save(
                new Invoice("Globex Corp", new BigDecimal("1200.00"), LocalDate.of(2026, 7, 1)));

        Optional<Invoice> found = invoices.findById(saved.getId());

        assertTrue(found.isPresent());
        assertAll("round trip",
            () -> assertNotNull(found.get().getId()),
            () -> assertEquals("Globex Corp", found.get().getCustomerName()),
            () -> assertEquals(new BigDecimal("1200.00"), found.get().getTotal()),
            () -> assertEquals(InvoiceStatus.DRAFT, found.get().getStatus()));
    }

    @Test
    void findByIdOnAMissingRowIsEmptyNotNullAndNotAnException() {
        assertTrue(invoices.findById(999L).isEmpty());
    }
}
```

*`@DataJpaTest`: a slice test — it boots only the JPA layer against a fresh embedded database, wraps each test in a rolled-back transaction, and leaves the web/service world out entirely.*

`mvn test`: green, fast, and self-cleaning — the rollback means the two tests can't contaminate each other (JUnit's isolation guarantee, extended to the database). Everything from the Unit Testing topic applies verbatim; only the fixture got heavier.

---

## Exercises

1. **A second aggregate.** Add a `Customer` entity (id, name, billing email) with its own repository, and extend the runner to save two customers and list them. Every annotation decision from Part 1 recurs — make each one deliberately, including one new decision Part 1 didn't force: should `email` be unique? (Look up `@Column(unique = true)` and use it, then prove it works by saving a duplicate and reading the failure.)

2. **Pages, not lists.** `findAll()` returning everything is a trap the first time a table has a million rows. Using `findAll(PageRequest.of(page, size, Sort.by("total").descending()))`, print invoices in pages of 2: for each page, its number, `getTotalElements()`, `getTotalPages()`, and the rows. Seed enough invoices to make three pages. (You'll need a couple more `save` calls — or a loop; the repository won't mind.)

3. **Schema review.** A teammate submits this entity. List at least three distinct problems, say what each breaks *concretely* (not "bad practice" — what goes wrong, when), and write the corrected version. There are more than three.

   ```java
   @Entity
   public class Payment {

       @Id
       private String payerEmail;

       private double amount;

       @Enumerated
       private PaymentMethod method;      // enum: CARD, ACH, WIRE

       private String order;

       public Payment(String payerEmail, double amount) {
           this.payerEmail = payerEmail;
           this.amount = amount;
       }
   }
   ```
