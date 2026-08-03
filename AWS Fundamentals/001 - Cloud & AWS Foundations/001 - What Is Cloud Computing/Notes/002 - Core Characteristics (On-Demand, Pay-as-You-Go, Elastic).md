# Core Characteristics (On-Demand, Pay-as-You-Go, Elastic)

Cloud computing isn't just "someone else's servers" — it's a specific way of consuming computing resources that's defined by a handful of characteristics working together. These three are the ones that show up in almost every conversation about why the cloud is different from traditional infrastructure.

## On-Demand (Self-Service)

**On-demand** means we can provision resources ourselves, whenever we need them, without waiting on a person. Need a new server? Launch it from the console or an API call, and it's ready in minutes — no ticket to IT, no approval chain, no waiting for hardware to ship.

This is a bigger deal than it sounds. In an on-prem world, provisioning a new server might involve a purchase order, a vendor lead time, and a rack-and-stack by a data center technician. On-demand collapses that entire process into a self-service action any authorized engineer can take directly.

## Pay-as-You-Go

**Pay-as-you-go** means we're billed for what we actually use, rather than paying a fixed amount upfront regardless of usage. Run a server for three hours and shut it down — we pay for three hours. Store a file for a week and delete it — we pay for a week of storage.

This changes the financial shape of a project. Instead of a large upfront purchase we have to justify before knowing whether a workload will succeed, we start small, pay only for what we consume, and let cost grow (or shrink) alongside actual usage. It also means idle resources are a choice, not a sunk cost — leaving a server running when nobody needs it is money we're choosing to spend, not equipment gathering dust that we already paid for.

## Elastic (Elasticity)

**Elasticity** is the ability to scale resources up or down automatically as demand changes, and to release resources we no longer need. A retail site might run a handful of servers on a normal Tuesday and automatically add dozens more during a big sale, then scale back down afterward.

Elasticity is what makes pay-as-you-go pricing actually valuable. Being billed only for what we use doesn't help much if we still have to guess our capacity needs months in advance and provision for the worst case. Elasticity means we don't have to guess — capacity follows demand in near real time.

## How They Fit Together

These three characteristics reinforce each other:

- **On-demand** gets rid of the provisioning delay.
- **Pay-as-you-go** gets rid of the upfront cost.
- **Elasticity** gets rid of the need to guess capacity in advance.

Together, they mean we can start small, experiment cheaply, and let our infrastructure grow or shrink with actual demand instead of a forecast. That combination is the foundation for the benefits we'll explore next — scalability, high availability, and cost efficiency all build directly on these three characteristics.
