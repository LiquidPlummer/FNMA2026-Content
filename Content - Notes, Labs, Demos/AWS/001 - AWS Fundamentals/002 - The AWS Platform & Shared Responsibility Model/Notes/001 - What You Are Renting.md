# What You Are Renting

Underneath every abstraction, a cloud provider rents us someone else's hardware, on demand, billed by use. Holding onto that plain description keeps a lot of later behavior from being surprising.

---

## The Physical Reality

AWS owns data centers full of servers, disks, and network equipment. When we launch an EC2 instance, a hypervisor on one of those physical servers allocates a slice of CPU, memory, and network to us. When we put an object in S3, bytes land on physical disks in a specific geographic location. There is no cloud — there is a building in Virginia with a lot of hardware in it and a very good API in front of it.

Three consequences follow directly, and all three explain behavior we will meet repeatedly:

**Physical things fail.** A host can die, taking the instances on it with it. A disk can fail. A data center can lose power. AWS's scale makes individual failures routine rather than exceptional, which is why so much of AWS's design assumes components disappear without warning.

**Physical things are finite.** Capacity in a given Availability Zone for a given instance type is a real, limited quantity. An `InsufficientInstanceCapacity` error means exactly what it says: AWS does not currently have a free machine of that shape in that location.

**Physical things have a location.** Data is somewhere specific. That location determines latency, which regulations apply, and what it costs to move bytes elsewhere.

---

## What "On Demand" Actually Means

The rental is granted and revoked by API call, in seconds to minutes, with no negotiation and no contract per transaction. That is the genuinely new part. It has two implications worth stating early.

**We can create expensive things instantly.** A misconfigured script can launch fifty large instances in under a minute, and nothing stops it except service quotas and our own controls. The absence of a purchasing process is the feature *and* the risk.

**We keep paying until we say stop.** Rental is continuous. A resource that exists is billed whether or not anyone uses it. Forgetting about a resource is the single most common way to waste money on AWS, and it happens because there is no physical object sitting in a rack reminding anyone it exists.

---

## Billed by Use — But "Use" Varies

Different services meter different things, and the unit is not always time:

| What is metered | Example |
|---|---|
| Time a resource exists | EC2 instance-seconds, RDS instance-hours |
| Data stored, over time | S3 gigabyte-months, EBS provisioned gigabyte-months |
| Requests made | S3 `GET`/`PUT` counts, API Gateway requests |
| Data moved | Bytes out to the internet, bytes across Availability Zones |
| Work performed | Lambda gigabyte-seconds, DynamoDB read/write units |

*The billing unit differs per service, so "how am I charged for this?" is a question to ask about each service separately.*

Note that EBS is billed on **provisioned** capacity, not used capacity. A 500 GB volume with 4 GB of data on it costs 500 GB. Provisioned-versus-consumed is a distinction worth checking for every storage service we adopt.

---

## Key Takeaways

- AWS rents physical hardware on demand, billed by use; the abstraction sits on real machines in real buildings.
- Hardware fails, capacity is finite, and location matters — which is why AWS designs assume components can vanish.
- On-demand provisioning means expensive resources can be created instantly, with no purchasing process to slow anyone down.
- Billing is continuous for as long as a resource exists, so forgotten resources are the most common source of waste.
- Each service meters something different — time, storage, requests, data movement, or work — and some meter provisioned rather than consumed capacity.
