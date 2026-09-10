# Handler, Event & Context

Every Lambda function has the same shape: a handler taking an event and a context, returning a result. The details of each vary by language and by what invoked the function.

---

## The Handler

The handler is the entry point, named in the function's configuration as `file.function`:

```python
# file: app.py, configured as handler "app.handler"
def handler(event, context):
    return {"statusCode": 200, "body": "ok"}
```

*The configured handler string must match the file and function name exactly; a mismatch produces a runtime error rather than a deployment failure.*

Equivalent shapes in other runtimes:

```javascript
// Node.js — async handlers return a promise
export const handler = async (event, context) => {
  return { statusCode: 200, body: "ok" };
};
```

```java
// Java — implements a typed interface
public class Handler implements RequestHandler<Map<String,Object>, String> {
    public String handleRequest(Map<String,Object> event, Context context) {
        return "ok";
    }
}
```

*The pattern is identical across runtimes: an event in, a context alongside, a result out.*

---

## The Event

The event is the input, and **its shape depends entirely on what invoked the function**. This is the main source of confusion for people new to Lambda — there is no single event format.

**API Gateway (HTTP API, payload v2.0):**

```json
{
  "version": "2.0",
  "rawPath": "/orders/5501",
  "requestContext": { "http": { "method": "GET", "sourceIp": "203.0.113.42" } },
  "headers": { "authorization": "Bearer ..." },
  "queryStringParameters": { "include": "items" },
  "body": null,
  "isBase64Encoded": false
}
```

**S3 notification:**

```json
{
  "Records": [{
    "eventName": "ObjectCreated:Put",
    "s3": {
      "bucket": { "name": "uploads" },
      "object": { "key": "images/photo.jpg", "size": 102400 }
    }
  }]
}
```

**SQS:**

```json
{
  "Records": [{
    "messageId": "059f36b4-87a3-...",
    "body": "{\"orderId\": \"O-5501\"}",
    "receiptHandle": "AQEBwJnKyrHigUMZj6..."
  }]
}
```

*Three entirely different shapes. Note that S3 and SQS deliver a `Records` array — these sources batch, so a handler assuming a single record is a bug waiting for the second one.*

Two things follow. **Read the event documentation for the specific source**, since guessing the shape wastes time. And **log the raw event during development**, which is the fastest way to see exactly what arrives.

For API Gateway specifically, note that **`body` is a string**, not a parsed object, and may be base64-encoded when `isBase64Encoded` is true.

---

## The Context

The context provides information about the invocation and the environment:

| Property | Use |
|---|---|
| `aws_request_id` | Unique per invocation — log it for correlation |
| `get_remaining_time_in_millis()` | Milliseconds until timeout |
| `function_name`, `function_version` | Identity |
| `memory_limit_in_mb` | Configured memory |
| `log_group_name`, `log_stream_name` | Where output is going |

The remaining-time method is the genuinely useful one. It allows a function to stop work gracefully rather than being killed:

```python
def handler(event, context):
    for record in event["Records"]:
        if context.get_remaining_time_in_millis() < 5000:
            # Stop and let remaining records be retried
            break
        process(record)
```

*Checks the budget before each item, leaving time to return cleanly. A timeout kill produces no result and no clean shutdown.*

`aws_request_id` should be included in every log line. It is the key that ties a request's logs together, and it appears in the `REPORT` line CloudWatch writes for each invocation.

---

## The Return Value

What to return depends on the caller.

**API Gateway proxy integration** expects a specific shape:

```python
return {
    "statusCode": 200,
    "headers": {"Content-Type": "application/json"},
    "body": json.dumps({"orderId": "O-5501"}),
}
```

*`body` must be a string, not an object. Returning a dict produces a 502 from API Gateway, which is a confusing error for a function that ran correctly.*

**Asynchronous invocations** ignore the return value entirely.

**SQS batch processing** can return partial batch failures, which is covered with SQS.

**Errors** are signalled by raising an exception. What happens next depends on the invocation type — the subject of the retries note.

---

## Key Takeaways

- The handler signature is consistent across runtimes: an event, a context, and a returned result.
- The configured handler string must match the file and function name exactly.
- Event shape depends entirely on the invoking service, so read that service's documentation and log the raw event while developing.
- S3 and SQS deliver a `Records` array — handlers must loop rather than assume one record.
- API Gateway delivers `body` as a string, possibly base64-encoded.
- `context.get_remaining_time_in_millis()` allows graceful completion before a timeout kill.
- Log `aws_request_id` on every line to correlate an invocation's output.
- API Gateway proxy integration requires `statusCode` and a string `body`; returning an object produces a 502.
