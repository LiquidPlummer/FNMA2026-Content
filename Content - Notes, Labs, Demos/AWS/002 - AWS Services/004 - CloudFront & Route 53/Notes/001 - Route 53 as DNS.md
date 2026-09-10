# Route 53 as DNS

**Route 53** is AWS's DNS service. Most of it is ordinary DNS; the parts worth attention are hosted zones, TTL behavior, and alias records — which are a Route 53 invention that solves a real problem.

---

## Hosted Zones

A **hosted zone** is a container for the DNS records of one domain.

**Public hosted zones** answer queries from the internet. Creating one gives four **name servers**, and the domain's registrar must be updated to delegate to them. Until that delegation exists, the zone answers nothing — a zone can be perfectly configured and completely inactive because the registrar still points elsewhere.

**Private hosted zones** answer only from associated VPCs. This is how internal names like `db.internal.example.com` resolve inside a VPC and nowhere else. The same domain can have both a public and a private zone, with VPC clients getting private answers and the internet getting public ones — a split-horizon arrangement that is useful and easy to confuse yourself with.

Hosted zones are billed monthly (about $0.50) plus per query, so abandoned zones for domains no longer in use are a small recurring cost.

---

## Record Types

| Type | Purpose |
|---|---|
| `A` | Name to IPv4 address |
| `AAAA` | Name to IPv6 address |
| `CNAME` | Name to another name |
| `MX` | Mail servers |
| `TXT` | Arbitrary text — domain verification, SPF, DKIM |
| `NS` | Delegation to name servers |
| `SOA` | Zone metadata |
| `ALIAS` | Route 53-specific; covered below |

**`CNAME` cannot exist at the zone apex.** The apex is the bare domain — `example.com` with no subdomain. DNS requires `NS` and `SOA` records there, and a `CNAME` cannot coexist with other records.

This is a genuine problem, because the natural thing to want is `example.com` pointing at a load balancer, and a load balancer has a DNS name rather than a fixed IP. Standard DNS offers no solution. Alias records are Route 53's answer.

---

## TTL

**Time To Live** tells resolvers how long to cache an answer. It is the single most consequential setting during a change.

A record with a 3,600-second TTL may be served from caches for an hour after it is changed. Resolvers that cached the old value do not check back until it expires, so a failover or migration is not complete when the record is updated — it is complete when the last cache expires.

The practical approach:

**Lower the TTL before a planned change.** Reduce to 60 seconds, wait for the old TTL to expire, then make the change. Raise it again afterwards.

**Keep TTLs low on records that fail over**, typically 60 seconds.

**Keep TTLs higher on stable records** — 3,600 seconds or more — since each query is billed and cached answers resolve faster.

**Alias records to AWS resources have no configurable TTL.** Route 53 manages it, which removes the question for the common case.

---

## Domain Registration

Route 53 can also register domains, which keeps registration and DNS in one place and configures delegation automatically. Registration is a separate function from hosting — a domain registered elsewhere can use Route 53 for DNS by updating the registrar's name server records, which is a common arrangement.

---

## Health Checks

Route 53 health checks monitor an endpoint from multiple global locations and can remove failing records from DNS answers. They are what makes DNS-based failover work, and they are covered with routing policies.

---

## Practical Notes

**Changes propagate quickly within Route 53** — usually within seconds — but client-side caching is governed by TTL, which is what actually determines when a change takes effect. "DNS propagation" delays are almost always cached TTLs rather than Route 53 being slow.

**Query logging** to CloudWatch Logs records queries against a public zone. Useful for diagnosing resolution problems and for seeing what is actually being asked for.

**Prefer alias records to AWS resources**, since alias queries to AWS targets are free while standard queries are billed.

---

## Key Takeaways

- A hosted zone holds a domain's records; a public zone answers the internet only once the registrar delegates to its name servers.
- Private hosted zones resolve only within associated VPCs, enabling internal-only names.
- `CNAME` cannot exist at the zone apex, which is why alias records exist.
- TTL controls how long resolvers cache an answer and determines when a change actually takes effect.
- Lower TTLs before planned changes, keep them low on failover records and higher on stable ones.
- Alias records to AWS resources have TTL managed by Route 53 and are not billed per query.
- Slow-seeming DNS changes are almost always cached TTLs rather than Route 53 propagation.
