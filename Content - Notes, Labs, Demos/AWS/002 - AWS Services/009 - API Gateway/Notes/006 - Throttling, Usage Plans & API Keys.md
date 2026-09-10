# Throttling, Usage Plans & API Keys

Rate limiting at the gateway protects everything behind it. Without it, a client's traffic reaches the backend directly and the only limit is whatever the backend runs out of first.

---

## Throttling Levels

Throttling applies at several levels, and the **most restrictive wins**:

| Level | Scope |
|---|---|
| Account | All APIs in the region — a default of 10,000 requests/second |
| Stage | Every route in a stage |
| Method / route | One specific route |
| Usage plan | One API key's traffic |

Two parameters at each level:

**Rate** — steady-state requests per second.
**Burst** — the bucket size, allowing a short spike above the rate.

The mechanism is a token bucket. Tokens refill at the rate; a burst can consume up to the bucket size at once. So a rate of 100 with a burst of 200 permits a momentary spike of 200 followed by 100/second sustained.

```bash
aws apigateway update-stage --rest-api-id abc123 --stage-name prod \
  --patch-operations \
    op=replace,path=/throttle/rateLimit,value=500 \
    op=replace,path=/throttle/burstLimit,value=1000
```

*Stage-level throttling. Individual methods can be set lower — an expensive report endpoint limited well below the rest of the API.*

Exceeding a limit returns **429 Too Many Requests**.

---

## Why It Matters

Without gateway throttling, a client's traffic passes straight through. The consequences depend on the backend:

- **Lambda** — consumes account concurrency, throttling every other function in the region.
- **RDS** — exhausts the connection limit, affecting everything using that database.
- **A third-party API** — hits its rate limit, potentially with a cost or a ban.

Method-level throttling is the useful refinement. A `GET /orders` costing one DynamoDB read and a `POST /reports` triggering a two-minute job should not share a limit.

---

## Usage Plans and API Keys

For APIs with identified consumers, a **usage plan** attaches throttling and quotas to an **API key**. This is a REST API feature; HTTP APIs do not have it.

```bash
aws apigateway create-usage-plan --name "standard-tier" \
  --throttle rateLimit=50,burstLimit=100 \
  --quota limit=100000,period=MONTH \
  --api-stages apiId=abc123,stage=prod
```

*Fifty requests per second and 100,000 per month per key. Keys are then associated with the plan and distributed to consumers.*

Clients send the key in a header:

```
x-api-key: aBcDeF123456
```

Usage plans give per-consumer rate limits, monthly quotas, tiering (free, standard, premium), and per-key usage data for billing or monitoring.

**API keys are not authentication.** They are sent in a header, appear in logs and browser tooling, and are not secret. They identify a caller for metering. An API handling anything sensitive needs a real authorizer alongside the key.

---

## Client-Side Handling

A 429 is a normal response, not an error condition, and clients should handle it properly:

**Retry with exponential backoff and jitter.** Retrying immediately makes the situation worse; retrying in lockstep across many clients produces synchronized bursts.

**Respect `Retry-After`** where present.

**Do not retry indefinitely.** A bounded number of attempts, then a clear failure.

AWS SDKs implement this for AWS APIs; a custom client calling our API needs it written.

---

## What Throttling Does Not Solve

**It does not distinguish legitimate from malicious traffic.** A limit high enough for real users is high enough for a determined attacker to be a problem. AWS WAF, attached to a REST API or to CloudFront in front of an HTTP API, provides rate-based rules by IP, geographic blocking, and pattern matching.

**It does not protect against expensive individual requests.** A single query scanning an entire table is one request. Cost control there belongs in the backend.

**Account-level throttling is shared.** One API consuming the regional limit affects the others, so stage and method limits matter as much for isolation between our own APIs as for protection from clients.

---

## Key Takeaways

- Throttling applies at account, stage, method, and usage plan levels, with the most restrictive winning.
- Rate is sustained requests per second and burst is the token bucket size allowing short spikes.
- Without gateway throttling, client traffic reaches the backend and consumes Lambda concurrency or database connections.
- Set method-level limits lower for expensive endpoints rather than relying on one stage-wide limit.
- Usage plans attach rate limits and monthly quotas to API keys, and are a REST API feature only.
- API keys identify callers for metering and are not authentication.
- Clients should retry 429s with exponential backoff and jitter, bounded by a retry limit.
- Throttling does not distinguish malicious traffic — that is what WAF is for.
