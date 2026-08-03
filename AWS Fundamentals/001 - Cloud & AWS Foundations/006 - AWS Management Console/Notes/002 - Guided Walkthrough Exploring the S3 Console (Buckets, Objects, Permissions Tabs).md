# Guided Walkthrough: Exploring the S3 Console (Buckets, Objects, Permissions Tabs)

With the general console concepts from the previous topic in hand, let's apply them to a specific service. S3 is a great first stop for this kind of walkthrough — its console is organized around a small number of clear concepts that map directly onto how the service actually works, and we'll be using S3 in depth in the next module anyway.

Rather than following exact click-paths (which can shift as AWS updates the console over time), this walkthrough focuses on the structure we should expect to find and what each part represents — that structure is far more stable than any specific button placement.

## Finding S3

Using the service search habit from the previous topic, search for S3 and open it. The first thing the S3 console shows is a list of **buckets** — assuming any exist yet in the account. If this is a brand new account, that list will be empty, which is itself useful information: it confirms buckets don't exist by default and have to be deliberately created.

## The Bucket List View

At the top level, the console shows buckets as a flat list — notably, *not* organized by folders the way a typical file explorer might group top-level items, since (as covered in the Global Infrastructure lesson) bucket names are unique across all of AWS, not just within our account. Each bucket in the list typically shows a few key attributes at a glance, most importantly which **region** it lives in — a good reminder that even though S3 has some global characteristics (like the shared naming namespace), an individual bucket's data physically resides in one specific region.

## Opening a Bucket: The Objects View

Opening a bucket drops us into its contents — the **objects** stored inside it. Object storage doesn't have "real" folders the way a traditional filesystem does; what looks like a folder in the console is actually just a naming convention, where object names sharing a common prefix (like `images/`) are displayed as if grouped into a folder. Understanding this distinction matters later when working with S3 programmatically, since operations that seem like "moving a folder" are really operations on a batch of individually named objects that happen to share a prefix.

Selecting an individual object typically surfaces its properties — details like size, storage class, and when it was last modified — which is worth exploring for any object we upload, just to get a feel for what metadata S3 tracks per object.

## The Permissions Tab

Both buckets and individual objects have a **permissions** area in the console, and this is the single most important tab to understand early. It's where access control is configured — who (or what) is allowed to read, write, or manage the contents. Because S3 buckets can be configured for public access, and because a misconfigured permissions setting is one of the most common real-world cloud security mistakes (as mentioned in the Shared Responsibility lesson), it's worth deliberately reviewing this tab on any bucket we create, rather than accepting default settings without looking.

The specific controls available here — block public access settings, bucket policies, access control lists — are worth their own dedicated exploration once we cover S3 in depth in the next module. For now, the goal of this walkthrough is simply to know that this tab exists and holds real consequences, so we develop the habit of checking it rather than skipping past it.

## What to Take Away

The exact pixels will shift over time, but the underlying shape won't: a list of buckets, each holding a list of objects, each with a permissions area controlling access. That three-level structure — buckets, objects, permissions — is the mental model to carry forward into the S3 module ahead.
