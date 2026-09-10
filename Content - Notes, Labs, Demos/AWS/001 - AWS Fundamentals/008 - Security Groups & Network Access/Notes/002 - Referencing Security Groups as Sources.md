# Referencing Security Groups as Sources

A security group rule can name **another security group** as its source instead of an IP range. This is a small syntactic difference with a large practical effect, and it is the single most useful habit in VPC security design.

---

## What It Means

A rule with a security group source permits traffic from **any network interface currently in that group**, whatever its IP address:

```
DB security group (sg-db)
  inbound: TCP 5432 from sg-app
```

*Any instance in `sg-app` may connect to port 5432, regardless of its IP. Membership is evaluated continuously, not resolved once into addresses.*

The rule expresses identity — "application servers" — rather than location. It does not care which addresses those servers currently hold.

---

## Why It Beats CIDR Ranges

**It survives instance replacement.** Auto Scaling terminates and launches instances constantly, and each new one gets a different private IP. A CIDR-based rule either has to be updated on every change (impossible to automate reliably) or made broad enough to cover the whole subnet — which then also covers everything else in that subnet.

**It is precise without being brittle.** `10.0.128.0/20` means "everything in that subnet," which includes any future workload placed there. `sg-app` means the application tier, and only the application tier.

**It follows the workload.** Moving instances to a different subnet or AZ requires no rule change, because the rule was never about location.

**It documents intent.** Reading `from sg-app` in a database's rules says what the rule is for. Reading `from 10.0.128.0/20` requires cross-referencing subnet allocations to work out the same thing.

---

## The Tiered Pattern

The standard arrangement gives each tier a group that admits only the tier in front of it:

```
Internet
   │ 443
   ▼
sg-alb        inbound: 443 from 0.0.0.0/0
   │ 8080
   ▼
sg-app        inbound: 8080 from sg-alb
   │ 5432
   ▼
sg-db         inbound: 5432 from sg-app
```

*A chain in which each link references the previous group. The only CIDR in the design is at the internet edge, where it is genuinely appropriate.*

This has a useful property: an instance placed in `sg-app` immediately gains the correct access, and one removed from it immediately loses it. Access is controlled by group membership, which is a property of the instance rather than of a rule someone has to remember to edit.

### Self-referencing groups

A group can reference itself, permitting its members to talk to each other:

```
sg-cluster
  inbound: TCP 7000-7001 from sg-cluster
```

*Cluster members reach each other on the gossip ports while nothing outside the group can. This is the standard pattern for Cassandra, Elasticsearch, Redis clusters, and similar peer-to-peer systems.*

---

## Constraints

**Same VPC, or a peered one.** Referencing a group across VPCs works only over an active peering connection or Transit Gateway, and requires the reference to be configured explicitly.

**Not for external sources.** Traffic from outside AWS has no security group, so an office network or a partner's system still needs a CIDR.

**Referenced groups cannot be deleted.** A group named in another group's rules is pinned until that rule is removed. This is usually helpful — it prevents breaking a dependency by accident.

**Not visible in the instance's own view of traffic.** The instance still sees ordinary source IP addresses; the group reference is evaluated in AWS's network layer, not conveyed to the workload.

---

## When a CIDR Is Correct

Security group references are not always the answer:

- **Internet-facing rules** — `0.0.0.0/0` on a load balancer's HTTPS listener.
- **On-premises networks** reaching in over VPN or Direct Connect.
- **Specific external systems** — a partner's fixed address as a `/32`.
- **AWS-managed prefix lists**, such as restricting an ALB to CloudFront's published ranges.

The distinction is simply whether the source is inside the VPC. Inside, use a group reference; outside, a CIDR is the only option available.

---

## Key Takeaways

- A security group rule can name another security group as its source, permitting traffic from any interface in that group regardless of IP.
- Group references survive instance replacement, which CIDR rules do not.
- They are more precise than a subnet CIDR, which would also cover unrelated workloads in that subnet.
- The tiered pattern chains groups so each tier accepts only the one in front of it, with CIDRs used only at the internet edge.
- Self-referencing groups let cluster members communicate with each other while excluding everything else.
- Group references require the same VPC or an explicitly configured peering connection, and cannot represent sources outside AWS.
- Use CIDRs for internet traffic, on-premises networks, and specific external systems.
