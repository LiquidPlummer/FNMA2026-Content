# Designing for AZ Failure

Knowing that Availability Zones exist doesn't automatically make an application resilient — that requires actively designing around the assumption that any single AZ can fail at any moment. This topic is about what that design looks like in practice.

## The Core Principle: Don't Depend on One AZ

The single most important rule is straightforward: **anything that must stay available shouldn't live in only one AZ.** If our entire application — servers, database, everything — runs in a single AZ, then that AZ failing takes the whole application down with it, no matter how well-architected the application is otherwise.

## What Multi-AZ Design Looks Like

**Compute:** Instead of running one EC2 instance, run multiple instances spread across two or more AZs, with a **load balancer** in front distributing traffic across all of them. If one AZ goes down, the load balancer stops sending traffic to the instances in that AZ and continues serving requests from the surviving ones. Pairing this with an **Auto Scaling group** means AWS can even launch replacement instances in the healthy AZs automatically.

**Data:** Databases need a different strategy, since data has to actually be consistent across locations, not just duplicated. Services like RDS offer a **Multi-AZ** deployment option, where a standby copy of the database is kept synchronized in a second AZ. If the primary AZ fails, RDS automatically fails over to the standby with minimal disruption.

**Storage:** S3 already replicates object data across multiple AZs within a region automatically, as part of how the service is built — this is one of the reasons S3 is described as having very high durability, without us having to do anything extra to achieve it.

## A Simple Mental Model

```
Single-AZ design:                Multi-AZ design:

  [ AZ-A ]                        [ AZ-A ]      [ AZ-B ]
  ┌─────────┐                     ┌────────┐    ┌────────┐
  │ Server  │                     │ Server │    │ Server │
  │ Database│    ← one failure    │        │    │        │
  └─────────┘      = total        └───┬────┘    └───┬────┘
                    outage             └──── LB ─────┘
                                    ← one AZ failing
                                      still leaves the
                                      other one serving
```
*A rough sketch contrasting a single point of failure with a load-balanced, multi-AZ layout.*

## Why This Isn't the Default

If Multi-AZ design is clearly better for availability, why doesn't everything just default to it? Two honest reasons: **cost** (running redundant resources across multiple AZs costs more than running one copy) and **complexity** (keeping data consistent across locations is genuinely harder than keeping it in one place). For a low-stakes internal tool, a single AZ might be a perfectly reasonable trade-off. For anything customer-facing or business-critical, the cost of downtime usually outweighs the cost of redundancy — which is exactly the kind of trade-off judgment that comes up constantly when designing real AWS architectures.
