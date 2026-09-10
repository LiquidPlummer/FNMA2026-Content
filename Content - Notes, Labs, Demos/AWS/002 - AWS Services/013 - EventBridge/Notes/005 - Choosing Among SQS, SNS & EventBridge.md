# Choosing Among SQS, SNS & EventBridge

Three messaging services with overlapping capabilities. The decision follows from **what the consumer needs**, not from what the producer wants to send.

---

## The Core Distinction

**SQS** — one message, one consumer, durable, consumer sets the pace.

**SNS** — one message, many consumers, pushed immediately, not stored.

**EventBridge** — one event, routed by content to many targets, with archive and replay.

---

## The Comparison

| | SQS | SNS | EventBridge |
|---|---|---|---|
| Consumers per message | One | Many | Many |
| Delivery | Pull | Push | Push |
| Durability | Up to 14 days | None | None (archive is separate) |
| Ordering | FIFO available | FIFO available | **None** |
| Filtering | None | By attribute | **By full content** |
| Latency | Low | **Lowest** | Higher, sub-second |
| Throughput | **Highest** | High | Lower, quota-limited |
| Target types | Consumers poll | ~7 protocols | **20+ AWS services** |
| AWS event sources | No | No | **Yes, extensive** |
| Archive and replay | No | No | **Yes** |
| Schema registry | No | No | Yes |
| Cost per million | ~$0.40 | ~$0.50 + delivery | ~$1.00 |

---

## Deciding by Consumer Need

The most reliable framing is to ask what the consumer requires.

**"I need work distributed to workers, and none of it can be lost."** → **SQS.** Durability and pace control are exactly what a queue provides.

**"Several systems need to know this happened, immediately."** → **SNS**, or SNS with SQS queues per consumer if loss is unacceptable.

**"Different consumers care about different events, and I don't want to route this in code."** → **EventBridge.** Content-based routing is its distinguishing feature.

**"I need to react to something an AWS service did."** → **EventBridge.** It is the only one with native AWS event sources.

**"I need to reprocess events after fixing a bug."** → **EventBridge**, with archive and replay.

**"Order matters."** → **SQS FIFO**, or SNS FIFO to SQS FIFO. EventBridge cannot do this at all.

**"Very high throughput at the lowest cost."** → **SQS**, which has the highest limits and lowest per-message cost.

---

## They Combine

These are not mutually exclusive, and the useful architectures use several:

```
Order service
     │ PutEvents
     ▼
EventBridge (orders-bus)
     ├── rule: order.placed  ──► SQS ──► Fulfillment
     ├── rule: order.placed  ──► SQS ──► Email
     ├── rule: order.*       ──► SQS ──► Analytics
     └── rule: total > 10000 ──► SNS ──► Fraud alerts
```

*EventBridge routes by content; queues give each consumer durability and pace; SNS pushes an urgent alert. Each service does what it is best at.*

**The general pattern: EventBridge for routing, SQS for buffering, SNS for immediate notification.**

---

## Common Mistakes

**Using SNS where durability is needed.** A direct SNS-to-Lambda subscription loses messages when the function is unavailable. Add the queue.

**Using EventBridge where ordering matters.** It provides none, and no configuration adds it.

**Using SQS for fanout by creating several queues and writing to each.** That puts routing in the producer, which then changes every time a consumer is added — the coupling messaging was meant to remove.

**Using EventBridge for very high throughput.** Its quotas are lower than SQS and SNS, and it costs more per message.

**Routing in the consumer.** A function receiving every event and returning early for most is paying for invocations to do nothing. Filter in the rule or the subscription.

---

## A Reasonable Default

For a new event-driven application:

**Start with EventBridge on a custom bus.** Publishing events without knowing all consumers is exactly what it is for, and adding a consumer later is a rule rather than a producer change.

**Put an SQS queue between each rule and its consumer** where losing an event would matter.

**Add SNS** where immediate push to email, SMS, or mobile is needed.

**Use SQS directly**, without EventBridge, for straightforward work queues — a task producer and a worker pool with no routing decision to make.

The mistake worth avoiding is putting EventBridge in front of a single consumer that always receives everything. That is a queue with extra latency and cost.

---

## Key Takeaways

- SQS delivers one message to one consumer durably; SNS pushes to many without storing; EventBridge routes by content with archive and replay.
- Decide by what the consumer needs — durability, immediacy, routing, or ordering.
- Only EventBridge has native AWS service event sources and replay; only SQS and SNS offer ordering.
- SQS has the highest throughput and lowest cost; EventBridge has the lowest limits and highest cost.
- The services combine: EventBridge routes, SQS buffers, SNS notifies.
- Do not use SNS alone where message loss matters, or EventBridge where order matters.
- Fanout implemented by writing to several queues puts routing in the producer, which defeats decoupling.
- Filter in the rule or subscription rather than returning early in the consumer.
