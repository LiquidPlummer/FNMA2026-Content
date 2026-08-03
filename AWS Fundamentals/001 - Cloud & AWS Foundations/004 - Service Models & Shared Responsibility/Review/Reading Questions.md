# Reading Questions — Service Models & Shared Responsibility

Use these questions while you read the notes. Each one points at something worth understanding well enough to talk about on the job.

## Defining IaaS, PaaS, and SaaS

1. What's the core difference between IaaS, PaaS, and SaaS in terms of what you're handed versus what you have to build yourself?
2. Why is EC2 the "textbook example" of IaaS specifically?
3. What do you give up, and what do you gain, moving from IaaS to PaaS? Use Lambda as your example.
4. Where does Amazon Chime fall on this spectrum, and why?
5. Why is asking "where does this fall on the IaaS-to-SaaS spectrum?" described as a useful lens for evaluating any new AWS service you encounter?

## What AWS Manages vs What the Customer Manages

6. What's the shorthand distinction between "security of the cloud" and "security in the cloud," and which side is AWS responsible for?
7. List at least three things that fall squarely on AWS's side of the shared responsibility line.
8. List at least three things that fall squarely on the customer's side.
9. In the S3 bucket example, why is a misconfigured public bucket the customer's fault and not AWS's, even though it's "AWS's product"?
10. Why does AWS draw the responsibility line where it does, rather than somewhere else?

## How Responsibility Shifts Across Service Types

11. Why does the customer's share of responsibility shrink as you move from IaaS toward SaaS? What's the underlying logic?
12. Compare EC2 and RDS: what does AWS take over with RDS that it doesn't with EC2, and what does the customer still own in both cases?
13. What's still the customer's job even in a fully-managed SaaS product, if AWS is managing the entire stack?
14. Why is understanding this shifting boundary described as a major factor in why teams choose managed services over self-managed ones — beyond just raw performance?
