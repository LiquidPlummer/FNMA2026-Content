# Constraints and Output Compliance

Left unconstrained, an AI answers the way a helpful colleague talks — some preamble, the substance, alternative approaches, a safety caveat, an offer to elaborate. Fine for conversation; useless when the output feeds a script, a file, or a workflow with a required shape. **Output constraints** pin down form and scope: what format, what's included, what's off-limits. **Compliance** is the craft of actually getting the model to obey — which takes more than asking once.

---

## Format Constraints

State the shape, show the shape, forbid the extras:

```text
Extract every REST endpoint from this controller as JSON.

Output a JSON array only — no prose, no markdown fences, no explanation.
Each element exactly:
{
  "method": "GET|POST|PUT|DELETE",
  "path": "/api/...",
  "auth": "required|none|unknown"
}
If a value can't be determined, use "unknown" — never omit the key,
never invent a value.
```

*The format trifecta: declare the format, provide the exact schema as an example, and pre-answer the edge case (unknowns) that would otherwise trigger improvisation.*

The example-schema line does the heaviest lifting — it's a one-shot demonstration (the few-shot lesson, applied to structure), and models follow shown structure far more reliably than described structure. The "no prose, no fences" line targets the classic failure: a perfect JSON array wrapped in "Here's the extraction you requested!" — which breaks any parser downstream. And the *unknowns rule* closes the door on the model's instinct to fill gaps with plausible inventions rather than admit ignorance.

The same pattern serves every structured target: markdown tables ("columns: X, Y, Z, no other sections"), commit messages ("conventional commits format, subject ≤ 72 chars"), CSV, YAML config, javadoc. When outputs feed *machines* rather than readers, this hardens into schemas-and-templates as infrastructure — the Structured AI IO unit later takes it there.

---

## Scope Constraints

The second family bounds *what the AI may touch* — the guardrails that make delegation safe:

- **Change scope:** "Modify only `validateAddress` — do not touch other methods, imports, or formatting elsewhere." The counter to the quiet scope-creep from the blind-acceptance lesson.
- **Dependency scope:** "Standard library only — no new dependencies." An AI's instinct is to reach for a library; in our locked-down environment, half of those aren't even mirrored internally.
- **Design scope:** "No new abstractions — solve it inline; I'll refactor if it recurs." Prevents the speculative interface-and-factory scaffolding AI loves to erect.
- **Style compliance:** "Match this file's existing conventions, even where they differ from your defaults" — plus whatever the ruleset already establishes (rulesets *are* standing constraints; the prompt handles the per-task ones).

*Rule of thumb: every constraint we state is a review comment we won't have to make.*

---

## When the Model Doesn't Comply

Compliance degrades in predictable ways, with predictable fixes:

1. **Buried constraints get lost.** In long prompts, models weight beginnings and ends; a rule in the middle of paragraph three is a rule at half strength. Put constraints in a labeled block ("Rules:") at the start or end — and repeat the critical one.
2. **Politeness leaks back.** The "no prose" rule obeyed for one reply, forgotten three turns later — restate constraints when a long session drifts, or restart with the constraint in the opening prompt.
3. **Conflicts resolve silently.** Ask for "complete javadoc" and "under 40 lines" and the model quietly sacrifices one; if two constraints can tension, rank them ("if length conflicts with completeness, completeness wins").
4. **Verification beats vigilance.** For anything feeding a pipeline, *check* compliance mechanically — parse the JSON, lint the diff for out-of-scope files — rather than eyeballing. A constraint we can't verify is a preference, not a constraint.

That last point generalizes into the topic that closes this unit: if prompts are engineering artifacts with expected outputs, they can be *evaluated* — systematically, against criteria, across variations. Prompt evaluation, next.
