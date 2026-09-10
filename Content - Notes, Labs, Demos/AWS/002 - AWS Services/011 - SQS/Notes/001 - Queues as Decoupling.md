# Queues as Decoupling

A queue lets a producer hand off work without waiting for a consumer. That single property produces most of the benefits of message-based architecture.

---

## The Problem

Direct synchronous calls couple two systems tightly:

```
Order service ──HTTP──► Email service
```

*If the email service is down, slow, or overloaded, the order fails or hangs. The order service's availability is now bounded by the email service's.*

Every dependency added this way multiplies the failure surface. A service calling five others is available only when all five are.

---

## With a Queue

```
Order service ──► SQS queue ──► Email service
```

*The order service writes a message and returns. The email service consumes when it can.*

What this changes:

**They do not have to run at the same time.** The consumer can be down, restarting, or deploying. Messages accumulate and are processed when it returns.

**Load is absorbed.** A burst of a thousand orders enqueues a thousand messages. The consumer processes at its own rate, and the queue holds the difference — the standard remedy for a downstream system that cannot scale as fast as its producer.

**Failures are isolated.** The email service failing does not fail orders.

**They scale independently.** Consumer count is driven by queue depth, not by producer behavior.

**Retries are built in.** A message not deleted becomes visible again and is redelivered.

---

## What It Costs

Being honest about the trade:

**The result is not immediate.** The producer knows the message was accepted, not that the work succeeded. Anything needing a result now cannot use a queue.

**Reporting failures is harder.** If email sending fails three messages later, the user's request has long since completed. Communicating that requires another mechanism.

**Ordering is not guaranteed** on standard queues.

**Duplicates happen**, so consumers must be idempotent.

**There is another component** to monitor, secure, and reason about.

**Debugging spans a boundary.** Tracing a request through a queue requires correlation IDs carried in the message.

---

## When a Queue Fits

**Good candidates:** sending email, generating reports or thumbnails, syncing to an external system, processing uploads, anything expensive that the user need not wait for.

The test is whether the caller genuinely needs the result before responding. Sending a confirmation email does not need to complete before the order response returns — and making it synchronous means an email provider outage becomes an order outage.

**Poor candidates:** anything whose result the caller needs, and anything requiring strict global ordering at high throughput.

---

## SQS Specifically

**SQS** is a managed queue: no brokers, no partitions, no capacity planning. It scales automatically, replicates across Availability Zones, and is billed per request.

Its characteristics:

- **Pull-based.** Consumers poll; SQS does not push. Lambda integration polls on our behalf.
- **One consumer per message.** A message read by one consumer is hidden from others while being processed. Broadcasting to several consumers requires SNS or EventBridge.
- **Messages persist** up to 14 days, with a default retention of 4 days.
- **256 KB maximum** message size.
- **No message browsing.** Messages cannot be inspected without receiving them, which makes debugging different from a broker with a queue browser.

```python
sqs.send_message(
    QueueUrl=queue_url,
    MessageBody=json.dumps({"orderId": "O-5501", "type": "confirmation"}),
    MessageAttributes={"correlationId": {"StringValue": cid, "DataType": "String"}},
)
```

*A correlation ID in message attributes is what makes tracing across the queue boundary possible — worth including from the start.*

---

## Key Takeaways

- A queue decouples producer and consumer in time, so they need not run simultaneously.
- It absorbs bursts, isolates failures, and allows independent scaling.
- The cost is that results are not immediate, failure reporting needs another mechanism, and debugging crosses a boundary.
- Use a queue when the caller does not need the result before responding.
- SQS is pull-based with one consumer per message; broadcasting requires SNS or EventBridge.
- Messages are up to 256 KB and retained up to 14 days, with a 4-day default.
- Messages cannot be browsed without receiving them.
- Include a correlation ID in every message so requests can be traced across the queue.
