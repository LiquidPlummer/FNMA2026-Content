# KMS Keys

**KMS** (Key Management Service) holds encryption keys and performs cryptographic operations with them. The defining property: the key material never leaves KMS. We do not receive the key — we ask KMS to use it on our behalf, and every use is authorized and logged.

---

## What a KMS Key Is

A **KMS key** (formerly "customer master key" or CMK) is a logical key with an ID, an ARN, a policy, and key material that KMS stores and never exports.

Two operations matter:

```bash
# Encrypt up to 4 KB directly with a KMS key
aws kms encrypt --key-id alias/app-key --plaintext fileb://secret.txt --output text \
  --query CiphertextBlob

# Decrypt it — note the key is not specified; KMS reads it from the ciphertext
aws kms decrypt --ciphertext-blob fileb://encrypted.bin --output text --query Plaintext
```

*KMS encrypts small payloads directly, up to 4 KB. The decrypt call identifies its own key because the key ID is embedded in the ciphertext.*

The 4 KB limit is deliberate and shapes everything else — bulk data is handled by envelope encryption, covered separately.

---

## AWS-Managed vs Customer-Managed

The distinction people most often get wrong.

### AWS-managed keys

Created automatically per service, per region, with names like `aws/s3`, `aws/ebs`, `aws/rds`. They appear when a service first needs encryption.

- **Free.** No monthly charge.
- **Policy is fixed.** We cannot edit it; access is granted implicitly to the service on behalf of our account.
- **Rotation is automatic**, yearly, and not configurable.
- **Cannot be deleted or disabled.**
- **Cannot be shared across accounts.** This is the constraint that most often forces a change: a snapshot encrypted with an AWS-managed key cannot be shared with another account, so a cross-account restore is impossible.

### Customer-managed keys

Created by us.

- **Cost about $1/month each**, plus per-request charges.
- **Policy is ours**, which means we can express exactly who may use the key.
- **Rotation is optional** and configurable.
- **Can be disabled or scheduled for deletion** (with a mandatory 7–30 day waiting period).
- **Can be shared with other accounts** via key policy.
- **Support key-level audit and control** — including revoking access to encrypted data by disabling the key.

### What the difference actually buys

Choosing a customer-managed key buys three concrete things:

1. **Control over who can decrypt**, separate from who can read the resource. This is the second authorization layer covered in the next note.
2. **Cross-account sharing** of encrypted snapshots, images, and objects.
3. **The ability to make data unreadable** by disabling or deleting the key, without touching the data itself.

If none of those are needed, an AWS-managed key is a reasonable default and costs nothing. The mistake is creating dozens of customer-managed keys reflexively, and the opposite mistake is discovering at restore time that an AWS-managed key cannot be shared.

---

## Aliases

Keys are identified by UUID, which is unreadable. An **alias** is a friendly name that can be repointed to a different key:

```bash
aws kms create-alias --alias-name alias/app-data --target-key-id 1234abcd-12ab-34cd-56ef-1234567890ab
```

*Applications reference `alias/app-data`, so the underlying key can be replaced without changing any application configuration.*

---

## Rotation

**Automatic rotation** generates new key material yearly while retaining old material. Ciphertext encrypted with previous material still decrypts, because KMS keeps every version. Nothing needs re-encrypting, and the key ID and alias do not change — rotation is essentially free and invisible.

**Manual rotation** means creating a genuinely new key and repointing the alias. New data uses the new key; old data still needs the old key to decrypt, so the old key must be kept.

Automatic rotation is what compliance requirements usually mean, and it is a single checkbox.

---

## Cost

Small but not zero, and it can surprise:

- $1 per customer-managed key per month
- ~$0.03 per 10,000 requests
- AWS-managed keys are free, but requests against them are billed

A workload decrypting an object on every request generates a KMS call per request. High-throughput applications should use **data key caching** so one KMS call covers many operations — the mechanism for that is envelope encryption.

---

## Key Takeaways

- KMS stores key material that never leaves the service; we request operations rather than receiving keys.
- Direct KMS encryption is limited to 4 KB, which is why envelope encryption exists.
- AWS-managed keys are free with fixed policies, automatic yearly rotation, and no cross-account sharing.
- Customer-managed keys cost about $1/month and give us the key policy, optional rotation, deletion, and cross-account sharing.
- The main reasons to choose customer-managed: controlling decrypt access separately, sharing encrypted data across accounts, and being able to revoke access by disabling the key.
- Aliases decouple applications from key IDs; automatic rotation retains old material so nothing needs re-encrypting.
- KMS request charges accumulate in high-throughput workloads, making data key caching worthwhile.
