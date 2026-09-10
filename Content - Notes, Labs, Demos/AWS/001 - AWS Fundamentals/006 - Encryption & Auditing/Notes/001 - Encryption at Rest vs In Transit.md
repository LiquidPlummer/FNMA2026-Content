# Encryption at Rest vs In Transit

Two different protections, against two different threats, using two different mechanisms. Conflating them leads to systems that are encrypted in the sense that satisfies a checklist and not in the sense that helps.

---

## Encryption at Rest

Data is encrypted when written to storage and decrypted when read. It protects against someone obtaining the **storage medium or the stored bytes**: a stolen disk, a decommissioned drive, a copied snapshot, a leaked backup file.

What it does *not* protect against is the far more common case: someone with valid credentials and permission to read the data. If an IAM principal can call `s3:GetObject`, S3 decrypts the object and hands it over. Encryption at rest is invisible to an authorized caller — which is exactly the point, and also its limit.

This is worth stating plainly because "the bucket is encrypted" is often offered as an answer to "could this data leak?" It is not. Encryption at rest does nothing against a permissions failure, and permissions failures are how data actually leaks.

Where it does earn its place: compliance requirements almost universally mandate it, physical media handling is genuinely a risk at data center scale, and snapshots and backups get copied to places nobody anticipated.

---

## Encryption in Transit

Data is encrypted while moving over a network, via **TLS**. It protects against someone **observing or modifying traffic in flight**: a compromised network segment, a machine-in-the-middle, or traffic traversing an untrusted network.

The failure modes are different from at-rest, and more subtle:

- An endpoint that accepts both HTTP and HTTPS will be used over HTTP by something, eventually.
- A client that does not verify certificates is vulnerable to interception, and disabling verification is a common "fix" for a certificate error.
- Traffic inside a VPC is not automatically encrypted. Private does not mean encrypted.

Most AWS service endpoints are HTTPS-only, so calls to the AWS APIs are encrypted by default. Traffic between our own components — application to database, service to service — is our responsibility.

---

## The Comparison

| | At rest | In transit |
|---|---|---|
| Protects against | Physical media access, copied storage | Network observation and tampering |
| Mechanism | KMS keys, service-side encryption | TLS |
| Useless against | Authorized credentials reading the data | Anything once the data has landed |
| AWS default | Varies by service; increasingly on | Usually on for AWS API endpoints |
| Our job | Enabling it, choosing the key | Enforcing it, verifying certificates |

Neither substitutes for the other, and neither substitutes for access control.

---

## Enforcing In-Transit Encryption

TLS availability is not TLS usage. Making it mandatory requires a policy:

```json
{
  "Effect": "Deny",
  "Principal": "*",
  "Action": "s3:*",
  "Resource": [
    "arn:aws:s3:::company-reports",
    "arn:aws:s3:::company-reports/*"
  ],
  "Condition": {
    "Bool": { "aws:SecureTransport": "false" }
  }
}
```

*A bucket policy denying every non-HTTPS request. Because explicit deny always wins, no other policy can permit plaintext access to this bucket.*

Equivalent controls exist elsewhere: RDS parameter groups can require SSL connections, ALB listeners can redirect HTTP to HTTPS, and API Gateway is HTTPS-only.

---

## Where They Meet

A complete path needs both, plus access control at each hop:

```
Browser ──TLS──► CloudFront ──TLS──► ALB ──TLS──► EC2 ──TLS──► RDS
                                                              │
                                                        encrypted at rest
                                                        (EBS + snapshots)
```

*Every network hop encrypted in transit, storage encrypted at rest, and IAM or database credentials governing who may traverse the path at all.*

A frequent gap is the last hop. Traffic from an application to its database inside a VPC is often plaintext, on the reasoning that the VPC is private. It is private in the sense that it is not routable from the internet, which is not the same as unobservable.

---

## Key Takeaways

- Encryption at rest protects stored bytes from physical or copied-media access and does nothing against an authorized caller.
- Encryption in transit protects data from network observation and tampering, using TLS.
- Neither protects against a permissions failure, which is how data most often leaks.
- Private network traffic inside a VPC is not encrypted unless we encrypt it.
- Availability of TLS is not enforcement; use `aws:SecureTransport` denies, RDS SSL requirements, and HTTPS redirects.
- The application-to-database hop is the one most often left in plaintext.
