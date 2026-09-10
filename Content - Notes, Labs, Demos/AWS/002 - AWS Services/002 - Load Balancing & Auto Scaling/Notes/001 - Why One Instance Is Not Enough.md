# Why One Instance Is Not Enough

A single EC2 instance running an application has two independent problems. It is a **single point of failure**, and it is a **fixed capacity ceiling**. Load balancing addresses the first; auto scaling addresses the second. Both are needed, and confusing them leads to solving only half the problem.

---

## The Single Point of Failure

Everything that can take one instance offline:

- **The physical host fails.** Hardware dies, and the instance dies with it.
- **The Availability Zone has an incident.** Power, cooling, or network in one facility.
- **AWS retires the instance** because its hardware is degrading.
- **The application crashes**, runs out of memory, or deadlocks.
- **A deployment goes wrong**, and the only instance is the broken one.
- **The operating system needs a reboot** for a kernel patch.

Some of these are rare; all of them happen. With one instance, each is an outage — and the last two are entirely routine operations that become incidents purely because there is nowhere for traffic to go.

The instance also cannot be replaced without downtime, which means ordinary maintenance requires a maintenance window. That constraint tends to slow patching, which creates its own problems over time.

---

## The Capacity Ceiling

An instance has a fixed size. When load exceeds what it can serve, the options are:

**Scale vertically** — stop the instance, choose a larger type, start it. This requires downtime, has an upper bound at the largest available instance type, and is a manual response to a problem that arrives faster than a person can react.

**Scale horizontally** — add more instances. No downtime, no practical ceiling, and it can be automated.

The economics differ too. Vertical scaling means provisioning for peak and paying for that size continuously, including overnight when the load is a tenth of peak. Horizontal scaling allows capacity to track demand.

```
Vertical:    [────── m5.4xlarge, 24/7 ──────]   sized for peak, paid always

Horizontal:  [m5.large]                          quiet period
             [m5.large][m5.large][m5.large]      peak
             [m5.large]                          quiet again
```

*Fixed capacity sized for peak versus capacity that follows demand — the difference is what elasticity actually delivers.*

---

## What Each Piece Solves

**A load balancer** distributes requests across several instances and stops sending traffic to unhealthy ones. It solves availability: an instance failing removes capacity rather than causing an outage. It also gives clients one stable address, so instances can be replaced without anything reconfiguring.

**An Auto Scaling group** maintains a target number of healthy instances, replacing failures and adjusting count with demand. It solves both capacity and recovery — the replacement of a failed instance is automatic rather than a page for someone.

Together they produce a system where individual instances are disposable:

```
                    ┌── Instance (AZ-a) ──┐
Clients ──► ALB ────┼── Instance (AZ-b) ──┼──► Auto Scaling group
                    └── Instance (AZ-c) ──┘    maintains count, replaces failures
```

*The load balancer removes unhealthy targets from rotation; the Auto Scaling group replaces them. Neither requires human involvement.*

---

## What It Requires of the Application

This only works if instances are interchangeable, which imposes real constraints — the subject of a later note, but worth stating now because they shape application design:

- **No local session state.** A user's next request may hit a different instance.
- **No local file storage** for anything that must persist.
- **Any instance can be terminated at any time**, with no special shutdown.
- **Instances must start without manual steps.**

An application holding sessions in memory does not work behind a load balancer without sticky sessions, and sticky sessions reintroduce the failure mode we were removing.

---

## The Cost of Not Doing It

Worth being direct: this costs more. Three instances cost three times one instance, plus a load balancer at roughly $16–20/month, plus cross-AZ transfer.

The comparison is against downtime. For a production system, one AZ incident or one failed host justifies the difference immediately. For a development environment used by four people during working hours, a single instance is a perfectly reasonable choice.

The decision is per environment, and it should be a decision rather than an accident.

---

## Key Takeaways

- One instance is both a single point of failure and a fixed capacity ceiling — two separate problems.
- Host failures, AZ incidents, retirements, crashes, deployments, and reboots all become outages with one instance.
- Vertical scaling requires downtime and has an upper bound; horizontal scaling has neither and can track demand.
- A load balancer provides a stable address and removes unhealthy instances from rotation.
- An Auto Scaling group maintains instance count and replaces failures automatically.
- Both require instances to be interchangeable: no local session state, no local persistent files, and unattended startup.
- The redundancy costs more, and whether it is worth it differs by environment.
