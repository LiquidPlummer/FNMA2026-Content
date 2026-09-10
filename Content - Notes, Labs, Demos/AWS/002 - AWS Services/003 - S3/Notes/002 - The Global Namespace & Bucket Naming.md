# The Global Namespace & Bucket Naming

Bucket names are **globally unique across all of AWS**. Not per account, not per region — across every account in the world. This is unusual enough to be worth understanding, and it has practical consequences.

---

## What Global Means

If any AWS customer anywhere has a bucket named `reports`, nobody else can create one with that name. Attempting it returns `BucketAlreadyExists`, with no indication of who holds it.

The namespace is global; the **data is not**. A bucket exists in one region, and its objects are stored there. So the name is global and the storage is regional — a split that causes real confusion.

The reason for the global namespace is the URL format. `https://my-bucket.s3.amazonaws.com/key` requires the name to be unambiguous, since the hostname must resolve to one bucket.

---

## Naming Rules

Bucket names must:

- Be 3–63 characters
- Contain only lowercase letters, numbers, hyphens, and periods
- Begin and end with a letter or number
- Not be formatted as an IP address
- Not begin with `xn--`, `sthree-`, or end with `-s3alias` or `--ol-s3`

**Avoid periods in bucket names.** A name with a period breaks virtual-hosted-style HTTPS access, because the wildcard certificate for `*.s3.amazonaws.com` does not cover `my.bucket.s3.amazonaws.com`. Unless the bucket is being used for static website hosting on a custom domain requiring a matching name, use hyphens.

Names cannot be changed. Renaming means creating a new bucket, copying every object, and deleting the old one.

---

## Choosing Names

Because short obvious names are taken, and because names are permanent, a convention helps:

```
<org>-<purpose>-<environment>-<region>
acme-reports-production-us-east-1
acme-logs-staging-eu-west-1
```

*A prefix guarantees availability, and the remaining components keep the name self-describing.*

Including the account ID or a random suffix is also common, particularly in CloudFormation templates where a fixed name would collide when the stack is deployed twice:

```yaml
ReportsBucket:
  Type: AWS::S3::Bucket
  Properties:
    BucketName: !Sub "acme-reports-${AWS::AccountId}-${AWS::Region}"
```

*Deriving the name from account and region makes the template deployable to several accounts and regions without collision.*

Alternatively, omitting `BucketName` entirely lets CloudFormation generate a unique one — the simplest approach when the name does not need to be predictable.

---

## The Security Consequence

Global uniqueness means bucket names are **discoverable**. Anyone can test whether a name exists by requesting it, and attackers routinely enumerate predictable names — `companyname-backups`, `companyname-data` — looking for buckets left public.

Two implications:

**Do not treat a name as secret.** Obscurity is not a control. Block Public Access and correct bucket policies are what protect data.

**Avoid names that reveal information.** `acme-acquisition-2026-financials` tells anyone probing that something exists, even if they cannot read it.

---

## Bucket Sniping

Because names are global and permanent, a released name can be claimed by anyone. This matters in two ways.

**Deleting a bucket releases its name immediately.** If anything still references that name — an application, a script, a hard-coded URL — and someone else claims it, those references now point at a bucket controlled by a stranger. For buckets whose names appear in published URLs or partner integrations, retaining the empty bucket is cheaper than the alternative.

**A referenced-but-nonexistent bucket is a hazard.** A configuration pointing at a bucket that does not exist can be turned into a working path by an attacker who creates it.

---

## Access Formats

Two URL styles:

```
https://my-bucket.s3.us-east-1.amazonaws.com/path/to/key   # virtual-hosted (current)
https://s3.us-east-1.amazonaws.com/my-bucket/path/to/key   # path-style (deprecated)
```

*Virtual-hosted style is the standard; path-style is deprecated for new buckets. This is why period-containing names cause certificate problems.*

Including the region in the endpoint avoids a redirect and is slightly faster.

---

## Key Takeaways

- Bucket names are globally unique across all AWS accounts, while bucket data remains in one region.
- Names are 3–63 characters, lowercase, and cannot be changed after creation.
- Avoid periods, which break HTTPS certificate matching for virtual-hosted URLs.
- Use an organization prefix plus purpose, environment, and region, or derive names from account and region in templates.
- Names are discoverable and routinely enumerated, so they are not a security control and should not reveal sensitive information.
- Deleting a bucket releases its name for anyone to claim, which is a risk when the name is still referenced anywhere.
