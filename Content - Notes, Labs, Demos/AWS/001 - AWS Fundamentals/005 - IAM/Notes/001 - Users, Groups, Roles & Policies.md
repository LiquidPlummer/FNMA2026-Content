# Users, Groups, Roles & Policies

IAM is built from four object types. Nearly everything about AWS authorization is a combination of these, so it is worth being precise about what each one is.

---

## The Four Things

### Users

An **IAM user** is a long-lived identity representing a person or an external system. A user can have:

- A **password**, for console sign-in.
- **Access keys** — an access key ID and secret access key — for the CLI and SDKs.

Both are long-lived credentials: they exist until someone deletes them. That property is exactly what makes users the least preferred identity type in modern AWS practice.

### Groups

A **group** is a collection of users, and exists only to attach policies to several users at once. Three constraints:

- Groups cannot be nested.
- Groups are not identities — nothing "runs as" a group, and a group cannot be granted permission to do anything by another principal.
- A user can belong to multiple groups and receives the union of their policies.

Groups are administrative convenience, nothing more.

### Roles

A **role** is an identity with permissions but **no permanent credentials**. Nobody logs in as a role. Instead, a principal *assumes* the role and receives temporary credentials that expire — typically in an hour, configurable up to twelve.

Roles are the answer to almost every "how does X get permission to do Y" question:

- How does an EC2 instance call S3? An instance profile containing a role.
- How does a Lambda function write logs? Its execution role.
- How does a user in one account access another? Cross-account role assumption.
- How does a CI pipeline deploy? A role assumed through OIDC federation.

### Policies

A **policy** is a JSON document listing permissions. Policies grant nothing on their own — they take effect when attached to a user, group, role, or resource. Three varieties:

- **AWS-managed** — written and maintained by AWS (`AmazonS3ReadOnlyAccess`). Convenient, and usually broader than needed.
- **Customer-managed** — written by us, reusable across identities. The normal choice for real work.
- **Inline** — embedded in a single identity and deleted with it. Useful for genuinely one-off permissions, awkward to audit.

---

## How They Fit Together

```
        ┌── Group "developers" ── policy: ReadOnlyAccess
        │
User ───┤
        └── inline policy: allow s3:PutObject on one bucket

Role "app-server-role" ── policy: allow dynamodb:* on one table
  ▲
  └── assumed by: EC2 instance (via instance profile)
```

*Users collect permissions from groups and attached policies; roles hold permissions that other principals borrow temporarily.*

---

## ARNs

Every AWS resource has an **Amazon Resource Name**, and policies are written in terms of them:

```
arn:aws:iam::123456789012:user/kyle
arn:aws:iam::123456789012:role/app-server-role
arn:aws:s3:::my-bucket/reports/*
arn:aws:dynamodb:us-east-1:123456789012:table/Orders
```

*The general shape is `arn:aws:<service>:<region>:<account>:<resource>`; IAM and S3 leave region blank because they are not region-scoped, and S3 also leaves the account field blank.*

Reading an ARN accurately is a prerequisite for writing policies, since the `Resource` element is a list of them.

---

## Why Roles Beat Users

The pattern worth carrying forward: **users for humans who cannot federate, roles for everything else.** A role issues credentials that expire automatically, so there is nothing to rotate and a leaked credential has a short useful life. An access key, by contrast, works until someone notices and revokes it — and leaked keys are found and used within minutes.

---

## Key Takeaways

- IAM has four object types: users, groups, roles, and policies.
- Users are long-lived identities with passwords and access keys; groups exist only to attach policies to several users at once and are not identities.
- Roles have permissions but no permanent credentials — a principal assumes one and receives temporary credentials.
- Policies are JSON documents that grant nothing until attached; they come as AWS-managed, customer-managed, or inline.
- ARNs identify resources and are what policy `Resource` elements reference.
- Prefer roles over users wherever possible, because temporary credentials expire on their own.
