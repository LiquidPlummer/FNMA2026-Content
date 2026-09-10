# CIDR Blocks & Sizing

A VPC's address range is chosen once and is difficult to change afterwards. Sizing it correctly requires understanding CIDR notation and anticipating growth we cannot yet see.

---

## CIDR Notation

A **CIDR block** is an address plus a prefix length: `10.0.0.0/16`. The prefix length says how many leading bits are fixed; the remaining bits are available for hosts.

| CIDR | Fixed bits | Addresses | Usable in AWS |
|---|---|---|---|
| `/16` | 16 | 65,536 | 65,531 |
| `/20` | 20 | 4,096 | 4,091 |
| `/24` | 24 | 256 | 251 |
| `/28` | 28 | 16 | 11 |

The rule of thumb: each step up in prefix length halves the range. A `/24` is half a `/23`.

**AWS reserves five addresses in every subnet**, which is why the usable count is always five short:

- `.0` — network address
- `.1` — the VPC router
- `.2` — DNS
- `.3` — reserved for future use
- `.255` — broadcast address (unused, but reserved)

That matters at small sizes. A `/28` gives eleven usable addresses, and AWS does not permit subnets smaller than `/28`.

---

## Choosing a VPC Range

Two constraints apply.

**Use private address space** (RFC 1918), or the range will conflict with real internet addresses:

- `10.0.0.0/8`
- `172.16.0.0/12`
- `192.168.0.0/16`

**AWS allows `/16` to `/28`** for a VPC.

The usual choice is a `/16` from the `10.x` space. It provides 65,536 addresses, costs nothing extra, and leaves room to grow. There is no charge for unused address space, so a range that is too small is a real problem and a range that is too large is not.

### Avoiding overlap

This is the decision that causes the most pain later. Two networks with overlapping ranges cannot be connected — VPC peering, Transit Gateway, and VPN all require non-overlapping CIDRs.

So the range must be chosen against everything it might ever connect to: other VPCs in the organization, on-premises networks, partner networks, and future acquisitions. In practice this means an organization-wide allocation plan:

```
10.0.0.0/16    production,  us-east-1
10.1.0.0/16    production,  eu-west-1
10.10.0.0/16   staging,     us-east-1
10.20.0.0/16   development, us-east-1
192.168.0.0/16 on-premises
```

*Each environment and region gets a distinct block, so any pair can be connected later without renumbering.*

The failure mode is picking `10.0.0.0/16` for every VPC because it is the obvious default, then discovering that none of them can peer.

---

## Dividing Into Subnets

Subnets carve the VPC range into per-AZ pieces. A workable pattern for a `/16` across three AZs:

```
VPC            10.0.0.0/16     (65,536 addresses)

Public   AZ-a  10.0.0.0/20     (4,091 usable)
Public   AZ-b  10.0.16.0/20
Public   AZ-c  10.0.32.0/20
Private  AZ-a  10.0.128.0/20
Private  AZ-b  10.0.144.0/20
Private  AZ-c  10.0.160.0/20
                               ~half the range still unallocated
```

*Public subnets grouped low, private grouped high, with a large gap left for expansion — new subnets can be added without fragmenting the plan.*

Sizing guidance:

- **Private subnets should be larger.** Most resources belong there, and Auto Scaling can expand quickly.
- **Public subnets can be small.** They usually hold only load balancers and NAT gateways.
- **Leave gaps.** Contiguous allocation makes later additions awkward.
- **Watch for address-hungry services.** EKS assigns an IP per pod, and Lambda in a VPC consumes addresses per concurrent execution. Both exhaust small subnets faster than expected.

---

## Changing It Later

The original CIDR **cannot be changed or shrunk**. The only remedies:

- **Add secondary CIDR blocks.** A VPC can have several, which is the standard escape hatch when the first one fills. New subnets can use the new range, though the result is less tidy.
- **Rebuild the VPC.** This means recreating every resource in it and migrating traffic.

Since additional address space is free, the practical advice is to allocate a `/16`, use a fraction of it, and leave the rest.

---

## Key Takeaways

- CIDR notation fixes a prefix length; each additional bit halves the usable range.
- AWS reserves five addresses per subnet, and subnets cannot be smaller than `/28`.
- Use RFC 1918 private space; VPC ranges must be between `/16` and `/28`.
- Overlapping ranges cannot be peered or connected by VPN, so plan allocations organization-wide before creating VPCs.
- Size private subnets larger than public ones, and leave unallocated gaps for expansion.
- EKS and VPC-attached Lambda consume addresses quickly and can exhaust undersized subnets.
- The original CIDR cannot be changed; secondary CIDR blocks are the only non-destructive fix.
