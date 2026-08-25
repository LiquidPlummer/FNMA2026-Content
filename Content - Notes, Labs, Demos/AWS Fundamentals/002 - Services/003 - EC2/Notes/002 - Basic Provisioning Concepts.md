# Basic Provisioning Concepts

Launching an EC2 instance involves more decisions than just picking an instance type. This topic walks through the core concepts involved in getting an instance up, running, and reachable — the pieces that show up every time, regardless of what the instance is ultimately going to run.

## Key Pairs

EC2 instances (particularly Linux ones) are typically secured with **key pairs** rather than passwords by default. AWS (or we, ahead of time) generates a public/private key pair: the public key is installed on the instance automatically at launch, and we keep the private key ourselves. Connecting to the instance means authenticating with that private key rather than typing a password.

This matters practically for one big reason: **the private key is only available to download once, at creation time.** Losing it doesn't lock us out entirely (there are recovery paths), but it's meaningfully more painful than losing a password we could just reset — so treating that private key file like any other sensitive credential, and storing it somewhere safe immediately, is worth building as a habit from the first instance we ever launch.

## Security Groups (A First Look)

Every instance is associated with one or more **security groups** — a virtual firewall controlling what network traffic is allowed to reach it, and what it's allowed to send out. By default, a new security group typically denies all inbound traffic until we explicitly allow it (for example, allowing inbound traffic on the port a web server listens on). We'll go into security groups in real depth in the VPC lesson later in this module — for now, the important thing to know is that a freshly launched instance is *not* automatically reachable from the internet just because it has a public IP address; the security group has to explicitly permit that traffic first.

## Storage: EBS Volumes

An EC2 instance needs a disk to boot from and store data on. This is typically provided by **EBS** (Elastic Block Store) — network-attached block storage that persists independently of the instance's lifecycle (with the right configuration), meaning the data on it can survive the instance being stopped, or even be detached and reattached to a different instance entirely. This is worth contrasting with S3: EBS is block storage, behaving like a traditional disk attached to one instance at a time, while S3 is object storage accessed over HTTP, as covered in the S3 lesson — different tools for different jobs.

## Launching an Instance: The Shape of It

Regardless of the specific console flow (which, again, changes over time), launching an instance always involves the same core decisions, in roughly this order:

1. **Choose an AMI** — the OS and starting software.
2. **Choose an instance type** — the hardware profile, based on the workload's bottleneck.
3. **Configure networking** — which VPC/subnet, and whether it gets a public IP.
4. **Attach or create a security group** — what traffic is allowed in and out.
5. **Select or create a key pair** — how we'll authenticate to it.
6. **Configure storage** — the EBS volume(s) attached to it.

## Stopping vs Terminating

One distinction that trips up a lot of newcomers: **stopping** an instance shuts it down but preserves it (and, typically, its EBS storage) so it can be started again later — we generally stop paying for compute time while it's stopped, though attached storage usually continues to incur its own cost. **Terminating** an instance deletes it permanently — there's no starting it back up afterward. Knowing which one we actually want before clicking it matters, since terminating is generally not reversible.

## Why These Concepts Matter Beyond EC2

Key pairs, security groups, and network configuration aren't unique to EC2 — they're foundational concepts that reappear throughout AWS wherever compute resources need to be secured and connected to a network. Getting comfortable with them here pays off directly when we get to the VPC lesson, where we'll dig much deeper into how networking and security groups actually work.
