# The Free Tier

The free tier has three distinct kinds of offer with different expiry behavior. Most unexpected first bills come from not knowing which kind a given service falls into.

---

## The Three Kinds

**12-month free tier.** Available for the first year after account creation, then billed at normal rates. Includes 750 hours/month of `t2.micro` or `t3.micro` EC2, 5 GB of S3 standard storage, and 750 hours/month of `db.t3.micro` RDS.

**Always free.** Permanent, with no expiry. Includes 1 million Lambda requests and 400,000 GB-seconds per month, 25 GB of DynamoDB storage, 10 custom CloudWatch metrics, and 1 million SNS publishes.

**Short-term trials.** A fixed period from the moment the service is first used, unrelated to account age.

The most important consequence: **the 12-month tier expires on a date, not on usage.** Resources that were free for eleven months start billing in month thirteen with no change in behavior and no notification tied to the resource itself.

---

## What Is Not Covered

The common surprises, all of which sit next to something that is free:

**EBS volumes attached to free-tier instances.** The 30 GB allowance is limited; larger root volumes are billed. And the volume bills whether the instance is running or stopped.

**Elastic IPs.** Public IPv4 addresses are now charged hourly, including unattached ones. An Elastic IP left over from a deleted instance is a small permanent charge.

**NAT gateways.** Not in the free tier at all. Following a tutorial that creates one costs roughly $32/month plus processing from the moment it exists — a frequent source of a surprising first bill.

**Data transfer beyond the allowance.** 100 GB/month out is free; beyond that is billed.

**Load balancers.** The 750 free hours cover one balancer continuously; a second is billed.

**RDS storage and backups** beyond the included allowance.

**Anything above the size limit.** A `t3.small` is not free-tier eligible. Selecting a larger type because the small one felt slow moves the resource entirely out of the free tier.

**The free tier is per account, not per region or per resource.** Running one `t3.micro` in two regions consumes 1,500 instance-hours against a 750-hour allowance.

---

## Staying Within It

**Enable free tier usage alerts.** In Billing preferences — these notify at 85% of a free tier limit and are off by default. Turning them on is the single most useful step.

**Set a budget with a low threshold.** A $1 budget alert fires on any charge at all, which is the earliest possible signal that something outside the free tier was created.

```bash
aws budgets create-budget --account-id 123456789012 --budget '{
  "BudgetName": "free-tier-guard",
  "BudgetLimit": {"Amount": "1", "Unit": "USD"},
  "TimeUnit": "MONTHLY",
  "BudgetType": "COST"
}'
```

*A $1 monthly budget, which combined with a notification subscriber alerts on essentially any non-free spend.*

**Check the Free Tier page in the Billing console.** It shows current usage against each limit and forecasts month-end overage.

**Note the account creation date.** The 12-month tier expires on its anniversary. Anyone relying on it should know when that is.

**Delete rather than stop.** A stopped instance still bills for its EBS volume and any Elastic IP. Only deletion stops all charges.

---

## Using the Free Tier for Learning

It genuinely covers a lot of experimentation, with a few practical adjustments:

- Use a **separate account** for learning, so its spend is isolated and it can be closed cleanly.
- Prefer **serverless services** — Lambda, DynamoDB, S3 — since their always-free allowances do not expire.
- **Avoid NAT gateways** in practice VPCs; use public subnets or VPC endpoints instead.
- **Tear down after each session.** Most unexpected bills come from resources left running after an exercise ended.
- **Set the $1 budget alert on day one**, before creating anything.

---

## Key Takeaways

- The free tier has three kinds of offer: 12-month, always-free, and short-term trials.
- The 12-month tier expires on the account's anniversary regardless of usage, and resources begin billing with no change in behavior.
- Always-free allowances for Lambda, DynamoDB, and CloudWatch metrics do not expire.
- NAT gateways, Elastic IPs, larger EBS volumes, and above-size instance types are common uncovered charges.
- Allowances are per account, not per region, so multi-region use consumes them faster.
- Enable free tier usage alerts and a low budget alert, both of which are off by default.
- Delete rather than stop, since stopped instances still bill for volumes and addresses.
