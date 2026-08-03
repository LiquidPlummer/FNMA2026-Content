# Where It Fits vs RDS

With both RDS and DynamoDB now covered, the natural next question is: when do we reach for which one? The two represent genuinely different approaches to storing data, and the right choice depends on the shape of the workload — not on one being categorically "better."

## The Fundamental Difference

RDS gives us a **relational** database: data organized into tables with a fixed schema, related to each other through foreign keys, queried with SQL, and capable of complex operations like joins across multiple tables. DynamoDB gives us a **NoSQL, key-based** database: schema-flexible items, retrieved primarily by their primary key, with no native concept of joining data across tables.

That difference in querying model is really the crux of the decision. Relational databases are built around flexible, ad hoc querying — we can ask new, unanticipated questions of the data using SQL, joining tables in ways we didn't necessarily plan for upfront. DynamoDB is built around fast, predictable access by key — it excels when we already know the access patterns in advance and design the table around them, but it's a poor fit for flexible, unanticipated querying after the fact.

## When RDS Fits Better

- **Complex relationships between data** — orders that belong to customers, which have addresses, which relate to shipping records, all queried together — are naturally expressed relationally and awkward to model in a key-based store.
- **Ad hoc or evolving query needs** — reporting, analytics, or an application whose querying needs aren't fully known upfront benefit from SQL's flexibility.
- **Strong consistency and transactional guarantees across multiple related records** are a native, well-understood strength of relational databases.

## When DynamoDB Fits Better

- **Very high, unpredictable scale**, especially with simple, well-known access patterns (like "look up a user's profile by user ID") — DynamoDB's automatic partitioning handles this kind of load without the capacity planning a relational database would need.
- **Latency-sensitive applications** needing very fast, consistent response times at scale — things like a shopping cart, a session store, or a leaderboard.
- **Schema that varies or evolves per item**, where forcing a single rigid table structure would be awkward.
- **Workloads that don't need cross-table joins** — if the data model naturally decomposes into independent lookups by key, DynamoDB's simplicity becomes an advantage rather than a limitation.

## A Simple Litmus Test

A useful, if oversimplified, way to frame the choice: **if we can describe the application's data access needs as "given this ID, fetch that record" over and over, DynamoDB is a strong fit. If we regularly need to ask questions that span multiple related tables in ways we can't fully predict ahead of time, RDS is the better starting point.**

| | RDS | DynamoDB |
|---|---|---|
| Data model | Relational (tables, foreign keys) | Key-based (items, partition/sort key) |
| Schema | Fixed, defined upfront | Flexible per item |
| Querying | Flexible SQL, joins across tables | Fast lookups by key; joins not native |
| Scaling | Primarily vertical, with some read scaling | Horizontal, largely automatic |
| Best fit | Complex relationships, evolving queries | High-scale, predictable access patterns |

## Not Mutually Exclusive

It's worth closing on this: real architectures often use *both*, for different parts of the same system — RDS for the core relational data with complex relationships, and DynamoDB for a specific high-throughput, simple-access-pattern piece like session storage or an activity feed. The decision isn't "which database do we use for the whole application," but "which store fits this particular piece of data and how it's accessed."
