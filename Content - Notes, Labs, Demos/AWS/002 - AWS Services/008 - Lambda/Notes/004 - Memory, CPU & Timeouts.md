# Memory, CPU & Timeouts

Two settings that control performance and cost. The first is more consequential than its name suggests, because **memory also controls CPU**.

---

## Memory Controls CPU

Memory is configurable from 128 MB to 10,240 MB, and **CPU is allocated proportionally**. There is no separate CPU setting.

At roughly 1,769 MB a function receives the equivalent of one full vCPU; above that, additional vCPUs. At 128 MB it receives a small fraction of one.

This produces a result that surprises people: **more memory is often cheaper.**

Billing is per gigabyte-second — memory multiplied by duration. If doubling memory more than halves duration, total cost falls:

| Memory | Duration | GB-seconds | Relative cost |
|---|---|---|---|
| 128 MB | 4,000 ms | 0.500 | 1.00 |
| 512 MB | 900 ms | 0.450 | 0.90 |
| 1024 MB | 400 ms | 0.400 | 0.80 |
| 1769 MB | 250 ms | 0.432 | 0.86 |
| 3008 MB | 240 ms | 0.703 | 1.41 |

*For a CPU-bound function, cost falls until the work stops parallelizing, then rises. The cheapest setting is also the fastest here — 1024 MB is both.*

The curve differs per function. CPU-bound work benefits substantially; I/O-bound work waiting on a network call gains little, since more CPU does not make an API respond faster.

**AWS Lambda Power Tuning** is an open-source state machine that runs a function at several memory settings and reports the cost and duration curve. It takes minutes and frequently finds that the default 128 MB is both slower and more expensive than a higher setting.

---

## Finding the Right Setting

The `REPORT` line shows actual memory used:

```
REPORT RequestId: abc-123 Duration: 412.55 ms Billed Duration: 413 ms
Memory Size: 1024 MB Max Memory Used: 178 MB
```

*Using 178 MB of 1024 MB is not waste — the memory was bought for the CPU that comes with it.*

This is the point that needs stating clearly: **`Max Memory Used` being far below `Memory Size` is not a reason to reduce the setting.** Memory is the CPU dial. Reducing it to match actual memory consumption slows the function and often costs more.

Reduce memory only when a function is genuinely I/O-bound and duration does not increase.

---

## Timeouts

The timeout ranges from 1 second to **15 minutes**, which is a hard platform limit.

Choosing it:

**Not too long.** A function with a 15-minute timeout that hangs consumes concurrency for 15 minutes, potentially throttling everything else. It also delays the error.

**Not too short.** Killed invocations produce no result and no clean shutdown, and for retryable sources they will be retried — repeating the work and possibly the side effects.

**A reasonable rule:** set it to roughly twice the observed p99 duration. Enough headroom for a slow case, short enough to fail quickly when something is wrong.

**A timeout is not a graceful stop.** The environment is killed. No cleanup runs, no `finally` block completes, no partial result is returned. Using `get_remaining_time_in_millis()` to stop early is how a function returns cleanly instead.

---

## When 15 Minutes Is Not Enough

The limit is not adjustable, so a longer job needs a different approach:

**Split the work.** Process a batch, then invoke the function again with a continuation token. Step Functions coordinates this well.

**Use Step Functions.** A state machine can run for up to a year, orchestrating many short Lambda invocations.

**Use ECS or Batch.** For genuinely long-running compute, a container without a time limit is the right tool.

**Use Fargate for the long tail.** A common arrangement runs the fast majority on Lambda and dispatches rare long jobs to a container.

The wrong response is trying to compress a 30-minute job into 15 minutes. If the work genuinely takes that long, Lambda is not the right service for it.

---

## Ephemeral Storage

`/tmp` defaults to 512 MB and is configurable up to 10 GB, billed separately. It persists across warm invocations, so files must be cleaned up — a function writing to `/tmp` without removing files eventually fails on a full disk in a warm environment, which presents as an intermittent failure.

---

## Key Takeaways

- Memory is the only performance dial, and CPU scales with it — one full vCPU at roughly 1,769 MB.
- Billing is per gigabyte-second, so more memory is often cheaper when it reduces duration proportionally more.
- `Max Memory Used` far below `Memory Size` is not waste — the memory was bought for CPU.
- Use Lambda Power Tuning to find the cost and duration curve rather than guessing.
- I/O-bound functions gain little from extra memory; CPU-bound functions gain substantially.
- The timeout maximum is 15 minutes and is not adjustable; set it to roughly twice observed p99 duration.
- A timeout kills the environment with no cleanup — use remaining-time checks to stop gracefully.
- For longer work, split with Step Functions or move to ECS or Batch.
