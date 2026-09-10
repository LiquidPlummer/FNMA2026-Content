# Data Transfer and Global Services

Two loose ends from the region and AZ model: what it costs to move bytes across those boundaries, and the small set of services that ignore the boundaries entirely.

---

## Data Transfer as a Line Item

Moving data between AWS locations is metered. The rates are individually small and the volumes are not, which is why data transfer is routinely a top-three line on a bill and almost never in anyone's estimate.

The general shape, most to least expensive per gigabyte:

| Path | Charged? |
|---|---|
| Out to the internet | Yes — the most expensive common path |
| Between regions | Yes, per GB |
| Between AZs in a region | Yes, **in both directions** |
| Within one AZ, private IPs | Free |
| In from the internet | Free |

Four points do most of the work here.

**Inbound is free, outbound is not.** Uploading a terabyte to S3 costs nothing in transfer; downloading it costs real money. Architectures that pull large volumes out of AWS repeatedly are the ones that get expensive.

**Cross-AZ is billed both ways.** An instance in `us-east-1a` querying a database in `us-east-1b` is charged for the request and the response. Continuous chatter between tiers spread across AZs accumulates steadily.

**Private IPs matter.** Traffic between instances in the same AZ is free over private IPs, and charged if it routes over public IPs — even between two instances in the same VPC. Addressing resources by their private DNS name is a real cost decision, not a style preference.

**NAT gateways charge for processing.** Every gigabyte through a NAT gateway carries a processing fee on top of the hourly charge, and on top of any transfer charge. Private subnets pulling large packages from the internet pay for it, which is a common reason to use VPC endpoints for S3 instead.

---

## Global Services

Most AWS services are region-scoped. A handful are not — they have a single global namespace and a control plane that lives outside any one region, usually in `us-east-1`.

**IAM.** Users, roles, and policies are account-wide. A role created once works in every region.

**Route 53.** DNS is inherently global; hosted zones are not region-scoped.

**CloudFront.** A distribution serves from edge locations worldwide rather than from one region.

**S3 (partly).** The bucket *namespace* is global — no two accounts anywhere can hold the same bucket name — but a bucket's data lives in one region. This split is a frequent source of confusion: global name, regional storage.

**WAF and Shield**, when attached to CloudFront, are configured globally.

### The `us-east-1` catch

Because several global services keep their control plane in `us-east-1`, some operations must be performed there regardless of where the workload runs:

- ACM certificates for CloudFront **must** be requested in `us-east-1`. A certificate in another region cannot be attached to a distribution, and this stops people regularly.
- CloudFront and Route 53 API calls are made against `us-east-1` endpoints.
- Some global service metrics appear only in `us-east-1` CloudWatch, so alarms for them must be created there.

---

## Key Takeaways

- Data transfer out to the internet, between regions, and between AZs is billed per gigabyte; inbound from the internet is free.
- Cross-AZ traffic is charged in both directions and accumulates continuously between chatty tiers.
- Traffic over private IPs within an AZ is free, while the same traffic over public IPs is not.
- NAT gateway processing charges apply per gigabyte on top of hourly and transfer costs.
- IAM, Route 53, CloudFront, and the S3 bucket namespace are global; S3 object data is still regional.
- Several global services are controlled from `us-east-1` — notably, CloudFront's ACM certificate must be issued there.
