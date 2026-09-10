# Guardrails, IAM & Data Handling

Controls on what a model produces, who may invoke it, and what happens to the data sent to it.

---

## Guardrails

**Bedrock Guardrails** apply policies to prompts and responses independently of the model, so the same protections work across models and are not bypassable by prompt engineering alone.

Four kinds of policy:

**Content filters** block harmful categories — hate, insults, sexual content, violence, misconduct, and prompt attacks — at configurable strengths for input and output separately.

**Denied topics** block subjects defined in natural language. A support assistant can be configured to refuse financial advice, with a description and examples.

**Word filters** block specific terms, including a managed profanity list and custom words such as competitor names.

**Sensitive information filters** detect and either block or **mask** PII — names, emails, phone numbers, credit card numbers, SSNs — plus custom regex patterns. Masking is the more useful mode, replacing values rather than refusing the request.

```python
response = client.converse(
    modelId=model_id,
    messages=[{"role": "user", "content": [{"text": user_input}]}],
    guardrailConfig={
        "guardrailIdentifier": "abcd1234",
        "guardrailVersion": "3",
    },
)
```

*Applied to both input and output. `stopReason` indicates when a guardrail intervened, which the application should handle distinctly from a normal response.*

**Contextual grounding checks** are a further policy that scores whether a response is supported by the provided context, which addresses the RAG failure mode of a model answering beyond its sources.

Guardrails are versioned and can be tested in the console before deployment. They are also independent of the model, so switching models keeps the same protections.

---

## What Guardrails Do Not Do

**They do not guarantee correctness.** A guardrail blocks harmful content; it does not verify that an answer is accurate.

**They are not a substitute for authorization.** A guardrail cannot decide whether this user may see this data. That check belongs in the application, before retrieval.

**They add latency and cost**, being an additional evaluation on both input and output.

---

## IAM Permissions

Bedrock permissions are ordinary IAM, and the useful practice is scoping them to specific models:

```json
{
  "Effect": "Allow",
  "Action": ["bedrock:InvokeModel", "bedrock:InvokeModelWithResponseStream"],
  "Resource": [
    "arn:aws:bedrock:us-east-1::foundation-model/anthropic.claude-sonnet-4-20250514-v1:0"
  ]
}
```

*Restricting to named models prevents an application from invoking a far more expensive one, which is a cost control as much as a security one.*

Other actions worth distinguishing:

| Action | Purpose |
|---|---|
| `bedrock:InvokeModel` | Buffered invocation |
| `bedrock:InvokeModelWithResponseStream` | Streaming invocation |
| `bedrock:Converse` / `ConverseStream` | The unified API |
| `bedrock:ListFoundationModels` | Discovery |
| `bedrock-agent-runtime:Retrieve` | Knowledge Base retrieval |
| `bedrock-agent-runtime:RetrieveAndGenerate` | Retrieval plus generation |

Note that Knowledge Base and Agent operations use a **different service prefix** (`bedrock-agent-runtime`), which is a common reason a policy granting `bedrock:*` still produces an access denial.

**Guardrail enforcement** can be required by policy, so an application cannot invoke a model without one:

```json
{
  "Effect": "Deny",
  "Action": "bedrock:InvokeModel",
  "Resource": "*",
  "Condition": { "Null": { "bedrock:GuardrailIdentifier": "true" } }
}
```

*Denies invocation unless a guardrail is specified — an enforceable control rather than a convention.*

---

## Where Data Goes

The question that most often determines whether Bedrock is usable in a regulated context.

**Prompts and responses are not used to train models.** AWS states that customer content submitted to Bedrock is not used to train the underlying foundation models, and is not shared with model providers.

**Data stays in the region** where the request is made, unless cross-region inference profiles are enabled — in which case requests may be served from other regions in the profile, which needs checking against residency requirements.

**Data is encrypted** in transit and at rest.

**Bedrock does not retain prompts or responses** for service operation beyond what is needed to serve the request.

**Model invocation logging is opt-in.** Enabling it writes full prompts and responses to S3 or CloudWatch Logs — which is valuable for debugging and evaluation, and means prompt content lands in a log store. If prompts contain sensitive data, that log store needs the same protection as the source data.

**CloudTrail records invocations** as API calls — who called, when, which model — without the prompt content.

---

## Practical Guidance

**Do not send data the model does not need.** Redact identifiers before including a document in a prompt where the task does not require them.

**Treat output as untrusted input.** Model output rendered into a page can carry injection payloads; output used to build a query or command must be validated exactly like user input.

**Guard against prompt injection.** Content retrieved from documents or supplied by users can contain instructions aimed at the model. Guardrails help, and separating instructions from data in the prompt structure helps more. Never let model output directly authorize an action.

**Log token usage and guardrail interventions** so cost and policy effectiveness are both visible.

---

## Key Takeaways

- Guardrails apply content filters, denied topics, word filters, and PII detection to both prompts and responses, independently of the model.
- PII filters can mask rather than block, which is usually more useful.
- Contextual grounding checks score whether a response is supported by the provided context.
- Guardrails do not verify correctness and are not authorization.
- Scope IAM permissions to specific model ARNs, which controls cost as well as access.
- Knowledge Base operations use the `bedrock-agent-runtime` prefix, so `bedrock:*` alone is insufficient.
- A `Null` condition on `bedrock:GuardrailIdentifier` can require guardrails on every invocation.
- Prompts are not used to train models and stay in-region unless cross-region inference is enabled.
- Model invocation logging is opt-in and writes full prompt content, so the log store needs matching protection.
- Treat model output as untrusted input, and never let it directly authorize an action.
