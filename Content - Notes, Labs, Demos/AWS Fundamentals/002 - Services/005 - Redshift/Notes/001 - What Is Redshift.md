# What Is Redshift

**Redshift** is AWS's managed **data warehouse** service. It's built to answer a different kind of question than RDS or DynamoDB are — not "fetch this specific record," but "analyze patterns across enormous amounts of historical data." Understanding *that* distinction is more important than any specific Redshift feature, because it explains why the service exists at all.

## OLTP vs OLAP

This is the conceptual split that Redshift sits on one side of. Databases are broadly categorized by the kind of workload they're optimized for:

- **OLTP (Online Transaction Processing)** — optimized for many small, fast, individual read/write operations: creating an order, updating a user's profile, checking a single account balance. RDS and DynamoDB are both, fundamentally, OLTP-oriented tools, even though they take very different approaches to it.
- **OLAP (Online Analytical Processing)** — optimized for complex queries that scan and aggregate huge volumes of data at once: "what was total revenue by region, by month, for the last three years?" This is Redshift's territory.

These two workload shapes want genuinely different underlying architectures, which is why a dedicated service like Redshift exists rather than everyone just running analytics queries against their production RDS database.

## Columnar Storage: The Key Architectural Difference

The single biggest reason Redshift performs well at analytical queries is **columnar storage**. A traditional relational database (like RDS) stores data **row by row** — all of a single record's fields sit together on disk, which is efficient when we want to fetch or update one whole record at a time (an OLTP pattern).

Redshift instead stores data **column by column** — all the values for a single field, across every row, sit together. This turns out to be extremely efficient for analytical queries, because those queries typically only care about a handful of columns out of a table that might have dozens, and they're aggregating across *many* rows for those columns (a `SUM` of a revenue column across millions of rows, for instance). Columnar storage means Redshift can read just the columns a query actually needs, skip everything else, and compress similar values sitting next to each other very effectively — all of which would offer little benefit under a row-oriented layout.

## Why Not Just Use RDS for Analytics Too

It's a fair question — RDS supports SQL and can technically run analytical queries. The problem is that RDS's row-oriented storage and general-purpose design aren't optimized for scanning millions or billions of rows to compute an aggregate, and running heavy analytical queries against a production RDS database can degrade performance for the transactional workload that database is actually meant to serve. Redshift exists specifically to take that analytical load off to a separate system, purpose-built for it, so the two workloads don't compete with (or slow down) each other.

## Where the Data Comes From

Redshift is rarely the *original* home of data — it's typically a destination that data is loaded (or continuously streamed) into from elsewhere: application databases, S3, or external data sources. This is a natural tie-back to the ETL concept — extracting data from operational systems, transforming it into a shape suited for analysis, and loading it into Redshift, where it can be queried efficiently for reporting and analytics without touching the operational systems it came from.

With the "what and why" established, the next topic places Redshift alongside RDS and DynamoDB to make the decision of which one fits a given need more concrete.
