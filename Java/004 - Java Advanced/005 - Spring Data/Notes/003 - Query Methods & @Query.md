# Query Methods & @Query

The generated repository covers CRUD; real applications ask richer questions. Spring Data answers with two mechanisms on the same interface: **derived query methods**, where the method *name* is parsed into a query, and **`@Query`**, where we write the query ourselves in JPQL. Knowing where the first stops paying and the second starts is the practical skill of this lesson.

---

## Derived Query Methods

Declare a method whose name follows the grammar — `findBy` + field + condition, joined with `And`/`Or` — and Spring Data generates the implementation at startup:

```java
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCustomer(String customer);
    Optional<Invoice> findByInvoiceNumber(String number);         // unique → Optional
    List<Invoice> findByStatusAndTotalGreaterThan(InvoiceStatus s, BigDecimal min);
    List<Invoice> findByCreatedAtBetween(Instant from, Instant to);
    List<Invoice> findByCustomerContainingIgnoreCase(String fragment);
    List<Invoice> findTop10ByStatusOrderByTotalDesc(InvoiceStatus s);

    long countByStatus(InvoiceStatus s);
    boolean existsByInvoiceNumber(String number);
    List<Invoice> findByStatus(InvoiceStatus s, Pageable page);   // pagination composes in
}
```

*The naming grammar in action: fields, comparison keywords (`GreaterThan`, `Between`, `Containing`), ordering, limits — all parsed from the name.*

The generation happens at **startup**, which restores a compile-time-like guarantee: misspell a field (`findByCustomor`) and the application fails to boot with a clear error — not at 3 a.m. when the query first runs. Parameters bind positionally to the name's conditions; return types are flexible and meaningful — `Optional<T>` for at-most-one (the honest signature, again), `List<T>` for many, `long`/`boolean` for the count/exists forms, `Page<T>` with a `Pageable` parameter.

The grammar's limit is readability, and it arrives fast: `findByStatusAndCreatedAtAfterAndTotalGreaterThanEqualOrderByCustomerAsc` is a sentence pretending to be an identifier. House rule: **two, maybe three conditions — beyond that, switch mechanisms.**

---

## @Query: JPQL by Hand

**`@Query`** takes over where names give out. The language is **JPQL** — SQL-shaped, but phrased in *entities and fields* rather than tables and columns:

```java
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.status = :status
          AND i.total >= :minimum
        ORDER BY i.total DESC
        """)
    List<Invoice> findLargeByStatus(@Param("status") InvoiceStatus status,
                                    @Param("minimum") BigDecimal minimum);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.customer = :customer")
    BigDecimal totalBilledTo(@Param("customer") String customer);
}
```

*JPQL queries (in text blocks — the Strings lesson's syntax earning its keep): named parameters, aggregates, full control.*

Note `Invoice` and `i.total`, not `invoices` and `total_amount` — JPQL queries the *object model*, and Hibernate translates to SQL through the entity mappings, preserving database portability. The **named parameters** (`:status` bound via `@Param`) are the only acceptable way to get values in — string-concatenating a query is the door to **SQL injection**, the same lesson every data technology teaches and the reason it's worth stating in bold here: **parameters, never concatenation.**

Queries that *change* data add `@Modifying` (with `@Transactional` at the call site) — `UPDATE Invoice i SET i.status = 'CANCELLED' WHERE ...` — useful for bulk operations that would be absurd as load-mutate-save loops. And when JPQL itself can't express the query (database-specific features, exotic SQL), `@Query(value = "SELECT ...", nativeQuery = true)` drops to raw SQL — trading portability, explicitly and locally.

---

## Choosing, and Keeping Queries Honest

The escalation ladder, in the order to reach for it:

1. **Inherited CRUD** — free.
2. **Derived methods** — self-documenting, boot-verified; the default for simple lookups.
3. **`@Query` JPQL** — joins, aggregates, projections, anything with more than ~3 conditions.
4. **Native SQL** — the escape hatch; use sparingly and comment why.

Whatever the mechanism, repository methods deserve the `@DataJpaTest` treatment from last lesson — derived names *parse* at startup, but only a test proves the query means what we intended (boundary values for `Between`, case behavior of `Containing`, empty results). The queries so far touch one entity at a time, though — and real questions join: invoices *with their line items*, customers *with their invoices*. Mapping relationships between entities, and the loading semantics that come with them, is the final Spring Data lesson.
