# Alias Records & Routing Policies

Two Route 53 features that go beyond ordinary DNS: **alias records**, which solve the apex problem for AWS targets, and **routing policies**, which return different answers to different queries.

---

## Alias Records

An **alias record** is a Route 53 extension that points a name at an AWS resource. To a client it looks like an ordinary `A` record — Route 53 resolves the target and returns its addresses.

```
example.com          ALIAS → my-alb-1234.us-east-1.elb.amazonaws.com
www.example.com      ALIAS → d111111abcdef8.cloudfront.net
assets.example.com   ALIAS → my-bucket.s3-website-us-east-1.amazonaws.com
```

*Each returns addresses directly, so no second lookup is required and the apex record is legal.*

What alias records provide over a `CNAME`:

**They work at the zone apex.** `example.com` can point at a load balancer, which plain DNS cannot express.

**They are free to query.** Standard record queries are billed; alias queries to AWS targets are not.

**They track the target automatically.** When an ALB's IP addresses change — which they do — the alias follows without any record update.

**They resolve in one lookup.** A `CNAME` requires the client to resolve the target separately.

**They integrate with health checks**, so a failing target can be removed from answers.

Alias targets are limited to AWS resources: ALBs and NLBs, CloudFront distributions, S3 website endpoints, API Gateway, Elastic Beanstalk environments, VPC endpoints, Global Accelerator, and other records in the same zone. For anything outside AWS, a `CNAME` is still required — and still cannot sit at the apex.

**Use an alias whenever the target is an AWS resource.** There is no case where a `CNAME` to an AWS target is preferable.

---

## Routing Policies

A routing policy decides which answer Route 53 returns when several records share a name.

**Simple.** One record, one answer. The default.

**Weighted.** Distributes across records by assigned weight:

```
api.example.com  →  weight 90  →  ALIAS  current-alb
api.example.com  →  weight 10  →  ALIAS  new-alb
```

*Sends roughly 10% of resolutions to the new target — the mechanism for canary releases and blue/green traffic shifting. Weight 0 removes a target without deleting the record.*

**Latency-based.** Returns the region with the lowest measured latency to the resolver. Requires resources in several regions, each with a record specifying its region. Latency is measured by AWS from real observations, not computed from geography.

**Geolocation.** Routes by the resolver's location — continent, country, or US state. Used for regulatory data routing, localized content, and licensing restrictions. **Always configure a default record** for locations that match nothing, or those queries get no answer.

**Geoproximity.** Routes by geographic distance with an adjustable bias to expand or shrink a resource's catchment area. More granular than geolocation and less common.

**Failover.** A primary and a secondary record, where the secondary is returned only when the primary's health check fails.

**Multivalue answer.** Returns up to eight healthy records at random. A rough approximation of load balancing with health checking, useful where a real load balancer is not warranted — but it is not a load balancer, since clients choose and cache their own answer.

---

## Choosing

| Goal | Policy |
|---|---|
| One target | Simple |
| Gradual rollout, A/B split | Weighted |
| Serve users from the nearest region | Latency-based |
| Regulatory or licensing routing | Geolocation |
| Active/passive disaster recovery | Failover |
| Basic redundancy without a load balancer | Multivalue answer |

Policies combine. A common multi-region arrangement uses latency-based routing to pick the nearest region, with each region's record backed by a health check so an unhealthy region is skipped in favor of the next-nearest.

---

## Practical Notes

**All records with one name must use the same policy type.** Mixing weighted and latency records for the same name is not permitted.

**Health checks are what make routing dynamic.** Without them, failover and latency policies still return records for regions that are down.

**TTL still governs client behavior.** A weighted split changes which answers are handed out, but clients holding a cached answer keep using it until it expires. DNS-based traffic shifting is gradual, not immediate.

---

## Key Takeaways

- Alias records point at AWS resources, work at the zone apex, are free to query, and track target address changes automatically.
- Prefer alias over `CNAME` for every AWS target; `CNAME` remains necessary for non-AWS targets and still cannot sit at the apex.
- Weighted routing splits traffic by proportion and is the mechanism for canary and blue/green releases.
- Latency-based routing uses measured latency rather than geography; geolocation routes by resolver location and needs a default record.
- Failover routing returns a secondary only when the primary's health check fails.
- All records sharing a name must use the same routing policy type.
- Health checks make routing dynamic, and TTL means DNS-based shifts are gradual rather than immediate.
