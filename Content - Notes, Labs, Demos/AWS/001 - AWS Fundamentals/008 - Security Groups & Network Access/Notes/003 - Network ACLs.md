# Network ACLs

A **network ACL (NACL)** is the second filtering layer in a VPC. It differs from a security group in three ways that matter: it is **stateless**, it is **subnet-level**, and it is **evaluated in rule-number order** with support for deny.

---

## Stateless

A NACL does not track connections. Inbound and outbound are evaluated entirely independently, which means **both directions must be allowed explicitly**.

This is the property that causes the most trouble. An inbound rule permitting port 443 is not sufficient for a working HTTPS connection — the response also needs an outbound rule.

Responses go to **ephemeral ports**, chosen by the client from a high range. Since the specific port is not known in advance, the outbound rule must cover the range:

| Client type | Ephemeral range |
|---|---|
| Linux kernels | 32768–60999 |
| Windows (modern) | 49152–65535 |
| NAT gateways | 1024–65535 |
| ELB | 1024–65535 |

In practice, an outbound allow for TCP 1024–65535 is the usual rule, which is broad enough to be worth noticing.

```
Inbound  100  TCP  443          0.0.0.0/0    ALLOW
Outbound 100  TCP  1024-65535   0.0.0.0/0    ALLOW
```

*Both rules are required for a single HTTPS request to complete. The security group equivalent would be one inbound rule.*

---

## Subnet-Level

A NACL is associated with a **subnet**, not an instance, so it applies to everything in that subnet uniformly. Every subnet has exactly one NACL; if none is assigned, it uses the VPC's default.

Because it is subnet-level, a NACL cannot distinguish between workloads sharing a subnet. It also does not filter traffic between two instances in the *same* subnet — that traffic never crosses the subnet boundary, so the NACL never sees it. Security groups do filter it.

---

## Numbered Rules, Evaluated in Order

NACL rules are numbered, and evaluation proceeds from lowest to highest. **The first matching rule wins, and evaluation stops there.**

```
Inbound rules
 90   DENY   TCP  22   203.0.113.9/32
100   ALLOW  TCP  22   203.0.113.0/24
110   ALLOW  TCP  443  0.0.0.0/0
 *    DENY   all       0.0.0.0/0
```

*Rule 90 blocks one address before rule 100 can allow its subnet — the exception a security group cannot express. The implicit `*` rule denies anything unmatched.*

Order dependence is what makes NACLs both capable and error-prone. Inserting a rule at the wrong number silently changes behavior, so conventional practice is to number in increments of 10 or 100 to leave insertion room.

---

## Default NACLs

**The default NACL allows all inbound and all outbound traffic.** It is effectively transparent, which is why most people never encounter NACLs at all — the default does not block anything, leaving security groups as the only active filter.

A **custom** NACL is the opposite: it denies everything until rules are added. Creating one and associating it with a subnet without adding rules cuts off all traffic to that subnet immediately, and that is a genuinely confusing outage to diagnose.

---

## Security Groups vs NACLs

| | Security group | Network ACL |
|---|---|---|
| Applies to | Network interface | Subnet |
| State | Stateful | Stateless |
| Rules | Allow only | Allow and deny |
| Evaluation | All rules, union | In number order, first match wins |
| Return traffic | Automatic | Must be allowed explicitly |
| Default (new) | Deny in, allow out | Deny everything |
| Default (VPC default) | Allow within group | Allow everything |

---

## When to Use Them

Most VPCs get by on security groups alone, with default NACLs left permissive. That is a reasonable position — security groups are more precise, easier to reason about, and less prone to accidental outages.

NACLs earn their place in a few specific cases:

**Blocking a specific address.** Security groups cannot deny. Blocking an abusive IP is a NACL rule.

**A coarse subnet-wide guarantee.** A NACL on a database subnet denying everything except the application subnet's range is a backstop that survives a mistaken security group change.

**Compliance requirements** that specify subnet-level controls explicitly.

The recommended default is to treat security groups as the primary control and NACLs as a coarse safety net — and if custom NACL rules are added, to document the ephemeral port requirement, because that is what the next person will trip over.

---

## Key Takeaways

- NACLs are stateless, so inbound and outbound rules are both required, with responses using ephemeral high ports.
- They attach to subnets, apply uniformly to everything in them, and do not filter traffic within a single subnet.
- Rules are numbered and evaluated lowest to highest, with the first match deciding — which allows deny rules and exceptions.
- The default NACL allows all traffic; a newly created custom NACL denies all traffic until rules are added.
- Security groups are the primary control in most designs; NACLs suit IP blocking, coarse subnet guarantees, and compliance mandates.
- Number rules in increments to leave room for insertion, since order changes behavior.
