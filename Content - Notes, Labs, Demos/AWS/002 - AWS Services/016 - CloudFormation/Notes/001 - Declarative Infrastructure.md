# Declarative Infrastructure

**CloudFormation** takes a description of the desired end state and makes reality match it. We do not write the steps; we write the outcome.

---

## Declarative vs Imperative

**Imperative** describes steps:

```bash
aws ec2 create-vpc --cidr-block 10.0.0.0/16
aws ec2 create-subnet --vpc-id vpc-... --cidr-block 10.0.1.0/24
aws ec2 create-internet-gateway
# ... and so on
```

*Every step must be written, in order, with error handling and the IDs from earlier steps threaded through. Running it twice creates a second VPC.*

**Declarative** describes the result:

```yaml
Resources:
  VPC:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: 10.0.0.0/16
  PublicSubnet:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !Ref VPC
      CidrBlock: 10.0.1.0/24
```

*States what should exist. CloudFormation determines the order from the dependencies, creates what is missing, and does nothing when everything already matches.*

---

## What Reconciliation Provides

**Idempotence.** Deploying the same template repeatedly converges on the same state. There is no "already exists" error to handle.

**Dependency ordering.** `!Ref VPC` tells CloudFormation the subnet needs the VPC, so it creates the VPC first. Ordering is derived from references rather than written by hand.

**Change detection.** On an update, CloudFormation compares desired against actual and changes only the difference.

**Deletion.** Removing a resource from the template deletes it. Imperative scripts almost never handle deletion, which is why manually built environments accumulate resources nobody removes.

**Rollback.** A failed update reverts to the previous state automatically.

---

## What It Costs

Being honest about the trade-offs:

**It is slower.** A stack creating a VPC, subnets, an ALB, and an RDS instance takes many minutes. Clicking is faster for one resource.

**The failure modes are its own.** A stack stuck in `UPDATE_ROLLBACK_FAILED` is a specific kind of problem requiring specific knowledge, and it is more confusing than a failed CLI command.

**Templates are verbose.** A production VPC template runs to hundreds of lines of YAML for something conceptually simple.

**Not everything is supported.** New services and new features sometimes lack CloudFormation support for a period after launch, and some resource properties are never exposed.

**There is a learning cost**, in the template language and in how CloudFormation behaves when things go wrong.

For a single experimental resource, the console is genuinely faster. For anything that must be reproduced, reviewed, or deleted cleanly, the trade is clearly worthwhile.

---

## What Belongs in a Template

The useful principle: **anything that should exist tomorrow.**

- Networking — VPCs, subnets, route tables, security groups
- Compute — launch templates, Auto Scaling groups, Lambda functions
- Data stores — RDS instances, DynamoDB tables, S3 buckets
- IAM roles and policies
- Monitoring — alarms, log groups with retention

What does not belong: one-off investigation resources, and application data. Note that **log groups do belong** — declaring them in a template is how retention is set before a service auto-creates the group with the never-expire default.

---

## Stacks

A **stack** is the unit of management: a template plus its parameters, and the collection of resources created from it.

```bash
aws cloudformation deploy \
  --template-file network.yaml \
  --stack-name production-network \
  --parameter-overrides Environment=production VpcCidr=10.0.0.0/16
```

*Creates the stack if absent, updates it if present. `deploy` handles both, unlike the older `create-stack` and `update-stack`.*

Deleting a stack deletes its resources — which makes cleanup a single reliable operation rather than a hunt for things that were created together.

---

## Where CloudFormation Sits

It is one of several options, and the choice is mostly about authoring experience rather than capability:

- **CloudFormation** — native, YAML or JSON, no additional tooling.
- **SAM** — a CloudFormation extension with concise syntax for serverless resources.
- **CDK** — infrastructure defined in a programming language, synthesized to CloudFormation.
- **Terraform** — multi-cloud, with its own state management.

SAM and CDK produce CloudFormation, so understanding CloudFormation explains what they generate and is what makes debugging them possible. That is the reason to learn it first even when using something else.

---

## Key Takeaways

- CloudFormation reconciles reality to a declared end state rather than executing steps.
- Dependency order is derived from references, so ordering need not be written.
- Redeploying is idempotent, updates change only the difference, and removing a resource deletes it.
- Failed updates roll back automatically.
- The costs are slower deployment, verbose templates, distinctive failure modes, and occasional gaps in resource support.
- Put anything durable in a template — including log groups, so retention is set before auto-creation.
- A stack is the unit of creation, update, and deletion, making cleanup a single operation.
- SAM and CDK generate CloudFormation, so understanding it is what makes them debuggable.
