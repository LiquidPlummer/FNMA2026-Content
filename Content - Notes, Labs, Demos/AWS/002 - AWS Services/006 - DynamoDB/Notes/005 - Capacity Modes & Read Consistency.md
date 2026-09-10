# Capacity Modes & Read Consistency

Two settings that determine what DynamoDB costs and what a read guarantees.

---

## Capacity Units

Throughput is measured in units, and the arithmetic determines both cost and throttling.

**Read Capacity Units (RCU):**
- 1 RCU = one **strongly consistent** read of up to 4 KB per second
- 1 RCU = **two** eventually consistent reads of up to 4 KB per second
- Transactional reads cost double

**Write Capacity Units (WCU):**
- 1 WCU = one write of up to 1 KB per second
- Transactional writes cost double

Two consequences follow. **Reads round up to 4 KB and writes to 1 KB**, so a 0.5 KB item costs a full unit — which is why many small items cost more than the same bytes in fewer larger ones. And **writes are four times more expensive per byte than reads**, making write patterns the thing to optimize.

Index writes count separately: an item written to a table with three GSIs projecting the changed attributes consumes capacity four times.

---

## On-Demand

Pay per request, with no capacity to configure.

- **No throttling from capacity settings** — it scales automatically.
- Roughly **6–7 times the per-request cost** of provisioned capacity used efficiently.
- Instant scaling to double the previous peak; larger jumps may briefly throttle.
- No planning, no autoscaling configuration, no wasted capacity.

Suits unpredictable traffic, new applications with unknown patterns, spiky workloads, and development environments. It is the right default when the pattern is unknown.

---

## Provisioned

Specify RCU and WCU per second; requests beyond it are throttled.

- **Much cheaper per request** when utilization is high.
- Requires **autoscaling** to be useful, adjusting capacity toward a target utilization.
- Autoscaling reacts in minutes, so sharp spikes are throttled while it responds.
- Reserved capacity offers further discounts for committed usage.

Suits steady, predictable traffic at meaningful volume.

**Switching between modes is allowed once every 24 hours**, so starting on-demand and moving to provisioned once the pattern is understood is a sound approach.

---

## Burst Capacity and Throttling

Provisioned tables accumulate up to 300 seconds of unused capacity as burst, which absorbs short spikes. It is best-effort and not a substitute for correct provisioning.

When throttled, DynamoDB returns `ProvisionedThroughputExceededException`. AWS SDKs retry with exponential backoff automatically, so brief throttling appears as latency rather than errors.

Sustained throttling has three usual causes, and distinguishing them matters:

1. **Provisioned capacity genuinely too low** — raise it or enable autoscaling.
2. **A hot partition** — traffic concentrated on one partition key, throttling while the table is far below its total. This is a key design problem, not a capacity problem.
3. **An under-provisioned GSI** — which throttles writes to the base table.

The second is the one people mis-diagnose, because raising table capacity does not help.

---

## Read Consistency

Every read chooses between two guarantees.

**Eventually consistent (the default).** May return slightly stale data — typically within a second. Costs half an RCU per 4 KB.

**Strongly consistent.** Always returns the most recent committed write. Costs a full RCU, has slightly higher latency, and **is not available on Global Secondary Indexes**.

```python
response = table.get_item(
    Key={"CustomerId": "C-1001", "OrderId": "O-5503"},
    ConsistentRead=True,
)
```

*Requests a strongly consistent read. The default is eventually consistent, so this is opt-in per request.*

DynamoDB replicates across three Availability Zones. A write is acknowledged once a majority have it; an eventually consistent read may reach a replica that has not yet caught up.

**Use eventual consistency by default** — it is half the cost and sufficient for most reads. **Use strong consistency** for read-after-write within a single operation, and for reads driving a decision where staleness would be incorrect, such as checking a balance before deducting from it.

Note that a conditional write is atomic regardless of read consistency, so the common "check then write" pattern is better expressed as a single conditional write than as a strongly consistent read followed by a write.

---

## Cost Notes

**Storage** is billed per gigabyte-month, on top of throughput.

**TTL deletions are free.** Setting a TTL attribute lets DynamoDB delete expired items at no capacity cost — much cheaper than deleting them with `DeleteItem`, and the standard way to expire session data, caches, and event records.

**DynamoDB Streams** and **global tables** carry their own charges.

**DAX**, an in-memory cache in front of DynamoDB, offers microsecond reads for read-heavy workloads at the cost of running cluster nodes.

---

## Key Takeaways

- Reads round to 4 KB and writes to 1 KB, so many small items cost more than the same data in fewer larger ones.
- Writes cost roughly four times more per byte than reads, and each projecting index multiplies write cost.
- On-demand needs no planning and costs several times more per request; provisioned is cheaper at high, predictable utilization.
- Mode can be switched once per 24 hours, so starting on-demand and moving later is practical.
- Sustained throttling comes from low capacity, a hot partition, or an under-provisioned GSI — only the first is fixed by raising table capacity.
- Eventually consistent reads are the default and cost half as much; strong consistency is opt-in and unavailable on GSIs.
- Prefer a conditional write over a strongly consistent read followed by a write.
- TTL-based deletion is free and is the cheapest way to expire data.
