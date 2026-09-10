# Budgets & Alerts

AWS bills in arrears. Without something watching, the first signal that spending changed arrives weeks after it happened — which is too late to prevent it. **AWS Budgets** provides the signal in advance.

---

## What a Budget Is

A budget sets a threshold and notifies when spending approaches or exceeds it. The threshold can be evaluated against actual spend or against a forecast.

**Actual** notifies once spending has already crossed the line. **Forecasted** notifies when AWS projects the month will cross it, based on the trend so far — typically several days earlier, which is what makes it actionable.

The usual arrangement is both: forecast alerts as an early warning, actual alerts as confirmation.

```bash
aws budgets create-budget --account-id 123456789012 --budget '{
  "BudgetName": "monthly-total",
  "BudgetLimit": {"Amount": "5000", "Unit": "USD"},
  "TimeUnit": "MONTHLY",
  "BudgetType": "COST"
}' --notifications-with-subscribers '[{
  "Notification": {
    "NotificationType": "FORECASTED",
    "ComparisonOperator": "GREATER_THAN",
    "Threshold": 100,
    "ThresholdType": "PERCENTAGE"
  },
  "Subscribers": [{"SubscriptionType": "EMAIL", "Address": "platform@example.com"}]
}]'
```

*A $5,000 monthly budget that notifies when the forecast exceeds 100% of it — an alert days before the limit is actually reached.*

---

## Kinds of Budget

**Cost budgets** track spending in dollars. The common case.

**Usage budgets** track a quantity — instance hours, gigabytes stored. Useful where usage matters more than price.

**Reservation and Savings Plan budgets** track utilization and coverage, alerting when a commitment is going unused. Directly valuable, because unused commitment is money already spent.

Budgets can be scoped with filters — by service, tag, account, or region — which is what makes per-team budgets possible.

---

## A Workable Set

One budget for the account total is not enough to locate a problem. A more useful arrangement:

| Budget | Scope | Purpose |
|---|---|---|
| Account total | Everything | The overall guardrail |
| Per environment | `Environment` tag | Catches non-production growing unexpectedly |
| Per team | `Team` tag | Gives teams their own visibility |
| Largest service | Service filter | Watches the dominant line specifically |
| Savings Plan utilization | Commitment | Flags unused commitment |

Per-environment budgets are especially worthwhile. Development and staging spend is where waste concentrates — resources left running after hours, oversized test databases, forgotten experiments — and nobody watches it the way they watch production.

---

## Budget Actions

Budgets can do more than notify. A **budget action** applies a control when a threshold is crossed:

- Attach a restrictive IAM policy to specified users or roles
- Attach an SCP to an account
- Stop EC2 instances or RDS instances

This is a genuine spending control rather than a warning. It suits environments where a hard limit is appropriate — a training account, a sandbox, a per-team development account — and it is dangerous in production, where automatically stopping instances converts a cost problem into an outage.

The usual pattern: budget actions in non-production, notifications only in production.

---

## Cost Anomaly Detection

Budgets catch spending against a threshold. **Cost Anomaly Detection** catches unusual patterns against a learned baseline, which is a different and complementary signal.

It notices a service that suddenly costs three times its normal amount even when the total stays under budget — a forgotten cluster, a runaway retry loop, a misconfigured data pipeline. Because it learns per-service patterns, it detects changes that a total-spend budget hides.

It costs nothing, takes a few minutes to configure with an SNS topic, and is one of the higher-value things to enable in a new account.

---

## Setting Thresholds

**Set the account budget at the expected total, not at a scary number.** A budget set far above normal spend never fires until something is badly wrong.

**Use multiple thresholds on one budget** — 50%, 80%, 100% — for progressive warning rather than a single late alert.

**Alert on forecast for the early signal**, on actual for confirmation.

**Send alerts where people look.** Email alone gets filtered. SNS to a chat channel is read.

**Revise thresholds as the system grows.** A budget that fires every month is ignored within two months, which makes it worse than no budget.

---

## Key Takeaways

- AWS bills in arrears, so without budgets the first signal arrives weeks late.
- Forecast alerts warn days before actual alerts, making them the actionable ones.
- Budgets can track cost, usage, or commitment utilization, and can be filtered by service, tag, account, or region.
- Per-environment and per-team budgets locate a problem in a way an account total cannot.
- Budget actions can attach restrictive policies or stop instances — appropriate in sandboxes, dangerous in production.
- Cost Anomaly Detection catches per-service spikes that stay within an overall budget, and is free.
- Set thresholds near expected spend, use progressive levels, and route alerts where people actually read them.
