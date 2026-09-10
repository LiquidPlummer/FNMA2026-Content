# CloudWatch Logs Insights

**Logs Insights** is a query language for CloudWatch Logs. Enough of it to find one request among millions is a small amount of syntax, and it is the difference between scrolling log streams and answering a question.

---

## The Query Structure

Queries are pipelines of commands separated by `|`:

```
fields @timestamp, @message
| filter @message like /ERROR/
| sort @timestamp desc
| limit 20
```

*The basic shape: select fields, filter, sort, limit. Every query is some variation of this.*

Several fields are always present: `@timestamp`, `@message` (the raw event), `@logStream`, and — for Lambda — `@requestId`, `@duration`, and `@billedDuration`.

---

## The Commands That Matter

**`fields`** selects and computes fields:

```
fields @timestamp, @message, duration_ms / 1000 as duration_s
```

**`filter`** narrows results, with comparisons, regular expressions, and boolean logic:

```
filter status = "error" and duration_ms > 1000
filter @message like /timeout/
filter ispresent(order_id)
```

**`stats`** aggregates — the command that turns logs into answers:

```
stats count(*) as errors by bin(5m)
stats avg(duration_ms), max(duration_ms), pct(duration_ms, 99) by endpoint
```

*`bin(5m)` groups into five-minute buckets, producing a time series from log events. `pct` computes percentiles.*

**`parse`** extracts fields from unstructured text:

```
parse @message "user=* action=* status=*" as user, action, status
```

*Glob-style extraction. Regular expressions are also supported. This is unnecessary for JSON logs, which are parsed automatically — the clearest practical argument for structured logging.*

**`sort`** and **`limit`** work as expected.

---

## Finding One Request

The common task during an incident. If the application logs a correlation ID:

```
fields @timestamp, @message
| filter correlation_id = "a1b2c3d4-e5f6"
| sort @timestamp asc
```

*Every event for one request, in order. Insights can query up to 50 log groups at once, so this traces a request across several services in a single query.*

Without a correlation ID, the fallback is time and text:

```
fields @timestamp, @message
| filter @timestamp >= 1757426400000 and @timestamp <= 1757426460000
| filter @message like /order-4471/
| sort @timestamp asc
```

That works and is much slower to write and to run. Emitting a correlation ID on every log line is a small change that repays itself the first time it is needed.

---

## Queries Worth Keeping

**Error rate over time:**

```
filter level = "ERROR"
| stats count(*) as errors by bin(1m)
```

**Slowest endpoints:**

```
stats avg(duration_ms) as avg, pct(duration_ms, 99) as p99, count(*) as n by endpoint
| sort p99 desc
```

**Lambda cold starts:**

```
filter @type = "REPORT"
| stats count(*) as invocations,
        sum(strcontains(@message, "Init Duration") > 0) as cold_starts by bin(1h)
```

*`REPORT` lines carry duration and memory data; the presence of `Init Duration` marks a cold start.*

**Lambda memory headroom:**

```
filter @type = "REPORT"
| stats max(@maxMemoryUsed / 1000 / 1000) as max_mb, max(@memorySize / 1000 / 1000) as configured_mb
```

*Compares peak usage against the configured allocation — the basis for right-sizing memory.*

---

## Practical Notes

**Cost and speed both follow the time range.** Insights bills per gigabyte scanned, and the time range determines how much is scanned. Narrowing from 24 hours to 15 minutes is the single most effective optimization for both.

**Filter early.** Placing `filter` before `stats` reduces the work done downstream.

**Results are capped at 10,000 rows**, so exploratory queries should aggregate rather than list.

**Save queries.** The ones used during incidents should be saved rather than reconstructed under pressure.

**Insights does not query S3.** It queries CloudWatch Logs only. Logs archived to S3 are queried with Athena instead, which is a different tool and a different syntax — one more reason to keep a useful window in CloudWatch.

---

## Key Takeaways

- Queries are pipelines of `fields`, `filter`, `stats`, `parse`, `sort`, and `limit`.
- `@timestamp`, `@message`, and `@logStream` are always available; Lambda adds `@requestId` and duration fields.
- `stats` with `bin()` turns log events into time series, and supports percentiles.
- JSON logs are parsed automatically, while unstructured text requires `parse` — the practical case for structured logging.
- A correlation ID makes tracing one request across up to 50 log groups a single query.
- Cost and speed both depend on the scanned time range, so narrow it first and filter before aggregating.
- Results cap at 10,000 rows, and Insights cannot query logs archived to S3.
