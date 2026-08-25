# Module Review — Cloud & AWS Foundations

A curated set of the ~25 most important questions from across this module's full question bank — the concepts worth walking away able to explain on the job, pulled together regardless of which lesson they came from. Cost management is intentionally light here; the mechanics of Free Tier limits, budgets, and Cost Explorer matter less day-to-day than the architectural fundamentals below.

## Cloud Fundamentals

1. Why is elasticity described as what makes pay-as-you-go pricing "actually valuable"? What would pay-as-you-go look like without it?
2. How do on-demand, pay-as-you-go, and elasticity reinforce each other, rather than being three independent features?

## Scalability & Availability

3. What's the core difference between vertical and horizontal scaling, and what does each one cost you in return for the capacity it adds?
4. Why does horizontal scaling typically require an application to be designed differently (e.g., avoiding in-memory session state), while vertical scaling usually doesn't?
5. Give an AWS-specific example of vertical scaling and one of horizontal scaling. What's actually different about what you're doing in each case?
6. What's the difference between high availability and fault tolerance — and why do most real-world AWS systems aim for the former rather than the latter?
7. How does redundancy actually deliver high availability? What has to be true about the redundant copies for this to work?

## Global Infrastructure: Regions & Availability Zones

8. Why are AWS regions fully independent of each other by default — what would break, or what would be lost, if they weren't?
9. What four factors typically drive the choice of which region to run a workload in?
10. What does it mean for an AWS service to be "region-scoped," and what's the most common mistake this causes for people new to AWS?
11. What makes an Availability Zone "independent," and why does that independence matter?
12. How is an AZ different from a region — how would you explain the relationship between the two?
13. What's the core design principle for surviving an AZ failure, and what happens if it's ignored?
14. How does a Multi-AZ design for compute (EC2 + load balancer) actually survive an AZ going down? Walk through what happens.
15. If Multi-AZ design is clearly better for availability, why isn't it the default for every workload? What are the two honest trade-offs?

## Service Models & Shared Responsibility

16. What's the core difference between IaaS, PaaS, and SaaS in terms of what you're handed versus what you have to build yourself?
17. What do you give up, and what do you gain, moving from IaaS to PaaS? Use Lambda as your example.
18. What's the shorthand distinction between "security of the cloud" and "security in the cloud," and which side is AWS responsible for?
19. In the S3 bucket example, why is a misconfigured public bucket the customer's fault and not AWS's, even though it's "AWS's product"?
20. Why does the customer's share of responsibility shrink as you move from IaaS toward SaaS? What's the underlying logic?
21. Compare EC2 and RDS: what does AWS take over with RDS that it doesn't with EC2, and what does the customer still own in both cases?
22. What's still the customer's job even in a fully-managed SaaS product, if AWS is managing the entire stack?

## Cost Awareness

23. Why does the fact that "the Free Tier doesn't cap what we can spend" matter? What's the danger it's warning about?

## Using the Console

24. In what sense are "folders" in the S3 console not real folders? What's actually going on under the hood?
25. Why is the permissions tab singled out as "the single most important tab to understand early" for an S3 bucket?
