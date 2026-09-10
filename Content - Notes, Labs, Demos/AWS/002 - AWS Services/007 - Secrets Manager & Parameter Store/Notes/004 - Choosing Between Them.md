# Choosing Between Them

Parameter Store and Secrets Manager overlap substantially. The decision comes down to three factors: cost, rotation, and size limits.

---

## The Comparison

| | Parameter Store | Secrets Manager |
|---|---|---|
| Cost | **Free** (standard) | ~$0.40 per secret per month |
| Max value size | 4 KB standard, 8 KB advanced | **64 KB** |
| Built-in rotation | No | **Yes** |
| RDS integration | No | **Native** |
| Cross-account sharing | Via advanced setup | **Resource policies** |
| Multi-region replication | No | **Yes** |
| Hierarchical paths | **Yes** | Naming convention only |
| Fetch a whole branch | **Yes** | No |
| Versioning | Yes, with labels | Yes, with staging labels |
| Encryption | Optional (SecureString) | Always |
| Public parameters | **Yes** (AMI IDs, etc.) | No |

---

## The Three Deciding Factors

**Cost.** Parameter Store standard parameters are free; Secrets Manager charges per secret per month. For a hundred configuration values, that is $0 versus $40/month — before multiplying across environments. This alone determines where non-secret configuration belongs.

**Rotation.** Secrets Manager rotates automatically with a tested, staged promotion. Parameter Store has no rotation mechanism; changing a value is a manual write or custom automation. Where rotation is required — by policy, compliance, or good practice on a production database — Secrets Manager does it properly and building the equivalent is not worth the effort.

**Size.** 64 KB versus 4 KB matters for certificates, private keys, and large JSON blobs. Most credentials fit comfortably in either.

---

## A Practical Split

The arrangement that works well in practice:

**Parameter Store for:**
- Application configuration — endpoints, timeouts, feature flags, log levels
- Non-rotating secrets — a third-party API key that has no rotation mechanism
- Anything referenced by CloudFormation, ECS task definitions, or CodeBuild
- AMI IDs and other AWS public parameters

**Secrets Manager for:**
- Database credentials, especially RDS with managed rotation
- Anything with a rotation requirement
- Secrets shared across accounts
- Secrets needing multi-region replication
- Values above 4 KB

```
/orders/production/api/timeout        → Parameter Store (String, free)
/orders/production/log/level          → Parameter Store (String, free)
/orders/production/thirdparty/key     → Parameter Store (SecureString, free)
prod/orders/db                        → Secrets Manager (rotated)
```

*Configuration and static secrets in the free service; the rotating database credential in the one built for it.*

---

## The Overlap Worth Knowing

**Parameter Store can read Secrets Manager secrets.** A parameter referenced with the `/aws/reference/secretsmanager/` prefix resolves to the secret's value:

```bash
aws ssm get-parameter --name /aws/reference/secretsmanager/prod/orders/db --with-decryption
```

*Reads a Secrets Manager secret through the SSM API, so an application or service that only speaks Parameter Store can use a rotated secret.*

This is useful where an integration supports Parameter Store but not Secrets Manager — ECS task definitions historically being one — while keeping rotation in Secrets Manager.

---

## Where Neither Belongs

Worth restating: **the best option is often no stored secret at all.**

- **RDS IAM authentication** issues short-lived tokens instead of passwords.
- **IAM roles** give AWS service access with no credential.
- **OIDC federation** lets CI systems obtain AWS credentials without storing any.

Each removes a secret entirely rather than storing it well. Check for these before deciding where to put a credential — the question "which store?" sometimes has the answer "neither."

---

## A Note on Consistency

Whichever is chosen, **use one convention consistently**. An account with some secrets in Parameter Store, some in Secrets Manager, some in environment variables, and some in a config file has no coherent answer to "where is this credential and who can read it?"

Documenting the split — configuration in Parameter Store, rotating credentials in Secrets Manager — and applying it uniformly is worth more than optimizing the choice for each individual value.

---

## Key Takeaways

- The decision turns on cost, rotation, and size; the services are otherwise similar in capability.
- Parameter Store standard parameters are free, making it the right home for configuration and the many non-secret values an application has.
- Secrets Manager charges per secret per month and provides tested, staged automatic rotation.
- Secrets Manager supports 64 KB values, cross-account resource policies, and multi-region replication.
- Parameter Store offers hierarchical paths, branch fetches, path-based IAM policies, and AWS public parameters.
- Parameter Store can read Secrets Manager secrets through a reference prefix, bridging integrations that support only one.
- Prefer IAM authentication where available, which removes the stored credential entirely.
- Apply one documented convention consistently rather than optimizing each value individually.
