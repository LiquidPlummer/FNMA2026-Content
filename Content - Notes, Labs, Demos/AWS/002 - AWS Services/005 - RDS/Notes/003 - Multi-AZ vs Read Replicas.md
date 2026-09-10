# Multi-AZ vs Read Replicas

Two RDS features that both create a second copy of the database and solve entirely different problems. Confusing them is one of the most consequential misunderstandings in RDS.

**Multi-AZ is for availability. Read replicas are for read scaling.**

---

## Multi-AZ

Multi-AZ maintains a **standby instance in another Availability Zone**, kept in sync by synchronous replication. Every committed write is durable in both zones before the commit returns.

The standby is not usable. It serves no queries, accepts no connections, and exists only to take over.

On failure — instance failure, AZ failure, storage failure, or an instance class change — RDS promotes the standby and **repoints the DNS endpoint** at it. The application reconnects to the same endpoint and finds the new primary. Failover typically takes 60–120 seconds.

```
Application ──► rds-endpoint.amazonaws.com
                        │
                        ▼
                Primary (us-east-1a)  ══sync══►  Standby (us-east-1b)
                                                  no reads, no connections
```

*The endpoint is a DNS name that RDS repoints on failover; the application does not change configuration.*

**Cost: roughly double.** The standby is a full instance doing no work.

**Latency cost:** synchronous replication means every write waits for the second AZ to acknowledge. This adds a small amount of write latency — a real trade for a write-heavy workload.

**Multi-AZ DB clusters** are a newer variant with two readable standbys and faster failover, offering some of both features at higher cost.

---

## Read Replicas

A read replica is a **separate, readable instance** kept up to date by **asynchronous** replication.

- It has its **own endpoint**, and applications must be written to use it.
- It is **read-only**.
- Replication is asynchronous, so it **lags** — usually milliseconds, sometimes much more under write load.
- It can be in the same AZ, another AZ, or **another region**.
- Up to 5 (or 15 for Aurora) per primary.
- It can be **promoted** to a standalone writable instance, which is a manual action and breaks replication permanently.

Read replicas provide no automatic failover. If the primary fails, the replica keeps serving stale reads and nothing promotes it.

---

## The Comparison

| | Multi-AZ | Read replica |
|---|---|---|
| Purpose | Availability | Read scaling |
| Replication | Synchronous | Asynchronous |
| Readable | No | Yes |
| Endpoint | Same as primary | Its own |
| Automatic failover | Yes | No |
| Cross-region | No | Yes |
| Application changes | None | Must route reads |
| Lag | None | Yes |

---

## Replication Lag

The property that makes read replicas subtle. A write to the primary is not immediately visible on a replica, so this sequence produces a bug:

```
1. User submits a form       → write to primary
2. Redirect to detail page   → read from replica
3. Replica hasn't caught up  → "record not found"
```

*A read-after-write against a replica can return stale data or nothing, and it fails intermittently — worse than failing consistently.*

The usual handling: **route reads that must reflect a recent write to the primary.** Many applications send all reads within a user's session-after-write window to the primary, and everything else to replicas. Analytical and reporting queries, which tolerate lag well, are the clearest replica candidates.

`ReplicaLag` is a CloudWatch metric and should be alarmed on. Lag grows under heavy write load and during long transactions, and a replica that falls far behind is serving meaningfully stale data.

---

## Using Both

They are complementary, and a production database commonly has both:

```
Primary (us-east-1a) ══sync══► Standby (us-east-1b)     ← availability
       ║
       ╠══async══► Read replica (us-east-1c)            ← read scaling
       ╚══async══► Read replica (eu-west-1)             ← regional reads / DR
```

*Multi-AZ handles failure of the primary; replicas absorb read load; a cross-region replica provides both regional reads and a disaster recovery position.*

A cross-region replica is the standard disaster recovery arrangement for RDS. Promotion is manual and loses whatever was in flight, which makes it a recovery mechanism rather than a failover one.

---

## Choosing

**Enable Multi-AZ** for any production database where downtime is costly. Skip it in development, where restoring from a snapshot is acceptable.

**Add read replicas** when reads are the bottleneck and the application can route them — analytics and reporting first, since they tolerate lag.

**Neither helps write throughput.** Both leave a single writer. Scaling writes means a larger instance, and beyond that, sharding or a different data store.

---

## Key Takeaways

- Multi-AZ provides availability through a synchronous, unreadable standby with automatic failover on the same endpoint.
- Read replicas provide read scaling through asynchronous, readable copies with their own endpoints and no automatic failover.
- Multi-AZ roughly doubles cost and adds write latency; replicas add cost per replica and require application routing.
- Replication lag causes intermittent read-after-write bugs; route reads needing recent writes to the primary.
- Alarm on `ReplicaLag`, which grows under write load and long transactions.
- Cross-region replicas serve regional reads and disaster recovery, with manual promotion.
- Neither feature increases write throughput — that remains bounded by a single writer.
