# The Shared Responsibility Model

The **shared responsibility model** is AWS's statement of where its obligations end and ours begin. It is not a slogan — it is the map used to answer "whose fault is this?" after an incident, and it is the basis of what AWS will and will not do for us.

---

## The Basic Division

AWS's usual phrasing is that AWS is responsible for the security **of** the cloud, and the customer is responsible for security **in** the cloud.

```
┌─────────────────────────────────────────────┐
│  OURS — security IN the cloud                │
│  • Our data, and whether it is encrypted     │
│  • IAM identities, policies, and credentials │
│  • Network configuration (SGs, subnets)      │
│  • OS patching, where we have an OS          │
│  • Application code and its vulnerabilities  │
├─────────────────────────────────────────────┤
│  AWS — security OF the cloud                 │
│  • Physical data centers and access to them  │
│  • The hypervisor and host operating system  │
│  • The network backbone between facilities   │
│  • Hardware lifecycle and disposal           │
│  • The managed-service software itself       │
└─────────────────────────────────────────────┘
```

*The lower half is AWS's, permanently and without configuration; the upper half is ours, and every item in it is something we can get wrong.*

The dividing line is roughly this: **AWS operates the infrastructure; we configure and use it.** AWS guarantees the lock on the door works. Whether we leave the door open is our decision.

---

## Why This Framing Exists

It is not just a legal position. It is genuinely descriptive of what each party is able to do.

AWS cannot decide who in our organization should read a given S3 bucket — it does not know our organization. It cannot know whether a given port should be open, because it does not know our application. It cannot know whether the data we are storing is sensitive. Every one of those decisions requires context AWS does not have, so every one of them is ours.

Conversely, we cannot patch a hypervisor, replace a failed disk, or control physical access to a building in Ohio. Those require access we do not have, so they are AWS's.

The model is a division based on **who has the information and the access to act**, and that is a more useful way to remember it than memorizing the two lists.

---

## Where It Gets Applied

Two examples make the practical difference clear.

**A publicly readable S3 bucket that exposes customer data.** S3 worked exactly as designed and as configured. The bucket policy said "allow public reads," and S3 allowed public reads. This falls entirely on our side of the line. AWS provides tools to prevent it — Block Public Access, IAM policies, access analyzers — but does not override the configuration we gave it.

**A vulnerability in the hypervisor that lets one tenant read another's memory.** This is AWS's, and AWS patches it, usually without us knowing it happened.

Between those poles sit the cases people actually get wrong, and those depend on the service type — which is the subject of the next note.

---

## Key Takeaways

- AWS secures the cloud (facilities, hardware, hypervisor, backbone network, managed-service software); we secure what we put in it (data, identities, network configuration, application code).
- The split follows who has the information and access to act, not an arbitrary contract boundary.
- AWS will not override our configuration, so a resource exposed by a policy we wrote is our incident.
- The model is the reference used to assign responsibility after something goes wrong, so it is worth knowing before something does.
