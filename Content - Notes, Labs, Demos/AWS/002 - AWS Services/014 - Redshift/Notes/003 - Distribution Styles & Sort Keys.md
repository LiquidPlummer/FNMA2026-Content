# Distribution Styles & Sort Keys

Two per-table decisions that determine Redshift performance more than anything else. **Distribution** controls which node holds each row; **sort order** controls which blocks a query can skip.

---

## Distribution Styles

Because compute nodes work in parallel on their own data, a join between two tables requires matching rows to be on the same node. When they are not, Redshift **redistributes** data across the network mid-query — which is usually the dominant cost of a slow query.

**`KEY`** — rows are distributed by the hash of a chosen column. Rows with the same value land on the same slice.

```sql
CREATE TABLE orders (
    order_id      BIGINT,
    customer_id   BIGINT,
    order_date    DATE,
    total         DECIMAL(10,2)
)
DISTSTYLE KEY
DISTKEY (customer_id)
SORTKEY (order_date);
```

*Joining `orders` to `customers` on `customer_id` requires no data movement if `customers` uses the same distribution key.*

**`ALL`** — a full copy of the table on every node. Joins never redistribute, at the cost of storing the table N times and slower loads. Appropriate for small, slow-changing dimension tables.

**`EVEN`** — round-robin distribution. Uniform storage, and every join redistributes. The right choice for a table that is not joined, or has no good key.

**`AUTO`** (the default) — Redshift chooses, starting with `ALL` for small tables and moving to `EVEN` or `KEY` as they grow. A reasonable default that leaves gains available for tables with a clear join pattern.

### Choosing a distribution key

**Choose the column used most often in joins.** That is the whole objective.

**Choose one with high cardinality and even distribution.** A low-cardinality key concentrates rows on few slices, and one slice holding most of the data means one node doing most of the work while the others idle. This is **data skew**, and it is the most common cause of a query that is slow for no apparent reason.

Skew is checkable:

```sql
SELECT slice, COUNT(*) FROM stv_blocklist
WHERE tbl = (SELECT id FROM stv_tbl_perm WHERE name = 'orders' LIMIT 1)
GROUP BY slice ORDER BY 2 DESC;
```

*Uneven counts across slices indicate skew. A ratio worse than roughly 2:1 between the largest and smallest is worth investigating.*

---

## Sort Keys

The sort key determines physical row order, which drives **zone map** effectiveness. A query filtering on the sort key skips blocks that cannot contain matching values.

**Choose the column most often used in `WHERE` clauses** — typically a date or timestamp, since analytical queries almost always constrain a time range.

**Compound sort keys** (the default) sort by the listed columns in order:

```sql
SORTKEY (order_date, customer_id)
```

*Effective for filters on `order_date`, or on `order_date` and `customer_id` together. A filter on `customer_id` alone benefits little, because rows for one customer are scattered across every date.*

The leading column matters most. This mirrors compound index behavior in a relational database.

**Interleaved sort keys** give equal weight to each column, helping when queries filter on different columns unpredictably. They carry higher maintenance cost, requiring `VACUUM REINDEX`, and are rarely worth it — a compound key on the dominant filter column is usually better.

---

## The Two Together

```sql
CREATE TABLE fact_orders (
    order_id     BIGINT,
    customer_id  BIGINT,
    product_id   BIGINT,
    order_date   DATE,
    total        DECIMAL(10,2)
)
DISTKEY (customer_id)     -- co-locate with the customers dimension for joins
SORTKEY (order_date);     -- skip blocks outside the queried date range
```

*The distribution key serves joins; the sort key serves filters. They address different costs and are chosen for different reasons.*

The typical star schema arrangement: the fact table distributed on its most-joined dimension key and sorted on date, with small dimension tables set to `DISTSTYLE ALL`.

---

## Maintenance

**`VACUUM`** reclaims space from deleted rows and restores sort order. `VACUUM SORT ONLY` re-sorts; `VACUUM DELETE ONLY` reclaims space. Redshift runs automatic vacuum in the background, and heavily modified tables can still benefit from an explicit run.

**`ANALYZE`** updates the statistics the query planner uses. Stale statistics produce poor plans, and this is a frequent cause of a query that suddenly became slow with no other change.

**Loading in sort key order** avoids much of the vacuum burden — appending data already sorted by date to a date-sorted table requires no re-sorting.

---

## Key Takeaways

- Distribution style determines which node holds each row, and mismatched distribution forces data redistribution during joins.
- `KEY` co-locates rows for joins, `ALL` replicates small tables everywhere, `EVEN` distributes uniformly, and `AUTO` chooses.
- Choose a distribution key with high cardinality and even spread, or data skew leaves one node doing most of the work.
- Sort keys determine physical order and drive zone map block skipping.
- Choose the most commonly filtered column, usually a date, as the leading sort key.
- Compound sort keys favour the leading column; interleaved keys cost more maintenance and are rarely worth it.
- Distribution serves joins and sorting serves filters — they are separate decisions.
- Run `ANALYZE` to keep statistics current, since stale statistics silently degrade query plans.
