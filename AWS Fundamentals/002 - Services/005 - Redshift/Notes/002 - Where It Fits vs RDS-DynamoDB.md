# Where It Fits vs RDS/DynamoDB

We've now covered three very different AWS database services. Placing them side by side makes the decision of which to reach for much more concrete than considering any one of them in isolation.

## The Three at a Glance

| | RDS | DynamoDB | Redshift |
|---|---|---|---|
| **Workload type** | OLTP (transactional) | OLTP (transactional) | OLAP (analytical) |
| **Data model** | Relational, fixed schema | Key-based, flexible schema | Relational, but column-oriented storage |
| **Optimized for** | Complex relationships, moderate scale | High-scale, simple key-based access | Scanning and aggregating huge datasets |
| **Typical query** | "Get this customer's open orders" | "Get this user's profile by ID" | "Total revenue by region, last 3 years" |
| **Storage layout** | Row-oriented | Item-based, partitioned by key | Column-oriented |

## The Underlying Pattern

RDS and DynamoDB, despite their very different data models, are both fundamentally answering the same *kind* of question: fast, targeted operations on individual records or small sets of related records, as part of an application's live, everyday operation. The choice between them (covered in the DynamoDB lesson) comes down to how relational the data is and how much flexibility versus scale is needed.

Redshift is answering a different kind of question entirely: not "what is true about this one record right now," but "what patterns exist across a huge volume of records, accumulated over time." It's not really in competition with RDS or DynamoDB for the same job — it's solving a problem neither of them is built for.

## A Common Real-World Architecture

Because these services solve different problems, it's completely normal — expected, even — for a single system to use more than one of them together:

```
Application  →  RDS or DynamoDB     (operational data: live orders, user accounts)
                      │
                      ▼  (ETL: extract, transform, load)
                 Redshift            (historical data: analytics, reporting, dashboards)
```
*A typical pattern: transactional stores handle live application traffic; Redshift handles analysis of the accumulated history.*

The operational database (RDS or DynamoDB) handles the live application — fast, individual reads and writes that keep the product working moment to moment. Periodically (or continuously), that data is extracted, transformed, and loaded into Redshift, where analysts and reporting tools can run heavy, complex queries across years of accumulated history without putting any load on the system actually running the application.

## The Decision Framework, Extended

Building on the litmus test from the DynamoDB topic, we can extend it one step further:

- **Need fast, simple lookups by key, at high scale?** → DynamoDB.
- **Need complex relationships and flexible querying, at moderate scale, for live application data?** → RDS.
- **Need to analyze large volumes of historical data for reporting or business intelligence?** → Redshift.

These aren't mutually exclusive choices made once for an entire system — they're choices made per workload, based on what that specific piece of data needs to do. Recognizing which category a new requirement falls into is one of the most practically useful skills to take away from this module.
