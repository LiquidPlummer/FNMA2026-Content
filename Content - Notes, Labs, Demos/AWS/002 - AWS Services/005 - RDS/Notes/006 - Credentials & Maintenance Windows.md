# Credentials & Maintenance Windows

Two ongoing operational concerns: how database credentials are stored and rotated, and what AWS does to the instance on its own schedule.

---

## The Master Password

RDS requires a master password at creation. It is a real credential with real consequences, and where it goes matters.

**Where it should not go:** a CloudFormation template, an environment variable in a task definition, a config file in a repository, or a shared document. All of these persist, get copied, and appear in places nobody anticipated. A password in a CloudFormation template is visible to anyone who can describe the stack.

**Managed master user password** is the current answer. RDS generates the password, stores it in Secrets Manager, and manages rotation:

```bash
aws rds create-db-instance \
  --db-instance-identifier orders-prod \
  --engine postgres --db-instance-class db.r6g.large \
  --master-username dbadmin \
  --manage-master-user-password \
  --master-user-secret-kms-key-id alias/rds-secrets
```

*RDS creates the password, stores it in Secrets Manager, and rotates it — the password never exists anywhere we handle it.*

This removes the entire problem of the initial credential. Applications retrieve it from Secrets Manager at runtime.

---

## Application Credentials

The master user should not be the application's login. It has more privilege than any application needs, and sharing it means every application has full database access.

The pattern:

1. Connect once as the master user.
2. Create a role per application with only the privileges it needs.
3. Store that credential in Secrets Manager.
4. The application retrieves it at runtime.

```sql
CREATE ROLE orders_app WITH LOGIN PASSWORD 'placeholder';
GRANT CONNECT ON DATABASE orders TO orders_app;
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO orders_app;
```

*An application role with no DDL privileges — it cannot drop tables, which limits the damage from a bug or an injection flaw.*

Secrets Manager supports **managed rotation for RDS credentials**, which handles the two-user rotation strategy that avoids a window where the old password has been changed but the new one is not yet deployed. This is covered in the Secrets Manager lesson.

**IAM database authentication** is the alternative that removes passwords entirely, with the connection-rate constraints noted previously.

---

## Maintenance Windows

RDS applies patches during a weekly **maintenance window** — a 30-minute period we choose. Choosing it deliberately matters, because the default is assigned arbitrarily and may fall during peak hours.

What happens during a maintenance window:

**Minor version upgrades**, if `AutoMinorVersionUpgrade` is enabled.

**OS patches**, some of which require a reboot.

**Hardware maintenance**, occasionally.

Some maintenance requires downtime. **With Multi-AZ, RDS patches the standby, fails over, and patches the former primary** — so downtime is reduced to the failover time rather than the full patch duration. This is a real operational benefit of Multi-AZ beyond failure protection, and it is often underweighted.

Pending maintenance is visible before it is applied:

```bash
aws rds describe-pending-maintenance-actions
```

*Lists what is scheduled and when. Some actions can be applied immediately rather than waiting for the window, which is useful when a controlled time is preferable to an automatic one.*

---

## The Backup Window

Separate from maintenance: a daily window when the automated snapshot is taken. On single-AZ instances this causes brief I/O suspension; on Multi-AZ it is taken from the standby with no primary impact.

The backup window should not overlap the maintenance window, and both should sit in a low-traffic period.

---

## Forced Upgrades

The consequence of handing over patching, stated plainly.

AWS publishes end-of-support dates for engine versions. When a version reaches that date, **AWS upgrades the instance automatically** during a maintenance window. This is not optional and not indefinitely deferrable.

Major version upgrades can involve extended downtime and behavior changes — a query planner that chooses differently, a deprecated function, a changed default. Having that happen automatically, unrehearsed, during a maintenance window is a poor way to encounter it.

The practical response:

**Track the deprecation schedule** for the engine version in use.

**Upgrade on our own timetable**, having rehearsed on a restored snapshot.

**Use a blue/green deployment** for major upgrades where downtime matters. RDS Blue/Green Deployments create a synchronized copy of the environment, allow the upgrade to be applied and tested there, and switch over in about a minute.

---

## Key Takeaways

- Use the managed master user password so RDS generates and rotates it in Secrets Manager, and it never passes through our hands.
- Never put database passwords in templates, environment variables, or repositories.
- Create a per-application role with minimal privileges rather than sharing the master user.
- Choose the maintenance window deliberately, since the default may fall during peak hours.
- With Multi-AZ, patching happens on the standby followed by a failover, reducing downtime substantially.
- Keep the backup window separate from the maintenance window, both in low-traffic periods.
- AWS force-upgrades engine versions at end of support, so track deprecation dates and upgrade on a rehearsed schedule.
- RDS Blue/Green Deployments allow a major upgrade to be tested on a synchronized copy before a short switchover.
