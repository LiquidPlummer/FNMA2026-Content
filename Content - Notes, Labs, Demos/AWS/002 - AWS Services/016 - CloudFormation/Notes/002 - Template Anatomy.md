# Template Anatomy

A CloudFormation template has a fixed set of top-level sections. Only `Resources` is required; the others make templates reusable and configurable.

---

## The Sections

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: Application network and compute

Parameters:      # inputs supplied at deploy time
Mappings:        # static lookup tables
Conditions:      # boolean expressions controlling resource creation
Resources:       # what to create — the only required section
Outputs:         # values exported from the stack
```

---

## Parameters

Inputs supplied at deploy time, which is what makes one template serve several environments:

```yaml
Parameters:
  Environment:
    Type: String
    AllowedValues: [development, staging, production]
    Description: Deployment environment

  InstanceType:
    Type: String
    Default: t3.micro
    AllowedValues: [t3.micro, t3.small, m5.large]

  VpcId:
    Type: AWS::EC2::VPC::Id          # console shows a dropdown of real VPCs
```

*`AllowedValues` validates before deployment starts. AWS-specific types like `AWS::EC2::VPC::Id` validate that the resource exists and give a picker in the console.*

Two important points about parameters:

**`NoEcho: true`** hides a value in the console and in API responses. It does **not** encrypt it — the value is still in the stack, and it is still visible to anyone who can describe it in some contexts. **Secrets do not belong in parameters.** Reference a Secrets Manager secret instead:

```yaml
DBPassword:
  Type: AWS::SSM::Parameter::Value<String>
  Default: /app/production/db/password
```

*Resolves from Parameter Store at deploy time. For Secrets Manager, use a dynamic reference in the resource property so the value never enters the template at all.*

**Too many parameters is a smell.** A template with twenty parameters is usually one that should be several templates, or one that should use mappings and conditions instead.

---

## Mappings

Static lookup tables keyed by two levels:

```yaml
Mappings:
  EnvironmentConfig:
    development:
      InstanceType: t3.micro
      MinSize: 1
    production:
      InstanceType: m5.large
      MinSize: 3

Resources:
  ASG:
    Type: AWS::AutoScaling::AutoScalingGroup
    Properties:
      MinSize: !FindInMap [EnvironmentConfig, !Ref Environment, MinSize]
```

*One parameter selects a whole configuration set, rather than passing each value separately.*

This is the cleaner alternative to many parameters — the environment name is the input, and the values follow from it.

---

## Conditions

Boolean expressions controlling whether resources are created or which values are used:

```yaml
Conditions:
  IsProduction: !Equals [!Ref Environment, production]

Resources:
  Database:
    Type: AWS::RDS::DBInstance
    Properties:
      MultiAZ: !If [IsProduction, true, false]
      DeletionProtection: !If [IsProduction, true, false]

  ReadReplica:
    Type: AWS::RDS::DBInstance
    Condition: IsProduction       # created only in production
```

*A `Condition` on a resource controls whether it exists at all; `!If` in a property controls its value.*

This is how one template produces a small development environment and a full production one without duplication.

---

## Resources

The core section. Each resource has a logical ID, a type, and properties:

```yaml
Resources:
  ApplicationBucket:
    Type: AWS::S3::Bucket
    Properties:
      BucketName: !Sub "${AWS::StackName}-${AWS::AccountId}-assets"
      VersioningConfiguration:
        Status: Enabled
      PublicAccessBlockConfiguration:
        BlockPublicAcls: true
        BlockPublicPolicy: true
        IgnorePublicAcls: true
        RestrictPublicBuckets: true
```

*The logical ID `ApplicationBucket` is how other resources reference it. Deriving the bucket name from stack and account keeps the template deployable anywhere.*

**Logical IDs are the stack's identity for a resource.** Renaming one in a template does not rename the resource — CloudFormation sees the old ID removed and a new one added, so it **deletes and recreates**. For a database or a bucket, that is data loss, which makes logical IDs effectively permanent once deployed.

---

## Outputs

Values exposed from the stack, optionally exported for other stacks:

```yaml
Outputs:
  VpcId:
    Description: VPC ID
    Value: !Ref VPC
    Export:
      Name: !Sub "${AWS::StackName}-VpcId"

  LoadBalancerDNS:
    Value: !GetAtt LoadBalancer.DNSName
```

*An `Export` name must be unique per account per region and is how other stacks import the value.*

---

## Pseudo Parameters

Values CloudFormation supplies automatically, available in any template:

`AWS::Region`, `AWS::AccountId`, `AWS::StackName`, `AWS::StackId`, `AWS::Partition`, `AWS::NoValue`

Using these instead of hard-coded values is what makes a template portable across accounts and regions.

---

## Key Takeaways

- Only `Resources` is required; parameters, mappings, conditions, and outputs make templates reusable.
- Use `AllowedValues` and AWS-specific parameter types to validate inputs before deployment.
- `NoEcho` hides a value without encrypting it — reference Parameter Store or Secrets Manager instead of passing secrets.
- Many parameters usually indicate a template that should use mappings, or be split.
- Mappings let one environment name select a whole configuration set.
- Conditions control whether resources are created and which property values apply.
- Renaming a logical ID deletes and recreates the resource, so treat logical IDs as permanent.
- Use pseudo parameters rather than hard-coded account IDs and regions.
