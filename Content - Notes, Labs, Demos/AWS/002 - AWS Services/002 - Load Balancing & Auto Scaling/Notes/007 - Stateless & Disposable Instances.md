# Stateless & Disposable Instances

Auto Scaling works by creating and destroying instances freely. That only produces a working system if instances are **interchangeable** — and that requirement constrains how the application is written.

---

## What the Group Assumes

An Auto Scaling group will, without warning:

- Terminate any instance to scale in
- Terminate a healthy instance to rebalance across AZs
- Replace an instance that fails a health check
- Replace every instance during an instance refresh
- Terminate a spot instance with two minutes' notice

For any of these to be safe, an instance being destroyed must lose nothing that matters. That is what "stateless and disposable" means in practice.

---

## Where State Accumulates

**Session state in memory.** The most common problem. A user logs in, their session lives in the process, and their next request reaches a different instance that has never heard of them. They are logged out — or worse, intermittently logged out, which is harder to diagnose.

The load balancer's **sticky sessions** appear to solve this by pinning a user to one instance. They do not: when that instance is terminated, its users lose their sessions anyway, and load distributes unevenly because existing users cannot be rebalanced. Stickiness converts a constant problem into an occasional one.

The real fix is to move sessions out of the process — to ElastiCache (Redis), DynamoDB, or a signed token held by the client. Any of these makes instances genuinely interchangeable.

**Files on local disk.** Uploads written to the instance's filesystem exist on that instance only. Another instance cannot serve them, and terminating the instance deletes them. Uploads belong in S3; shared files that genuinely need filesystem semantics belong on EFS.

**In-memory caches.** A local cache is fine as an optimization — each instance builds its own and a cold instance is slower until it warms up. It becomes a problem when it is the only copy of something, or when cache inconsistency between instances causes incorrect behavior.

**Scheduled jobs on instances.** A cron job on every instance in a group of five runs five times. On a group that scales, it runs an unpredictable number of times. Scheduled work belongs in EventBridge Scheduler triggering a Lambda function or an ECS task — somewhere it runs exactly once.

**Logs written locally.** Logs on a terminated instance are gone, and they are usually most wanted precisely when an instance has been terminated for failing. Ship them to CloudWatch Logs as they are written.

---

## What Disposability Requires

**Start unattended.** No manual steps, no interactive prompts. An instance must reach a serving state on its own.

**Start reasonably fast.** Startup time is the floor on how quickly the group can respond to load. Ten-minute boots make scaling ineffective, and baking dependencies into the AMI is the usual fix.

**Shut down gracefully.** On `SIGTERM`, stop accepting new work, finish in-flight requests, flush anything buffered, and exit. Lifecycle hooks and deregistration delay give time for this — but only if the application uses it.

**Tolerate being killed anyway.** Graceful shutdown is best-effort. A spot reclamation gives two minutes; a host failure gives none. Work that must not be lost belongs in a queue with a visibility timeout, so an unfinished item is redelivered rather than dropped.

**Carry no unique identity.** An instance should not be special. Anything requiring exactly one instance to do something needs a different mechanism — a leader election, a queue, or a scheduled job outside the fleet.

---

## The Practical Test

A useful question when reviewing a design: **what breaks if a random instance is terminated right now?**

If the answer is "some users get logged out," "recent uploads disappear," "a nightly job doesn't run," or "we lose the debug logs we needed," then the instances are not disposable and the group's behavior will eventually cause an incident.

If the answer is "capacity drops until a replacement launches," the design is correct.

---

## Where State Belongs

| State | Where it goes |
|---|---|
| Sessions | ElastiCache, DynamoDB, or a signed client token |
| Uploaded files | S3 |
| Shared filesystem | EFS |
| Application data | RDS or DynamoDB |
| Logs | CloudWatch Logs |
| Metrics | CloudWatch |
| Configuration | Parameter Store or the AMI |
| Secrets | Secrets Manager |
| Scheduled jobs | EventBridge Scheduler |

The pattern is consistent: **state lives in a managed service, and instances hold only what they can rebuild.** That is what makes them replaceable, and it is the precondition for everything else in this lesson working.

---

## Key Takeaways

- Auto Scaling terminates instances for scale-in, rebalancing, health failures, refreshes, and spot reclamation.
- Sessions in process memory break when instances change; sticky sessions hide the problem rather than fixing it.
- Local files, local-only caches, per-instance cron jobs, and local logs all fail when an instance is replaced.
- Instances must start unattended and quickly, and handle `SIGTERM` by draining in-flight work.
- Graceful shutdown is best-effort — critical work belongs in a queue that redelivers unfinished items.
- Ask what breaks if a random instance is terminated now; the only acceptable answer is a temporary capacity drop.
- State belongs in managed services, leaving instances holding only what they can rebuild.
