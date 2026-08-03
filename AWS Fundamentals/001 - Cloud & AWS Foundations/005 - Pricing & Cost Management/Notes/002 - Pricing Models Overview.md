# Pricing Models Overview

AWS doesn't charge a single flat rate for using its services — different pricing models exist to fit different usage patterns, and picking the right one for a given workload can make a meaningful difference in cost. We won't quote specific rates here (they vary by service, region, and change over time — always check the current pricing page for the service in question), but understanding the *shapes* these models take is a durable skill that doesn't go stale.

## Pay-As-You-Go (On-Demand)

The default model: pay for resources for exactly as long as we use them, with no upfront commitment. This is the most flexible option — nothing to sign up for, nothing to plan around — and it's the natural starting point for any new or unpredictable workload. The trade-off is that on-demand pricing is typically the most expensive rate *per unit of usage*, since we're paying for the flexibility of zero commitment.

## Commitment-Based Discounts (Reserved / Savings Plans)

For workloads with steady, predictable usage — a server that needs to run continuously for the foreseeable future, for example — AWS offers discounted rates in exchange for committing to a certain amount of usage over a set term. The trade-off is the mirror image of on-demand: a lower rate, in exchange for giving up some flexibility. This model only makes sense once we're confident a workload's baseline usage is predictable — committing to capacity we're not sure we'll need can end up costing more than just paying on-demand rates would have.

## Spot Pricing

Some services let us bid on unused AWS capacity at a steep discount, with the catch that AWS can reclaim that capacity (interrupting our workload) on short notice if it's needed elsewhere at full price. This model fits workloads that are flexible about *when* they run and resilient to interruption — batch processing jobs, non-urgent data analysis, or fault-tolerant distributed workloads that can pick up where they left off. It's a poor fit for anything that needs to run continuously and can't tolerate being stopped unexpectedly.

## Usage-Based Pricing (Per-Request or Per-Resource)

Some services don't bill for time at all — they bill per unit of actual usage: per API request, per gigabyte stored, per gigabyte transferred. Lambda is a good example: instead of paying for a server to sit running, we pay only for the compute time actually consumed while a function executes. This model can be extremely cost-efficient for workloads with unpredictable or bursty traffic, since there's no cost at all during idle periods — but it can also become more expensive than a flat-rate server at a high enough, sustained volume.

## Choosing Between Them

| Model | Best fit | Trade-off |
|---|---|---|
| On-Demand | New or unpredictable workloads | Highest per-unit cost, maximum flexibility |
| Reserved / Savings Plans | Steady, predictable, long-running workloads | Discounted rate, less flexibility |
| Spot | Flexible, interruption-tolerant workloads | Deep discount, no availability guarantee |
| Usage-Based | Bursty or idle-heavy workloads | Scales naturally with actual use |

There's no universally "best" model — the right choice depends entirely on the shape of the workload's usage over time. Many real architectures end up blending several of these for different parts of the same system. Turning this awareness into actual cost control day-to-day is what billing, budgets, and Cost Explorer (the next two topics) are built for.
