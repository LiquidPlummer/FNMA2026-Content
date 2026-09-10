# Publish/Subscribe

**SNS** delivers one message to many subscribers. Where a queue gives a message to one consumer, a topic gives it to all of them — which is a different problem being solved.

---

## The Model

A publisher sends to a **topic**. Every **subscription** on that topic receives a copy:

```
                     ┌──► Email subscription
Publisher ──► Topic ─┼──► SQS queue
                     ├──► Lambda function
                     └──► HTTPS endpoint
```

*One publish, four deliveries. The publisher knows nothing about the subscribers, and adding one requires no change to the publisher.*

That last property is the point. A new consumer of "order placed" is a new subscription, not a code change in the order service.

---

## Push, Not Pull

The operational difference from SQS.

**SNS pushes.** It delivers to each subscriber as the message arrives — invoking the Lambda function, making the HTTP request, sending the email.

**SQS pulls.** Consumers poll.

This produces the trade-off that shapes when each is right:

| | SNS | SQS |
|---|---|---|
| Delivery | Push, immediate | Pull, when ready |
| Consumers per message | Many | One |
| Persistence | None — delivered or retried, then dropped | Up to 14 days |
| Absent consumer | Delivery fails and is retried, then lost | Message waits |
| Rate control | Subscriber must handle the rate | Consumer sets its own |

**SNS does not store messages.** If a subscriber is unavailable through the retry policy, the message is gone. There is no backlog to work through when it returns.

This is the central limitation, and the reason for the pattern in the next note.

---

## Message Structure

```python
sns.publish(
    TopicArn=topic_arn,
    Subject="Order placed",
    Message=json.dumps({"orderId": "O-5501", "total": 249.99}),
    MessageAttributes={
        "eventType": {"DataType": "String", "StringValue": "order.placed"},
        "region":    {"DataType": "String", "StringValue": "us-east-1"},
    },
)
```

*Message attributes are metadata used for subscription filtering — they allow subscribers to receive only relevant messages, which is covered separately.*

**Message structure can vary by protocol.** With `MessageStructure="json"`, different content can be sent to different protocols — a short SMS and a longer email from one publish.

Messages are limited to **256 KB**, as with SQS.

---

## Subscription Protocols

| Protocol | Delivers to |
|---|---|
| **SQS** | A queue — the most important, covered next |
| **Lambda** | Direct function invocation |
| **HTTP/HTTPS** | A webhook endpoint |
| **Email** | Plain text or JSON |
| **SMS** | Text message |
| **Mobile push** | APNs, FCM |
| **Kinesis Data Firehose** | Delivery to S3, Redshift, and others |

**HTTP subscriptions require confirmation.** SNS sends a `SubscriptionConfirmation` message containing a URL, and the endpoint must visit it before deliveries begin. A subscription stuck in `PendingConfirmation` means the endpoint did not handle it — a common setup problem.

**Email subscriptions require a human to click a link**, which makes them unsuitable for automated provisioning.

---

## Where SNS Fits

**Fanout to several systems.** One event, several independent reactions.

**Alerting.** CloudWatch alarms publish to SNS, which delivers to email, chat, or an incident system.

**Mobile push notifications**, which SNS handles across platforms.

**Decoupling a publisher from an unknown set of consumers.**

Where it fits poorly: anything needing durability, ordering, or consumer-controlled rate. Those are queue properties, and combining SNS with SQS is how they are obtained.

---

## Key Takeaways

- SNS delivers each published message to every subscription on a topic, and the publisher knows nothing about subscribers.
- SNS pushes to subscribers; SQS is polled by consumers.
- SNS does not persist messages — a subscriber unavailable through the retry policy loses the message.
- Message attributes carry metadata used for subscription filtering.
- Messages are limited to 256 KB, and content can vary per protocol.
- HTTP subscriptions must confirm via a URL SNS sends, and email subscriptions require a human to click a link.
- SNS suits fanout, alerting, and mobile push; durability, ordering, and rate control require SQS.
