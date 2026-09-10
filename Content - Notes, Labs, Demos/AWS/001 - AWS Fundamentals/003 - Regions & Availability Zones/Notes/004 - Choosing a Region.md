# Choosing a Region

Region choice is made once, early, and is expensive to reverse — nothing migrates automatically, and most resources cannot move at all. Four factors decide it, and they can conflict.

---

## Latency

Physical distance sets a floor on round-trip time that no amount of engineering removes. A user in London talking to `us-east-1` pays roughly 80–90 ms per round trip, before the application does any work. If a page load involves several sequential requests, that multiplies.

The rule is to put compute near whoever is waiting on it — which is usually the end user, but not always. A nightly batch job that reads from an on-premises system should sit near that system, not near a user.

CloudFront can absorb some of this for static or cacheable content by serving from edge locations worldwide, but it does not help uncacheable API calls that must reach the origin. Latency-sensitive dynamic workloads need the region itself to be close.

---

## Data Residency

Regulation frequently dictates where data may physically reside — GDPR for EU personal data, plus sector-specific and national requirements elsewhere. Because a region is a real geographic location, region choice *is* the data residency control.

This factor usually outranks the others. If data must stay in the EU, the region is an EU region, and latency or price arguments do not change that. Worth confirming before design work starts, because retrofitting is a migration.

---

## Service Availability

Not every service exists in every region, and newer services take time to propagate. Regions also differ in which EC2 instance types they offer — newer generations often reach the large regions first.

Checking availability before committing is straightforward:

```bash
# Which regions offer a given service
aws ssm get-parameters-by-path \
  --path /aws/service/global-infrastructure/services/bedrock/regions \
  --query "Parameters[].Value" --output text
```

*AWS publishes its regional service availability in SSM public parameters, so it can be queried rather than looked up by hand.*

The failure mode to avoid is designing an architecture and then discovering that one of its services is unavailable in the chosen region — leaving a choice between changing region and changing design.

---

## Price

Prices differ by region for the same resource, sometimes by 10–30% for compute. `us-east-1` is typically among the cheapest; regions such as São Paulo and Sydney are typically higher.

Price is real but rarely decisive. A 20% compute difference does not justify placing a workload where its users are 150 ms away, and it never overrides a residency requirement. Where it does matter is for workloads with no latency or residency constraints at all — batch processing, build agents, disaster-recovery capacity.

---

## Putting It Together

A workable order of operations:

1. **Residency** — this is usually a hard constraint. Apply it first, and it may decide the question outright.
2. **Latency** — of the remaining regions, which are near the users or systems that wait on the workload?
3. **Service availability** — do the services and instance types the design needs exist there?
4. **Price** — break remaining ties.

Two additional considerations are worth naming. Some organizations standardize on a small set of regions to keep operations manageable, and that consistency is worth more than a marginally better fit. And `us-east-1` is the default in most tooling, so workloads land there unless someone decides otherwise — a default is not a decision.

---

## Key Takeaways

- Region choice is difficult to reverse; resources are region-scoped and do not migrate on their own.
- Latency has a physical floor set by distance; CloudFront helps cacheable content, not dynamic API calls.
- Data residency requirements are usually hard constraints and typically decide the question first.
- Service and instance-type availability varies by region and can be queried before committing.
- Regional price differences are real but rarely outweigh latency or residency.
- `us-east-1` is the tooling default, so verify that a workload is there deliberately rather than by accident.
