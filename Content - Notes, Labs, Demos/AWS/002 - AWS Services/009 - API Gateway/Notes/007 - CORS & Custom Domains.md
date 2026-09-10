# CORS & Custom Domains

Two things every browser-facing API needs. CORS fails in a distinctive and confusing way, and custom domains are what keep clients from hard-coding an API's identity.

---

## What CORS Actually Is

**Cross-Origin Resource Sharing** is a browser security mechanism. When JavaScript on `https://app.example.com` calls `https://api.example.com`, the browser checks whether the API permits it — and blocks the response if not.

Three points make the failures comprehensible:

**It is enforced by the browser, not the server.** The request often reaches the API and is processed successfully; the browser then refuses to give the response to the JavaScript that asked for it.

**It does not apply outside browsers.** `curl`, Postman, and server-to-server calls are unaffected. This is why "it works in Postman and fails in the browser" is the signature CORS symptom.

**It is not a security control for the API.** It restricts what browser JavaScript from other origins may do. It does not prevent anyone from calling the API directly.

---

## Preflight Requests

For anything beyond a simple request — a custom header, a non-simple content type, or a method other than GET, HEAD, or POST — the browser first sends an `OPTIONS` request:

```
OPTIONS /orders HTTP/1.1
Origin: https://app.example.com
Access-Control-Request-Method: POST
Access-Control-Request-Headers: authorization, content-type
```

The API must answer with the permitted origins, methods, and headers:

```
Access-Control-Allow-Origin: https://app.example.com
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type
Access-Control-Max-Age: 3600
```

*`Max-Age` caches the preflight result, avoiding an extra round trip on every subsequent request.*

An `Authorization` header alone triggers a preflight, so essentially every authenticated API call from a browser makes two requests unless the preflight is cached.

---

## Configuring It

**On an HTTP API**, CORS is configuration:

```bash
aws apigatewayv2 update-api --api-id abc123 --cors-configuration \
  'AllowOrigins=https://app.example.com,AllowMethods=GET,POST,OPTIONS,AllowHeaders=authorization,content-type,MaxAge=3600'
```

*API Gateway answers preflights itself; the backend never sees them.*

**On a REST API**, "Enable CORS" in the console creates an `OPTIONS` method with a mock integration returning the headers. With **proxy integration**, this handles the preflight but **the function must still return CORS headers on the actual response** — because proxy integration passes responses through unchanged.

This split is the most common CORS failure with REST APIs: the preflight succeeds, the real request is processed, and the browser blocks the response because it lacks the headers.

```python
CORS_HEADERS = {
    "Access-Control-Allow-Origin": "https://app.example.com",
    "Access-Control-Allow-Credentials": "true",
}

def handler(event, context):
    return {
        "statusCode": 200,
        "headers": {**CORS_HEADERS, "Content-Type": "application/json"},
        "body": json.dumps(result),
    }
```

*Headers must be on **every** response, including error responses — an error path that omits them produces a CORS error masking the real failure.*

---

## Why CORS Failures Are Confusing

The symptoms rarely point at the cause:

- **The browser reports a CORS error for a 500.** The request failed, the error response lacked CORS headers, and the browser reports the CORS problem instead of the actual error.
- **A 403 from an authorizer appears as a CORS error**, because gateway error responses do not carry CORS headers by default. REST APIs allow configuring gateway responses to include them, which is worth doing.
- **Credentials require exact origins.** With `Access-Control-Allow-Credentials: true`, the origin cannot be `*`. It must name the specific origin.

**The best fix is to avoid CORS entirely.** Serving the front end and the API from the same origin — CloudFront routing `/api/*` to API Gateway and everything else to S3 — makes every request same-origin. No preflights, no headers, no class of failure.

---

## Custom Domains

The generated endpoint embeds the API ID and puts the stage in the path:

```
https://abc123.execute-api.us-east-1.amazonaws.com/prod/orders
```

Clients hard-coding this are coupled to both the API's identity and its stage. A custom domain removes that:

```bash
aws apigatewayv2 create-domain-name --domain-name api.example.com \
  --domain-name-configurations CertificateArn=arn:aws:acm:us-east-1:...:certificate/abc

aws apigatewayv2 create-api-mapping --domain-name api.example.com \
  --api-id abc123 --stage prod --api-mapping-key v1
```

*Maps `api.example.com/v1` to the `prod` stage. A Route 53 alias record then points the domain at the API Gateway endpoint.*

Two endpoint types, with the certificate rule from the CloudFront lesson applying:

- **Regional** — served from the API's region; the certificate must be in **that region**.
- **Edge-optimized** — served through CloudFront; the certificate must be in **`us-east-1`**.

Custom domains also enable versioning by base path, so `api.example.com/v1` and `api.example.com/v2` map to different APIs under one hostname — and repointing a mapping moves clients between APIs with no client change.

---

## Key Takeaways

- CORS is enforced by the browser, so a request may succeed while the browser blocks the response — hence "works in Postman."
- CORS is not a security control for the API; it constrains browser JavaScript from other origins.
- Preflight `OPTIONS` requests are triggered by custom headers, including `Authorization`; `Max-Age` caches the result.
- HTTP APIs handle CORS as configuration; REST APIs with proxy integration require the function to return headers on every response.
- Error responses must carry CORS headers too, or a real error appears as a CORS failure.
- With credentials enabled, the allowed origin must be explicit rather than `*`.
- Serving the front end and API from one origin via CloudFront removes CORS entirely.
- Custom domains decouple clients from the API ID and stage; regional endpoints need a same-region certificate and edge-optimized ones need `us-east-1`.
