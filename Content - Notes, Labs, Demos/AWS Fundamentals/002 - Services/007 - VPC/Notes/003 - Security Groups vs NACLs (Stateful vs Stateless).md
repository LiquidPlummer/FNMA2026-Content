# Security Groups vs NACLs (Stateful vs Stateless)

AWS actually provides two different mechanisms for filtering network traffic within a VPC: **security groups** and **network ACLs (NACLs)**. They sound similar — both define inbound and outbound rules — but they operate at different levels and behave in a genuinely different way. Understanding that difference matters, because it's a common source of confusing, hard-to-diagnose connectivity problems.

## Security Groups

A **security group** operates at the level of individual resources — attached directly to things like EC2 instances or RDS databases, as introduced back in the EC2 lesson. Its rules only ever specify what to *allow*; there's no way to write an explicit "deny" rule in a security group. Anything not explicitly allowed is implicitly denied.

The defining behavioral trait of a security group is that it's **stateful**. If we allow inbound traffic on a given port, the corresponding *response* traffic going back out is automatically allowed too, without needing a matching outbound rule. In other words, a security group tracks the state of a connection: once a request is let in, the reply is automatically let out, treating the request and its response as one related exchange rather than two independent, unrelated flows.

## Network ACLs (NACLs)

A **NACL** operates at the level of an entire subnet — its rules apply to all traffic crossing the subnet boundary, regardless of which specific resource inside the subnet the traffic is headed to or from. Unlike security groups, NACLs support both explicit **allow** and explicit **deny** rules, and rules are evaluated in numbered order, with the first matching rule deciding the outcome.

The defining behavioral trait of a NACL is that it's **stateless**. Allowing inbound traffic on a port does *not* automatically allow the corresponding outbound response — that has to be permitted by a separate, explicit outbound rule. Every direction of traffic has to be accounted for independently; NACLs have no concept of "this outbound traffic is just the reply to that inbound request."

## Why Stateful vs Stateless Trips People Up

This is the single most common practical confusion between the two: someone configures a NACL to allow inbound traffic on a port, tests it, and finds connections still failing — because they forgot that the *outbound* leg (the response) also needs its own explicit rule under a stateless model. With a security group, that same mistake isn't even possible to make, because the statefulness handles the response automatically. Knowing which of the two we're looking at, and therefore which behavior to expect, is essential to troubleshooting connectivity issues efficiently rather than guessing.

## Comparing the Two

| | Security Group | NACL |
|---|---|---|
| **Applies to** | Individual resource (e.g., an EC2 instance) | Entire subnet |
| **Rule types** | Allow only | Allow and explicit deny |
| **Behavior** | Stateful (responses auto-allowed) | Stateless (both directions need rules) |
| **Evaluation** | All rules considered together | Evaluated in numbered order, first match wins |
| **Typical use** | Primary, day-to-day access control | Coarse, subnet-wide guardrails |

## Working Together, Not Instead of Each Other

These two mechanisms aren't competing options — traffic passing into a subnet and reaching a resource is filtered by *both*, in sequence: the NACL governs the subnet boundary, and the security group governs the specific resource. In everyday practice, security groups do most of the fine-grained access-control work, since they're easier to reason about (stateful, allow-only) and scoped precisely to the resource that needs protecting. NACLs are typically used more sparingly, as a coarser, subnet-wide backstop — for example, explicitly blocking a known-bad range of addresses at the subnet level, something a security group's allow-only model can't directly express. Together, along with the public/private subnet split from the first VPC topic, these layers form the defense-in-depth approach that underlies most well-designed AWS network architectures.
