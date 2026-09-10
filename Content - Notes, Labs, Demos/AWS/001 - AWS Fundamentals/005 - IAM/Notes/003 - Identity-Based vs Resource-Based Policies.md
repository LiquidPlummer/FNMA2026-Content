# Identity-Based vs Resource-Based Policies

Policies attach in two places, and which place changes what the policy can express. The difference matters most when access crosses an account boundary.

---

## Identity-Based Policies

Attached to a user, group, or role. They answer: **what may this identity do?**

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::company-reports/*"
  }]
}
```

*Attached to a role, this says the role may read objects from that bucket. There is no `Principal` — the identity holding the policy is the principal.*

These are the common case. Most permission grants are identity-based.

---

## Resource-Based Policies

Attached to a resource. They answer: **who may act on this resource?** They require a `Principal`, because the resource has no inherent identity.

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "AWS": "arn:aws:iam::444455556666:role/PartnerRole" },
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::company-reports/*"
  }]
}
```

*Attached to the bucket, this says a role in a different account may read its objects. The `Principal` element is what makes it resource-based.*

Only some services support them. The ones met most often are S3 bucket policies, SQS queue policies, SNS topic policies, Lambda resource policies, KMS key policies, and IAM role trust policies.

---

## Why Both Exist

Two capabilities that identity-based policies cannot provide:

**Cross-account access.** An identity-based policy in account A cannot grant access to a resource in account B — account A does not own that resource and cannot speak for it. The resource's own policy must permit it.

**Managing access from the resource's side.** A bucket policy answers "who can read this bucket?" in one document. Answering the same question from identity policies means inspecting every identity in the account.

---

## How They Combine

Within one account, either policy is sufficient: **identity-based OR resource-based grants access.** If the bucket policy allows a role, the role does not also need an identity policy for it.

Across accounts, **both are required**. The request is evaluated in both accounts and must pass in each:

```
Account A                          Account B
─────────                          ─────────
Role "reporter"                    Bucket "company-reports"
  identity policy:                   bucket policy:
  Allow s3:GetObject       ───►      Allow Principal=A:role/reporter
  on B's bucket                      Action s3:GetObject

Both must allow. Either one denying is fatal.
```

*Cross-account access requires the caller's account to permit the call outbound and the resource's account to permit it inbound.*

This is the single most common cross-account failure: one side is configured and the other is not, and the error message does not say which.

KMS is a partial exception worth knowing — a KMS key's key policy must grant access; an identity policy alone is not sufficient unless the key policy delegates to IAM.

---

## Choosing

| Situation | Use |
|---|---|
| Granting an application permissions | Identity-based, on its role |
| Cross-account access | Both — identity-based and resource-based |
| Answering "who can read this bucket?" in one place | Resource-based |
| Allowing S3 or EventBridge to invoke a Lambda | Resource-based, on the function |
| Broad permissions across many resources | Identity-based |

---

## Key Takeaways

- Identity-based policies attach to users, groups, and roles and say what that identity may do.
- Resource-based policies attach to a resource, require a `Principal`, and say who may act on it.
- Only some services support resource-based policies: S3, SQS, SNS, Lambda, KMS, and IAM role trust policies among them.
- Within one account, either policy type granting access is enough.
- Across accounts, both sides must allow the call, and a missing grant on either side produces the same unhelpful error.
- KMS is stricter: the key policy must grant access, not just the caller's identity policy.
