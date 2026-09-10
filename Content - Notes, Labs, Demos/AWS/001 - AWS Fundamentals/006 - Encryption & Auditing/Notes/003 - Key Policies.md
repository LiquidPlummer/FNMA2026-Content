# Key Policies

Every KMS key carries a **key policy** — a resource-based policy controlling who may use it. It is a second authorization layer that sits alongside IAM, and KMS is stricter about it than other services are about their resource policies.

---

## KMS Is Different

For most services, an identity policy alone is enough within an account. For KMS it is not: **the key policy must grant access.** An IAM policy saying `kms:Decrypt` on a key grants nothing unless that key's policy also permits it, directly or by delegating to IAM.

This is why a policy that looks correct still produces `AccessDeniedException` — the identity policy is fine, and the key policy was never touched.

---

## The Default Delegation

When a key is created through the console, its default policy includes this:

```json
{
  "Sid": "Enable IAM User Permissions",
  "Effect": "Allow",
  "Principal": { "AWS": "arn:aws:iam::123456789012:root" },
  "Action": "kms:*",
  "Resource": "*"
}
```

*This delegates authorization to IAM for that account: identities may use the key if their IAM policies allow it. `:root` here means the account, not the root user.*

With this statement present, KMS behaves like other services and IAM policies work as expected. Without it, only principals named explicitly in the key policy can use the key — and if nobody is named, **the key becomes permanently unusable**, including by administrators. KMS warns about this, and it is not recoverable.

---

## Two Kinds of Permission

Key policies separate **using** a key from **managing** it, and the split is worth preserving.

**Key users** perform cryptographic operations:

```json
{
  "Sid": "AllowUse",
  "Effect": "Allow",
  "Principal": { "AWS": "arn:aws:iam::123456789012:role/app-server-role" },
  "Action": [
    "kms:Encrypt",
    "kms:Decrypt",
    "kms:ReEncrypt*",
    "kms:GenerateDataKey*",
    "kms:DescribeKey"
  ],
  "Resource": "*"
}
```

*The standard set for an application: encrypt, decrypt, and generate data keys. `GenerateDataKey` is required for envelope encryption, so omitting it breaks S3 and EBS usage even when `Decrypt` is present.*

**Key administrators** manage the key's lifecycle — `kms:PutKeyPolicy`, `kms:ScheduleKeyDeletion`, `kms:EnableKeyRotation` — without necessarily being able to decrypt anything with it.

Separating these means an administrator can rotate and manage keys without reading the data they protect, and an application can decrypt data without being able to change who else can.

---

## Grants

For temporary or programmatic delegation, **grants** are an alternative to editing the policy:

```bash
aws kms create-grant \
  --key-id alias/app-data \
  --grantee-principal arn:aws:iam::123456789012:role/batch-job \
  --operations Decrypt GenerateDataKey
```

*Creates a revocable delegation without modifying the key policy — useful for short-lived access and for AWS services that need to use a key on our behalf.*

Grants can be revoked individually and are how several AWS services obtain key access internally.

---

## The Two-Layer Effect

Because both layers must allow, encryption becomes a genuine second control:

```
s3:GetObject on the object   ──┐
                               ├──► both required to read an encrypted object
kms:Decrypt on the key       ──┘
```

*Reading a KMS-encrypted S3 object needs the S3 permission and the KMS permission; either one missing produces a denial.*

This is what makes customer-managed keys useful for separation of duties. A role can be granted broad S3 access while being excluded from the key policy, and it will list and see objects it cannot read. It is also why "I have S3 admin and still get access denied" is a routine confusion — the missing permission is on the key.

Cross-account access uses the same mechanism: the key policy names the other account's principal, and that account grants its own identities `kms:Decrypt`. Both sides again.

---

## Key Takeaways

- KMS requires the key policy to grant access; an IAM policy alone is not sufficient.
- The default key policy delegates to IAM via an account-root principal, which is what makes IAM policies work for that key.
- A key policy naming nobody makes the key permanently unusable, and that cannot be undone.
- Separate key users (encrypt, decrypt, generate data keys) from key administrators (policy and lifecycle).
- `kms:GenerateDataKey` is required for envelope encryption; omitting it breaks S3 and EBS even when `Decrypt` is granted.
- Grants provide revocable, temporary delegation without editing the key policy.
- Reading encrypted data needs both the service permission and the key permission, which is what enables separation of duties.
