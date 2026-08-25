# What AWS Manages vs What the Customer Manages

AWS is explicit about a single organizing idea for security and operations on its platform: the **Shared Responsibility Model**. It draws a clear line between what AWS is responsible for and what we, the customer, are responsible for — and understanding that line is essential, because security incidents often trace back to someone assuming the "other side" was handling something that neither side actually was.

## AWS's Responsibility: "Security *of* the Cloud"

AWS is responsible for protecting the infrastructure that runs every AWS service — the physical data centers, the hardware, the networking, and the virtualization layer that separates one customer's resources from another's. Concretely, this includes:

- **Physical security** of data centers — controlling who can physically access the buildings and hardware.
- **Hardware maintenance** — replacing failed disks, servers, and networking equipment.
- **The virtualization layer** — the software that safely isolates one customer's virtual machines from another's on shared physical hardware.
- **Global network infrastructure** connecting regions and Availability Zones.

This is often summarized as security *of* the cloud — AWS makes sure the platform itself is secure and reliable.

## The Customer's Responsibility: "Security *in* the Cloud"

We, in turn, are responsible for securing what we build *on* that platform. This includes:

- **Data** — classifying it, encrypting it where appropriate, and controlling who can access it.
- **Identity and access management** — configuring IAM users, roles, and policies correctly so people and systems only have the access they actually need.
- **Operating system and network configuration** — for services like EC2, patching the OS, configuring firewalls (security groups), and keeping software up to date.
- **Application-level security** — writing code that doesn't have exploitable vulnerabilities, and configuring the application correctly.

This is security *in* the cloud — we're responsible for how we use the platform, even though AWS built and secures the platform itself.

## A Concrete Example

Consider an S3 bucket containing sensitive files. AWS guarantees the physical durability of that data and the security of the underlying storage infrastructure — a hardware failure in an S3 data center will not lose the data, and no other AWS customer can access the physical disks it sits on. But if we configure the bucket's *permissions* to allow public read access by mistake, that's on us. AWS built a secure vault; we're the ones who left the door unlocked. This exact scenario — a misconfigured public bucket — is one of the most common real-world cloud security incidents, precisely because it sits squarely in customer territory.

## Why This Model Exists

The line has to be drawn somewhere, and AWS draws it at the boundary of what it can reasonably control. AWS can guarantee the physical hardware is secure because it owns and operates it directly. It cannot guarantee that a customer configured their IAM policies sensibly, because that decision belongs entirely to the customer. Understanding this boundary is one of the most practically important things to internalize about using AWS — it tells us exactly where our own diligence has to start.
