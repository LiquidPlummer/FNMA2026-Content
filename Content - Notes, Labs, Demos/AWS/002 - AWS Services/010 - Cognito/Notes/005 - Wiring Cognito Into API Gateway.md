# Wiring Cognito Into API Gateway

Putting the pieces together: a browser authenticates against a user pool and calls an API that validates the resulting token.

---

## The Flow

```
1. Browser  ──credentials──►  Cognito user pool
2. Cognito  ──ID + access + refresh tokens──►  Browser
3. Browser  ──Authorization: Bearer <token>──►  API Gateway
4. API Gateway validates the token against the pool
5. API Gateway ──event with claims──►  Lambda
6. Lambda reads requestContext.authorizer for identity
```

*The function never validates a token. API Gateway does, and passes the verified claims through.*

---

## Configuring the Authorizer

**On an HTTP API**, a JWT authorizer:

```bash
aws apigatewayv2 create-authorizer \
  --api-id abc123 \
  --authorizer-type JWT \
  --name cognito-jwt \
  --identity-source '$request.header.Authorization' \
  --jwt-configuration 'Issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ABC123,Audience=1example23456789'
```

*`Issuer` is the pool's URL and `Audience` is the app client ID. The authorizer is then attached to routes.*

**On a REST API**, a Cognito user pool authorizer references the pool ARN directly and is attached per method.

---

## Reading Identity in the Function

Claims arrive in the event, with the shape differing by API type:

```python
def handler(event, context):
    # HTTP API (payload 2.0)
    claims = event["requestContext"]["authorizer"]["jwt"]["claims"]
    # REST API:
    # claims = event["requestContext"]["authorizer"]["claims"]

    user_id = claims["sub"]
    groups = claims.get("cognito:groups", "")
    return get_orders_for_user(user_id)
```

*`sub` is the stable user identifier and the right key for application data. Note that `cognito:groups` arrives as a comma-separated string in some payload formats and a list in others — worth checking rather than assuming.*

Two rules for using these claims:

**Trust `requestContext.authorizer` and nothing else.** These claims were verified by API Gateway. A user ID from a header, a query parameter, or the request body is client-supplied and must never be used for authorization.

**Use `sub`, not email or username.** `sub` is immutable; email and username can change, and using them as a key produces orphaned data when they do.

---

## Resource-Level Authorization

The authorizer establishes that the caller is a valid, authenticated user. **It does not establish that they may access the specific resource requested.**

```python
def handler(event, context):
    user_id = event["requestContext"]["authorizer"]["jwt"]["claims"]["sub"]
    order_id = event["pathParameters"]["orderId"]

    order = table.get_item(Key={"OrderId": order_id}).get("Item")
    if not order:
        return {"statusCode": 404, "body": ""}
    if order["CustomerId"] != user_id:
        return {"statusCode": 404, "body": ""}      # 404, not 403
    return {"statusCode": 200, "body": json.dumps(order)}
```

*The ownership check is the security control. Returning 404 rather than 403 avoids confirming that the resource exists to someone not entitled to know.*

Omitting this check is one of the most common API vulnerabilities: an authenticated user changes an identifier in the URL and receives another user's data. The authorizer worked correctly; the resource check was missing.

A stronger version avoids the read entirely by including the user in the key — a DynamoDB partition key of the user ID means another user's data cannot be fetched even by mistake.

---

## Handling Token Expiry in the Client

Tokens expire in an hour, and unhandled expiry logs users out mid-session:

```javascript
async function apiCall(path, options = {}) {
  let token = await getValidToken();          // refreshes if near expiry
  let res = await fetch(path, {
    ...options,
    headers: { ...options.headers, Authorization: `Bearer ${token}` },
  });
  if (res.status === 401) {
    token = await refreshTokens();            // refresh and retry once
    res = await fetch(path, {
      ...options,
      headers: { ...options.headers, Authorization: `Bearer ${token}` },
    });
  }
  return res;
}
```

*Refreshes proactively and retries once on a 401. The Amplify libraries handle this, which is a reason to use them rather than calling the endpoints directly.*

---

## Common Failures

**401 with a valid token.** Usually the wrong `Audience` — a token issued for a different app client of the same pool. Also check that the correct token type is being sent.

**CORS error on the auth header.** An `Authorization` header triggers a preflight; if `OPTIONS` requires authorization, the preflight fails. **`OPTIONS` routes must not require an authorizer.**

**Claims missing from the event.** Reading the wrong path for the API type — HTTP APIs nest claims under `jwt`, REST APIs do not.

**Users logged out after an hour.** No refresh handling in the client.

**Works locally, fails deployed.** Different app client IDs or pool IDs between environments — these belong in configuration, not in code.

---

## Key Takeaways

- API Gateway validates the token and passes verified claims to the function, which never validates anything itself.
- HTTP APIs nest claims under `requestContext.authorizer.jwt.claims`; REST APIs place them directly under `authorizer.claims`.
- Trust only claims from `requestContext.authorizer` — never a user identifier from a header, query string, or body.
- Use `sub` as the user key, since email and username can change.
- The authorizer proves identity, not entitlement — always check that this user may access this specific resource.
- Return 404 rather than 403 for resources the caller does not own, to avoid confirming existence.
- `OPTIONS` routes must not require an authorizer, or preflight requests fail as CORS errors.
- Clients must refresh proactively and retry once on 401, or users are logged out when tokens expire.
