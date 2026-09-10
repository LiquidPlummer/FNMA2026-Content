# How an Authorization Decision Is Made

Every AWS API call is evaluated the same way. The algorithm is short, and knowing it turns "why is this denied?" into a checklist rather than guesswork.

---

## The Algorithm

For each request, IAM gathers every applicable policy and evaluates them together:

1. **Start from an implicit deny.** Nothing is permitted by default.
2. **Evaluate all explicit denies.** If any applicable policy has a matching `Deny`, the request is denied. Evaluation ends here.
3. **Evaluate Service Control Policies.** If an SCP applies and does not allow the action, deny.
4. **Evaluate permissions boundaries.** If one applies and does not allow the action, deny.
5. **Evaluate session policies.** If one applies and does not allow the action, deny.
6. **Evaluate identity-based and resource-based policies.** If either allows, allow.
7. **Otherwise, deny.**

```
             ┌─────────────────────┐
Request ───► │ Any explicit Deny?  │──yes──► DENY
             └──────────┬──────────┘
                        no
                        ▼
             ┌─────────────────────┐
             │ SCP / boundary /    │──no───► DENY
             │ session allow it?   │
             └──────────┬──────────┘
                       yes
                        ▼
             ┌─────────────────────┐
             │ Identity or         │──no───► DENY (implicit)
             │ resource policy     │
             │ allows it?          │
             └──────────┬──────────┘
                       yes
                        ▼
                      ALLOW
```

*The two rules that carry the most weight: an explicit deny ends evaluation immediately, and an absent allow is a denial.*

---

## The Two Rules That Matter

**Explicit deny always wins.** No allow overrides it. Not an administrator policy, not a resource policy, not `"Action": "*"`. If any evaluated policy denies the action, it is denied. This makes `Deny` a reliable guardrail — an SCP denying `s3:DeleteBucket` cannot be worked around by anyone in the account, including the root user.

**Implicit deny is the default.** An action nobody mentioned is denied. Permissions are additive from a base of nothing, so a policy never needs to deny things it simply does not grant.

---

## The Restricting Layers

Three mechanisms cap permissions rather than granting them. None of them ever grants anything.

**Service Control Policies** apply to accounts in an AWS Organization and set a ceiling for every identity in the account, including root. Common use: denying regions the organization does not operate in, or preventing anyone from disabling CloudTrail.

**Permissions boundaries** attach to a user or role and cap what its policies can achieve. The effective permission is the intersection of the identity's policies and its boundary. The usual use is delegation — letting a team create roles for itself without letting it create a role more powerful than it holds.

**Session policies** are passed when assuming a role, narrowing that specific session below the role's own permissions.

Together they mean the effective permission set is:

```
identity policies ∩ boundary ∩ SCP ∩ session policy, minus every explicit Deny
```

*Each additional layer can only subtract; permissions never exceed the most restrictive layer.*

---

## Diagnosing a Denial

A denial message names the principal, the action, and often the reason:

```
User: arn:aws:iam::123456789012:user/kyle is not authorized to perform:
s3:GetObject on resource: arn:aws:s3:::company-reports/q3.pdf
because no identity-based policy allows the s3:GetObject action
```

*The trailing clause is the useful part — "no identity-based policy allows" means implicit deny, while "with an explicit deny" points at a specific denying statement.*

Work the list in order:

1. **Is the identity what we think?** Run `aws sts get-caller-identity`.
2. **Is the action name right?** `s3:GetObject` is not `s3:GetObjects`.
3. **Is the resource ARN right?** Bucket versus `bucket/*` is the usual S3 error.
4. **Is there an explicit deny?** Check SCPs and boundaries, not only the attached policies.
5. **Does a condition fail?** Source IP, MFA, and encryption conditions deny silently when unmet.
6. **Is a second permission needed?** Reading a KMS-encrypted object requires `kms:Decrypt` in addition to `s3:GetObject`.

The **IAM Policy Simulator** evaluates a hypothetical call against real policies without making it, and `--dry-run` does the same for many EC2 operations.

---

## Key Takeaways

- Evaluation starts from an implicit deny; permissions are purely additive from nothing.
- An explicit `Deny` anywhere ends evaluation and cannot be overridden by any allow.
- SCPs, permissions boundaries, and session policies only restrict — they never grant.
- Effective permissions are the intersection of all layers, minus every explicit deny.
- Denial messages distinguish "no policy allows" (implicit) from "an explicit deny," which points at different fixes.
- Check identity, action spelling, resource ARN, explicit denies, unmet conditions, and secondary permissions such as `kms:Decrypt`.
