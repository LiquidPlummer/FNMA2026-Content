# Diagnosing a Failed Connection

"I can't connect" has a small number of causes and a reliable order for checking them. Working the list top to bottom is faster than guessing, because the early checks are cheap and rule out the most common problems.

---

## Read the Symptom First

The failure mode narrows the search considerably before any configuration is examined:

| Symptom | Likely cause |
|---|---|
| **Connection refused** (immediate) | Reached the host; nothing listening on that port |
| **Connection timeout** (hangs) | Packets dropped — security group, NACL, or routing |
| **Host not found** | DNS resolution, not connectivity |
| **TLS/certificate error** | Connected successfully; the problem is the certificate |
| **403 or 401** | Connected successfully; the problem is authorization |

The refused/timeout distinction does the most work. **Refused** means the packet arrived and the host actively rejected it — the network path is fine, and the service is not listening. **Timeout** means something dropped the packet silently, which is what firewalls do.

---

## The Ordered Checklist

**1. Is the service actually running and listening?**

```bash
ss -tlnp | grep 5432
```

*Confirms something is listening on the port. A service bound to `127.0.0.1` rather than `0.0.0.0` accepts only local connections — a frequent cause of "refused" from another host.*

**2. Is DNS resolving to what we expect?** Check that the hostname resolves, and to a private address if the connection should be internal.

**3. Is there a route?** The source subnet's route table needs an entry covering the destination. Within a VPC the `local` route covers this automatically. Across VPCs, to on-premises, or to the internet, a specific route must exist — and for peering, **both** sides need routes.

**4. Does the source security group allow it outbound?** Usually yes, since the default is allow-all, but egress restrictions are increasingly common and easy to forget.

**5. Does the destination security group allow it inbound?** Check the protocol, port, and source. If the source is another security group, confirm the instance is genuinely in that group.

**6. Do the NACLs allow it in both directions?** Both subnets, both directions, remembering the ephemeral port range on the return path.

**7. Is a public IP required and present?** An instance in a public subnet with no public IP is unreachable from the internet regardless of rules.

**8. Is a host firewall in the way?** `iptables`, `firewalld`, or Windows Firewall inside the instance are invisible from the AWS console and block traffic that AWS has already permitted.

---

## Tools That Shortcut the List

**VPC Reachability Analyzer** evaluates the entire path between two resources and reports the exact component blocking it:

```bash
aws ec2 create-network-insights-path \
  --source i-0app123 --destination i-0db456 \
  --destination-port 5432 --protocol tcp
# then start an analysis on the returned path ID
```

*Analyzes the configuration path without sending packets, naming the specific security group or NACL rule responsible for a block. It is the fastest route to an answer for anything non-obvious.*

**VPC Flow Logs** record accepted and rejected traffic per interface:

```
2 123456789012 eni-0abc 10.0.128.5 10.0.1.20 45231 5432 6 10 840 ... REJECT OK
```

*The trailing `REJECT` shows the packet was dropped, and the addresses and ports identify which flow — enough to determine which layer is responsible.*

Flow logs record whether traffic was accepted or rejected but not which rule decided, so they confirm the direction and layer rather than naming the rule.

---

## The Usual Culprits

In rough order of how often they turn out to be the answer:

1. **Security group missing the inbound rule** — or allowing the wrong port.
2. **Service bound to `127.0.0.1`** instead of all interfaces.
3. **No public IP** on an instance expected to be internet-reachable.
4. **NACL missing the outbound ephemeral range** — produces a hang, and is hard to spot.
5. **Wrong security group referenced** — the source instance is not in the group the rule names.
6. **Missing route** — especially the second half of a peering connection.
7. **Host firewall** inside the instance.

---

## Working It Efficiently

Two habits save the most time. **Start from the symptom**, since refused and timeout point in different directions and eliminate half the list immediately. And **check both directions**, because stateless NACLs and egress rules mean a path can work outbound and fail on the return — which presents as a hang rather than an obvious block.

---

## Key Takeaways

- "Connection refused" means the packet arrived and nothing was listening; "timeout" means something dropped it silently.
- Check in order: service listening, DNS, routes, source egress, destination ingress, NACLs both ways, public IP, host firewall.
- A service bound to `127.0.0.1` is a common cause of refused connections from other hosts.
- VPC Reachability Analyzer identifies the exact blocking component without sending traffic.
- VPC Flow Logs show accepted and rejected flows, confirming direction and layer though not the specific rule.
- Missing NACL outbound ephemeral rules and missing public IPs are the two most easily overlooked causes.
