# Common Use Cases (Storage, Hosting, Backups)

S3's simple object-storage model — a key, a value, and some metadata, accessed over HTTP — turns out to be flexible enough to support a surprisingly wide range of real-world uses. A few patterns come up constantly.

## General-Purpose Storage

The most straightforward use case: S3 as a place to store files that an application needs to read and write, without hosting them on the application's own servers. Think user-uploaded profile pictures, generated PDF reports, log files, or datasets used by a data pipeline. Offloading this kind of storage to S3 rather than a server's local disk means the files survive independently of any particular server, and they're accessible from anywhere the application runs — including, if the application scales horizontally, from every one of its many instances at once.

This decoupling matters more than it might first seem: if files were stored on a single server's local disk instead, that server would become a hidden single point of failure, and horizontal scaling (spreading the application across multiple servers) would break, since only the original server would actually have the files.

## Static Website Hosting

S3 can serve files directly over HTTP, which means a bucket can host a **static website** — HTML, CSS, JavaScript, and images, with no server-side code execution required. This is a great fit for content that doesn't need a backend to generate it dynamically: documentation sites, marketing pages, or the built output of a modern frontend framework.

The appeal here is largely operational: there's no server to patch, scale, or keep running. We upload the files, and S3 handles serving them to however many visitors show up, with the same durability and availability guarantees as any other S3 usage. This is a genuinely different mode from "application storage" above — here, the bucket's contents *are* the product being served, not files an application happens to read and write.

## Backups and Archival

Because S3 is built for durability (multiple copies of data spread across a region's infrastructure, as mentioned back in the Designing for AZ Failure topic) and doesn't require pre-provisioning capacity, it's a natural destination for backups — database snapshots, exported logs, disaster-recovery copies of critical data. Rather than managing backup tapes or a separate storage system, applications and services can write backups straight to an S3 bucket and rely on S3 to keep that data safe and durable.

S3 also offers multiple **storage classes** — different tiers optimized for different access patterns, such as data accessed frequently versus data kept for long-term archival that's rarely, if ever, retrieved. Choosing the right class for backup data (versus data that needs to be available instantly) is a meaningful cost lever, though the specific classes and their trade-offs are worth their own deeper look beyond this introductory pass.

## Why One Service Covers All Three

It might seem strange that the same service handles active application storage, website hosting, and long-term backups — but all three are really the same underlying need expressed differently: durably store some bytes under a name, and make them retrievable later, at whatever scale is required. That generality is the whole appeal of object storage as a model, and it's why S3 tends to show up somewhere in almost every AWS architecture, no matter what the application actually does.
