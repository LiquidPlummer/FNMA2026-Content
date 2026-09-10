# Versioning

**Versioning** keeps every version of an object rather than replacing it. It protects against accidental overwrite and deletion — and it changes deletion semantics in ways that surprise people and cost money.

---

## How It Works

With versioning enabled, each `PUT` to an existing key creates a new version with a new version ID. Previous versions remain and are individually retrievable.

```
Key: config.json
├── Version 3 (current)  — written 2026-09-09
├── Version 2            — written 2026-08-14
└── Version 1            — written 2026-07-01
```

*A `GET` with no version ID returns the current version; specifying a version ID returns that one.*

**Versioning has three states**, and the third is the important one:

- **Unversioned** — the default for a new bucket
- **Enabled** — versions are retained
- **Suspended** — new writes stop creating versions, but **existing versions remain**

**Versioning cannot be turned off**, only suspended. Once enabled, the bucket permanently carries the concept of versions, and existing versions persist until explicitly deleted.

---

## Deletion Behavior

This is where versioning departs most from intuition.

**Deleting an object does not delete it.** S3 creates a **delete marker** — a zero-byte object that becomes the current version. A `GET` then returns 404, but every previous version still exists and is still billed.

```
Key: config.json
├── Delete marker (current)  ← GET returns 404
├── Version 3
├── Version 2
└── Version 1                ← all still stored, all still billed
```

*The object appears deleted while all its data remains.*

**Restoring is deleting the delete marker.** Removing the marker makes the previous version current again — which is exactly the recovery this feature exists for.

**Permanent deletion requires a version ID.** Only `DeleteObject` with a specific version ID removes data:

```bash
# Creates a delete marker — data remains
aws s3api delete-object --bucket reports --key config.json

# Permanently removes one version
aws s3api delete-object --bucket reports --key config.json --version-id 3HL4kqtJlcpXroDTDmJ+rmSpXd3dIbrHY
```

*The first hides the object; only the second frees storage.*

---

## The Cost Consequence

**A versioned bucket grows without bound by default.** Every overwrite retains the old version. A 100 MB file rewritten daily accumulates 3 GB a month, indefinitely, while `aws s3 ls` shows one 100 MB object.

This is one of the most common sources of unexplained S3 cost. The visible object list bears no relationship to what is being billed.

The fix is a lifecycle rule for non-current versions:

```json
{
  "NoncurrentVersionExpiration": { "NoncurrentDays": 90, "NewerNoncurrentVersions": 3 },
  "Expiration": { "ExpiredObjectDeleteMarker": true }
}
```

*Keeps the three most recent non-current versions and deletes anything older than 90 days, while cleaning up delete markers left with nothing behind them.*

**Enabling versioning without this rule is incomplete configuration.** The two belong together.

To see what is actually stored, list versions rather than objects:

```bash
aws s3api list-object-versions --bucket reports \
  --query "[length(Versions), length(DeleteMarkers)]"
```

*Counts versions and delete markers, which is usually much larger than the object count.*

---

## MFA Delete

**MFA Delete** requires an MFA token to permanently delete a version or to change the versioning state. It is a strong protection against both accidental and malicious deletion.

Its constraints are significant: it can only be enabled by the **root user**, only via the CLI or API, and it makes routine version cleanup require MFA. It suits buckets holding irreplaceable data — audit logs, compliance records — and is impractical for general use.

**Object Lock** is usually the better tool for retention requirements. It enforces write-once-read-many for a defined period, must be enabled at bucket creation, and prevents deletion even by administrators. It is what compliance regimes generally require.

---

## When to Enable Versioning

**Enable it** for buckets holding anything hard to reproduce — configuration, documents, uploads, application state — and for anything where accidental overwrite would be a real problem. It is also a prerequisite for cross-region replication.

**Consider skipping it** for buckets holding logs, derived artifacts, or build output that is regenerable and written once. Versioning adds cost and complexity with little benefit there.

**In all cases, pair it with lifecycle rules.** Versioning without expiration is a bucket that grows forever.

---

## Key Takeaways

- Versioning retains every version of an object; it can be suspended but never disabled, and existing versions persist.
- Deleting an object creates a delete marker — the data remains and continues to be billed.
- Restoring an object means deleting its delete marker; permanent removal requires deleting a specific version ID.
- Versioned buckets grow without bound by default, and the object listing does not reflect what is stored.
- Always pair versioning with a non-current version expiration lifecycle rule.
- Use `list-object-versions` to see actual storage, which is usually far more than the object count suggests.
- MFA Delete is strong but root-only and impractical for general use; Object Lock is the usual answer for compliance retention.
