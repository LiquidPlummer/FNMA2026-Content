# AI Persistence Solutioning

The persistence layer — schemas, entities, migrations, query design — is high-leverage AI territory: the work is pattern-rich (a thousand teams have modeled orders and customers), the artifacts are structured, and mistakes are *expensive and sticky* (a bad schema outlives every app that reads it). That combination sets the working posture for this lesson: delegate the drafting generously, review the data decisions ferociously.

---

## Schema and Entity Design

AI designs schemas well when briefed like a data modeler, not a code generator — give it the *domain facts and access patterns*, and demand the reasoning:

```text
Design the persistence for invoice management. Domain facts:
- invoices belong to one customer; 1-200 line items each
- line items are immutable once the invoice is issued
- invoices are soft-deleted, never removed (audit requirement)
- money amounts: exact decimals, multi-currency later is likely

Access patterns (in priority order):
1. fetch one invoice with all line items
2. list a customer's invoices, newest first, paginated
3. monthly totals per customer for reporting

Produce: the tables/columns with types and constraints, the JPA entities,
and — for each non-obvious choice — a one-line justification.
Flag any decision I should make instead of you.
```

*A modeling brief: facts, access patterns, and required justifications — the plan-first pattern applied to data.*

Reviewing the draft, the checklist runs on the decisions that hurt later: **types** (`DECIMAL`/`BigDecimal` for money — never float — and the currency column the "later is likely" fact demands now); **constraints in the database**, not just the entity (NOT NULL, unique, FK — the DB is the last line of defense and AI often leaves it to JPA annotations alone); **indexes matched to the stated access patterns** (pattern 2 needs `(customer_id, created_at)` — ask "which index serves each access pattern?" and gaps surface immediately); **relationship mappings** against the fetch behavior we actually want (the LAZY/EAGER and cascade rules from the Java curriculum apply verbatim — AI defaults are frequently EAGER-happy). A persona sharpens all of this: *"Now review your own design as a skeptical DBA who has to run this for five years."*

---

## Migrations: The High-Stakes Artifact

Schema *changes* are where AI assistance meets irreversibility. AI drafts Flyway/Liquibase migrations fluently — and the review bar is the highest in this lesson, because a bad migration executes against production data exactly once, destructively:

- **Demand the safe sequence, not just the DDL.** Adding a NOT NULL column to a live table is a three-step dance (add nullable → backfill → add constraint); AI knows this *when asked* — "write this as a zero-downtime migration" — and skips it when not.
- **Interrogate data loss explicitly:** *"Which rows or values could this migration lose or alter? What existing data would violate the new constraint?"* Run the check query it proposes *before* the migration.
- **Never let AI renumber or edit applied migrations** — new migrations only; checksums exist for a reason.
- **Test on a production-shaped copy** — which requires realistic data, the next lesson's subject.

---

## Query Work

The remaining daily win: AI translates intent to queries (JPQL, derived method names, native SQL) and — often more valuably — *reviews* them: "explain this query's plan risks given these indexes," "rewrite to avoid the N+1," "why is this slow at 10M rows?" Paste the real query and the `EXPLAIN` output; the analysis quality jumps with the evidence, exactly as in the debugging lesson. The standing caveats: verify suggested query methods exist (invented derived-query keywords are a classic hallucination), and parameterize always — string-built SQL from an AI is still injectable SQL (the Security unit returns to this with teeth).

The layer's overall shape: AI accelerates every artifact — schema, entity, migration, query — while the *decisions* (types, constraints, indexes, deletion semantics) stay human, because they're the part with a decade of consequences. And every one of those artifacts needs data to test against, which brings us to seeding.
