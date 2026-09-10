# Resources That Cost Money While Idle

Some AWS resources bill for existing rather than for working. They produce no metrics, appear on no dashboard, and are the most common form of waste in a mature account — because nothing about them draws attention.

---

## The List

**EBS volumes.** Billed on provisioned size, attached or not, whether the instance is running or stopped. **Unattached volumes are the classic case**: an instance is terminated without "delete on termination" set, and its volume persists indefinitely at full price.

**Elastic IPs.** Public IPv4 addresses are billed hourly, including unattached ones. An address left over from a deleted instance is a small permanent charge that never grows and never stops.

**NAT gateways.** Roughly $32/month each, before any traffic. A NAT gateway in a development VPC that nobody uses costs the same as one carrying production traffic.

**Load balancers.** Billed hourly regardless of traffic. An ALB with no registered targets bills the same as one serving requests.

**Snapshots.** Billed per gigabyte of changed data. They accumulate silently — automated backup policies without expiry produce a growing archive nobody reviews.

**Stopped RDS instances.** Stopping stops the instance charge but **not** the storage or backup charge. RDS also **restarts a stopped instance automatically after seven days**, which surprises people who stopped it to save money.

**Provisioned DynamoDB capacity.** Provisioned read and write units bill continuously whether requests arrive or not.

**Old AMIs and their snapshots.** Deregistering an AMI does not delete its underlying snapshots, so they persist and bill.

**Idle Redshift clusters and OpenSearch domains.** Billed by the hour regardless of query volume, and both are expensive per hour.

**Data in infrequent-access storage classes.** S3 Standard-IA and One Zone-IA have a 30-day minimum billing duration and per-gigabyte retrieval charges. Small, frequently accessed objects can cost more there than in Standard.

**KMS customer-managed keys.** About $1/month each, indefinitely. Individually trivial, and easy to accumulate.

---

## Why They Persist

**They produce no signal.** An idle NAT gateway generates no errors and no alerts. Cost Explorer shows it only if someone groups by usage type and looks.

**Deletion feels risky.** An unattached volume with no tags gives no clue whose it is or whether it matters. Nobody deletes it, so it stays forever.

**Nobody owns them.** They are usually left by someone who has moved on, or by a stack that was partially deleted.

**They are individually small.** A $32 NAT gateway does not stand out. Forty of them across development accounts is $1,280/month.

---

## Finding Them

```bash
# Unattached EBS volumes
aws ec2 describe-volumes --filters Name=status,Values=available \
  --query "Volumes[].[VolumeId,Size,CreateTime]" --output table

# Unassociated Elastic IPs
aws ec2 describe-addresses \
  --query "Addresses[?!AssociationId].[PublicIp,AllocationId]" --output table

# Load balancers with no registered targets
aws elbv2 describe-target-groups \
  --query "TargetGroups[].[TargetGroupArn,LoadBalancerArns]" --output table
```

*Three of the most common cases. Each is quick to run and usually returns more than expected in an established account.*

**AWS Trusted Advisor** checks several of these automatically, and its cost optimization checks are available on Business support and above. **Cost Explorer grouped by usage type** shows charges with no corresponding activity — the pattern to look for is a steady, flat line for something nobody is using.

---

## Preventing Accumulation

**Set `DeleteOnTermination` on EBS volumes.** The default is true for root volumes and false for additional ones, which is the source of most orphaned volumes.

**Use infrastructure as code and delete whole stacks.** A stack deletion removes everything it created; manual cleanup misses things by definition.

**Give non-production resources an expiry.** A tag such as `DeleteAfter=2026-12-01`, with automation that acts on it, converts an indefinite resource into a temporary one.

**Schedule non-production shutdown.** Development instances stopped outside working hours cost roughly a third as much. Note that this stops compute charges only — volumes continue billing.

**Apply snapshot lifecycle policies.** Data Lifecycle Manager expires snapshots on a schedule, which prevents unbounded growth.

**Review quarterly.** Run the queries above, and delete what nobody claims. Taking a final snapshot before deleting a volume makes the decision reversible and cheap.

---

## Key Takeaways

- Several resources bill for existing rather than for use: unattached volumes, unassociated Elastic IPs, idle NAT gateways and load balancers, snapshots, and provisioned capacity.
- A stopped EC2 instance still bills for its volumes; a stopped RDS instance still bills for storage and restarts automatically after seven days.
- Deregistering an AMI leaves its snapshots in place and billing.
- These persist because they generate no signal, have no owner, and are individually small.
- Query for unattached volumes, unassociated addresses, and empty target groups — established accounts typically have many.
- Set `DeleteOnTermination`, delete whole stacks rather than individual resources, and apply snapshot lifecycle policies.
- Schedule non-production shutdown for compute, and review idle resources quarterly.
