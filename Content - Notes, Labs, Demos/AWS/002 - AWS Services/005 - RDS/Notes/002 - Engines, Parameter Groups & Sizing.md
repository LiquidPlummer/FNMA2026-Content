# Engines, Parameter Groups & Sizing

Three configuration decisions made at or near creation: which engine and version, where engine settings live, and how the instance and its storage are sized.

---

## Parameter Groups

Engine configuration lives in a **parameter group** — the RDS equivalent of `postgresql.conf` or `my.cnf`. There is no file to edit, because there is no filesystem access.

A new instance uses a **default parameter group**, which **cannot be modified**. Changing any engine setting requires creating a custom group and associating it with the instance.

```bash
aws rds create-db-parameter-group \
  --db-parameter-group-name app-postgres16 \
  --db-parameter-group-family postgres16 \
  --description "Application settings"

aws rds modify-db-parameter-group \
  --db-parameter-group-name app-postgres16 \
  --parameters "ParameterName=log_min_duration_statement,ParameterValue=1000,ApplyMethod=immediate"
```

*Creating a custom group and enabling slow query logging above one second. The parameter group family must match the engine major version.*

**Parameters are either dynamic or static.** Dynamic parameters apply immediately; **static parameters require an instance reboot**. Changing `shared_buffers` and expecting it to take effect without a reboot is a common misunderstanding — the console shows the group as `pending-reboot` until then.

Because the default group cannot be edited, **creating a custom parameter group at the start** is worthwhile even if nothing is changed initially. Otherwise the first configuration change requires creating a group and associating it, which itself needs a reboot.

Related: **option groups** enable engine-specific features — Oracle features, SQL Server integrations, MySQL plugins. PostgreSQL uses parameter groups for most of this and rarely needs option groups.

---

## Instance Sizing

RDS instance classes mirror EC2's families:

| Class | Optimized for |
|---|---|
| `db.t` | Burstable — development, small workloads |
| `db.m` | Balanced general purpose |
| `db.r` | Memory-optimized |
| `db.x` | Very high memory |

**Memory is usually the binding constraint.** A database performs well when its working set fits in the buffer cache and poorly when queries hit disk. This is why `db.r` classes are common for production databases — more memory per dollar than `db.m`.

**Avoid `db.t` classes for production.** They share the burstable CPU credit mechanism, and a database that exhausts its credits under load slows dramatically at exactly the wrong moment.

Two constraints follow directly from instance size:

**Connection limits scale with memory.** `max_connections` defaults to a formula based on instance memory, so a small instance supports few connections. An application with a large connection pool per instance, multiplied across an Auto Scaling group, can exhaust them — which is why **RDS Proxy** or an application-side pooler exists.

**Resizing requires a restart.** Changing instance class causes downtime — brief with Multi-AZ, since the standby is modified and promoted, but not zero.

---

## Storage

**Storage types:**

- **gp3** — general purpose SSD, the default choice, with IOPS and throughput configurable independently of size
- **gp2** — older, with IOPS tied to size
- **io1/io2** — provisioned IOPS for demanding workloads
- **Magnetic** — legacy, not for new instances

The `gp2` sizing trap appears here as it does with EBS: because IOPS scale with size, a `gp2` volume is often over-provisioned purely to obtain performance. `gp3` removes that coupling.

**Storage can grow but never shrink.** Reducing storage requires dumping and restoring into a new instance. Over-provisioning storage is a permanent decision.

**Storage autoscaling** raises the ceiling automatically when free space runs low, up to a configured maximum. It prevents the outage caused by a full disk, and it does not reduce storage afterwards — so a temporary spike leaves a permanently larger, permanently billed volume. The maximum should be set deliberately.

**Storage is billed on provisioned size**, not used. A 1 TB volume holding 50 GB costs 1 TB.

---

## Engine Version

Two settings govern upgrades:

**`AutoMinorVersionUpgrade`** applies minor versions during the maintenance window. Generally worth leaving on — minor versions are bug and security fixes, and disabling it accumulates a larger upgrade later.

**Major version upgrades are always explicit** and require testing. They can involve extended downtime and are not automatically reversible, so the standard approach is to restore a snapshot to a test instance and rehearse the upgrade there first.

AWS publishes deprecation schedules for engine versions. **A version reaching end of support is upgraded automatically**, so tracking those dates avoids being surprised by a forced major upgrade.

---

## Key Takeaways

- Engine configuration lives in parameter groups; the default group cannot be modified, so create a custom one at the start.
- Static parameters require a reboot to take effect, and the console shows `pending-reboot` until then.
- Memory is usually the binding constraint, making `db.r` classes common for production.
- Avoid burstable `db.t` classes in production, since credit exhaustion degrades performance under load.
- `max_connections` scales with instance memory, which is why connection pooling or RDS Proxy is often needed.
- Storage can grow but never shrink, and is billed on provisioned size.
- Storage autoscaling prevents full-disk outages but never reduces afterwards, so set the maximum deliberately.
- Leave minor version auto-upgrade on; rehearse major upgrades on a restored snapshot, and track deprecation dates.
