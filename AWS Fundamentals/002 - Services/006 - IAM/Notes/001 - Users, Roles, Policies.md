# Users, Roles, Policies

**IAM** (Identity and Access Management) is AWS's service for controlling *who* (or *what*) can do *what* within an AWS account. It's a global service, as mentioned back in the Global Infrastructure lesson — IAM identities and permissions apply account-wide, not per region. Every meaningful action taken in AWS, whether by a person clicking in the console or a piece of software calling an API, is checked against IAM permissions first.

## Users

An **IAM user** represents a single person or a specific application that needs long-term, direct credentials to access AWS. Each user has its own identity, its own credentials, and its own set of permissions. In a real organization, we'd typically create one IAM user per person who needs access — never share a single user's credentials between multiple people, since that destroys any ability to trace who actually took a given action.

It's worth noting upfront that IAM users are increasingly treated as something to use sparingly, in favor of roles (covered next) — a theme we'll return to.

## Roles

An **IAM role** is similar to a user in that it's an identity with permissions attached, but with one crucial difference: a role isn't tied to one specific person or credential set. Instead, a role is something that gets **assumed** — temporarily, by a user, an application, or an AWS service itself — which grants whoever assumes it the role's permissions for a limited time, without handing out long-term credentials at all.

This matters enormously in practice. A classic example: an EC2 instance running an application that needs to read from an S3 bucket. Instead of embedding long-term IAM user credentials directly into the application's configuration (a real security liability if that server or its configuration is ever compromised), we attach an IAM role to the instance. AWS handles issuing that instance short-term, automatically-rotated credentials behind the scenes, and the application simply has the permissions the role grants, with nothing sensitive to leak.

## Policies

A **policy** is a document (written in JSON) that defines *what* is actually allowed or denied — the permissions themselves. Policies are attached to users, roles, or groups, and they specify things like which actions are permitted (e.g., reading from S3, but not writing to it), on which specific resources, and under what conditions.

A simplified policy might say, in effect: "allow reading objects from this specific S3 bucket, and nothing else." Policies are what turn a user or role from "an identity that exists" into "an identity that can actually do specific things" — an identity with no policies attached can authenticate successfully but is unable to perform any action at all.

## How They Fit Together

```
IAM User/Role  ←  has attached  →  IAM Policy
   (the "who")                      (the "what's allowed")
```
*A user or role is an identity; a policy is the set of permissions granted to it.*

A user or role without any attached policy has an identity but no capabilities — being authenticated (proving who you are) doesn't mean being authorized (having permission to do something). Every real permission in AWS flows through a policy attached somewhere in this chain.

## The Broader Shift: Roles Over Long-Term Users

The IAM user model — long-term credentials, directly issued — is the older, more traditional pattern, and it's inherently riskier: a long-term credential (like an access key) that leaks or gets checked into source code by accident is a standing liability until someone notices and revokes it. Roles, with their short-term, automatically expiring credentials, avoid that entire category of risk. The direction most AWS guidance now points is toward using roles wherever an *application* needs access, and reserving individual IAM users mainly for actual human sign-in, often through a centralized identity provider rather than in each account directly. Understanding *why* that shift matters is the subject of the next topic: least privilege.
