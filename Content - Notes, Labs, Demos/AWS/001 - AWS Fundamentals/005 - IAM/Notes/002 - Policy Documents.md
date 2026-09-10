# Policy Documents

An IAM policy is a JSON document. Its structure is small — four elements do nearly all the work — and learning to read one fluently is the highest-value skill in IAM.

---

## The Shape

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ReadReportsBucket",
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:ListBucket"],
      "Resource": [
        "arn:aws:s3:::company-reports",
        "arn:aws:s3:::company-reports/*"
      ]
    }
  ]
}
```

*A minimal policy: read objects from one bucket and list its contents. Note that the bucket and the objects inside it are two different ARNs.*

`Version` is always `"2012-10-17"` — it is the policy language version, not a document version, and there is no newer value. `Statement` is a list, evaluated as a whole.

---

## The Four Elements

### Effect

`"Allow"` or `"Deny"`. Every statement has exactly one.

### Action

The API operations the statement covers, as `service:Operation`:

```json
"Action": "s3:GetObject"                        // one action
"Action": ["s3:GetObject", "s3:PutObject"]      // several
"Action": "s3:*"                                // all S3 actions
"Action": "s3:Get*"                             // all S3 actions starting with Get
```

*Wildcards are permitted anywhere in the action name; `"*"` alone means every action in every service.*

### Resource

Which resources the actions apply to, as ARNs. The most common mistake in S3 policies is the bucket/object distinction:

- `arn:aws:s3:::my-bucket` — the bucket itself. `ListBucket` operates on this.
- `arn:aws:s3:::my-bucket/*` — objects in the bucket. `GetObject` operates on these.

A policy granting `GetObject` on `arn:aws:s3:::my-bucket` grants nothing usable, because objects are not the bucket.

Some actions do not operate on a specific resource (`ec2:DescribeInstances` describes many), and those require `"Resource": "*"`.

### Condition

Optional constraints that must hold for the statement to apply:

```json
{
  "Effect": "Allow",
  "Action": "s3:*",
  "Resource": "arn:aws:s3:::company-reports/*",
  "Condition": {
    "IpAddress": { "aws:SourceIp": "203.0.113.0/24" },
    "Bool": { "aws:SecureTransport": "true" }
  }
}
```

*Both conditions must be satisfied: the call must come from that IP range and must use HTTPS. Multiple conditions in one block are combined with AND.*

Condition keys come in global varieties (`aws:SourceIp`, `aws:SecureTransport`, `aws:PrincipalTag`, `aws:RequestedRegion`) and service-specific ones (`s3:prefix`, `ec2:InstanceType`). Each service's documentation lists the keys it supports.

---

## Two Elements That Cause Confusion

**`NotAction` and `NotResource`** invert the match — "every action except these." They are easy to misread and usually broader than intended. `NotAction` with `Effect: Allow` grants every action in AWS except the listed ones, including services that did not exist when the policy was written. Prefer listing what is allowed.

**`Principal`** appears only in **resource-based** policies, where it specifies *who* the policy applies to. Identity-based policies have no `Principal` element, because the identity they are attached to is the principal. Seeing `Principal` in a document is a reliable signal of which kind it is.

---

## Reading a Policy Quickly

Four questions, in order:

1. Are any statements `Deny`? Those are absolute and override everything.
2. What actions does it allow — narrow, or wildcarded?
3. What resources — specific ARNs, or `"*"`?
4. Are there conditions that limit when the allow applies?

A policy with `"Action": "*"` and `"Resource": "*"` grants full administrative access to the account. It appears more often than it should, usually because a narrower policy was harder to write than to skip.

---

## Key Takeaways

- A policy is JSON with a fixed `Version` and a list of statements, each carrying `Effect`, `Action`, `Resource`, and optional `Condition`.
- Actions are `service:Operation` and support wildcards; resources are ARNs.
- In S3, the bucket and its objects are different ARNs — `ListBucket` needs the bucket, `GetObject` needs `bucket/*`.
- Conditions in a single block are ANDed together and can restrict by IP, transport, tags, region, and service-specific keys.
- `NotAction` and `NotResource` invert matching and are usually broader than intended.
- `Principal` appears only in resource-based policies, which is how to tell the two kinds apart.
