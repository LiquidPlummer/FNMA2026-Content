# Instances, AMIs & Instance Types

An **EC2 instance** is a virtual machine. Launching one requires two decisions: what disk image it boots from, and what size of hardware it runs on.

---

## AMIs

An **Amazon Machine Image (AMI)** is a template containing a root volume snapshot, launch permissions, and a block device mapping. It determines what the instance is running the moment it boots.

Sources, in rough order of how much is already done for us:

- **AWS-provided** — Amazon Linux 2023, Ubuntu, Windows Server, and others, patched and published regularly.
- **Marketplace** — vendor images, sometimes carrying an hourly software charge on top of the instance cost.
- **Community** — published by anyone. Unverified, and worth treating accordingly.
- **Custom** — built by us, from a configured instance or with a tool like Packer.

Three properties matter in practice:

**AMIs are region-scoped.** An AMI in `us-east-1` cannot launch an instance in `eu-west-1`. Copying it to another region creates a new AMI with a different ID — which is why multi-region templates cannot hard-code an AMI ID.

**AMI IDs change with every update.** `ami-0abc123` is a specific build, not "the latest Amazon Linux." AWS publishes the current ID in SSM Parameter Store so templates can resolve it dynamically:

```bash
aws ssm get-parameter \
  --name /aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64 \
  --query "Parameter.Value" --output text
```

*Resolves the current Amazon Linux 2023 AMI ID, so templates stay correct as new builds are published.*

**Custom AMIs trade build time for boot time.** Baking dependencies into an image makes instances start faster and more predictably than installing them at boot with user data. The cost is a pipeline to rebuild and re-patch the image.

---

## Instance Types

The name encodes what the instance is:

```
m5.2xlarge
│││ │
│││ └── size
││└──── generation
│└───── additional capabilities
└────── family
```

**Families** group by what the instance is optimized for:

| Family | Optimized for | Typical use |
|---|---|---|
| `t` | Burstable | Small, variable workloads |
| `m` | Balanced | General-purpose applications |
| `c` | Compute | CPU-bound work, batch processing |
| `r` | Memory | Caches, in-memory databases |
| `i`, `d` | Storage | High local disk I/O |
| `g`, `p` | Accelerated | GPU workloads, machine learning |

**Suffix letters** add detail: `g` means Graviton (ARM), `a` means AMD, `n` means enhanced network, `d` means local NVMe storage. So `m7g.large` is a seventh-generation general-purpose Graviton instance.

**Sizes** scale roughly linearly — `large`, `xlarge`, `2xlarge`, `4xlarge` — with both resources and price roughly doubling at each step.

---

## Burstable Instances

The `t` family works differently and is worth understanding before it causes an incident.

A `t3.micro` does not get a full vCPU continuously. It earns **CPU credits** at a fixed rate and spends them when running above its baseline (a small percentage of a core). While credits last, it performs like a full instance. When they run out, it is throttled hard to the baseline — and a workload that was fine for hours becomes unusably slow.

**Unlimited mode** (the default on newer `t` instances) allows borrowing beyond the credit balance for an additional charge, which prevents throttling but produces an unexpected bill instead.

Burstable instances suit genuinely intermittent work. For sustained load they are a false economy, and the `CPUCreditBalance` metric is the one to alarm on.

---

## Choosing

**Match the family to the constraint.** A memory-bound application on a `c` instance wastes CPU and starves for memory. Measure which resource is actually limiting before choosing.

**Start smaller than expected and measure.** Resizing is a stop, a change, and a start. Over-provisioning at launch tends to persist because nobody revisits it.

**Consider Graviton.** ARM instances offer better price-performance than equivalent x86 types, and most interpreted and JVM workloads run on them without modification. The requirement is that dependencies have ARM builds.

**Prefer newer generations.** `m7` typically costs less per unit of work than `m5` while offering better performance. There is rarely a reason to launch an older generation.

---

## Key Takeaways

- An AMI defines what the instance boots; it is region-scoped, and its ID changes with every published build.
- Resolve AMI IDs from SSM Parameter Store rather than hard-coding them in templates.
- Custom AMIs shorten and stabilize boot at the cost of maintaining an image pipeline.
- Instance type names encode family, generation, capabilities, and size — `m7g.large` is a Graviton general-purpose instance.
- Match family to the limiting resource: `c` for CPU, `r` for memory, `m` for balanced.
- Burstable `t` instances throttle sharply when CPU credits are exhausted, so alarm on `CPUCreditBalance` and avoid them for sustained load.
- Prefer newer generations and consider Graviton for better price-performance with no commitment.
