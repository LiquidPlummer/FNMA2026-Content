# Visibility Timeout & Polling

Two settings that determine whether messages are processed once or repeatedly, and how much polling costs.

---

## Visibility Timeout

When a consumer receives a message, it is not deleted — it becomes **invisible** to other consumers for the **visibility timeout**. The consumer must explicitly delete it after processing:

```
Receive ──► message hidden for N seconds ──► process ──► DeleteMessage
                                          └─► no delete? becomes visible again
```

*Deletion is the acknowledgement. A consumer that crashes mid-processing never deletes, so the message reappears and is retried — which is the mechanism that makes SQS reliable.*

**The visibility timeout must exceed the processing time.** If processing takes 60 seconds and the timeout is 30, the message becomes visible again while still being processed, and a second consumer picks it up. Both complete, and the work happens twice.

This produces a distinctive symptom: **everything works, and every message is processed several times.** The default timeout is 30 seconds, which is shorter than many people assume.

Set it to comfortably above the worst-case processing time — two to three times the observed p99.

### Extending it in flight

For variable processing times, a consumer can extend the timeout for a message it is still working on:

```python
sqs.change_message_visibility(
    QueueUrl=queue_url,
    ReceiptHandle=receipt_handle,
    VisibilityTimeout=300,
)
```

*A heartbeat pattern: extend periodically while processing continues, so a long message does not reappear and a crashed consumer's message reappears promptly.*

This is better than setting a very long timeout globally, which delays retry after a genuine crash.

**The receipt handle** is per receive, not per message. A message received twice has two different receipt handles, and only the current one can delete or extend it.

---

## Short vs Long Polling

**Short polling** (the default when `WaitTimeSeconds` is 0) queries a subset of servers and returns immediately — often with no messages even when the queue is not empty.

**Long polling** waits up to 20 seconds for a message to arrive before returning:

```python
response = sqs.receive_message(
    QueueUrl=queue_url,
    MaxNumberOfMessages=10,
    WaitTimeSeconds=20,
)
```

*Waits for a message rather than returning empty immediately.*

**Long polling is better in essentially every respect:**

**Fewer requests, less cost.** Short polling in a tight loop generates a continuous stream of billed empty receives. A consumer polling every 100 ms makes 864,000 requests a day per consumer, nearly all empty. Long polling reduces that to a handful per minute.

**Lower latency.** Counter-intuitively — long polling returns as soon as a message arrives, while short polling waits for the next loop iteration.

**No false empties.** Short polling can return nothing while messages exist, because it samples a subset of servers.

Set `ReceiveMessageWaitTimeSeconds` to 20 on the queue itself, so every consumer gets long polling without each having to request it.

---

## Batching

`MaxNumberOfMessages` retrieves up to 10 messages per call, and `SendMessageBatch` sends up to 10.

Batching reduces request count and therefore cost, and it complicates error handling: a batch of 10 where message 7 fails means deciding what happens to 1–6 and 8–10. The answer is per-message deletion — delete each message as it succeeds, so failures leave only the failed messages to reappear.

---

## Message Retention and Delay

**Retention** defaults to 4 days and can be set from 60 seconds to 14 days. A message not consumed within that period is deleted permanently. A long retention gives more time to fix a broken consumer before losing anything.

**Delivery delay** postpones a message's first visibility, up to 15 minutes — set per queue or per message. It is useful for simple scheduling and for backing off a retry.

For delays beyond 15 minutes, EventBridge Scheduler or Step Functions are the right tools.

---

## Key Takeaways

- Receiving a message hides it for the visibility timeout; deleting it is the acknowledgement.
- A visibility timeout shorter than processing time causes every message to be processed multiple times.
- Set the timeout to two or three times observed p99 processing time.
- Extend visibility in flight with a heartbeat rather than setting a very long global timeout.
- Receipt handles are per receive, and only the current one can delete or extend a message.
- Long polling with `WaitTimeSeconds=20` is cheaper, lower latency, and avoids false empty responses.
- Set `ReceiveMessageWaitTimeSeconds` on the queue so all consumers get long polling by default.
- Delete messages individually within a batch so a single failure does not affect the rest.
