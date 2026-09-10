# The Execution Model

**Lambda** runs code in response to events. Understanding what happens around an invocation — particularly what persists between them — explains most of Lambda's behavior.

---

## The Basic Cycle

An event arrives, Lambda runs the function, the function returns a result. There is no server to provision, no process to keep running, and no scaling to configure.

Underneath, Lambda manages **execution environments** — isolated sandboxes containing the runtime and our code. The lifecycle of one:

```
1. Download the code
2. Start the execution environment          ┐
3. Run initialization code (outside handler)│ ← cold start
4. Run the handler                          ┘
   ...
5. Run the handler again (same environment) ← warm invocation
6. Freeze between invocations
7. Eventually, shut down
```

*Steps 1–3 happen once per environment. Steps 4–6 repeat for as long as the environment is reused.*

---

## Environment Reuse

**The environment may be reused, and this is the most important operational fact about Lambda.**

After a handler returns, the environment is frozen rather than destroyed. A subsequent invocation may thaw the same environment and run the handler again — skipping initialization entirely.

What persists between invocations in the same environment:

- **Module-level and global variables**
- **Open connections** — database, HTTP clients
- **Files written to `/tmp`** (512 MB by default, configurable to 10 GB)
- **Anything cached in memory**

This is what makes connection reuse and secret caching work:

```python
import boto3, json

# Runs once per environment, on cold start only
ssm = boto3.client("ssm")
CONFIG = json.loads(
    ssm.get_parameter(Name="/orders/config", WithDecryption=True)["Parameter"]["Value"]
)

def handler(event, context):
    # Runs on every invocation
    return process(event, CONFIG)
```

*Initialization outside the handler runs once per environment. Placing the client creation and configuration fetch inside the handler would repeat both on every invocation.*

---

## What Reuse Requires

Reuse is an optimization, not a guarantee, and treating it as one causes bugs.

**Never rely on state persisting.** The next invocation may be a fresh environment. Anything that must survive belongs in DynamoDB, S3, or another store.

**Never leak state between invocations.** A global variable holding request-specific data is visible to the next invocation — which may be a different user's request. This is a genuine security bug, and it appears only under concurrency.

```python
# Wrong: request state in a global, visible to the next invocation
current_user = None

def handler(event, context):
    global current_user
    current_user = event["user"]      # leaks across invocations
```

*Request-scoped data belongs in local variables. Globals are for things shared safely across all invocations — clients, configuration, connection pools.*

**Clean up `/tmp`.** It persists across invocations and is finite. A function writing temporary files without removing them eventually fails on a full disk, in a warm environment, intermittently.

---

## Concurrency

Each execution environment handles **one invocation at a time**. Concurrent invocations require concurrent environments.

```
100 simultaneous requests → 100 execution environments
```

*There is no in-process concurrency model. Lambda scales by adding environments, which is why a function's code does not need to be thread-safe within an invocation — but also why 100 concurrent invocations means 100 database connections unless something pools them.*

That last point is why RDS Proxy exists, and why DynamoDB pairs with Lambda more naturally than RDS does.

---

## Invocation Types

**Synchronous** — the caller waits for the result. API Gateway, ALB, direct `Invoke`. Errors return to the caller.

**Asynchronous** — Lambda queues the event and returns immediately. S3 notifications, SNS, EventBridge. Lambda retries failures automatically and can send failures to a dead-letter queue or destination.

**Stream and poll-based** — Lambda polls a source and invokes with batches. SQS, Kinesis, DynamoDB Streams. Retry behavior depends on the source.

The invocation type determines error handling and retry behavior, which is covered separately.

---

## What Lambda Does Not Provide

**No background work after the response.** Once the handler returns, the environment freezes. A thread started but not awaited may never run — and may resume, confusingly, during a later invocation.

**No guaranteed local state.** Covered above.

**No long-running processes.** The maximum execution time is 15 minutes, and it is not adjustable.

**No inbound network connections.** A function cannot listen on a port. It is invoked; it does not serve.

---

## Key Takeaways

- Lambda runs code in execution environments that are created, reused, and eventually discarded.
- Initialization outside the handler runs once per environment; handler code runs every invocation.
- Module-level variables, open connections, and `/tmp` contents persist across warm invocations.
- Reuse is an optimization, never a guarantee — durable state belongs in a data store.
- Request-scoped data in globals leaks to the next invocation, which is a real security bug under concurrency.
- `/tmp` persists and is finite, so temporary files must be cleaned up.
- Each environment handles one invocation at a time, so concurrency means one environment and one connection per concurrent request.
- Work started but not awaited before the handler returns may never complete.
