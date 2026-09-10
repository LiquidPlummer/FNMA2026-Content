# The Root Account & MFA

Every AWS account has a root user — the email address the account was created with. It has permissions no policy can restrict, and handling it correctly is a short, fixed checklist.

---

## What Root Is

The **root user** is the account owner, identified by the sign-up email address and password. It differs from every other identity in one way that matters: **its permissions cannot be limited by IAM policies.** There is no policy to attach that restricts it, and it cannot be denied by an identity-based policy.

Only Service Control Policies, from an AWS Organization, can constrain root — and only for member accounts, not the organization's management account.

Root can therefore do things no IAM user can, including deleting the account, and its credentials being compromised means the account is compromised entirely.

---

## The Handful of Tasks That Require Root

Root is genuinely required for a small, stable list:

- Changing the account name, root email address, or root password
- Changing or removing the payment method and viewing certain billing settings
- Closing the account
- Restoring IAM user permissions after someone has locked everyone out
- Registering as a seller in the Reseller Program
- Signing up for GovCloud
- Certain S3 operations, such as removing an object-lock legal hold placed by root

That is close to the whole list, and none of them are routine. Everything else — creating resources, managing IAM, deploying, debugging — should be done by an IAM identity or an assumed role.

---

## Securing Root

A fixed checklist, done immediately on account creation:

**1. Enable MFA on root.** A hardware key or an authenticator app. This is the single most important control on an AWS account. Root with only a password is one phishing email away from total compromise.

**2. Delete root access keys.** Root should have none. If any exist, delete them — there is no legitimate reason for programmatic root access, and a root access key is the worst possible credential to leak.

**3. Use a strong, unique password stored in a password manager.** It should not resemble any other password anyone uses.

**4. Use a distribution list for the email address**, not an individual's mailbox. When that person leaves, the account should not become unrecoverable. Ensure the list is monitored and its members are controlled.

**5. Create IAM identities for actual work**, and stop signing in as root.

**6. Alarm on root usage.** Root sign-in should be rare enough that every occurrence is worth investigating.

```json
{
  "source": ["aws.signin"],
  "detail-type": ["AWS Console Sign In via CloudTrail"],
  "detail": { "userIdentity": { "type": ["Root"] } }
}
```

*An EventBridge pattern matching root console sign-ins; routed to SNS it produces an alert on every root use.*

---

## MFA More Broadly

Multi-factor authentication should not stop at root:

- **Every human with console access** should have MFA. IAM Identity Center can enforce it centrally.
- **Sensitive operations** can require MFA through a policy condition:

```json
{
  "Effect": "Deny",
  "Action": ["iam:*", "kms:ScheduleKeyDeletion", "ec2:TerminateInstances"],
  "Resource": "*",
  "Condition": {
    "BoolIfExists": { "aws:MultiFactorAuthPresent": "false" }
  }
}
```

*Denies these actions unless the session was authenticated with MFA. `BoolIfExists` matters here — plain `Bool` would not match sessions where the key is absent, such as service-linked calls.*

MFA types, roughly in order of strength: hardware security keys (FIDO2), virtual authenticator apps (TOTP), and hardware TOTP tokens. SMS is no longer supported for AWS accounts.

---

## Root Is Not a Break-Glass Account

A common mistake is treating root as an emergency administrative login. It should not be one — root is for the handful of tasks that require it, and nothing else.

For emergencies, create a dedicated break-glass IAM role with strong permissions, MFA required, alarms on every use, and credentials stored securely offline. That gives a recoverable path that is monitored, attributable, and revocable — none of which is true of root.

---

## Key Takeaways

- The root user is identified by the account's email address and cannot be restricted by IAM policies; only SCPs limit it, and not in an organization's management account.
- Only a short list of tasks requires root — account settings, billing details, account closure, and recovering from an IAM lockout.
- Enable MFA on root, delete any root access keys, use a unique password, and point the email at a monitored distribution list.
- Alarm on every root sign-in, since legitimate use should be rare.
- Require MFA for human console access and use `aws:MultiFactorAuthPresent` with `BoolIfExists` to gate sensitive actions.
- Use a dedicated, monitored break-glass role for emergencies rather than treating root as one.
