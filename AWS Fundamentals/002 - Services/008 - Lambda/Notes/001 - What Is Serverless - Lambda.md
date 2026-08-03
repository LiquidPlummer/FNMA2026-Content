# What Is Serverless / Lambda

Every service covered so far in this module — EC2, RDS, DynamoDB, Redshift — still involves us thinking about *some* unit of provisioned capacity, even when AWS manages a lot of the operational burden. **Lambda** represents a further step along that same spectrum: a **serverless** compute service where we don't think about servers, instances, or capacity at all — we just provide code, and AWS runs it.

## What "Serverless" Actually Means

"Serverless" is a slightly misleading name — there are, of course, still real servers involved somewhere. What it actually means is that *we* never provision, manage, patch, or scale those servers ourselves; that entire layer is fully abstracted away by AWS. This is the far end of the IaaS-to-PaaS-to-SaaS spectrum from earlier in the course, applied to compute: we don't choose an instance type, we don't decide how many servers to run, and we don't manage an operating system at all. We provide a function; AWS decides how and where to run it.

## How Lambda Works

A **Lambda function** is a piece of code (in one of several supported languages) that runs in response to being **invoked** — triggered by an event, rather than running continuously the way a server does. Between invocations, a Lambda function isn't running at all, and isn't costing anything either — we're charged based on actual execution, not on time spent idle waiting for something to happen. When an invocation occurs, AWS automatically provisions whatever compute is needed to run the function, executes it, and returns the result.

This event-driven model is a genuine departure from EC2's always-on server model. An EC2 instance runs continuously, whether or not it's doing useful work at any given moment, and we pay for that continuous running time. A Lambda function only exists, computationally, for the brief window it's actually executing.

## Automatic Scaling, By Design

Because Lambda has no persistent server to reason about, scaling isn't something we configure — it's an inherent property of the model. If a function is invoked once, AWS runs one instance of it. If it's invoked a thousand times simultaneously, AWS runs (within limits) up to a thousand concurrent instances of it, each handling one invocation, without us ever provisioning capacity for that in advance. This is horizontal scaling taken to its most automatic extreme, directly building on the scalability concepts from earlier in the course, but with the scaling decision entirely removed from our hands.

## The Trade-Offs

Serverless isn't a strictly better version of EC2 — it's a different point on the control-versus-convenience spectrum, and it comes with real constraints:

- **Execution time limits.** Lambda functions are designed for relatively short-lived work, not long-running processes — a function that needs to run continuously for hours is a poor fit.
- **No persistent local state between invocations.** Each invocation may run on a fresh underlying environment, so a function generally can't rely on something it stored locally during a previous invocation still being there.
- **Less control over the runtime environment** than a full server provides, since we're not managing the OS or underlying infrastructure at all.
- **Cold starts.** A function that hasn't been invoked recently may take a brief moment longer to start up on its next invocation, since AWS has to provision the execution environment fresh.

## Where This Leaves Us

Lambda trades away the fine-grained control (and the operational burden) of managing servers, in exchange for automatic scaling, no idle cost, and effectively no infrastructure to manage at all. That trade makes it an excellent fit for certain kinds of work and a poor fit for others — which is exactly what the next topic explores, through the events and use cases where Lambda tends to shine.
