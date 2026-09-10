# Log Groups & Log Streams

CloudWatch Logs has two levels of organization and several different ways that logs arrive. Knowing which mechanism delivers which logs is what makes "where are my logs?" answerable.

---

## The Two Levels

**A log group** is a named container that owns the configuration — retention, encryption, metric filters, subscription filters. It is the level everything is set at.

**A log stream** is an ordered sequence of events from a single source within that group. Streams hold data and carry no configuration of their own.

```
Log group: /aws/lambda/order-processor
├── Stream: 2026/09/09/[$LATEST]a1b2c3d4...   ← one execution environment
├── Stream: 2026/09/09/[$LATEST]e5f6g7h8...   ← another, running concurrently
└── Stream: 2026/09/08/[$LATEST]i9j0k1l2...
```

*Lambda creates one stream per execution environment, so concurrent invocations write to different streams — which is why a single request's logs are easier to find by searching the group than by browsing streams.*

Naming follows conventions per service:

| Source | Log group |
|---|---|
| Lambda | `/aws/lambda/<function-name>` |
| ECS (awslogs driver) | Whatever the task definition specifies |
| API Gateway | `API-Gateway-Execution-Logs_<api-id>/<stage>` |
| VPC Flow Logs | Whatever the flow log specifies |
| RDS | `/aws/rds/instance/<name>/<log-type>` |

---

## How Logs Arrive

Four mechanisms, and they behave differently.

**Services write directly.** Lambda, API Gateway, Step Functions, and others write to CloudWatch Logs automatically — provided their execution role permits it. A Lambda function whose role lacks `logs:CreateLogStream` and `logs:PutLogEvents` runs correctly and produces no logs at all, which is a confusing failure because the function itself works.

**The CloudWatch agent, on EC2.** Nothing on an EC2 instance reaches CloudWatch by default. The agent is installed, configured with which files to tail, and given an instance profile with log permissions:

```json
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [{
          "file_path": "/var/log/app/application.log",
          "log_group_name": "/app/production",
          "log_stream_name": "{instance_id}"
        }]
      }
    }
  }
}
```

*Agent configuration tailing one file into a named group, with one stream per instance. The `{instance_id}` placeholder is what keeps streams separate across a fleet.*

**Container log drivers.** ECS tasks using the `awslogs` driver send container stdout and stderr directly. On EKS, Fluent Bit or a similar collector is deployed to do the same.

**The API.** `PutLogEvents` writes arbitrary events, which is how custom tooling ships logs.

---

## Structured Logging

Log events are strings, but writing them as JSON changes what can be done with them. CloudWatch Logs Insights parses JSON fields automatically, making them directly queryable:

```python
import json, logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)

logger.info(json.dumps({
    "event": "order_processed",
    "order_id": order_id,
    "duration_ms": elapsed,
    "status": "success",
}))
```

*Emitting JSON means Insights can filter on `order_id` and aggregate `duration_ms` without pattern-matching text.*

The difference is substantial. Unstructured text requires regular expressions to extract anything; structured events support `stats avg(duration_ms) by status` directly. Deciding this early is worthwhile, since retrofitting means changing every log statement.

---

## Practical Notes

**Streams are created automatically** by whatever writes to them, so they rarely need managing directly.

**Ordering is per stream.** Events within a stream are ordered; across streams, only timestamps relate them. Correlating a request across services requires a correlation ID in the events themselves.

**Sequence tokens** were historically required for `PutLogEvents` and are no longer needed — older code and documentation still reference them.

**Delivery is near-real-time**, typically within seconds, but not instantaneous. A few seconds' delay during an incident is normal and not a sign that logging is broken.

**Log groups can be created before anything writes to them.** Doing this in infrastructure code lets retention and encryption be set from the start, rather than a service auto-creating the group with the never-expire default.

---

## Key Takeaways

- Log groups hold configuration — retention, encryption, filters — while log streams hold the events.
- Lambda creates a stream per execution environment, so a single request's logs are best found by searching the group.
- Services write directly if their role permits it; a Lambda without log permissions runs silently with no output.
- EC2 requires the CloudWatch agent, since nothing on an instance reaches CloudWatch by default.
- Emitting JSON makes fields directly queryable in Logs Insights, and is much easier to adopt early than to retrofit.
- Create log groups in infrastructure code so retention and encryption are set before a service auto-creates them.
