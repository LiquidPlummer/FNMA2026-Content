# Invoking a Model

The mechanics of a request: what goes in, what parameters control the output, and the difference between waiting for a complete response and streaming one.

---

## The Request

A request carries a **prompt**, **inference parameters**, and optionally a **system prompt** and conversation history:

```python
response = client.converse(
    modelId="anthropic.claude-sonnet-4-20250514-v1:0",
    system=[{"text": "You are a support assistant. Answer only from the provided context."}],
    messages=[
        {"role": "user", "content": [{"text": "What is the refund window?"}]},
        {"role": "assistant", "content": [{"text": "The refund window is 30 days."}]},
        {"role": "user", "content": [{"text": "Does that include shipping?"}]},
    ],
    inferenceConfig={"maxTokens": 500, "temperature": 0.2, "topP": 0.9},
)
```

*The `system` prompt sets behavior; `messages` carries the conversation. Models are stateless — **the full history is sent on every request**, which is why conversation length drives cost.*

That last point deserves emphasis. There is no server-side session. A long conversation means resending the whole history each turn, so input token cost grows with conversation length.

---

## Inference Parameters

**`maxTokens`** caps the response length. A response hitting this limit is **truncated mid-sentence**, not summarized — worth checking `stopReason` in the response to distinguish a complete answer from a cut-off one.

**`temperature`** (0 to 1) controls randomness. Low values produce consistent, focused output; high values produce varied output. For extraction, classification, and structured output, use a low value. For creative generation, higher.

**`topP`** is an alternative sampling control. Adjusting one or the other is usual; adjusting both together makes the effect hard to reason about.

**`stopSequences`** end generation when a string appears, which is useful for structured output formats.

---

## Streaming vs Buffered

**Buffered** (`converse`) waits for the complete response. Simple, and the user sees nothing until generation finishes — which for a long response can be many seconds.

**Streaming** (`converse_stream`) returns tokens as they are generated:

```python
stream = client.converse_stream(
    modelId=model_id,
    messages=[{"role": "user", "content": [{"text": prompt}]}],
)
for event in stream["stream"]:
    if "contentBlockDelta" in event:
        print(event["contentBlockDelta"]["delta"]["text"], end="", flush=True)
```

*Text arrives incrementally, so output appears immediately rather than after the full response completes.*

**Use streaming for anything a person is waiting on.** Total generation time is the same; perceived latency is far lower because output starts appearing in under a second.

**Use buffered for machine consumption** — a Lambda function parsing structured output has no use for partial results, and buffered is simpler.

Note that streaming interacts awkwardly with API Gateway, which buffers responses. Streaming to a browser typically uses a Lambda function URL with response streaming enabled, or WebSockets.

---

## Structured Output

Asking for JSON and parsing the response is a common need, and the naive version is fragile — models often wrap JSON in explanatory text or markdown fences.

Three practices make it reliable:

**Ask explicitly and precisely.** State the exact schema and that the response must contain nothing else.

**Prefill the response** where the model supports it, starting the assistant turn with `{` so generation continues from there.

**Use tool definitions.** Where a model supports tool use, defining a tool with a typed input schema causes the model to return structured arguments rather than free text — the most reliable route to parseable output:

```python
response = client.converse(
    modelId=model_id,
    messages=[{"role": "user", "content": [{"text": document}]}],
    toolConfig={
        "tools": [{
            "toolSpec": {
                "name": "record_extraction",
                "inputSchema": {"json": {
                    "type": "object",
                    "properties": {
                        "invoice_number": {"type": "string"},
                        "total": {"type": "number"},
                    },
                    "required": ["invoice_number", "total"],
                }},
            }
        }],
        "toolChoice": {"tool": {"name": "record_extraction"}},
    },
)
```

*Forcing a specific tool makes the model return arguments matching the schema, which is far more dependable than parsing prose.*

**Validate regardless.** Even with a schema, the response should be checked before use.

---

## Errors and Retries

**`ThrottlingException`** is the most common. Bedrock enforces per-model, per-region quotas, and exceeding them throttles requests. Retry with exponential backoff — AWS SDKs do this for a bounded number of attempts, and high-volume applications need their own handling.

**`ValidationException`** usually means the request exceeds the context window or a parameter is out of range.

**`AccessDeniedException`** commonly means model access has not been requested for that model, rather than an IAM problem.

**Timeouts** matter for long generations. A Lambda function calling Bedrock needs a timeout accommodating the full response, and the SDK's own read timeout may need raising above its default.

---

## Key Takeaways

- Models are stateless — the full conversation history is sent on every request, so cost grows with conversation length.
- `maxTokens` truncates rather than summarizing; check `stopReason` to detect a cut-off response.
- Use low `temperature` for extraction and classification, higher for creative generation, and adjust either temperature or `topP` rather than both.
- Streaming reduces perceived latency substantially and should be used wherever a person is waiting.
- API Gateway buffers responses, so streaming to a browser needs a function URL or WebSockets.
- For structured output, prefer tool definitions with a typed schema over parsing prose, and validate the result regardless.
- Handle `ThrottlingException` with exponential backoff, and note that `AccessDeniedException` often means model access was never requested.
