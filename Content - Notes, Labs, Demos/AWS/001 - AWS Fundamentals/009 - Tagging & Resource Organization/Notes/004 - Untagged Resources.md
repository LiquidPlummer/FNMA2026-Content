# Untagged Resources

Untagged is the default state. Every mechanism that depends on tags — cost attribution, automation, access control — fails silently for resources that lack them, and untagged resources accumulate steadily unless something prevents it.

---

## Where They Come From

**Resources created by other resources.** An EC2 instance creates EBS volumes. A volume creates snapshots. An RDS instance creates automated backups. **None of these inherit tags.** This is the largest single source of untagged resources, and it grows on its own without anyone doing anything.

**Console creation.** The console's tag step is optional and easy to skip when the goal is to test something quickly. The test resource then outlives the test.

**Resources created before the scheme existed.** Anything predating the tagging policy is untagged, and there is no signal distinguishing it from a recent oversight.

**Service-created resources.** Load balancer network interfaces, Lambda-managed ENIs, and similar resources are created by AWS on our behalf and often carry only `aws:`-prefixed tags.

**Emergency work.** Resources created at 2 a.m. during an incident are not tagged carefully, and are not revisited afterwards.

---

## What Breaks

**Cost attribution.** Untagged spend appears in Cost Explorer only as "No tag value." When that category is 30% of the bill, cost reporting is not actionable — the largest single line cannot be assigned to anyone. And because cost allocation tags apply only from activation forward, tagging those resources now does not recover the history.

**Automation.** A backup job selecting `Backup=daily` skips untagged volumes silently. Nobody notices until a restore is attempted. This is the most damaging failure, because the absence of a backup is invisible until it matters.

**Cleanup.** An unattached volume with no tags gives no indication of whose it is or whether it is needed. The safe assumption is always "leave it," so it stays indefinitely, billing every month.

**Access control.** Under ABAC, an untagged resource matches no team's condition — it becomes inaccessible to everyone except administrators, which produces confusing permission failures.

---

## Finding Them

The Resource Groups Tagging API queries across services:

```bash
aws resourcegroupstaggingapi get-resources \
  --tag-filters Key=Environment \
  --query "ResourceTagMappingList[].ResourceARN"
```

*Returns resources that have an `Environment` tag. Comparing against a full resource inventory identifies what is missing it — the API filters for presence, so absence is derived by difference.*

Cost Explorer is often the faster route: group by a tag key and the "No tag value" bucket shows both the scale of the problem and which services contribute most.

For ongoing detection, **AWS Config** provides a `required-tags` rule that continuously evaluates resources and reports non-compliance, which turns a periodic audit into a standing signal.

---

## Preventing Them

In order of effectiveness:

**1. Enforce at creation with IAM or SCP conditions.** The only approach that genuinely guarantees coverage, because it makes untagged creation impossible.

**2. Tag through infrastructure as code.** Stack-level tags in CloudFormation and `default_tags` in Terraform apply to everything they create, with no per-resource effort.

**3. Handle derived resources specifically.** Auto Scaling groups can propagate tags to instances. Data Lifecycle Manager copies volume tags to snapshots. Each of these is a separate setting, and each closes a leak that otherwise grows continuously.

**4. Detect and remediate.** Config rules plus a remediation action can tag resources automatically — often with `Owner` derived from the CloudTrail record of who created them.

**5. Report on it.** A weekly figure for untagged spend keeps the number visible. Untagged resources persist mainly because nobody is looking at them.

---

## Dealing With Existing Ones

For an account with a substantial untagged population, ordering by value:

1. **Start with cost.** Sort untagged resources by spend and tag the expensive ones first — a small number of resources usually accounts for most of the untagged bill.
2. **Use CloudTrail to find owners.** The creation event names the principal, which is often enough to assign an `Owner` tag.
3. **Tag as unknown rather than leaving blank.** `Owner=unknown` is a category that can be tracked and reduced; an absent tag is invisible.
4. **Treat unattached resources as candidates for deletion.** Unattached EBS volumes and unassociated Elastic IPs cost money and do nothing.
5. **Stop the inflow first.** Cleaning up while new untagged resources are still being created does not converge.

---

## Key Takeaways

- Untagged is the default, and derived resources like volumes, snapshots, and backups never inherit tags.
- Untagged spend appears as an unattributable block in Cost Explorer, and tagging later does not recover past cost data.
- Automation that selects by tag skips untagged resources silently — missed backups being the most damaging case.
- Untagged resources are rarely deleted because nobody can tell whether they are needed.
- Enforcement at creation via IAM or SCP conditions is the only reliable prevention; IaC stack tags cover most of the rest.
- Configure tag propagation explicitly for Auto Scaling instances and lifecycle-managed snapshots.
- When cleaning up, stop the inflow first, then work in descending order of cost.
