# Default Bus, Custom Buses & Schedules

Where events come from, why application events belong on their own bus, and how EventBridge replaces cron.

---

## The Default Bus

Every account has a default bus, and AWS services publish to it automatically with no configuration. What is already flowing through it:

| Source | Examples |
|---|---|
| `aws.ec2` | Instance state changes, spot interruption warnings |
| `aws.s3` | Object created, deleted (with EventBridge enabled on the bucket) |
| `aws.ecs` | Task state changes, deployment events |
| `aws.codepipeline` | Stage and action state changes |
| `aws.health` | Service health and scheduled maintenance |
| `aws.signin` | Console sign-ins, including root |
| `aws.config` | Compliance state changes |
| `aws.securityhub` | Security findings |

This makes a class of automation straightforward. Reacting to a spot interruption, alerting on root sign-in, or triggering a workflow when a pipeline fails is a rule and a target.

```json
{
  "source": ["aws.ec2"],
  "detail-type": ["EC2 Spot Instance Interruption Warning"]
}
```

*Fires on a spot interruption notice, giving a target two minutes to drain work. No agent, no polling.*

---

## Custom Buses

Application events should go on a **custom bus**, not the default one. The reasons are practical:

**Isolation.** Application events are separated from the constant stream of AWS service events.

**Access control.** A bus has a resource policy, so publishing and rule creation can be granted per bus. The default bus cannot be restricted the same way, since AWS services must publish to it.

**Clarity.** Rules on a custom bus concern the application, so their purpose is obvious.

**Independent limits.** Quotas apply per bus.

```bash
aws events create-event-bus --name orders-bus
```

A bus per bounded context — `orders-bus`, `inventory-bus` — is a common arrangement, with cross-bus rules where contexts genuinely need to interact.

---

## Cross-Account Events

A bus policy can allow other accounts to publish, or a rule can target a bus in another account:

```json
{
  "Effect": "Allow",
  "Principal": { "AWS": "arn:aws:iam::444455556666:root" },
  "Action": "events:PutEvents",
  "Resource": "arn:aws:events:us-east-1:123456789012:event-bus/orders-bus"
}
```

*Permits another account to publish. This is how event-driven architectures span team and account boundaries.*

The usual arrangement is a central bus receiving events from producer accounts, with consumer accounts subscribing via rules that forward to their own buses. It works well and makes the event schema a published contract — changing it affects consumers in accounts we do not control.

---

## Scheduled Rules

EventBridge runs rules on a schedule, replacing cron on a server:

```bash
aws events put-rule --name nightly-report \
  --schedule-expression "cron(0 2 * * ? *)"

aws events put-rule --name every-five-minutes \
  --schedule-expression "rate(5 minutes)"
```

*`rate()` for simple intervals, `cron()` for specific times. Both are UTC by default.*

**The cron syntax has six fields**, not five: minute, hour, day-of-month, month, day-of-week, year. And **either day-of-month or day-of-week must be `?`** — they cannot both be specified. `cron(0 2 * * ? *)` is daily at 02:00 UTC; the `?` and the trailing year field are the two things people get wrong.

Why this beats cron on an instance:

- **No server to run, patch, or keep alive.**
- **It runs exactly once**, rather than once per instance in a fleet.
- **Failures are visible** in CloudWatch metrics.
- **It is defined in infrastructure code** rather than in a crontab someone has to remember exists.

---

## EventBridge Scheduler

**EventBridge Scheduler** is a separate, newer service for scheduling specifically, and it is the better choice for new scheduled work:

- **Time zone support**, including daylight saving handling — `cron(0 2 * * ? *)` in `America/New_York` stays at 02:00 local through the changeover.
- **One-time schedules** at a specific timestamp.
- **A flexible time window** that jitters execution, avoiding a thundering herd of schedules all firing at once.
- **Much higher schedule limits**, supporting millions of individual schedules — which enables per-user or per-entity reminders, not just system-wide jobs.
- **Built-in retries and a dead-letter queue.**

```bash
aws scheduler create-schedule --name send-reminder \
  --schedule-expression "at(2026-09-15T14:00:00)" \
  --schedule-expression-timezone "America/New_York" \
  --flexible-time-window '{"Mode":"OFF"}' \
  --target '{"Arn":"arn:aws:lambda:...:function:reminder","RoleArn":"arn:aws:iam::...:role/SchedulerRole"}'
```

*A one-time schedule in a named time zone — something scheduled rules cannot express.*

**Use EventBridge Scheduler for new scheduled work.** Scheduled rules on a bus remain for existing setups and where the schedule belongs alongside other rules.

---

## Key Takeaways

- The default bus carries AWS service events automatically, making infrastructure automation a rule plus a target.
- Put application events on a custom bus for isolation, per-bus access control, and clarity.
- Bus resource policies allow cross-account publishing, making the event schema a contract across teams.
- Scheduled rules replace cron with no server, exactly-once execution, and visible failures.
- EventBridge cron has six fields, and day-of-month or day-of-week must be `?`.
- EventBridge Scheduler adds time zones with daylight saving, one-time schedules, jitter windows, and millions of schedules.
- Prefer EventBridge Scheduler for new scheduled work.
