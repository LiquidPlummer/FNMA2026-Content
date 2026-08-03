# Module Review — Answer Key

Mirrors "Module Review - Reading Questions.md" 1:1. Source lesson noted in brackets for each entry, in case deeper context is needed than what's given here.

## Cloud Fundamentals

**Q1: Why is elasticity described as what makes pay-as-you-go pricing "actually valuable"? What would pay-as-you-go look like without it?** *[What Is Cloud Computing]*

A: Without **elasticity**, we'd still have to guess our capacity needs months in advance and over-provision for the worst case — meaning we'd be paying (via pay-as-you-go) for capacity we provisioned defensively, even if it's mostly idle. Elasticity removes the need to guess: capacity follows demand in near real time, so pay-as-you-go billing actually reflects real usage rather than a padded forecast.

---

**Q2: How do on-demand, pay-as-you-go, and elasticity reinforce each other, rather than being three independent features?** *[What Is Cloud Computing]*

A: Each one removes a different obstacle: **on-demand** removes the provisioning delay, **pay-as-you-go** removes the large upfront cost, and **elasticity** removes the need to guess capacity ahead of time. Together, they let an organization start small, experiment cheaply, and let infrastructure grow or shrink with actual demand — take any one away and the other two lose most of their value (e.g., pay-as-you-go without elasticity just means paying, over time, for a guess).

## Scalability & Availability

**Q3: What's the core difference between vertical and horizontal scaling, and what does each one cost you in return for the capacity it adds?** *[Benefits of Cloud]*

A: **Vertical scaling** makes a single machine more powerful (more CPU/RAM/storage) — simple, usually no code change, but capped by the largest instance size available and remains a single point of failure. **Horizontal scaling** adds more machines and spreads load across them (typically behind a load balancer) — no real ceiling and better fault tolerance, but it costs application complexity, since the app has to run as multiple independent copies (usually meaning it can't rely on storing state in a single server's memory).

---

**Q4: Why does horizontal scaling typically require an application to be designed differently (e.g., avoiding in-memory session state), while vertical scaling usually doesn't?** *[Benefits of Cloud]*

A: With **vertical scaling**, there's still only one instance of the application running — it just has more resources, so the app doesn't need to know or care. With **horizontal scaling**, requests can land on *any* of several machines, so anything the app stored only in one server's memory (like a user's session) may not be there for the next request. This is why horizontal scaling generally pushes toward **statelessness** — state needs to live somewhere shared (a database, a cache) rather than in one server's local memory.

---

**Q5: Give an AWS-specific example of vertical scaling and one of horizontal scaling. What's actually different about what you're doing in each case?** *[Benefits of Cloud]*

A: Resizing an EC2 instance to a larger instance type is **vertical scaling** — one machine, made bigger. Placing multiple EC2 instances behind a load balancer, with an **Auto Scaling group** adding or removing instances based on demand, is **horizontal scaling** — the number of machines changes, not the size of any one of them.

---

**Q6: What's the difference between high availability and fault tolerance — and why do most real-world AWS systems aim for the former rather than the latter?** *[Benefits of Cloud]*

A: **High availability (HA)** means the system is designed to minimize downtime — it may have a brief blip while it detects a failure and redirects traffic, but it keeps running or recovers quickly. **Fault tolerance** is a stronger guarantee: *zero* interruption, even during a failure. Most systems aim for HA rather than full fault tolerance because true fault tolerance is expensive and hard to achieve, and a brief, automatic recovery is "good enough" for the vast majority of real-world use cases.

---

**Q7: How does redundancy actually deliver high availability? What has to be true about the redundant copies for this to work?** *[Benefits of Cloud]*

A: **Redundancy** means running multiple copies of a component so that if one fails, another is already there to take over. For this to actually improve availability, the copies need to be independent enough that whatever takes down one of them (a power failure, a networking issue) doesn't also take down the others — which is exactly why redundant copies are placed in physically separate locations (e.g., different Availability Zones) rather than side by side on the same rack.

## Global Infrastructure: Regions & Availability Zones

