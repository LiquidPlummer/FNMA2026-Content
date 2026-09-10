# Where CDK and Terraform Sit

CloudFormation is one of several ways to define AWS infrastructure. Knowing how the alternatives relate to it makes the choice a decision rather than a preference.

---

## The Landscape

**CloudFormation** — AWS-native, YAML or JSON, state managed by AWS.

**SAM** — a CloudFormation extension with shorthand for serverless resources. A SAM template *is* a CloudFormation template with additional resource types that expand into standard ones.

**CDK** — infrastructure written in TypeScript, Python, Java, Go, or C#, synthesized into CloudFormation and deployed by it.

**Terraform** — a separate tool from HashiCorp, multi-cloud, with its own state file and its own provider model.

**Pulumi** — like CDK in using general-purpose languages, like Terraform in being multi-cloud with its own state.

---

## SAM

SAM's value is concision for serverless:

```yaml
Transform: AWS::Serverless-2016-10-31

Resources:
  OrdersApi:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: src/
      Handler: app.handler
      Runtime: python3.12
      Events:
        Api:
          Type: HttpApi
          Properties:
            Path: /orders
            Method: GET
```

*About fifteen lines producing a function, its execution role, its log group, an HTTP API, the route, and the invoke permission — roughly a hundred lines of plain CloudFormation.*

`sam local` also runs functions locally against sample events, which is a genuine development-loop improvement.

**Use SAM for serverless-heavy applications.** It is CloudFormation, so everything about stacks, change sets, and drift applies unchanged.

---

## CDK

CDK defines infrastructure in code:

```typescript
const vpc = new ec2.Vpc(this, 'Vpc', { maxAzs: 3, natGateways: 1 });

const cluster = new rds.DatabaseCluster(this, 'Database', {
  engine: rds.DatabaseClusterEngine.auroraPostgres({ version: AuroraPostgresEngineVersion.VER_16_1 }),
  vpc,
  writer: rds.ClusterInstance.serverlessV2('writer'),
});
```

*A few lines produce a VPC with public and private subnets across three AZs, route tables, a NAT gateway, and an Aurora cluster with subnet groups and security groups — hundreds of lines of CloudFormation.*

**What CDK provides:** loops, conditionals, and functions instead of template gymnastics; type checking and IDE completion; **constructs** that encode sensible defaults; and unit-testable infrastructure.

**What it costs:** another layer to debug — an error may be in the code, the synthesized template, or the deployment; constructs that hide what is being created, which matters when the generated resources cost money; version churn in the CDK libraries; and a synthesis step between source and deployment.

**`cdk synth` is the essential habit.** It prints the generated CloudFormation, which is how a construct's actual output becomes visible. Deploying constructs without ever reading their output is how a surprising bill appears.

---

## Terraform

Terraform's distinguishing features are multi-cloud support and its own state model:

```hcl
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
  tags       = { Name = "production" }
}
```

**Where it is stronger:** one tool across AWS, Azure, GCP, and many SaaS providers; `terraform plan` is generally considered a better preview than change sets; a large module ecosystem; and provider support for new AWS features sometimes arrives before CloudFormation's.

**Where CloudFormation is stronger:** **AWS manages the state**, so there is no state file to store, lock, or corrupt — which is a real operational burden in Terraform, requiring a remote backend with locking; native service integration; StackSets for multi-account deployment; and no version-compatibility management for the tool itself.

The state file is the crux. Terraform's state is a file we own and must protect: lose it and Terraform no longer knows what it manages; corrupt it and recovery is manual; share it carelessly between engineers and concurrent applies conflict. CloudFormation has none of these problems because the state lives in the service.

---

## Choosing

| Situation | Choice |
|---|---|
| Serverless application on AWS | SAM |
| Complex AWS infrastructure, team comfortable with code | CDK |
| AWS-only, prefer explicit declarations | CloudFormation |
| Multi-cloud, or already using Terraform | Terraform |
| Existing investment in one of these | Keep it |

**The last row matters most.** These tools are broadly equivalent in capability, and the cost of maintaining two is real. Consistency within an organization is worth more than picking the theoretically better tool.

---

## Why CloudFormation Is Still Worth Learning

Even when using SAM or CDK, CloudFormation is what actually runs:

- **SAM and CDK both produce CloudFormation.** Debugging a failed deployment means reading CloudFormation events and error messages.
- **Stack behavior is CloudFormation behavior.** Change sets, rollback states, drift, deletion policies, and replacement all apply.
- **Resource property documentation is CloudFormation documentation.** CDK constructs map to CloudFormation resource types and their properties.
- **The failure modes are the same.** `ROLLBACK_COMPLETE`, `UPDATE_ROLLBACK_FAILED`, and replacement-driven data loss occur identically.

Understanding the layer underneath is what makes the layer above debuggable — which is the reason this lesson exists in this order.

---

## Key Takeaways

- SAM is CloudFormation with serverless shorthand, expanding into standard resources and adding local testing.
- CDK writes infrastructure in a programming language and synthesizes CloudFormation, gaining loops, types, and testability.
- CDK constructs hide what is created, so `cdk synth` is essential for knowing what will be deployed and billed.
- Terraform is multi-cloud with a better plan output, at the cost of owning and protecting a state file.
- CloudFormation's state is managed by AWS, removing the state-file operational burden entirely.
- The tools are broadly equivalent, so organizational consistency matters more than the choice.
- SAM and CDK both deploy through CloudFormation, so its events, failure states, and behaviors apply regardless.
