# Columnar Storage

**Redshift** stores data by column rather than by row. That single structural difference determines which queries are fast, which are slow, and why Redshift is not a replacement for a transactional database.

---

## Row vs Column Storage

A row store keeps each record's fields together:

```
Row store:   [1, Alice, 2026-01-15, 249.99] [2, Bob, 2026-01-16, 89.50] ...
```

*Reading one complete record touches one contiguous region — efficient for "get order 5501."*

A column store keeps each field's values together:

```
Column store:
  order_id:    [1, 2, 3, 4, ...]
  customer:    [Alice, Bob, Carol, ...]
  order_date:  [2026-01-15, 2026-01-16, ...]
  total:       [249.99, 89.50, 412.00, ...]
```

*Reading `total` for every row touches one contiguous region and reads nothing else.*

---

## What This Changes

**Analytical queries read far less data.** `SELECT AVG(total) FROM orders` reads only the `total` column. In a row store it would read every column of every row and discard most of it. On a table with 50 columns, that is roughly a fiftieth of the I/O.

**Compression is dramatically better.** A column holds values of one type, often with low cardinality and similar magnitude. Run-length, delta, and dictionary encodings apply far more effectively than to mixed-type rows. Compression ratios of 3–10x are typical, and less data on disk means less to read.

**Single-row operations are expensive.** Fetching one complete row means reading from every column's storage. Inserting one row means writing to every column. Both are efficient in a row store and inefficient here.

**Updates and deletes are costly.** Redshift marks rows deleted rather than removing them, and reclaiming the space requires a `VACUUM`. Frequent small updates degrade performance steadily.

---

## The Consequence

| Query | Row store | Column store |
|---|---|---|
| `SELECT * WHERE id = 5501` | **Fast** | Slow |
| `SELECT AVG(total)` over 100M rows | Slow | **Fast** |
| `INSERT` one row | **Fast** | Slow |
| `INSERT` 10 million rows in bulk | Slow | **Fast** |
| `UPDATE` one row | **Fast** | Slow |
| `GROUP BY category` over everything | Slow | **Fast** |

**Redshift is for reading a few columns across very many rows.** That is what analytics is, and it is the opposite of what a transactional application does.

---

## Zone Maps

Alongside columnar storage, Redshift keeps **zone maps** — the minimum and maximum value of each column within each 1 MB block.

A query filtering `WHERE order_date >= '2026-09-01'` checks zone maps and skips every block whose maximum date is earlier, without reading them.

This can eliminate most of a table's I/O, and it is why **sort key choice matters so much**. When data is sorted by the column queries filter on, zone maps are highly selective. When it is not, blocks contain a wide range of values, every zone map overlaps the filter, and nothing is skipped.

Sort keys are covered with distribution styles in a later note; the mechanism they serve is this one.

---

## What Redshift Is Not

**Not a transactional database.** No efficient single-row operations, no high-concurrency writes, and no primary key enforcement — Redshift accepts declared constraints and does not enforce them, which surprises people who rely on the database to prevent duplicates.

**Not a low-latency lookup store.** Queries take seconds; that is normal for scanning millions of rows and unacceptable for a user-facing page load.

**Not a document or key-value store.** It supports `SUPER` for semi-structured data, and it is a relational analytical database.

The correct arrangement is Redshift alongside a transactional store — an application writing to RDS or DynamoDB, with data loaded into Redshift for analysis.

---

## Key Takeaways

- Redshift stores data by column, so a query reads only the columns it references.
- Columnar layout enables much better compression, typically 3–10x, further reducing I/O.
- Single-row reads, inserts, updates, and deletes are all expensive, and deletes require `VACUUM` to reclaim space.
- Redshift suits reading few columns across many rows — analytical queries, not transactional ones.
- Zone maps record min and max per block, letting Redshift skip blocks that cannot match a filter.
- Zone map effectiveness depends on sort key choice, which is why sort keys matter so much.
- Redshift does not enforce primary keys or uniqueness despite accepting the declarations.
- Use it alongside a transactional database, not instead of one.
