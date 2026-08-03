# Cloud vs Traditional On-Prem Infrastructure

Before we can talk about what makes the cloud useful, we need a clear picture of what it replaced. **On-premises infrastructure** ("on-prem") means the organization owns and operates its own physical servers, networking gear, and data center space — either in a room down the hall or in a rented facility they fully control. **Cloud computing** means renting compute, storage, and networking from a provider like AWS, who owns the actual hardware and makes it available to us over the internet, on demand.

## How On-Prem Works

In an on-prem model, the organization is responsible for the entire stack, end to end:

- **Buying hardware** — servers, storage arrays, network switches, racks.
- **Housing it** — a data center with power, cooling, and physical security.
- **Maintaining it** — replacing failed disks and power supplies, patching firmware, planning for eventual hardware refreshes.
- **Staffing it** — engineers to rack equipment, run cabling, and keep everything online.

Every one of those steps takes time and money before a single application ever runs. If we need more capacity, someone has to forecast demand, get budget approved, order equipment, and wait for it to arrive and be installed — a process that can take weeks or months.

## How the Cloud Works

With cloud computing, AWS owns and operates the physical data centers, and we consume resources through an API or a web console. Instead of buying a server, we launch a **virtual machine**. Instead of building a storage array, we create a **storage bucket**. The underlying hardware still exists — it's just no longer our problem.

This shifts what the organization is responsible for. AWS handles the physical layer: buying and racking hardware, running the data centers, replacing failed components, and keeping the lights on. We focus on configuring and using the resources themselves.

## The Core Trade-Off

| | On-Prem | Cloud |
|---|---|---|
| **Upfront cost** | High (buy hardware before using it) | Low (pay for what's provisioned) |
| **Time to provision** | Weeks to months | Minutes |
| **Who maintains hardware** | The organization | The cloud provider |
| **Capacity planning** | Must forecast and over-provision for peak demand | Scale up or down as needed |
| **Physical control** | Full control over hardware and location | No physical access; trust the provider's controls |

Neither model is universally "better" — plenty of organizations still run on-prem for reasons like regulatory requirements, existing investment in hardware, or workloads with steady, predictable demand that don't benefit much from elasticity. But for most new workloads, the cloud's speed and flexibility are hard to beat, which is why it's become the default starting point. The rest of this module digs into exactly why: the specific characteristics that make cloud computing valuable, and how AWS's infrastructure is organized to deliver them.
