# The Problem With Credentials

Every application needs credentials it did not generate — a database password, a third-party API key, a signing secret. They have to exist somewhere, and most of the obvious places are wrong.

---

## Where They Must Not Go

**In source code.** The most direct failure. Once committed, a secret is in the repository history permanently — removing it from the current version does not remove it from git history, and rewriting history does not remove it from anyone's existing clone. Public repositories are scanned continuously, and secrets committed to them are found and used within minutes.

**In configuration files in the repository.** The same problem with an extra step. `config.prod.json` is still in the repository.

**In environment variables.** This one is contested, so it is worth being specific about the failure modes:

- Environment variables appear in process listings on some systems.
- They are inherited by every child process, including anything the application shells out to.
- Crash handlers and error reporters frequently capture the full environment and send it to a third party.
- On Lambda, environment variables are visible to anyone with `lambda:GetFunctionConfiguration`.
- They appear in container definitions, CloudFormation templates, and CI logs.
- Changing one requires redeploying.

Environment variables are better than source code and worse than a secrets service.

**In an AMI or container image.** Baked in, distributed to everyone who can pull the image, and present in every layer of the image history.

**In user data.** Readable from instance metadata by anything on the instance, and by anyone with `ec2:DescribeInstanceAttribute`.

---

## What a Secrets Service Provides

**Storage outside the artifact.** The secret is not in the code, the image, or the template. The deployed artifact contains a *reference* to the secret, not the secret.

**Access control.** Reading a secret requires an IAM permission, so access is granted and revoked centrally.

**Auditing.** Every retrieval is a CloudTrail event, so "who read this secret" is answerable.

**Rotation.** The value can change without redeploying anything, because the application fetches the current value at runtime.

**Encryption at rest** with a KMS key, so decryption is a separate permission from reading.

The pattern that follows:

```
Application ──IAM──► Secrets Manager ──KMS──► plaintext secret
     │
     └── holds only the secret's NAME, never its value
```

*The deployed application carries an identifier. The value is fetched at runtime using the role it already has.*

---

## The Two Services

AWS provides two, with overlapping capabilities:

**Parameter Store** (part of Systems Manager) stores configuration and secrets in a hierarchical namespace. Standard parameters are free.

**Secrets Manager** is purpose-built for secrets, with built-in rotation and native integration with RDS and other services. It costs roughly $0.40 per secret per month plus API charges.

Choosing between them is the subject of a later note. The important point is that either is dramatically better than the alternatives above.

---

## What Still Has to Be Right

A secrets service does not solve everything:

**Something must be able to read the secret.** That something is an IAM role, and its permissions must be scoped — a role that can read every secret in the account has undone much of the benefit.

**The secret still reaches the application's memory**, so it can still be logged, included in an error message, or exposed by a debug endpoint. That failure mode is covered separately.

**A secret is only as good as its rotation.** A credential never rotated since 2019, stored perfectly, is still a credential that has been valid for years.

**The best secret is no secret.** Where IAM authentication is possible — RDS IAM auth, IAM roles for AWS services, OIDC federation for CI — there is no credential to store, leak, or rotate at all. That is strictly better than storing one well, and it is worth checking for before reaching for a secrets store.

---

## Key Takeaways

- Secrets in source code persist in git history and in every existing clone, and public repositories are scanned continuously.
- Environment variables leak through process listings, child processes, crash reporters, and API calls that read function configuration.
- Baking secrets into images or user data distributes them to everyone with access to the artifact.
- A secrets service keeps the value outside the artifact, gates access through IAM, audits retrieval, and allows rotation without redeployment.
- Deployed applications should hold a secret's name, fetching the value at runtime with their existing role.
- Scope read permissions per secret; a role able to read everything defeats much of the purpose.
- Prefer IAM authentication where it exists — no stored credential is better than a well-stored one.
