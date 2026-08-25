# What Is S3, Buckets & Objects

**S3** (Simple Storage Service) is AWS's **object storage** service — a place to store and retrieve any amount of data, of essentially any type, without managing the underlying storage hardware ourselves. It's one of the oldest and most foundational AWS services, and its two core concepts are simple enough to state in a sentence: files (**objects**) live inside containers (**buckets**).

## Object Storage vs Other Kinds of Storage

It helps to place S3 against the storage models we might already be familiar with:

- **Block storage** (like an EC2 instance's attached disk) stores raw chunks of data the operating system organizes into a filesystem — think of it as a virtual hard drive.
- **File storage** provides a shared filesystem, complete with a real directory structure, that multiple systems can mount and browse.
- **Object storage** — what S3 provides — stores whole, self-contained units of data (objects), each with a unique key (name) and metadata, accessed over HTTP rather than mounted as a drive.

Object storage trades away some things a traditional filesystem offers, like fine-grained in-place editing of part of a file, in exchange for massive scalability and simplicity: we don't manage disks, partitions, or filesystems at all, we just store and fetch objects by key.

## Buckets

A **bucket** is a container for objects — the top-level organizational unit in S3. Every bucket:

- Has a **globally unique name** across all of AWS, not just within our account (touched on earlier in the Global Infrastructure lesson).
- Lives in a specific **region**, chosen when the bucket is created.
- Can hold effectively unlimited objects and data, with no need to pre-allocate capacity — we don't declare how big a bucket will be before using it.

Buckets are also where bucket-level settings live — things like permissions, versioning, and lifecycle rules (rules for automatically transitioning or deleting objects over time), which we'll build on in the next topic.

## Objects

An **object** is the actual unit of data stored in S3 — a file, essentially, plus the metadata that describes it. Every object consists of:

- **Key** — the object's name, which acts as its unique identifier within the bucket. As covered in the console walkthrough, keys that share a common prefix (like `photos/2024/`) are displayed as if organized into folders, but that folder structure is really just a naming convention — S3 itself has no true concept of nested directories.
- **Value** — the actual data (the bytes of the file itself).
- **Metadata** — information about the object, such as its content type, size, and last-modified time, along with any custom metadata we choose to attach.
- **Version ID** — if versioning is enabled on the bucket, each update to an object creates a new version rather than overwriting the old one.

## A Simple Mental Model

```
Bucket: "company-reports"           (globally unique name, lives in one region)
  ├── key: "2024/q1-summary.pdf"    (an object)
  ├── key: "2024/q2-summary.pdf"    (an object)
  └── key: "logos/company-logo.png" (an object)
```
*Buckets contain objects, addressed by key — the "folders" here are just a naming convention within the keys.*

## Why This Design Matters

Because objects are addressed by a flat key rather than a real filesystem path, and because buckets don't require pre-planning capacity, S3 scales to enormous amounts of data without the kind of planning a traditional filesystem or disk array would require. That simplicity — store an object, fetch it later by key — is exactly what makes S3 useful for such a wide range of situations, which is what the next topic covers.
