# Subnets, Route Tables & the Internet Gateway

The most important thing to understand about subnets: **there is no "public" checkbox.** Public and private are outcomes of routing, not settings, and knowing that makes VPC troubleshooting far more direct.

---

## Subnets

A **subnet** is a slice of the VPC's address range bound to exactly one Availability Zone. Choosing a subnet for a resource is how we choose its AZ.

- A subnet is in one AZ and cannot span AZs.
- A resource is in one subnet.
- A subnet has one route table (a table may serve many subnets).
- Subnet CIDRs within a VPC cannot overlap.

Because a subnet is AZ-bound, a multi-AZ deployment always means multiple subnets — typically one per AZ per tier.

---

## Route Tables

A **route table** is a list of destination CIDRs and targets. Traffic leaving a subnet is matched against it, most specific prefix first.

A default route table for a public subnet:

| Destination | Target |
|---|---|
| `10.0.0.0/16` | `local` |
| `0.0.0.0/0` | `igw-0abc123` |

The `local` route is created automatically, covers the VPC's own range, and **cannot be removed or overridden**. It is why every resource in a VPC can reach every other resource at the routing level, regardless of subnet. (Security groups and NACLs then decide whether that traffic is permitted — routing and filtering are separate concerns.)

The second line is what makes this subnet public: a default route pointing at an internet gateway.

Longest-prefix matching means a more specific route wins. A table with both `0.0.0.0/0 → igw` and `10.1.0.0/16 → pcx-peering` sends traffic for `10.1.x.x` over the peering connection and everything else to the internet.

---

## The Internet Gateway

An **internet gateway (IGW)** is a horizontally scaled, redundant component attached to a VPC that allows traffic to and from the internet. There is at most one per VPC, and it does not constrain bandwidth or introduce a failure point.

It does two things:

1. Routes traffic between the VPC and the internet.
2. Performs network address translation for instances with public IPv4 addresses, mapping the private address to the public one.

Attaching an IGW changes nothing on its own. It has effect only when a route table points at it.

---

## Public vs Private, Precisely

**A subnet is public if its route table has a route to an internet gateway.** That is the entire definition.

```
Public subnet                        Private subnet
route table:                         route table:
  10.0.0.0/16 → local                  10.0.0.0/16 → local
  0.0.0.0/0   → igw-0abc123            0.0.0.0/0   → nat-0def456
```

*Identical subnets differing only in where the default route points — that difference is what "public" and "private" mean.*

For an instance in a public subnet to actually be reachable from the internet, three things must all be true:

1. The subnet routes `0.0.0.0/0` to an internet gateway.
2. The instance has a **public IPv4 address** or an Elastic IP.
3. Security groups and network ACLs permit the traffic.

Missing the second is a frequent confusion: an instance in a correctly configured public subnet, with permissive security groups, that is still unreachable — because it has no public IP. The subnet's `MapPublicIpOnLaunch` setting controls whether one is assigned automatically.

---

## A Typical Arrangement

```
Internet
   │
   ▼
Internet gateway
   │
   ├── Public subnet  10.0.0.0/20   ── ALB, NAT gateway
   │        route: 0.0.0.0/0 → igw
   │
   └── Private subnet 10.0.128.0/20 ── application servers, RDS
            route: 0.0.0.0/0 → nat
```

*Load balancers and NAT gateways occupy the public subnets; everything holding data or running application code sits in private subnets, reachable only through the load balancer.*

The main route table is used by any subnet not explicitly associated with another. Leaving the main table with an internet route is risky — a new subnet then becomes public by default, silently. Making the main route table private-only is a small change that prevents accidental exposure.

---

## Key Takeaways

- A subnet belongs to exactly one Availability Zone; choosing a subnet chooses an AZ.
- Route tables match destinations by longest prefix; the `local` route covers the VPC range and cannot be removed.
- An internet gateway attaches to a VPC and only takes effect when a route table points to it.
- A subnet is public purely because its route table routes `0.0.0.0/0` to an internet gateway — there is no public flag.
- Internet reachability also requires a public IP or Elastic IP on the instance, plus permissive security groups and NACLs.
- Keep the main route table free of internet routes so new subnets do not become public by accident.
