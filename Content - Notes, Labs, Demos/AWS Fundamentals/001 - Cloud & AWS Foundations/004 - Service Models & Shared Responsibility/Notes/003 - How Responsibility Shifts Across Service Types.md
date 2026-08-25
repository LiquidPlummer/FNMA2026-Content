# How Responsibility Shifts Across Service Types

The Shared Responsibility Model isn't a fixed 50/50 split — where the line falls depends on which *type* of service we're using. This is where the IaaS/PaaS/SaaS spectrum from earlier directly connects to shared responsibility: the more AWS manages about *how* a service runs, the more of the security burden shifts onto AWS as well.

## The General Rule

As we move from IaaS toward SaaS, AWS takes on progressively more responsibility, and the customer's slice shrinks. This makes intuitive sense — AWS can only be responsible for layers it actually controls, so as AWS takes over more layers (the OS, the runtime, the application platform), those layers move out of customer responsibility and into AWS's.

## IaaS Example: EC2

With EC2, AWS is responsible for the physical hardware, the virtualization layer, and the global network. We're responsible for nearly everything above that: choosing and patching the guest operating system, configuring security groups (firewall rules), managing any software we install, and securing the application and its data. This is the largest customer responsibility slice of the three models, because AWS's job stops at "here is a working virtual machine."

## PaaS Example: RDS (Managed Database)

With a managed service like RDS, AWS takes over more: it manages the underlying operating system, installs and patches the database engine, and handles automated backups and failover if we choose Multi-AZ. Our responsibility narrows to things like: what data we put in the database, how we configure access to it (database users, network access via security groups), and whether we enable encryption options AWS provides. AWS still isn't responsible for a poorly written query or an overly permissive database user we created — those remain ours.

## SaaS Example: A Fully Managed Application

With a SaaS product, AWS (or a third-party vendor building on AWS) manages essentially the entire stack, including the application itself. Our responsibility shrinks to things like: managing which of our users have accounts, what data we choose to put into the system, and configuring any access controls the application exposes to us. We're not securing infrastructure at all anymore — just our own usage of the finished product.

## Visualizing the Shift

```
                IaaS (EC2)         PaaS (RDS)          SaaS
Customer:   ████████████████   ████████░░░░░░░░   ██░░░░░░░░░░░░░░
AWS:        ░░░░░░░░░░░░░░░░   ░░░░░░░░████████   ░░██████████████
            (app, OS, data,    (data, access,      (data, user
             network config)    some config)        access only)
```
*A rough sketch of how the customer's share of responsibility (█) shrinks as AWS manages more of the stack, moving from IaaS to SaaS.*

## The Practical Takeaway

When evaluating any AWS service, it's worth explicitly asking: "which layers does AWS manage here, and which are still mine?" That question has a different answer for every service, and getting it wrong in either direction is a problem — assuming AWS covers something it doesn't leaves a gap (like an unpatched database left exposed), while duplicating effort AWS already handles just wastes time. This shifting boundary is also a major factor in *why* teams choose managed services over self-managed ones — it's often less about raw performance and more about how much operational and security burden gets handed to AWS instead of staying in-house.
