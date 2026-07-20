# AI for Data Seeding

Every environment below production needs data: local dev databases, demo instances, integration test fixtures, performance test loads. Hand-writing it produces the sad familiar result — three customers named "Test Test," every invoice for $100, no edge cases anywhere. Generating **realistic, varied, edge-case-bearing seed data** is a task AI is almost perfectly shaped for — with one bright-line rule governing all of it: **synthetic data only, never sanitized production data by way of a prompt.** (Copying real customer rows into an AI conversation is a privacy incident, not a shortcut — the PII Guardrails lesson formalizes this; here we simply never need to, because generation beats copying.)

---

## Prompting for Data Worth Testing Against

The quality lever is specifying *distribution and edge cases*, not just shape:

```text
Generate seed data for our invoice schema (#file:V1__init.sql): 25 customers,
~200 invoices, as INSERT statements (PostgreSQL).

Realism requirements:
- names/companies/addresses: plausible and diverse, clearly fictional
  (no real-looking emails — use example.com/example.org domains)
- amounts: log-normal-ish spread, $12 to $48,000, not round numbers
- dates: weighted toward recent months across two years; include
  invoices issued on month boundaries and Feb 29
- statuses: ~70% PAID, 20% PENDING, 10% CANCELLED — and every status
  must appear for at least one customer

Deliberate edge cases (include and mark with a comment):
- a customer with zero invoices, and one with 40+
- names with apostrophes, diacritics, and CJK characters
- a maximum-length memo field; an invoice with 200 line items
- same-day duplicate-amount invoices for one customer
```

*A seeding brief: schema attached, distributions stated, and the edge cases named — because unprompted AI data is uniformly pleasant and finds no bugs.*

That last section is the difference between decoration and a test asset. Default generated data is *too clean* — evenly spread, ASCII-only, all mid-sized. Real data has skew, gaps, hostile strings, and boundary dates, and every item in that edge-case list is a production bug class: the apostrophe that breaks naive SQL, the empty-relationship customer that NPEs the dashboard, the 40-invoice customer that exposes the missing pagination.

**Respect referential integrity by construction:** generate in dependency order (customers → invoices → line items) and have totals *derive* from line items rather than being independently random — inconsistent seed data (invoice totals that don't match their items) wastes debugging hours on bugs that aren't real. Asking the AI to "make derived fields consistent, and state which fields derive from which" handles it.

---

## Formats and Mechanisms

Match the artifact to the consumer: **SQL inserts** for schema-level seeding, **JSON/CSV fixtures** for import pipelines, **builder calls** for test code (`anInvoice().withStatus(PAID)...` — AI generates both the builder and the fleet of varied instances), or — for volume — **a generator program**: past a few hundred rows, have AI write a *seeding script* (Java + Faker, or SQL `generate_series`) with the distributions parameterized, rather than emitting ten thousand literal rows. The script is reviewable, re-runnable, and tunable; the ten-thousand-row dump is none of those. Determinism matters in test fixtures too — a fixed random seed, so failures reproduce.

Two verification habits close the loop, per the usual discipline: load the data (FK violations and over-length strings surface immediately — the constraint check *is* the test), and spot-check a sample against the brief ("show me the row counts per status and the min/max amounts" — or just ask AI to write that verification query too).

Seed data rounds out the persistence toolkit: schema, migrations, queries, and the data to exercise them. Next topic, the reliability problem that gets sharper the further we get from well-trodden ground: hallucination, in cloud contexts especially.
