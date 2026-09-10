# What You Are Billed For

AWS bills four kinds of thing: **compute time, storage, requests, and data transfer.** Nearly every line on a bill is one of these, and knowing which ones a service charges for is how a design's cost becomes predictable.

---

## The Four Categories

**Compute time** — how long something ran, scaled by how large it was. EC2 bills per second with a one-minute minimum, scaled by instance size. Lambda bills per millisecond, scaled by allocated memory. RDS bills per hour of instance time.

**Storage** — how much data is held, over time, in gigabyte-months. Storing 100 GB for half a month costs 50 GB-months.

**Requests** — how many API operations were performed. S3 charges per `GET` and `PUT`, DynamoDB per read and write unit, API Gateway per request, Lambda per invocation.

**Data transfer** — how many bytes moved, and across which boundary.

---

## Provisioned vs Consumed

The distinction that catches people out most often: some services charge for what we **allocated**, others for what we **used**.

| Service | Basis |
|---|---|
| EBS volume | **Provisioned** — a 500 GB volume with 4 GB on it costs 500 GB |
| S3 | Consumed — only bytes actually stored |
| RDS storage | **Provisioned** |
| DynamoDB (on-demand) | Consumed — per request |
| DynamoDB (provisioned) | **Provisioned** — capacity units whether used or not |
| Lambda | Consumed — per invocation and duration |
| Elastic IP | **Provisioned** — billed while allocated, attached or not |

Provisioned resources cost the same whether busy or idle. That is what makes over-provisioning expensive and makes right-sizing worthwhile.

---

## The Shape by Service

**EC2:** instance-seconds by type and size, plus EBS volumes (provisioned), plus data transfer. Stopping an instance stops the compute charge; **the EBS volume keeps billing**, which is why stopped instances are not free.

**S3:** storage by class, plus requests, plus retrieval charges for the colder classes, plus transfer out. For workloads with many small objects, request charges can exceed storage charges.

**Lambda:** invocations, plus gigabyte-seconds of duration. Doubling memory doubles the per-millisecond rate — but often more than halves the duration, so it can be cheaper as well as faster.

**RDS:** instance-hours (doubled under Multi-AZ), plus provisioned storage, plus I/O on some storage types, plus backup storage beyond the free allotment, plus transfer.

**DynamoDB:** read and write units, plus storage, plus optional features like backups and streams.

---

## The Free-Tier Illusion

Small experiments cost almost nothing, which creates a poor intuition for what production costs. A `t3.micro` is roughly $7.50/month; an `m5.2xlarge` running continuously is around $280/month. Scale changes the arithmetic entirely, and a design that seems cheap at test volume can be expensive at production volume.

---

## Estimating Before Building

The useful habit is to enumerate the four categories for each component:

```
Component: image processing service
  Compute   Lambda,  2M invocations/month × 1024 MB × 800 ms  ≈  $28
  Storage   S3,      500 GB standard                          ≈  $12
  Requests  S3,      2M PUT + 4M GET                          ≈  $12
  Transfer  1 TB out to internet                              ≈  $92
                                                       total  ≈  $144
```

*Working through each category makes the dominant cost visible — here, data transfer out is nearly two thirds of the total and would be the first thing to address.*

The AWS Pricing Calculator does this arithmetic, but the value is in enumerating the categories, because the surprising costs are usually in a category nobody considered — most often transfer or requests.

---

## Key Takeaways

- Nearly every charge is compute time, storage, requests, or data transfer.
- Some services bill provisioned capacity (EBS, RDS storage, Elastic IPs) and others bill consumption (S3, Lambda, on-demand DynamoDB).
- A stopped EC2 instance still bills for its EBS volumes.
- Request charges can exceed storage charges for workloads with many small objects.
- Lambda memory affects both the per-millisecond rate and the duration, so more memory is sometimes cheaper.
- Free-tier experience is a poor guide to production cost; the arithmetic changes with scale.
- Estimate by enumerating all four categories per component — the surprises are usually in transfer or requests.
