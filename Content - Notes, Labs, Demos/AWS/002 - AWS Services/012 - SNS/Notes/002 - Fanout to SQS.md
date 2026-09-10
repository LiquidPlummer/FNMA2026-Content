# Fanout to SQS

Subscribing SQS queues to an SNS topic combines the strengths of both: SNS broadcasts, and each queue gives its consumer durability and pace control. It is the most important SNS pattern and worth understanding as a solution to a specific problem.

---

## The Problem It Solves

SNS delivering directly to Lambda functions works until something is unavailable:

```
Topic ──► Lambda A     ← succeeds
      ──► Lambda B     ← throttled or failing → message eventually lost
      ──► Lambda C     ← succeeds
```

*SNS does not store messages. If B exhausts its retries, its copy is gone, while A and C processed theirs. The system is now inconsistent, with no backlog to recover from.*

Direct delivery also means each subscriber must handle the publish rate. A burst of ten thousand messages arrives as ten thousand near-simultaneous invocations, and a subscriber that cannot keep up has no way to slow down.

---

## The Pattern

Put a queue in front of each consumer:

```
                     ┌──► SQS queue A ──► Lambda A
Publisher ──► Topic ─┼──► SQS queue B ──► Lambda B
                     └──► SQS queue C ──► Lambda C
```

*Each subscriber gets its own durable buffer. A consumer being down means its queue grows; when it returns, it works through the backlog. Nothing is lost, and each consumer processes at its own rate.*

What this adds over direct subscription:

**Durability.** Messages persist up to 14 days per queue.

**Independent failure.** One consumer failing does not affect the others.

**Rate control.** Each consumer polls at its own pace; the burst sits in the queue.

**Retry and dead-lettering per consumer**, with independently tuned policies.

**Replay.** A DLQ redrive reprocesses failed messages after a fix.

The cost is another component per subscriber, a small amount of latency, and SQS request charges. For anything where losing a message matters, it is the right trade.

---

## Setting It Up

Two things must be configured, and the second is where this usually goes wrong.

**Subscribe the queue to the topic:**

```bash
aws sns subscribe --topic-arn arn:aws:sns:us-east-1:123456789012:orders \
  --protocol sqs \
  --notification-endpoint arn:aws:sqs:us-east-1:123456789012:orders-email \
  --attributes RawMessageDelivery=true
```

**Allow SNS to write to the queue** via the queue's resource policy:

```json
{
  "Effect": "Allow",
  "Principal": { "Service": "sns.amazonaws.com" },
  "Action": "sqs:SendMessage",
  "Resource": "arn:aws:sqs:us-east-1:123456789012:orders-email",
  "Condition": {
    "ArnEquals": { "aws:SourceArn": "arn:aws:sns:us-east-1:123456789012:orders" }
  }
}
```

*Without this policy the subscription appears active and no messages arrive — the most common failure in this setup. The `SourceArn` condition prevents any other topic from writing to the queue.*

---

## Raw Message Delivery

By default, SNS wraps the message in an envelope:

```json
{
  "Type": "Notification",
  "MessageId": "...",
  "TopicArn": "arn:aws:sns:...",
  "Message": "{\"orderId\":\"O-5501\"}",
  "MessageAttributes": { ... }
}
```

*The actual payload is a **string** inside `Message`, requiring the consumer to parse twice.*

**`RawMessageDelivery=true`** removes the envelope, delivering the payload directly:

```python
# With RawMessageDelivery
body = json.loads(record["body"])

# Without it
body = json.loads(json.loads(record["body"])["Message"])
```

*Raw delivery is simpler and makes the consumer independent of whether the message came via SNS or directly.*

The trade: raw delivery moves message attributes into SQS message attributes rather than the body, and removes SNS metadata such as `MessageId` and `Timestamp`. Where that metadata is needed, keep the envelope.

---

## Cross-Account Fanout

The pattern extends across accounts, which is how event-driven architectures span team boundaries: a topic in one account, queues in others, each account owning its own consumer.

This requires the topic policy to allow subscription from those accounts and each queue policy to allow the topic. It also means the topic becomes a published contract — changing its message format affects consumers in accounts we may not control, which is a coordination problem rather than a technical one.

---

## When Direct Subscription Is Fine

The queue is not always warranted:

- **Email and SMS** subscriptions, which have no consumer to buffer for.
- **Notifications where loss is acceptable** — a dashboard update, a non-critical alert.
- **Low volume with a reliable consumer**, where the added component is not worth it.

The question is whether losing a message would matter. If yes, add the queue.

---

## Key Takeaways

- SNS does not persist messages, so a failing direct subscriber loses its copy while others succeed.
- Subscribing an SQS queue per consumer gives each one durability, independent failure handling, and rate control.
- The queue's resource policy must allow the SNS service principal, or the subscription silently delivers nothing.
- Use an `aws:SourceArn` condition so only the intended topic can write to the queue.
- `RawMessageDelivery=true` removes the SNS envelope so consumers parse once, at the cost of SNS metadata.
- The pattern works across accounts, which makes the topic a published contract to coordinate.
- Direct subscription is fine for email, SMS, and notifications where message loss is acceptable.
