# Tokens & JWT Validation

A user pool issues three tokens on successful authentication. They have different purposes, and using the wrong one is a common and consequential mistake.

---

## The Three Tokens

**ID token.** Contains identity claims — who the user is. Email, name, custom attributes, group membership. Default lifetime one hour.

**Access token.** Contains authorization claims — scopes and groups, but not profile attributes. Intended for authorizing API calls. Default lifetime one hour.

**Refresh token.** Used to obtain new ID and access tokens without re-authenticating. Default lifetime 30 days, configurable from 1 hour to 10 years.

```json
// ID token claims
{
  "sub": "a1b2c3d4-...",
  "email": "user@example.com",
  "custom:tenant_id": "acme-corp",
  "cognito:groups": ["admins"],
  "token_use": "id",
  "aud": "1example23456789",
  "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ABC123",
  "exp": 1757430000
}
```

*Note `token_use`, which distinguishes token types, and `aud`, which names the app client the token was issued for.*

---

## Which Token to Send

**The specification-correct answer is the access token** for authorizing API calls, with the ID token reserved for the client to learn about the user.

**In practice, Cognito's ID token is frequently used** because the access token carries no user attributes — no email, no custom claims — and applications often need them. API Gateway's Cognito authorizer accepts either.

Two workable positions:

**Send the access token** and use pre-token-generation to add the needed claims to it. Specification-correct, and requires configuration.

**Send the ID token** and validate it strictly, checking `token_use` is `id`. Pragmatic, widely done, and requires being deliberate about it.

What matters is choosing one and validating accordingly. The failure to avoid is accepting either without checking `token_use`, which allows a token intended for one purpose to be used for another.

---

## Validating a JWT

If API Gateway's authorizer validates the token, the backend does not repeat the work — it reads the claims from `requestContext.authorizer`. Where validation is done in application code, all of these checks are required:

**1. Verify the signature** against the pool's public keys, fetched from the JWKS endpoint:

```
https://cognito-idp.<region>.amazonaws.com/<userPoolId>/.well-known/jwks.json
```

*The keys are stable; fetching them on every request is wasteful, so cache them with a periodic refresh.*

**2. Check the issuer (`iss`)** matches the expected user pool.

**3. Check the audience (`aud`)** matches the expected app client — otherwise a token from a different client of the same pool is accepted.

**4. Check expiry (`exp`)** and not-before, allowing small clock skew.

**5. Check `token_use`** is the expected type.

**6. Verify the algorithm is `RS256`.** Rejecting `none` and refusing to let the token's own header select the algorithm closes a classic JWT vulnerability.

```python
import jwt
from jwt import PyJWKClient

jwks = PyJWKClient(f"https://cognito-idp.{region}.amazonaws.com/{pool_id}/.well-known/jwks.json")

def verify(token):
    key = jwks.get_signing_key_from_jwt(token).key
    claims = jwt.decode(
        token, key,
        algorithms=["RS256"],
        audience=CLIENT_ID,
        issuer=f"https://cognito-idp.{region}.amazonaws.com/{pool_id}",
    )
    if claims["token_use"] != "id":
        raise ValueError("wrong token type")
    return claims
```

*Uses a maintained library. Writing JWT validation by hand is a reliable way to introduce a security flaw — the failure modes are subtle and well documented by attackers.*

---

## Whose Job Validation Is

| Setup | Validator |
|---|---|
| API Gateway with a Cognito or JWT authorizer | API Gateway |
| ALB with OIDC authentication | The ALB |
| Application receiving tokens directly | The application |
| Lambda authorizer | The authorizer function |

**Where API Gateway validates, the backend must still not trust arbitrary input** — it reads claims from `requestContext.authorizer`, which API Gateway populated, and not from a header the client controlled.

---

## Expiry and Refresh

Access and ID tokens expire in an hour by default. Clients use the refresh token to obtain new ones:

```
POST /oauth2/token
grant_type=refresh_token&client_id=...&refresh_token=...
```

*Returns new ID and access tokens without re-authentication.*

Practical points:

**Handle expiry in the client.** Refresh proactively before expiry, or on a 401, then retry once. Not doing so logs users out mid-session.

**Refresh tokens can be revoked**, which invalidates the session — the mechanism behind global sign-out.

**Access and ID tokens cannot be revoked.** They are valid until they expire, so a compromised or logged-out token remains usable for up to its lifetime. This is why short token lifetimes matter, and why a shorter lifetime is the main mitigation available.

**Store tokens carefully in browsers.** `localStorage` is readable by any script on the page, so an XSS flaw yields the token. HTTP-only cookies avoid that and introduce CSRF considerations. Neither is unambiguously correct; the choice depends on the application's threat model and should be deliberate.

---

## Key Takeaways

- User pools issue an ID token with identity claims, an access token for authorization, and a refresh token for renewal.
- The access token is specification-correct for API calls, but carries no user attributes — many applications send the ID token instead.
- Always check `token_use` so a token cannot be used for the wrong purpose.
- Validation means checking signature against JWKS, issuer, audience, expiry, token use, and algorithm.
- Cache JWKS keys rather than fetching per request, and use a maintained JWT library.
- Where API Gateway validates, read claims from `requestContext.authorizer`, not from client-supplied headers.
- Refresh tokens can be revoked; access and ID tokens cannot, so short lifetimes are the main mitigation.
- Browser token storage is a deliberate trade between XSS exposure in `localStorage` and CSRF considerations with cookies.
