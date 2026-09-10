# Where the Pitch Holds and Where It Doesn't

Each claim in the pitch is either structurally true, or true only under conditions that someone has to create and pay for. Sorting them into those two piles is the most useful thing we can do with them.

---

## What Is Structurally True

**Capacity really is elastic.** An Auto Scaling group can go from two instances to twenty in the time it takes them to boot, and back down again an hour later. A DynamoDB table in on-demand mode absorbs a traffic spike with no configuration change at all. Nothing about owning hardware approximates this, because owned capacity is fixed at purchase time.

**There really is no hardware to buy.** The procurement cycle, the rack space, the spare-parts inventory, the hardware refresh — those disappear. A team can start work on the day it decides to, rather than eleven weeks later.

These two are worth internalizing, because a great deal of good architecture on AWS comes from taking them seriously: building systems that expand and contract, and treating individual servers as replaceable rather than precious.

---

## What Is Conditional

### High availability is architected and paid for

AWS does not hand it out. A single EC2 instance in a single Availability Zone has roughly the availability of a single server, and when the underlying host fails, the instance goes with it.

Surviving the loss of an Availability Zone requires deliberately running redundant capacity in more than one, putting a load balancer in front of it, keeping state somewhere that outlives any individual instance, and paying for all of it. The building blocks are available; the outcome is not automatic.

### Pay-as-you-go can cost more than owning hardware

Per-hour pricing is excellent for variable load and poor for steady load. A workload that runs at a constant size, twenty-four hours a day, for three years, is close to the worst case for on-demand pricing — it pays a premium for elasticity it never uses.

AWS's own answer to this is commitment-based pricing (Reserved Instances and Savings Plans), which trades that flexibility back for a lower rate. The point is that "pay only for what you use" describes a pricing model, not a guarantee of a lower bill.

### Managed services remove operational work, not responsibility

RDS handles patching and backups. It does not stop anyone from putting the database in a public subnet with a weak password. This distinction matters enough that the next lesson is built around it.

---

## The Practical Consequence

| Claim | Status | What it actually depends on |
|---|---|---|
| Elastic capacity | Structurally true | Designing the workload to scale horizontally |
| No hardware purchase | Structurally true | Nothing — this one is unconditional |
| High availability | Conditional | Multi-AZ design, redundant capacity, and the cost of both |
| Pay-as-you-go savings | Conditional | Load being variable rather than steady |
| Managed services | Conditional | Understanding what remains ours |

Every conditional item on that list resolves into a decision someone makes and a line on a bill. That is the level this material works at from here on.

---

## Key Takeaways

- Elastic capacity and the absence of hardware procurement are real and unqualified.
- High availability is an architecture we build and fund, not a property we receive.
- Pay-as-you-go is a pricing model; at steady load it can cost more than ownership, which is why commitment pricing exists.
- Managed services remove operational work while leaving configuration and access control with us.
- Each conditional claim becomes a design decision and a cost, and that is how the rest of this material treats them.
