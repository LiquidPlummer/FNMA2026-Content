# What Is DynamoDB

**DynamoDB** is AWS's fully managed **NoSQL** database service. As noted in the course outline, we won't cover general NoSQL theory here — that's broader material that applies beyond AWS — but we do need a working understanding of DynamoDB itself, since it's a distinct, AWS-specific service with its own concepts and strong opinions about how data should be modeled.

## The Core Building Blocks

DynamoDB organizes data into **tables**, similar to a relational database — but the similarity mostly ends there.

- **Items** are DynamoDB's rows — each item is a single record in a table.
- **Attributes** are an item's fields — similar to columns, but DynamoDB doesn't enforce a fixed schema across items. Two items in the same table can have completely different sets of attributes, aside from the key.
- **Primary key** — every table requires one, and it's how DynamoDB locates items efficiently. It's either a simple **partition key** alone, or a **composite key** made of a partition key plus a **sort key**. The partition key determines which physical partition (a segment of DynamoDB's underlying storage) an item lives on; the optional sort key lets multiple items share a partition key while remaining individually addressable and sorted.

## Fully Managed, and What That Means Here

DynamoDB is managed the way RDS is managed, but arguably even more so — there's no underlying server to think about at all, not even conceptually. We don't choose an instance type, we don't patch anything, and (in its on-demand capacity mode) we don't even pre-provision throughput — DynamoDB scales automatically to handle the traffic it receives. This is a meaningfully different operational posture from RDS, where we're still, in a real sense, managing a database instance that happens to be automated. With DynamoDB, the table is closer to a pure API than a server we're indirectly responsible for.

## Schema Flexibility

Unlike a relational database, DynamoDB doesn't require us to define columns and their types upfront for the whole table — only the primary key's attributes are fixed at table-creation time. Every other attribute is defined per item, as data is written. This gives a lot of flexibility to evolve what data an item holds over time without a formal schema migration, but it also shifts responsibility onto the application: since DynamoDB won't enforce a consistent shape, the application has to be disciplined about what it actually writes.

## Built for Horizontal Scale

DynamoDB's underlying design is horizontal scaling taken to its logical extreme — data is automatically spread (partitioned) across many physical nodes based on the partition key, and DynamoDB adds more partitions behind the scenes as a table grows. This is precisely why the choice of partition key matters so much: a well-chosen partition key spreads traffic evenly across partitions, while a poorly chosen one can concentrate traffic on a single partition and create a bottleneck, no matter how much capacity the table nominally has. We won't go deep into key-design strategy in this introductory pass, but it's worth knowing that "how do I pick a good partition key" is one of the first real design questions anyone doing serious DynamoDB work runs into.

## Where This Leaves Us

DynamoDB trades away the flexible querying and strong consistency guarantees of a relational database in exchange for near-limitless, low-latency horizontal scale and freedom from schema rigidity. Whether that trade makes sense for a given workload — and specifically, how it stacks up against RDS — is exactly what the next topic works through.
