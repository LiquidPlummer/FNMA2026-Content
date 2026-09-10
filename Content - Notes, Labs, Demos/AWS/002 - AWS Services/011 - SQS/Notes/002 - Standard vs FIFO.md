# Standard vs FIFO

SQS offers two queue types with different guarantees. The choice is permanent — a queue's type cannot be changed — and FIFO's guarantees come with a throughput cost.

---

## The Guarantees

| | Standard | FIFO |
|---|---|---|
| Ordering | Best effort | **Strict, per message group** |
| Delivery | **At least once** | Exactly once (with deduplication) |
| Duplicates | Possible | Prevented within a 5-minute window |
| Throughput | Nearly unlimited | 300 msg/s, or 3,000 batched; higher with high-throughput mode |
| Naming | Any | Must end in `.fifo` |

---

## Standard Queues

**Ordering is best effort.** Messages usually arrive roughly in order and sometimes do not. Any consumer depending on order will eventually be wrong.

**Delivery is at least once.** A message may be delivered more than once — because SQS stores messages redundantly and a delete may not reach every copy, or because a consumer failed after processing but before deleting.

This is not rare enough to ignore. **Consumers must be idempotent**, which is the defining requirement of standard queues.

In exchange, throughput is effectively unlimited and latency is low. For most workloads this is the right choice.

---

## FIFO Queues

**Ordering is strict within a message group.** The `MessageGroupId` partitions the queue: messages in one group are delivered in order, and different groups proceed independently and in parallel.

```python
sqs.send_message(
    QueueUrl=fifo_url,
    MessageBody=json.dumps({"orderId": "O-5501", "event": "shipped"}),
    MessageGroupId="O-5501",                      # order within this order
    MessageDeduplicationId="O-5501-shipped-v1",   # duplicate suppression
)
```

*Events for one order are strictly ordered; events for different orders proceed in parallel. The group ID choice determines both ordering scope and achievable parallelism.*

**Group ID selection is the key design decision.** A single group means total ordering and no parallelism — a serial consumer regardless of how many are running. Many groups mean ordering where it matters and parallelism everywhere else. Using a customer ID, an order ID, or an account ID is typical.

**Deduplication** works two ways. An explicit `MessageDeduplicationId` suppresses duplicates with the same ID within a five-minute window. Alternatively, content-based deduplication hashes the body. Either way, **the window is five minutes and is not configurable** — a genuine duplicate arriving six minutes later is delivered.

**Throughput is limited.** 300 messages per second, or 3,000 with batching. High-throughput mode raises this substantially by relaxing deduplication scope to the message group. Still, a workload needing tens of thousands of messages per second is not a FIFO workload.

---

## Choosing

**Use standard** unless ordering or deduplication is genuinely required. It is faster, cheaper, and simpler, and idempotent consumers are good practice regardless.

**Use FIFO** when order genuinely matters — a sequence of state changes to one entity, financial transactions, or an event stream where out-of-order processing produces incorrect results.

The question worth asking: **is ordering actually required, or does it just feel safer?** Many systems assumed to need ordering do not, because each message is independent. A confirmation email and a shipping notification for different orders have no ordering relationship at all.

Where only *some* messages need ordering, the answer is usually FIFO with a well-chosen group ID rather than a single global sequence.

---

## Exactly Once Is Narrower Than It Sounds

FIFO's "exactly once processing" means SQS will not *deliver* a duplicate within the deduplication window. It does not mean a consumer processes a message exactly once.

A consumer that receives a message, processes it, and crashes before deleting it will see that message again after its visibility timeout — from the same delivery, not a duplicate one.

**So idempotency is still required on FIFO queues.** It is a smaller problem than on standard queues, and it is not eliminated. Any design relying on a message being processed exactly once, with no idempotency, is relying on something no queue provides.

---

## Key Takeaways

- Standard queues offer best-effort ordering and at-least-once delivery with effectively unlimited throughput.
- FIFO queues offer strict ordering within a message group and deduplication within a fixed five-minute window.
- `MessageGroupId` determines both ordering scope and parallelism — one group means serial processing.
- FIFO throughput is limited to 300 messages per second, or 3,000 batched, unless high-throughput mode is enabled.
- Queue type is fixed at creation and FIFO queue names must end in `.fifo`.
- Use standard unless ordering is genuinely required, since many workloads only feel like they need it.
- FIFO's exactly-once guarantee covers delivery, not processing — consumers still need idempotency.
