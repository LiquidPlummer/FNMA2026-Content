# Why Console-Only Work Doesn't Reproduce

The console is an excellent tool for looking at things and a poor one for building them. The reason is specific: a console action produces a change but no description of that change.

---

## The Missing Artifact

Configuring a VPC through the console might involve forty clicks across six screens. When it is finished, the VPC exists — and nothing anywhere records what was done. The knowledge exists only in the resulting state and in whatever the person remembers.

Compare the same work as CLI commands or a CloudFormation template. Those are files. They can be read, diffed, reviewed, stored in version control, and run again.

The console does not produce that artifact. Everything below follows from its absence.

---

## What Breaks Without It

**Reproducing the environment.** Building an identical staging environment means repeating the clicks and hoping nothing was missed. Differences between environments accumulate quietly, and the bug that only appears in production usually traces back to one of them.

**Review.** A colleague cannot review a click. There is no diff, no pull request, no record of what changed relative to what. Console changes bypass whatever review process the organization has for code, no matter how rigorous that process is.

**Knowing the current state.** The console shows what exists now, not why. Six months later, nobody knows whether an unusual security group rule is load-bearing or left over from a debugging session, and the safe assumption is always "leave it," so it stays forever.

**Recovery.** If a region becomes unavailable, or someone deletes something important, rebuilding from clicks is slow and error-prone. Rebuilding from a template is a command.

**Onboarding.** New team members have nothing to read. The system's actual configuration is not written down anywhere.

**Auditing at scale.** CloudTrail records that someone changed a security group, which is genuinely useful. But CloudTrail is an after-the-fact log, not a description of intended state — it answers "what happened" rather than "what should exist."

---

## Drift

There is a specific failure worth naming. When infrastructure is defined in code *and* someone also edits it in the console, the two disagree. This is **drift**.

Drift is worse than pure console work, because it is invisible. The template says one thing, reality says another, and the next deployment either silently reverts someone's emergency fix or fails in a confusing way. CloudFormation offers drift detection precisely because this happens constantly.

---

## A Workable Practice

Nobody uses the console for nothing. The distinction that holds up is between reading and writing:

| Use the console for | Use code for |
|---|---|
| Exploring an unfamiliar service | Anything that will exist longer than today |
| Reading state and metrics | Anything that must exist in more than one environment |
| Investigating an incident | Anything anyone else needs to understand |
| Learning what a service does | Anything requiring review |

The practical rule: **build it in the console once to learn it, then write it down before it matters.** Experimenting by clicking is a good way to understand a service. Leaving production configured that way is how a system becomes something nobody can rebuild.

When an urgent console change is unavoidable during an incident, the fix is to record it in code afterward, while it is still fresh — otherwise it becomes permanent drift by default.

---

## Key Takeaways

- A console action changes state without producing any description of the change, so there is nothing to review, diff, or replay.
- Without that artifact, environments cannot be reproduced reliably, and differences between them accumulate unnoticed.
- Console changes bypass code review entirely, however strict that process is elsewhere.
- Mixing console edits with infrastructure as code produces drift, which is invisible until the next deployment.
- Use the console to explore, read state, and investigate; use code for anything durable, replicated, or reviewed.
- After an emergency console change, write it into code promptly or it becomes permanent.
