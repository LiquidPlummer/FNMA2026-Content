# Materialized Views

A regular **view** is just a saved query — a name given to a `SELECT` statement so you can reference it like a table. It doesn't store any data of its own; every time you query it, the underlying query runs fresh against the live data.

A **materialized view** is the same idea with one key difference: the result of the query is actually **stored**, like a table, instead of recomputed every time. Querying a materialized view means reading pre-computed results, not re-running the original (possibly expensive) query.

## Why It Matters

Some analytical queries are expensive — heavy aggregations across huge fact tables, joins across several large dimensions, the kind of thing that's slow to compute on every single request. If that same result is going to be asked for repeatedly (a dashboard that a dozen people check throughout the day, for instance), recomputing it from scratch every time is wasted work.

A materialized view trades that repeated computation for storage: pay the cost of running the query once, store the result, and every subsequent read is fast — until the underlying data changes.

## The Catch: Staleness

Because the result is stored rather than computed live, a materialized view can drift out of sync with the underlying tables as soon as they're updated. It has to be **refreshed** — the original query re-run and the stored result replaced — to catch up. Depending on the system, that refresh might be:

- Manual, triggered on demand
- Scheduled, on a regular interval
- Automatic, triggered by changes to the underlying data

Choosing a materialized view over a plain view (or over just querying the base tables directly) means explicitly accepting that trade-off: faster reads, in exchange for results that are only as fresh as the last refresh.

## View vs. Materialized View, at a Glance

| | View | Materialized View |
|---|---|---|
| Stores data? | No — just the query definition | Yes — the query's result set |
| Freshness | Always current (runs live) | As current as the last refresh |
| Read cost | Full query runs every time | Cheap — reading stored data |
| Best for | Simplifying/reusing a query, no perf concern | Expensive queries read often, some staleness is acceptable |

## Why This Matters for Application Developers

This same trade-off — compute it fresh every time vs. compute it once and cache the result — shows up constantly outside of databases too (application-level caching, precomputed API responses, and so on). Materialized views are that same idea applied directly inside the data layer, which makes them a useful mental model even before touching a specific warehouse product's implementation of them.
