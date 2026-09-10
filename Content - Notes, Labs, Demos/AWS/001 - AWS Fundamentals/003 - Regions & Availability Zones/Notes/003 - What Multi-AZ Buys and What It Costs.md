# What Multi-AZ Buys and What It Costs

"Multi-AZ" is used loosely to mean several different things. Being precise about which one is in play — and what each one costs — is the difference between paying for resilience and paying for nothing.

---

## Three Different Meanings

**Spreading stateless capacity.** An Auto Scaling group with instances in three AZs behind a load balancer. If one AZ fails, its instances fail their health checks, the load balancer stops sending traffic there, and the group replaces them elsewhere. Capacity drops until replacements launch, but the service stays up. This is the cheapest form of multi-AZ, because the instances would exist anyway — we are only choosing where to put them.

**Replicated state with failover.** RDS Multi-AZ maintains a standby copy in a second AZ, synchronously replicated. On failure, RDS promotes the standby and repoints the DNS name, typically in a minute or two. This form **doubles the database cost**, because the standby is a full instance that serves no read traffic — its only job is to exist.

**Multi-AZ by default.** S3, DynamoDB, SQS, and other region-scoped services already replicate across AZs internally, at no additional cost and with nothing to configure. Data stored in them survives an AZ failure automatically.

---

## What It Actually Protects Against

Multi-AZ protects against the loss of a data center: power, cooling, network, or physical failure confined to one zone. That is a real category of event, and it happens.

It does not protect against:

- **A region-wide failure** — a control plane problem in a region affects all its AZs at once.
- **Bad application code** — deployed to every AZ simultaneously.
- **A bad configuration change** — applied everywhere at once.
- **Deleted data** — replication faithfully replicates a `DELETE`. That is what backups are for.

Multi-AZ is a narrow protection against one specific class of infrastructure failure. It is worth having, and it is not a general answer to "is this resilient?"

---

## The Costs

Three distinct costs, and only the first is obvious.

**Duplicated resources.** RDS Multi-AZ costs twice the instance price. A NAT gateway per AZ — the standard pattern, since a single NAT gateway is itself a single point of failure — multiplies both the hourly charge and the per-gigabyte processing charge.

**Cross-AZ data transfer.** This is the one that surprises people. Traffic between AZs is billed **in both directions**, per gigabyte. A chatty application whose web tier is randomly load-balanced across three AZs while its database sits in one will send most of its query traffic across zone boundaries, and pay for it continuously. It is a small per-gigabyte number attached to a very large number of gigabytes.

**Complexity.** Every stateful component needs an answer to "what happens when this AZ disappears?" Sessions in local memory, files on a local disk, and singleton background jobs all become problems that did not exist in a single-AZ design.

---

## Deciding

The honest version of the decision:

| Component | Usual answer | Why |
|---|---|---|
| Stateless app servers | Multi-AZ, always | Nearly free — the instances exist regardless |
| Load balancer | Multi-AZ, always | ALBs are multi-AZ by design |
| Production database | Multi-AZ if downtime is costly | Doubles cost; buys minutes instead of hours of recovery |
| Non-production database | Usually single-AZ | Restore from snapshot is acceptable there |
| S3, DynamoDB, SQS | Already multi-AZ | No decision to make |

For a database, the question is concrete: what does an hour of downtime cost, and how does that compare with running a second instance year-round? For many production systems the answer is clearly yes. For a development environment it is clearly no. Applying Multi-AZ everywhere by reflex is how non-production environments end up costing as much as production.

---

## Key Takeaways

- "Multi-AZ" can mean spreading stateless capacity, replicating state with failover, or a service that is already multi-AZ internally — the costs differ enormously.
- Spreading stateless instances across AZs is nearly free; RDS Multi-AZ roughly doubles database cost for a standby that serves no traffic.
- S3, DynamoDB, and SQS replicate across AZs with no configuration or extra charge.
- Multi-AZ protects against a data center failure — not against region failures, bad deploys, or deleted data.
- Cross-AZ data transfer is billed per gigabyte in both directions and accumulates continuously in chatty applications.
- Decide per component by comparing the cost of downtime with the cost of the standby, rather than applying it uniformly.
