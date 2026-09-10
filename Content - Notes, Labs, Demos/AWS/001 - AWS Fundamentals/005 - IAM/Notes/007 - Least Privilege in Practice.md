# Least Privilege in Practice

**Least privilege** means granting exactly the permissions needed and nothing more. Everyone agrees with it. Most environments do not achieve it, and the reason is structural rather than a lack of discipline.

---

## Why Starting Broad and Narrowing Rarely Happens

The usual plan is: grant broad permissions to get things working, then tighten them once the required set is known. The tightening step almost never happens, for reasons that are entirely rational in the moment.

**Nothing forces it.** Broad permissions produce no error, no alert, and no failing test. There is no signal that the work is unfinished.

**Narrowing risks breakage.** Removing a permission might break something in a code path nobody has exercised recently. The change carries real downside risk and no visible upside.

**The required set is hard to determine.** A single console action can require a dozen permissions. Some are needed only on error paths, only during scaling, or only at month-end.

**Nobody owns it.** The person who wrote the policy has moved on. The person maintaining the service does not know which permissions are load-bearing.

So `AdministratorAccess` gets attached "for now," and stays. Recognizing this pattern is the point — the fix is not more resolve, it is a different order of operations.

---

## Start Narrow Instead

Granting nothing and adding permissions as things fail inverts every incentive above. Now the feedback loop works for us: each denial names exactly the missing permission, and the resulting policy contains only what was actually needed.

```
User: arn:aws:iam::123456789012:role/report-generator is not authorized
to perform: s3:PutObject on resource: arn:aws:s3:::reports/2026-q3.pdf
```

*A denial message names the action and the resource — the exact statement to add. Iterating on these produces a minimal policy naturally.*

This is slower on the first pass and faster over the life of the system, because the alternative never converges.

---

## Tools That Help

**IAM Access Analyzer** reads CloudTrail history for a role and generates a policy containing only the actions it actually used. This is the most practical way to tighten an existing over-broad role: run it for a period covering normal operation, then compare the generated policy with the current one.

**Last-accessed data** shows, per service, when a role last used it. Services never touched are strong candidates for removal.

**The IAM Policy Simulator** tests a proposed policy against specific calls before deploying it.

**Access Analyzer findings** identify resources shared outside the account — public buckets, cross-account role trust — which catches the highest-severity mistakes.

```bash
# When did this role last use each service it has access to?
aws iam generate-service-last-accessed-details \
  --arn arn:aws:iam::123456789012:role/report-generator
```

*Returns a job ID; retrieving its results shows per-service last-used timestamps, exposing permissions that are granted but unused.*

---

## What Good Enough Looks Like

Perfect least privilege is not a realistic target for every role. A workable standard:

- **No `"Action": "*"` with `"Resource": "*"`** outside a small number of deliberately administrative roles.
- **Resources scoped to specific ARNs** wherever the resource is known — this is the highest-value constraint, because it limits blast radius even when actions are broad.
- **Separate roles per workload.** One role per application, not one shared role for everything. Shared roles accumulate the union of everyone's needs and can never be narrowed.
- **Wildcards on actions within a service** (`s3:Get*`) are acceptable when paired with a specific resource.
- **Production separated from everything else**, ideally by account.

Scoping resources tightly while allowing broader actions is usually a better trade than the reverse, and it is far easier to maintain.

---

## The Cost of Getting It Wrong

Over-broad permissions do not cause incidents by themselves — they determine how bad an incident becomes. A leaked credential for a role scoped to one bucket exposes one bucket. The same leak on an administrative role exposes the account.

Least privilege is blast-radius management. That framing makes the priority clear: the roles that most need scoping are the ones most exposed — CI/CD pipelines, internet-facing applications, and anything a developer runs locally.

---

## Key Takeaways

- Broad-then-narrow fails because nothing signals that narrowing is outstanding and narrowing carries breakage risk with no visible benefit.
- Starting from no permissions and adding on failure converges, because each denial names the exact missing permission.
- IAM Access Analyzer generates policies from actual CloudTrail usage and is the practical way to tighten existing roles.
- Service last-accessed data identifies granted-but-unused permissions.
- A realistic standard: no `*`/`*` outside deliberate admin roles, resources scoped to specific ARNs, one role per workload.
- Scoping resources tightly matters more than scoping actions, since it limits blast radius directly.
- Least privilege determines how bad an incident becomes, so prioritize the most exposed roles.
