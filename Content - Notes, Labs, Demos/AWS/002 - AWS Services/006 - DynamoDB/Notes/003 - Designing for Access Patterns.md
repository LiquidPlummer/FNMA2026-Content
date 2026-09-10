# Designing for Access Patterns

Relational modelling normalizes data first and writes queries later. DynamoDB reverses this: **enumerate the queries first, then design a table that serves them.** The reversal is not a stylistic preference — it follows from the key being permanent and queries being limited to it.

---

## Why the Order Is Reversed

In a relational database, a new query is a new `WHERE` clause. The schema does not change, and the query planner finds a path — perhaps slowly, but it works.

In DynamoDB, a query not supported by the key or an index has no path. The options are a full table scan, a new index, or a data model change. **The data model is the query plan**, and it is fixed at creation.

So access patterns come first. Getting them wrong means migrating a table rather than writing a different query.

---

## The Process

**1. List every access pattern.** Concretely, with the inputs available at the time:

```
1. Get a customer by customer ID
2. Get all orders for a customer, most recent first
3. Get one order by order ID
4. Get all orders with status "pending", across all customers
5. Get all orders placed in a date range for a customer
6. Get all items in an order
```

**2. Note the frequency and latency requirement of each.** A pattern run thousands of times a second deserves the primary key; one run nightly for a report can afford a scan or a separate process.

**3. Design keys so the highest-value patterns are single queries.**

**4. Add secondary indexes for the patterns that remain.**

**5. Accept that rare patterns may be inefficient.** Pattern 4 above — all pending orders across all customers — is a poor fit for a key-value store. A sparse index, a separate table, or exporting to another system for analytics is a better answer than distorting the model.

---

## Single-Table Design

The patterns above can be served by one table with generic key names:

| PK | SK | Attributes |
|---|---|---|
| `CUST#C-1001` | `PROFILE` | name, email |
| `CUST#C-1001` | `ORDER#2026-09-09#O-5503` | total, status |
| `CUST#C-1001` | `ORDER#2026-03-22#O-5502` | total, status |
| `ORDER#O-5503` | `ITEM#A-12` | sku, qty, price |
| `ORDER#O-5503` | `ITEM#B-07` | sku, qty, price |

*Generic `PK` and `SK` names are conventional, because the attributes hold different meanings per item type. Patterns 1, 2, 5, and 6 are each one query.*

The appeal is fewer requests: retrieving a customer with their recent orders is one query rather than a join or several round trips. The cost is readability — the table is difficult to interpret without documentation, and ad hoc inspection is unpleasant.

**Single-table design is not mandatory.** For a small application with a handful of entities and modest scale, separate tables are clearer and the extra requests are inconsequential. The technique earns its complexity at scale, or where minimizing round trips matters.

---

## Denormalization

Without joins, data needed together is often stored together — including duplicated.

An order item might carry the product name and price at time of purchase, rather than referencing a product record. This is partly to avoid a second lookup and partly correct on its own terms: the price at purchase time is a property of the order, not the product.

Duplication means updates must touch several items. The judgement is whether the duplicated data changes often, and whether it should change retroactively. Historical values that should not change are ideal candidates; frequently updated shared values are not.

---

## Recognising a Poor Fit

DynamoDB is the wrong choice when:

- **Access patterns are unknown or change frequently.** An analytics or exploratory workload needs ad hoc queries.
- **Complex joins across many entities are central.**
- **Aggregations are needed** — DynamoDB has no `SUM`, `COUNT`, or `GROUP BY`. Aggregates must be maintained incrementally with atomic counters or computed elsewhere.
- **Strong transactional consistency across many entities is required.** Transactions exist but are limited to 100 items.
- **Data volume is small and query flexibility matters more than scale.**

Choosing DynamoDB and then needing relational queries produces the worst outcome: scans everywhere, unpredictable cost, and a data model that fights every requirement.

---

## Key Takeaways

- Enumerate access patterns before designing the table, since the key is permanent and queries are limited to it.
- The data model is effectively the query plan; an unsupported query has no path other than a scan or a new index.
- Record frequency and latency needs per pattern, and give the primary key to the highest-value ones.
- Single-table design serves several entity types from one table with generic key names, trading readability for fewer requests.
- Separate tables remain reasonable for small applications; single-table design earns its complexity at scale.
- Denormalization is normal — duplicate data that should not change retroactively, and think carefully about data that should.
- DynamoDB is a poor fit for ad hoc queries, complex joins, and aggregations.
