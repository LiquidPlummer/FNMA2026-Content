# What a Region Is, How to Choose One

AWS doesn't run one giant global data center — it runs infrastructure spread across the world, organized into a hierarchy. The largest unit in that hierarchy is the **Region**.

## What a Region Is

A **Region** is a geographic area containing a cluster of AWS data centers — for example, a region covering the US East coast, or one covering Western Europe. Each region is fully independent: it has its own separate set of resources, and by default, nothing we create in one region is visible or accessible from another. If we launch a server in one region, it doesn't exist as far as a different region is concerned unless we explicitly copy or replicate it there.

Regions exist because of physics and law as much as engineering. Physics, because network distance creates latency — a user in Tokyo talking to a server in Virginia will always experience more delay than one talking to a server in Tokyo. Law, because many countries and industries have **data residency** requirements dictating that certain data must stay within a specific country's borders.

## How to Choose a Region

When deciding where to run a workload, a few factors typically drive the decision:

- **Latency** — pick a region close to the majority of your users, since physical distance directly affects response time.
- **Compliance and data residency** — some data, by law or contract, must be stored within a specific country or geographic area.
- **Service availability** — not every AWS service launches in every region simultaneously; newer services often reach a handful of regions before rolling out everywhere.
- **Cost** — pricing for the same service can vary slightly between regions, since costs like electricity and real estate differ by location.

Many organizations end up using multiple regions at once — one as the primary location close to most users, and others for disaster recovery, compliance needs, or serving users in a different part of the world.

## Why Regional Isolation Is a Feature, Not a Limitation

It might seem inconvenient that resources don't automatically span regions, but that isolation is deliberate. It contains the blast radius of a problem — an outage affecting one region doesn't cascade into every other region — and it gives organizations precise control over exactly where their data physically lives, which is often a hard legal requirement rather than a nice-to-have.

Within a region, AWS provides further structure for redundancy and isolation — that's the role of the **Availability Zone**, which we'll cover shortly. First, though, it's worth understanding which AWS services live at the region level at all, versus which ones sit above regions entirely — that's next.
