# Data Transfer

Data transfer is the cost that surprises people, for a consistent reason: it does not appear on any resource. There is no "data transfer" object to look at, no dashboard showing it accumulating, and no obvious owner. It arrives at the end of the month as a large line nobody planned.

---

## The Rules

**Inbound from the internet is free.** Uploading to AWS costs nothing.

**Outbound to the internet is the most expensive path**, roughly $0.09/GB in most regions, with volume tiers reducing it at large scale.

**Cross-region transfer is charged**, roughly $0.02/GB.

**Cross-AZ transfer is charged in both directions**, roughly $0.01/GB each way — so $0.02/GB for a request and its response.

**Within one AZ over private IPs is free.** The same traffic over public IPs is not, even between instances in the same VPC.

```
Internet ──free──►  AWS  ──$0.09/GB──►  Internet

AZ-a  ◄──$0.01/GB each direction──►  AZ-b

instance ──free (private IP)──► instance    (same AZ)
instance ──charged (public IP)──► instance  (same AZ)
```

*The asymmetry between free inbound and expensive outbound shapes which architectures are cheap and which are not.*

---

## Where the Charges Accumulate

**Serving content directly from S3 or EC2.** A site serving 10 TB/month of images directly pays roughly $900/month in transfer. Through CloudFront the rate is lower and origin fetches are reduced by caching, often cutting it substantially.

**Chatty cross-AZ traffic.** Application servers spread across three AZs talking to a database in one AZ send most of their traffic across zone boundaries. Individually tiny, continuously accumulating.

**NAT gateway processing.** Every gigabyte through a NAT gateway carries a processing charge on top of any transfer charge. Private subnets pulling container images or packages from the internet pay both.

**Cross-region replication.** S3 replication, RDS cross-region read replicas, and DynamoDB global tables all move data continuously and charge for it.

**Data egress to another cloud or on-premises.** Full-rate internet egress unless Direct Connect is in place.

---

## Reducing It

**CloudFront for anything public.** Lower per-gigabyte rates than direct transfer, plus caching that avoids repeat origin fetches. Transfer from AWS origins into CloudFront is free.

**VPC endpoints for AWS services.** S3 and DynamoDB gateway endpoints are free and remove both NAT processing and transfer charges for that traffic.

**AZ-aware placement.** Keeping a request's path within one AZ eliminates cross-AZ charges. Some load balancing and service-mesh configurations support zone-aware routing for exactly this reason.

**Private IPs and private DNS names.** Free within an AZ; the same traffic over public addresses is charged. This is why addressing resources by private DNS matters.

**Compression.** Gzip or Brotli on responses reduces billed bytes directly, and it is usually a configuration change rather than a code change.

**S3 Bucket Keys and caching generally.** Anything that avoids a repeated fetch avoids the transfer for it.

---

## Making It Visible

The core problem is attribution: transfer charges do not name the workload that caused them.

**Cost Explorer**, grouped by usage type, distinguishes `DataTransfer-Out-Bytes`, `DataTransfer-Regional-Bytes` (cross-AZ), and NAT gateway processing. This is the fastest way to see which category dominates.

**VPC Flow Logs** identify which interfaces move the most data, which is how a specific chatty component gets found.

**Cost allocation tags** attribute transfer to a team or product, though coverage depends on the tagging being in place first.

The habit worth forming is checking the transfer categories in Cost Explorer monthly. Because transfer is not attached to a resource, nothing else surfaces it — an unexpected transfer bill has usually been growing for months.

---

## Key Takeaways

- Inbound transfer is free; outbound to the internet is the most expensive common path.
- Cross-AZ transfer is charged in both directions, and accumulates continuously between chatty tiers.
- Private-IP traffic within an AZ is free, while the same traffic over public IPs is charged.
- NAT gateway processing charges apply on top of transfer charges.
- CloudFront lowers egress rates and reduces origin fetches; transfer from AWS origins into CloudFront is free.
- Gateway VPC endpoints for S3 and DynamoDB remove both NAT and transfer charges for that traffic.
- Transfer is not attached to any resource, so review it in Cost Explorer by usage type rather than waiting for it to surface.
