# Data Warehousing

A **data warehouse** is a system purpose-built to store and query large volumes of historical data for analysis — the OLAP side of the [OLTP vs. OLAP](001%20-%20OLTP%20vs%20OLAP.md) split. It's a distinct concept from the operational database an application runs on, even though both are commonly queried with SQL.

## What Makes It Different From an Operational Database

An operational (OLTP) database is optimized to read and write individual records quickly, over and over, all day. A data warehouse is optimized for the opposite pattern: relatively infrequent but very heavy queries that scan and aggregate across millions or billions of rows at once.

That difference in access pattern drives real architectural differences:

- **Storage layout** — warehouses are frequently **columnar** (values from a single column stored together) rather than row-oriented, since analytical queries usually touch a handful of columns across huge numbers of rows. Reading only the columns you need, and skipping the rest, is a big part of why warehouses are fast at this kind of query.
- **Schema design** — warehouse data is often intentionally denormalized (see dimensional modeling) to minimize joins for the kinds of aggregate queries analysts run.
- **Workload isolation** — keeping analytics off the operational database means heavy reporting queries never compete with, or slow down, the live application.

## Where the Data Comes From

A warehouse is almost never the *original* home of data. It's a destination — data is extracted from operational systems (application databases, event streams, third-party sources) and loaded in, usually on a schedule or continuously.

That movement is the **ETL** (Extract, Transform, Load) pattern: pull raw data out of its source, reshape it into a form suited for analysis, and load it into the warehouse. Its cousin, **ELT** (Extract, Load, Transform), loads the raw data first and lets the warehouse itself do the transforming — a common approach with modern cloud warehouses that have plenty of compute to spare.

## Data Warehouse vs. Data Lake

You'll also hear the term **data lake** — worth distinguishing:

| | Data Warehouse | Data Lake |
|---|---|---|
| Data shape | Structured, schema defined up front | Any format — structured, semi-structured, raw files |
| Best for | SQL analytics, BI, reporting | Large-scale storage, ML/data science, flexible reprocessing |
| Schema | Schema-on-write | Schema-on-read |

Many organizations use both — a lake for cheap, flexible raw storage, and a warehouse (or a "lakehouse" that blends the two) for structured analytical querying on top of it.

## Why It Matters for Application Developers

Even without building or querying a warehouse directly, it's worth recognizing the shape of the problem: if a request is "give me a fast answer about one thing," that's operational, and belongs on the database backing the app. If it's "help me understand a pattern across a huge amount of history," that's analytical, and belongs in a warehouse — pulling that kind of query onto the production database is a common and avoidable source of performance trouble.
