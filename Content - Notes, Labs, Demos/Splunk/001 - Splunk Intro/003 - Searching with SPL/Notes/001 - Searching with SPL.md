# Searching with SPL

Splunk searches are written in the **Search Processing Language (SPL)**. Before looking at the language, we need to be clear on what it operates on: events and the fields inside them.

---

## Events

An **event** is a single record in Splunk: one timestamp and one chunk of raw text. Usually that's a single line from a log:

```text
192.0.2.15 - - [11/Sep/2026:14:02:07 -0400] "GET /checkout HTTP/1.1" 500 1043
```

*One event from a web server's access log — a single request, with its timestamp embedded in the text.*

The original text is always kept exactly as it arrived, in a field called `_raw`. Whatever else Splunk works out about an event, the raw text remains the source of truth. And whatever the data started as — a log line, a Windows security record, a JSON message from an application — once it's in Splunk, it's an event.

---

## Time Comes First

Of everything attached to an event, the timestamp, stored as `_time`, is the one Splunk cares about most. Data is stored in time order, and every search runs over a time range. When we search the last hour, Splunk skips everything outside that hour without reading it.

That makes the timestamp more than a detail. An event with the wrong timestamp is effectively lost: it's on disk, but a search over the time it actually happened won't find it. This is why getting timestamps right gets so much attention when new data is brought in.

---

## Fields

A **field** is a key/value pair pulled out of an event. From the event above, Splunk can extract:

| Field | Value |
|---|---|
| `clientip` | `192.0.2.15` |
| `method` | `GET` |
| `uri` | `/checkout` |
| `status` | `500` |
| `bytes` | `1043` |

Fields are what make searching precise. A plain search for `500` matches any event containing that number anywhere — a byte count, a response time, an order ID. A search for `status=500` matches only events whose status is 500.

### Default Fields

A few fields exist on every event, because Splunk assigns them when the data is written:

| Field | What it holds |
|---|---|
| `_time` | The event's timestamp |
| `host` | The machine it came from |
| `source` | The file or input it came from |
| `sourcetype` | The format of the data |
| `index` | The index it's stored in |
| `_raw` | The original, unmodified text |

Fields that start with an underscore are Splunk's internal fields.

### Extracted Fields

Everything else — `status`, `clientip`, `user`, `error_code` — is an **extracted field**, pulled from the raw text at search time. Splunk extracts some automatically, like anything written as `key=value`, and the sourcetype brings extraction rules for known formats. Teams define their own for everything else.

Because extraction happens at search time, these fields aren't stored with the data. A new extraction defined today works immediately on every event already in Splunk — schema-on-read, seen from the searcher's side.

---

## The Pipeline Model

An SPL search is a **pipeline**: a series of stages separated by the pipe character, `|`. Each stage takes the output of the one before it, does something to it, and passes the result along — the same idea as pipes in a Unix shell.

```spl
index=web sourcetype=access_combined status>=500 earliest=-24h
| stats count by host
| sort -count
```

*A three-stage search that finds which web servers produced the most server errors in the last 24 hours.*

Reading it one stage at a time:

1. **`index=web sourcetype=access_combined status>=500 earliest=-24h`** — Retrieve events from the `web` index: only web access logs, only server errors (status 500 and up), only from the last 24 hours. The output is a set of matching events.
2. **`stats count by host`** — Count those events, grouped by the server they came from. The output is no longer a list of events; it's a table with one row per host and a count.
3. **`sort -count`** — Order that table by count, highest first. The `-` means descending.

The result is a short list of servers, worst first. That's the method for reading any SPL search: one stage at a time, asking what goes in and what comes out.

### Why the First Stage Matters

The first stage is different from the rest. It's the stage that goes to the indexes and reads stored data. Every stage after it works on what the first stage handed over — filtering, reshaping, or summarizing it.

That makes the first stage where speed is won or lost. Narrowing there lets Splunk avoid reading data at all. Narrowing later — pulling back a year of events and then discarding most of them — reaches the same answer after doing enormously more work.

Three things narrow a search more than anything else:

- **Time range.** The biggest lever by far, since data is stored by time. Searching the last four hours instead of "All time" can be the difference between a few seconds and a search that never finishes.
- **Index.** So Splunk looks in one place instead of every place.
- **Sourcetype.** So Splunk considers only the kind of data we care about.

A well-written search states these up front, narrows further with specific terms and field values, and only then pipes into commands.

---

## Commands Worth Recognizing

SPL has well over a hundred commands, but a handful account for most of what we'll see in real searches. They fall into three families:

| Family | Command | What it does |
|---|---|---|
| Filtering | `search` | Keeps events matching terms or field values; implied at the start of every search |
| | `where` | Keeps results matching an expression, such as comparing two fields |
| Summarizing | `stats` | Counts, sums, averages, and more, grouped by one or more fields |
| | `top` | Lists the most common values of a field |
| | `timechart` | Summarizes over time intervals; the basis for most line charts |
| Shaping | `eval` | Creates or changes a field using a calculation |
| | `table` | Keeps only the listed fields, displayed as columns |
| | `sort` | Orders the results |
| | `rename` | Renames fields, usually for display |

The summarizing commands are known as **transforming commands**: they turn a list of events into a table of results. That table is what charts and dashboards are built from.

---

## `stats`: The One to Remember

If we remember one command, it should be `stats`. Most useful output from Splunk is some version of *count things, grouped by something*: errors by server, failed logins by user, requests by status code, sales by region.

`stats` takes an aggregation — `count`, `sum`, `avg`, `max`, `dc` (distinct count), and others — and an optional `by` clause saying how to group. For anyone who knows SQL, `stats count by host` does the same job as `SELECT host, COUNT(*) ... GROUP BY host`.

Recognize that shape, and most of the SPL we'll ever read becomes legible.

---

## Key Takeaways

- An event is one timestamped record of raw text; the original text is always preserved in `_raw`.
- `_time` is Splunk's most important field: data is stored by time, and every search runs over a time range.
- Fields are key/value pairs. Default fields (`_time`, `host`, `source`, `sourcetype`, `index`) are set when data is written; extracted fields are pulled out at search time and can be added anytime.
- An SPL search is a pipeline: each stage, separated by `|`, works on the output of the one before it.
- The first stage is what reads stored data, so narrowing by time range, index, and sourcetype there is what makes searches fast.
- Commands fall into filtering (`search`, `where`), summarizing (`stats`, `top`, `timechart`), and shaping (`eval`, `table`, `sort`, `rename`) — and `stats count by <field>` is the pattern to know.
