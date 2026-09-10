# Drift, Deletion Policies & Data Loss

Two ways a stack diverges from reality, and the settings that stop a stack deletion from destroying data.

---

## Drift

**Drift** is the difference between what a template declares and what actually exists. It happens when someone changes a resource outside CloudFormation — a console edit during an incident, a manual security group rule, a CLI command.

```bash
aws cloudformation detect-stack-drift --stack-name production-network

aws cloudformation describe-stack-resource-drifts \
  --stack-name production-network \
  --stack-resource-drift-status-filters MODIFIED DELETED
```

*Detection is asynchronous — the first call starts it, the second reads results once complete.*

Each resource is reported as `IN_SYNC`, `MODIFIED`, `DELETED`, or `NOT_CHECKED`, with modified resources showing expected against actual values.

---

## Why Drift Matters

**The next update may revert it.** A security group rule added manually to fix an incident is removed by the next deployment, because the template does not contain it — and the incident recurs with no obvious cause.

**It may not revert, which is worse.** CloudFormation updates only properties whose template values changed. A manually modified property the template does not change is often left alone, so drift persists silently and the template no longer describes reality.

**Rollbacks fail.** An update rolling back to a state that no longer matches reality can leave the stack in `UPDATE_ROLLBACK_FAILED`.

**The template stops being trustworthy.** Once reality and template disagree, reading the template no longer tells anyone what exists — which removes the main benefit of infrastructure as code.

---

## Handling It

**Detect it regularly.** Drift detection can be scheduled and its results alarmed on, so divergence is caught within days rather than discovered during an incident.

**Restrict console write access in production.** If people cannot change resources by hand, drift does not occur. This is the most effective control and the least popular one.

**Update the template after emergency changes.** When a console change is genuinely necessary during an incident, record it in the template promptly. The window in which everyone remembers what was changed is short.

**`NOT_CHECKED` is a real gap.** Not every resource type supports drift detection, so a clean drift report does not guarantee a clean stack.

---

## Deletion Policies

By default, deleting a stack deletes every resource in it — including databases and buckets holding data. **`DeletionPolicy`** overrides that per resource:

```yaml
Database:
  Type: AWS::RDS::DBInstance
  DeletionPolicy: Snapshot
  UpdateReplacePolicy: Snapshot
  Properties:
    DBInstanceIdentifier: orders-production

DataBucket:
  Type: AWS::S3::Bucket
  DeletionPolicy: Retain
  UpdateReplacePolicy: Retain
```

*`Snapshot` takes a final snapshot before deleting; `Retain` leaves the resource in place, outside the stack.*

Three values:

| Policy | Effect |
|---|---|
| `Delete` | Default — the resource is deleted |
| `Retain` | The resource is left in place, no longer managed |
| `Snapshot` | A final snapshot is taken, then the resource is deleted |

`Snapshot` is supported by RDS, EBS volumes, ElastiCache, Redshift, and a few others.

**`UpdateReplacePolicy` is the one people forget.** It applies when an update *replaces* a resource rather than when the stack is deleted — and replacement is the more common path to accidental data loss, because it happens during an ordinary update rather than a deliberate deletion.

**Set both on every stateful resource.** `DeletionPolicy` alone leaves the replacement case unprotected.

---

## What Retain Leaves Behind

`Retain` prevents data loss and creates a different problem: the resource still exists, still bills, and is no longer managed by anything. Over time, retained resources accumulate as untracked infrastructure nobody remembers creating.

The practical response is to tag retained resources so they are identifiable later, and to review them periodically rather than assuming a deleted stack means deleted resources.

Note also that **S3 buckets must be empty to delete**. A bucket with objects and a `Delete` policy causes the stack deletion to fail partway, leaving it in a partially deleted state.

---

## A Checklist for Stateful Resources

For any resource holding data:

1. **`DeletionPolicy: Retain` or `Snapshot`.**
2. **`UpdateReplacePolicy` set to match.**
3. **A stack policy denying `Update:Replace`** on it.
4. **Termination protection** on the stack.
5. **Service-level deletion protection** where available, such as RDS `DeletionProtection`.
6. **Change sets reviewed** before every production update, watching for `Replacement: True`.

Layered deliberately, because each catches a different path to the same outcome. Change sets catch it before deployment, stack policies block it during, and deletion policies preserve the data if it happens anyway.

---

## Key Takeaways

- Drift is divergence between the template and reality, caused by changes made outside CloudFormation.
- Drift may be silently reverted by a later update, or persist unnoticed — both undermine the template as a description of reality.
- Detect drift on a schedule, restrict production console write access, and update templates promptly after emergency changes.
- Not all resource types support drift detection, so `NOT_CHECKED` is a genuine gap.
- `DeletionPolicy` controls what happens on stack deletion: `Delete`, `Retain`, or `Snapshot`.
- `UpdateReplacePolicy` covers replacement during an update, which is the more common cause of accidental data loss.
- Set both on every stateful resource.
- Retained resources continue to bill and become untracked, so tag and review them.
- Layer change set review, stack policies, deletion policies, and service-level protection.
