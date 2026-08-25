# Dimensional Modeling

**Dimensional modeling** is a way of designing a database schema specifically for analytical querying — the standard approach used inside data warehouses. If normalization is the design philosophy behind operational (OLTP) databases, dimensional modeling is its counterpart on the analytical (OLAP) side.

## Facts and Dimensions

The core idea: split your data into two kinds of tables.

- **Fact tables** store measurable events — things that happened. A sale, a shipment, a click. Each row is one event, holding numeric **measures** (revenue, quantity, duration) plus foreign keys pointing out to the dimensions that give it context.
- **Dimension tables** describe the who/what/where/when around those events — products, customers, stores, dates. Each row is one instance of a business entity, with descriptive attributes as columns.

A query against a dimensional model almost always follows the same shape: start at the fact table, join out to whichever dimensions you need, filter and group by dimension attributes, and aggregate the fact's measures. That consistency is the whole point — it makes analytical queries predictable and fast, instead of requiring a different complex join path for every question.

## Why Not Just Normalize Like an OLTP Database?

An OLTP schema minimizes redundancy — data gets broken into many small, related tables so nothing is duplicated. That's great for keeping writes consistent, but it means a single business question can require joining half a dozen tables.

A dimensional model does the opposite on purpose: it **denormalizes**, keeping dimension tables wide and flat even if that means repeating values. The trade is more storage for far simpler, faster queries — a trade that makes sense in a warehouse, where reads vastly outnumber writes and storage is comparatively cheap.

## Star Schema vs. Snowflake Schema

There are two common ways to arrange the dimensions around a fact table:

| | Star Schema | Snowflake Schema |
|---|---|---|
| Dimension shape | Flat — one table per dimension | Normalized — split into sub-tables by hierarchy |
| Joins per query | Fewer | More |
| Redundancy | Higher (values repeated) | Lower (each value stored once) |
| Common use | Default choice — BI tools expect this shape | Very large or frequently-restructured dimensions |

*Star* is the far more common default in modern warehouses — the storage saved by snowflaking rarely outweighs the extra join complexity, especially with modern compression. Snowflaking still shows up when a dimension is large enough, or changes structure often enough, that normalizing it genuinely reduces maintenance pain.

## Why This Matters for Application Developers

The tables an application writes to (OLTP, normalized) are usually not the same tables an analyst queries against (OLAP, dimensional) — and that's expected, not a sign something's wrong. Recognizing a fact-vs-dimension shape in a warehouse schema, and understanding *why* it looks so different from the application's own database, makes it much easier to reason about where a given piece of data actually lives and how it got there.
