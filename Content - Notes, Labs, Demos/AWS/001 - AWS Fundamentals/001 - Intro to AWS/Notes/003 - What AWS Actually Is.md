# What AWS Actually Is

Structurally, AWS is not one product. It is a very large number of independent services that happen to share three things: an account, an API, and a permissions system. Most of what is confusing about AWS follows from that shape, so it is worth understanding before we touch any individual service.

---

## A Large Number of Independent Services

There are over two hundred AWS services. They were built by different teams, at different times, with different design philosophies, and they are not consistent with one another. Two services may use different words for the same idea, or the same word for different ideas. S3 calls its container a **bucket**, DynamoDB calls its container a **table**, SQS calls its container a **queue** — and none of the three behave alike.

This is not a flaw to work around; it is the actual structure. AWS is best understood as a catalog of separately designed tools rather than a platform with one unifying model. The practical consequence is that knowing one service transfers less than we would like to the next one. What does transfer is the three things they share.

---

## The Three Things They Share

### 1. The account

Every resource we create belongs to exactly one AWS **account**. The account is the boundary for billing and the primary boundary for isolation — resources in one account cannot see resources in another unless someone deliberately connects them. When we say "our environment," we usually mean an account.

### 2. The API

Every service exposes an HTTPS API, and *every* action is an API call. Clicking a button in the web console issues an API call. Running an AWS CLI command issues an API call. An application using an AWS SDK issues an API call. There is no privileged back channel — the console is just another client.

```
Console  ─┐
CLI      ─┼──►  HTTPS API  ──►  Service (EC2, S3, IAM, …)
SDK      ─┘
```

*The three ways we interact with AWS are three clients for one API; none of them can do anything the others cannot.*

This is the single most useful structural fact about AWS. It means anything done by hand can be scripted, anything done in the console can be found in the CLI reference, and every action is uniformly loggable.

### 3. The permissions system

**IAM** (Identity and Access Management) evaluates every one of those API calls. Each call arrives with an identity attached, and IAM decides whether that identity may perform that action on that resource. The mechanism is the same for all services — one policy language governs deleting an S3 object, launching an EC2 instance, and invoking a Lambda function.

Because IAM sits in front of everything, it is the one service where a gap in understanding causes problems everywhere else. That is why it appears early in this unit rather than as one service among many.

---

## Why This Framing Helps

Three habits follow from this structure:

- **Learn services individually.** Do not expect knowledge of one to predict the behavior of another. Read what a given service actually does.
- **Treat the API as the source of truth.** When the console's behavior is ambiguous, the API documentation describes what really happens.
- **Expect permissions to break first.** When something does not work, an authorization failure is a more likely explanation than a bug.

---

## Key Takeaways

- AWS is a catalog of independently designed services, not a uniform platform; terminology and behavior differ between them.
- All services share an account (the billing and isolation boundary), an API (every action is an API call), and IAM (which authorizes every call).
- The console, CLI, and SDKs are three clients for the same API, with no capability differences between them.
- Because IAM governs every service, it is foundational, and this unit covers it early.
