# AI Knowledge Lookup & Explanation

Half of professional development is reading: unfamiliar code, unfamiliar frameworks, unfamiliar error-message dialects. AI turns reading into conversation — code that explains itself on demand, at whatever depth we ask, with follow-up questions welcome. Used well, this is the fastest way to ramp onto a codebase or concept; used lazily, it substitutes fluent-sounding summaries for understanding. This lesson covers both edges.

---

## Explaining Code

Highlight, then ask — but ask *at a level*:

```text
Level 1:  What does this class do, in two sentences? (orientation)

Level 2:  Walk through this method — what happens on each branch,
          and what invariants does it maintain? (working understanding)

Level 3:  Why might the author have chosen a ConcurrentHashMap here
          instead of synchronizing? What breaks if we swap it? (design intent)
```

*Three depths of the same question — matching the ask to the need instead of defaulting to "explain this."*

The generic "explain this code" produces a line-by-line paraphrase — rarely what we need. Better verbs: *summarize* (orientation), *trace* ("follow an order through this method — what state changes?"), *justify* (design reasoning), *compare* ("how does this differ from the version in PaymentService?"), and the ramp-up special: *"draw me the call flow from the controller to the database for a POST to /invoices"* — AI is remarkably good at narrating architecture from code it can see.

The comprehension multiplier is asking **follow-ups against our own mental model**: "so if two requests hit this concurrently, the second one waits?" A yes/no with explanation either confirms the model in our head or repairs it — this is the active-reading loop that makes the knowledge stick.

---

## Explaining Concepts and APIs

Same conversational advantage for the things around the code — "what is optimistic locking and when does JPA use it?", "difference between `CompletableFuture.thenApply` and `thenCompose`?", "what does this Gradle deprecation warning actually want from me?" The AI answers at our level, with examples in our language, tuned by our follow-ups. Tell it what we already know ("I know Java streams well — explain Kafka Streams by analogy") and the explanation meets us there.

The caveat that keeps this honest: **the model's knowledge has a training cutoff and a confidence problem.** For stable, foundational material (language semantics, patterns, established frameworks) it's reliable and current enough. For *version-specific* facts — "what's new in Spring Boot 3.4", exact method signatures, configuration keys — it may be outdated or, worse, plausibly wrong (the hallucination lesson formalizes this). The working rule: **concepts from the AI, specifics from the docs** — and when a specific matters, ask for it in verifiable form: "show the official documentation section name I should look up" beats trusting a recited signature.

---

## Comprehension Debt: The Failure Mode

The risk isn't wrong answers — it's *unearned* right ones. Reading a fluent summary feels like understanding; the difference surfaces later, when we modify code we never actually understood. Symptoms: we can't predict what a change would break, can't answer a teammate's "why" question, re-ask the AI the same thing weekly.

The antidotes are cheap:

- **Verify against the source.** An explanation is a map; spot-check it against the code — especially any sentence starting "this ensures that..." (the AI describes *intent* generously; the code may not deliver it).
- **Close the loop in our own words.** After an explanation, state it back — to the AI ("so, in short: X happens unless Y?"), in a code comment, or in the PR description. If we can't, we don't have it yet.
- **Prefer teaching mode for things we'll own.** "Explain, then quiz me with three questions about the edge cases" — a prompt that turns lookup into learning for the code we'll be maintaining. For code we're merely passing through, the two-sentence summary is fine; calibrate the depth to the ownership.

Understanding what exists is the prerequisite for the next topic's theme — making the AI produce work that fits *our* practices, starting with personal rulesets.
