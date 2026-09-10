# How the Line Moves by Service Type

The shared responsibility line is not fixed. It sits in a different place for every service, and the pattern is consistent: **the more AWS manages, the less we control.** Comparing EC2, RDS, and Lambda for the same workload shows the whole spectrum.

---

## The Same Workload, Three Ways

Suppose we need a PostgreSQL database and an application that talks to it.

### EC2 — we get a virtual machine

We choose an AMI, launch an instance, and we have an operating system. From there, installing PostgreSQL, configuring it, patching the OS, configuring backups, and arranging failover are all ours.

- **AWS manages:** the physical host, the hypervisor, the virtual hardware
- **We manage:** the OS and its patches, PostgreSQL and its version, backups, failover, monitoring, tuning
- **We control:** essentially everything above the virtual hardware, including kernel parameters and any extension we want to compile

### RDS — we get a database endpoint

We choose an engine and a version, and RDS gives us a hostname and port. There is no shell access to the underlying host, and there is no OS for us to log into.

- **AWS manages:** the host, the OS, the database engine installation, patching, automated backups, and (if enabled) Multi-AZ failover
- **We manage:** schema, queries, users and passwords, which security groups can reach it, whether encryption is on, and parameter group settings
- **We control:** database configuration exposed through parameter groups — a large subset of engine settings, but not all of them, and nothing at the OS level

### Lambda — we get a function invocation

We upload code and specify a handler. There is no server, no OS, and no long-running process we control.

- **AWS manages:** everything up to and including the language runtime, plus scaling and availability
- **We manage:** our code, its dependencies, the memory setting, the timeout, and the execution role
- **We control:** the function's behavior and a small set of configuration knobs

---

## The Trade-Off, Stated Plainly

| | EC2 | RDS | Lambda |
|---|---|---|---|
| OS access | Full | None | None |
| Patching | Ours | AWS's | AWS's |
| Version choice | Any | From a supported list | From a supported list |
| Failure recovery | We build it | Multi-AZ, if enabled and paid for | Automatic |
| Scaling | We build it | Vertical; replicas for reads | Automatic |
| Runtime limits | The instance's | The instance's | 15-minute maximum, memory caps |
| Forced upgrades | Never | Yes, in maintenance windows | Yes, runtime deprecations |

Two rows deserve emphasis, because they are what people fail to anticipate.

**Forced upgrades.** On EC2 we can run an ancient PostgreSQL version indefinitely. On RDS, AWS deprecates engine versions and will eventually upgrade us during a maintenance window whether we are ready or not. On Lambda, runtimes are deprecated on a published schedule, and eventually functions on them stop being invocable. Handing over patching means handing over the timing of upgrades.

**Constraints we cannot lift.** Lambda's fifteen-minute execution limit is not a setting. If a job takes twenty minutes, Lambda is the wrong service, and no amount of configuration changes that. Managed services come with boundaries that are simply not ours to move.

---

## Choosing Where to Sit on the Spectrum

The question is not "which is best," it is which trade-off the workload can accept:

- Choose **more management** when the operational work is not where our value is, and the constraints fit. Most applications should not be running their own database.
- Choose **less management** when we need control the managed service will not give — a specific engine version, an unsupported extension, a kernel parameter, an execution longer than the platform allows.

The wrong reason to pick EC2 is familiarity. The wrong reason to pick a managed service is assuming it removes work that it does not — which is the subject of the next note.

---

## Key Takeaways

- The responsibility line moves per service: more AWS management means less customer control, consistently.
- EC2 gives full OS access and full responsibility for patching, backups, and failover.
- RDS removes OS and engine administration but also removes OS access and control of upgrade timing.
- Lambda removes nearly all infrastructure work and imposes hard limits, like the fifteen-minute execution ceiling, that cannot be configured away.
- Handing over patching means handing over upgrade scheduling — managed services force version changes on their timetable, not ours.
- Pick a point on the spectrum by what control the workload genuinely needs, not by familiarity.
