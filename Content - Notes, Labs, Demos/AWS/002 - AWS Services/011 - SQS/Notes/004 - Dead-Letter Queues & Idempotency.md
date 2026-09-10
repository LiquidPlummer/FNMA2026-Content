# Dead-Letter Queues & Idempotency

What happens to messages that cannot be processed, and why every consumer must tolerate seeing a message more than once.

---

## The Poison Message Problem

A message that always fails — malformed JSON, a reference to a deleted record, an unhandled edge case — is retried forever. Each retry consumes capacity, fills logs, and delays the messages behind it. A single bad message can stall a queue indefinitely.

A **dead-letter queue (DLQ)** is where such messages go after a set number of attempts.

---

## Configuring a DLQ

A redrive policy on the source queue names the DLQ and a maximum receive count:

```json
{
  "deadLetterTargetArn": "arn:aws:sqs:us-east-1:123456789012:orders-dlq",
  "maxReceiveCount": 5
}
```

*After five receives without deletion, the message moves to the DLQ. The count is receives, not failures — a message received and abandoned by a crashing consumer counts.*

Choosing `maxReceiveCount`:

- **Too low** — transient failures send valid messages to the DLQ.
- **Too high** — genuinely bad messages are retried too long.
- **3 to 5** is typical, tuned to how transient the expected failures are.

Two configuration points: **the DLQ must be the same type as the source** — a FIFO queue needs a FIFO DLQ — and its **retention should be longer than the source queue's**, since a message may sit there for days before anyone investigates.

---

## The DLQ Is Not the End

A DLQ with messages in it is a signal that something needs attention. Two practices make it useful rather than a place messages disappear into:

**Alarm on `ApproximateNumberOfMessagesVisible` for the DLQ.** Any message arriving there is worth knowing about, and a DLQ nobody watches is equivalent to discarding the messages.

**Redrive after fixing the cause.** SQS supports moving messages back to the source queue:

```bash
aws sqs start-message-move-task \
  --source-arn arn:aws:sqs:us-east-1:123456789012:orders-dlq \
  --destination-arn arn:aws:sqs:us-east-1:123456789012:orders
```

*Moves messages back for reprocessing after the bug is fixed — which is what makes a DLQ a holding area rather than a discard pile.*

---

## Idempotency

**SQS guarantees at-least-once delivery.** A message may be processed more than once, for several reasons:

- Standard queues can deliver duplicates.
- A consumer crashing after processing but before deleting causes redelivery.
- A visibility timeout shorter than processing time causes concurrent processing.
- A DLQ redrive reprocesses messages.

**This is normal operation, not an error condition.** Any consumer with side effects must tolerate it.

### Implementing it

The general pattern is a deduplication record keyed by something stable:

```python
def handler(event, context):
    for record in event["Records"]:
        message_id = record["messageId"]
        try:
            dedup_table.put_item(
                Item={"MessageId": message_id, "TTL": int(time.time()) + 86400},
                ConditionExpression="attribute_not_exists(MessageId)",
            )
        except ClientError as e:
            if e.response["Error"]["Code"] == "ConditionalCheckFailedException":
                continue                      # already processed
            raise
        process(json.loads(record["body"]))
```

*A conditional write claims the message atomically; a duplicate finds the record present and skips. The TTL expires records automatically at no capacity cost.*

**Choosing the key matters.** `messageId` is unique per message and works for SQS duplicates. A business identifier — an order ID, a transaction ID — is better where the same logical operation might arrive as two different messages.

**Some operations are naturally idempotent** and need no deduplication at all: setting a value (rather than incrementing it), writing to a known key, deleting something. Where an operation can be expressed that way, that is simpler and more robust than a deduplication table.

**The ordering trade-off:** claiming before processing risks losing an operation if the consumer then crashes; processing before claiming risks duplication. Which is safer depends on whether a missed operation or a repeated one is worse — for a payment, duplication is worse; for a notification, a miss usually is.

---

## Monitoring

| Metric | Meaning |
|---|---|
| `ApproximateNumberOfMessagesVisible` | Backlog — a growing value means consumers are behind |
| `ApproximateAgeOfOldestMessage` | **The best single health signal** |
| `NumberOfMessagesSent` / `Received` | Producer and consumer rates |
| `ApproximateNumberOfMessagesNotVisible` | In flight |

**`ApproximateAgeOfOldestMessage` is the metric to alarm on.** Queue depth alone is ambiguous — a thousand messages processed in ten seconds is fine, while ten messages sitting for an hour is not. Age captures the thing that actually matters.

---

## Key Takeaways

- A dead-letter queue receives messages after `maxReceiveCount` receives without deletion, preventing a poison message from stalling the queue.
- The count is receives, not failures, so a crashing consumer increments it.
- A DLQ must match the source queue type and should have longer retention.
- Alarm on DLQ depth and redrive messages after fixing the cause.
- At-least-once delivery means consumers with side effects must be idempotent.
- Use a conditional write on `messageId` or a business identifier, with a TTL to expire records.
- Naturally idempotent operations avoid the need for a deduplication table entirely.
- Alarm on `ApproximateAgeOfOldestMessage` rather than queue depth, which is ambiguous on its own.