**Q8: Why are AWS regions fully independent of each other by default — what would break, or what would be lost, if they weren't?** *[AWS Global Infrastructure]*

A: Each **Region** has its own separate set of resources, and nothing created in one is visible or accessible from another by default. This independence contains the **blast radius** of a problem — an outage in one region can't cascade into every other region — and gives organizations precise control over exactly where their data physically lives, which is often a legal requirement, not just a preference. If regions weren't independent, an issue anywhere could potentially ripple everywhere.

---

**Q9: What four factors typically drive the choice of which region to run a workload in?** *[AWS Global Infrastructure]*

A: **Latency** (pick a region close to most users), **compliance and data residency** (legal/contractual requirements on where data lives), **service availability** (not every service launches in every region at the same time), and **cost** (pricing can vary slightly by region due to local costs like electricity and real estate).

---

**Q10: What does it mean for an AWS service to be "region-scoped," and what's the most common mistake this causes for people new to AWS?** *[AWS Global Infrastructure]*

A: A **region-scoped** service means a resource exists in exactly one region and is invisible from any other — switching the console's region selector changes the entire list of visible resources. The common mistake: creating a resource, switching regions to check something else, and then being confused when the resource seems to have "disappeared" — it hasn't; it's just not in the region currently being viewed.

---

**Q11: What makes an Availability Zone "independent," and why does that independence matter?** *[AWS Global Infrastructure]*

A: An **Availability Zone (AZ)** has its own independent power, cooling, and networking — separate enough physically that a failure in one (a power outage, a cooling failure, a fire) doesn't take down the others. That independence is what makes an AZ a genuinely separate failure domain, not just a different room in the same building.

---

**Q12: How is an AZ different from a region — how would you explain the relationship between the two?** *[AWS Global Infrastructure]*

A: A **Region** is the broad geographic area; **AZs** are the individual, physically isolated pockets of infrastructure within it — a region typically contains several (often three or more). It's a hierarchy: choose a region first, then (automatically or explicitly) an AZ within that region for a given resource.

---

**Q13: What's the core design principle for surviving an AZ failure, and what happens if it's ignored?** *[AWS Global Infrastructure]*

A: **Anything that must stay available shouldn't live in only one AZ.** If an entire application — servers, database, everything — runs in a single AZ, that AZ failing takes the whole application down with it, regardless of how well-architected the application is otherwise.

---

**Q14: How does a Multi-AZ design for compute (EC2 + load balancer) actually survive an AZ going down? Walk through what happens.** *[AWS Global Infrastructure]*

A: Instead of one EC2 instance, multiple instances run spread across two or more AZs, with a **load balancer** distributing traffic across all of them. If one AZ fails, the load balancer detects the unhealthy instances (via health checks) and stops sending traffic to them, continuing to serve requests from the instances in the surviving AZ(s). Paired with an **Auto Scaling group**, AWS can even launch replacement instances in the healthy AZs automatically.

---

**Q15: If Multi-AZ design is clearly better for availability, why isn't it the default for every workload? What are the two honest trade-offs?** *[AWS Global Infrastructure]*

A: **Cost** — running redundant resources across multiple AZs costs more than running a single copy. **Complexity** — keeping data consistent across locations is genuinely harder than keeping it in one place. For a low-stakes internal tool, a single AZ might be a reasonable trade-off; for anything customer-facing or business-critical, the cost of downtime usually outweighs the cost of redundancy.

## Service Models & Shared Responsibility

**Q16: What's the core difference between IaaS, PaaS, and SaaS in terms of what you're handed versus what you have to build yourself?** *[Service Models & Shared Responsibility]*

A: **IaaS** hands you raw infrastructure (VMs, storage, networking) with no application-level structure — you decide everything from the OS up. **PaaS** hands you a managed environment for running your code — you provide the application, the platform handles provisioning, scaling, and OS maintenance. **SaaS** hands you a complete, ready-to-use application — you just use it, with no infrastructure, platform, or code to manage at all.

---

**Q17: What do you give up, and what do you gain, moving from IaaS to PaaS? Use Lambda as your example.** *[Service Models & Shared Responsibility]*

