# Tables, Items & Attributes

**DynamoDB** is a managed key-value and document database. Its data model is small, and the differences from a relational database shape everything about how it is used.

---

## The Model

A **table** holds **items**. An item is a collection of **attributes**. An attribute is a name-value pair.

```
Table: Orders

Item 1: { "CustomerId": "C-1001", "OrderId": "O-5501",
          "Total": 249.99, "Status": "shipped",
          "Items": [{"sku": "A-12", "qty": 2}] }

Item 2: { "CustomerId": "C-1001", "OrderId": "O-5502",
          "Total": 89.50, "Status": "pending" }
```

*Both items are in the same table with different attributes — `Items` exists on one and not the other. This is legal and normal.*

The properties that distinguish it from a relational table:

**There is no schema beyond the key.** Only the primary key attributes are required and typed at table creation. Every other attribute is per item.

**Items in one table need not resemble each other.** A table can hold orders, customers, and products with entirely different attribute sets.

**There are no joins.** Data from two entities is combined by the application, or by storing it together in the first place.

**There are no foreign keys, no referential integrity, and no cascading deletes.**

---

## Attribute Types

| Type | Notation | Notes |
|---|---|---|
| String | `S` | UTF-8, cannot be empty in a key |
| Number | `N` | Sent as a string, 38 digits of precision |
| Binary | `B` | Base64-encoded in transit |
| Boolean | `BOOL` | |
| Null | `NULL` | |
| List | `L` | Ordered, mixed types |
| Map | `M` | Nested key-value, like a JSON object |
| String Set | `SS` | Unique values, unordered |
| Number Set | `NS` | |
| Binary Set | `BS` | |

Lists and maps nest arbitrarily, which is what makes DynamoDB a document store as well as a key-value store. A whole JSON document can be one item.

Numbers are transmitted as strings to preserve precision across languages. SDKs handle the conversion, and it is visible in raw API responses.

---

## The Primary Key

Every table has a primary key, declared at creation and unchangeable. Two forms:

**Simple primary key** — a **partition key** alone. It must be unique across the table.

**Composite primary key** — a **partition key** plus a **sort key**. The combination must be unique, so many items can share a partition key with different sort keys.

The composite form is what makes DynamoDB useful beyond simple key-value lookup, and it is covered in the next note.

---

## Operations

The API is small and deliberate:

| Operation | Effect |
|---|---|
| `PutItem` | Write an item, replacing any existing one with that key |
| `GetItem` | Read one item by full primary key |
| `UpdateItem` | Modify specific attributes of an item |
| `DeleteItem` | Remove an item |
| `Query` | Read items sharing a partition key |
| `Scan` | Read every item in the table |
| `BatchGetItem` / `BatchWriteItem` | Up to 100 reads / 25 writes per call |
| `TransactWriteItems` | Up to 100 writes, all-or-nothing |

```python
table.put_item(Item={
    "CustomerId": "C-1001",
    "OrderId": "O-5501",
    "Total": Decimal("249.99"),
    "Status": "pending",
})

response = table.get_item(Key={"CustomerId": "C-1001", "OrderId": "O-5501"})
item = response.get("Item")
```

*`GetItem` requires the complete primary key. There is no way to fetch by a non-key attribute without a `Query` on an index or a `Scan`.*

**Conditional writes** are the mechanism for correctness under concurrency:

```python
table.put_item(
    Item={"CustomerId": "C-1001", "OrderId": "O-5501", "Total": Decimal("249.99")},
    ConditionExpression="attribute_not_exists(OrderId)",
)
```

*Writes only if no item with that key exists, failing otherwise. This is how uniqueness and optimistic locking are enforced — DynamoDB has no transactions in the relational sense but does have atomic conditional writes.*

---

## What This Means in Practice

The single most important consequence: **`GetItem` requires the full primary key.** There is no "find items where status is pending" without either an index or scanning the entire table.

In a relational database, an unanticipated query is a new `WHERE` clause. In DynamoDB it may require a new index, or a change to how data is stored. This is why access patterns are designed before the table — the subject of the next two notes.

---

## Key Takeaways

- A DynamoDB table holds items made of attributes, with no schema beyond the primary key.
- Items in one table can have entirely different attributes, and there are no joins, foreign keys, or referential integrity.
- Attribute types include scalars, lists, maps, and sets, with lists and maps nesting arbitrarily.
- The primary key is either a partition key alone or a partition key plus sort key, fixed at creation.
- `GetItem` requires the complete primary key; there is no lookup by non-key attributes without an index or a scan.
- Conditional writes enforce uniqueness and optimistic locking atomically.
- Adding an unanticipated query may require a new index or a data model change, unlike a relational `WHERE` clause.
