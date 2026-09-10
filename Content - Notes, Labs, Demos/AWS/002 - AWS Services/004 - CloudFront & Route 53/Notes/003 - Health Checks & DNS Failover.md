# Health Checks & DNS Failover

Route 53 **health checks** monitor endpoints and remove unhealthy records from DNS answers. This provides failover at the DNS layer, which reaches across regions — and comes with limitations that follow from how DNS works.

---

## How Health Checks Work

Route 53 checks an endpoint from **multiple global locations**, and an endpoint is considered healthy when enough of those locations agree.

Three kinds:

**Endpoint checks** request a URL or open a TCP connection to an IP address or domain name.

**Calculated checks** combine other health checks with boolean logic — healthy only if at least two of three components are.

**CloudWatch alarm checks** derive health from an alarm's state, which is how internal metrics like queue depth or error rate can drive DNS failover.

```bash
aws route53 create-health-check --caller-reference hc-primary-2026 \
  --health-check-config '{
    "Type": "HTTPS",
    "FullyQualifiedDomainName": "primary.example.com",
    "ResourcePath": "/health",
    "RequestInterval": 30,
    "FailureThreshold": 3
  }'
```

*Checks `/health` over HTTPS every 30 seconds, marking the endpoint unhealthy after three consecutive failures — roughly 90 seconds to detect.*

`RequestInterval` may be 10 or 30 seconds; 10 costs more. `FailureThreshold` ranges from 1 to 10.

---

## Failover Routing

The standard active/passive arrangement:

```
example.com  PRIMARY    ALIAS → us-east-1 ALB   [health check A]
example.com  SECONDARY  ALIAS → us-west-2 ALB   [health check B]
```

*The primary is returned while healthy. When its health check fails, Route 53 returns the secondary instead.*

The secondary can be a full standby environment, a scaled-down one, or a static maintenance page on S3 — the last being a cheap way to show something useful during an outage rather than a connection error.

---

## What DNS Failover Cannot Do

Being clear about the limits prevents relying on this more than it deserves.

**It is not fast.** The total time to recovery is detection time plus TTL:

```
3 failed checks × 30s  =  90s detection
+ 60s TTL              =  60s cache expiry
────────────────────────────────────────
≈ 150 seconds before clients move
```

*And that is with a low TTL. With a 3,600-second TTL, some clients continue hitting the failed endpoint for an hour.*

**Clients ignore TTLs.** Some resolvers, browsers, and JVMs cache DNS answers longer than the TTL says — the JVM's default DNS caching has historically been indefinite. Some clients keep existing connections open regardless of DNS.

**It is coarse.** DNS moves all traffic or none. It cannot shift a portion of requests, and it cannot fail over a single failing path within an otherwise healthy region.

For failover within a region, a load balancer's health checks are faster and more precise — they act on the next request rather than waiting for a cache to expire. **DNS failover is for what a load balancer cannot cover**: whole-region failure, or failing over to something entirely outside AWS.

---

## Configuring Health Checks Well

**Check a meaningful endpoint.** A check hitting `/` may succeed while the application is broken. `/health` should verify the instance can genuinely serve.

**Beware of checking too deeply.** As with load balancer health checks, an endpoint verifying a shared database causes every region to report unhealthy when that database has a problem — failing over to a region with the same issue, or to nothing.

**Allow Route 53's checker IP ranges** through security groups and firewalls. AWS publishes them, and a health check blocked by a firewall reports permanent failure.

**Use string matching** where useful — a check can require specific text in the first 5,120 bytes of the response, catching a page that returns 200 while displaying an error.

**Set alarms on health check status.** A health check flipping to unhealthy should notify someone, not only change DNS.

---

## Combining With Other Policies

Health checks attach to records under any routing policy, which is what makes multi-region deployments practical:

- **Latency-based with health checks** — send users to the nearest healthy region, skipping unhealthy ones.
- **Weighted with health checks** — split traffic while excluding failed targets.
- **Multivalue answer with health checks** — return only healthy records from the set.

**Alias records to ALBs can use `EvaluateTargetHealth`** instead of a separate health check. Route 53 then uses the load balancer's own view of its targets, which needs no additional configuration or cost and reflects what the ALB already knows.

---

## Key Takeaways

- Route 53 health checks probe endpoints from multiple locations and can also derive health from CloudWatch alarms.
- Failover routing returns a secondary record only when the primary's health check fails.
- Recovery time is detection time plus TTL, typically over two minutes even with aggressive settings.
- Some clients cache DNS beyond the TTL, so a portion of traffic persists on the failed endpoint.
- DNS failover is coarse and slow — use load balancer health checks within a region and DNS for whole-region failover.
- Avoid health endpoints that check shared dependencies, which cause all regions to fail together.
- Allow Route 53's published checker IP ranges, and alarm on health check state changes.
- For ALB alias records, `EvaluateTargetHealth` reuses the load balancer's own health assessment at no cost.
