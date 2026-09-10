# How the Two Layers Combine

Following a single packet through both filters shows exactly where each layer applies, and explains why a change to one can appear to have no effect.

---

## The Path of a Packet

A request from the internet to an instance in a private subnet, arriving via a load balancer:

```
Internet
   │
   ▼
[1] NACL — public subnet, INBOUND       ← stateless, ordered, allow+deny
   │
   ▼
[2] Security group — ALB, INBOUND       ← stateful, allow-only
   │
  ALB
   │
   ▼
[3] Security group — ALB, OUTBOUND
   │
   ▼
[4] NACL — public subnet, OUTBOUND
   │
   ▼
[5] NACL — private subnet, INBOUND
   │
   ▼
[6] Security group — instance, INBOUND
   │
   ▼
Instance
```

*Six checks on the way in. The response traverses them in reverse, but only the NACL checks are re-evaluated — the security groups permit the return automatically.*

The rule for the return path is the important part:

- **Security groups** allow the response automatically, because they are stateful. No outbound rules are consulted for it.
- **NACLs** evaluate the response as new traffic, so the outbound rules must permit the ephemeral port range.

---

## Both Must Allow

A packet is delivered only if **every** layer it crosses permits it. There is no override — an allow in a security group does not overcome a NACL deny, and vice versa.

For traffic to succeed:

- Every NACL it crosses must allow it, **in both directions**.
- Every security group it reaches must allow it inbound (and outbound, if the source's group restricts egress).

Conversely, a single deny anywhere stops it. This is why "I opened the security group and it still doesn't work" is common — a NACL, an egress rule on the source, or a routing gap is blocking it further along.

---

## Traffic That Skips a Layer

Two cases are worth knowing, because they explain otherwise puzzling behavior.

**Same-subnet traffic skips NACLs entirely.** Two instances in one subnet communicate without their packets crossing the subnet boundary, so NACLs never evaluate them. Only security groups apply. A NACL rule intended to isolate those instances from each other has no effect.

**Nothing skips security groups.** Every packet to or from a network interface is evaluated by its groups, including traffic between instances in the same subnet.

---

## A Worked Failure

A common scenario: an application server cannot reach its database.

| Layer | Configuration | Result |
|---|---|---|
| App instance SG, outbound | Allow all | Passes |
| App subnet NACL, outbound | Allow 5432 to `10.0.128.0/20` | Passes |
| DB subnet NACL, inbound | Allow 5432 from `10.0.0.0/20` | Passes |
| DB instance SG, inbound | Allow 5432 from `sg-app` | Passes |
| DB subnet NACL, **outbound** | Allow 5432 only | **Blocked** |

The request arrives. The database processes it. The response is sent to the application's ephemeral port — say 45231 — and the DB subnet's outbound NACL, which allows only port 5432, drops it.

The symptom is a connection that hangs and times out rather than being refused. Every security group is correct, and the NACL's inbound rules are correct too. The missing piece is the outbound ephemeral range on the NACL:

```
Outbound  200  TCP  1024-65535  10.0.0.0/20  ALLOW
```

*Permits responses back to the application subnet's ephemeral ports. Without it, only the direction of the initial request works.*

A hang rather than an immediate rejection is itself a useful signal: it usually means something is silently dropping packets, which points at NACLs or routing rather than at security groups.

---

## Key Takeaways

- A packet crosses NACLs at each subnet boundary and security groups at each network interface; every layer must allow it.
- Security groups permit return traffic automatically; NACLs evaluate it as new traffic and need outbound ephemeral-port rules.
- No layer overrides another — a single deny anywhere blocks the packet.
- Traffic between instances in the same subnet bypasses NACLs entirely and is filtered only by security groups.
- A connection that hangs rather than being refused usually indicates silent dropping, pointing at NACLs or routing.
- When a security group change seems to have no effect, check the other layers before changing it further.
