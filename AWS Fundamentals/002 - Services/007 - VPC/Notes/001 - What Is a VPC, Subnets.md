# What Is a VPC, Subnets

Every AWS resource that talks over a network — an EC2 instance, an RDS database — needs to live *somewhere* on a network. **VPC** (Virtual Private Cloud) is the service that defines that network: our own private, isolated slice of AWS's infrastructure, within which we control addressing, segmentation, and traffic flow.

## What a VPC Is

A **VPC** is an isolated virtual network within a region, logically separated from every other customer's networks (and from our own other VPCs, if we have more than one). When we launch resources like EC2 instances or RDS databases, they attach to a VPC, and it's the VPC's configuration that determines how those resources can communicate — with each other, with the broader internet, and with anything else.

A VPC is defined by a range of IP addresses (a **CIDR block**) that it owns — for example, a range like `10.0.0.0/16`, which provides a large block of private IP addresses for resources inside the VPC to use. The exact numbers aren't the important part here; what matters is the concept: a VPC starts life as one big address space that we then divide up.

## Subnets

We rarely put every resource directly into the VPC's address space undivided — instead, we carve the VPC's IP range into smaller pieces called **subnets**. A subnet is a segment of the VPC's address range, tied to a single Availability Zone. This connects directly back to the AZ concepts from earlier in the course: spreading subnets (and the resources in them) across multiple AZs is exactly how we apply the "don't depend on one AZ" principle at the network level.

## Public vs Private Subnets

The most important distinction to understand about subnets is the difference between **public** and **private**:

- A **public subnet** has a route to an **internet gateway** — a VPC component that allows traffic to flow to and from the public internet. Resources in a public subnet *can* be reachable from the internet (assuming their security group also allows it, per the EC2 lesson).
- A **private subnet** has no such route. Resources here cannot be reached directly from the internet, and typically can't initiate outbound connections to it either, without additional components designed to allow that safely.

This split maps naturally onto a common architecture pattern: put internet-facing resources (like a load balancer, or a web server that needs to be publicly reachable) in a public subnet, and put resources that should never be directly exposed — like a database — in a private subnet. The database can still be reached *by* the web server (since they're in the same VPC and can communicate internally), but it's simply unreachable from the outside world, no matter how a security group is configured, because there's no network path there in the first place.

## A Simple Mental Model

```
VPC (10.0.0.0/16)
  ├── Public Subnet  (AZ-A)  →  Internet Gateway  →  Internet
  │     └── Load Balancer, web servers
  └── Private Subnet (AZ-A)  →  no direct internet route
        └── Database
```
*Public subnets have a path to the internet; private subnets don't — regardless of what any individual resource's security group allows.*

## Why This Layered Isolation Matters

Subnetting isn't just organizational tidiness — it's a genuine security boundary that exists *before* any security group is even considered. A database sitting in a private subnet is protected from direct internet exposure structurally, at the network layer, rather than relying entirely on a correctly configured firewall rule to keep it safe. That's a meaningfully stronger guarantee: even a misconfigured security group can't undo the fact that there's no route to the internet at all. With the VPC and subnet structure established, the next topic looks at how traffic is actually filtered once it's flowing through this structure.
