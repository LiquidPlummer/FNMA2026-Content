# High Availability & Fault Tolerance

Hardware fails, networks have outages, and even entire data centers can go offline. **High availability** and **fault tolerance** are two related but distinct ideas about how a system keeps running when that happens.

## High Availability

**High availability (HA)** means a system is designed to minimize downtime, typically by removing single points of failure. An HA system doesn't promise that nothing will ever fail — it promises that when something does fail, the system keeps running (or recovers quickly) instead of going down entirely.

The classic way to achieve this is **redundancy**: running multiple copies of a component so that if one fails, another is already there to take over. A web application running on two servers in two separate locations is more highly available than the same application running on a single server — if one location has a problem, the other keeps serving traffic.

## Fault Tolerance

**Fault tolerance** is a stronger guarantee: the system continues operating with *no interruption*, even when a component fails. The distinction from high availability is subtle but real — an HA system might have a brief blip while it detects a failure and redirects traffic, whereas a truly fault-tolerant system rides through the failure without any visible impact at all.

In practice, most real-world systems aim for high availability rather than full fault tolerance, since true fault tolerance (zero interruption, ever) is expensive and hard to achieve. "Highly available" is the term we'll hear far more often when people describe well-architected AWS systems.

## How AWS Enables This

AWS's physical infrastructure is built specifically to make high availability achievable:

- **Multiple Availability Zones (AZs)** within a region let us run redundant copies of our application in physically separate locations that are still close enough for fast networking between them. If one AZ has a problem — a power failure, a networking issue — the others keep running.
- **Load balancers** distribute traffic across healthy resources and automatically stop sending traffic to ones that fail health checks.
- **Auto Scaling groups** can detect an unhealthy instance and replace it automatically, without a person having to intervene.
- **Managed services** (like RDS's Multi-AZ deployments) build redundancy in as a configuration option rather than something we have to engineer ourselves.

## Why It Matters

Downtime has a real cost — lost revenue, lost trust, and for some systems, real-world consequences (think healthcare or financial systems). High availability is one of the clearest reasons organizations move to the cloud: building genuinely redundant infrastructure on-prem means owning multiple data centers, which is far out of reach for most organizations. AWS makes that same redundancy available as a design choice within a single account, which is exactly what the next lesson's discussion of Regions and Availability Zones is built around.
