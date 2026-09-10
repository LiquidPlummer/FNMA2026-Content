# Buses, Rules & Targets

**EventBridge** routes events from sources to targets based on their content. It is the third messaging service, and it differs from SNS and SQS in that routing decisions are made on what the event contains.

---

## The Three Objects

**An event bus** receives events. Every account has a **default bus** that AWS services publish to automatically, and custom buses can be created for application events.

**A rule** matches events on the bus and routes them. It contains an event pattern — or a schedule — and a list of targets.

**A target** is what receives a matched event. Over 20 AWS services are supported.

```
Event ──► Event bus ──┬── Rule A (pattern) ──► Lambda function
                      ├── Rule B (pattern) ──► SQS queue
                      └── Rule C (pattern) ──► Step Functions
```

*One event can match several rules and be delivered to all their targets. Rules are independent — there is no ordering or precedence between them.*

---

## Event Structure

Every event has a standard envelope with a service-defined `detail`:

```json
{
  "version": "0",
  "id": "7bf73129-1428-4cd3-a780-95db273d1602",
  "detail-type": "Order Placed",
  "source": "com.acme.orders",
  "account": "123456789012",
  "time": "2026-09-09T14:22:31Z",
  "region": "us-east-1",
  "resources": [],
  "detail": {
    "orderId": "O-5501",
    "customerId": "C-1001",
    "total": 249.99
  }
}
```

*The envelope fields are consistent across all events; `detail` holds the payload. `source` and `detail-type` are the fields rules most often match on.*

Publishing a custom event:

```python
events.put_events(Entries=[{
    "Source": "com.acme.orders",
    "DetailType": "Order Placed",
    "Detail": json.dumps({"orderId": "O-5501", "total": 249.99}),
    "EventBusName": "orders-bus",
}])
```

*`Detail` is a JSON **string**, not an object — a frequent mistake that produces events which never match a pattern.*

---

## Targets

The breadth of targets is EventBridge's distinguishing feature:

| Target | Typical use |
|---|---|
| Lambda | Process the event |
| SQS | Durable buffering |
| SNS | Further fanout |
| Step Functions | Start a workflow |
| ECS task | Run a container |
| Kinesis | Stream ingestion |
| API destination | Call an external HTTP API |
| Another event bus | Cross-account or cross-region routing |
| Systems Manager | Run a command |

**API destinations** deserve attention: EventBridge can call an external HTTPS endpoint with managed authentication, rate limiting, and retries. That turns an event into a webhook to a third-party service with no Lambda function in between.

A rule can have **up to five targets**, each receiving the same event.

---

## Input Transformation

A target need not receive the raw event. **Input transformers** reshape it:

```json
{
  "InputPathsMap": {
    "orderId": "$.detail.orderId",
    "total": "$.detail.total"
  },
  "InputTemplate": "{\"id\": \"<orderId>\", \"amount\": <total>, \"source\": \"eventbridge\"}"
}
```

*Extracts fields and builds a custom payload, so the target receives what it expects rather than the EventBridge envelope.*

This matters when the target is an existing API or a function with its own input contract.

---

## How It Differs From SNS and SQS

**Routing is on content.** SNS filters per subscription; EventBridge matches patterns against the whole event, including nested fields, in the rule itself.

**AWS services publish to it automatically.** The default bus already carries EC2 state changes, Auto Scaling events, CodePipeline transitions, and much more — with nothing to configure.

**It has more target types**, including several that would otherwise need a Lambda function to reach.

**It has an event archive and replay**, which neither SNS nor SQS provides.

The trade-offs: **higher latency** than SNS (typically under a second, but not the sub-100ms of SNS), **lower throughput limits**, and **no ordering guarantee at all**.

---

## Key Takeaways

- EventBridge routes events from buses to targets using content-based rules.
- Every account has a default bus that AWS services publish to automatically; custom buses hold application events.
- Events share a standard envelope, with `source` and `detail-type` being the usual match fields.
- `Detail` must be a JSON string when publishing, not an object.
- Rules support up to five targets across more than 20 service types, including API destinations for external HTTP calls.
- Input transformers reshape the event so targets receive their expected payload.
- EventBridge routes on content, has native AWS event sources, and supports archive and replay.
- It has higher latency and lower throughput than SNS, and provides no ordering guarantee.
