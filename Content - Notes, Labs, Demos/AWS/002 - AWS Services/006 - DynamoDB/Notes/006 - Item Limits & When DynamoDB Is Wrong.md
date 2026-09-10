# Item Limits & When DynamoDB Is Wrong

Hard limits that shape the data model, and an honest account of when a different service is the better answer.

---

## The 400 KB Item Limit

**An item cannot exceed 400 KB**, including attribute names, values, and all nested structure. This is a hard limit with no adjustment.

It rules out storing documents, images, or large payloads as attributes, and it constrains a pattern that otherwise looks natural: an item that accumulates a growing list.

An order item holding a list of line items works until an order has too many. A user item holding a list of activity works until the user is active. Both fail at an unpredictable point, in production, after working for a long time.

**The remedy is to split growth into items rather than into attributes:**

```
PK: ORDER#O-5503   SK: METADATA        { total, status, customer }
PK: ORDER#O-5503   SK: ITEM#001        { sku, qty, price }
PK: ORDER#O-5503   SK: ITEM#002        { sku, qty, price }
```

*Each line item is its own item, so the order grows without bound. One query on the partition key retrieves all of it, and there is no size ceiling.*

For genuinely large payloads, store the object in S3 and keep a reference in DynamoDB. That is the standard pattern for anything above a few kilobytes.

---

## Other Limits

| Limit | Value |
|---|---|
| Item size | 400 KB |
| Partition key value | 2,048 bytes |
| Sort key value | 1,024 bytes |
| GSIs per table | 20 (adjustable) |
| LSIs per table | 5, fixed at creation |
| `BatchGetItem` | 100 items / 16 MB |
| `BatchWriteItem` | 25 items / 16 MB |
| `TransactWriteItems` | 100 items |
| Query/Scan response | 1 MB per call |
| LSI partition size | 10 GB per partition key value |

Two deserve emphasis.

**The 1 MB response limit** means results are paginated. A query returning more than 1 MB returns a `LastEvaluatedKey`, and code ignoring it silently processes only the first page. This is one of the most common DynamoDB bugs, and it appears only once data grows.

```python
items, kwargs = [], {"KeyConditionExpression": Key("CustomerId").eq("C-1001")}
while True:
    response = table.query(**kwargs)
    items.extend(response["Items"])
    if "LastEvaluatedKey" not in response:
        break
    kwargs["ExclusiveStartKey"] = response["LastEvaluatedKey"]
```

*Paginating until exhausted. Omitting this loop returns partial results with no error.*

**The 100-item transaction limit** bounds what can be made atomic. DynamoDB transactions are real ACID transactions, and they are limited in scope compared with a relational database.

---

## When DynamoDB Is the Wrong Choice

**Ad hoc and analytical queries.** No `GROUP BY`, no `SUM`, no `JOIN`. Aggregations must be maintained incrementally or computed elsewhere. A reporting workload belongs in Redshift, Athena over S3, or a relational database.

**Access patterns that change frequently.** An application still discovering its query needs will keep hitting patterns the key does not support. A relational database absorbs this far better.

**Complex relationships.** Many-to-many with traversal in several directions is achievable but awkward. A graph or relational database expresses it directly.

**Large-scale transactions.** Beyond 100 items, or where a long multi-statement transaction is needed.

**Full-text search.** DynamoDB has none. OpenSearch does.

**Small datasets where flexibility matters more than scale.** A few thousand rows with varied query needs is a relational workload. DynamoDB's advantages appear at scale, and its constraints apply at any size.

---

## When It Is the Right Choice

**Known access patterns at high volume.** Session stores, user profiles, shopping carts, device state, event records — all with a clear key and predictable queries.

**Serverless applications.** No connection pool, no VPC requirement, no instance to size. It fits Lambda in a way RDS does not without a proxy.

**Unpredictable or spiky traffic**, using on-demand capacity.

**Very large scale**, where a single relational writer would be the bottleneck.

**Simple key-value lookup with strict latency requirements.** Single-digit millisecond reads, consistently, at any table size.

---

## The Honest Summary

DynamoDB trades query flexibility for predictable performance at scale. That is a good trade when access patterns are known and volume is high, and a poor one when queries are exploratory or volume is modest.

The most expensive mistake is choosing it for a workload that later needs relational queries — the result is scans everywhere, unpredictable cost, and a model that resists every new requirement. Choosing a relational database for a workload that later needs DynamoDB's scale is a migration; choosing DynamoDB for a relational workload is a rewrite.

---

## Key Takeaways

- Items cannot exceed 400 KB, which rules out growing lists inside a single item.
- Split growth across items using the sort key rather than into a list attribute, and keep large payloads in S3.
- Query and scan responses cap at 1 MB and paginate — code that ignores `LastEvaluatedKey` silently returns partial results.
- Transactions are limited to 100 items, which bounds what can be made atomic.
- DynamoDB is wrong for ad hoc queries, aggregations, full-text search, complex relationships, and evolving access patterns.
- It is right for known access patterns at high volume, serverless applications, spiky traffic, and strict latency requirements.
- The trade is query flexibility for predictable performance at scale, and it is a poor trade at modest volume.
