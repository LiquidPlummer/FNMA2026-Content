# Structured Inputs & Outputs

When AI output feeds a *person*, prose is fine. When it feeds a *program* — a script parsing results, a CI step consuming a report, a pipeline chaining one AI call into the next — structure stops being a preference and becomes a contract. **Structured IO** is the discipline of templated inputs and schema-bound outputs that make AI calls behave like functions: typed in, typed out, validated in between. It's the constraints lesson from Unit 2, hardened into infrastructure.

---

## Structured Outputs: Schema as Contract

The pattern that turns "please output JSON" into something a pipeline can trust:

```text
Analyze the attached test failures. Respond with JSON matching EXACTLY:

{
  "failures": [
    {
      "test": "string — fully qualified test name",
      "category": "assertion | timeout | infrastructure | flaky-suspect",
      "likely_cause": "string, one sentence",
      "confidence": "high | medium | low",
      "suggested_owner_file": "string path or null"
    }
  ],
  "summary": "string, max 2 sentences"
}

Rules: valid JSON only — no markdown fences, no commentary.
Unknown values: use null or "low" confidence — never invent.
Every enum value must be from the lists shown.
```

*A schema-bound prompt: exact shape shown (the one-shot example trick), enums enumerated, unknown-handling pre-decided, prose banned.*

The receiving side completes the contract — **parse and validate before use**, exactly as with any untrusted input: JSON parses, required keys present, enums in range. On failure, the standard recovery is one **repair round-trip** — feed the output and the validator error back ("this failed validation: [error]; emit corrected JSON only") — then fail the pipeline loudly if it's still wrong. One repair attempt catches most glitches; infinite retry loops hide real problems. Two design rules learned the hard way: keep schemas *flat and small* (deep nesting multiplies malformation; if the schema needs a page, split the task), and make enums *closed* — freetext categories drift run-to-run, which poisons anything aggregating them.

Where this applies is broader than pipelines: commit messages, changelog entries, PR descriptions, triage labels, extraction tasks (endpoints from code, TODOs from a module), evaluation rubric scores (the LLM-as-judge from Prompt Evaluation emits structured verdicts for exactly this reason) — any output something else consumes.

---

## Structured Inputs: Templates as Reusable Prompts

The mirror image: recurring tasks get a **prompt template** — fixed instructions with typed slots — so quality is engineered once and inputs just fill holes:

```text
=== TICKET TRIAGE v3 (do not edit instructions; fill slots) ===
Ticket title:   {{title}}
Ticket body:    {{body}}
Component list: {{components_csv}}

Classify per the JSON schema below. A ticket matching no component
gets "unassigned" — never guess a component not in the list.
[schema...]
```

*A slotted template: instructions and schema are versioned assets; per-use content flows through delimited slots.*

Templates carry three quiet wins: **consistency** (every triage runs the same evaluated, versioned prompt — the Prompt Evaluation lifecycle applies: rubric, test inputs, version number), **separation of data from instructions** (delimiters make clear what's *content to analyze* vs. *commands to follow* — which matters for the injection risks in the next unit: ticket text saying "ignore previous instructions" should sit inertly inside a slot), and **composability** — one call's validated JSON output becomes the next template's slot input, which is precisely how multi-step AI workflows chain reliably (the Workflow Designs lesson builds on this exact mechanism).

---

## The Function-Call Mental Model

The unit's two lessons converge on one upgrade: treating an AI call less like a chat and more like a **typed function** — curated context in (managed window, templated input), schema-validated artifact out, with defined failure behavior. That's the shape that lets AI participate in real systems: testable (golden inputs → expected shapes), monitorable (validation failure rates), and chainable. It's also the shape everything in the remaining units assumes — agents composing steps, hooks validating calls, governance auditing them. Prose chat is the interactive surface; structured IO is the engineering surface.
