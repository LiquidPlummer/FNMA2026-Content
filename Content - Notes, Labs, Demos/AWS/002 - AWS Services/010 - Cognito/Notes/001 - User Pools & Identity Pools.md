# User Pools & Identity Pools

Cognito is two services under one name, and confusing them is the single biggest obstacle to understanding it.

**A user pool authenticates people. An identity pool hands out AWS credentials.**

---

## User Pools

A **user pool** is a user directory. It handles sign-up, sign-in, password storage, multi-factor authentication, password reset, and email or phone verification, and it issues **JWTs** on successful authentication.

```
User ──credentials──► User pool ──► ID token, access token, refresh token
```

*The output is tokens. The application validates them, or API Gateway does, and uses the claims to identify the user.*

This is the half most applications need. A web or mobile application with its own users and its own API needs authentication and tokens — and nothing else Cognito offers.

---

## Identity Pools

An **identity pool** (also called Cognito Federated Identities) exchanges a proof of identity for **temporary AWS credentials**:

```
Token (from a user pool, Google, Apple, SAML…)
      │
      ▼
Identity pool ──assumes an IAM role──► temporary AWS credentials
      │
      ▼
Client calls AWS services directly — S3, DynamoDB, …
```

*The output is AWS credentials, letting a browser or mobile app call AWS APIs directly with no backend in the path.*

Identity pools also support **unauthenticated identities**, giving limited AWS access to users who have not signed in — useful for anonymous read access to specific resources.

---

## Which One Is Needed

The question that resolves it: **does the client need to call AWS services directly?**

**Usually not.** The typical architecture is:

```
Browser ──JWT──► API Gateway ──► Lambda ──IAM role──► DynamoDB
```

*The Lambda function's execution role provides AWS access. The browser never holds AWS credentials, and no identity pool is involved.*

**Sometimes yes.** A mobile app uploading large files directly to S3 avoids routing gigabytes through an API. There, an identity pool provides scoped credentials to the client.

| Need | Service |
|---|---|
| Sign-up and sign-in | User pool |
| Tokens for an API | User pool |
| Social or enterprise login | User pool (federation) |
| Client calls AWS services directly | Identity pool |
| Anonymous access to AWS resources | Identity pool |
| Both | Both, with the user pool as the identity pool's provider |

**Most applications need only a user pool.** Reaching for an identity pool by default adds a component and gives browser code AWS credentials — which is a larger surface than an API endpoint.

---

## Scoping Identity Pool Credentials

Where an identity pool is warranted, credentials must be scoped per user, or every user can access every user's data.

Policy variables do this:

```json
{
  "Effect": "Allow",
  "Action": ["s3:GetObject", "s3:PutObject"],
  "Resource": "arn:aws:s3:::user-uploads/${cognito-identity.amazonaws.com:sub}/*"
}
```

*Each user reaches only their own prefix, because the identity ID is substituted at evaluation time. Without this, one role grants everyone access to the whole bucket.*

**Role mapping** can also select different roles based on token claims — a user pool group determining which role is assumed, which is how tiered access is implemented.

---

## What Cognito Is Not

**It is not an authorization system.** It establishes who someone is. What they may do is our application's decision, using the claims it provides.

**It is not a general-purpose user database.** Custom attributes are limited and their types are fixed at creation. Application-specific user data belongs in DynamoDB or a relational database, keyed by the Cognito `sub`.

**It is not a session store.** Tokens carry state; server-side session storage is separate.

---

## Key Takeaways

- User pools authenticate users and issue JWTs; identity pools exchange identity proof for temporary AWS credentials.
- Most applications need only a user pool, with the backend's IAM role providing AWS access.
- Identity pools are for clients calling AWS services directly, such as a mobile app uploading to S3.
- Identity pools support unauthenticated identities for limited anonymous AWS access.
- Scope identity pool roles with `${cognito-identity.amazonaws.com:sub}` or every user can reach every user's data.
- Role mapping selects different IAM roles based on token claims or group membership.
- Cognito establishes identity, not authorization, and is not a general user database — store application data separately, keyed by `sub`.
