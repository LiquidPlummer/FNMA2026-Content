# Delivery, Retries, Archive & Replay

What EventBridge guarantees about delivery, and the archive feature that neither SNS nor SQS provides.

---

## Delivery Guarantees

**EventBridge delivers at least once.** An event may be delivered to a target more than once, so **targets must be idempotent** — the same requirement as SQS, for the same reason.

**There is no ordering guarantee.** Events may arrive out of order, and unlike SQS there is no FIFO variant. Any consumer depending on order needs to handle it — with a sequence number in the event, a timestamp comparison, or by making the operations order-independent.

**Delivery is fast but not instant.** Typically under a second, sometimes longer. It is not a low-latency synchronous path.

---

## Retries and Failure Handling

Each target has a retry policy:

```bash
aws events put-targets --rule order-placed --targets '[{
  "Id": "process-order",
  "Arn": "arn:aws:lambda:us-east-1:123456789012:function:order-processor",
  "RetryPolicy": {
    "MaximumRetryAttempts": 5,
    "MaximumEventAgeInSeconds": 3600
  },
  "DeadLetterConfig": {
    "Arn": "arn:aws:sqs:us-east-1:123456789012:events-dlq"
  }
}]'
```

*Retries up to five times within an hour, then sends the event to a dead-letter queue. Defaults are 185 retries over 24 hours.*

**Every production target should have a dead-letter queue.** Without one, an event exhausting its retries is discarded silently — and unlike SQS, there is no queue holding it and no metric that obviously says so.

The DLQ receives the event with attributes explaining the failure — the error code, the message, and the rule and target involved — which is more context than an SQS DLQ provides.

---

## Metrics

| Metric | Meaning |
|---|---|
| `Invocations` | Successful target invocations |
| `FailedInvocations` | **Permanent failures** |
| `ThrottledRules` | Rules throttled by quota |
| `DeadLetterInvocations` | Events sent to a DLQ |
| `MatchedEvents` | Events matching a rule |

Two comparisons are informative. `MatchedEvents` at zero on a rule expected to fire means the pattern is wrong. `MatchedEvents` much higher than `Invocations` means deliveries are failing.

`FailedInvocations` is the primary alarm.

---

## Archive and Replay

EventBridge can **archive events** and later **replay** them — a capability neither SNS nor SQS has.

```bash
aws events create-archive --archive-name orders-archive \
  --event-source-arn arn:aws:events:us-east-1:123456789012:event-bus/orders-bus \
  --retention-days 90 \
  --event-pattern '{"source": ["com.acme.orders"]}'
```

*Archives matching events for 90 days. An archive can cover everything on a bus or a filtered subset.*

Replaying sends archived events back to the bus over a time range:

```bash
aws events start-replay --replay-name reprocess-sept \
  --event-source-arn arn:aws:events:us-east-1:123456789012:archive/orders-archive \
  --event-start-time 2026-09-01T00:00:00Z \
  --event-end-time 2026-09-09T00:00:00Z \
  --destination '{"Arn":"arn:aws:events:us-east-1:123456789012:event-bus/orders-bus","FilterArns":["arn:aws:events:...:rule/orders-bus/reprocess-rule"]}'
```

*Replays a week of events to specific rules. `FilterArns` is what makes this safe — replaying to all rules would re-trigger every consumer.*

### What it is for

**Recovering from a consumer bug.** A function processed a week of events incorrectly. Fix it, replay that week to that rule alone, and the work is redone with no producer involvement.

**Populating a new consumer.** A new service needs the history it missed. Replay to its rule only.

**Testing against real events.** Replay production events into a test environment.

**Investigating.** Archived events are a record of what actually happened.

### The caution

Replay re-delivers events, so **consumers must be idempotent** — the same requirement as before, now with a mechanism that can trigger it deliberately.

And **replay must be scoped**. Replaying to a whole bus re-triggers every rule and every target, which can mean sending a week of duplicate emails. `FilterArns` limiting the replay to specific rules is essential rather than optional.

---

## Schema Registry

A related capability: EventBridge can **discover event schemas** on a bus and generate typed code bindings from them. Where events cross team boundaries, this makes the event structure an explicit, versioned contract rather than something consumers infer from examples.

---

## Key Takeaways

- EventBridge delivers at least once with no ordering guarantee, so targets must be idempotent.
- Each target has a retry policy defaulting to 185 attempts over 24 hours.
- Configure a dead-letter queue per target, or exhausted events are discarded silently.
- The DLQ receives failure context — error code, message, rule, and target.
- Alarm on `FailedInvocations`, and compare `MatchedEvents` against `Invocations` to spot delivery problems.
- Archives retain events for a configurable period and can be filtered by pattern.
- Replay redelivers archived events over a time range and is how a consumer bug is remediated.
- Always scope a replay with `FilterArns`, or every rule and target re-fires.
