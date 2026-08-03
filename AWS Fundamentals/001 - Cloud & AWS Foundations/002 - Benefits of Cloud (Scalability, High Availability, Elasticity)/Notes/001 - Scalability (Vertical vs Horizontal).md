# Scalability (Vertical vs Horizontal)

**Scalability** is a system's ability to handle more load by adding resources. When traffic grows, a scalable system grows with it instead of slowing down or falling over. There are two fundamentally different ways to add that capacity, and the choice between them shapes how an application has to be built.

## Vertical Scaling (Scaling Up)

**Vertical scaling** means making a single machine more powerful — more CPU, more RAM, faster storage. If a server is struggling under load, we resize it to a bigger instance and it can now handle more work on its own.

Vertical scaling is simple: the application doesn't need to know or care that it's running on a bigger machine, so there's usually no code change required. But it has a hard ceiling — there's a largest instance size AWS offers, and once we hit it, we're out of room. It also means a single point of failure: if that one (now very powerful) machine goes down, everything on it goes down with it.

## Horizontal Scaling (Scaling Out)

**Horizontal scaling** means adding more machines rather than making one machine bigger, and spreading the load across all of them — typically with a **load balancer** sitting in front to distribute incoming requests. Instead of one large server handling everything, we might have ten smaller servers each handling a slice of the traffic.

Horizontal scaling doesn't have the same hard ceiling — need more capacity, add more machines. It also improves fault tolerance: if one of ten servers fails, the other nine keep serving traffic while the failed one is replaced. The trade-off is complexity — the application needs to be designed to run as multiple independent copies, which usually means it can't rely on storing state (like a user's session) in the memory of a single server, since the next request might land on a different one.

## Comparing the Two

| | Vertical Scaling | Horizontal Scaling |
|---|---|---|
| **How it works** | Make one machine bigger | Add more machines |
| **Ceiling** | Limited by the largest instance size available | Effectively no ceiling |
| **Fault tolerance** | Single point of failure | One machine failing doesn't take down the system |
| **Application impact** | Usually none | Often requires designing for statelessness |
| **Typical use** | Quick fix, or workloads that are hard to parallelize (e.g., some databases) | Web applications, APIs, anything that can run as multiple copies |

## Why This Distinction Matters in AWS

AWS makes both approaches easy, but they show up differently across services. Resizing an EC2 instance to a larger type is vertical scaling. Placing multiple EC2 instances behind a load balancer, with an **Auto Scaling group** adding or removing instances based on demand, is horizontal scaling. Understanding which kind of scaling a service or architecture relies on is a recurring theme throughout AWS — it's part of why some services (like DynamoDB) are built from the ground up to scale horizontally, while others (like a traditional relational database) lean more heavily on scaling vertically.
