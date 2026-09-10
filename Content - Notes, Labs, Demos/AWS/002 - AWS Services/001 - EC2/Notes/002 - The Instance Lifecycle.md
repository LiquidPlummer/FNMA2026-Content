# The Instance Lifecycle

An instance moves through a small set of states. What matters at each transition is **what survives it** — data, IP addresses, and charges behave differently for a stop than for a terminate.

---

## The States

```
pending ──► running ──┬──► stopping ──► stopped ──► (start) ──► pending
                      │
                      └──► shutting-down ──► terminated   [permanent]
```

*Stop is reversible; terminate is not. The instance ID survives a stop and is gone after a terminate.*

---

## Stop

Stopping is roughly powering off a machine:

- **The EBS root volume persists**, with all its data.
- **The instance ID stays the same**, and the instance can be started again.
- **The private IP is retained.**
- **The automatically assigned public IP is released** — a different one is assigned on start. Anything referencing the old address breaks.
- **Instance store data is lost permanently.**
- **The instance charge stops.**
- **EBS volumes continue to bill.**

That last point is the one that matters for cost: a stopped instance is cheaper, not free. A stopped `m5.large` with a 500 GB volume still costs roughly $40/month in storage.

Starting an instance places it on **different physical hardware**, which is why stop/start resolves problems caused by a failing host — and why anything tied to the specific host is lost.

**Hibernate** is a variant that writes memory contents to the root volume and restores them on start, preserving in-memory state. It requires specific instance types and configuration, and the saved memory occupies EBS space.

---

## Terminate

Termination is permanent. The instance is deleted and the ID is never reused.

What happens to volumes depends on **`DeleteOnTermination`**, set per volume:

- **Root volumes default to `true`** — deleted with the instance.
- **Additional volumes default to `false`** — they survive, unattached, billing indefinitely.

This asymmetry is the source of most orphaned EBS volumes. Attaching a data volume and terminating the instance months later leaves the volume behind, with nothing indicating what it was for.

```bash
# Check the setting before terminating anything with data on it
aws ec2 describe-instances --instance-ids i-0abc123 \
  --query "Reservations[].Instances[].BlockDeviceMappings[].[DeviceName,Ebs.DeleteOnTermination]" \
  --output table
```

*Shows which attached volumes will be deleted on termination and which will persist.*

**Termination protection** blocks the API call entirely and is worth enabling on anything whose loss would be an incident. It does not prevent an OS-level shutdown, and it does not apply to Auto Scaling terminations.

---

## Reboot

A reboot is not a stop/start. The instance stays on the **same host**, keeps its public IP, and preserves instance store data. It is equivalent to restarting the operating system, and it is the right operation when only the OS needs restarting.

---

## What Survives What

| | Reboot | Stop/Start | Terminate |
|---|---|---|---|
| EBS root volume | Yes | Yes | Per `DeleteOnTermination` |
| Instance store data | Yes | **No** | No |
| Private IP | Yes | Yes | No |
| Auto-assigned public IP | Yes | **No** | No |
| Elastic IP | Yes | Yes (if allocated) | Released from instance |
| Instance ID | Yes | Yes | No |
| Physical host | Same | Different | — |

---

## Instance Retirement

AWS occasionally schedules an instance for **retirement** when its underlying hardware is degrading. A notification arrives with a date, and on that date the instance is stopped or terminated depending on its root volume type.

Two responses. For EBS-backed instances, a stop and start before the deadline migrates to healthy hardware. For anything important, the better answer is an architecture where a single instance disappearing does not matter — which is what Auto Scaling groups provide, and the subject of the next lesson.

---

## Key Takeaways

- Stop preserves the EBS root volume, instance ID, and private IP, but releases an auto-assigned public IP and discards instance store data.
- A stopped instance still bills for its EBS volumes; only termination stops all charges.
- Starting a stopped instance moves it to different physical hardware.
- Root volumes default to `DeleteOnTermination=true`, additional volumes to `false` — the main source of orphaned volumes.
- Termination protection blocks the API call but not an OS shutdown or an Auto Scaling termination.
- A reboot keeps the same host, public IP, and instance store data.
- AWS retires instances on degrading hardware, and a stop/start migrates them before the deadline.
