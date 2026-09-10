# Pricing, Access & Region Availability

Three practical constraints encountered before any Bedrock code runs: getting permission to use a model, finding a region where it exists, and estimating what it will cost.

---

## Model Access Requests

**Models are not available by default.** Access must be requested per model, per region, through the Bedrock console.

For most models this is immediate — a checkbox and an acknowledgement of the provider's terms. Some require a use case description and a short review.

This is the usual cause of `AccessDeniedException` on a first attempt. The IAM policy is correct, and model access was never granted for that model in that region.

**Request access in every region the application will use.** Access is per region, and a working development setup in `us-east-1` says nothing about production in `eu-west-1`.

---

## Region Availability

Model availability varies considerably by region, and it is a real constraint on architecture. A model available in `us-east-1` may not exist in `eu-central-1`, which matters when data residency requires an EU region.

```bash
aws bedrock list-foundation-models --region eu-central-1 \
  --query "modelSummaries[].modelId" --output text
```

*Check before designing around a specific model — availability changes, and assumptions from one region do not transfer.*

**Cross-region inference profiles** help: they route requests across several regions automatically, improving availability and throughput during capacity constraints. The trade is that requests may be served from a region other than the one called, which needs checking against data residency requirements.

---

## Token-Based Pricing

Billing is per token, with **separate rates for input and output**. Output tokens typically cost several times more than input tokens.

A **token** is roughly ¾ of a word in English — 1,000 tokens is about 750 words. Non-English text and code tokenize less efficiently.

Estimating a workload:

```
Per request:
  System prompt          200 tokens
  Retrieved context    3,000 tokens
  User question           50 tokens
  ─────────────────────────────────
  Input                3,250 tokens
  Output                 500 tokens

100,000 requests/month:
  Input:    325M tokens × input rate
  Output:    50M tokens × output rate
```

*Input dominates in retrieval-augmented applications, because retrieved context is resent on every request.*

That pattern is worth noting: **retrieved context is usually the largest cost driver**. Retrieving ten documents instead of three triples input cost with no guarantee of a better answer.

Three levers reduce cost meaningfully:

**Use a smaller model where it suffices.** Rates differ by an order of magnitude across model sizes, and classification or extraction rarely needs the largest model.

**Reduce context.** Retrieve fewer, more relevant documents. Trim system prompts. Truncate conversation history rather than resending it indefinitely.

**Cache.** Repeated identical questions do not need repeated inference. Where supported, prompt caching reduces the cost of a large, stable prefix such as a long system prompt or a fixed document.

Actual token usage is reported per response:

```python
usage = response["usage"]
# {"inputTokens": 3250, "outputTokens": 487, "totalTokens": 3737}
```

*Logging this per request is how cost becomes attributable rather than a monthly surprise.*

---

## On-Demand vs Provisioned Throughput

**On-demand** is pay-per-token with shared capacity, subject to per-region quotas and possible throttling under load. The right default.

**Provisioned throughput** reserves dedicated capacity in model units, billed hourly for a committed term. It provides guaranteed throughput and predictable latency, and it is expensive — appropriate only for sustained high volume or a hard latency requirement.

Nearly every application should start on-demand and consider provisioned throughput only when throttling becomes a genuine constraint.

---

## Quotas

Bedrock enforces per-model, per-region quotas on requests per minute and tokens per minute. Defaults are modest and vary by model.

Two consequences:

**Throttling appears under load, not in development.** A working prototype says nothing about behavior at production volume.

**Quota increases take time.** Requesting them ahead of a launch is better than discovering the limit during one.

Exponential backoff with jitter is required regardless, since throttling is a normal condition rather than an error.

---

## Cost Monitoring

Bedrock appears in Cost Explorer by model, so per-model spend is visible. Two additions make it manageable:

**Log token usage per request** with a tag identifying the feature, so cost is attributable to what caused it.

**Set a budget alert.** Bedrock costs scale with usage, and a bug producing a retry loop can generate substantial spend quickly. A budget alert is the cheapest protection available.

---

## Key Takeaways

- Model access must be requested per model, per region, and its absence causes `AccessDeniedException` despite correct IAM.
- Model availability varies by region, which constrains architectures with data residency requirements.
- Cross-region inference profiles improve availability but may serve requests from another region.
- Billing is per token with output costing several times more than input; a token is roughly ¾ of a word.
- In retrieval-augmented applications, resent context is usually the largest cost driver.
- Reduce cost with a smaller model, less context, and caching of stable prefixes.
- Log the per-response `usage` block so cost is attributable to features.
- Start on-demand; provisioned throughput is expensive and suits only sustained high volume.
- Per-model quotas cause throttling under production load, so request increases ahead of launch and always use backoff.
