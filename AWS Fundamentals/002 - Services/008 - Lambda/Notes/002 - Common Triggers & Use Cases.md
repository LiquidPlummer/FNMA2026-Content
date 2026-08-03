# Common Triggers & Use Cases

Lambda functions don't run on their own — something has to **trigger** (invoke) them. Understanding the common triggers is really the same thing as understanding where Lambda tends to fit best, since the trigger a function responds to and the use case it serves are usually two sides of the same coin.

## What a Trigger Is

A **trigger** is an event source configured to invoke a Lambda function automatically whenever a relevant event occurs. Rather than a person or a scheduled process explicitly calling the function, the function is wired directly to something else in AWS, and runs whenever that something else produces an event it's listening for. This is what makes Lambda genuinely event-driven, rather than just "a program we run manually less often."

## Common Trigger Sources

- **S3 events** — a Lambda function can be triggered whenever an object is created, updated, or deleted in a bucket. A very common pattern: a user uploads an image to S3, which triggers a Lambda function that automatically generates a resized thumbnail and saves it back to another bucket.
- **API Gateway** — Lambda can serve as the backend for an HTTP API, invoked directly by incoming web requests. This enables a fully serverless application backend: no server sits waiting for requests around the clock; a function invocation happens only when a request actually arrives.
- **Scheduled events** — Lambda can be invoked on a recurring schedule, similar to a traditional cron job, without needing a persistent server to host that schedule. Useful for periodic maintenance tasks, generating regular reports, or cleaning up stale data.
- **Database and stream events** — Lambda can react to changes in a DynamoDB table (via DynamoDB Streams) or messages arriving on a queue or streaming service, processing each change or message as it occurs.
- **Direct invocation** — a function can also simply be called directly, by another application or service, as part of a larger workflow.

## Why These Use Cases Fit Lambda Well

Looking at that list, a pattern emerges: Lambda tends to shine on work that is **short-lived, event-triggered, and variable in frequency** — exactly the profile that plays to serverless's strengths and avoids its constraints from the previous topic. Resizing an uploaded image, responding to an API request, and running a nightly cleanup job are all naturally short bursts of work, triggered by something happening, rather than continuous processes.

Contrast this with a poor fit: a long-running background worker that needs to maintain an open connection for hours, or a process that depends on state built up in memory across many separate operations. That kind of workload fights against Lambda's short-execution, stateless-between-invocations model, and is generally better served by EC2 or a container-based service instead.

## A Concrete End-to-End Example

Pulling several of this course's concepts together, a common serverless pattern looks like this:

```
User uploads a file  →  S3 bucket (trigger: object created)
                              │
                              ▼
                     Lambda function invoked
                              │
                              ▼
                Processes the file, writes a
                result to DynamoDB or another
                S3 bucket
```
*A typical event-driven pipeline: an S3 upload triggers a Lambda function, which does some processing and writes output elsewhere — with no server provisioned or managed at any point in the flow.*

Notice what's absent from that diagram: no EC2 instance, no capacity planning, no idle server waiting for an upload that might not come for hours. That absence is the entire value proposition of serverless computing, and it's a fitting note to close this module on — Lambda represents just how far a workload's operational burden can shrink once we're willing to give up direct control over the infrastructure it runs on, echoing the IaaS-to-SaaS spectrum and shared responsibility trade-offs we started this course with.
