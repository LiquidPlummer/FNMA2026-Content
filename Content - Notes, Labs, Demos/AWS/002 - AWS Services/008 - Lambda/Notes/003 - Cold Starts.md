# Cold Starts

A **cold start** is the extra latency when Lambda has to create a new execution environment rather than reusing one. It is the most discussed Lambda characteristic and often the most overstated.

---

## What Actually Happens

On a cold start, before the handler runs:

1. **Download the deployment package** — proportional to package size
2. **Start the execution environment** — the runtime process
3. **Initialize the runtime** — JVM start, Python interpreter, Node process
4. **Run initialization code** — everything at module level, outside the handler

Only then does the handler execute. Steps 1–3 are AWS's; step 4 is ours, and it is usually the largest and the only one we control.

---

## Typical Magnitudes

Rough figures, which vary considerably:

| Runtime | Typical cold start |
|---|---|
| Python, Node.js | 100–300 ms |
| Go, Rust | 50–150 ms |
| Java, .NET | 500 ms – several seconds |
| Any runtime in a VPC | Add ~10–100 ms (much improved from earlier years) |
| Container image | Depends on size; often comparable to zip |

**Initialization code dominates.** A function importing a large framework, creating several clients, and fetching configuration can spend seconds in step 4 — far more than the platform's own overhead.

---

## What Actually Reduces Them

**Reduce package size.** Only what is needed. A Python function bundling the whole AWS SDK when it uses one client, or a Node function shipping `node_modules` including dev dependencies, downloads more than necessary. Tree-shaking and dependency pruning help directly.

**Minimize initialization work.** Move anything not needed on every invocation out of module scope, or make it lazy:

```python
_db = None

def get_db():
    global _db
    if _db is None:
        _db = create_connection()
    return _db

def handler(event, context):
    if event.get("type") == "healthcheck":
        return {"ok": True}          # never touches the database
    return query(get_db(), event)
```

*Lazy initialization means the connection is created only when a code path needs it, so invocations that do not are unaffected.*

**Increase memory.** Memory also determines CPU, so a function doing meaningful initialization work initializes faster with more memory. This frequently reduces cold start duration and sometimes reduces total cost.

**Choose a lighter runtime** where it is an option. Go and Rust start fastest; Java and .NET are slowest without additional measures.

**Use SnapStart for Java.** It snapshots the initialized environment and restores from it, cutting Java cold starts dramatically. It requires care with anything that must be unique per environment — random seeds, cached credentials, connection state — since the snapshot is shared.

**Provisioned concurrency** keeps a set number of environments initialized and ready, eliminating cold starts for that many concurrent invocations. It is billed hourly whether used or not, so it is a real cost, appropriate for latency-sensitive endpoints with known traffic patterns.

---

## What Does Not Help

**Warming pings.** Invoking a function on a schedule keeps *one* environment warm. It does nothing for concurrency — the second simultaneous request still cold-starts — and it costs invocations. It was a common workaround and has largely been superseded by provisioned concurrency, which addresses the actual problem.

**Obsessing over the platform overhead.** For most functions, the 100–200 ms of platform cold start is small relative to the work the function does. Effort is better spent on initialization code.

---

## Whether It Matters

**It usually does not.** Cold starts affect a small fraction of invocations — a steadily invoked function reuses environments and rarely cold-starts. For asynchronous work, batch processing, and scheduled jobs, an extra 200 ms is irrelevant.

**It matters for:**

- **User-facing synchronous APIs** with tight latency budgets
- **Functions with heavy initialization**, where the cost is seconds rather than milliseconds
- **Spiky traffic**, where a burst creates many environments at once and a large share of requests cold-start
- **Chained functions**, where several cold starts accumulate in one request

---

## Measuring

CloudWatch Logs `REPORT` lines include `Init Duration` on cold starts only:

```
REPORT RequestId: abc-123 Duration: 45.32 ms Billed Duration: 46 ms
Memory Size: 512 MB Max Memory Used: 89 MB Init Duration: 412.55 ms
```

*`Init Duration` appears only on cold starts, so its presence identifies them and its value shows how much is initialization.*

```
filter @type = "REPORT"
| stats count(*) as invocations,
        sum(strcontains(@message, "Init Duration") > 0) as cold_starts,
        avg(@initDuration) as avg_init_ms by bin(1h)
```

*Cold start rate and average initialization time per hour — the data needed to decide whether any of this is worth acting on.*

Measure before optimizing. A function cold-starting on 0.1% of invocations does not need provisioned concurrency.

---

## Key Takeaways

- A cold start includes package download, environment startup, runtime initialization, and our initialization code.
- Initialization code is usually the largest component and the only one we control directly.
- Reduce package size, minimize or lazily defer initialization, and increase memory to get more CPU during init.
- SnapStart substantially reduces Java cold starts but shares snapshot state across environments.
- Provisioned concurrency eliminates cold starts for a set concurrency level, billed hourly regardless of use.
- Warming pings keep only one environment warm and do nothing for concurrent requests.
- Cold starts matter for latency-sensitive synchronous APIs, heavy initialization, spiky traffic, and chained functions.
- `Init Duration` in the `REPORT` line identifies cold starts and quantifies initialization cost.
