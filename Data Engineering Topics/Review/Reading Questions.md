# Data Engineering Topics — Reading Questions

Use these questions while you read the notes. Each one points at something worth understanding well enough to talk about on the job.

## OLTP vs. OLAP

1. What's the fundamental difference between the kind of question an OLTP system answers and the kind an OLAP system answers?
2. Why do OLTP and OLAP workloads typically end up on separate systems rather than sharing one database?
3. In the common OLTP-to-OLAP architecture, what role does ETL play in keeping the two in sync?

## Data Warehousing

4. How does a data warehouse's storage layout typically differ from an operational database's, and why does that difference help analytical queries?
5. What's the difference between ETL and ELT, and why might a modern cloud warehouse favor ELT?
6. How does a data warehouse differ from a data lake, and when might an organization want both?
7. Why is it a problem to run heavy analytical queries directly against a production application's database instead of a warehouse?

## Dimensional Modeling

8. What's the difference between a fact table and a dimension table, and what does a typical query against a dimensional model look like?
9. Why does dimensional modeling deliberately denormalize data, when OLTP schema design goes out of its way to normalize?
10. What's the tradeoff between a star schema and a snowflake schema, and which is the more common default?

## Materialized Views

11. What's the core difference between a regular view and a materialized view?
12. What tradeoff are you accepting when you choose a materialized view over a plain view or a live query?
13. What does it mean for a materialized view to be "stale," and what are the ways it might get refreshed?
