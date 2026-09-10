# Enabling Encryption on S3, EBS & RDS

Where the encryption setting lives, and — more importantly — whether it can be changed after the resource exists. The answer differs per service, and getting it wrong means rebuilding.

---

## S3

Encryption is per bucket (as a default) and per object (as an override). Every bucket now has **SSE-S3** encryption on by default, so objects are never unencrypted at rest.

The choice is which key type:

| Option | Key | Notes |
|---|---|---|
| **SSE-S3** | S3-managed | Default, free, no KMS control |
| **SSE-KMS** | KMS key | Auditable, controllable, KMS request charges apply |
| **DSSE-KMS** | KMS key, twice | Two layers, for specific compliance requirements |
| **SSE-C** | Supplied per request | We manage the key entirely; S3 stores none of it |

```bash
aws s3api put-bucket-encryption --bucket company-reports \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "aws:kms",
        "KMSMasterKeyID": "alias/app-data"
      },
      "BucketKeyEnabled": true
    }]
  }'
```

*Sets the bucket's default encryption to a specific KMS key. `BucketKeyEnabled` reduces KMS request volume substantially and should generally be on.*

**Changeable later — but only for new objects.** Changing bucket encryption does not re-encrypt existing objects. Those keep the key they were written with. Re-encrypting means copying the objects over themselves, which is a real job for a large bucket.

---

## EBS

Encryption is set **at volume creation and cannot be changed**. There is no way to encrypt an existing volume in place.

Converting one requires a detour through a snapshot:

```bash
aws ec2 create-snapshot --volume-id vol-0abc123 --description "pre-encryption"
aws ec2 copy-snapshot --source-region us-east-1 --source-snapshot-id snap-0abc123 \
  --encrypted --kms-key-id alias/ebs-key
# then create a new volume from the encrypted snapshot, stop the instance,
# detach the old volume, and attach the new one
```

*The copy step is where encryption is applied; a snapshot copy can be encrypted even when the source is not. The instance must be stopped to swap the root volume.*

Because this is disruptive, **enable account-level EBS encryption by default** — a per-region setting that encrypts every new volume automatically, including those created by Auto Scaling and by services that create volumes on our behalf. It costs nothing and prevents the problem entirely.

Snapshots inherit the volume's encryption, and volumes created from a snapshot inherit the snapshot's. An unencrypted lineage stays unencrypted until someone breaks it with an encrypted copy.

---

## RDS

The strictest of the three: **encryption is set at instance creation and cannot be changed**. There is no modify operation for it.

Converting an unencrypted instance requires a full rebuild:

1. Snapshot the unencrypted instance.
2. Copy the snapshot with encryption enabled.
3. Restore a new instance from the encrypted copy.
4. Repoint applications, then delete the original.

That is downtime or a migration, not a setting change. Enabling encryption at creation is essentially free by comparison, so the practical rule is to enable it on every RDS instance including non-production ones — a development database created unencrypted is the one that later gets promoted.

Two related points: read replicas inherit the source's encryption state, and automated backups and snapshots are encrypted whenever the instance is.

---

## The Pattern

| Service | Set at | Changeable later? |
|---|---|---|
| S3 | Bucket default or per object | Yes, for new objects only |
| EBS | Volume creation | No — snapshot, copy encrypted, replace |
| RDS | Instance creation | No — snapshot, copy encrypted, restore |
| DynamoDB | Always on | Key type can change |
| EFS | Filesystem creation | No |

The general rule for AWS storage: **encryption is a creation-time decision.** S3 is the flexible exception, and even there existing objects are unaffected.

The practical response is to make it a default rather than a decision. Turn on EBS encryption by default per region, set bucket encryption in whatever templates create buckets, and enable RDS encryption in every environment. Retrofitting is the expensive path, and it is entirely avoidable.

---

## Key Takeaways

- S3 buckets are encrypted by default with SSE-S3; SSE-KMS adds auditability and control at the cost of KMS requests.
- Changing S3 bucket encryption affects new objects only — existing objects keep their original key.
- EBS encryption is fixed at volume creation; converting requires a snapshot, an encrypted copy, and a volume swap.
- Enable account-level EBS encryption by default per region so every new volume is covered automatically.
- RDS encryption is fixed at instance creation; converting requires a snapshot, encrypted copy, and restore.
- Enable `BucketKeyEnabled` on KMS-encrypted buckets to cut KMS request costs.
- Treat encryption as a creation-time default in every environment, since retrofitting is disruptive.
