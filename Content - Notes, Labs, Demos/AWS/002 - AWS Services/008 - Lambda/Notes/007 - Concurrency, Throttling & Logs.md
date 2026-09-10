# Concurrency, Throttling & Logs

How many copies of a function can run at once, what happens at the limit, and how to find what a particular invocation did.

---

## Concurrency

**Concurrency** is the number of invocations running simultaneously. Each needs its own execution environment.

A useful approximation:

```
concurrency ≈ requests per second × average duration in seconds
```

*100 requests/second at 200 ms averages 20 concurrent executions. Halving duration halves concurrency — which is another reason duration matters beyond latency.*

**Account concurrency** is a regional limit shared by every function, defaulting to 1,000 and raisable on request. This is the single most consequential quota in a serverless architecture: one function consuming all of it throttles every other function in the region.

---

## Reserved and Provisioned Concurrency

Two settings that do different things, and are easily confused.

**Reserved concurrency** partitions the account pool. Setting 100 on a function guarantees it can reach 100 and caps it there, removing 100 from the shared pool.

It does two jobs at once:

- **Guarantees capacity** for a critical function.
- **Caps a function** to protect something downstream — a database with a connection limit, or a rate-limited third-party API.

Setting it to **0** disables a function entirely, which is a useful emergency control.

**Provisioned concurrency** keeps environments initialized and ready, eliminating cold starts for that many concurrent invocations. It is billed hourly whether used or not, and it applies to a specific version or alias — never to `$LATEST`.

| | Reserved | Provisioned |
|---|---|---|
| Purpose | Guarantee and cap concurrency | Eliminate cold starts |
| Cost | Free | Hourly |
| Effect on pool | Removes from shared pool | Does not partition |
| Applies to | The function | A version or alias |

---

## Scaling and Burst

Lambda scales quickly but not instantly. Each function scales by a burst allowance, then by a further increment every 10 seconds until it reaches its limit.

A sudden spike from zero to thousands of concurrent requests exceeds the burst and produces throttling while Lambda scales — which is a common cause of errors during a traffic surge that the account limit alone does not explain.

---

## Throttling

At the concurrency limit, Lambda rejects invocations with `TooManyRequestsException` (HTTP 429). What that means depends on the invocation type:

- **Synchronous** — the error returns to the caller, which sees a failed request.
- **Asynchronous** — Lambda retries with backoff for up to six hours, so throttling is usually invisible.
- **SQS** — messages remain in the queue and are retried, so throttling delays rather than drops work.

This asymmetry is worth internalizing: **queue-based invocation absorbs throttling; synchronous invocation converts it into user-visible errors.** Putting a queue between a spiky producer and a function is the standard remedy.

The `Throttles` metric should be alarmed on. Sustained throttling means either the limit needs raising or something is consuming more concurrency than expected.

---

## Reading Logs

Everything written to stdout or stderr goes to CloudWatch Logs, in a group named `/aws/lambda/<function-name>`, with a stream per execution environment.

Because streams are per environment and not per request, browsing streams is an inefficient way to find one invocation. Querying the group is better:

```
fields @timestamp, @message
| filter @requestId = "abc-123-def"
| sort @timestamp asc
```

*All log lines for one invocation. `@requestId` is populated automatically and matches `context.aws_request_id`.*

**Log structured JSON**, so fields are queryable:

```python
import json, logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def handler(event, context):
    logger.info(json.dumps({
        "event": "order_received",
        "order_id": event["orderId"],
        "request_id": context.aws_request_id,
    }))
```

*Structured events allow filtering and aggregating on fields directly, rather than pattern-matching text.*

Lambda also supports **advanced logging controls** — setting log format to JSON, a minimum log level, and a custom log group — without changing code.

---

## The REPORT Line

Every invocation produces a summary:

```
REPORT RequestId: abc-123 Duration: 412.55 ms Billed Duration: 413 ms
Memory Size: 1024 MB Max Memory Used: 178 MB Init Duration: 320.11 ms
```

*The most useful single line Lambda produces: actual duration, billed duration, configured and used memory, and — on cold starts only — initialization time.*

It supports the two most common tuning questions: whether memory is set well, and how often cold starts occur.

---

## The Metrics Worth Alarming On

| Metric | Why |
|---|---|
| `Errors` | Function failures |
| `Throttles` | Hitting a concurrency limit |
| `Duration` (p99) | Approaching the timeout |
| `ConcurrentExecutions` | Approaching the account limit |
| `DeadLetterErrors` | Failures that could not even reach the DLQ |
| `IteratorAge` | Stream processing falling behind |

`IteratorAge` deserves attention for Kinesis and DynamoDB Stream consumers — a growing value means the function is not keeping up, and records eventually expire unprocessed.

---

## Key Takeaways

- Concurrency is roughly requests per second times duration, so reducing duration reduces concurrency needs.
- Account concurrency is a regional pool shared by all functions, defaulting to 1,000.
- Reserved concurrency guarantees and caps a function's concurrency and removes it from the shared pool; setting it to 0 disables the function.
- Provisioned concurrency keeps environments warm, is billed hourly, and applies to a version or alias.
- Lambda scales in bursts, so sudden spikes can throttle even below the account limit.
- Synchronous throttling reaches the user; queue-based invocation absorbs it as delay.
- Find an invocation's logs by querying `@requestId` rather than browsing per-environment streams.
- The `REPORT` line gives duration, billed duration, memory used, and cold start init time.
