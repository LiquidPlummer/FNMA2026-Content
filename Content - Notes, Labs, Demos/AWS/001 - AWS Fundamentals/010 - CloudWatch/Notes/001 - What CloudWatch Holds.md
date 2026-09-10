# What CloudWatch Holds

CloudWatch is three loosely related things sharing a name and a console. Separating them makes the service far easier to work with, because each has different behavior, different costs, and different setup requirements.

---

## The Three Things

**Metrics** — numeric time-series data. CPU utilization, request count, queue depth, error rate. Each data point is a number with a timestamp and a set of dimensions.

**Logs** — text emitted by applications and services. Lambda output, application logs, VPC flow logs, CloudTrail events. Unstructured or semi-structured, and searched rather than plotted.

**Events** (now largely **EventBridge**) — notifications that something happened. An instance changed state, a scheduled rule fired, a build completed. EventBridge is covered as its own topic later.

They connect at the edges — a log pattern can produce a metric, a metric alarm can emit an event — but they are separate subsystems.

---

## Metrics

A metric is identified by a **namespace**, a **name**, and **dimensions**:

```
Namespace:  AWS/EC2
Name:       CPUUtilization
Dimensions: InstanceId=i-0abc123
```

*Dimensions make the metric specific. The same metric name with a different `InstanceId` is a different time series.*

Three properties shape how metrics are used:

**Dimensions define separate series.** `CPUUtilization` for instance A and instance B are distinct metrics, not one metric filtered two ways. This is why a metric for a fleet cannot be produced simply by ignoring the dimension.

**Granularity varies.** Basic monitoring publishes every 5 minutes; detailed monitoring every 1 minute, at additional cost. Custom metrics can go to 1 second (high resolution).

**Retention is automatic and tiered.** Data ages into coarser aggregates — 1-minute data is kept 15 days, 5-minute data 63 days, 1-hour data 15 months. Older data is not deleted, but fine granularity is lost, so a 1-minute spike from six months ago cannot be recovered.

---

## Logs

Logs are organized in two levels:

- A **log group** is a named container, usually per application or service — `/aws/lambda/order-processor`.
- A **log stream** is a sequence of events from one source, usually per instance or per function execution environment.

Retention is set **per log group** and **defaults to Never Expire**. That default is the most expensive thing in CloudWatch, and it is covered in its own note.

---

## What It Costs

Costs are per-subsystem, and the shape differs:

| Item | Rough cost |
|---|---|
| Log ingestion | ~$0.50 per GB |
| Log storage | ~$0.03 per GB per month |
| Custom metrics | ~$0.30 per metric per month |
| Metric API calls | Per 1,000 requests |
| Alarms | ~$0.10 per alarm per month |
| Logs Insights queries | ~$0.005 per GB scanned |
| Dashboards | Free up to 3, then per dashboard |

Two of these dominate real bills. **Log ingestion** is charged per gigabyte, so verbose debug logging in production is directly expensive. And **custom metrics are charged per unique dimension combination** — a metric dimensioned by user ID or request ID creates a separate metric per value, and thousands of them appear as a large, surprising line item. High-cardinality dimensions are the classic CloudWatch cost mistake.

---

## What Is Not Included

Worth knowing so the gaps are not discovered during an incident:

- **Memory and disk usage on EC2 are not collected by default.** The hypervisor cannot see inside the instance, so the CloudWatch agent must be installed.
- **Application logs are not collected by default** on EC2, for the same reason.
- **Traces are a different service.** Distributed tracing is X-Ray.
- **Real user monitoring** is CloudWatch RUM, configured separately.

---

## Key Takeaways

- CloudWatch is three subsystems: metrics (numeric time series), logs (text output), and events (now EventBridge).
- Metrics are identified by namespace, name, and dimensions, with each dimension combination forming its own series.
- Metric resolution degrades with age — 1-minute data is retained 15 days, then aggregated coarser.
- Logs are organized into log groups and streams, and retention defaults to never expiring.
- Log ingestion and high-cardinality custom metrics are the two main cost drivers.
- EC2 memory, disk, and application logs require the CloudWatch agent; they are not collected by default.
