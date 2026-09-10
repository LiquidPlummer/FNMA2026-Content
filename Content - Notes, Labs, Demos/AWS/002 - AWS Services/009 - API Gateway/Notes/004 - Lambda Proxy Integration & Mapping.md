# Lambda Proxy Integration & Mapping

How a request reaches a Lambda function and how the response gets back. The proxy integration is the common case, and it imposes a specific contract that produces a distinctive failure when broken.

---

## Proxy Integration

With **Lambda proxy integration**, API Gateway passes the entire request to the function as a structured event and expects a specifically shaped response:

```python
def handler(event, context):
    order_id = event["pathParameters"]["orderId"]
    return {
        "statusCode": 200,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps({"orderId": order_id, "status": "shipped"}),
    }
```

*The contract: `statusCode` is required, and `body` **must be a string**. Returning a dict as `body` produces a 502 Bad Gateway from API Gateway.*

That 502 is worth recognizing, because it is confusing — the function ran successfully, CloudWatch shows no error, and the client receives a gateway error. The cause is almost always a malformed response shape.

The full response format:

```json
{
  "statusCode": 200,
  "headers": { "Content-Type": "application/json" },
  "multiValueHeaders": { "Set-Cookie": ["a=1", "b=2"] },
  "body": "{\"orderId\":\"O-5501\"}",
  "isBase64Encoded": false
}
```

*`multiValueHeaders` is needed for headers appearing more than once. `isBase64Encoded` signals binary content.*

**HTTP APIs with payload format 2.0 are more forgiving** — returning a plain object or string is treated as a 200 with that body serialized as JSON. The explicit format still works and is clearer when status codes vary.

---

## What Arrives in the Event

The proxy event carries the whole request:

```json
{
  "resource": "/orders/{orderId}",
  "path": "/orders/O-5501",
  "httpMethod": "GET",
  "headers": { "Authorization": "Bearer ...", "Content-Type": "application/json" },
  "queryStringParameters": { "include": "items" },
  "pathParameters": { "orderId": "O-5501" },
  "requestContext": {
    "requestId": "abc-123",
    "identity": { "sourceIp": "203.0.113.42" },
    "authorizer": { "claims": { "sub": "user-789" } }
  },
  "body": "{...}",
  "isBase64Encoded": false
}
```

*This is the REST API (format 1.0) shape. Note `body` is a string requiring parsing, and `requestContext.authorizer` carries whatever an authorizer added — which is how identity reaches the function.*

Three details that cause bugs:

**`queryStringParameters` is `null`** when there are none, not an empty object. Indexing it directly fails.

**Header names vary in case.** HTTP headers are case-insensitive, and what arrives may not match what was sent. HTTP APIs lowercase them; REST APIs preserve the client's casing.

**Binary content is base64-encoded** when `isBase64Encoded` is true, and the API's binary media types must be configured for this to work.

---

## Non-Proxy Integration and Mapping Templates

The alternative, available on REST APIs, uses **mapping templates** written in VTL (Velocity Template Language) to transform the request before it reaches the backend and the response on the way out.

```velocity
## Request mapping template
{
  "orderId": "$input.params('orderId')",
  "requestedBy": "$context.authorizer.claims.sub",
  "source": "api-gateway"
}
```

*Constructs a custom payload, so the function receives a clean domain object rather than an HTTP envelope.*

This is genuinely useful in two cases:

**Integrating a backend whose shape cannot change** — a legacy service, or an AWS service called directly.

**Direct AWS service integration**, which removes Lambda entirely. A REST API can put a message on SQS or write to DynamoDB using a mapping template.

The cost is significant, and worth being honest about: **VTL is difficult to write, difficult to test, and difficult to debug.** It is logic in configuration rather than in code — no unit tests, no type checking, and errors that surface as gateway failures with little detail. For most APIs, proxy integration with the transformation in the function is clearer and more maintainable.

---

## Request Validation

REST APIs can validate requests against a JSON Schema model before invoking anything:

```json
{
  "type": "object",
  "required": ["customerId", "items"],
  "properties": {
    "customerId": { "type": "string" },
    "items": { "type": "array", "minItems": 1 }
  }
}
```

*A malformed request is rejected with a 400 at the gateway, costing an API Gateway request rather than a Lambda invocation.*

This is a REST-only feature. On an HTTP API, validation happens in the function — which costs an invocation but keeps the schema alongside the code that uses it.

---

## Key Takeaways

- Lambda proxy integration passes the full request as a structured event and requires `statusCode` with a **string** `body`.
- Returning an object as `body` produces a 502 even though the function succeeded — the usual cause of that error.
- HTTP API payload format 2.0 accepts a plain object or string as a 200 response.
- `queryStringParameters` is null rather than empty when absent, and header casing varies by API type.
- `requestContext.authorizer` carries claims added by an authorizer, which is how identity reaches the function.
- Mapping templates transform requests and responses in VTL, enabling direct AWS service integration without Lambda.
- VTL is hard to test and debug, so prefer proxy integration with transformation in code unless a specific need requires otherwise.
- REST APIs validate requests against JSON Schema at the gateway; HTTP APIs validate in the function.
