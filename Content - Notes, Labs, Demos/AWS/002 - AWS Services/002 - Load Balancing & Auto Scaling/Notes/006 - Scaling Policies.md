# Scaling Policies

A **scaling policy** changes the desired capacity of an Auto Scaling group in response to a metric. There are several kinds, and one of them is the right answer most of the time.

---

## Target Tracking

**Target tracking** is the default recommendation. We name a metric and a target value, and AWS manages the rest:

```bash
aws autoscaling put-scaling-policy \
  --auto-scaling-group-name app-servers \
  --policy-name cpu-target \
  --policy-type TargetTrackingScaling \
  --target-tracking-configuration '{
    "PredefinedMetricSpecification": {"PredefinedMetricType": "ASGAverageCPUUtilization"},
    "TargetValue": 60.0
  }'
```

*Keeps average CPU near 60% by adding and removing instances. AWS creates the underlying alarms and calculates how many instances to change by.*

It works like a thermostat: set the target, and the system adjusts. Available predefined metrics include average CPU, average network in/out, and — usually the best choice for a web service — **requests per target on an ALB**, which tracks the actual work each instance is doing rather than a proxy for it.

Target tracking scales out aggressively and in conservatively, which is the correct asymmetry: being slow to add capacity causes an outage, while being slow to remove it costs a little money.

**Choosing the target value.** Too high leaves no headroom for the time it takes new instances to launch — at 90% CPU, the group is already saturated while instances boot. Too low wastes capacity. Around 50–70% is typical, and the right number depends on how fast instances start.

---

## Step Scaling

**Step scaling** defines explicit adjustments per alarm threshold range:

| CPU | Adjustment |
|---|---|
| 60–70% | +1 instance |
| 70–85% | +2 instances |
| 85%+ | +4 instances |

This gives direct control over the response magnitude, which matters when load arrives in large jumps and a proportional response is too slow. The cost is that thresholds and step sizes must be tuned by hand, and re-tuned as the application changes.

Use it when target tracking's behavior does not fit — typically for sharp, large spikes, or for scaling on a custom metric with known break points.

---

## Scheduled Scaling

**Scheduled scaling** changes capacity at set times, independent of any metric:

```bash
aws autoscaling put-scheduled-update-group-action \
  --auto-scaling-group-name app-servers \
  --scheduled-action-name weekday-morning \
  --recurrence "0 7 * * 1-5" \
  --min-size 6 --desired-capacity 8
```

*Raises capacity at 07:00 on weekdays, ahead of the traffic rather than in response to it. Times are UTC unless a time zone is specified.*

This is the answer to reactive scaling's fundamental limitation: a policy responds *after* load arrives, and instances take minutes to become useful. For predictable patterns — business hours, a scheduled batch window, a known campaign — scheduling capacity in advance removes that lag entirely.

Scheduled and dynamic policies combine well: schedule the expected baseline, and let target tracking handle variation around it.

---

## Predictive Scaling

**Predictive scaling** uses machine learning on historical traffic to forecast load and provision ahead of it. It suits workloads with genuine cyclical patterns and needs at least 24 hours of history, improving with more.

It is best used in combination with target tracking — prediction handles the daily cycle, and reactive scaling absorbs whatever the forecast missed.

---

## Cooldowns and Warmup

Without a delay, a group scales out, sees the metric still high because new instances have not started serving, scales out again, and overshoots badly.

**Cooldown** (for simple scaling) blocks further activity for a period after a scaling action.

**Instance warmup** (for target tracking and step scaling) excludes newly launched instances from the metric aggregation until they have been running long enough to serve traffic. This is the more precise mechanism and is what current policy types use.

Warmup should match the realistic time from launch to serving requests — including boot, user data, application start, and health check success. Setting it too short causes overshoot; too long makes scaling sluggish.

---

## Scale-In Behavior

Removing instances needs care.

**Termination policy** decides which instance goes. The default balances across AZs, then removes the one closest to the next billing hour. `OldestInstance` is useful during a gradual fleet rollover.

**Scale-in protection** marks specific instances as not eligible for termination — appropriate for an instance doing long-running work.

**Deregistration delay (connection draining)** on the target group lets in-flight requests finish before the instance is terminated. Set it slightly above the longest normal request duration; too long makes scale-in and deployments slow.

---

## Choosing

| Situation | Policy |
|---|---|
| General web application | Target tracking on requests per target |
| CPU-bound processing | Target tracking on average CPU |
| Predictable daily pattern | Scheduled, plus target tracking |
| Sharp, large spikes | Step scaling |
| Cyclical with history | Predictive, plus target tracking |
| Queue-driven workers | Target tracking on a custom metric of queue depth per instance |

The last row is worth noting. Scaling workers on queue depth alone breaks as the fleet grows — a depth of 1,000 means something different with 2 workers than with 40. Publishing a custom metric of *backlog per instance* gives target tracking something that stays meaningful at any fleet size.

---

## Key Takeaways

- Target tracking is the default choice: name a metric and a target, and AWS manages the adjustments.
- Requests per target is usually a better signal for web services than CPU.
- Set the target with enough headroom to cover instance startup time — typically 50–70%.
- Step scaling gives explicit control over response size and suits sharp spikes at known thresholds.
- Scheduled scaling provisions ahead of predictable load, removing the lag inherent in reactive scaling.
- Instance warmup prevents overshoot by excluding new instances from metrics until they are serving.
- Configure termination policy and deregistration delay so scale-in does not drop in-flight requests.
- For queue-driven workers, scale on backlog per instance rather than raw queue depth.
