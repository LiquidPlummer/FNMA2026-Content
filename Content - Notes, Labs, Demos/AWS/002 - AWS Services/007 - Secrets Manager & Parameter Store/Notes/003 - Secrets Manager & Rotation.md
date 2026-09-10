# Secrets Manager & Rotation

**Secrets Manager** is purpose-built for credentials. Its distinguishing feature is **built-in rotation** — the ability to change a secret's value automatically, on a schedule, without downtime.

---

## Storing a Secret

```bash
aws secretsmanager create-secret \
  --name prod/orders/db \
  --description "Orders database credentials" \
  --secret-string '{"username":"orders_app","password":"s3cr3t","host":"orders.abc123.us-east-1.rds.amazonaws.com","port":5432}' \
  --kms-key-id alias/app-secrets
```

*Secrets are commonly stored as JSON so related values travel together — one retrieval yields the whole connection configuration.*

Retrieval:

```python
import boto3, json

client = boto3.client("secretsmanager")
secret = json.loads(
    client.get_secret_value(SecretId="prod/orders/db")["SecretString"]
)
connection = connect(host=secret["host"], user=secret["username"], password=secret["password"])
```

*The application holds only the secret's name. Access requires `secretsmanager:GetSecretValue` plus `kms:Decrypt` on the key.*

---

## Versions and Staging Labels

Secrets Manager maintains versions with **staging labels** — the mechanism that makes rotation safe:

- **`AWSCURRENT`** — the version applications get by default
- **`AWSPREVIOUS`** — the prior version
- **`AWSPENDING`** — a new version being created during rotation

Rotation is fundamentally a matter of moving `AWSCURRENT` from one version to another, with `AWSPREVIOUS` retained so anything still holding the old value keeps working briefly.

---

## Rotation

Rotation runs a Lambda function on a schedule. AWS provides ready-made functions for RDS, DocumentDB, and Redshift; custom secrets need a custom function.

The rotation function is called four times, in order:

**`createSecret`** — generate a new value and store it as `AWSPENDING`.
**`setSecret`** — apply it in the target system, such as changing the database password.
**`testSecret`** — verify the new value works, typically by connecting with it.
**`finishSecret`** — move `AWSCURRENT` to the new version.

```bash
aws secretsmanager rotate-secret \
  --secret-id prod/orders/db \
  --rotation-lambda-arn arn:aws:lambda:us-east-1:123456789012:function:SecretsRotation \
  --rotation-rules '{"ScheduleExpression": "rate(30 days)"}'
```

*Rotates every 30 days. The four-step sequence with a test before promotion is what prevents rotation from breaking the application.*

---

## The Two-User Strategy

The subtlety that makes rotation work without downtime.

**Single-user rotation** changes the password of the user the application is using. There is a window between the password changing and every application instance picking up the new value, during which connections fail.

**Alternating-user rotation** uses two database users. Rotation updates the one *not* currently in `AWSCURRENT`, tests it, then promotes it:

```
Cycle 1:  AWSCURRENT → orders_app_a     (rotation updates orders_app_b, then promotes)
Cycle 2:  AWSCURRENT → orders_app_b     (rotation updates orders_app_a, then promotes)
```

*The credential being changed is never the one in active use, so there is no window where the current secret is invalid.*

This requires both users to exist with identical privileges, and it is the correct choice for production databases where a failed connection window is unacceptable.

---

## Rotation Is Not Free of Consequences

Two practical points that catch people:

**Applications must handle the change.** An application reading the secret once at startup and caching it forever will fail after rotation. It needs to re-fetch on an authentication failure, or refresh periodically.

**The rotation function needs network access.** A rotation Lambda changing an RDS password must reach the database — so it needs VPC configuration, a security group allowing it, and either NAT or a VPC endpoint to reach the Secrets Manager API. This is the most common reason a rotation setup fails.

---

## Cross-Account and Replication

Secrets support **resource policies**, so a secret can be shared with another account — useful for a shared database credential. This requires a customer-managed KMS key, since the AWS-managed key cannot be shared.

**Multi-region replication** keeps read-only replicas of a secret in other regions, so a multi-region application reads locally rather than across regions.

---

## Cost

Roughly **$0.40 per secret per month**, plus about $0.05 per 10,000 API calls. Individually small, and it adds up across many secrets and many environments — which is the main argument for keeping non-secret configuration in Parameter Store.

Caching also reduces the API charge substantially, and is covered in the retrieval note.

---

## Key Takeaways

- Secrets Manager stores credentials, commonly as JSON so related values are retrieved together.
- Staging labels `AWSCURRENT`, `AWSPREVIOUS`, and `AWSPENDING` are what make rotation safe.
- Rotation runs a Lambda through create, set, test, and finish steps, testing before promotion.
- AWS supplies rotation functions for RDS, DocumentDB, and Redshift; other secrets need a custom function.
- Alternating-user rotation avoids any window where the current credential is invalid, and is correct for production databases.
- Applications must re-fetch after rotation — caching a secret indefinitely fails at the next rotation.
- Rotation Lambdas need VPC placement, security group access to the target, and network access to the Secrets Manager API.
- Cost is roughly $0.40 per secret per month, which argues for keeping non-secret configuration in Parameter Store.
