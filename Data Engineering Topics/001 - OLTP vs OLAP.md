# OLTP vs. OLAP

Every database workload leans one of two ways: keeping an application running moment to moment, or making sense of everything that's happened over time. That split has a name — **OLTP** and **OLAP** — and it's the foundation for a lot of data engineering decisions, including which storage technology to reach for.

## OLTP: Online Transaction Processing

**OLTP** systems handle the day-to-day operation of an application: creating an order, updating a profile, checking a single account balance. The defining traits:

- Many small, fast operations, happening constantly
- Each operation typically touches one record or a small, related set of records
- Reads and writes are roughly balanced — the system is constantly changing
- Data is usually normalized to avoid redundancy and keep writes consistent

This is the world application developers live in most of the time. The relational database (or NoSQL store) sitting behind a typical web app is an OLTP system.

## OLAP: Online Analytical Processing

**OLAP** systems answer a different kind of question: not "what's true about this one record right now," but "what patterns exist across everything that's happened." Think: "total revenue by region, by month, for the last three years."

- Queries scan and aggregate huge volumes of historical data
- Reads dominate — data is loaded in batches or streams, then queried heavily
- Data is often denormalized (see dimensional modeling) to make those big aggregate queries fast and simple
- Optimized for a handful of wide, complex queries rather than millions of tiny ones

## Why the Distinction Matters

OLTP and OLAP want fundamentally different things from their underlying storage. A system tuned to make single-record lookups and updates fast (OLTP) is not the same system that's good at scanning billions of rows to compute an aggregate (OLAP) — and trying to do both well with one system usually means doing neither well.

This is why analytical workloads typically get pulled out into a separate system — a **data warehouse** — rather than running heavy reporting queries against the same database an application depends on for live traffic. Running both on one system risks analytical queries slowing down (or locking up) the operational workload real users are waiting on.

## The Common Pattern

```
Application  →  OLTP database        (operational data: live orders, user accounts)
                      │
                      ▼  ETL / streaming
                 OLAP data warehouse  (historical data: analytics, reporting, dashboards)
```
*A typical architecture: the operational store handles live application traffic; the warehouse handles analysis of the accumulated history, kept in sync via ETL.*

It's normal — expected, even — for a real system to have both: an OLTP store running the application, and a separate OLAP store fed by that data for reporting and business intelligence. Recognizing which kind of question you're being asked to answer is the first step in knowing which one you need.
