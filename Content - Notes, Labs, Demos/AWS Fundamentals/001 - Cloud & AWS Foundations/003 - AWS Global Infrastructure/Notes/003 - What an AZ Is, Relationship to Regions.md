# What an AZ Is, Relationship to Regions

Inside every region, AWS infrastructure is further divided into **Availability Zones**, and this is the layer that makes high availability practically achievable within a single region.

## What an Availability Zone Is

An **Availability Zone (AZ)** is one or more physically separate data centers within a region, each with its own independent power, cooling, and networking. A region typically contains multiple AZs — often three or more — connected to each other by high-speed, low-latency links.

The key word is *independent*. An AZ is built so that a failure in one — a power outage, a cooling failure, a fire — doesn't take down the others. They're far enough apart geographically to avoid sharing a single point of failure, but close enough together that network traffic between them is fast, so an application spanning multiple AZs doesn't pay a significant latency penalty for doing so.

## The Relationship to Regions

Think of it as a hierarchy:

```
Region (e.g., a US region)
  ├── Availability Zone A
  ├── Availability Zone B
  └── Availability Zone C
```

A region is the broad geographic area; the AZs are the individual, physically isolated pockets of infrastructure within it. When we launch a resource like an EC2 instance, we choose a region, and then (often automatically, or explicitly if we want control) a specific AZ within that region.

## Why AZs Exist

AZs exist to give us redundancy *without* the latency cost of spreading across regions. If we deployed a highly available application by running copies of it in two different regions on opposite sides of a continent, the network distance between those copies could introduce noticeable delay for things like data replication. AZs solve this: they're isolated enough to protect against real failures, but close enough that treating them as a single logical unit — for example, replicating a database synchronously across two AZs — is practical.

This is why the standard AWS guidance for building a resilient application is: **spread resources across multiple AZs within a region**, rather than relying on a single AZ. A single AZ is still a single point of failure, just a smaller one than a single server.

## What's Next

Understanding that AZs exist is only half the picture — the more important skill is *designing* for the fact that any one AZ can fail at any time. That's the focus of the next topic.
