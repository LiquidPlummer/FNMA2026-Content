# Alarms

An **alarm** watches a metric and changes state when it crosses a threshold. State changes trigger actions. The mechanism is small; getting alarms to be useful rather than noisy is where the work is.

---

## The Three States

**`OK`** — the metric is within the threshold.
**`ALARM`** — the threshold has been breached for the configured number of periods.
**`INSUFFICIENT_DATA`** — not enough data points to decide.

`INSUFFICIENT_DATA` is the one that causes trouble. It occurs when a metric stops being published entirely — an instance terminates, a Lambda function stops being invoked, an agent dies. An alarm sitting in `INSUFFICIENT_DATA` is not alarming, and by default it triggers nothing. So the failure that stops a metric being reported is exactly the failure the alarm does not catch.

The `TreatMissingData` setting decides what happens:

| Setting | Behavior |
|---|---|
| `missing` | Keeps the current state (default) |
| `notBreaching` | Treats missing data as `OK` |
| `breaching` | Treats missing data as `ALARM` |
| `ignore` | Leaves the state unchanged |

For anything where "no data" means "something is broken" — a heartbeat, a health check — `breaching` is the correct choice.

---

## Configuration

```bash
aws cloudwatch put-metric-alarm \
  --alarm-name orders-api-5xx \
  --namespace AWS/ApplicationELB \
  --metric-name HTTPCode_Target_5XX_Count \
  --dimensions Name=LoadBalancer,Value=app/orders-alb/abc123 \
  --statistic Sum --period 60 \
  --evaluation-periods 3 --datapoints-to-alarm 2 \
  --threshold 10 --comparison-operator GreaterThanThreshold \
  --treat-missing-data notBreaching \
  --alarm-actions arn:aws:sns:us-east-1:123456789012:ops-alerts
```

*Alarms when 5xx responses exceed 10 in at least 2 of any 3 consecutive one-minute periods, notifying an SNS topic.*

The parameters that decide whether an alarm is useful:

**`period`** — the aggregation window. Must be at least as long as the metric's publishing interval; a 1-minute period on a 5-minute metric produces mostly empty periods.

**`evaluation-periods` and `datapoints-to-alarm`** — together, the "M of N" rule. Requiring 2 of 3 tolerates a single transient spike while still catching a sustained problem. Requiring 1 of 1 produces alerts on noise.

**`statistic`** — `Average` hides outliers, `Sum` suits counts, `Maximum` catches spikes, and percentiles (`p95`, `p99`) describe latency far better than averages do. An average response time of 200 ms can conceal a p99 of eight seconds.

---

## What an Alarm Can Trigger

**SNS notification** — the most common, fanning out to email, chat, or an incident tool.

**Auto Scaling action** — adding or removing instances. This is how step scaling policies work.

**EC2 action** — stop, terminate, reboot, or recover an instance. `recover` is particularly useful paired with `StatusCheckFailed_System`, migrating the instance to healthy hardware automatically.

**Systems Manager action** — creating an OpsItem or an incident record.

**A Lambda function** — indirectly, via SNS or EventBridge, for automated remediation.

---

## Composite Alarms

A **composite alarm** combines other alarms with boolean logic:

```
ALARM(orders-api-5xx) AND NOT ALARM(deployment-in-progress)
```

*Suppresses the error alarm during a deployment, so expected transient errors do not page anyone.*

Composite alarms are the main tool against alert storms. A single failure that breaches CPU, latency, error rate, and health check alarms simultaneously sends four pages for one incident; a composite alarm sends one, and the individual alarms become supporting detail rather than notifications.

---

## Making Alarms Useful

The failure mode is not too few alarms — it is too many, which trains people to ignore them.

**Alarm on symptoms, not causes.** Users experience errors and latency. High CPU is only interesting if it causes one of those. An alarm on elevated error rate is actionable; an alarm on 80% CPU on a healthy instance is not.

**Every alarm should require an action.** If nobody would do anything, it should be a dashboard, not an alarm.

**Set thresholds from observed behavior**, not intuition. Look at a few weeks of the metric before choosing a number.

**Separate paging from notification.** Page for things needing immediate human attention; route everything else to a channel people read during working hours.

**Always set `TreatMissingData`.** The default of holding state is rarely the intended behavior.

**Alarm on the absence of expected activity.** A nightly job producing no invocations is a failure that no threshold on error count will catch.

---

## Key Takeaways

- Alarms have three states, and `INSUFFICIENT_DATA` means the failure that stops metric publication may go unnoticed.
- Set `TreatMissingData` deliberately — `breaching` for heartbeats and health checks.
- The M-of-N rule via `evaluation-periods` and `datapoints-to-alarm` tolerates transient spikes while catching sustained problems.
- Choose the statistic carefully; percentiles describe latency far better than averages.
- Alarms can notify SNS, drive Auto Scaling, recover EC2 instances, and trigger remediation.
- Composite alarms combine alarms with boolean logic to suppress noise and prevent alert storms.
- Alarm on user-visible symptoms, require an action for every alarm, and set thresholds from observed data.
