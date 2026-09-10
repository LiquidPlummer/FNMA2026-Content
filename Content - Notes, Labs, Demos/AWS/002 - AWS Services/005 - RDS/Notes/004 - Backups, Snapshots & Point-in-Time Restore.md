# Backups, Snapshots & Point-in-Time Restore

RDS provides two distinct backup mechanisms with different lifecycles. The difference matters most at the moment an instance is deleted.

---

## Automated Backups

RDS takes a daily snapshot during a configured backup window and continuously captures transaction logs, typically every five minutes.

Together these enable **point-in-time restore (PITR)** — recovery to any second within the retention period, not just to a snapshot boundary.

**Retention is 0 to 35 days.** Setting it to 0 **disables automated backups entirely**, which also disables PITR. It is the default for some creation paths, and it is worth checking rather than assuming.

**Automated backups are deleted when the instance is deleted**, unless a final snapshot is requested. This is the critical property: the backups protecting a database disappear with the database.

Backup storage equal to the database size is included; beyond that is billed.

---

## Manual Snapshots

A manual snapshot is taken on demand and **persists until explicitly deleted**. It survives instance deletion.

```bash
aws rds create-db-snapshot \
  --db-instance-identifier orders-prod \
  --db-snapshot-identifier orders-prod-pre-upgrade-20260909
```

*A manual snapshot before a risky change. It remains after the instance is gone, which is what distinguishes it from an automated backup.*

Manual snapshots can be copied across regions and shared with other accounts — the mechanism for cross-region disaster recovery and for handing a database copy to another team.

Note the encryption interaction from the Fundamentals unit: **a snapshot encrypted with an AWS-managed key cannot be shared with another account.** Cross-account sharing requires a customer-managed key.

---

## The Comparison

| | Automated | Manual |
|---|---|---|
| Created by | RDS, daily | Us, on demand |
| Retention | 0–35 days | Until deleted |
| Survives instance deletion | **No** | Yes |
| Point-in-time restore | Yes | No — snapshot moment only |
| Cross-region copy | Via copy to manual | Yes |
| Cross-account share | No | Yes |

---

## Point-in-Time Restore

```bash
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier orders-prod \
  --target-db-instance-identifier orders-prod-restored \
  --restore-time 2026-09-09T14:20:00Z
```

*Restores to a specific second. The restore creates a **new instance** — it never overwrites the original.*

Three properties shape how PITR is used:

**It always creates a new instance.** Recovery means restoring, verifying, and then repointing the application — the original is untouched, which is safe but means recovery is not instant.

**It takes time.** Restoring a large database can take a long time, driven by size rather than by how far back the restore point is. This is why recovery time objectives need testing rather than assuming.

**The restored instance has default settings.** Parameter groups, security groups, and Multi-AZ are not carried over and must be specified or applied afterwards. A restored instance with a default security group is unreachable, which is a confusing thing to discover mid-incident.

---

## The Practical Failure Mode

The scenario that catches people:

1. Someone deletes an RDS instance with `SkipFinalSnapshot=true`.
2. Automated backups are deleted along with it.
3. There are no manual snapshots.
4. The data is unrecoverable.

Three protections, in order of value:

**Deletion protection.** The delete API call fails while it is set. It is a single flag and should be on for every production database.

**Always take a final snapshot.** Not skipping it costs a few minutes and preserves everything.

**Regular manual snapshots**, or an AWS Backup plan, so recovery does not depend on the instance's own automated backups.

**AWS Backup** is worth naming: it manages backup plans across RDS, EBS, DynamoDB, EFS, and other services, with centralized retention, cross-region copies, and vault-level access controls. For anything beyond a single database, it is a better answer than per-service backup settings — and its backups live outside the resource, so they survive its deletion.

---

## Testing Restores

An untested backup is an assumption. The things that go wrong are only visible in a rehearsal:

- The restore takes far longer than expected.
- The restored instance is unreachable because of default security groups.
- A required parameter group setting is missing.
- The application cannot connect because credentials differ.
- The backup did not include what everyone assumed it did.

Restoring to a test instance periodically converts these from incident discoveries into known quantities.

---

## Key Takeaways

- Automated backups plus transaction logs enable point-in-time restore within a 0–35 day retention window.
- Setting retention to 0 disables automated backups and PITR entirely.
- Automated backups are deleted with the instance; manual snapshots persist until explicitly deleted.
- Manual snapshots can be copied across regions and shared across accounts, but sharing requires a customer-managed KMS key.
- PITR always creates a new instance and does not carry over parameter groups, security groups, or Multi-AZ settings.
- Enable deletion protection and always take a final snapshot when deleting.
- AWS Backup centralizes backup policy across services and stores backups outside the resource.
- Rehearse restores, since duration and configuration gaps only surface when one is attempted.
