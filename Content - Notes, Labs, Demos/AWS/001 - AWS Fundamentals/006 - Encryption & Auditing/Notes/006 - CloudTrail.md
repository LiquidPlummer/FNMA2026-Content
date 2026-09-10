# CloudTrail

**CloudTrail** records the API calls made in an account. Since every AWS action is an API call, CloudTrail is the record of everything anyone did — through the console, the CLI, an SDK, or another AWS service.

---

## What Gets Recorded

Each event captures the identity, the action, the parameters, the source, and the outcome:

```json
{
  "eventTime": "2026-09-09T14:22:31Z",
  "eventSource": "s3.amazonaws.com",
  "eventName": "DeleteBucket",
  "awsRegion": "us-east-1",
  "sourceIPAddress": "203.0.113.42",
  "userAgent": "aws-cli/2.15.30",
  "userIdentity": {
    "type": "AssumedRole",
    "arn": "arn:aws:sts::123456789012:assumed-role/DeployRole/ci-build-4471",
    "accountId": "123456789012"
  },
  "requestParameters": { "bucketName": "old-reports" },
  "responseElements": null,
  "errorCode": null
}
```

*A single event answers who, what, when, from where, and whether it succeeded. The role session name — `ci-build-4471` — is what makes a shared role attributable.*

Denied calls are recorded too, with an `errorCode` of `AccessDenied`. That makes CloudTrail as useful for debugging permissions as for security review.

---

## Three Kinds of Event

**Management events** — control-plane operations: creating an instance, changing a policy, deleting a bucket. These are on by default and free for the first copy.

**Data events** — data-plane operations: `s3:GetObject`, `lambda:Invoke`, `dynamodb:PutItem`. These are **off by default**, because their volume is enormous and they are billed per event. They are also the only way to answer "who read this object," so they are usually enabled selectively on sensitive resources.

**Insights events** — detected anomalies in call-rate patterns. Optional and separately billed.

The management/data split is the most important operational fact about CloudTrail. Someone downloading every object in a bucket generates no management events at all. If data events are off, the read is invisible.

---

## Event History vs a Trail

Two things are easily confused.

**Event history** is on automatically in every account, viewable in the console, and covers the **last 90 days of management events only**. It is not configurable, cannot be extended, and is not stored anywhere we control.

**A trail** is something we create. It delivers events to an S3 bucket (and optionally CloudWatch Logs), retains them for as long as we choose, and can include data events.

The distinction matters at the worst moment. Investigating something that happened four months ago without a trail means the record no longer exists. Every account should have a trail configured before it is needed.

---

## Configuring a Trail

```bash
aws cloudtrail create-trail \
  --name org-audit-trail \
  --s3-bucket-name company-cloudtrail-logs \
  --is-multi-region-trail \
  --enable-log-file-validation \
  --kms-key-id alias/cloudtrail-key

aws cloudtrail start-logging --name org-audit-trail
```

*Creating a trail does not start it — `start-logging` is a separate call, and forgetting it produces a trail that records nothing.*

Four settings deserve attention:

**Multi-region.** A single-region trail misses activity elsewhere, and activity in an unused region is exactly what an investigation cares about. Always enable it.

**Log file validation.** Adds digest files that detect tampering or deletion. Cheap, and it is what makes the trail credible as evidence.

**Encryption with a KMS key.** Trail contents include request parameters, which can be sensitive.

**Organization trails.** In an AWS Organization, one trail can cover every member account, delivering to a central bucket that member accounts cannot write over.

---

## Protecting the Trail

An attacker's first move is often to stop logging. Standard protections:

- **Deliver to a separate, locked-down account** so nobody with access to the workload account can alter the logs.
- **Deny `cloudtrail:StopLogging` and `cloudtrail:DeleteTrail`** via SCP, which not even root in a member account can bypass.
- **Enable S3 Object Lock** on the log bucket for write-once retention.
- **Alarm on trail configuration changes** — a rare event that is always worth reviewing.

---

## Cost

Management events for the first trail are free; additional trails are billed per event. Data events are billed per event and are where costs escalate — enabling `s3:GetObject` data events on a high-traffic bucket can produce a surprising bill on its own.

The larger cost is usually storage: trail logs accumulate in S3 indefinitely unless a lifecycle rule transitions them to cheaper classes and eventually expires them.

---

## Key Takeaways

- CloudTrail records every API call with the identity, action, parameters, source, and result, including denied calls.
- Management events are on by default; data events like `s3:GetObject` are off and must be enabled per resource.
- Event history covers only the last 90 days of management events — a trail is required for anything longer or for data events.
- Creating a trail does not start it; `start-logging` is a separate step.
- Enable multi-region coverage, log file validation, and KMS encryption; use an organization trail where applicable.
- Protect logs in a separate account, block `StopLogging` via SCP, and alarm on trail configuration changes.
- Data events and long-term S3 storage are the two main cost drivers.
