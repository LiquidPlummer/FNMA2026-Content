# Storage Classes & Lifecycle Rules

S3 offers several **storage classes** trading storage price against retrieval cost and access latency. **Lifecycle rules** move objects between them automatically as they age.

---

## The Classes

| Class | Storage cost | Retrieval | Availability | Minimum duration |
|---|---|---|---|---|
| Standard | Baseline | Free | 99.99% | None |
| Intelligent-Tiering | Varies + monitoring fee | Free | 99.9% | None |
| Standard-IA | ~45% less | Per GB | 99.9% | 30 days |
| One Zone-IA | ~65% less | Per GB | 99.5%, **one AZ** | 30 days |
| Glacier Instant Retrieval | ~75% less | Per GB, milliseconds | 99.9% | 90 days |
| Glacier Flexible Retrieval | ~85% less | Minutes to hours | 99.99% | 90 days |
| Glacier Deep Archive | ~95% less | Hours | 99.99% | 180 days |

All classes except One Zone-IA store data across at least three Availability Zones with the same durability. **One Zone-IA stores in a single AZ**, so an AZ loss loses the data — acceptable only for data reproducible from elsewhere.

---

## The Trade-Off

Cheaper classes charge to retrieve. That produces a threshold: below a certain access frequency, a colder class is cheaper; above it, retrieval charges exceed the storage saving.

The minimum duration charges are equally important. An object deleted from Standard-IA after 5 days is **billed for the full 30 days**. Data with a short life span costs more in a cheaper class than in Standard.

Two additional traps:

**Small objects.** Standard-IA and colder classes bill a minimum of 128 KB per object. A million 5 KB objects are billed as if they were 128 KB each — twenty-five times the actual data.

**Transitions cost money.** Each lifecycle transition is a request charge per object. Moving a million small objects to a colder class can cost more in transitions than it saves in storage.

---

## Intelligent-Tiering

Intelligent-Tiering monitors access patterns and moves objects between tiers automatically, with **no retrieval charges** and no minimum duration for the frequent and infrequent tiers.

It charges a small per-object monitoring fee, which makes it uneconomic for very large numbers of tiny objects and a good default for anything else with an unpredictable access pattern.

Its main advantage is removing the guesswork. Where access patterns are genuinely unknown, it avoids both the retrieval charges of guessing too cold and the storage cost of guessing too warm.

---

## Lifecycle Rules

A lifecycle rule transitions or expires objects based on age:

```json
{
  "Rules": [{
    "ID": "archive-and-expire-reports",
    "Status": "Enabled",
    "Filter": { "Prefix": "reports/" },
    "Transitions": [
      { "Days": 30,  "StorageClass": "STANDARD_IA" },
      { "Days": 90,  "StorageClass": "GLACIER_IR" },
      { "Days": 365, "StorageClass": "DEEP_ARCHIVE" }
    ],
    "Expiration": { "Days": 2555 },
    "AbortIncompleteMultipartUpload": { "DaysAfterInitiation": 7 }
  }]
}
```

*Progressively colder storage as objects age, deletion after seven years, and cleanup of abandoned multipart uploads.*

Rules can filter by prefix, by tag, or by object size. Filtering by size is useful specifically to avoid transitioning small objects where the minimum-size billing would make it counterproductive.

**Every bucket should have an `AbortIncompleteMultipartUpload` rule.** Failed uploads leave parts that are billed and do not appear in a normal object listing, and this is a common explanation for unexplained bucket cost.

---

## Versioning Interaction

With versioning enabled, lifecycle rules can act separately on current and non-current versions:

```json
{
  "NoncurrentVersionTransitions": [
    { "NoncurrentDays": 30, "StorageClass": "GLACIER_IR" }
  ],
  "NoncurrentVersionExpiration": { "NoncurrentDays": 90 },
  "Expiration": { "ExpiredObjectDeleteMarker": true }
}
```

*Archives old versions after 30 days, deletes them after 90, and removes delete markers left with no versions behind them.*

Without non-current version expiration, **versioned buckets grow without bound** — every overwrite retains the previous version forever. This is one of the most common causes of S3 cost growing far beyond what the visible object list suggests.

---

## Choosing

- **Standard** for actively accessed data and anything short-lived.
- **Intelligent-Tiering** when the access pattern is unknown and objects are not tiny.
- **Standard-IA** for data accessed a few times a year, kept beyond 30 days.
- **One Zone-IA** only for reproducible data such as derived thumbnails.
- **Glacier Instant Retrieval** for archives needing immediate access.
- **Glacier Flexible / Deep Archive** for compliance retention accessed rarely or never.

Before adopting a colder class, check object size and expected lifetime. Small, short-lived objects are cheaper in Standard despite the higher per-gigabyte rate.

---

## Key Takeaways

- Colder storage classes trade lower storage cost for retrieval charges and minimum billing durations.
- One Zone-IA stores data in a single Availability Zone and is only appropriate for reproducible data.
- Objects under 128 KB are billed at 128 KB in IA and colder classes, which can make transitions counterproductive.
- Lifecycle transitions incur a per-object request charge that can exceed the saving for many small objects.
- Intelligent-Tiering removes the guesswork for unpredictable access at the cost of a per-object monitoring fee.
- Add an `AbortIncompleteMultipartUpload` rule to every bucket, since abandoned parts bill invisibly.
- Versioned buckets grow without bound unless non-current version expiration is configured.
