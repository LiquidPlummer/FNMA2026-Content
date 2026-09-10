# Delivery Retries & FIFO Topics

What SNS does when a delivery fails, and the FIFO variant that adds ordering and deduplication.

---

## Retry Policies Differ by Protocol

SNS retries failed deliveries, and the policy depends on the subscriber type.

**AWS-managed endpoints** — SQS, Lambda, Kinesis Data Firehose — get an aggressive built-in policy: immediate retries, then exponential backoff, for a total of over 100,000 attempts across roughly 23 days. Delivery to these is effectively guaranteed short of a configuration problem.

**HTTP/HTTPS endpoints** get a configurable policy, defaulting to three retries over about 20 seconds. This is short, and it is the default that surprises people — a webhook endpoint briefly unavailable loses messages.

```json
{
  "healthyRetryPolicy": {
    "numRetries": 50,
    "minDelayTarget": 5,
    "maxDelayTarget": 300,
    "backoffFunction": "exponential"
  }
}
```

*A delivery policy on an HTTP subscription giving 50 retries with growing delays — far more resilient than the default three.*

**Email and SMS** deliveries are attempted once or a few times with no configurable policy.

---

## Delivery Status and Failures

**Delivery status logging** is off by default and records successes and failures to CloudWatch Logs. Enabling it is how delivery problems become visible — without it, a failing subscription is silent.

**A dead-letter queue can be attached to a subscription**, receiving messages that exhaust their retries:

```bash
aws sns set-subscription-attributes \
  --subscription-arn arn:aws:sns:...:subscription/... \
  --attribute-name RedrivePolicy \
  --attribute-value '{"deadLetterTargetArn":"arn:aws:sqs:us-east-1:123456789012:sns-dlq"}'
```

*A per-subscription DLQ. Without one, a message exhausting retries is discarded with no record beyond delivery logs.*

**Every production subscription should have a DLQ or use the SQS fanout pattern.** Both address the same problem — SNS not storing messages — and either is better than silent loss.

---

## Metrics Worth Watching

| Metric | Meaning |
|---|---|
| `NumberOfMessagesPublished` | Publish rate |
| `NumberOfNotificationsDelivered` | Successful deliveries |
| `NumberOfNotificationsFailed` | **Failures after retries** |
| `NumberOfNotificationsFilteredOut` | Excluded by filter policies |

`NumberOfNotificationsFailed` is the one to alarm on. Comparing delivered against published per subscription also reveals a subscriber quietly failing.

---

## FIFO Topics

**FIFO topics** provide strict ordering and deduplication, mirroring FIFO queues:

```python
sns.publish(
    TopicArn=fifo_topic_arn,
    Message=json.dumps({"orderId": "O-5501", "event": "shipped"}),
    MessageGroupId="O-5501",
    MessageDeduplicationId="O-5501-shipped-v1",
)
```

*Ordering is guaranteed within a message group; deduplication suppresses repeats within a five-minute window.*

The constraints are significant:

**FIFO topics can only deliver to FIFO SQS queues.** No Lambda, no HTTP, no email, no mobile push. This single restriction determines most of when they are usable.

**Topic names must end in `.fifo`.**

**Throughput is limited** — 300 messages per second, or 3,000 batched.

**Ordering is per message group**, so group ID choice determines both ordering scope and parallelism.

They fit a specific case: an ordered event stream fanned out to several queue-based consumers that each need the same order. Financial transactions and entity state changes are typical.

For most fanout, standard topics are correct — higher throughput, every protocol, and consumers that can be made order-independent.

---

## Choosing Between SNS and SQS

The distinction to carry forward:

| Need | Service |
|---|---|
| One message, many consumers | SNS |
| One message, one consumer | SQS |
| Durable buffering | SQS |
| Immediate push delivery | SNS |
| Consumer-controlled pace | SQS |
| Both broadcast and durability | SNS → SQS fanout |
| Ordering with one consumer | SQS FIFO |
| Ordering with several consumers | SNS FIFO → SQS FIFO |

The third messaging service, EventBridge, adds routing on content and a large set of AWS-native event sources. The three-way comparison closes the EventBridge topic.

---

## Key Takeaways

- Retry policies differ by protocol: AWS endpoints retry for days, while HTTP endpoints default to three retries over about 20 seconds.
- Configure a longer delivery policy for HTTP subscriptions, since the default loses messages during brief outages.
- Delivery status logging is off by default and is how failures become visible.
- Attach a subscription DLQ or use SQS fanout, or messages exhausting retries are discarded silently.
- Alarm on `NumberOfNotificationsFailed`, and compare delivered against published per subscription.
- FIFO topics provide ordering and deduplication but deliver only to FIFO SQS queues.
- FIFO topic throughput is limited, and ordering scope follows the message group ID.
- Use SNS for broadcast, SQS for durable single-consumer delivery, and the fanout pattern for both.
