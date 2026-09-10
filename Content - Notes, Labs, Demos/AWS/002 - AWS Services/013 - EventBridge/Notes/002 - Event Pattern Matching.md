# Event Pattern Matching

An **event pattern** decides which events a rule matches. Filtering happens before delivery, so targets are invoked only for events they care about.

---

## The Basic Shape

A pattern mirrors the structure of the events it matches, with values replaced by arrays of alternatives:

```json
{
  "source": ["com.acme.orders"],
  "detail-type": ["Order Placed"],
  "detail": {
    "total": [{ "numeric": [">", 100] }],
    "region": ["us-east-1", "us-west-2"]
  }
}
```

*Matching rules: every field in the pattern must match, values within an array are alternatives, and fields absent from the pattern are ignored.*

So the pattern is **AND across fields, OR within a field** — the same logic as SNS filter policies, applied to the full event structure rather than to attributes.

**Nested matching is what distinguishes it.** The pattern descends into `detail` and matches on fields inside it, which SNS attribute filtering cannot do.

---

## Matching Operators

| Operator | Example |
|---|---|
| Exact | `{"detail-type": ["Order Placed"]}` |
| Prefix | `{"source": [{"prefix": "com.acme."}]}` |
| Suffix | `{"detail": {"file": [{"suffix": ".csv"}]}}` |
| Anything but | `{"source": [{"anything-but": ["com.acme.test"]}]}` |
| Numeric | `{"detail": {"total": [{"numeric": [">=", 100, "<", 1000]}]}}` |
| Exists | `{"detail": {"customerId": [{"exists": true}]}}` |
| CIDR | `{"detail": {"sourceIp": [{"cidr": "10.0.0.0/8"}]}}` |
| Wildcard | `{"detail": {"key": [{"wildcard": "orders/*/summary.json"}]}}` |

Two composites are worth knowing:

**`anything-but` with a prefix** excludes a family of values:

```json
{"source": [{"anything-but": {"prefix": "com.acme.test."}}]}
```

*Matches everything except test sources — useful for a rule that should ignore synthetic events.*

**`$or` at the top level** expresses alternatives across different fields, which the default AND cannot:

```json
{
  "$or": [
    { "detail": { "status": ["failed"] } },
    { "detail": { "retryCount": [{ "numeric": [">", 3] }] } }
  ]
}
```

*Matches failed events or heavily retried ones. Without `$or`, this requires two rules.*

---

## Matching AWS Service Events

The default bus carries events from AWS services, and rules against them need no publishing setup:

```json
{
  "source": ["aws.ec2"],
  "detail-type": ["EC2 Instance State-change Notification"],
  "detail": { "state": ["terminated", "stopped"] }
}
```

*Fires when an instance stops or terminates. AWS publishes these automatically.*

Common sources include `aws.ec2`, `aws.s3`, `aws.ecs`, `aws.codepipeline`, `aws.health`, `aws.signin`, and many others. Each service documents its `detail-type` values and detail structure.

This is a genuinely useful capability: reacting to infrastructure events — an instance terminating, a pipeline failing, a security finding appearing — requires only a rule.

---

## Why a Pattern Does Not Match

Patterns fail silently. A rule that matches nothing produces no error, no invocation, and no obvious signal. The usual causes:

**`Detail` published as an object rather than a string.** `put_events` requires `Detail` to be a JSON string. Passing a dict produces a malformed event.

**Values are case-sensitive.** `"Order Placed"` does not match `"order placed"`.

**A field is missing from the event.** A pattern requiring a field that some events lack matches only those that have it.

**Numeric values published as strings.** `{"numeric": [">", 100]}` does not match `"total": "150"`.

**Matching on the wrong nesting level** — a field in `detail` matched at the top level, or the reverse.

The `TestEventPattern` API checks a pattern against an event without deploying anything:

```bash
aws events test-event-pattern \
  --event-pattern file://pattern.json \
  --event file://sample-event.json
```

*Returns whether the pattern matches. Faster than deploying a rule and watching for an invocation that may never come.*

**Alarm on a rule's `Invocations` metric being zero** where events are expected. A silently non-matching rule is otherwise indistinguishable from a quiet system.

---

## Content Filtering vs Downstream Filtering

Filtering in the rule rather than in the target is worth doing deliberately:

**Cost.** An unmatched event does not invoke a Lambda function. Filtering in the function pays for every invocation.

**Clarity.** The rule documents what the target cares about, visible without reading code.

**Blast radius.** A target that only ever receives relevant events cannot mishandle an unexpected one.

The counter-argument is that filters in configuration are less testable than code. `TestEventPattern` and pattern definitions in infrastructure code address most of that.

---

## Key Takeaways

- Event patterns mirror event structure, with AND across fields and OR within a field's array.
- Patterns match nested fields inside `detail`, which SNS attribute filtering cannot do.
- Operators include prefix, suffix, anything-but, numeric ranges, existence, CIDR, and wildcard.
- `$or` at the top level expresses alternatives across different fields.
- AWS services publish to the default bus automatically, so reacting to infrastructure events needs only a rule.
- Patterns fail silently — the usual causes are `Detail` sent as an object, case mismatches, and numbers published as strings.
- Use `TestEventPattern` to verify a pattern before deploying, and alarm on rules receiving zero invocations.
- Filtering in the rule avoids invocation cost and documents what each target consumes.
