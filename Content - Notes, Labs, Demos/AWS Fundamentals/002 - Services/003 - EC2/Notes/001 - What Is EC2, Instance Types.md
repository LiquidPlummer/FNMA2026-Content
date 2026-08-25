# What Is EC2, Instance Types

**EC2** (Elastic Compute Cloud) is AWS's core IaaS offering — virtual servers, rentable by the hour or second, that we configure and manage ourselves. If S3 is AWS's answer to "I need to store files," EC2 is AWS's answer to "I need a computer to run something on." It's one of the oldest AWS services and, for a lot of workloads, still the default starting point for running an application.

## What an EC2 Instance Is

An **instance** is a single virtual server. Launching one means choosing:

- An **Amazon Machine Image (AMI)** — a template defining the operating system and any pre-installed software the instance starts with. AWS provides standard AMIs (various Linux distributions, Windows Server), and we can also create our own from a configured instance to reuse later.
- An **instance type** — which determines the virtual hardware: how much CPU, memory, storage, and network performance the instance gets (covered in more detail below).
- **Networking and security settings** — which VPC and subnet the instance lives in, and which security group (firewall rules) apply to it — both covered in depth in the VPC lesson later in this module.

Once launched, the instance behaves like a real server: we can connect to it (typically over SSH for Linux or RDP for Windows), install software, and run whatever workload we need — a web server, an application backend, a batch job, anything a computer can run.

## Instance Types

AWS offers many different **instance types** — essentially, different hardware profiles — grouped into families optimized for different kinds of workloads. Rather than memorizing specific type names (which AWS adds to and revises over time), it's more useful to understand the *categories* they fall into:

- **General purpose** — a balanced ratio of compute, memory, and networking, suited to a wide range of everyday workloads like web servers and small-to-medium applications where no single resource is the clear bottleneck.
- **Compute optimized** — a higher ratio of CPU relative to memory, suited to workloads that are CPU-bound: batch processing, media transcoding, high-performance computing.
- **Memory optimized** — extra RAM relative to CPU, suited to workloads that need to hold large amounts of data in memory: in-memory caches, large databases, real-time big-data processing.
- **Storage optimized** — high-throughput, high-IOPS local storage, suited to workloads doing heavy, fast disk I/O: certain databases and data warehousing workloads.
- **Accelerated computing** — instances with attached GPUs or other specialized hardware, suited to machine learning training/inference, graphics rendering, or scientific computing.

Within each family, AWS also offers a range of **sizes** — the same balance of resources, scaled up or down (this is the vertical scaling concept from earlier in the course, applied concretely). Because exact type names, specs, and pricing change as AWS releases new hardware generations, the practical habit is to identify which *category* fits a workload's bottleneck, then check AWS's current instance type documentation for the specific options available in that category at the time.

## Choosing an Instance Type

The core question to ask is: **what resource is this workload actually bottlenecked on?** A web server handling many small, fast requests is usually a good fit for general purpose. A service crunching large in-memory datasets wants memory optimized. Getting this roughly right matters more than picking the "perfect" specific size — it's easy to resize an instance later (that's vertical scaling in action), so starting with the right *category* and adjusting the *size* as real usage data comes in is a perfectly reasonable approach.

With instance types covered, the next topic turns to the practical side: what's actually involved in getting an instance up and running.
