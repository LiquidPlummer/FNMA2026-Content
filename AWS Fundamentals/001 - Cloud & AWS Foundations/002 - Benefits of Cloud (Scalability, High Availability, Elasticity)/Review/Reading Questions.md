# Reading Questions — Benefits of Cloud (Scalability, High Availability, Elasticity)

Use these questions while you read the notes. Each one points at something worth understanding well enough to talk about on the job.

## Scalability (Vertical vs Horizontal)

1. What's the core difference between vertical and horizontal scaling, and what does each one cost you in return for the capacity it adds?
2. Why does horizontal scaling typically require an application to be designed differently (e.g., avoiding in-memory session state), while vertical scaling usually doesn't?
3. Give an AWS-specific example of vertical scaling and one of horizontal scaling. What's actually different about what you're doing in each case?
4. Why is DynamoDB built to scale horizontally from the ground up, while a traditional relational database leans more on vertical scaling?

## High Availability & Fault Tolerance

5. What's the difference between high availability and fault tolerance — and why do most real-world AWS systems aim for the former rather than the latter?
6. How does redundancy actually deliver high availability? What has to be true about the redundant copies for this to work?
7. Name at least two specific AWS mechanisms that help make a system highly available, and briefly explain how each one contributes.
8. Why is high availability described as one of the clearest reasons organizations move to the cloud?

## Cost Efficiency (CapEx vs OpEx)

9. What's the fundamental difference between CapEx and OpEx spending, and which one does on-prem infrastructure typically fall under?
10. What real downsides does CapEx spending create, especially when a forecast turns out to be wrong?
11. Name at least two ways the CapEx-to-OpEx shift changes the risk profile of a new project.
12. The notes call the cloud's cost model "a trade-off, not a free win." Under what conditions might owned, on-prem hardware actually cost less over the long run than the cloud?
