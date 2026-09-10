# Triggering Lambda & Message Limits

Connecting a queue to a Lambda function is the most common SQS consumer pattern. The integration handles polling for us and has behaviors worth understanding, particularly around partial failures.

---

## The Event Source Mapping

An **event source mapping** polls the queue and invokes the function with batches:

```bash
aws lambda create-event-source-mapping \
  --function-name order-processor \
  --event-source-arn arn:aws:sqs:us-east-1:123456789012:orders \
  --batch-size 10 \
  --maximum-batching-window-in-seconds 5 \
  --function-response-types ReportBatchItemFailures
```

*Lambda polls, batches up to 10 messages or waits 5 seconds, and invokes the function. `ReportBatchItemFailures` enables partial batch responses.*

What Lambda handles for us: polling with long polling, scaling consumers with queue depth, and **deleting messages automatically when the function returns successfully**.

That last point is the critical difference from a self-managed consumer. **The function must not delete messages itself** — Lambda deletes the whole batch on success. Deleting manually and then failing produces confusing behavior.

---

## Partial Batch Failures

The default behavior is coarse: **if the function raises an exception, the entire batch returns to the queue.** A batch of 10 where message 7 fails means all 10 are retried — and the 9 that succeeded are processed again.

`ReportBatchItemFailures` fixes this. The function returns the identifiers of failed messages, and only those return to the queue:

```python
def handler(event, context):
    failures = []
    for record in event["Records"]:
        try:
            process(json.loads(record["body"]))
        except Exception:
            logger.exception("failed", extra={"messageId": record["messageId"]})
            failures.append({"itemIdentifier": record["messageId"]})
    return {"batchItemFailures": failures}
```

*Only failed messages return to the queue. This requires `ReportBatchItemFailures` on the event source mapping — without it, the return value is ignored and the whole batch is retried.*

**Enable this on every SQS-triggered function.** Without it, one bad message in a batch causes nine unnecessary reprocessings, which both wastes work and makes idempotency failures far more likely.

---

## Scaling

Lambda scales SQS consumers automatically: starting with five concurrent invocations, adding up to 60 more per minute while the backlog grows, and reducing as it drains.

Two consequences worth planning for:

**Concurrency can climb quickly.** A large backlog produces many concurrent invocations, consuming account concurrency and possibly overwhelming a downstream database. **Reserved concurrency on the function** is the control — it caps consumption while messages wait safely in the queue.

**Scaling reacts to backlog**, so a sudden burst is processed with some delay while Lambda scales up. This is usually acceptable; the queue is absorbing the burst by design.

---

## Configuration That Must Line Up

Three settings interact, and a mismatch causes duplicate processing:

```
Function timeout        ≤  Visibility timeout
Visibility timeout      ≥  Function timeout × batch processing
```

**The visibility timeout must be at least the function timeout**, and AWS recommends six times it for safety with batches. If the function can run for 60 seconds and the visibility timeout is 30, messages reappear while still being processed.

Also note that **the batch's messages share one visibility timeout** starting when the batch is received, so a batch of 10 messages taking 10 seconds each needs a timeout covering the whole batch.

---

## Message Size and Large Payloads

**Messages are limited to 256 KB**, including attributes. Two approaches for larger payloads:

**Store in S3, send a reference.** The standard pattern:

```python
key = f"payloads/{uuid4()}.json"
s3.put_object(Bucket="payloads", Key=key, Body=json.dumps(large_payload))
sqs.send_message(QueueUrl=queue_url, MessageBody=json.dumps({"s3Key": key}))
```

*The queue carries a pointer; the consumer fetches the object. This also keeps message costs down, since SQS bills per 64 KB chunk.*

**The Extended Client Library** does this automatically for Java and Python, transparently offloading large messages to S3.

Note that **SQS bills per request in 64 KB chunks**, so a 256 KB message counts as four requests. Large messages are more expensive than their count suggests, which is another reason for the S3 pattern.

---

## Key Takeaways

- An event source mapping polls the queue, batches messages, invokes the function, and deletes messages on success.
- The function must not delete messages itself — Lambda handles deletion for the whole batch.
- Without `ReportBatchItemFailures`, one failed message causes the entire batch to be retried.
- Enable partial batch responses on every SQS-triggered function and return failed message identifiers.
- Lambda scales consumers with backlog, so use reserved concurrency to protect downstream systems.
- The visibility timeout must exceed the function timeout, with headroom for the whole batch.
- Messages are limited to 256 KB; store larger payloads in S3 and send a reference.
- SQS bills per 64 KB chunk, so large messages cost more than the message count suggests.
