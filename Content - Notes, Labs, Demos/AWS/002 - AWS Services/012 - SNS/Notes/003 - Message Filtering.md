# Message Filtering

A **filter policy** on a subscription determines which messages that subscriber receives. Filtering happens in SNS, before delivery — so unwanted messages never reach the consumer at all.

---

## Why Filter at the Topic

Without filtering, every subscriber receives every message and discards what it does not need. That means paying for delivery, paying for the invocation, and running code whose only outcome is to ignore the message.

Filtering moves that decision into SNS. The subscriber only receives what matches.

The alternative — a topic per event type — is also viable and produces a proliferation of topics, each needing subscriptions and permissions. One topic with filtered subscriptions is usually easier to manage, and it means a new consumer interested in a subset needs no new topic.

---

## Filter Policies

A filter policy is JSON matched against the message's **attributes** (or, with the right scope setting, its body):

```json
{
  "eventType": ["order.placed", "order.cancelled"],
  "region": ["us-east-1"],
  "total": [{ "numeric": [">", 100] }]
}
```

*All three conditions must match. Within one attribute, the listed values are alternatives — so this matches placed or cancelled orders, in us-east-1, over 100.*

The logic is **AND across attributes, OR within an attribute**. Expressing "A or B" across different attributes requires two subscriptions.

---

## Matching Operators

| Operator | Example |
|---|---|
| Exact | `{"eventType": ["order.placed"]}` |
| Anything but | `{"eventType": [{"anything-but": ["test.event"]}]}` |
| Prefix | `{"eventType": [{"prefix": "order."}]}` |
| Suffix | `{"filename": [{"suffix": ".csv"}]}` |
| Numeric | `{"total": [{"numeric": [">=", 100, "<", 1000]}]}` |
| Exists | `{"customerId": [{"exists": true}]}` |
| IP address | `{"sourceIp": [{"cidr": "10.0.0.0/8"}]}` |

**Prefix matching supports hierarchical event naming.** With types like `order.placed`, `order.shipped`, and `order.cancelled`, one subscription can match `{"prefix": "order."}` and another a specific type — which makes the naming convention worth establishing early.

---

## Attribute vs Body Filtering

By default, filters match **message attributes**. Setting `FilterPolicyScope` to `MessageBody` matches the message body instead:

```bash
aws sns set-subscription-attributes \
  --subscription-arn arn:aws:sns:...:subscription/... \
  --attribute-name FilterPolicyScope --attribute-value MessageBody
```

```json
{ "order": { "status": ["shipped"], "total": [{ "numeric": [">", 100] }] } }
```

*Body filtering matches nested JSON, so no duplication into attributes is needed.*

Which to use:

**Attributes** keep filtering metadata separate from the payload, so the message body can change without breaking filters. This is the more robust choice.

**Body** avoids duplicating fields into attributes, and couples filters to the payload structure — a body change can silently stop matching.

Attribute-based filtering is generally preferable, with the publisher deliberately setting attributes as a filtering contract.

---

## An Example

```
Topic: orders

Subscription A (fulfillment queue)
  {"eventType": ["order.placed"]}

Subscription B (analytics queue)
  {}                                          ← everything

Subscription C (high-value alerts)
  {"eventType": ["order.placed"], "total": [{"numeric": [">", 10000]}]}

Subscription D (EU compliance queue)
  {"region": [{"prefix": "eu-"}]}
```

*One topic serving four consumers with different interests. An empty policy receives everything; each other subscription receives only what it needs.*

---

## Practical Notes

**Attributes must be present and correctly typed.** A numeric filter against an attribute published as `String` does not match. `DataType` must be `Number` for numeric operators — a common and silent failure.

**A missing attribute does not match** unless the policy uses `{"exists": false}`.

**Test filters before relying on them.** A too-narrow policy silently delivers nothing, and there is no error to observe — the subscription simply receives no messages.

**`NumberOfNotificationsFilteredOut`** is a CloudWatch metric showing how many messages a filter excluded. It is the way to confirm a filter is behaving as intended.

**Filtering reduces cost**, since SNS charges per delivery. A topic publishing a million messages to five subscribers costs five million deliveries; filtering to the relevant subset reduces that proportionally.

---

## Key Takeaways

- Filter policies decide which messages a subscription receives, applied in SNS before delivery.
- Logic is AND across attributes and OR within an attribute; cross-attribute OR needs separate subscriptions.
- Operators include exact match, anything-but, prefix, suffix, numeric ranges, existence, and CIDR.
- Prefix matching supports hierarchical event type names, which is worth establishing as a convention.
- Attribute filtering is more robust than body filtering, since the payload can change independently.
- Numeric operators require the attribute's `DataType` to be `Number`, or matching fails silently.
- A too-narrow filter delivers nothing with no error — check `NumberOfNotificationsFilteredOut`.
- Filtering reduces delivery charges as well as consumer work.
