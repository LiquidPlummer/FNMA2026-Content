# Cost Explorer & Tag Attribution

**Cost Explorer** is the tool for understanding a bill. Getting useful answers from it depends on knowing how to slice the data and on cost allocation tags having been activated in advance.

---

## Grouping and Filtering

Cost Explorer's value is in its grouping dimensions. The same total, grouped differently, answers different questions:

| Group by | Answers |
|---|---|
| **Service** | Which services cost the most |
| **Usage type** | The specific charge — `BoxUsage:m5.large`, `DataTransfer-Out-Bytes` |
| **Tag** | Which team, product, or environment |
| **Region** | Where spend is concentrated |
| **Linked account** | Which account, in an organization |
| **Instance type** | Which sizes dominate EC2 spend |

**Usage type is the most informative and the most overlooked.** "EC2 — $4,200" is not actionable. Grouped by usage type, that becomes instance hours, EBS storage, NAT gateway hours, NAT processing, and transfer — and it is usually clear which one to address.

A useful default view: group by service, filter to the largest one, regroup by usage type. Two clicks from "the bill went up" to "NAT gateway processing tripled."

---

## Cost Allocation Tags

Grouping by tag requires the tag to be **activated as a cost allocation tag** in the Billing console. Until then, tags exist on resources but do not appear as a Cost Explorer dimension.

Two properties matter:

**Activation is not retroactive.** A tag activated today produces data from today forward. Tagging resources correctly for six months and forgetting to activate the tag means six months of data that cannot be grouped by it.

**Activation is per tag key**, done once, in the management account of an organization.

Two categories exist:

- **AWS-generated tags** — `aws:createdBy` and similar, applied automatically.
- **User-defined tags** — ours, and the ones that matter.

The practical sequence: define the tag scheme, activate the keys as cost allocation tags, then start creating resources. Any other order loses data.

---

## Reading Untagged Spend

The "No tag value" bucket is the honest measure of how well tagging is working. A large one means cost attribution is not usable — the biggest single line cannot be assigned.

Its usual contributors are the resources nothing tags automatically: EBS volumes created by instances, snapshots, Elastic IPs, NAT gateways, load balancer hours, and data transfer. Several of these are pure infrastructure that no single team owns, which is a genuine allocation question rather than a tagging failure. Shared costs like NAT gateways and transfer often need a documented split rather than a tag.

---

## Other Views

**Cost and Usage Report (CUR)** is the complete, line-item dataset delivered to S3. Cost Explorer is a summarized view; the CUR is the underlying data, queryable with Athena. Necessary for detailed chargeback and custom analysis.

**Cost Anomaly Detection** applies machine learning to spending patterns and alerts on unusual changes. It catches the sudden increases — a forgotten cluster, a runaway process — that a monthly review would find weeks later. It costs nothing and is worth enabling.

**Rightsizing recommendations** identify over-provisioned EC2 instances based on observed utilization.

**Savings Plans recommendations** analyze usage history to suggest a commitment level.

---

## A Monthly Routine

Fifteen minutes, and it catches most problems while they are small:

1. **Total versus last month.** Any significant change gets explained.
2. **Top five services**, and whether the ranking changed.
3. **The largest service, by usage type.** This is where a change is usually explained.
4. **Untagged spend** as a percentage, and whether it is growing.
5. **Data transfer categories**, since nothing else surfaces them.
6. **Anomaly detection findings.**

The value is in doing it consistently. Costs grow gradually, and a monthly comparison catches a trend that a one-off review at year end would find only after it had accumulated.

---

## Key Takeaways

- Cost Explorer's value is in grouping; usage type is the most informative dimension and the most overlooked.
- Grouping by tag requires activating cost allocation tags in the Billing console, which is not retroactive.
- Activate tag keys before creating resources, or the cost data for that period cannot be attributed.
- Untagged spend concentrates in derived resources — volumes, snapshots, NAT gateways, transfer — some of which need a documented allocation rather than a tag.
- The Cost and Usage Report holds the full line-item data for detailed analysis via Athena.
- Cost Anomaly Detection is free and catches sudden increases well before a monthly review would.
- A short monthly review comparing totals, services, usage types, and untagged share catches most problems early.
