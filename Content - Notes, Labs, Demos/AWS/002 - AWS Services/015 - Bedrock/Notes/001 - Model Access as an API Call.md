# Model Access as an API Call

**Bedrock** provides foundation models through an API. There is no infrastructure to provision, no model to deploy, and no GPU to manage — a model invocation is an HTTPS request like any other AWS call.

---

## What Bedrock Is

A single API surface over models from several providers, with AWS handling the serving infrastructure. The practical consequences:

**Nothing to run.** No instances, no containers, no model weights to download.

**Billed per token**, not per hour. No cost when no requests are made.

**One IAM-governed API** across providers, so switching models is a parameter change rather than a new integration.

**Data stays within AWS.** Requests do not traverse a third-party provider's infrastructure, which is frequently the deciding factor for regulated workloads.

---

## Model Families

Bedrock hosts models from several providers, and the selection changes as new models are released. The families available include:

| Provider | Models |
|---|---|
| Anthropic | Claude |
| Amazon | Nova, Titan |
| Meta | Llama |
| Mistral AI | Mistral, Mixtral |
| Cohere | Command, Embed |
| Stability AI | Stable Diffusion |

Because the roster changes, the reliable approach is to query it rather than rely on a list:

```bash
aws bedrock list-foundation-models --region us-east-1 \
  --query "modelSummaries[].[modelId,providerName]" --output table
```

*Lists models available in a region. Availability differs by region, so this is worth running for the region actually being used.*

---

## Choosing a Model

Selection depends on four things, and they trade against each other:

**Capability.** Larger models handle complex reasoning, long documents, and nuanced instructions better. Smaller models are adequate for classification, extraction, and short summarization.

**Cost.** Priced per input and output token, and rates differ by an order of magnitude across model sizes. A high-volume classification task on a large model costs far more than the same task on a small one, often with no improvement in quality.

**Latency.** Smaller models respond faster. For an interactive application this may matter more than a marginal quality difference.

**Context window.** How much text fits in one request. Long-document work needs a large window; short interactions do not.

The practical approach is to start with a capable model to establish that the task is achievable, then test whether a smaller and cheaper one is sufficient. Starting small and scaling up wastes time debugging prompts against a model that was never going to succeed.

Note also that **model choice is not permanent**. Because the API is uniform, changing models is a parameter change — worth structuring code so the model ID is configuration rather than a constant.

---

## Two APIs

**`InvokeModel`** takes a provider-specific request body. Each provider has its own format, so switching models means changing the payload structure.

**`Converse`** provides a **unified interface across models**, with one request shape regardless of provider:

```python
import boto3

client = boto3.client("bedrock-runtime", region_name="us-east-1")

response = client.converse(
    modelId="anthropic.claude-sonnet-4-20250514-v1:0",
    messages=[{"role": "user", "content": [{"text": "Summarize this report: ..."}]}],
    inferenceConfig={"maxTokens": 1000, "temperature": 0.3},
)
print(response["output"]["message"]["content"][0]["text"])
```

*The `Converse` API normalizes request and response shapes, so changing `modelId` requires no other change.*

**Use `Converse` for new work.** It removes provider-specific payload handling and makes model comparison straightforward — the same code can be run against several models to evaluate them.

---

## What Bedrock Is Not

**Not a hosting service for custom models.** Fine-tuning and importing models are supported for some families; training a model from scratch is SageMaker's job.

**Not free.** Every token is billed, and a naive implementation sending large context on every request becomes expensive quickly.

**Not deterministic.** The same prompt can produce different output. Applications must tolerate variation, and `temperature` reduces it without eliminating it.

**Not a replacement for validation.** Model output is text, and text presented as structured data still needs parsing and checking.

---

## Key Takeaways

- Bedrock serves foundation models through an API with no infrastructure to manage, billed per token.
- Requests stay within AWS, which is often the deciding factor for regulated workloads.
- Available models vary by region — query `list-foundation-models` rather than relying on a static list.
- Choose by capability, cost, latency, and context window; a smaller model is often sufficient and much cheaper.
- Start with a capable model to prove the task is achievable, then test smaller ones.
- Keep the model ID in configuration, since the uniform API makes switching cheap.
- Prefer the `Converse` API, which normalizes requests across providers and simplifies model comparison.
- Output is non-deterministic and unvalidated — applications must parse and check it.
