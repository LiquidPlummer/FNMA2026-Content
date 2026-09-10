# EBS Volumes & Snapshots

**EBS (Elastic Block Store)** provides network-attached block storage. An EBS volume behaves like a disk, persists independently of any instance, and is the durable storage most EC2 workloads rely on.

---

## Volume Basics

An EBS volume is created in a specific Availability Zone and **can only attach to an instance in that same AZ**. There is no cross-AZ attach — moving a volume to another zone means snapshotting it and creating a new volume from that snapshot there.

A volume attaches to one instance at a time (Multi-Attach exists for `io1`/`io2` with a cluster-aware filesystem, which is a specialist case). It persists when the instance stops, and survives termination unless `DeleteOnTermination` is set.

**Billing is on provisioned size, not used size.** A 500 GB volume holding 4 GB costs 500 GB. This is the most common EBS cost mistake, and it is why volumes should be sized to need rather than to a comfortable round number.

---

## Volume Types

| Type | Description | Suits |
|---|---|---|
| `gp3` | General purpose SSD, baseline 3,000 IOPS | Default choice |
| `gp2` | Older general purpose SSD, IOPS tied to size | Legacy |
| `io2` | Provisioned IOPS SSD, high durability | Demanding databases |
| `st1` | Throughput-optimized HDD | Large sequential reads |
| `sc1` | Cold HDD | Infrequently accessed bulk data |

**`gp3` is the default choice, and `gp2` should generally be migrated.** `gp3` is roughly 20% cheaper per gigabyte, provides 3,000 IOPS at any size, and allows IOPS and throughput to be provisioned independently of capacity.

`gp2` ties IOPS to size — 3 IOPS per GB — which forces over-provisioning capacity to obtain performance. A 1 TB `gp2` volume bought purely for its 3,000 IOPS is exactly the case `gp3` fixes. Modifying `gp2` to `gp3` is a live operation requiring no downtime.

`gp2` also has a burst credit mechanism, and its `BurstBalance` metric is worth alarming on — exhausting credits collapses performance the same way `t`-instance CPU credits do.

---

## Modifying Volumes

Volumes can be modified live — size increased, type changed, IOPS adjusted — without detaching:

```bash
aws ec2 modify-volume --volume-id vol-0abc123 --size 200 --volume-type gp3
```

*Grows the volume and converts it to `gp3` while the instance runs. The filesystem must then be extended inside the OS with `growpart` and `resize2fs` or `xfs_growfs`.*

Two constraints: **volumes can only grow, never shrink**, and there is a cooldown of several hours before the same volume can be modified again. Shrinking requires creating a smaller volume and copying data across.

---

## Snapshots

A **snapshot** is a point-in-time backup of a volume, stored in S3 (in AWS-managed storage, not a bucket we can see).

**Snapshots are incremental.** The first captures everything; each subsequent one stores only changed blocks. Deleting a snapshot does not break later ones — AWS retains whatever blocks the remaining snapshots need. So retaining many snapshots is cheaper than it appears, and deleting one saves less than expected.

**Snapshots are region-scoped** and can be copied across regions or accounts, which is how backups are made resilient to a region failure and how encrypted data crosses account boundaries.

**Snapshots of an attached, running volume can be inconsistent** in the same way as pulling the power on a machine. Databases with data in memory need their own quiescing — flushing to disk or using the database's backup mechanism — before the snapshot is meaningful.

```bash
aws ec2 create-snapshot --volume-id vol-0abc123 \
  --description "pre-upgrade" \
  --tag-specifications 'ResourceType=snapshot,Tags=[{Key=Environment,Value=production}]'
```

*Tags at creation matter here: snapshots do not inherit tags from their volume, and untagged snapshots accumulate with no indication of what they are.*

---

## Lifecycle Management

Snapshots grow without bound unless something expires them. **Data Lifecycle Manager (DLM)** creates and deletes them on a schedule based on tags:

- Select volumes by tag
- Snapshot on a schedule
- Retain a set count or age
- Optionally copy to another region
- Copy volume tags onto the snapshots

This is the standard answer to both problems at once — backups happen automatically, and old ones expire rather than accumulating.

---

## Performance Notes

**EBS is network-attached**, so its throughput shares the instance's network capacity. Small instances have limited EBS bandwidth regardless of the volume's provisioned performance — a `gp3` volume with 1,000 MB/s provisioned cannot exceed what the instance can carry. **EBS-optimized** instances (the default on current generations) provide dedicated bandwidth for it.

Volume performance is also per volume, so striping several volumes with RAID 0 raises aggregate throughput at the cost of making a single volume failure fatal to the set.

---

## Key Takeaways

- EBS volumes are AZ-scoped and attach only to instances in the same AZ.
- Billing is on provisioned size, so an oversized volume costs full price regardless of usage.
- `gp3` is the default choice: cheaper than `gp2`, 3,000 baseline IOPS at any size, with performance provisioned separately from capacity.
- Volumes can be grown and retyped live but never shrunk, with a cooldown between modifications.
- Snapshots are incremental, region-scoped, and copyable across regions and accounts.
- Snapshots of a running volume can be crash-inconsistent; databases need quiescing first.
- Snapshots do not inherit volume tags — tag at creation, and use Data Lifecycle Manager to expire them.
- EBS throughput is bounded by the instance's network capacity, not only by the volume's provisioned performance.
