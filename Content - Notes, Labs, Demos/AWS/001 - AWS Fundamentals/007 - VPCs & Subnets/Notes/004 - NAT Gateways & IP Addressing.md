# NAT Gateways & IP Addressing

Private subnets still need outbound internet access — for package updates, API calls, and license checks. NAT gateways provide it, and they are one of the more expensive components in a typical VPC.

---

## The Problem NAT Solves

An instance in a private subnet has no route to the internet, so it cannot download a package or call an external API. Giving it a public IP and an internet gateway route would solve that, but it would also make the instance reachable from the internet — which is exactly what the private subnet was for.

**Network Address Translation** provides asymmetry: outbound connections work, inbound connections do not. The NAT device substitutes its own address on the way out and reverses the substitution on the replies. Since it only tracks connections it initiated, unsolicited inbound traffic has nowhere to go.

---

## NAT Gateways

A **NAT gateway** is a managed component placed in a **public** subnet, with private subnets routing their default traffic to it.

```
Private subnet                Public subnet              Internet
  instance ──0.0.0.0/0──►  NAT gateway  ──0.0.0.0/0──►  IGW ──►
  10.0.128.5                (Elastic IP)
```

*The NAT gateway sits in a public subnet with an Elastic IP; the private subnet's route table sends outbound traffic to it rather than to the internet gateway.*

Two placement requirements catch people out:

- **The NAT gateway must be in a public subnet** — a subnet with an internet gateway route. Putting it in a private subnet produces a configuration that looks right and moves no traffic.
- **The private subnet's route table must point to the NAT gateway**, not the internet gateway.

A NAT gateway is **AZ-scoped**. One in `us-east-1a` is a single point of failure for every subnet routing to it, and traffic from other AZs also incurs cross-AZ transfer charges. The standard pattern is one NAT gateway per AZ, with each private subnet routing to the one in its own zone.

---

## The Cost

NAT gateways are frequently among the top line items on a VPC bill, from two charges:

- **An hourly charge** per gateway — roughly $32/month each, so three AZs cost about $97/month before any traffic.
- **A per-gigabyte processing charge** on everything passing through, roughly $0.045/GB, *on top of* any data transfer charge.

A workload pulling container images or large datasets through NAT can spend more on processing than on the compute doing the work.

Three ways to reduce it:

**VPC endpoints for AWS services.** Traffic to S3 and DynamoDB through a gateway endpoint bypasses NAT entirely and is free. For a workload reading heavily from S3, this is the single largest saving available.

**Fewer NAT gateways in non-production.** One NAT gateway for a development VPC, accepting the AZ dependency, cuts the fixed cost by two thirds.

**A NAT instance for very low volume.** A small EC2 instance running NAT software costs less per hour but must be managed, patched, and made highly available. Rarely worth it outside development.

---

## IP Addressing

Three address types, with different lifecycles.

**Private IP.** Assigned from the subnet's range, and kept for the life of the instance. It survives stop and start. Instances communicate with each other over private IPs, and that traffic within an AZ is free.

**Public IP.** Assigned from AWS's pool when the subnet has `MapPublicIpOnLaunch` enabled. The important property: **it is released when the instance stops, and a different one is assigned on start.** Anything referencing that address breaks. Public IPv4 addresses are now charged hourly whether or not they are attached to a running instance.

**Elastic IP (EIP).** A public IPv4 address allocated to the account and kept until released. It can be moved between instances, which is what makes it useful for failover and for addresses that appear in someone else's firewall rules.

| | Private IP | Public IP | Elastic IP |
|---|---|---|---|
| Survives stop/start | Yes | No | Yes |
| Movable between instances | No | No | Yes |
| Charged | No | Hourly | Hourly |

An unattached Elastic IP is still billed — an allocated address doing nothing costs the same as one in use. Leftover EIPs from deleted instances are a small, persistent, easily overlooked charge.

---

## Preferring Names Over Addresses

Depending on a specific IP address is fragile. Better mechanisms:

- **Load balancer DNS names** for reaching a fleet.
- **Private DNS names** within a VPC, which resolve to private IPs automatically.
- **Route 53 records** for anything that must have a stable name.

An Elastic IP is appropriate when an external party has hard-coded the address in a firewall rule and cannot be persuaded to use a hostname.

---

## Key Takeaways

- NAT allows outbound connections from private subnets while blocking unsolicited inbound traffic.
- A NAT gateway must sit in a public subnet, and private route tables must point at it rather than the internet gateway.
- NAT gateways are AZ-scoped; one per AZ avoids both a failure point and cross-AZ transfer charges.
- Costs are hourly plus per-gigabyte processing, and heavy traffic through NAT is a common source of unexpected spend.
- Gateway VPC endpoints for S3 and DynamoDB bypass NAT and are free.
- Private IPs persist across stop/start; automatically assigned public IPs do not and change on restart.
- Elastic IPs are stable and movable, and are billed even when unattached.
