# Inbound vs Outbound Firewall Rules

Subnets and routing (from the previous topic) determine whether a network *path* exists at all. Firewall rules determine what's actually allowed to travel along that path, once it exists. Every firewall rule in AWS networking is defined in one of two directions, and keeping that distinction clear is fundamental to understanding how traffic is controlled.

## Inbound Rules

**Inbound rules** govern traffic *coming into* a resource — a request arriving at an EC2 instance, for example. If we want a web server to be reachable, we need an inbound rule permitting traffic on whatever port it listens on (commonly the ports used for standard web traffic). Without that inbound rule, requests arriving at the instance are simply dropped, regardless of whether a network path to reach it exists.

Inbound rules are the more commonly discussed half of firewall configuration, because forgetting one produces an obvious, immediate symptom: "I can't connect to my server." A brand-new security group, as mentioned back in the EC2 lesson, typically denies all inbound traffic by default — nothing gets in until we explicitly say otherwise.

## Outbound Rules

**Outbound rules** govern traffic *leaving* a resource — a server making a request out to a database, calling an external API, or downloading a software update. It's easy to forget these exist, because many default configurations allow all outbound traffic, which means outbound rules often "just work" without anyone thinking about them.

That default openness isn't automatic everywhere, though, and it's not necessarily the right posture for a sensitive workload either. In a security-conscious design, restricting outbound traffic matters too — if a server is compromised, unrestricted outbound access could let an attacker exfiltrate data or communicate with an external system they control. Deliberately restricting outbound rules to only the specific destinations a resource legitimately needs to reach is a real, underused hardening technique.

## Why Both Directions Matter Independently

The key insight is that inbound and outbound rules are **evaluated independently** — allowing inbound traffic on a port says nothing about what's allowed to leave, and vice versa. A resource can be wide open to inbound requests but heavily restricted on what it's allowed to send out, or the reverse. Thinking through both directions separately, rather than assuming "traffic can get in, so it must be able to get out too" (or the reverse), is essential to reasoning correctly about what a given configuration actually permits.

## A Concrete Example

Consider a web server that needs to accept requests from the public internet and query a database in a private subnet:

- **Inbound**, on the web server: allow traffic on the web server's port, from anywhere (or from a load balancer).
- **Outbound**, on the web server: allow traffic to the database's port, destined for the database's private subnet.
- **Inbound**, on the database: allow traffic on the database's port, but *only* from the web server (not from anywhere).
- **Outbound**, on the database: typically nothing needs to be explicitly opened, since a database usually doesn't need to initiate its own outbound connections.

Notice that each resource's rules are scoped as narrowly as the workload actually requires — this is the least privilege principle from the IAM lesson, applied to network traffic instead of permissions. The specific mechanism AWS uses to enforce these rules, and an important nuance in how inbound and outbound rules interact with each other, is the subject of the next topic.
