# Retries, Errors & Execution Roles

What happens when a function fails depends entirely on how it was invoked — and the permissions it runs with come from a role that also governs whether its logs appear at all.

---

## Retry Behavior by Invocation Type

**Synchronous.** No retries by Lambda. The error is returned to the caller, which decides what to do. API Gateway returns 502; a direct `Invoke` receives the error payload. Any retry is the caller's, and AWS SDKs retry certain error classes automatically.

**Asynchronous.** Lambda retries **twice by default**, with delays, for a total of three attempts. Both the retry count (0–2) and the maximum event age (60 seconds to 6 hours) are configurable. After exhausting retries, the event goes to a **dead-letter queue** or an **on-failure destination**, or is discarded.

**Poll-based (SQS, Kinesis, DynamoDB Streams).** Retry behavior comes from the source. An SQS message returns to the queue after its visibility timeout and is redelivered until the queue's `maxReceiveCount` sends it to a dead-letter queue. Kinesis and DynamoDB Streams retry a batch until it succeeds or the records expire — **blocking the shard** meanwhile, unless bisect-on-error or a failure destination is configured.

| Invocation | Retries | Failure handling |
|---|---|---|
| Synchronous | None | Returned to caller |
| Asynchronous | 2 by default | DLQ or destination |
| SQS | Until `maxReceiveCount` | Queue's DLQ |
| Kinesis / DynamoDB Streams | Until success or expiry | Blocks the shard; configurable |

---

## Idempotency

Because retries happen, and because most event sources deliver at least once, **a function may run more than once for the same event**. This is not an edge case — it is the normal operating assumption.

The consequence: any function with side effects needs to tolerate repetition. Charging a card twice, sending two emails, or double-incrementing a counter are all outcomes of a retry that the platform considers successful behavior.

```python
def handler(event, context):
    order_id = event["orderId"]
    try:
        table.put_item(
            Item={"OrderId": order_id, "ProcessedAt": now()},
            ConditionExpression="attribute_not_exists(OrderId)",
        )
    except ClientError as e:
        if e.response["Error"]["Code"] == "ConditionalCheckFailedException":
            return {"status": "already processed"}
        raise
    charge_card(order_id)
```

*A conditional write claims the work atomically. A retry finds the item already present and exits without charging again.*

The general pattern is a deduplication record keyed by something stable in the event — an order ID, a message ID, or the SQS `messageId`. Note the ordering trade-off: claiming before doing the work risks a lost operation if the function then crashes, while doing the work first risks duplication. Which is safer depends on whether a missed operation or a duplicated one is worse.

---

## Destinations

For asynchronous invocations, **destinations** are richer than a dead-letter queue. They route both successes and failures, and include the request and response context:

```bash
aws lambda put-function-event-invoke-config \
  --function-name order-processor \
  --maximum-retry-attempts 1 \
  --destination-config '{
    "OnSuccess": {"Destination": "arn:aws:sqs:us-east-1:123456789012:processed"},
    "OnFailure": {"Destination": "arn:aws:sqs:us-east-1:123456789012:failed"}
  }'
```

*Failures carry the original event plus the error, which a DLQ does not — a DLQ receives only the event, leaving the failure reason to be found in logs.*

Destinations can target SQS, SNS, EventBridge, or another Lambda function.

---

## Execution Roles

Every function has an **execution role** — the IAM role it assumes when it runs. It provides the credentials the SDK finds automatically, and it is what governs everything the function can do.

**Logging permissions are part of it**, and their absence produces a distinctive failure:

```json
{
  "Effect": "Allow",
  "Action": ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"],
  "Resource": "arn:aws:logs:us-east-1:123456789012:log-group:/aws/lambda/order-processor:*"
}
```

*Without these, the function runs correctly and produces no logs at all — which is a confusing thing to debug, because there is nothing to read.*

The managed policy `AWSLambdaBasicExecutionRole` grants exactly this and is the standard starting point.

Beyond logging, grant only what the function needs, scoped to specific resources:

```json
{
  "Effect": "Allow",
  "Action": ["dynamodb:GetItem", "dynamodb:PutItem"],
  "Resource": "arn:aws:dynamodb:us-east-1:123456789012:table/Orders"
}
```

*One table, two actions. One role per function keeps this narrow; a shared role accumulates the union of every function's needs.*

Two more roles worth distinguishing. **VPC-attached functions** need `AWSLambdaVPCAccessExecutionRole` to create network interfaces. And the **resource policy** on the function — separate from the execution role — controls who may *invoke* it, which is how S3, EventBridge, and API Gateway are permitted to call it.

---

## Key Takeaways

- Synchronous invocations are not retried by Lambda; asynchronous ones retry twice by default; poll-based sources follow their own retry rules.
- Kinesis and DynamoDB Stream failures block the shard until success or expiry unless configured otherwise.
- Retries and at-least-once delivery mean functions with side effects must be idempotent.
- Use a conditional write on a stable event identifier to deduplicate, choosing the ordering that makes the safer failure.
- Destinations carry the error context that dead-letter queues do not, and can route successes as well.
- The execution role provides the function's credentials and must include CloudWatch Logs permissions, or the function runs with no output.
- Scope execution roles per function to specific resources; VPC access and invoke permissions are separate concerns.
