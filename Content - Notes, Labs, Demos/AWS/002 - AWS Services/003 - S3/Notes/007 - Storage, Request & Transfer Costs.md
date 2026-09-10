# Storage, Request & Transfer Costs

S3 bills three separate things, and for many workloads storage is not the largest. Estimating an S3 bill from stored gigabytes alone routinely produces a figure that is wrong by a wide margin.

---

## The Three Components

**Storage** — per gigabyte-month, varying by class. Roughly $0.023/GB for Standard.

**Requests** — per operation, and the rate differs by operation type:

| Operation | Approximate cost |
|---|---|
| `PUT`, `COPY`, `POST`, `LIST` | $0.005 per 1,000 |
| `GET`, `SELECT` | $0.0004 per 1,000 |
| Lifecycle transitions | $0.01 per 1,000 |
| Glacier retrievals | Per request plus per GB |

Writes cost roughly twelve times more than reads. `LIST` is billed at the write rate, which matters for applications that list frequently.

**Data transfer** — out to the internet at roughly $0.09/GB. Inbound is free, as is transfer to CloudFront.

---

## When Requests Dominate

The pattern that surprises people: **many small objects**.

Consider 100 million objects of 10 KB each — about 1 TB total.

- Storage: 1,000 GB × $0.023 = **$23/month**
- Writing them once: 100M × $0.005/1,000 = **$500**
- Reading each once a month: 100M × $0.0004/1,000 = **$40/month**

Storage is the smallest number by a wide margin. For workloads with high object counts and small objects — IoT telemetry, per-event files, individual log lines — request charges are the bill.

The response is to reduce object count by batching. Combining a thousand small events into one object cuts request charges by roughly a thousand-fold and usually improves read performance too, since one `GET` replaces a thousand.

---

## When Transfer Dominates

Serving content directly to the internet from S3:

- 10 TB/month out: 10,000 GB × $0.09 = **$900/month**
- Storing that same 10 TB: **$230/month**

Transfer is four times storage. Through CloudFront, the per-gigabyte rate is lower, transfer from S3 to CloudFront is free, and cached objects avoid origin requests entirely — frequently cutting the total substantially.

**Any public content served at volume should go through CloudFront**, and the cost argument is usually stronger than the latency argument.

---

## The Costs That Hide

Several charges do not correspond to anything visible in an object listing.

**Incomplete multipart uploads.** Parts from failed uploads are stored and billed but do not appear in `ListObjectsV2`. A bucket can hold hundreds of gigabytes of them invisibly. The lifecycle rule to abort them is the fix, and its absence is a common explanation for unexplained cost.

**Non-current versions.** In a versioned bucket, every overwrite retains the previous version. The object list shows current versions only.

**Delete markers.** Small individually, but numerous in a bucket with many deletions.

**Minimum object size in cold classes.** Objects under 128 KB are billed at 128 KB in Standard-IA and colder.

**Minimum storage duration.** Deleting from Standard-IA before 30 days still bills 30 days; Glacier Deep Archive bills 180.

**Lifecycle transition requests.** Moving many small objects to a colder class can cost more in transitions than the storage saving returns.

**Glacier retrieval charges.** Restoring a large archive incurs both request and per-gigabyte retrieval charges. A one-off restore of a Deep Archive dataset can be a substantial bill on its own.

---

## Investigating

**S3 Storage Lens** provides free account-wide metrics — object counts, size distribution, non-current version share, and incomplete multipart upload bytes. It is the fastest way to find where cost is concentrated.

**Cost Explorer grouped by usage type** separates `TimedStorage-ByteHrs`, `Requests-Tier1` (writes), `Requests-Tier2` (reads), and `DataTransfer-Out-Bytes`. This immediately shows which of the three components dominates.

**S3 Inventory** produces a scheduled report of every object with size, class, and version status — useful for detailed analysis of a large bucket without listing it repeatedly.

---

## Reducing Cost

In rough order of impact:

1. **Add lifecycle rules** — abort incomplete uploads, expire non-current versions, transition or delete aged data.
2. **Put CloudFront in front of public content.**
3. **Batch small objects** where request charges dominate.
4. **Choose storage classes by actual access pattern**, checking object size and lifetime first.
5. **Use S3 Bucket Keys** on KMS-encrypted buckets to cut KMS request charges.
6. **Compress before storing** — it reduces storage and transfer together.
7. **Use VPC gateway endpoints** so VPC traffic to S3 avoids NAT processing charges.

---

## Key Takeaways

- S3 bills storage, requests, and transfer separately, and storage is often the smallest.
- Writes and `LIST` cost roughly twelve times more than reads, so high object counts make requests the dominant charge.
- Batching small objects into larger ones cuts request costs dramatically.
- Serving public content directly from S3 makes transfer the dominant charge; CloudFront reduces both rate and volume.
- Incomplete multipart uploads and non-current versions are billed but invisible in a normal object listing.
- Cold storage classes carry minimum object sizes, minimum durations, transition charges, and retrieval charges.
- S3 Storage Lens and Cost Explorer by usage type identify which component dominates.
