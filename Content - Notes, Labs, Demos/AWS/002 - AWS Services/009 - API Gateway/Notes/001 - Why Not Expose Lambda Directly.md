# Why Not Expose Lambda Directly

A Lambda function can be invoked over HTTPS through a **function URL**, with no API Gateway at all. Knowing when that is sufficient — and what API Gateway adds — makes the choice deliberate rather than habitual.

---

## Function URLs

A function URL is a dedicated HTTPS endpoint on a Lambda function:

```bash
aws lambda create-function-url-config \
  --function-name order-processor \
  --auth-type AWS_IAM
```

*Produces a URL like `https://abc123.lambda-url.us-east-1.on.aws/`. `AWS_IAM` requires SigV4-signed requests; `NONE` makes it public.*

They are free, immediate, and adequate for a webhook receiver, an internal tool, or a single-purpose endpoint.

What they do not provide: request validation, usage plans, API keys, per-route authorizers, request and response transformation, caching, custom domains without CloudFront, or routing to anything other than that one function.

---

## What API Gateway Adds

**Routing.** One API surface across many functions and backends. `/orders` to one function, `/customers` to another, `/legacy/*` to an existing HTTP service — under one domain.

**Authorization at the edge.** Requests are authenticated and authorized before the backend is invoked. An unauthorized request costs an API Gateway request rather than a Lambda invocation, and the authorization logic is not duplicated in every function.

**Throttling and quotas.** Per-client rate limits and monthly quotas, enforced before anything downstream runs. Without this, a client's traffic reaches the function directly and the concurrency limit becomes the only protection.

**Request validation.** Malformed requests rejected against a schema before invoking anything.

**A stable contract.** The API's shape is independent of what implements it. A route can move from Lambda to a container without clients noticing.

**Stages.** Separate deployments — `dev`, `prod` — with independent configuration and their own throttling settings.

**Caching.** Responses cached at the gateway, so repeated requests never reach the backend.

**Usage plans and API keys** for exposing an API to identified consumers.

**Consolidated logging and metrics** across every route.

---

## The Trade

API Gateway adds cost — roughly $1 per million requests for HTTP APIs and $3.50 for REST APIs, plus data transfer — and a layer to configure and understand. It also adds latency, though typically small.

The honest comparison:

| Situation | Choice |
|---|---|
| One webhook receiver | Function URL |
| Internal tool, IAM-authenticated | Function URL |
| A public API with several routes | API Gateway |
| Anything needing rate limiting per client | API Gateway |
| Anything needing request validation | API Gateway |
| Anything with a token-based auth model | API Gateway |
| Static site with a few API calls | Either, with CloudFront in front |

---

## The Third Option

An **ALB can also invoke Lambda**, via a Lambda target group. This suits teams already running an ALB, or an architecture mixing container and Lambda backends behind one load balancer.

Comparing the three:

- **Function URL** — free, single function, minimal features.
- **API Gateway** — per-request pricing, richest feature set, API-oriented.
- **ALB** — hourly pricing plus capacity units, cheaper at very high volume, fewer API features.

The pricing crossover is worth knowing: API Gateway's per-request model is cheaper at low volume, while an ALB's hourly charge amortizes at high volume. For a low-traffic API, API Gateway usually costs less than running an ALB continuously.

---

## The Practical Default

**Use API Gateway for anything that is genuinely an API** — several routes, external consumers, authentication, or a contract that should outlast its implementation.

**Use a function URL for a single endpoint** with no need for the surrounding features, particularly internal or machine-to-machine ones.

The mistake worth avoiding is reaching for API Gateway reflexively for a single internal webhook, and equally, building a public multi-route API on function URLs and then reimplementing authorization, throttling, and validation inside every function.

---

## Key Takeaways

- Lambda function URLs provide a free HTTPS endpoint with optional IAM authentication and no additional features.
- API Gateway adds routing across backends, edge authorization, throttling, quotas, request validation, stages, and caching.
- Authorizing at the gateway means unauthorized requests never invoke a function and the logic is not duplicated.
- API Gateway costs per request and adds a small amount of latency.
- An ALB can also invoke Lambda, which suits mixed container and function backends.
- API Gateway is cheaper than an ALB at low volume; the ALB's hourly charge amortizes at high volume.
- Use function URLs for single internal endpoints and API Gateway for anything that is genuinely an API.
