# Query vs Scan & Secondary Indexes

Two ways to read many items, one of which is almost always wrong — and the index types that make the right one possible for more access patterns.

---

## Query vs Scan

**`Query`** reads items with a specific partition key, optionally narrowed by a sort key condition. DynamoDB goes directly to one partition and reads a contiguous run.

**`Scan`** reads **every item in the table**, then applies any filter. It touches all partitions and reads everything.

The difference is not a matter of degree:

| | Query | Scan |
|---|---|---|
| Reads | One partition | Every partition |
| Capacity consumed | Proportional to matched items | Proportional to **table size** |
| Latency | Consistent | Grows with the table |
| Cost | Predictable | Grows with the table |

---

## Why Filters Do Not Help

The most consequential misunderstanding in DynamoDB: **`FilterExpression` is applied after reading.**

```python
# Reads and pays for EVERY item, then discards non-matching ones
response = table.scan(FilterExpression=Attr("Status").eq("pending"))
```

*A scan over a 10 GB table consumes capacity for 10 GB even if three items match. The filter reduces what is returned, not what is read or billed.*

This is why a scan that seems fine in development becomes a problem in production. With a thousand items it is instant; with ten million it consumes the table's entire throughput and throttles everything else.

`Query` also accepts a `FilterExpression`, with the same caveat — the key condition determines what is read, and the filter only removes items from the response afterwards.

---

## When a Scan Is Acceptable

Scans are not forbidden, only usually wrong:

- **Small tables** — a configuration or lookup table of a few hundred items.
- **One-off migrations or backfills**, ideally with a rate limit.
- **Genuine full-table processing**, using parallel scan segments.
- **Export to analytics**, though S3 export is generally better.

If a scan runs in the request path of a user-facing operation, it is a design problem.

---

## Global Secondary Indexes

A **GSI** provides an alternative partition key and sort key for the same data:

```
Table:  PK = CustomerId,  SK = OrderId
GSI:    PK = Status,      SK = OrderDate
```

*The GSI enables querying by status and ordering by date — an access pattern the base table cannot serve.*

Properties:

- **Different partition and sort key** from the base table.
- **Can be added or removed after creation**, unlike the primary key. This is the main escape hatch when an access pattern was not anticipated.
- **Eventually consistent only.** A GSI cannot be read with strong consistency.
- **Has its own capacity**, separate from the table's. A GSI with insufficient capacity throttles **writes to the base table**, which is a surprising failure mode.
- **Up to 20 per table** by default.

**Sparse indexes** are a valuable technique. An item only appears in a GSI if it has the index's key attributes:

```python
# Only unprocessed orders carry this attribute, so only they appear in the index
table.update_item(
    Key={"CustomerId": "C-1001", "OrderId": "O-5503"},
    UpdateExpression="SET UnprocessedFlag = :f",
    ExpressionAttributeValues={":f": "Y"},
)
```

*A GSI on `UnprocessedFlag` contains only unprocessed orders. Removing the attribute when processed removes the item from the index — so a query returns exactly the pending work, with no scan and no filter.*

This turns "find all items in state X" — normally a scan — into a small, efficient query.

---

## Local Secondary Indexes

An **LSI** shares the table's partition key with a different sort key:

- **Must be created with the table** and cannot be added later.
- **Supports strongly consistent reads**, unlike a GSI.
- **Shares the table's capacity.**
- **Imposes a 10 GB limit per partition key value** across the table and all its LSIs.

That last constraint is severe and easy to hit unnoticed. Combined with the inability to add one later, LSIs are rarely the right choice — **prefer GSIs** unless strong consistency on an alternative sort key is genuinely required.

---

## Projections

Each index projects a subset of attributes:

- **`KEYS_ONLY`** — keys only; smallest and cheapest
- **`INCLUDE`** — keys plus named attributes
- **`ALL`** — every attribute; largest storage and write cost

Projection matters because **every write to the table also writes to each index projecting the changed attributes.** An index with `ALL` roughly doubles write cost for that table.

The efficient pattern is `KEYS_ONLY` or `INCLUDE` with the attributes the query actually needs. Fetching missing attributes afterwards requires a second read, so the trade is index write cost against read round trips.

---

## Key Takeaways

- `Query` reads one partition; `Scan` reads the entire table and consumes capacity proportional to table size.
- `FilterExpression` is applied after reading, so it reduces returned items but not capacity consumed or cost.
- Scans are acceptable for small tables, migrations, and full-table processing — not in a user-facing request path.
- GSIs provide alternative keys, can be added after creation, and are eventually consistent only.
- An under-provisioned GSI throttles writes to the base table.
- Sparse indexes contain only items carrying the index key, turning state queries into efficient lookups.
- LSIs must be created with the table and impose a 10 GB per-partition-key limit — prefer GSIs.
- Index projections drive write cost; use `KEYS_ONLY` or `INCLUDE` rather than `ALL` by default.
