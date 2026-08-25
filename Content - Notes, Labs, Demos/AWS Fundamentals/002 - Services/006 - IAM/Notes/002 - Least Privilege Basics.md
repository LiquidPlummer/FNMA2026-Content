# Least Privilege Basics

Understanding IAM's mechanics — users, roles, and policies — is only useful if we also understand the principle that should guide how we actually write those policies. That principle is **least privilege**, and it's one of the most important ideas in all of cloud security, not just AWS specifically.

## What Least Privilege Means

**Least privilege** means granting an identity (a user, a role, an application) only the exact permissions it needs to do its job — nothing more. If an application only ever needs to read files from one specific S3 bucket, its policy should grant *exactly* that: read access to that one bucket. Not write access it doesn't use. Not access to other buckets it never touches. Not broad account-wide permissions "just in case."

This sounds obvious stated plainly, but it's routinely violated in practice, usually for a very human reason: it's *easier* to grant broad permissions upfront than to figure out precisely which narrow permissions something actually needs.

## Why It's So Commonly Violated

Least privilege takes more upfront effort than the alternative. Granting broad, generous permissions means something almost certainly "just works" the first time, with no permission errors to debug. Scoping permissions down tightly requires actually understanding what an application does, testing it, and iterating on the policy — that's real work, and it's tempting to skip when a deadline is close and a wide-open policy would make the error messages go away.

The problem is that this trade-off doesn't disappear — it just gets deferred, and it gets *worse* the longer it's deferred, because more things end up depending on the overly broad permissions, making them harder to safely tighten later.

## Why It Matters: Blast Radius

The real value of least privilege becomes obvious the moment something goes wrong — a credential leaks, an application has a vulnerability that lets an attacker execute commands, a role gets assumed by something it shouldn't. In that moment, the *only* thing limiting the damage is what that specific identity was actually permitted to do.

An identity with narrowly scoped permissions has a small **blast radius** — if compromised, the damage is contained to whatever that narrow policy allowed. An identity with broad, "just in case" permissions has a large blast radius — a single compromised credential could mean an attacker can read every bucket in the account, spin up expensive resources, or worse, none of which the compromised application ever actually needed to do in the first place.

## Practical Habits

A few concrete habits put least privilege into practice:

- **Start narrow, then expand only as needed** — rather than starting broad and hoping to tighten later (which, as noted above, rarely actually happens), start with a minimal policy and add specific permissions as real, observed needs arise.
- **Scope to specific resources, not entire services** — a policy granting access to one named S3 bucket is meaningfully safer than one granting access to "all S3 buckets in the account."
- **Prefer roles over long-term user credentials** for anything an application needs, as covered in the previous topic — this limits *how long* a leaked credential remains useful, on top of limiting *what* it can do.
- **Periodically review permissions**, since access that was needed once can easily become access nobody remembers granting, or needing, months later.

## The Takeaway

Least privilege isn't a one-time setup task — it's an ongoing discipline that trades a small amount of upfront and recurring effort for a dramatically smaller worst-case outcome when something inevitably goes wrong. Given that IAM governs access to literally everything else in an AWS account, getting this discipline right is arguably one of the highest-leverage security practices available.
