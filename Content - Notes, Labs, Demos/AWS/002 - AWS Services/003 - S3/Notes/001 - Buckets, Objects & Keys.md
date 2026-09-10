# Buckets, Objects & Keys

**S3** stores objects in buckets. It looks like a filesystem in the console and is not one, and most S3 surprises come from that gap.

---

## The Model

A **bucket** is a container. An **object** is a blob of data plus metadata. A **key** is the object's full name within the bucket.

```
Bucket: company-reports
├── Key: 2026/q3/summary.pdf
├── Key: 2026/q3/details.csv
└── Key: 2026/q4/summary.pdf
```

*Three objects with keys that look like paths. Nothing about `2026/` or `q3/` exists as an object — those are just characters in the key.*

An object holds:

- **Key** — up to 1,024 UTF-8 bytes
- **Value** — 0 bytes to 5 TB
- **Version ID**, if versioning is enabled
- **Metadata** — system metadata like content type, plus user-defined key-value pairs
- **Storage class**

---

## Why It Is Not a Filesystem

**There are no directories.** The key `2026/q3/summary.pdf` is one flat string. The console displays a folder tree by splitting keys on `/`, but this is presentation. Creating a "folder" in the console creates a zero-byte object whose key ends in `/` — a placeholder, not a container.

The practical consequences:

**Renaming is copy-and-delete.** There is no rename operation. Renaming a "folder" means copying every object under that prefix to new keys and deleting the originals. For a large prefix this is a real job with real request charges.

**Listing is a prefix scan.** `ListObjectsV2` with a prefix scans keys in lexicographic order. Listing a prefix containing millions of objects is paginated and slow.

**There is no per-directory anything.** No directory permissions, no directory size, no atomic directory operation. Access control on a prefix is expressed as a policy pattern, not a property of a folder.

**Objects are immutable.** There is no partial update — no appending to an object, no writing to the middle of one. Changing an object means replacing it entirely.

That last point rules out a category of use. A log file that is appended to, a database file, a file opened for random writes — none of these work on S3. Those need EBS or EFS.

---

## Keys and Prefixes

A **prefix** is the leading portion of a key, used for filtering and organization:

```bash
aws s3api list-objects-v2 --bucket company-reports --prefix "2026/q3/"
```

*Returns objects whose keys start with that string. Prefixes are string matching, not directory traversal.*

S3 scales throughput per prefix — at least 3,500 write and 5,500 read requests per second each — so a key design spreading load across prefixes achieves higher aggregate throughput than one concentrating on a single prefix.

**Date-based keys are the common trap.** Keys like `2026/09/09/...` mean all of today's writes hit one prefix. Where throughput matters, putting a variable component earlier in the key spreads the load. Where it does not, date prefixes are convenient for lifecycle rules and worth keeping.

---

## Working With Objects

```bash
# High-level commands: filesystem-like
aws s3 cp report.pdf s3://company-reports/2026/q3/summary.pdf
aws s3 sync ./local-dir s3://company-reports/2026/q4/

# API-level commands: direct parameter control
aws s3api put-object --bucket company-reports --key 2026/q3/summary.pdf \
  --body report.pdf --content-type application/pdf \
  --metadata author=finance,reviewed=true
```

*`aws s3` provides `cp`, `sync`, and `ls` for moving files; `aws s3api` maps to the API and is needed for specific parameters.*

**Multipart upload** splits large objects into parts uploaded in parallel. The CLI does this automatically above a threshold. It is required above 5 GB, and it introduces a cost trap: **failed multipart uploads leave parts that are billed but invisible to a normal listing.** A lifecycle rule to abort incomplete multipart uploads after a few days is standard practice and is often the explanation for a bucket costing more than its contents suggest.

---

## Bucket Basics

Buckets are created in a region and objects stay there. There is a soft limit of 100 buckets per account, raisable to 1,000 — which means buckets are not the right unit for separating many small things. Prefixes within a bucket are.

Metadata is set at write time. Changing an object's content type or user metadata requires copying the object over itself.

---

## Key Takeaways

- S3 stores objects identified by a flat key; directories do not exist, and the console's folder view is presentation only.
- There is no rename — it is a copy followed by a delete, with request charges for each object.
- Objects are immutable, so appending or partial updates are impossible; changing an object replaces it.
- Prefixes are string matches used for filtering and for spreading throughput across partitions.
- Date-first keys concentrate writes on one prefix, which limits throughput for high-volume workloads.
- Multipart upload is automatic for large files, and abandoned parts bill silently — add a lifecycle rule to abort them.
- Bucket count is limited, so use prefixes rather than buckets to separate many small things.
