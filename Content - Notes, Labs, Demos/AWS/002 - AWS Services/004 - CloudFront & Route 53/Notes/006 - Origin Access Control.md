# Origin Access Control

**Origin Access Control (OAC)** lets CloudFront read from a private S3 bucket. It is how content is served publicly while the bucket itself remains completely closed.

---

## The Problem

To serve S3 content through CloudFront, CloudFront must be able to read the bucket. The obvious approach — making the bucket public — creates two problems:

**The bucket can be accessed directly**, bypassing CloudFront entirely. That means bypassing the cache (so full S3 request and transfer charges), bypassing WAF rules, and bypassing any signed-URL restriction. Anyone who discovers the bucket name reaches the objects without going through anything.

**A public bucket is a standing risk.** Block Public Access must be disabled, and the bucket becomes one policy mistake away from exposing more than intended.

OAC solves both: the bucket stays private, and only CloudFront can read it.

---

## How It Works

CloudFront signs its requests to S3 using SigV4 with an identity representing the distribution. A bucket policy grants access to that specific distribution.

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "AllowCloudFrontServicePrincipal",
    "Effect": "Allow",
    "Principal": { "Service": "cloudfront.amazonaws.com" },
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::my-site/*",
    "Condition": {
      "StringEquals": {
        "AWS:SourceArn": "arn:aws:cloudfront::123456789012:distribution/E1ABCDEF"
      }
    }
  }]
}
```

*Only CloudFront can read the bucket, and only this distribution — the `SourceArn` condition prevents another account's distribution from using the same bucket.*

With this in place, Block Public Access can be fully enabled. The bucket is private, direct access returns 403, and CloudFront serves content normally.

---

## Setting It Up

```bash
aws cloudfront create-origin-access-control --origin-access-control-config '{
  "Name": "my-site-oac",
  "OriginAccessControlOriginType": "s3",
  "SigningBehavior": "always",
  "SigningProtocol": "sigv4"
}'
```

*Creates the OAC, which is then attached to the distribution's S3 origin. The console offers to generate the matching bucket policy.*

Three things must line up:

1. The OAC is created and attached to the origin.
2. The bucket policy allows the CloudFront service principal with the `SourceArn` condition.
3. The origin is configured as the **S3 REST endpoint**, not the website endpoint.

That last point is the usual mistake. OAC does not work with the S3 website endpoint, because that endpoint requires a public bucket. Using the REST endpoint means losing S3's index document behavior — a request for `/about/` will not automatically serve `/about/index.html`. A CloudFront Function or a default root object handles this instead.

---

## OAC vs OAI

**Origin Access Identity (OAI)** is the older mechanism. OAC replaces it and should be used for new distributions.

OAC supports SSE-KMS encrypted buckets, all AWS regions, and dynamic requests (`PUT`, `POST`); OAI supports none of those. The KMS support is the practical differentiator — an OAI cannot read a KMS-encrypted object at all.

Existing OAI configurations continue to work, and migration is straightforward.

---

## Beyond S3

OAC now supports other origin types, including Lambda function URLs and MediaStore. The same principle applies: the origin is not publicly reachable, and only CloudFront can call it.

For ALB origins, the equivalent protection is a **custom header** — CloudFront adds a secret header, and the ALB has a rule rejecting requests without it. This is weaker than OAC's cryptographic signing (it is a shared secret) and is currently the standard approach for preventing direct access to an ALB behind CloudFront. Restricting the ALB's security group to CloudFront's published prefix list adds a second layer.

---

## Signed URLs and Cookies

A related mechanism for restricting access to *viewers* rather than to the origin. **CloudFront signed URLs** and **signed cookies** limit access to content by time, IP range, or other conditions — the CloudFront equivalent of S3 presigned URLs, used for paid content, private media, and time-limited downloads.

The difference: OAC controls whether CloudFront can read the origin; signed URLs control whether a viewer can read from CloudFront. They address different halves of the path and are often used together.

---

## Key Takeaways

- OAC lets CloudFront read a private S3 bucket, so Block Public Access can stay fully enabled.
- A public bucket allows direct access that bypasses the cache, WAF, and any signed-URL restriction.
- The bucket policy grants the CloudFront service principal with an `AWS:SourceArn` condition naming the distribution.
- The origin must be the S3 REST endpoint, not the website endpoint, so index-document behavior needs handling in CloudFront.
- OAC replaces OAI and is required for SSE-KMS encrypted buckets.
- For ALB origins, use a secret custom header plus CloudFront's prefix list in the security group.
- Signed URLs and cookies restrict viewer access to CloudFront, which is a separate concern from OAC.
