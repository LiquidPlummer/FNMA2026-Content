# Log Retention & Cost

**CloudWatch Logs retention defaults to Never Expire.** Every log group created without an explicit setting keeps everything forever, and the storage bill grows every month without anyone deciding it should.

---

## The Default

When Lambda, ECS, or any service creates a log group automatically, it is created with no retention policy. Nothing warns about this, and the cost is small at first — which is precisely why it goes unnoticed until it is large.

The arithmetic is unremarkable and adds up:

- 10 GB of logs per day
- Ingestion: 10 GB × $0.50 = $5/day, roughly $150/month, which is a steady cost
- Storage: accumulating at 300 GB/month × $0.03 = $9/month **for that month's logs alone**

After a year, storage alone is over $100/month and rising, for data nobody has read in eleven months. After three years it is several times that, and none of it is being looked at.

---

## Setting Retention

```bash
aws logs put-retention-policy \
  --log-group-name /aws/lambda/order-processor \
  --retention-in-days 30
```

*Sets 30-day retention. Events older than that are deleted automatically and permanently.*

Permitted values are fixed: 1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1096, 1827, 2192, 2557, 2922, 3288, and 3653 days.

Reasonable defaults by purpose:

| Log type | Retention | Reasoning |
|---|---|---|
| Application debug | 7–14 days | Only useful for recent investigation |
| Application production | 30–90 days | Covers most incident timelines |
| Access logs | 90 days in CloudWatch | Archive to S3 for longer |
| Audit / compliance | As required | Usually S3 with Object Lock, not CloudWatch |
| VPC Flow Logs | 7–30 days | Very high volume |

---

## Finding the Log Groups With No Retention

```bash
aws logs describe-log-groups \
  --query "logGroups[?!retentionInDays].[logGroupName,storedBytes]" \
  --output table
```

*Lists every log group with no retention policy alongside its stored size — usually a longer list than expected, since auto-created groups are never configured.*

This is worth running against any account that has been operating for a while. The typical result is dozens of groups, several of them large, all retaining data indefinitely.

---

## Preventing the Problem

**Create log groups in infrastructure code.** If the group exists with retention set before the service starts writing, the never-expire default never applies:

```yaml
OrderProcessorLogs:
  Type: AWS::Logs::LogGroup
  Properties:
    LogGroupName: /aws/lambda/order-processor
    RetentionInDays: 30
```

*Declaring the log group alongside the function means it is created with retention rather than auto-created without it.*

**Sweep periodically.** A scheduled Lambda that applies a default retention to any group lacking one keeps the problem from recurring as new services appear.

**Archive rather than retain.** Where logs must be kept for years, a subscription filter can stream them to S3, where storage is roughly a tenth the price and lifecycle rules can move them to Glacier. CloudWatch keeps a short working window; S3 holds the archive.

---

## Reducing Ingestion

Storage is the visible problem; ingestion is usually the larger cost, and it is reduced by logging less:

**Do not run debug logging in production.** The most common single cause of high ingestion, and often left enabled after an investigation.

**Sample high-volume events.** Logging 1% of successful health checks preserves the signal at a fraction of the volume. Log all failures.

**Log structured events, not prose.** A JSON object is usually smaller than a formatted sentence and far more useful.

**Do not log large payloads.** Full request and response bodies are expensive and frequently contain sensitive data that should not be there anyway.

**Check VPC Flow Logs.** They generate enormous volume. Logging rejected traffic only, rather than all traffic, cuts it dramatically while preserving most of the diagnostic value.

---

## The Query Cost

**Logs Insights charges per gigabyte scanned.** A query over 90 days of a large log group scans everything in the time range, and repeated broad queries during an incident add up. Narrowing the time range is both faster and cheaper, and it is the main reason to keep the CloudWatch window short and the archive in S3.

---

## Key Takeaways

- Log retention defaults to Never Expire, and auto-created log groups are never configured with one.
- Ingestion at roughly $0.50/GB usually costs more than storage, but storage accumulates indefinitely.
- Retention values are drawn from a fixed list; 7–30 days suits most application logs.
- Query for log groups with no `retentionInDays` — established accounts typically have many.
- Declare log groups in infrastructure code so they are created with retention already set.
- Archive to S3 for long-term retention at roughly a tenth the storage cost.
- Reduce ingestion by disabling production debug logging, sampling high-volume events, and limiting VPC Flow Logs to rejects.
- Logs Insights bills per gigabyte scanned, so narrow time ranges cost less.
