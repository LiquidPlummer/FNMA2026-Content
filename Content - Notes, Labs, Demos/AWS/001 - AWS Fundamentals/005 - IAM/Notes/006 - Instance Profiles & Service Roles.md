# Instance Profiles & Service Roles

The problem this solves: an EC2 instance needs to read from S3, but an instance cannot type a password. Instance profiles and service roles are how a resource gets credentials without anyone storing a secret on it.

---

## Instance Profiles

An **instance profile** is a container holding exactly one role, attached to an EC2 instance. The console creates it invisibly whenever a role is attached, so most people never encounter the term until they use the API or CloudFormation and discover the extra object.

The chain looks like this:

```
EC2 instance
   └── instance profile
          └── IAM role
                 ├── trust policy: Principal = ec2.amazonaws.com
                 └── permissions policy: allow s3:GetObject on my-bucket
```

*The trust policy must name the EC2 service, or the role cannot be attached at all.*

### How the credentials arrive

The instance retrieves credentials from the **Instance Metadata Service (IMDS)**, a link-local HTTP endpoint at `169.254.169.254` reachable only from the instance itself:

```bash
# IMDSv2: get a token, then use it to read the role's credentials
TOKEN=$(curl -sX PUT "http://169.254.169.254/latest/api/token" \
  -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
curl -s -H "X-aws-ec2-metadata-token: $TOKEN" \
  http://169.254.169.254/latest/meta-data/iam/security-credentials/
```

*This is what every AWS SDK does automatically; we rarely call it by hand, but seeing it explains where the credentials come from.*

The credentials are temporary and **rotated automatically** by AWS, several times a day, well before expiry. Every AWS SDK and the CLI find them without configuration — which is why an application running on a correctly configured instance needs no credential setup at all.

**IMDSv2** requires a session token obtained by `PUT` before metadata can be read. This exists because IMDSv1's plain `GET` was reachable through server-side request forgery — an attacker who could make the application fetch an arbitrary URL could fetch the instance's credentials. IMDSv2 should be enforced on new instances.

---

## Service Roles

The same mechanism, generalized. A **service role** lets an AWS service act on our behalf, and each service has its own trust principal:

| Service | Trust principal | Typical purpose |
|---|---|---|
| Lambda | `lambda.amazonaws.com` | Write logs, read a table, call another service |
| ECS tasks | `ecs-tasks.amazonaws.com` | Application permissions for a container |
| CodeBuild | `codebuild.amazonaws.com` | Pull source, push artifacts |
| CloudFormation | `cloudformation.amazonaws.com` | Create the resources in a template |
| RDS enhanced monitoring | `monitoring.rds.amazonaws.com` | Publish metrics |

Lambda's **execution role** is the most frequently met. Without one granting `logs:CreateLogStream` and `logs:PutLogEvents`, a function runs but produces no logs — a confusing failure, because the function itself works.

---

## The Confused Deputy Problem

When a service assumes a role on our behalf, it could in principle be tricked into using that role for someone else's resources. AWS guards against this with condition keys in the trust policy:

```json
{
  "Effect": "Allow",
  "Principal": { "Service": "s3.amazonaws.com" },
  "Action": "sts:AssumeRole",
  "Condition": {
    "StringEquals": { "aws:SourceAccount": "123456789012" },
    "ArnLike": { "aws:SourceArn": "arn:aws:s3:::my-bucket" }
  }
}
```

*The service may assume this role only when acting for our account and that specific bucket, so the role cannot be used on another customer's behalf.*

`aws:SourceAccount` and `aws:SourceArn` belong in any trust policy whose principal is an AWS service. AWS-generated trust policies increasingly include them by default.

---

## What This Replaces

The pattern being eliminated is: generate an access key, put it in a config file on the server, and hope. That approach leaks through backups, logs, images, and repositories, and requires a rotation process nobody enjoys running.

Instance profiles and service roles remove it. The practical rule follows: **an EC2 instance, container, or Lambda function should never have an access key stored on it.** If one is there, either the role is missing or the role's permissions are wrong — and the fix is the role, not the key.

---

## Key Takeaways

- An instance profile is a container for one role, attached to an EC2 instance so it can call AWS without stored credentials.
- Credentials are delivered through the Instance Metadata Service at `169.254.169.254` and rotated automatically; SDKs find them with no configuration.
- Enforce IMDSv2, which requires a token and closes the SSRF path that exposed IMDSv1 credentials.
- Service roles apply the same mechanism to Lambda, ECS, CodeBuild, CloudFormation, and others, each with its own trust principal.
- A Lambda function without log permissions in its execution role runs but produces no logs.
- Use `aws:SourceAccount` and `aws:SourceArn` conditions in service trust policies to prevent confused-deputy misuse.
- Stored access keys on AWS compute are a sign of a missing or misconfigured role.
