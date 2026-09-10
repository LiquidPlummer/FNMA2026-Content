# Instance Store vs EBS

Some instance types include **instance store** volumes — physical disks attached to the host machine. They are dramatically faster than EBS and dramatically less durable, and the distinction has to be understood before anything is written to one.

---

## What Instance Store Is

Instance store is local NVMe or SSD storage on the physical server hosting the instance. Nothing is networked; the disk is in the same machine.

**Data on instance store is lost when the instance stops or terminates**, and when the underlying hardware fails. It survives a reboot and nothing else.

This is not a durability setting or a configuration choice. When an instance stops, it releases the host, and the local disks go with it. The data is gone with no recovery path.

---

## The Comparison

| | Instance store | EBS |
|---|---|---|
| Location | Physical host | Network-attached |
| Survives stop | **No** | Yes |
| Survives terminate | No | Optionally |
| Survives reboot | Yes | Yes |
| Snapshots | Not supported | Supported |
| Detach and reattach | No | Yes |
| Resize | No | Yes, live |
| Performance | Very high, very low latency | Good, network-bounded |
| Cost | Included in instance price | Billed per provisioned GB |
| Availability | Only on certain instance types | Any instance |

The performance difference is real. Instance store on an `i4i` or `i3` instance delivers hundreds of thousands of IOPS at microsecond latencies — well beyond what any EBS volume provides, because there is no network in the path.

---

## When Instance Store Fits

The pattern is **data that can be regenerated or that exists elsewhere**:

- **Caches.** A cache node's contents are rebuildable from the source of truth.
- **Scratch and temporary files.** Intermediate data in a processing pipeline.
- **Replicated databases.** Cassandra, Elasticsearch, and similar systems replicate across nodes, so a node's local storage failing is a node replacement rather than data loss.
- **Buffers and spool space.** Data in transit that is checkpointed elsewhere.
- **Very high-throughput workloads** where EBS is the bottleneck and durability is handled at the application layer.

The common thread: losing the disk means rebuilding a node, not losing data.

---

## When It Does Not

Anything that is the sole copy of something. A database with no replicas, uploaded files, application state that cannot be reconstructed — for all of these the correct answer is EBS with snapshots, or S3.

The dangerous failure is subtle. An instance with instance store runs correctly for months. Then someone stops it to resize, or the host fails, and the data is gone — with no warning at the time it was written, because writing to instance store looks exactly like writing to any other disk.

---

## Practical Notes

**Instance store volumes are not mounted automatically** on most AMIs. They appear as block devices and must be formatted and mounted, typically through user data:

```bash
#!/bin/bash
mkfs -t xfs /dev/nvme1n1
mkdir -p /mnt/scratch
mount /dev/nvme1n1 /mnt/scratch
```

*User data formatting and mounting an instance store volume at boot. This runs on every launch, because the volume is genuinely new each time.*

Note that this reformats on each launch — appropriate for scratch space, and destructive if anyone assumed persistence.

**They are included in the instance price.** An `i4i.large` costs more per hour than an `m5.large` partly because the local storage is bundled. There is no separate storage charge and no way to add instance store to an instance type that lacks it.

**They cannot be added later.** Instance store availability is a property of the instance type, decided at launch.

**Encryption is automatic** on current generations, with keys managed by the hardware.

---

## Key Takeaways

- Instance store is physical disk on the host, offering very high performance with no durability guarantee.
- Data is lost on stop and on terminate, and survives only a reboot — this is inherent, not configurable.
- It cannot be snapshotted, detached, resized, or added to an instance type that lacks it.
- It suits caches, scratch space, and replicated data stores where losing a node means replacing it, not losing data.
- Never use it as the sole copy of anything, since the loss arrives without warning long after the data was written.
- Volumes usually need formatting and mounting via user data, which reformats them on every launch.
- The cost is bundled into the instance price rather than billed separately.
