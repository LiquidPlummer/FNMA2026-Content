# Regions

A **region** is a named geographic area — `us-east-1`, `eu-west-2`, `ap-southeast-1` — containing a cluster of AWS data centers. The important property is not geography, though. It is that each region is an **independent deployment of AWS**.

---

## Regions Are Independent

Each region runs its own copies of the AWS services, its own control plane, and its own API endpoints. Two consequences follow, and both are constant sources of confusion for people new to AWS.

**Resources are region-scoped.** An EC2 instance, a VPC, a security group, an SQS queue, an RDS database, a Lambda function — each exists in exactly one region. A security group in `us-east-1` cannot be attached to an instance in `us-west-2`. There is no "move to another region" operation for most resources; getting a workload into a new region means creating equivalent resources there.

**The console shows one region at a time.** The region selector in the console's top-right corner filters everything. The single most common early experience on AWS is "my instance disappeared," and the answer is almost always that the console is looking at a different region than the one where the instance was launched. The CLI has the same behavior — the configured region determines which endpoint the call goes to.

```bash
# The same command against two regions returns two different, unrelated lists
aws ec2 describe-instances --region us-east-1
aws ec2 describe-instances --region eu-west-1
```

*Every API call targets a specific region's endpoint; nothing here queries "all of AWS."*

---

## Independence Is a Feature

Region independence is deliberate. It means a large-scale failure in one region does not propagate to others — historically, significant AWS outages have been region-scoped, and workloads in other regions kept running.

It also means AWS launches services region by region. A new service typically appears in `us-east-1` first and reaches other regions over the following months. Some services never reach some regions at all.

The same independence is what makes multi-region architecture expensive and complicated. Because nothing is shared, running in two regions means running two of everything and solving data replication ourselves. There is no toggle for it.

---

## Region Naming

The identifier has a consistent shape: `<geographic area>-<direction>-<number>`.

| Code | Location |
|---|---|
| `us-east-1` | Northern Virginia |
| `us-east-2` | Ohio |
| `us-west-2` | Oregon |
| `eu-west-1` | Ireland |
| `eu-central-1` | Frankfurt |
| `ap-southeast-2` | Sydney |

`us-east-1` deserves a note of its own. It is AWS's oldest and largest region, it gets new services first, and several global services keep their control plane there. It is also the region where large-scale incidents have most often occurred, and where capacity constraints are most likely. Its default status in tooling means workloads often land there by accident rather than by decision.

---

## Key Takeaways

- A region is an independent deployment of AWS in a geographic area, with its own services, control plane, and API endpoints.
- Almost every resource is region-scoped and cannot be referenced from or moved to another region.
- The console and CLI operate on one region at a time; a "missing" resource is usually in a different region.
- Independence limits the blast radius of outages but makes multi-region deployment a matter of duplicating everything.
- Service availability differs by region, and new services generally reach `us-east-1` first.
