# Concurrency, WLM & Cost Control

Redshift is one of the more expensive AWS services to run carelessly. Managing concurrent queries and controlling idle spend are the two things that keep it reasonable.

---

## Concurrency Is Limited

Redshift is built for a modest number of large queries, not many small ones. A cluster runs a limited number of queries simultaneously — typically 5 to 50 depending on configuration — and the rest queue.

This is a design consequence, not a defect. Each query is expected to use substantial memory and parallelism across all slices. Running hundreds concurrently would leave each with too little memory to complete without spilling to disk.

The practical implication: **Redshift is not a backend for a user-facing application.** A dashboard refreshing for two hundred users generates two hundred queries, most of which queue. That workload belongs behind a cache, a materialized view, or a different data store.

---

## Workload Management

**WLM** controls how concurrency and memory are allocated across query queues.

**Automatic WLM** (the recommended default) lets Redshift manage concurrency and memory dynamically, assigning queries to priority levels:

```sql
CREATE USER dashboard_user WITH PASSWORD '...';
ALTER USER dashboard_user SET query_group TO 'dashboards';
```

*Query groups route queries to WLM queues, so a short dashboard query is not stuck behind a long ETL job.*

**Manual WLM** defines queues with fixed concurrency and memory percentages. It gives explicit control and requires tuning, and it is rarely better than automatic.

**Query priority** is the useful lever under automatic WLM. Assigning `HIGHEST` to interactive queries and `LOW` to background ETL means a long-running load does not block someone waiting on a report.

### Query monitoring rules

WLM rules can act on queries that misbehave:

```
IF query_execution_time > 3600 THEN abort
IF nested_loop_join_row_count > 1000000 THEN log
IF query_queue_time > 600 THEN change_query_priority
```

*Aborting a runaway query protects the cluster. A single unbounded cross join can otherwise consume it for hours.*

---

## Concurrency Scaling

When queries queue, **concurrency scaling** adds transient clusters automatically and routes queued queries to them. Credits accrue that cover a period of use per day, with charges beyond that.

It suits burst patterns — a morning dashboard refresh, an end-of-month reporting window — rather than sustained high concurrency, which needs a larger cluster or a different architecture.

---

## Result Caching

Redshift caches query results. An identical query against unchanged data returns from cache in milliseconds, using no compute.

This is free and automatic, and it makes repeated dashboard queries much cheaper than they appear — provided the queries are genuinely identical. A query with a `now()` timestamp or a changing parameter never hits the cache, which is worth knowing when a dashboard is slower than expected.

---

## Materialized Views

A **materialized view** stores the result of a query and can refresh incrementally:

```sql
CREATE MATERIALIZED VIEW daily_sales AS
SELECT order_date, category, SUM(total) AS revenue, COUNT(*) AS orders
FROM orders JOIN products USING (product_id)
GROUP BY order_date, category;

REFRESH MATERIALIZED VIEW daily_sales;
```

*Precomputes an expensive aggregation. Dashboard queries read the view instead of scanning the fact table.*

With auto-refresh enabled, Redshift maintains them in the background. This is the standard answer to a dashboard workload that would otherwise generate many expensive repeated queries — it converts them into cheap reads of a small table.

---

## Controlling Cost

Redshift bills by the hour for provisioned clusters, whether queried or not. The levers, in order of impact:

**Pause when unused.** A cluster used during business hours costs roughly a third as much if paused overnight and at weekends. Pausing can be scheduled, and storage is retained.

**Use Serverless for intermittent workloads.** Billing per second while queries run is dramatically cheaper for a cluster queried a few hours a day.

**Right-size.** RA3 nodes separate compute from storage, so growing data no longer requires adding compute.

**Reserved instances** offer substantial discounts for steady, predictable clusters committed for one or three years.

**Move cold data to S3 and query through Spectrum**, so cluster storage holds only what is queried often.

**Watch Spectrum scanning.** It is billed per terabyte scanned, and an unpartitioned external table on CSV can produce a large charge from a single careless query.

---

## Monitoring

| What to watch | Where |
|---|---|
| Queries queuing | `STV_WLM_QUERY_STATE` |
| Slow queries | `SVL_QUERY_SUMMARY`, `STL_QUERY` |
| Disk-based queries | `SVL_QUERY_SUMMARY` — memory exhaustion |
| Table skew | `SVV_TABLE_INFO` |
| Tables needing vacuum | `SVV_TABLE_INFO` — `unsorted` percentage |
| Load errors | `STL_LOAD_ERRORS` |

`SVV_TABLE_INFO` is the single most useful view. It reports skew, unsorted percentage, and whether distribution and sort keys are set — enough to identify most table-level performance problems at a glance.

---

## Key Takeaways

- Redshift runs a limited number of concurrent queries by design, so it is not a backend for user-facing applications.
- Automatic WLM with query priorities keeps interactive queries from queueing behind ETL.
- Query monitoring rules can abort runaway queries before they consume the cluster.
- Concurrency scaling adds transient capacity for bursts, with a daily credit allowance.
- Result caching is free and automatic, but only for identical queries against unchanged data.
- Materialized views precompute expensive aggregations and are the standard answer to repeated dashboard queries.
- Pause unused clusters or use Serverless for intermittent workloads — idle clusters bill continuously.
- `SVV_TABLE_INFO` reports skew, unsorted percentage, and key configuration in one place.
