# Partition Keys & Sort Keys

The primary key does two jobs: it identifies items, and it determines **where they are physically stored**. The second job is what makes key design the most consequential decision in DynamoDB.

---

## How Data Is Distributed

DynamoDB stores data across many **partitions** — storage and compute units spread across servers. The **partition key** decides which partition an item lives on:

```
hash(partition key) ──► partition
```

*The hash is what distributes data. Items with the same partition key always land on the same partition; different keys distribute across many.*

This mechanism is what provides DynamoDB's scalability, and it produces the constraints that follow.

---

## The Sort Key

With a composite key, items sharing a partition key are stored **together on one partition, sorted by sort key**:

```
Partition key: CUSTOMER#C-1001
├── Sort key: ORDER#2026-01-15#O-5501
├── Sort key: ORDER#2026-03-22#O-5502
└── Sort key: ORDER#2026-09-09#O-5503
```

*Physically adjacent and sorted, which is what makes range queries efficient — reading a contiguous run rather than seeking.*

This enables:

- **Range queries** — orders between two dates
- **Prefix queries** — `begins_with(sk, "ORDER#2026-")`
- **Ordered retrieval** — most recent first, using `ScanIndexForward=false`
- **Limit-based retrieval** — the ten most recent, without reading more

```python
response = table.query(
    KeyConditionExpression=Key("CustomerId").eq("CUSTOMER#C-1001")
                          & Key("OrderId").begins_with("ORDER#2026-"),
    ScanIndexForward=False,
    Limit=10,
)
```

*Reads the ten most recent 2026 orders for one customer as a single contiguous read. The partition key is always an exact match; only the sort key supports ranges.*

---

## Hot Partitions

Because the partition key determines placement, an uneven key distribution concentrates traffic:

```
Partition key = "2026-09-09"   →  every write today hits ONE partition
Partition key = "STATUS#active" →  every active item on ONE partition
```

*Low-cardinality keys collapse the distribution DynamoDB depends on.*

A partition has throughput limits — roughly 3,000 read units and 1,000 write units per second. Concentrating traffic on one produces throttling **while the table as a whole is far below its provisioned capacity**, which is a confusing symptom.

Adaptive capacity mitigates this by shifting capacity toward busy partitions, and it does not overcome a fundamentally bad key design.

**A good partition key has high cardinality and even access.** User ID, order ID, device ID, tenant ID. **A poor one has few distinct values or concentrated traffic:** status, date, country, boolean flags.

Where a naturally low-cardinality key is unavoidable, **write sharding** adds a suffix to spread it:

```
STATUS#active#01 ... STATUS#active#10
```

*Ten partitions instead of one; reads query all ten shards and merge. It adds complexity and is the standard remedy when the key cannot be changed.*

---

## Composite Sort Keys

A widely used technique: encode a hierarchy into the sort key with a delimiter.

```
PK: CUSTOMER#C-1001
SK: ORDER#2026-09-09#O-5503
SK: ADDRESS#billing
SK: ADDRESS#shipping
SK: PROFILE
```

*One partition holds a customer's profile, addresses, and orders. `begins_with(sk, "ORDER#")` retrieves orders; `begins_with(sk, "ADDRESS#")` retrieves addresses; a query with no sort key condition retrieves everything about the customer in one read.*

This is the basis of **single-table design** — storing several entity types in one table, arranged so that a single query retrieves everything an access pattern needs. It is efficient and considerably less readable than a relational schema, which is a genuine trade rather than a free win.

Two conventions make it workable: prefix sort key values with the entity type, and use zero-padded or ISO-8601 values so lexicographic sorting matches the intended order. `2026-09-09` sorts correctly as a string; `9/9/2026` does not.

---

## The Key Is Permanent

**The primary key cannot be changed after table creation.** Changing it means creating a new table and migrating every item.

That permanence, combined with the fact that the key determines which queries are possible, is why DynamoDB modelling starts with access patterns rather than with entities.

---

## Key Takeaways

- The partition key is hashed to select a physical partition; the sort key orders items within it.
- Items sharing a partition key are stored together and sorted, which makes range and prefix queries efficient.
- Queries match the partition key exactly and support ranges only on the sort key.
- Low-cardinality partition keys create hot partitions that throttle while the table is far below capacity.
- Good partition keys have high cardinality and even access; write sharding is the workaround when they do not.
- Composite sort keys with type prefixes allow one partition to hold several related entity types.
- Use zero-padded or ISO-8601 values so lexicographic sorting matches intended ordering.
- The primary key cannot be changed after creation — changing it means migrating to a new table.
