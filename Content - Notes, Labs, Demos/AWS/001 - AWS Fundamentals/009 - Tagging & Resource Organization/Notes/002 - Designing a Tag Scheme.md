# Designing a Tag Scheme

A tag scheme has to be designed before resources exist. Retrofitting one across an account that has been running for a year is a project, and the cost data it would have produced is gone.

---

## What Tags Are Used For

Three uses, each driving different requirements:

**Cost allocation.** Attributing spend to a team, product, or environment. Requires tags to be activated as **cost allocation tags** in the Billing console, and they only apply from activation forward — this is the single most time-sensitive reason to set the scheme up early.

**Automation targeting.** Selecting resources for backups, patching, scheduled shutdown, or cleanup. Requires consistent values, since automation matching `Environment=dev` misses `Environment=development`.

**Access control.** IAM conditions restricting actions to resources with particular tags. Requires that tag values be trustworthy, which means controlling who can change them.

---

## A Workable Scheme

Small and mandatory beats large and aspirational. A scheme with twenty tags gets partially applied; one with five gets applied.

| Tag | Purpose | Example values |
|---|---|---|
| `Environment` | Cost split, automation, access control | `production`, `staging`, `development` |
| `Owner` | Who to contact | `platform-team`, `data-eng` |
| `Project` | Cost attribution | `orders-api`, `reporting` |
| `CostCenter` | Finance chargeback | `CC-4471` |
| `ManagedBy` | What created it | `cloudformation`, `terraform`, `manual` |

`ManagedBy` earns its place quietly. It distinguishes resources under infrastructure-as-code from ones someone created by hand, which makes drift and orphaned resources visible.

---

## Rules That Prevent Fragmentation

**Fix the case convention.** `PascalCase` keys, lowercase values, applied without exception. `Environment` and `environment` are two distinct tags, and a report grouping by one silently omits the other.

**Enumerate the allowed values.** `Environment` should have three or four permitted values, written down. Free text produces `prod`, `production`, `Production`, and `prd` in the same account, none of which aggregate.

**Avoid tags that duplicate resource attributes.** A `Region` tag or an `InstanceType` tag adds nothing — AWS already knows those and can group by them.

**Never put secrets in tags.** Tags are visible to anyone with describe permissions on the resource, appear in CloudTrail, and are not encrypted.

**Prefer few mandatory tags to many optional ones.** Optional tags are applied inconsistently, which makes them useless for exactly the aggregation they were meant to enable.

---

## Enforcement

A scheme nobody enforces degrades within weeks. Three mechanisms, in increasing strictness:

**Tag policies** (AWS Organizations) define allowed keys and values and report non-compliance. They standardize capitalization and flag violations, but do not block creation.

**SCPs or IAM conditions** block creation of untagged resources outright:

```json
{
  "Effect": "Deny",
  "Action": ["ec2:RunInstances", "rds:CreateDBInstance"],
  "Resource": "*",
  "Condition": {
    "Null": { "aws:RequestTag/Environment": "true" }
  }
}
```

*Denies creating these resources unless an `Environment` tag is supplied in the request. `Null` with `true` tests for the tag being absent.*

This is the only mechanism that genuinely guarantees coverage, and it is worth the friction for the two or three tags that matter most.

**Config rules** detect non-compliant resources after creation and can trigger remediation. A useful backstop for resources created by services rather than by people.

---

## Applying Tags Automatically

The most reliable tagging is tagging nobody has to remember:

- **Infrastructure as code.** CloudFormation stack-level tags propagate to every supported resource in the stack, and Terraform's `default_tags` does the same. This covers most resources for free.
- **Auto Scaling groups** propagate tags to launched instances when configured to.
- **Organizations tag policies** keep values consistent across accounts.

The residue after all of that is manual resources — which is exactly what the `ManagedBy` tag exposes.

---

## Key Takeaways

- Design the scheme before resources exist; cost allocation tags apply only from activation forward.
- Keep the mandatory set small — around five tags — since large schemes get applied partially.
- Fix case conventions and enumerate allowed values, or reporting fragments across near-identical tags.
- Do not duplicate attributes AWS already knows, and never put secrets in tags.
- Tag policies report non-compliance; IAM or SCP conditions on `aws:RequestTag` are what actually enforce it.
- Stack-level tags in CloudFormation or Terraform cover most resources automatically, leaving manual creation as the gap.
