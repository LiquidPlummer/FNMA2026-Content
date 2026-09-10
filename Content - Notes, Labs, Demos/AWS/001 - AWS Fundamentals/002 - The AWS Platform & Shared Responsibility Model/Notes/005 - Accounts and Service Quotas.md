# Accounts and Service Quotas

Two structural facts shape day-to-day work on AWS more than their prominence suggests: the account is the real isolation boundary, and every service has limits that will stop us earlier than expected.

---

## The Account as the Unit of Isolation

An **AWS account** is a container for resources, identities, and a bill. It is the strongest boundary AWS offers, and it is stronger than most people assume:

- Resources in one account are invisible to another by default. An EC2 instance in account A cannot resolve or reach an RDS instance in account B without deliberate configuration.
- IAM identities are per-account. A user in account A does not exist in account B.
- The bill is per-account, which makes the account the cleanest way to attribute cost.

Because the boundary is genuinely strong, **multiple accounts are the normal way to separate environments.** Production in its own account, staging in another, and development in a third is a common arrangement. Doing the same separation with tags or naming conventions inside one account is far weaker — a mistake in one IAM policy can cross it, while crossing an account boundary requires explicit cross-account trust.

**AWS Organizations** manages a collection of accounts: consolidated billing, centrally created accounts, and **Service Control Policies (SCPs)** that set a ceiling on what any identity in a member account may do — including the account's own root user. An SCP does not grant permissions; it caps them.

```
Organization
├── Management account (billing, SCPs)
├── Production account
├── Staging account
└── Development account
```

*A common layout: one account per environment, governed centrally, so a mistake in development cannot touch production.*

---

## Service Quotas

Every AWS service enforces **quotas** (still widely called *limits*) on how much of it a single account can use in a single region. They exist to contain runaway spend and to let AWS plan capacity, and they apply from the first day an account exists.

Two kinds are worth distinguishing:

- **Adjustable quotas** can be raised by request — number of EC2 vCPUs, number of VPCs per region, Lambda concurrent executions. Raising them is a support ticket, and approval can take anywhere from minutes to days.
- **Hard limits** cannot be raised at all — 5 Elastic IPs per region is adjustable, but Lambda's 15-minute timeout and DynamoDB's 400 KB item size are not. These are design constraints, not paperwork.

### Where they surprise people

Quotas rarely bite during early experimentation. They bite at the worst moments:

- A new account starts with a **low** vCPU quota. Launching a production-sized fleet on day one fails.
- Auto Scaling silently stops adding instances at the vCPU quota, exactly during the traffic spike that triggered the scaling.
- A CloudFormation stack fails partway through and rolls back because it tried to create the sixth VPC in a region that allows five.
- Default quotas are **per region**, so capacity headroom in one region says nothing about another.

```bash
# List the quotas for a service and read the current value for one of them
aws service-quotas list-service-quotas --service-code ec2
aws service-quotas get-service-quota --service-code ec2 --quota-code L-1216C47A
```

*`list-service-quotas` shows every quota for a service; `get-service-quota` reads one, and the quota code identifies which limit — here, on-demand standard instance vCPUs.*

The practical habit is to check quotas **before** a launch that needs headroom, not after a deployment fails, because raising one is not instantaneous. Quotas are also visible in the Service Quotas console, where increases can be requested directly.

---

## Key Takeaways

- The account is AWS's strongest isolation boundary and the natural unit of billing; separate environments belong in separate accounts.
- AWS Organizations groups accounts for consolidated billing and applies Service Control Policies as a ceiling on permissions, including for the root user.
- Every service enforces per-account, per-region quotas; new accounts start with low defaults.
- Adjustable quotas require a support request that takes time; hard limits, like Lambda's 15-minute timeout, cannot be raised at all.
- Quotas commonly surface during scaling events and stack deployments, so verify headroom before it is needed.
