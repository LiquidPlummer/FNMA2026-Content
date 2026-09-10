# CloudFront as a Cache

**CloudFront** is a content delivery network: a cache sitting between clients and an origin, distributed across hundreds of edge locations worldwide. Understanding it as a cache — rather than as a generic accelerator — makes its behavior predictable.

---

## The Mechanism

A request goes to the nearest edge location. If the object is cached there, it is served immediately. If not, CloudFront fetches it from the origin, stores it, and serves it — so the next request for the same object at that location is a hit.

```
Client (London) ──► Edge location (London)
                         │ hit  ──► served from cache
                         │
                         └ miss ──► regional cache ──► Origin (us-east-1)
```

*A miss at the edge checks a regional cache before reaching the origin, which reduces origin load beyond what edge caches alone provide.*

---

## Origins

An origin is where CloudFront fetches from:

- **S3 bucket** — using Origin Access Control so the bucket stays private
- **ALB** — for dynamic applications
- **API Gateway** — for APIs
- **Any HTTP server**, including outside AWS

A distribution can have several origins, with **cache behaviors** routing paths to different ones — `/api/*` to an ALB and everything else to S3, from one domain.

---

## What CloudFront Actually Provides

**Reduced latency for cacheable content.** A cache hit is served from a nearby edge rather than a distant region.

**Reduced origin load.** Cached content never reaches the origin, which reduces both compute and S3 request charges.

**Lower data transfer cost.** CloudFront's per-gigabyte rates are below direct S3 or EC2 egress, and transfer from AWS origins into CloudFront is free. For high-volume public content this alone often justifies it.

**TLS termination at the edge**, with a free ACM certificate. The TLS handshake completes near the user rather than across an ocean, which is a real latency saving even for uncacheable content.

**A shield in front of the origin.** The origin need not be publicly reachable, and AWS Shield Standard DDoS protection is included.

**AWS WAF integration** for request filtering at the edge.

---

## What It Does Not Provide

**It does not speed up uncacheable dynamic content by much.** A personalized API response has to reach the origin regardless. The saving is the TLS handshake and AWS's backbone network for the origin fetch — real, but modest compared with a cache hit.

**It does not fix a slow origin.** Cache misses are as slow as the origin, plus a hop.

**It is not a load balancer.** It distributes by geography, not by load. An ALB still sits behind it.

---

## Cache Hit Ratio

The metric that determines whether CloudFront is doing anything useful. A distribution with a 20% hit ratio is passing 80% of requests to the origin and providing little.

What lowers it:

**Caching disabled by headers.** `Cache-Control: no-cache` from the origin means nothing is cached. This is the most common cause, and it is often inherited from a framework default.

**Too many cache key components.** The cache key determines what counts as the same object. Including a header, cookie, or query string that varies per user creates a separate cache entry per user, so nothing is ever reused.

**Short TTLs.** A 60-second TTL on content that changes daily throws away usable cache entries.

**Highly personalized content.** Genuinely per-user responses are not cacheable, which is a property of the content rather than a misconfiguration.

`CacheHitRate` is published as a CloudFront metric and is the first thing to check when CloudFront seems not to be helping.

---

## Practical Notes

**Compression.** CloudFront can compress responses automatically, reducing transfer cost and improving load times. It is enabled per cache behavior and applies to compressible content types.

**HTTP/2 and HTTP/3** are supported and improve performance for pages with many resources.

**Origin failover** allows a second origin used when the first returns errors — a form of redundancy independent of Route 53.

**Standard logs** to S3 and **real-time logs** to Kinesis provide per-request detail, which is what most CDN analysis depends on.

**Price classes** limit which edge locations are used. Restricting to North America and Europe lowers cost at the expense of performance elsewhere — appropriate when the audience is regional.

---

## Key Takeaways

- CloudFront caches content at edge locations, fetching from an origin on a miss and checking a regional cache in between.
- Origins can be S3, an ALB, API Gateway, or any HTTP server, with cache behaviors routing paths to different origins.
- It reduces latency for cacheable content, cuts origin load, lowers transfer cost, and terminates TLS near the user.
- Uncacheable dynamic content benefits mainly from TLS termination and backbone routing, not from caching.
- Cache hit ratio determines whether it is helping; `no-cache` headers and over-specific cache keys are the usual causes of a low one.
- Transfer from AWS origins into CloudFront is free, and CloudFront egress rates are below direct S3 or EC2 egress.
- Enable compression, and use price classes to trade global coverage for cost when the audience is regional.
