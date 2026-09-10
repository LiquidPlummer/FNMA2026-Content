# REST APIs vs HTTP APIs

API Gateway offers two products with confusingly similar names. **HTTP APIs** are newer, cheaper, and faster; **REST APIs** have more features. The naming is unhelpful — both serve REST-style HTTP APIs.

---

## The Comparison

| | REST API | HTTP API |
|---|---|---|
| Cost per million requests | ~$3.50 | ~$1.00 |
| Latency | Higher | Lower |
| Lambda / HTTP integration | Yes | Yes |
| Direct AWS service integration | Extensive | Limited |
| JWT authorizer | No | **Yes, built in** |
| Lambda authorizer | Yes | Yes |
| IAM authorization | Yes | Yes |
| Cognito user pool authorizer | **Yes, built in** | Via JWT authorizer |
| Request validation | **Yes** | No |
| Request/response transformation | **Yes, extensive** | Minimal |
| API keys and usage plans | **Yes** | No |
| Caching | **Yes** | No |
| WAF integration | **Yes** | No |
| Private APIs (VPC endpoint) | **Yes** | No |
| Canary deployments | **Yes** | No |
| Edge-optimized endpoint | **Yes** | No |
| WebSocket | Separate WebSocket API | No |

---

## When Each Is Right

**Use an HTTP API** for the common case: a JSON API backed by Lambda, authenticated with JWTs from Cognito or another OIDC provider. It is roughly a third of the cost, measurably lower latency, and simpler to configure. For most new serverless APIs this is the right choice.

**Use a REST API** when a specific feature requires it:

- **API keys and usage plans** — exposing an API to identified third parties with quotas.
- **Request validation** at the gateway, rejecting malformed requests before invoking anything.
- **Response caching** at the gateway.
- **AWS WAF** in front of the API.
- **Private APIs** reachable only through a VPC endpoint.
- **Extensive request or response transformation**, particularly when integrating a legacy backend.
- **Direct AWS service integration** without Lambda — writing to DynamoDB or SQS straight from the gateway.

That last one is worth noting as a genuine architectural option: a REST API can put a message on a queue or write an item to DynamoDB with no Lambda function at all. It removes a component, and it also moves logic into gateway configuration, which is harder to test and review than code.

---

## Two Differences Worth Detailing

**Payload format versions.** HTTP APIs default to payload format 2.0, which has a different event shape from REST APIs' 1.0 — `rawPath` instead of `path`, `requestContext.http.method` instead of `httpMethod`, and a simplified response format where returning a plain object or string is treated as a 200 response body.

This matters when migrating: a Lambda function written for a REST API will not work unchanged behind an HTTP API. The format can be pinned to 1.0 on an HTTP API to ease this.

**Authorization.** HTTP APIs include a built-in **JWT authorizer** that validates tokens against an OIDC issuer with no Lambda involved. REST APIs have no equivalent — they offer a Cognito user pool authorizer, or a Lambda authorizer for anything else.

For an API authenticated by Cognito or another OIDC provider, the HTTP API's JWT authorizer is simpler and cheaper than either REST alternative.

---

## Migration and Coexistence

The two are separate resources, not modes of one. Converting means creating a new API and repointing clients — helped considerably by a custom domain, since the domain can be moved between APIs without clients changing anything.

Running both is also reasonable. A custom domain can map different base paths to different APIs, so a REST API serving a legacy integration and an HTTP API serving new routes can share one hostname.

---

## Which to Start With

**Start with an HTTP API** unless a REST-only feature is known to be needed. It is cheaper and simpler, and the missing features are frequently obtainable elsewhere:

- **Caching** → CloudFront in front of the API
- **WAF** → attach to a CloudFront distribution instead
- **Request validation** → validate in the function
- **Rate limiting per client** → implement in the application, or use a Lambda authorizer

Those substitutes are not always equivalent — WAF at CloudFront protects a different boundary than WAF at the gateway, and validation in the function still costs an invocation. But they cover many cases at a third of the request cost.

---

## Key Takeaways

- HTTP APIs cost roughly a third of REST APIs and have lower latency; REST APIs have more features.
- HTTP APIs include a built-in JWT authorizer, which REST APIs lack.
- REST APIs uniquely offer request validation, caching, API keys and usage plans, WAF, private APIs, and extensive transformation.
- REST APIs can integrate directly with AWS services, removing Lambda from simple paths at the cost of logic living in configuration.
- Payload format 2.0 on HTTP APIs has a different event and response shape, so functions are not portable unchanged.
- The two are separate resources; a custom domain allows repointing or running both under one hostname.
- Start with an HTTP API unless a REST-only feature is required, substituting CloudFront for caching and WAF where possible.
