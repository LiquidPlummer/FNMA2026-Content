# Retrieval, Caching & Permissions

How an application actually gets a secret at runtime, why caching matters, and the two permissions every retrieval requires.

---

## The Two Permissions

Reading an encrypted secret needs **two** grants, and missing the second produces a confusing failure:

```json
{
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "secretsmanager:GetSecretValue",
      "Resource": "arn:aws:secretsmanager:us-east-1:123456789012:secret:prod/orders/db-*"
    },
    {
      "Effect": "Allow",
      "Action": "kms:Decrypt",
      "Resource": "arn:aws:kms:us-east-1:123456789012:key/1234abcd-...",
      "Condition": {
        "StringEquals": { "kms:ViaService": "secretsmanager.us-east-1.amazonaws.com" }
      }
    }
  ]
}
```

*The service permission to read and the KMS permission to decrypt. The `kms:ViaService` condition restricts the key's use to Secrets Manager, so the role cannot decrypt arbitrary data with it.*

Two details worth noting. Secret ARNs end in a **random six-character suffix**, so policies use a `-*` wildcard or the exact ARN — a policy naming `secret:prod/orders/db` without it matches nothing. And if the secret uses the AWS-managed key, the KMS permission is implicit; with a customer-managed key it must be granted explicitly, which is where this usually goes wrong.

The same applies to Parameter Store SecureStrings: `ssm:GetParameter` plus `kms:Decrypt`.

---

## Why Caching Matters

Retrieving a secret is an API call, with three costs:

**Latency.** 20–100 ms. On a Lambda function invoked thousands of times a second, that is added to every invocation.

**Money.** Secrets Manager charges per 10,000 API calls. A function called a million times a day fetching a secret per invocation generates a million calls.

**Throttling.** Both services have request rate limits. A large fleet fetching on every request will hit them, producing intermittent failures that look like the secret being unavailable.

**Fetch once, reuse, and refresh periodically.** The natural place is outside the request handler:

```python
import boto3, json, time

_cache = {"value": None, "fetched_at": 0}
TTL = 300

def get_db_secret():
    now = time.time()
    if _cache["value"] is None or now - _cache["fetched_at"] > TTL:
        client = boto3.client("secretsmanager")
        _cache["value"] = json.loads(
            client.get_secret_value(SecretId="prod/orders/db")["SecretString"]
        )
        _cache["fetched_at"] = now
    return _cache["value"]
```

*A five-minute cache. On Lambda, module-level state persists across invocations in a warm execution environment, so this fetches roughly once per environment per five minutes rather than once per invocation.*

AWS provides purpose-built caching libraries — `aws-secretsmanager-caching` for several languages — and the **Parameters and Secrets Lambda Extension**, which runs a local HTTP cache alongside the function:

```python
import urllib.request, os, json

url = "http://localhost:2773/secretsmanager/get?secretId=prod/orders/db"
req = urllib.request.Request(url, headers={
    "X-Aws-Parameters-Secrets-Token": os.environ["AWS_SESSION_TOKEN"]
})
secret = json.loads(json.loads(urllib.request.urlopen(req).read())["SecretString"])
```

*The extension caches locally and handles refresh, so the function makes a local HTTP call rather than an AWS API call.*

---

## Caching and Rotation Together

The tension: caching means the application may hold a value that has been rotated.

Three practices resolve it:

**Use a bounded TTL.** Five to fifteen minutes. Long enough to eliminate most calls, short enough that a rotated secret is picked up quickly.

**Re-fetch on authentication failure.** The important one. When a database connection fails to authenticate, invalidate the cache, fetch again, and retry once. This handles rotation correctly regardless of TTL.

**Rely on `AWSPREVIOUS`.** With alternating-user rotation, the previously current credential remains valid, so a cached value continues working through the transition.

Together these mean rotation is invisible to a correctly written application.

---

## Where Secrets Leak After Retrieval

The secrets service protects storage. Once the value is in memory, ordinary application hygiene applies.

**Logs.** Logging a configuration object or a connection string writes the secret to CloudWatch Logs, where it is retained and readable by anyone with log access. This is the most common leak.

**Error messages and stack traces.** A connection failure that includes the connection string, or an exception handler that dumps local variables, sends the secret to a log or an error-tracking service.

**Debug endpoints.** A `/debug/config` route that returns the running configuration.

**Crash reporters.** Many capture the full environment and local state and transmit it to a third party.

**Process listings.** Passing a secret as a command-line argument makes it visible in `ps` to every user on the host.

**Child processes.** Anything the application shells out to inherits its environment.

Practical mitigations: redact known secret keys before logging, never log full configuration objects, pass secrets via stdin rather than argv when invoking subprocesses, and review error handlers for what they include.

**CloudWatch Logs data protection** can detect and mask credential patterns in log data automatically, which is a useful backstop rather than a substitute for not logging them.

---

## Key Takeaways

- Reading an encrypted secret requires both the service permission and `kms:Decrypt`, and the KMS grant is what is usually missing.
- Secret ARNs carry a random suffix, so policies need a `-*` wildcard or the exact ARN.
- Use `kms:ViaService` to restrict a key grant to the secrets service.
- Cache retrieved secrets outside the request handler to avoid per-request latency, cost, and throttling.
- On Lambda, module-level state persists across warm invocations and is the natural cache location.
- Use a bounded TTL and re-fetch on authentication failure so rotation is handled correctly.
- The most common leak is logging a configuration object or connection string.
- Avoid passing secrets as command-line arguments, and review error handlers and crash reporters for what they capture.
