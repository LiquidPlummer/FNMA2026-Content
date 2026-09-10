# Parameter Store

**Parameter Store**, part of AWS Systems Manager, holds configuration values and secrets in a hierarchical namespace. It is free for standard parameters, which makes it the default place for application configuration.

---

## Parameter Types

**String** — plain text. Configuration values, feature flags, endpoint URLs.

**StringList** — comma-separated values, returned as a list.

**SecureString** — encrypted with a KMS key. Reading it requires both the SSM permission and KMS decrypt permission.

```bash
aws ssm put-parameter --name /orders/production/db/host \
  --value "orders.abc123.us-east-1.rds.amazonaws.com" --type String

aws ssm put-parameter --name /orders/production/db/password \
  --value "s3cr3t" --type SecureString --key-id alias/app-secrets
```

*A plain configuration value and an encrypted one. Specifying `--key-id` uses a customer-managed key; omitting it uses the AWS-managed `aws/ssm` key, which cannot be shared across accounts.*

---

## The Hierarchy

Names are paths, and the hierarchy is functional rather than cosmetic:

```
/orders/production/db/host
/orders/production/db/port
/orders/production/api/timeout
/orders/staging/db/host
```

An entire branch can be fetched in one call:

```bash
aws ssm get-parameters-by-path --path /orders/production \
  --recursive --with-decryption
```

*Retrieves every parameter under that path, decrypting SecureStrings. One call replaces many, which matters for both latency and API cost.*

The hierarchy also drives access control. An IAM policy can grant access to a path prefix:

```json
{
  "Effect": "Allow",
  "Action": ["ssm:GetParameter", "ssm:GetParametersByPath"],
  "Resource": "arn:aws:ssm:us-east-1:123456789012:parameter/orders/production/*"
}
```

*The production application reads only production parameters. A path convention of `/<application>/<environment>/...` makes this straightforward.*

---

## Standard vs Advanced

| | Standard | Advanced |
|---|---|---|
| Cost | Free | ~$0.05 per parameter per month |
| Size | 4 KB | 8 KB |
| Count per account | 10,000 | 100,000 |
| Parameter policies | No | Yes |
| Higher throughput option | Available (charged) | Available (charged) |

**Standard parameters are free**, which is Parameter Store's main practical advantage. A hundred configuration values cost nothing, where the same in Secrets Manager would be $40/month.

**Advanced parameters** add **parameter policies** — expiration, expiration notification, and no-change notification. The last is useful for compliance: a notification when a value has not been changed within a defined period, which is a light form of rotation reminder.

---

## Versioning

Every write creates a new version, and previous versions are retained:

```bash
aws ssm get-parameter --name /orders/production/db/password:3 --with-decryption
```

*Retrieves version 3 specifically. Omitting the version returns the latest.*

**Labels** are named pointers to versions, which allows referencing `current` or `previous` rather than numbers:

```bash
aws ssm label-parameter-version --name /orders/production/db/password \
  --parameter-version 4 --labels current
```

*Moving the `current` label is how a rollback is performed without changing the value.*

---

## Reading From an Application

```python
import boto3

ssm = boto3.client("ssm")
response = ssm.get_parameters_by_path(
    Path="/orders/production",
    Recursive=True,
    WithDecryption=True,
)
config = {p["Name"].split("/")[-1]: p["Value"] for p in response["Parameters"]}
```

*Fetches all configuration in one call at startup. `WithDecryption` requires `kms:Decrypt` on the key, in addition to the SSM permission.*

Two operational notes: `get-parameters-by-path` paginates above 10 parameters, and the default throughput limit is modest — an application fetching parameters on every request will hit it, which is why caching matters.

---

## Integration

Parameter Store is referenced directly by several services, which removes the fetch entirely:

- **ECS task definitions** can inject parameters as environment variables from a `valueFrom` reference.
- **CloudFormation** resolves `{{resolve:ssm:/path/to/param}}` at deploy time.
- **CodeBuild** and **CodePipeline** read parameters directly.
- **AWS publishes public parameters**, notably the latest AMI IDs.

---

## When to Use It

Parameter Store is the right choice for **configuration**, and an acceptable one for **secrets that do not need automatic rotation**. Its free tier makes it the natural home for the large number of non-secret values every application has.

The next note covers Secrets Manager, and the note after that compares them directly.

---

## Key Takeaways

- Parameter Store holds String, StringList, and SecureString parameters, with standard parameters free.
- Hierarchical paths allow fetching a whole branch in one call and granting IAM access by prefix.
- Use a `/<application>/<environment>/...` convention so path-based policies work cleanly.
- Advanced parameters cost per parameter and add policies for expiration and change notification.
- Every write creates a retained version, and labels provide named pointers for rollback.
- Reading a SecureString requires KMS decrypt permission in addition to the SSM permission.
- ECS, CloudFormation, and CodeBuild reference parameters directly, and AWS publishes public parameters such as current AMI IDs.
- Default throughput is modest, so cache values rather than fetching per request.
