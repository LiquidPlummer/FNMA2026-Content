# Intrinsic Functions & Cross-Stack References

Templates are static YAML, so values that are only known at deploy time — resource IDs, generated names, account numbers — come from **intrinsic functions**. Cross-stack references extend that between stacks.

---

## The Core Functions

**`!Ref`** returns a resource's primary identifier, or a parameter's value:

```yaml
VpcId: !Ref VPC              # the VPC's ID
InstanceType: !Ref InstType  # the parameter's value
```

*What `Ref` returns differs per resource type — a VPC returns its ID, an S3 bucket returns its name, an IAM role returns its name rather than its ARN. Each resource's documentation states this, and assuming is a common source of errors.*

**`!GetAtt`** returns a specific attribute:

```yaml
LoadBalancerDNS: !GetAtt LoadBalancer.DNSName
RoleArn: !GetAtt ExecutionRole.Arn
DatabaseEndpoint: !GetAtt Database.Endpoint.Address
```

*Used when `Ref` does not return what is needed — notably `!GetAtt Role.Arn`, since `!Ref` on a role returns the name.*

**`!Sub`** substitutes values into a string:

```yaml
BucketName: !Sub "${AWS::StackName}-${AWS::AccountId}-assets"

UserData:
  Fn::Base64: !Sub |
    #!/bin/bash
    echo "TABLE_NAME=${OrdersTable}" >> /etc/app.env
    echo "REGION=${AWS::Region}" >> /etc/app.env
```

*`!Sub` is the most useful of the three, handling both parameters and resource references inline. It is far more readable than `!Join`, which it largely replaces.*

---

## Other Functions Worth Knowing

```yaml
!Join [":", [a, b, c]]                          # "a:b:c"
!Select [0, !GetAZs ""]                         # first AZ in the region
!Split [",", !Ref CommaSeparatedList]           # string to list
!FindInMap [EnvConfig, !Ref Environment, Size]  # mapping lookup
!If [IsProduction, m5.large, t3.micro]          # conditional value
!ImportValue SharedNetwork-VpcId                # another stack's export
!Ref AWS::NoValue                               # omit a property entirely
```

Two are especially useful:

**`!GetAZs`** returns the region's Availability Zones, so a template can spread subnets across AZs without hard-coding names:

```yaml
AvailabilityZone: !Select [0, !GetAZs ""]
```

*Portable across regions, which hard-coded `us-east-1a` is not.*

**`AWS::NoValue`** removes a property conditionally:

```yaml
SnapshotIdentifier: !If [RestoreFromSnapshot, !Ref SnapshotId, !Ref "AWS::NoValue"]
```

*Either sets the property or omits it entirely — different from setting it to an empty string, which many properties reject.*

---

## Cross-Stack References

Large infrastructures are split into several stacks — network, data, application — which then need each other's values.

**Exports and imports** are the native mechanism. A stack exports:

```yaml
Outputs:
  VpcId:
    Value: !Ref VPC
    Export:
      Name: !Sub "${AWS::StackName}-VpcId"
```

And another imports:

```yaml
Resources:
  Subnet:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !ImportValue production-network-VpcId
```

*Export names are unique per account per region.*

Two constraints make this rigid:

**An exported value cannot be changed while it is imported.** The importing stack must stop using it first, which can require a coordinated multi-stack deployment.

**A stack cannot be deleted while its exports are in use.** This is protective and can make teardown awkward.

---

## Parameter Store as an Alternative

Because of that rigidity, many teams pass values through Parameter Store instead:

```yaml
# Producing stack
VpcIdParameter:
  Type: AWS::SSM::Parameter
  Properties:
    Name: /network/production/vpc-id
    Type: String
    Value: !Ref VPC

# Consuming stack
Parameters:
  VpcId:
    Type: AWS::SSM::Parameter::Value<String>
    Default: /network/production/vpc-id
```

*Looser coupling — the producing stack can change without blocking the consumer, and stacks can be deleted independently.*

The trade is losing the protection exports provide. An export prevents deleting something another stack depends on; a Parameter Store value does not, so a change can break a consumer silently.

**Exports suit tightly coupled stacks** where the dependency should be enforced. **Parameter Store suits looser coupling** where independent deployment matters more than the guardrail.

---

## Dynamic References

Values resolved at deployment time from Parameter Store or Secrets Manager, without appearing in the template:

```yaml
MasterUserPassword: '{{resolve:secretsmanager:prod/db/credentials:SecretString:password}}'
LogLevel: '{{resolve:ssm:/app/production/log-level}}'
```

*The secret is fetched at deploy time. The value never appears in the template, in the stack parameters, or in `describe-stacks` output — which is why this is the correct way to handle a password in CloudFormation.*

For RDS specifically, `ManageMasterUserPassword: true` is better still, letting RDS generate and rotate the password with no reference at all.

---

## Key Takeaways

- `!Ref` returns a resource's primary identifier, which differs per type — an IAM role returns its name, not its ARN.
- `!GetAtt` retrieves specific attributes such as `Role.Arn` and `Database.Endpoint.Address`.
- `!Sub` handles inline substitution of parameters and resource references, replacing most uses of `!Join`.
- `!GetAZs` with `!Select` spreads resources across AZs without hard-coding zone names.
- `AWS::NoValue` omits a property entirely, which differs from setting it empty.
- Exports and imports enforce dependencies, and an exported value cannot change or be deleted while imported.
- Parameter Store passes values with looser coupling, trading the dependency guardrail for independent deployment.
- Dynamic references resolve secrets at deploy time so values never enter the template or stack outputs.
