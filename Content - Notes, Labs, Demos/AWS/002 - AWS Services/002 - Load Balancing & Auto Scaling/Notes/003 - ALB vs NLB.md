# ALB vs NLB

AWS offers two general-purpose load balancers. The **ALB** works at layer 7 and understands HTTP; the **NLB** works at layer 4 and forwards TCP and UDP. The difference decides which one a workload needs.

---

## The Core Difference

**An ALB terminates the connection.** It accepts a TCP connection, parses the HTTP request, applies rules, and opens its own connection to a target. It is a proxy, and it can act on request content.

**An NLB forwards packets.** It selects a target and passes traffic through with minimal processing. It does not see HTTP because it does not parse it.

That single distinction produces every other difference between them.

---

## The Comparison

| | ALB | NLB |
|---|---|---|
| Layer | 7 (HTTP/HTTPS) | 4 (TCP/UDP/TLS) |
| Routing on content | Yes — path, host, headers | No |
| Protocols | HTTP, HTTPS, gRPC | TCP, UDP, TLS |
| Latency | Higher — parses requests | Very low |
| Throughput | High | Extremely high |
| Client IP preserved | No — in `X-Forwarded-For` | **Yes, by default** |
| Static IP | No | **Yes — one per AZ** |
| Elastic IP support | No | Yes |
| WebSockets | Yes | Yes |
| Lambda targets | Yes | No |
| Cross-zone | Always on, free | Off by default, **charged** |
| Authentication | Cognito, OIDC | No |

---

## When the Differences Matter

**Static IPs.** An NLB has one IP per Availability Zone, and Elastic IPs can be assigned. This matters when a client's firewall requires a fixed destination address — a common requirement with enterprise and financial partners who will not allow a DNS name in a rule. An ALB's addresses change, so it cannot satisfy that.

**Client IP preservation.** An NLB passes the original source address through, so the application sees the real client IP with no header parsing. For non-HTTP protocols with no header mechanism, this is the only way to get it.

**Non-HTTP protocols.** A game server on UDP, a database proxy, an MQTT broker — none of these are HTTP, so the ALB cannot serve them.

**Extreme performance.** An NLB handles millions of requests per second at very low latency, since it does not parse anything.

**Content routing.** Only the ALB can route `/api/*` to one service and `/static/*` to another, or serve several hostnames from one balancer.

---

## Cross-Zone Behavior

An important operational difference.

**On an ALB**, cross-zone load balancing is always on and free. Traffic distributes evenly across all targets regardless of zone.

**On an NLB**, it is **off by default**, and enabling it incurs data transfer charges. With it off, each NLB node sends only to targets in its own AZ — so if one zone has two targets and another has eight, the two receive the same total share as the eight. Uneven target distribution therefore produces uneven load, which is a real source of confusion when troubleshooting an overloaded instance.

Keeping target counts balanced across AZs matters more for an NLB than for an ALB.

---

## Choosing

**Use an ALB** for HTTP and HTTPS applications, which covers most web workloads. Content routing, TLS termination, authentication integration, and Lambda targets are all reasons.

**Use an NLB** when a static IP is required, when the original client IP must arrive without headers, when the protocol is not HTTP, or when latency and throughput requirements exceed what an ALB provides.

**Use both together** for the case that needs a static IP *and* content routing: an NLB with Elastic IPs in front, forwarding to an ALB behind it. This adds a hop and cost, and it is the standard answer when a partner requires a fixed address for an HTTP service.

---

## The Third Option

The **Gateway Load Balancer (GWLB)** exists for a narrower purpose: inserting third-party network appliances — firewalls, intrusion detection, deep packet inspection — transparently into a traffic path. It operates at layer 3 and is encountered mainly when deploying a vendor security appliance, not when load balancing an application.

The **Classic Load Balancer** is the previous generation and should not be used for new work.

---

## Key Takeaways

- An ALB terminates connections and parses HTTP; an NLB forwards packets at layer 4 without inspecting them.
- Only the ALB can route on path, host, or headers, or target Lambda functions.
- Only the NLB provides static IPs, supports Elastic IPs, and preserves the client's source address by default.
- NLBs handle non-HTTP protocols including UDP, and offer lower latency and higher throughput.
- Cross-zone load balancing is always on and free for ALBs, off by default and charged for NLBs.
- With NLB cross-zone off, uneven target counts across AZs produce uneven load.
- Combine an NLB in front of an ALB when both a static IP and content-based routing are required.