A: You give up low-level control — with Lambda, there's no server to SSH into because there isn't one you manage. You gain AWS taking on the operational burden of provisioning, scaling, and OS maintenance: you just write a function, and AWS handles running it, including scaling it to zero when it's not in use. You never have to patch it, but you also can't reach into the underlying infrastructure.

---

**Q18: What's the shorthand distinction between "security of the cloud" and "security in the cloud," and which side is AWS responsible for?** *[Service Models & Shared Responsibility]*

A: **"Security of the cloud"** is AWS's side — securing the physical infrastructure, hardware, and the virtualization layer that isolates customers from each other. **"Security in the cloud"** is the customer's side — securing what they build and configure on top of that platform (data, access control, application code). AWS secures the platform; the customer secures their use of it.

---

**Q19: In the S3 bucket example, why is a misconfigured public bucket the customer's fault and not AWS's, even though it's "AWS's product"?** *[Service Models & Shared Responsibility]*

A: AWS guarantees the physical durability and infrastructure security of S3 — that's "security of the cloud," and it's solid regardless of configuration. But *who can access* the bucket's contents is a permissions setting the customer controls — "security in the cloud." AWS built a secure vault; if the customer configures the door to be left open, that misconfiguration is on them, not a failure of AWS's underlying platform.

---

**Q20: Why does the customer's share of responsibility shrink as you move from IaaS toward SaaS? What's the underlying logic?** *[Service Models & Shared Responsibility]*

A: AWS can only be responsible for the layers it actually controls. As AWS takes over more layers of the stack (the OS, the runtime, the application platform itself), those layers move out of customer responsibility and into AWS's — so the customer's remaining slice necessarily shrinks as AWS's slice grows.

---

**Q21: Compare EC2 and RDS: what does AWS take over with RDS that it doesn't with EC2, and what does the customer still own in both cases?** *[Service Models & Shared Responsibility]*

A: With **EC2**, AWS stops at "here is a working virtual machine" — the customer patches the guest OS, configures security groups, and manages any installed software. With **RDS**, AWS additionally manages the underlying OS, installs and patches the database engine, and handles automated backups/failover (if Multi-AZ is chosen). In both cases, though, the customer still owns what data goes in, how access to it is configured, and — for RDS specifically — AWS still isn't responsible for a poorly written query or an overly permissive database user the customer created.

---

**Q22: What's still the customer's job even in a fully-managed SaaS product, if AWS is managing the entire stack?** *[Service Models & Shared Responsibility]*

A: Managing which of their own users have accounts, what data they choose to put into the system, and configuring whatever access controls the application exposes to them. The customer isn't securing infrastructure at all anymore — but they're still responsible for how they use the finished product.

## Cost Awareness

**Q23: Why does the fact that "the Free Tier doesn't cap what we can spend" matter? What's the danger it's warning about?** *[Pricing & Cost Management]*

A: The **Free Tier** discounts usage up to a defined limit — it doesn't block or prevent usage beyond that limit. It's entirely possible to use a service in good faith, exceed a limit without realizing it, and get charged normal rates for the overage. The danger is assuming "Free Tier" means "no bill is possible," when it really means "no bill *as long as usage stays within specific, per-service limits*."

## Using the Console

**Q24: In what sense are "folders" in the S3 console not real folders? What's actually going on under the hood?** *[AWS Management Console]*

A: **Object storage** has no true concept of nested directories — what looks like a folder in the console is really just object keys that share a common prefix (like `images/`) being *displayed* as if grouped together. There's no actual folder object; it's a naming convention the console renders visually. This matters later when working with S3 programmatically, since an operation that looks like "moving a folder" is really a batch of operations on individually named objects that happen to share a prefix.

---

**Q25: Why is the permissions tab singled out as "the single most important tab to understand early" for an S3 bucket?** *[AWS Management Console]*

A: Because S3 buckets can be configured for public access, and a misconfigured permissions setting is one of the most common real-world cloud security mistakes (echoing the Shared Responsibility lesson's public-bucket example). The consequences of getting this tab wrong are real and common enough that it's worth deliberately reviewing on every bucket created, rather than accepting default settings without looking.
