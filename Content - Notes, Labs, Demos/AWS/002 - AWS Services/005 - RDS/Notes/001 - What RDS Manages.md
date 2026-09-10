# What RDS Manages

**RDS** runs a relational database and hands us an endpoint. Knowing precisely which work it takes on — and which it leaves — determines whether it fits a given workload.

---

## What RDS Does

**Provisioning.** An instance with the engine installed and configured, ready in minutes.

**Patching.** Operating system and database engine patches applied during a maintenance window.

**Backups.** Automated daily snapshots plus continuous transaction log capture, enabling point-in-time restore.

**Failover.** With Multi-AZ enabled, automatic promotion of a standby when the primary fails.

**Replication.** Read replicas created and maintained with a single API call.

**Monitoring.** CloudWatch metrics for CPU, connections, storage, IOPS, and replica lag, with Performance Insights available for query-level analysis.

**Storage management.** Volume provisioning and, with storage autoscaling enabled, automatic growth.

Collectively this is the work of a database administrator, and it is genuinely removed.

---

## What RDS Does Not Do

**Schema design and query optimization.** A missing index is a missing index. RDS provides metrics showing the database is slow and does not fix it.

**Access control.** Which security groups can reach it, which subnets it sits in, and whether it has a public endpoint are our configuration.

**Database users and passwords.** Creating users, granting privileges, and rotating credentials remain ours.

**Application-level correctness.** SQL injection is unaffected by the database being managed.

**Capacity planning.** RDS does not know that a batch job will triple the connection count next Tuesday.

**Cost control.** It provisions exactly what is asked for and bills accordingly.

---

## What We Give Up

The trade named in the Fundamentals unit, made concrete.

**No OS access.** There is no shell on the host. Tools requiring filesystem access, custom agents, or a local process cannot be used.

**Limited engine configuration.** Settings are exposed through parameter groups, which cover most but not all engine parameters. A setting not exposed cannot be changed.

**No superuser.** The master user is a privileged account but not a true superuser. Some administrative commands, extensions, and operations are unavailable.

**Version choice from a supported list.** RDS supports specific engine versions, and not every minor version.

**Forced upgrades.** AWS deprecates engine versions and will eventually upgrade during a maintenance window whether or not the application is ready. This is the consequence of handing over patching, and it needs planning for rather than discovering.

**Limited extension support.** For PostgreSQL, RDS supports a specific extension list. An extension outside it cannot be installed at all.

---

## The Engine Options

| Engine | Notes |
|---|---|
| PostgreSQL | Broad feature set, strong extension ecosystem within the supported list |
| MySQL | Widely used, well understood |
| MariaDB | MySQL-compatible fork |
| Oracle | Licensing via AWS or bring-your-own |
| SQL Server | Licensing included, edition-dependent features |
| **Aurora** (PostgreSQL/MySQL compatible) | AWS-built storage layer |

**Aurora** deserves separate mention. It is wire-compatible with PostgreSQL and MySQL but uses a distributed storage layer replicating six ways across three AZs. It offers faster failover, faster replica creation, storage that grows automatically, and generally better performance — at a higher price, with its own operational behaviors. **Aurora Serverless v2** scales capacity automatically, which suits variable or intermittent workloads.

For a new relational workload on AWS, Aurora PostgreSQL is often the default choice; standard RDS PostgreSQL is cheaper and simpler for steady, modest workloads.

---

## When RDS Is Not the Answer

**When a specific engine version or unsupported extension is required.** Self-managing on EC2 is then the only option, with all the work that implies.

**When the workload is not relational.** Key-value access patterns belong in DynamoDB; analytics belong in Redshift.

**When cost matters more than availability** in a non-production environment. A small database on an EC2 instance is cheaper than RDS Multi-AZ, and for a development environment that may be the right trade.

**When scale exceeds what a single writer can handle.** RDS scales reads with replicas and writes only vertically. Beyond the largest instance, the answer is sharding or a different data store.

---

## Key Takeaways

- RDS manages provisioning, patching, backups, failover, replication, monitoring, and storage.
- It does not manage schema design, query performance, access control, database users, or capacity planning.
- There is no OS access and no true superuser; engine configuration is limited to what parameter groups expose.
- Engine versions come from a supported list, and AWS eventually forces upgrades during maintenance windows.
- PostgreSQL extension support is limited to a specific list, which can be a deciding constraint.
- Aurora is wire-compatible with PostgreSQL and MySQL with a distributed storage layer, better performance, and higher cost.
- RDS scales reads with replicas and writes only vertically, so very high write volume needs a different approach.
