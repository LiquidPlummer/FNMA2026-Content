# Envelope Encryption

KMS encrypts at most 4 KB per call, yet S3 encrypts multi-gigabyte objects with KMS keys. The mechanism reconciling those two facts is **envelope encryption**, and it explains a great deal of KMS behavior — including its permission requirements and its cost profile.

---

## The Problem

Sending bulk data to KMS for encryption would be unworkable. It would mean transmitting every byte to an external service and back, bounded by network throughput and KMS request limits, and it would put the data itself in front of a service that has no reason to see it.

So services do not send data to KMS. They send *keys*.

---

## The Mechanism

Two keys are involved. A **data key** encrypts the data locally; the **KMS key** encrypts the data key.

**Encrypting:**

1. The service calls `kms:GenerateDataKey`, which returns the same key twice — once in plaintext, once encrypted under the KMS key.
2. The service encrypts the data locally with the plaintext data key.
3. It discards the plaintext data key from memory.
4. It stores the encrypted data key alongside the ciphertext.

**Decrypting:**

1. The service reads the encrypted data key stored with the object.
2. It calls `kms:Decrypt` on that key — a small payload, well within limits.
3. KMS returns the plaintext data key.
4. The service decrypts the data locally and discards the key again.

```
GenerateDataKey ──► plaintext data key ──► encrypt 5 GB locally ──► discard
                └─► encrypted data key ──► stored next to the ciphertext

Decrypt(encrypted data key) ──► plaintext data key ──► decrypt locally ──► discard
```

*Bulk data never travels to KMS; only the small data key does. The "envelope" is the encrypted data key wrapped around the encrypted data.*

---

## What This Explains

**Why `kms:GenerateDataKey` is required.** An application with `kms:Decrypt` but not `kms:GenerateDataKey` can read encrypted S3 objects but not write them. This is a frequent and confusing permission gap, and it follows directly from the mechanism.

**Why encryption is fast.** Bulk encryption happens locally at local speed. KMS is involved once per object, not once per byte.

**Why the encrypted data key travels with the data.** An S3 object's encrypted data key is stored in its metadata. Copy the object elsewhere and the envelope goes with it — which is also why a copy is unreadable without access to the original KMS key.

**Why deleting a KMS key destroys data.** The data key cannot be decrypted, so the data cannot be decrypted. This is not a metaphor: the data becomes permanently unrecoverable, which is why KMS enforces a 7–30 day waiting period before deletion.

---

## Caching Data Keys

A per-request `kms:Decrypt` call adds latency and cost at high volume. **Data key caching** reuses a decrypted data key across many operations, trading a small amount of security isolation for a large reduction in KMS calls.

The AWS Encryption SDK implements this with configurable limits:

```python
from aws_encryption_sdk import LocalCryptoMaterialsCache, CachingCryptoMaterialsManager

cache = LocalCryptoMaterialsCache(capacity=100)
manager = CachingCryptoMaterialsManager(
    master_key_provider=key_provider,
    cache=cache,
    max_age=300.0,          # seconds a cached key may be reused
    max_messages_encrypted=1000,
)
```

*Bounds reuse by age and by message count, so a cached key covers many operations without being reused indefinitely.*

S3 offers a service-side equivalent, **S3 Bucket Keys**, which reduces KMS requests for a bucket by a large factor. It is a single setting and worth enabling on any bucket with meaningful request volume.

---

## Key Takeaways

- Envelope encryption encrypts data with a locally used data key, and encrypts that data key with the KMS key.
- Bulk data never reaches KMS; only the small data key does, which is why the 4 KB limit is not a constraint in practice.
- Writing encrypted data needs `kms:GenerateDataKey`, not just `kms:Decrypt`.
- The encrypted data key is stored with the data, so copies remain tied to the original KMS key.
- Deleting a KMS key permanently destroys the data encrypted under it, hence the mandatory waiting period.
- Data key caching and S3 Bucket Keys cut KMS request volume and cost at high throughput.
