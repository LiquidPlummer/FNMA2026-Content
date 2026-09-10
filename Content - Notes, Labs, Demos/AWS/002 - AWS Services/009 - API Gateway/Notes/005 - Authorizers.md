# Authorizers

An **authorizer** decides whether a request proceeds, before the backend is invoked. API Gateway offers several kinds, suited to different callers.

---

## IAM Authorization

The caller signs the request with SigV4 using AWS credentials, and API Gateway verifies the signature and evaluates IAM policies.

Suited to:

- **Service-to-service calls** within AWS, where the caller has a role
- **Internal tools** used by people with AWS credentials
- **Cross-account access**, controlled by the API's resource policy

The appeal is that no token infrastructure is needed — the caller's existing IAM identity is the credential, and access is granted with `execute-api:Invoke` permissions.

The limitation is that the caller must have AWS credentials and be able to sign requests. That rules out browsers and mobile applications without an identity pool, and most third-party consumers.

---

## Cognito User Pool Authorizer (REST APIs)

The caller sends a JWT issued by a Cognito user pool. API Gateway validates the signature, issuer, and expiry, and passes the claims to the backend:

```
Authorization: Bearer eyJraWQiOiJ...
```

*The token's claims arrive in `requestContext.authorizer.claims`, so the function gets the authenticated identity without validating anything itself.*

This is the simplest option for an API fronting a Cognito-authenticated application. Configuration is a reference to the user pool, with no code to write or maintain.

---

## JWT Authorizer (HTTP APIs)

The HTTP API equivalent, and more general — it validates tokens from **any OIDC-compliant issuer**, not only Cognito:

```bash
aws apigatewayv2 create-authorizer \
  --api-id abc123 --authorizer-type JWT \
  --name jwt-auth --identity-source '$request.header.Authorization' \
  --jwt-configuration 'Issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ABC123,Audience=1example23456789'
```

*Validates signature, issuer, audience, and expiry. Works with Cognito, Auth0, Okta, or any OIDC provider.*

It can also require specific **scopes** per route, which handles coarse authorization without any code.

For a token-authenticated API, this is the best option available — no Lambda, no cost per authorization, and no code to maintain.

---

## Lambda Authorizers

A Lambda function decides. It receives the request, performs whatever logic is needed, and returns an IAM policy allowing or denying the call.

Two types:

**Token authorizers** receive a single header value — the usual choice for bearer tokens.

**Request authorizers** receive the full request context — headers, query string, path, source IP — for decisions needing more than a token.

```python
def handler(event, context):
    token = event["authorizationToken"].replace("Bearer ", "")
    principal = validate(token)          # custom validation logic
    return {
        "principalId": principal["sub"],
        "policyDocument": {
            "Version": "2012-10-17",
            "Statement": [{
                "Action": "execute-api:Invoke",
                "Effect": "Allow",
                "Resource": event["methodArn"],
            }],
        },
        "context": {"tenantId": principal["tenant"]},
    }
```

*The returned policy allows or denies. The `context` object is passed to the backend in `requestContext.authorizer`, which is how additional attributes reach the function.*

Lambda authorizers are for anything the built-in options do not cover: a custom token format, a legacy authentication system, per-tenant logic, or authorization requiring a database lookup.

**Caching is important here.** The result can be cached by the identity source for up to an hour. Without caching, every request invokes the authorizer, adding latency and cost to each call:

```bash
--authorizer-result-ttl-in-seconds 300
```

*Five minutes of caching. The trade-off is that a revoked token remains accepted until the cache entry expires, so the TTL is a balance between cost and revocation latency.*

The `Resource` returned also matters. Returning `event["methodArn"]` scopes the cached policy to one method; returning a wildcard caches a decision covering the whole API, which is more efficient and less precise.

---

## Choosing

| Caller | Authorizer |
|---|---|
| AWS service or role | IAM |
| Browser or mobile app with Cognito | JWT (HTTP API) or Cognito authorizer (REST) |
| App using Auth0, Okta, or another OIDC provider | JWT (HTTP API) |
| Third-party with an API key | Usage plan (REST), plus another authorizer |
| Anything with custom or legacy authentication | Lambda authorizer |
| Public endpoint | None — but consider throttling and WAF |

**API keys are not authentication.** They identify a caller for usage plans and quotas; they are not secret in any meaningful sense and should not be the only control on a sensitive API.

---

## What the Backend Still Owes

An authorizer establishes *who* is calling. It rarely establishes *what they may access*.

A valid token proves the caller is a legitimate user. It does not prove they may read order `O-5501`. That check belongs in the backend, using the identity the authorizer supplied.

Skipping it produces one of the most common API vulnerabilities: an authenticated user changing an identifier in a URL and receiving someone else's data. The authorizer did its job correctly; the resource-level check was missing.

---

## Key Takeaways

- IAM authorization uses SigV4-signed requests and suits service-to-service and internal callers with AWS credentials.
- HTTP APIs' JWT authorizer validates tokens from any OIDC provider and can enforce scopes per route, with no code.
- REST APIs offer a Cognito user pool authorizer; other token formats require a Lambda authorizer.
- Lambda authorizers return an IAM policy and can pass extra attributes to the backend via the `context` object.
- Cache Lambda authorizer results to avoid invoking on every request, accepting delayed revocation as the trade.
- API keys identify callers for usage plans and are not authentication.
- Authorizers establish identity, not resource-level authorization — the backend must still check that this user may access this specific resource.
