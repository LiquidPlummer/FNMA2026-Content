# Availability Zones

An **Availability Zone (AZ)** is one or more data centers within a region, with independent power, cooling, and physical infrastructure, connected to the other AZs in that region by low-latency private links. Every region has at least three.

The useful way to think about an AZ is as a **failure domain**: the unit that can fail all at once, without taking its neighbors with it.

---

## What an AZ Boundary Buys

AZs in a region are physically separated — different buildings, different power feeds, often kilometers apart — but close enough that the network round trip between them is on the order of a millisecond.

That combination is the entire point. It is short enough to run a synchronously replicated database across two AZs, and far enough apart that a fire, flood, or power event in one is unlikely to affect the other.

```
Region: us-east-1
├── AZ us-east-1a  ──┐
├── AZ us-east-1b  ──┼── ~1 ms private network between them
└── AZ us-east-1c  ──┘
```

*Separate failure domains, close enough for synchronous replication — the design point AZs exist to hit.*

---

## AZ Names Are Per-Account

`us-east-1a` does not refer to the same physical data center for two different accounts. AWS randomizes the mapping of AZ names to physical zones per account, so that customers do not all crowd into the zone alphabetically first.

For work that must line up across accounts — a shared VPC, a cross-account architecture — use the **AZ ID** (`use1-az1`, `use1-az2`), which is stable and identical everywhere.

```bash
# Show both the account-specific name and the stable, cross-account AZ ID
aws ec2 describe-availability-zones --region us-east-1 \
  --query "AvailabilityZones[].[ZoneName,ZoneId]" --output table
```

*`ZoneName` is what our account calls the zone; `ZoneId` identifies the physical zone consistently across accounts.*

---

## Which Resources Are AZ-Scoped

This distinction determines what has to be planned for:

| Scope | Examples |
|---|---|
| **AZ-scoped** | EC2 instances, EBS volumes, subnets, RDS instances |
| **Region-scoped** | VPCs, security groups, S3 buckets, DynamoDB tables, SQS queues, Lambda functions |

Two entries matter especially. **A subnet lives in exactly one AZ** — that is how an instance ends up in a particular zone, since we choose the subnet. And **an EBS volume can only attach to an instance in its own AZ**; there is no cross-AZ attach. Moving a volume to another zone means taking a snapshot and creating a new volume from it there.

Region-scoped services like S3, DynamoDB, and SQS already replicate across AZs internally. We do not configure that and do not pay extra for it — an S3 object survives the loss of an AZ without any action from us.

---

## Single-AZ Is the Default

Nothing forces a multi-AZ deployment. Launching one instance in one subnet puts everything in one AZ, and when that AZ has a problem, the workload is down. This is the default outcome of the simplest possible setup, which is why the next note treats multi-AZ as a deliberate decision with a price attached.

---

## Key Takeaways

- An Availability Zone is an isolated failure domain within a region — separate power and facilities, roughly a millisecond of network latency away from its neighbors.
- Every region has at least three AZs; the separation is designed to survive a facility-level failure while still supporting synchronous replication.
- AZ names are randomized per account; use the stable AZ ID when coordinating across accounts.
- EC2 instances, EBS volumes, subnets, and RDS instances are AZ-scoped; VPCs, security groups, S3, DynamoDB, SQS, and Lambda are region-scoped.
- An EBS volume attaches only within its own AZ, and a subnet belongs to exactly one AZ.
- The simplest deployment is single-AZ, so AZ resilience only exists when someone builds it deliberately.
