# Metrics

Some metrics arrive without any configuration; others require an agent or explicit publishing. The dividing line follows a simple principle, and knowing it prevents building an alarm on a metric that does not exist.

---

## The Dividing Line

**AWS publishes what it can see from outside the resource. Anything inside is ours to report.**

The hypervisor observes an EC2 instance's CPU scheduling, its network interface, and its EBS I/O. It cannot see the operating system's memory allocation, its filesystem usage, or its process list — those exist inside a virtual machine the hypervisor deliberately does not inspect.

This explains the most common surprise in CloudWatch: **there is no memory metric for EC2 by default.**

---

## Metrics Available for Free

| Service | Examples |
|---|---|
| EC2 | `CPUUtilization`, `NetworkIn/Out`, `DiskReadBytes`, `StatusCheckFailed` |
| EBS | `VolumeReadOps`, `VolumeQueueLength`, `BurstBalance` |
| ALB | `RequestCount`, `TargetResponseTime`, `HTTPCode_Target_5XX_Count`, `HealthyHostCount` |
| RDS | `CPUUtilization`, `DatabaseConnections`, `FreeStorageSpace`, `ReadLatency` |
| Lambda | `Invocations`, `Errors`, `Duration`, `Throttles`, `ConcurrentExecutions` |
| SQS | `ApproximateNumberOfMessagesVisible`, `ApproximateAgeOfOldestMessage` |
| DynamoDB | `ConsumedReadCapacityUnits`, `ThrottledRequests`, `SuccessfulRequestLatency` |

Managed services generally expose more than EC2 does, because AWS operates the software and can see inside it. RDS reports free storage and connection counts; a self-managed database on EC2 reports neither.

Two EC2 metrics deserve particular attention. `StatusCheckFailed_System` indicates AWS infrastructure trouble, where recovery means stop/start to move to another host, while `StatusCheckFailed_Instance` indicates a problem within the instance itself. And `BurstBalance` on gp2 volumes predicts the performance collapse that follows exhausting I/O credits.

---

## Metrics Requiring the Agent

The **CloudWatch agent** runs on the instance and reports what the hypervisor cannot see:

- Memory used and available
- Disk space used per filesystem
- Swap usage
- Per-process metrics
- Custom application metrics via StatsD or collectd

```json
{
  "metrics": {
    "append_dimensions": { "InstanceId": "${aws:InstanceId}" },
    "metrics_collected": {
      "mem": { "measurement": ["mem_used_percent"] },
      "disk": {
        "measurement": ["used_percent"],
        "resources": ["/", "/data"]
      }
    }
  }
}
```

*Agent configuration collecting memory and disk usage, dimensioned by instance ID. Both are unavailable without it.*

The agent needs an instance profile granting `cloudwatch:PutMetricData` and, for logs, the log permissions as well. It also handles both metrics and logs, so it is installed once for both purposes.

Metrics from the agent are **custom metrics** and are billed as such — roughly $0.30 per metric per month. Collecting memory and disk across a large fleet is a real cost, and each dimension combination counts separately.

---

## Basic vs Detailed Monitoring

EC2 publishes at 5-minute intervals by default. **Detailed monitoring** raises this to 1 minute for an additional charge.

It matters when reaction time matters. A 5-minute period means an alarm requiring two consecutive breaching periods takes at least ten minutes to fire — too slow for an Auto Scaling policy responding to a traffic spike. Detailed monitoring is generally worthwhile for production Auto Scaling groups and unnecessary for steady-state batch instances.

---

## Custom Metrics

Applications publish their own metrics with `PutMetricData`:

```python
cloudwatch.put_metric_data(
    Namespace="OrderService",
    MetricData=[{
        "MetricName": "OrdersProcessed",
        "Value": 1,
        "Unit": "Count",
        "Dimensions": [{"Name": "Environment", "Value": "production"}],
    }],
)
```

*Publishes a business metric. Dimensions should be low-cardinality — `Environment` has a handful of values, whereas an order ID would create a separate billed metric per order.*

Reasons to publish a custom metric:

- **Business measures** — orders placed, signups, revenue — which correlate technical incidents with business impact.
- **Application state** invisible from outside, such as an internal queue depth or cache hit rate.
- **Derived measures** the application computes and AWS could not.

Two cost warnings. **Cardinality is the trap**: a dimension with thousands of distinct values creates thousands of metrics, each billed monthly. And **`PutMetricData` calls are billed**, so publish in batches (up to 1,000 data points per call) rather than one call per event.

For Lambda specifically, **Embedded Metric Format** avoids the API call entirely: writing specially structured JSON to the log causes CloudWatch to extract metrics from it, which is both cheaper and non-blocking.

---

## Key Takeaways

- AWS publishes metrics it can observe from outside a resource; anything inside the operating system requires the agent.
- There is no default EC2 memory or disk-space metric — this is the most common gap people encounter.
- Managed services expose more metrics than EC2 because AWS operates the software.
- `StatusCheckFailed_System` and `StatusCheckFailed_Instance` distinguish AWS infrastructure faults from instance faults.
- Detailed monitoring gives 1-minute granularity and matters where alarm reaction time does.
- Custom metrics are billed per unique dimension combination, so high-cardinality dimensions are expensive.
- Batch `PutMetricData` calls, and use Embedded Metric Format from Lambda to avoid the API call altogether.
