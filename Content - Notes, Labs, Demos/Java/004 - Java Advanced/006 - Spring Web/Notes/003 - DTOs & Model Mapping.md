# DTOs & Model Mapping

The last two lessons quietly used `InvoiceResponse` and `CreateInvoiceRequest` where `Invoice` — the JPA entity — would have "worked." That substitution has a name and a rationale. A **DTO** (data transfer object) is a class whose only job is carrying data across a boundary — here, the JSON shapes of the API — kept deliberately separate from the entity that models the database row. The separation looks like ceremony until the first time it isn't.

---

## Why Not Just Return the Entity?

Serializing entities straight to JSON couples the *public API contract* to the *database schema* — two things with different owners, different rates of change, and different security needs. The specific failure modes, each common enough to have war stories:

- **Leakage.** The entity has everything — cost basis, internal flags, audit columns, soon a `passwordHash` on some entity somewhere. Serialize the entity and every new column ships to the public by default. A DTO is an *allow-list*: only what's declared goes out.
- **Lazy-loading landmines.** Jackson walking an entity's relationships (previous topic) either triggers `LazyInitializationException` outside the transaction or serializes half the object graph — and bidirectional relationships (`Invoice` ↔ `Customer` ↔ invoices...) recurse until the stack dies.
- **Rigidity.** The API needs `customerName` flattened from the relationship; the schema wants normalization. With entities-as-JSON, one structure must serve both masters. DTOs let each side have its right shape.
- **Mass assignment on the way in.** Deserializing a request *into an entity* lets a malicious client set any field — `{"id": 7, "status": "PAID"}` on a create call. A request DTO physically cannot carry what it doesn't declare.

The rule that follows: **entities stop at the service layer; DTOs are the only types controllers see.** Requests arrive as purpose-named DTOs (`CreateInvoiceRequest` — carrying only what a *client* may specify), responses leave as others (`InvoiceResponse`).

---

## Records Are the DTO Type

DTOs are immutable data carriers — exactly what records (Unit 2) were made of:

```java
public record InvoiceResponse(
        Long id,
        String customer,          // flattened from the Customer relationship
        BigDecimal total,
        InvoiceStatus status,
        Instant createdAt) {}

public record CreateInvoiceRequest(
        @NotBlank String customer,
        @NotNull @Positive BigDecimal total) {}     // note what's absent: id, status, createdAt
```

*Response and request shapes as records: immutable, validated, and shaped for the API rather than the schema.*

Jackson handles records natively in both directions. The asymmetry between the two is the design: the response exposes server-owned facts; the request omits them — ids are generated, status starts at `PENDING`, timestamps come from the clock. Different operations get different DTOs when their shapes differ (`UpdateInvoiceRequest` without `customer`, say) — resisting the One Giant DTO that accumulates nullable fields for every use case.

---

## Mapping: The Honest Boilerplate

Something must convert. Written by hand, mapping is dull, explicit, and perfectly serviceable — a static factory on the DTO (or a small dedicated mapper class) is the standard shape:

```java
public record InvoiceResponse(Long id, String customer, BigDecimal total,
                              InvoiceStatus status, Instant createdAt) {

    public static InvoiceResponse from(Invoice entity) {
        return new InvoiceResponse(
                entity.getId(),
                entity.getCustomer().getName(),      // flatten inside the transaction!
                entity.getTotal(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}

// In the service:
public List<InvoiceResponse> findByStatus(InvoiceStatus status) {
    return repository.findWithCustomerByStatus(status).stream()   // JOIN FETCH from Spring Data
            .map(InvoiceResponse::from)                            // entity → DTO
            .toList();
}
```

*Hand mapping: a `from` factory per DTO, applied with a stream — every field conversion visible and debuggable.*

Two operational notes baked into that snippet: the relationship is flattened *while the entity is managed* (inside the transactional service, using the fetch strategies from Entity Relationships — DTO mapping is where lazy-loading discipline cashes out), and the request direction maps in the service too (`new Invoice(req.customer(), req.total())` — the entity's constructor enforcing its own invariants, as Unit 2 designed).

At scale, mapper *generators* take over the typing: **MapStruct** generates the same code at compile time from an interface declaration (fields matched by name, mismatches flagged at build time). Worth adopting once DTOs number in the dozens; the hand-written version above is what it generates, so nothing about it is magic. (Runtime reflection mappers like ModelMapper trade that compile-time safety away — the general reflection caveats apply.)

---

## The Boundary, Drawn

The type flow of the whole application is now explicit: **JSON ⇄ DTOs (controller) ⇄ entities (service, repositories) ⇄ rows** — each boundary translating, validating, and hiding what the next layer shouldn't see. That layering — who may call whom, and why the service sits in the middle — is the architecture this topic has been assembling piecemeal, and the final lesson lays it out whole.
