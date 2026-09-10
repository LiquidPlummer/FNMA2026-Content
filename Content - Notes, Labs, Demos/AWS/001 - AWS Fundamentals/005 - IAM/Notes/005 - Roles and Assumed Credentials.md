# Roles and Assumed Credentials

A role is an identity that nobody logs into. Principals *assume* it and receive temporary credentials. This mechanism replaces long-lived access keys in nearly every situation, and it is worth understanding how it actually works.

---

## The Two Policies on a Role

Every role has two distinct policy attachments, and confusing them is a common source of failure.

**The trust policy** (a resource-based policy) says *who may assume the role*:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Service": "ec2.amazonaws.com" },
    "Action": "sts:AssumeRole"
  }]
}
```

*This role can be assumed by the EC2 service, which is what allows it to be attached to an instance.*

**Permissions policies** say *what the role may do once assumed* — ordinary identity-based policies.

A role with permissions but a trust policy that names nobody useful is unusable. A role trusted by everything with no permissions is harmless. Both must be right.

Principals in a trust policy can be an AWS service, another account, a specific role or user, or a federated identity provider:

```json
"Principal": { "Service": "lambda.amazonaws.com" }
"Principal": { "AWS": "arn:aws:iam::444455556666:root" }
"Principal": { "AWS": "arn:aws:iam::444455556666:role/DeployRole" }
"Principal": { "Federated": "arn:aws:iam::123456789012:oidc-provider/token.actions.githubusercontent.com" }
```

*`:root` in a trust policy means "any identity in that account that also has permission to assume this role" — it does not mean the root user.*

---

## Assuming a Role

`sts:AssumeRole` returns temporary credentials:

```bash
aws sts assume-role \
  --role-arn arn:aws:iam::444455556666:role/DeployRole \
  --role-session-name kyle-deploy
```

```json
{
  "Credentials": {
    "AccessKeyId": "ASIAIOSFODNN7EXAMPLE",
    "SecretAccessKey": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
    "SessionToken": "FwoGZXIvYXdzEBYaDOEXAMPLE...",
    "Expiration": "2026-09-09T18:30:00Z"
  },
  "AssumedRoleUser": {
    "Arn": "arn:aws:sts::444455556666:assumed-role/DeployRole/kyle-deploy"
  }
}
```

*Three values instead of two — assumed credentials always include a session token, and they carry an explicit expiry.*

Two details worth noting. Assumed access key IDs begin with `ASIA` rather than `AKIA`, which distinguishes temporary from long-lived at a glance. And the `--role-session-name` appears in every CloudTrail entry for that session, so it should identify who or what is using the role.

Sessions default to one hour and can be configured up to twelve. When the CLI or an SDK assumes a role through profile configuration, it refreshes expiring credentials automatically.

---

## Why Roles Beat Access Keys

**Credentials expire on their own.** A leaked session token is useless within hours. A leaked access key works until someone notices — and public repositories are scanned continuously by people looking for exactly that.

**Nothing needs rotating.** There is no secret stored anywhere, so there is no rotation procedure to design or forget.

**Nothing needs storing.** No secret in a config file, an environment variable, or a repository. The credential is fetched at runtime and never written down.

**Sessions are attributable.** The session name in CloudTrail identifies who assumed the role, so a shared role still produces per-person audit records.

**Access is revoked centrally.** Removing someone's ability to assume a role cuts their access immediately, without touching any credential.

---

## Where Roles Show Up

| Situation | Mechanism |
|---|---|
| EC2 instance calling AWS | Instance profile |
| Lambda function calling AWS | Execution role |
| ECS/EKS task calling AWS | Task role |
| One AWS service acting on another | Service role |
| Access to another account | Cross-account role assumption |
| Human sign-in | IAM Identity Center, which assumes roles behind the scenes |
| CI/CD pipeline | OIDC federation to a role |

The last one is worth emphasizing. A CI system with OIDC federation exchanges a signed token from its provider for AWS credentials, which means **no AWS secret is stored in the CI system at all**. That removes the largest remaining reason teams keep long-lived keys.

---

## Key Takeaways

- A role carries a trust policy saying who may assume it and permissions policies saying what it can do; both must be correct.
- `sts:AssumeRole` returns an access key, a secret, and a session token with an explicit expiry.
- Temporary access keys start with `ASIA`; long-lived ones start with `AKIA`.
- The role session name is recorded in CloudTrail, preserving attribution for shared roles.
- Roles remove the need to store or rotate secrets and limit the useful life of any leak.
- Instance profiles, execution roles, task roles, cross-account assumption, and OIDC federation are all the same mechanism applied in different places.
