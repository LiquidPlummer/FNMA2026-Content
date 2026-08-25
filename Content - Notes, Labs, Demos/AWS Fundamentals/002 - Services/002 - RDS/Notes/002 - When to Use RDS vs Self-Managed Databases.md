# When to Use RDS vs Self-Managed Databases

RDS isn't the only way to run a relational database on AWS — we can always install a database engine directly on an EC2 instance and manage it ourselves, sometimes called a **self-managed** database. Both are legitimate choices; the right one depends on what we're optimizing for.

## The Case for RDS

For the large majority of applications, RDS is the sensible default. It removes real, recurring operational work:

- **No OS or engine patching to schedule ourselves** — RDS applies updates on our behalf (with control over when maintenance windows occur).
- **Backups are automatic**, including point-in-time recovery, rather than something we have to build and test ourselves.
- **Multi-AZ failover is a configuration option**, not an infrastructure project — turning on high availability is a setting, not a redesign.
- **Monitoring is built in**, surfacing database health metrics without us standing up separate tooling.

For a team without deep, dedicated database administration expertise — which describes most application teams — this operational burden is exactly the kind of undifferentiated heavy lifting that's better handed to AWS, freeing the team to focus on the application itself.

## The Case for Self-Managed

Self-managing a database on EC2 gives up RDS's automation in exchange for full control, and there are legitimate reasons to want that control:

- **Needing an engine, version, or extension RDS doesn't support.** RDS supports popular engines and versions, but not every possible configuration or plugin — a workload with a hard dependency on something outside that list has no choice but to self-manage.
- **Needing OS-level access.** RDS deliberately doesn't expose the underlying operating system — if a workload genuinely needs to install custom OS-level software alongside the database, or requires filesystem-level access, self-managing is the only path.
- **Highly specialized performance tuning.** RDS exposes many tuning options, but a self-managed database offers unrestricted, low-level control over the engine and OS configuration for teams with the expertise (and the need) to use it.
- **Cost at very specific, unusual scales.** In some narrow cases, a team with strong in-house database expertise and very particular usage patterns can tune a self-managed setup more precisely than a managed offering allows — though this is the exception, not the rule, and comes at the cost of taking on all the operational work RDS would otherwise absorb.

## A Practical Framework

The decision usually comes down to one question: **does this workload need something RDS genuinely can't provide, or would self-managing just be reinventing what RDS already does well?** In practice, most workloads land in the second category — the operational features RDS provides (patching, backups, failover, monitoring) are things almost every production database needs anyway, and building them ourselves well is a nontrivial undertaking.

| Consideration | Favors RDS | Favors Self-Managed |
|---|---|---|
| Team has dedicated DBA expertise | Not required | Enables full control |
| Needs unsupported engine/version/extension | — | Required |
| Wants automated backups & failover | Built in | Must build and maintain |
| Needs OS-level access | Not available | Available |
| Operational overhead tolerance | Low | Must be high |

The default assumption, absent a specific reason otherwise, should be RDS. Self-managing is a deliberate trade-off made for a specific, identified need — not a starting point.
