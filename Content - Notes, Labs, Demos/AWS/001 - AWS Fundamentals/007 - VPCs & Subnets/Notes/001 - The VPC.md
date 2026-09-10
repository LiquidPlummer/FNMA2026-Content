# The VPC

A **VPC** (Virtual Private Cloud) is a private network we define inside a region. Every EC2 instance, RDS database, and load balancer lives in one, and the VPC determines what can reach what.

---

## What a VPC Is

A VPC is a logically isolated section of AWS networking with an IP address range we choose. Inside it we create subnets, route tables, and gateways, and attach resources to those subnets.

The isolation is real. By default, two VPCs cannot communicate at all — not across accounts, not within one account, not even in the same region. Connectivity between them requires explicit configuration (peering, Transit Gateway, or PrivateLink).

Key properties:

- **A VPC is region-scoped.** It spans every Availability Zone in its region, but does not extend beyond the region.
- **A VPC belongs to one account.** Sharing across accounts requires AWS Resource Access Manager.
- **The IP range is chosen at creation.** It can be extended with additional ranges later, but the original block cannot be changed or shrunk.

---

## The Default VPC

Every account starts with a **default VPC** in each region, pre-configured for convenience:

- A `172.31.0.0/16` address range
- A public subnet in each Availability Zone
- An internet gateway, with routes already in place
- Instances receive public IP addresses automatically

This is why launching an instance in a fresh account works immediately with no networking setup. It is also why so many instances end up directly on the internet without anyone deciding they should be — every subnet in the default VPC is public.

The default VPC is fine for experimentation. For anything real, create a VPC deliberately, so subnet layout, routing, and internet exposure are decisions rather than inherited defaults.

---

## What a VPC Contains

```
VPC  10.0.0.0/16   (region: us-east-1)
│
├── Subnet 10.0.1.0/24   (us-east-1a, public)   ──► route table ──► internet gateway
├── Subnet 10.0.2.0/24   (us-east-1b, public)   ──► route table ──► internet gateway
├── Subnet 10.0.11.0/24  (us-east-1a, private)  ──► route table ──► NAT gateway
├── Subnet 10.0.12.0/24  (us-east-1b, private)  ──► route table ──► NAT gateway
│
├── Internet gateway
├── NAT gateway (in a public subnet)
├── Security groups
└── Network ACLs
```

*A conventional layout: public and private subnets paired across two Availability Zones, with routing rather than a flag determining which is which.*

The pieces:

- **Subnets** divide the VPC's address range; each belongs to exactly one Availability Zone.
- **Route tables** decide where traffic leaving a subnet goes.
- **An internet gateway** connects the VPC to the internet.
- **NAT gateways** let private subnets reach outward without being reachable inward.
- **Security groups and network ACLs** filter traffic.

---

## Why Not Just Use the Default

Three concrete reasons a purpose-built VPC is worth the effort:

**Private subnets.** Databases and application servers should not be reachable from the internet. The default VPC has no private subnets, so everything in it is one security group rule away from exposure.

**Address planning.** `172.31.0.0/16` may collide with an on-premises network or another VPC, which blocks peering and VPN connectivity later. Choosing a range that fits an overall plan avoids a painful rebuild.

**Explicit configuration.** A VPC defined in a template is reviewable and reproducible. The default VPC is a set of decisions AWS made, which nobody on the team has examined.

---

## Key Takeaways

- A VPC is an isolated virtual network in one region, spanning that region's Availability Zones.
- VPCs cannot communicate with each other without explicit peering, Transit Gateway, or PrivateLink configuration.
- The IP range is chosen at creation; additional ranges can be added, but the original cannot be changed.
- Every account has a default VPC in each region where all subnets are public and instances get public IPs automatically.
- The default VPC is suitable for experimentation, not for real workloads.
- A VPC contains subnets, route tables, gateways, security groups, and network ACLs, and routing is what makes a subnet public or private.
