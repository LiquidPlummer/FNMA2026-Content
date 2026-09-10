# Application Load Balancers

An **Application Load Balancer (ALB)** operates at layer 7 — it understands HTTP. That lets it route on the content of a request, not just distribute connections.

---

## The Components

Three objects, and requests flow through them in order:

**Listeners** check for connections on a protocol and port. An ALB typically has a listener on 443 for HTTPS, and often one on 80 that redirects to it.

**Rules** belong to a listener and decide what happens to a request, matched in priority order. Each rule has conditions and an action.

**Target groups** hold the destinations — instances, IP addresses, Lambda functions, or another ALB — and own the health check configuration.

```
Listener :443
├── Rule 10: path /api/*   ──► target group: api-servers
├── Rule 20: host img.*    ──► target group: image-servers
└── Default rule           ──► target group: web-servers
                                  ├── i-0aaa (AZ-a)  healthy
                                  ├── i-0bbb (AZ-b)  healthy
                                  └── i-0ccc (AZ-c)  unhealthy — no traffic
```

*Rules are evaluated in priority order, with the default rule catching anything unmatched. Only healthy targets receive requests.*

---

## Routing

Because the ALB parses HTTP, rules can match on:

- **Path** — `/api/*` to one group, `/static/*` to another
- **Host header** — `api.example.com` and `www.example.com` on one balancer
- **HTTP method**, **query string**, **source IP**, or **request headers**

Actions include forwarding to a target group, redirecting (a permanent HTTP-to-HTTPS redirect is a rule, not a backend concern), returning a fixed response, or requiring authentication via Cognito or OIDC.

This is what layer 7 buys. A single ALB can front several services, handle TLS termination, redirect HTTP, and return maintenance pages — work that would otherwise sit in application code or a separate proxy.

---

## Target Groups

A target group has a **target type**, and the choice has consequences:

- **`instance`** — registers EC2 instances by ID. Traffic goes to the instance's primary private IP.
- **`ip`** — registers IP addresses directly. Required for targets in a peered VPC, on-premises over VPN, or for ECS tasks in `awsvpc` mode.
- **`lambda`** — invokes a function per request.
- **`alb`** — forwards to another load balancer, used when an NLB fronts an ALB.

Registration is usually automatic: an Auto Scaling group attached to a target group registers and deregisters instances as it launches and terminates them.

---

## Cross-Zone Load Balancing

An ALB has nodes in each subnet it is configured with. **Cross-zone load balancing is enabled by default and cannot be disabled on an ALB** — every node can send to targets in any AZ.

This means traffic distributes evenly regardless of how many targets are in each zone. It also means requests routinely cross AZ boundaries, incurring cross-AZ data transfer charges, which is one of the steady costs of a multi-AZ deployment.

The ALB must be configured with **at least two subnets in different AZs**. That is a hard requirement, and it is why the public subnet layout needs at least two zones.

---

## Practical Notes

**The ALB's DNS name is the stable address.** Its IP addresses change as AWS scales it, so nothing should ever resolve and cache them. Route 53 alias records point at the ALB by name, at no charge.

**Security groups chain.** The ALB has its own security group; the targets' security group should allow traffic from the ALB's group rather than from a CIDR range. This is the tiered pattern from the networking lesson.

**Client IPs arrive in a header.** Because the ALB terminates the connection, targets see the ALB's IP as the source. The original client address is in `X-Forwarded-For`, and applications that log or rate-limit by IP must read it from there.

**Access logs are off by default.** Enabling them writes detailed per-request logs to S3, which is the main source of data for latency and error analysis.

**Deletion protection** prevents accidental removal of a load balancer that many things depend on.

---

## Cost

Two components: an hourly charge of roughly $16–20/month, plus **Load Balancer Capacity Units (LCUs)**, which meter new connections, active connections, processed bytes, and rule evaluations. The dominant dimension varies by workload — high request rates drive connection metrics, large downloads drive bytes.

For low-traffic services the hourly charge dominates, which makes running many lightly used ALBs relatively expensive. Consolidating several services behind one ALB using host- and path-based rules is the usual response.

---

## Key Takeaways

- An ALB works at layer 7, with listeners accepting connections, rules routing requests, and target groups holding destinations.
- Rules match on path, host, method, headers, query string, or source IP, and can forward, redirect, return fixed responses, or authenticate.
- Target types include instance, IP, Lambda, and another ALB; IP targets are required for peered VPCs and `awsvpc` ECS tasks.
- Cross-zone load balancing is always on for ALBs, which distributes evenly and generates cross-AZ transfer charges.
- An ALB requires subnets in at least two Availability Zones.
- Use the ALB's DNS name, never its IP addresses, and read client IPs from `X-Forwarded-For`.
- Cost is hourly plus capacity units, so consolidating low-traffic services behind one ALB is usually cheaper.
