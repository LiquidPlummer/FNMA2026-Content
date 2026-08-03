# Region-Scoped vs Global Services

Not every AWS service lives inside a region. Most do, but a handful sit above the regional structure entirely. Knowing which is which matters in practice — it affects where a resource shows up in the console, and whether we need to think about "which region is this in?" at all.

## Region-Scoped Services

Most AWS services are **region-scoped**: a resource we create exists in exactly one region, and is invisible from any other. EC2 instances, S3 buckets (mostly — more on this below), RDS databases, and Lambda functions are all region-scoped. Switch the region selector in the AWS console, and the list of resources we see changes completely, because we're now looking at a different region's inventory.

This is why the region selector in the console (usually in the top-right corner) matters so much — a common early mistake is creating a resource, switching regions to check something else, and being confused when the resource seems to have "disappeared." It hasn't disappeared; it's just not in the region we're currently viewing.

## Global Services

A small number of services are **global**: a single instance of the service applies across the entire AWS account, with no region selection involved. The most important example is **IAM** (Identity and Access Management) — users, roles, and policies aren't tied to any one region; they apply account-wide. Route 53 (DNS) is another example, since domain name resolution isn't inherently tied to a physical location either.

## A Middle Ground: S3

S3 is a useful edge case to understand. When we create an S3 **bucket**, we do choose a region for it, and the bucket's data physically lives there. But the S3 *service* itself — the console experience, bucket naming rules, and how we interact with it — feels more global than something like EC2, since bucket names must be globally unique across all of AWS, not just within a region. So S3 is best described as region-scoped for where data lives, but with some global characteristics layered on top (like that shared namespace for bucket names).

## Why the Distinction Matters

Understanding region-scoped versus global services helps explain a lot of day-to-day AWS behavior:

- Why an IAM user we create works the same way no matter which region we're viewing in the console.
- Why we sometimes have to explicitly re-create a resource in a second region for disaster recovery, rather than it "just being available" everywhere.
- Why S3 bucket names have to be unique across every AWS customer on Earth, not just within our own account.

Keeping this distinction in mind saves a lot of confusion later, especially once we're managing resources across multiple regions.
