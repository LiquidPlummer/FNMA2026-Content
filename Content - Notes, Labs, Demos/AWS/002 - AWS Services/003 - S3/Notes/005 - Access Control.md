# Access Control

S3 has more access control mechanisms than any other AWS service, largely for historical reasons. Knowing which to use — and which to ignore — prevents the misconfiguration that produces most public-bucket incidents.

---

## Block Public Access

**Start here.** Block Public Access is a set of four switches that override every other setting. When enabled, no bucket policy or ACL can make the bucket public, regardless of what it says.

```bash
aws s3api put-public-access-block --bucket company-reports \
  --public-access-block-configuration \
  "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
```

*All four settings on. This is the default for new buckets and should be left on unless there is a specific, understood reason to disable it.*

It can also be applied account-wide, which prevents anyone from making any bucket public. For organizations that never intend to serve content directly from S3, this is the single most effective control available.

**Public buckets are almost never the right answer.** Serving public content is CloudFront's job, using Origin Access Control to keep the bucket private. The bucket stays closed and CloudFront reads from it.

---

## Bucket Policies

A **bucket policy** is a resource-based policy attached to the bucket — the main mechanism for controlling S3 access.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "DenyInsecureTransport",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": [
        "arn:aws:s3:::company-reports",
        "arn:aws:s3:::company-reports/*"
      ],
      "Condition": { "Bool": { "aws:SecureTransport": "false" } }
    },
    {
      "Sid": "AllowAppRole",
      "Effect": "Allow",
      "Principal": { "AWS": "arn:aws:iam::123456789012:role/app-server-role" },
      "Action": ["s3:GetObject", "s3:PutObject"],
      "Resource": "arn:aws:s3:::company-reports/uploads/*"
    }
  ]
}
```

*Two statements: an explicit deny for non-HTTPS requests, and a scoped allow. Note the two ARN forms — the bucket for bucket-level actions, `bucket/*` for object-level ones.*

The bucket-versus-object ARN distinction is the most common S3 policy error. `s3:ListBucket` operates on the bucket ARN; `s3:GetObject` operates on `bucket/*`. A policy using the wrong one grants nothing usable.

---

## ACLs

**Access Control Lists** are the original S3 mechanism and are effectively deprecated. New buckets default to **Bucket owner enforced**, which disables ACLs entirely.

Leave them disabled. They are per-object, do not appear in the bucket policy, and are how objects end up publicly readable inside an otherwise correctly configured bucket. Any remaining use case — such as accepting objects from another account — is better handled with a bucket policy.

---

## Presigned URLs

A **presigned URL** grants temporary access to a specific object without any AWS credentials on the client:

```python
url = s3.generate_presigned_url(
    "get_object",
    Params={"Bucket": "company-reports", "Key": "2026/q3/summary.pdf"},
    ExpiresIn=3600,
)
```

*A URL anyone can use to download that object for one hour, after which it stops working.*

This is the standard way to give a browser access to private objects. The bucket stays private, the application authorizes the user, and the URL grants access to exactly one object for a limited time.

Presigned `PUT` URLs work the same way for uploads, letting a client upload directly to S3 without the bytes passing through the application.

Two constraints. A presigned URL carries the **permissions of whoever signed it**, so a URL signed by an over-privileged role grants more than intended. And it **cannot be revoked** before expiry — anyone holding the URL can use it, so expiry times should be short.

---

## VPC Endpoint Policies

For access from within a VPC, an endpoint policy restricts what can be reached through the endpoint. Combined with a bucket policy requiring the endpoint:

```json
{
  "Effect": "Deny",
  "Principal": "*",
  "Action": "s3:*",
  "Resource": "arn:aws:s3:::company-reports/*",
  "Condition": {
    "StringNotEquals": { "aws:SourceVpce": "vpce-0abc123" }
  }
}
```

*Denies access except through a specific VPC endpoint, so the bucket is unreachable from the internet even with valid credentials.*

This is a strong control for sensitive data — it means a leaked credential is not usable from outside the network.

---

## Which to Use

| Need | Mechanism |
|---|---|
| Prevent public access | Block Public Access, account-wide |
| Grant access to a role | IAM policy on the role |
| Grant cross-account access | Bucket policy plus the other account's IAM policy |
| Enforce HTTPS or encryption | Bucket policy with a `Deny` and a condition |
| Temporary browser access | Presigned URL |
| Serve public content | CloudFront with Origin Access Control |
| Restrict to a network | VPC endpoint policy plus bucket policy condition |
| Per-object permissions | ACLs — avoid; restructure instead |

---

## Key Takeaways

- Block Public Access overrides all other settings and should be enabled account-wide by default.
- Bucket policies are the main control; distinguish the bucket ARN from `bucket/*` for object actions.
- ACLs are effectively deprecated and disabled on new buckets — leave them off.
- Presigned URLs grant temporary access to one object with no client credentials, carrying the signer's permissions and no revocation.
- Serve public content through CloudFront with Origin Access Control rather than making a bucket public.
- VPC endpoint conditions can make a bucket unreachable from outside the network even with valid credentials.
- Use explicit `Deny` with conditions to enforce HTTPS and encryption, since deny cannot be overridden.
