# CloudTrail vs CloudWatch Logs

Both hold logs. They hold entirely different logs, produced by different things, answering different questions. Reaching for the wrong one is a common way to waste time during an incident.

---

## The Distinction

**CloudTrail records what was done to AWS.** Its events are API calls: someone created an instance, modified a security group, assumed a role, deleted an object. The subject is always an action against the AWS control plane or data plane, and the actor is always an identity.

**CloudWatch Logs holds what applications wrote.** Its contents are whatever a program emitted — a Lambda function's `print` output, an application's structured JSON logs, an nginx access log shipped by the CloudWatch agent, a VPC flow log. The subject is whatever the application decided to write.

| | CloudTrail | CloudWatch Logs |
|---|---|---|
| Records | AWS API calls | Application and service output |
| Produced by | AWS, automatically | Our code, or an agent we install |
| Answers | Who changed this resource? | What did the application do? |
| Schema | Fixed AWS event format | Whatever we emit |
| Enabled by | Existing (management events) | Configuration per source |
| Typical use | Audit, permission debugging, forensics | Debugging application behavior |

---

## Which One Answers the Question

Sorting a few questions makes the boundary concrete:

- *"Who deleted the production database?"* — CloudTrail. It is an API call.
- *"Why did the checkout request fail at 14:22?"* — CloudWatch Logs. That is application output.
- *"Did anyone change this security group last week?"* — CloudTrail.
- *"Which query was slow?"* — CloudWatch Logs, if the application or database logs queries.
- *"Who downloaded this S3 object?"* — CloudTrail, but only if data events were enabled.
- *"Did the Lambda function throw an exception?"* — CloudWatch Logs.
- *"Why did the Lambda function get `AccessDenied` calling DynamoDB?"* — Either. The application logs the error; CloudTrail records the denied call with the full identity and parameters.

That last pair is the useful overlap. When a permission fails, CloudTrail often gives a better answer than the application's own error message, because it records exactly which principal made which call.

---

## Where They Connect

CloudTrail can deliver events **into** CloudWatch Logs in addition to S3. This is worth doing, because it enables two things S3 delivery does not:

**Metric filters and alarms.** A pattern-matching filter over the log group produces a metric, and a metric can trigger an alarm:

```json
{ ($.eventName = "StopLogging") || ($.eventName = "DeleteTrail") }
```

*A CloudWatch metric filter over CloudTrail events; incrementing a metric on these events allows an alarm on someone disabling the audit trail.*

**Faster querying.** CloudWatch Logs Insights queries recent events interactively, which is quicker than reading compressed JSON from S3 during an active investigation.

The usual arrangement is both: S3 for durable, cheap, long-term retention, and CloudWatch Logs for a shorter window that supports alarms and interactive queries.

```
CloudTrail ──┬──► S3 bucket        (long retention, low cost, evidence)
             └──► CloudWatch Logs  (short retention, alarms, Insights queries)
```

*Each destination serves a different need; sending to both is standard practice.*

---

## A Third Thing: AWS Config

Worth naming to complete the picture. **AWS Config** records resource *configuration state* over time — what a security group's rules were on a given date, not who changed them.

- CloudTrail: who did what, when.
- CloudWatch Logs: what the application said.
- AWS Config: what the configuration looked like at a point in time.

An investigation frequently uses all three: Config shows the configuration changed, CloudTrail shows who changed it, and CloudWatch Logs shows what broke as a result.

---

## Key Takeaways

- CloudTrail records AWS API calls; CloudWatch Logs holds output written by applications and agents.
- CloudTrail answers "who changed this"; CloudWatch Logs answers "what did the application do."
- CloudTrail is populated automatically for management events; CloudWatch Logs requires something to write to it.
- For permission failures, CloudTrail often explains more than the application's error message.
- Delivering CloudTrail to both S3 and CloudWatch Logs gives durable retention plus alarms and interactive queries.
- AWS Config completes the set by recording resource configuration state over time.
