# On-Demand, Reserved & Spot

The three EC2 purchasing models are usually presented as a discount table. They are better understood as a decision about **what we are willing to commit to** — because each discount is paid for with a different kind of flexibility.

---

## What Each One Trades

**On-demand.** Pay per second, start and stop freely, no commitment. Full price. What we are buying is the option to change our minds at any moment.

**Reserved Instances and Savings Plans.** Commit to a level of usage for one or three years in exchange for 30–70% off. What we give up is the freedom to reduce spending during that term — the commitment is to spend, whether or not the capacity is used.

**Spot.** Bid on unused capacity for 70–90% off, and accept that AWS can reclaim the instance with **two minutes' notice**. What we give up is control over when the workload stops.

---

## Reserved Instances vs Savings Plans

Savings Plans are the more flexible successor and are usually the better choice.

**Reserved Instances** commit to a specific instance type in a specific region. Changing instance families means the reservation no longer applies. Convertible RIs allow exchanges with a smaller discount.

**Savings Plans** commit to a dollar amount per hour rather than to specific instances:

- **Compute Savings Plans** apply across instance families, regions, and even to Fargate and Lambda. Maximum flexibility, slightly smaller discount.
- **EC2 Instance Savings Plans** commit to a family in a region, allowing size changes within it. Larger discount, less flexibility.

For most organizations, a Compute Savings Plan covering baseline usage is the right instrument — it survives architecture changes that would strand a Reserved Instance.

### Sizing the commitment

The mistake is committing to peak usage. A commitment is paid whether or not it is used, so unused commitment is pure waste.

The safe approach is to commit to the **steady-state floor** — the level below which usage never drops — and leave everything above it on demand. Cost Explorer's recommendations are based on historical usage and are a reasonable starting point, adjusted downward if the architecture is likely to change.

---

## Spot

Spot instances run on spare capacity, reclaimed when AWS needs it back. A **two-minute interruption notice** is delivered through instance metadata and as an EventBridge event.

Suitable workloads:

- Batch processing and data pipelines
- CI/CD build agents
- Rendering and simulation
- Stateless web servers, mixed with on-demand for a baseline
- Anything checkpointable or restartable

Unsuitable workloads:

- Databases and anything holding state locally
- Long jobs that cannot checkpoint
- Anything where an interruption is user-visible

Practical guidance:

**Diversify across instance types and AZs.** Spot capacity is per type per AZ, so a fleet accepting several types is interrupted far less often than one requiring a single type.

**Handle the interruption notice.** Two minutes is enough to drain connections, checkpoint work, and deregister from a load balancer — but only if something is listening for the notice.

**Use mixed-instances Auto Scaling groups.** A group can hold a defined on-demand baseline plus spot for everything above it, giving most of the discount with a guaranteed floor.

---

## Comparing

| | On-demand | Savings Plan | Spot |
|---|---|---|---|
| Discount | — | 30–70% | 70–90% |
| Commitment | None | 1 or 3 years | None |
| Interruption | No | No | Two minutes' notice |
| Suits | Variable, unpredictable load | Steady baseline | Fault-tolerant, restartable work |

These combine rather than compete. A typical production account runs a Savings Plan covering baseline, spot for batch and build capacity, and on-demand for the variable remainder.

---

## Other Levers

**Graviton instances** (ARM-based, `m7g`, `c7g`) offer better price-performance than equivalent x86 types. The cost is verifying that the workload's dependencies build for ARM — for most interpreted and JVM applications that is straightforward, and the saving requires no commitment.

**Right-sizing** usually beats purchasing decisions. A 60%-discounted instance twice the size it needs to be is more expensive than a correctly sized on-demand one. Right-size first, then commit to the resulting baseline.

---

## Key Takeaways

- The three models trade discount against flexibility: on-demand keeps every option, commitment plans give up the ability to stop spending, and spot gives up control of when the workload stops.
- Savings Plans are more flexible than Reserved Instances; Compute Savings Plans span families, regions, Fargate, and Lambda.
- Commit to the steady-state floor rather than peak, since unused commitment is wasted.
- Spot instances are reclaimed with two minutes' notice and suit batch, CI, and stateless restartable work.
- Diversify spot across instance types and AZs, and handle the interruption notice.
- Mixed-instances Auto Scaling groups combine an on-demand baseline with spot capacity above it.
- Right-size before committing, and consider Graviton for a discount that requires no commitment.
