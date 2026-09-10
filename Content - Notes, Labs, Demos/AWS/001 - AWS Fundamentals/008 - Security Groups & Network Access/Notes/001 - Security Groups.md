# Security Groups

A **security group** is a virtual firewall attached to a network interface — in practice, to an instance, a load balancer, or an RDS database. It is the primary network access control in a VPC, and its two defining properties are that it is **stateful** and that it is **allow-only**.

---

## Stateful

A security group tracks connections. If an inbound request is allowed, the response is automatically allowed out, regardless of outbound rules. The reverse also holds: an allowed outbound connection has its replies permitted back in.

This is why security group rules are usually written in one direction only. A web server needs an inbound rule for port 443; it needs no outbound rule for the responses, because the return traffic is part of a connection already permitted.

Statefulness is the main practical difference between security groups and network ACLs, and it is what makes security groups much easier to reason about.

---

## Allow-Only

**There is no deny rule.** A security group contains only allow rules, and anything not explicitly allowed is denied.

This has a significant consequence: **security groups are purely additive**. An instance can have several (up to five by default), and the effective permission is the union of all their rules. Adding a security group can only ever permit more traffic, never less.

So a rule cannot be used to carve an exception out of a broader rule. "Allow the office network but block one address in it" is not expressible in a security group. That requires a network ACL, which does support deny.

---

## Rule Structure

Each rule specifies a protocol, a port range, and a source (inbound) or destination (outbound):

| Type | Protocol | Port range | Source |
|---|---|---|---|
| HTTPS | TCP | 443 | `0.0.0.0/0` |
| HTTP | TCP | 80 | `0.0.0.0/0` |
| SSH | TCP | 22 | `10.0.0.0/16` |
| PostgreSQL | TCP | 5432 | `sg-0app123` |

The source can be:

- **A CIDR block** — `0.0.0.0/0` for anywhere, `10.0.0.0/16` for the VPC, a specific address as `/32`.
- **Another security group** — covered in the next note, and usually the better option.
- **A prefix list** — a managed set of CIDRs, including AWS-published ones for services like CloudFront.

---

## Defaults

**A new security group has no inbound rules and one outbound rule allowing all traffic.** So by default nothing can reach the resource, and the resource can reach anything.

That outbound default is worth a moment's thought. It means a compromised instance can connect anywhere — which is how data leaves and how malware retrieves instructions. Restricting egress is more work than leaving it open, and it is one of the more effective controls available. In a subnet where the only legitimate outbound traffic is to an S3 endpoint and an internal API, saying so explicitly meaningfully limits what a compromise can accomplish.

---

## Scope and Lifecycle

- **Security groups are VPC-scoped.** One created in a VPC cannot be used in another.
- **They attach to network interfaces**, so a multi-interface instance can have different groups per interface.
- **Rule changes take effect immediately**, including on existing connections.
- **A group cannot be deleted while in use**, either by a resource or by another group's rules.

That last point is the usual reason a "leftover" security group cannot be deleted: something still references it.

---

## A Worked Example

Three tiers, each with its own group:

```
ALB security group (sg-alb)
  inbound:  443 from 0.0.0.0/0
  outbound: 8080 to sg-app

App security group (sg-app)
  inbound:  8080 from sg-alb
  outbound: 5432 to sg-db

DB security group (sg-db)
  inbound:  5432 from sg-app
  outbound: (none needed)
```

*Each tier accepts traffic only from the tier in front of it. No CIDR ranges appear except at the internet-facing edge, so the rules stay correct as instances are replaced.*

Nothing here references an instance IP address. As Auto Scaling replaces instances, the rules continue to hold — which is the reason for referencing security groups as sources.

---

## Key Takeaways

- Security groups are stateful: return traffic for an allowed connection is permitted automatically, so rules are usually written in one direction.
- They contain allow rules only; anything not allowed is denied, and there is no way to express an exception.
- Multiple security groups combine additively as a union, so adding one can only permit more traffic.
- A new security group denies all inbound and permits all outbound; restricting egress limits what a compromised instance can do.
- Sources can be CIDR blocks, other security groups, or managed prefix lists.
- Security groups are VPC-scoped, apply to network interfaces, take effect immediately, and cannot be deleted while referenced.
