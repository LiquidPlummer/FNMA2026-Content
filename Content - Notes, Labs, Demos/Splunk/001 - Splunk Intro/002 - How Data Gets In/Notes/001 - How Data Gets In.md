# How Data Gets In

Before anything can be searched, data has to travel from the machines that produce it into Splunk, and it gets organized along the way. Some of the decisions made during that trip are permanent, which is why the path is worth understanding.

---

## The Three Moving Parts

A Splunk deployment is built from three roles:

- **Forwarders** collect data on the machines that produce it and send it onward.
- **Indexers** receive that data, process it, and store it so it can be searched.
- **Search heads** are where people work. They take our searches, send them to the indexers, and assemble the results.

```text
web-01  [forwarder] ──┐
app-01  [forwarder] ──┼──►  Indexers  ◄───  Search head  ◄───  us
db-01   [forwarder] ──┘     (store)         (search + UI)
```

*Forwarders push data into the indexers; the search head sends our searches to the indexers and brings back what they find.*

In a small environment, one Splunk server plays both the indexer and search head roles. In a large one, each role is spread across many machines — dozens of indexers sharing the storage and search work, with several search heads in front of them. The roles are the same either way.

---

## Forwarders

A **forwarder** is Splunk software installed on a source machine. We tell it what to watch — log files, the Windows event log, the output of a script — and it sends new data to the indexers as it appears.

The one we'll run into most is the **Universal Forwarder (UF)**. It's a small, separate install built to do only one thing: collect and forward. It has no web interface, stores nothing, and can't search, which keeps it light enough to run on thousands of servers without anyone noticing. In organizations that use Splunk, it's often already installed on servers and sometimes on employee machines — on Windows, it appears as a service named `SplunkForwarder`.

A **heavy forwarder** is the contrast: a full Splunk installation configured to forward. It can filter, reroute, or mask data before passing it on, but it's far bigger, so it's used for specific jobs rather than installed everywhere.

Forwarders aren't the only way in. Applications can send events directly to Splunk over HTTP through the **HTTP Event Collector (HEC)**, and network devices commonly send **syslog**. Whatever the route, the data ends up at the indexers.

One practical note: Splunk has traditionally been licensed by how much data it takes in per day. That makes "what should we send?" a budget question as well as a technical one.

---

## Indexers

The **indexer** is where data lands and lives. As data arrives, the indexer:

1. Breaks the incoming stream into individual records, called **events**
2. Finds and records each event's timestamp
3. Labels each event with where it came from
4. Writes it to disk, along with an index of the terms it contains so it can be found quickly

When a search runs, the indexers are the ones that actually dig through the stored data. Adding indexers is how a deployment handles more data and keeps searches fast.

---

## Indexes

An **index** is a named storage area that data gets sent into. A typical deployment has many — `web`, `firewall`, `windows`, `payments_app`, and so on. Data not assigned anywhere lands in the default index, `main`, and Splunk's own logs live in indexes that start with an underscore, like `_internal`.

Data is separated into indexes for three reasons:

- **Access control.** Permissions are granted per index. Security data can live in an index most people can't see.
- **Retention.** Each index has its own rules for how long data is kept — firewall logs for a year for compliance, debug logs for two weeks.
- **Search speed.** A search that names its index only looks there, instead of through everything.

Inside an index, data is stored in time-ordered chunks called **buckets**, which age through stages: **hot** (being written now), **warm** (recent), **cold** (older, often on cheaper storage), and **frozen** (past retention, deleted or archived). These words come up whenever people talk about storage and retention.

---

## Sourcetype

Every event is labeled with three pieces of metadata about its origin:

- **host** — the machine it came from, like `web-01`
- **source** — the file or input it came from, like `/var/log/nginx/access.log`
- **sourcetype** — what kind of data it is

The **sourcetype** is Splunk's label for the data's format: this is a web server access log, this is syslog, this is JSON from our payments app. It's the most important of the three, because Splunk uses it to decide:

- **Where one event ends and the next begins.** Most logs are one event per line, but a Java stack trace is one event spread across dozens of lines.
- **Where the timestamp is, and how to read it.**
- **Which fields to pull out** when we search.

A wrong sourcetype causes trouble everywhere: stack traces chopped into fragments, events filed under the wrong time so searches miss them, and fields that never show up. Splunk ships with sourcetypes for common formats, like `access_combined` for web server access logs and `syslog`, and teams define their own for everything else.

---

## Index Time vs. Search Time

The most useful single idea for understanding how Splunk behaves is the split between what happens once, on the way in, and what happens every time we search.

**Index time** is when data is written. Decisions made here are baked in: the event's index, its host, source, and sourcetype, where the event boundaries fall, and its timestamp. Changing any of them later means deleting the data and bringing it in again — often impossible, since the original logs may be long gone.

**Search time** is everything that happens when a search runs: pulling fields out of the raw text, adding information from lookup tables, applying labels. None of it is stored with the data, so it can be added or changed at any point, and the change applies to all the data — including data that arrived years ago.

| Index time — fixed once written | Search time — flexible forever |
|---|---|
| Index | Field extractions |
| host, source, sourcetype | Lookups |
| Event boundaries | Tags and event types |
| Timestamp | Calculated fields |

This is schema-on-read in practice. The working rule in most Splunk shops: get the index-time decisions right when new data is first brought in, because they're permanent, and do everything else at search time, where mistakes are cheap to fix.

---

## Key Takeaways

- Forwarders collect data at the source, indexers store it, and search heads run searches against it.
- The Universal Forwarder is a lightweight agent that only collects and forwards — it's the piece of Splunk most likely to be running on machines we work with.
- Indexes are named storage areas that separate data for access control, retention, and search speed.
- Sourcetype tells Splunk what format the data is in, which controls event boundaries, timestamps, and field extraction.
- Index-time decisions (index, host, source, sourcetype, timestamp) are permanent; search-time work (fields, lookups, tags) can change at any time and applies to all data.
