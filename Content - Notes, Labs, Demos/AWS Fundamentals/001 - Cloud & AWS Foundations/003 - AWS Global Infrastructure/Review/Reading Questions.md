# Reading Questions — AWS Global Infrastructure

Use these questions while you read the notes. Each one points at something worth understanding well enough to talk about on the job.

## What a Region Is, How to Choose One

1. Why are AWS regions fully independent of each other by default — what would break, or what would be lost, if they weren't?
2. Regions exist partly because of "physics and law." What does each of those two forces actually explain?
3. What four factors typically drive the choice of which region to run a workload in?
4. Why does the reading describe regional isolation as "a feature, not a limitation," rather than just an inconvenience?

## Region-Scoped vs Global Services

5. What does it mean for an AWS service to be "region-scoped," and what's the most common mistake this causes for people new to AWS?
6. Why is IAM a global service rather than a region-scoped one? What about IAM's nature makes that make sense?
7. In what sense is S3 a "middle ground" between region-scoped and global? What part of it is global and what part isn't?
8. Why does understanding region-scoped vs. global services matter for disaster recovery planning?

## What an AZ Is, Relationship to Regions

9. What makes an Availability Zone "independent," and why does that independence matter?
10. How is an AZ different from a region — how would you explain the relationship between the two?
11. Why do AZs exist as a middle layer between "a single data center" and "a whole separate region"? What problem would spreading across regions instead create?

## Designing for AZ Failure

12. What's the core design principle for surviving an AZ failure, and what happens if it's ignored?
13. How does a Multi-AZ design for compute (EC2 + load balancer) actually survive an AZ going down? Walk through what happens.
14. Why does data (like a database) need a fundamentally different Multi-AZ strategy than stateless compute does?
15. Why doesn't S3 require you to do anything extra to get multi-AZ durability?
16. If Multi-AZ design is clearly better for availability, why isn't it the default for every workload? What are the two honest trade-offs?
