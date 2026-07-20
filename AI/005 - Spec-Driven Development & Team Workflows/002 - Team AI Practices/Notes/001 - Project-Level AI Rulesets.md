# Project-Level AI Rulesets

Personal rulesets (Unit 1) tune the AI to one developer; the moment a repo has five contributors with five private rulesets, the AI writes five dialects into one codebase. A **project-level ruleset** — a shared instructions file committed to the repository — makes the team's conventions the *default AI behavior for everyone who clones the repo*, including the teammate who never configured anything and the autonomous agent that starts cold. It is, in effect, the team's coding standards translated from wiki-prose (which AI never reads) into standing prompt context (which it always does).

---

## The Shared File

Same mechanism as the personal version — `.github/copilot-instructions.md`, committed — with content that now carries *team agreements* rather than personal taste (equivalent files exist for other tools — `CLAUDE.md`, `.cursorrules`; multi-tool teams often maintain one canonical document and thin per-tool pointers to it):

```markdown
# Project AI Instructions — invoicing-service

## Architecture (see docs/architecture.md for rationale)
- Strict layering: controllers → services → repositories. Controllers
  never touch repositories or entities; DTOs only at the web layer.
- All money as BigDecimal; all timestamps as Instant, UTC.

## Conventions
- Records for DTOs; constructor injection; Optional for lookups.
- Errors: domain exceptions + ApiExceptionHandler — never inline
  ResponseEntity error building in controllers.
- Golden exemplars: PaymentService/PaymentController/PaymentServiceTest —
  match their patterns for new components.

## Boundaries for AI-generated changes
- Never modify: db/migration/** (applied migrations), any file under legacy/**
- Ask before: adding dependencies, changing public API signatures,
  touching SecurityConfig.
- Generated code must come with tests; tests must not be weakened to pass.

## Project vocabulary
- "Assessment" = fee calculation event; "Period" = 30-day overdue window
  (see docs/glossary.md) — use these names in code and comments.
```

*A team ruleset: architecture law, conventions with golden exemplars, explicit no-go zones for AI changes, and the domain vocabulary that keeps generated names consistent.*

Note what's layered here beyond the personal version: **pointers to durable artifacts** (architecture doc, glossary, golden files — the ruleset stays lean by referencing, not duplicating), and a **boundaries section** — the team's first cut at agent guardrails, which the Autonomy unit will deepen. Personal rulesets still exist and compose on top ("I prefer terse chat answers") — but where they conflict, the project file wins, exactly like personal taste vs. team standards in code review.

---

## Governance: It's Code Now

The file changes AI behavior for the whole team, which makes it *load-bearing infrastructure* — treated accordingly:

- **Changes go through PR review**, like any shared config. A rule quietly added ("prefer static factory methods") reshapes every subsequent generation in the repo; the team should have seen it land. The PR discussion is also where convention disagreements get settled *once*, instead of re-litigated per code review.
- **Someone owns coherence.** Rulesets accrete; a periodic pruning pass (rules now default behavior, rules contradicting newer decisions, rules nobody can explain) keeps the context budget spent on live guidance. Stale rules are worse than missing ones — they steer confidently toward last year's architecture.
- **Verify rules actually steer.** The prompt-evaluation discipline applies: after adding a rule, run the prompt that used to misbehave. Teams routinely discover half their ruleset is aspiration ("write clean, maintainable code") that steers nothing — every rule should be concrete enough that its violation is *recognizable in a diff*.
- **New-joiner test.** The best audit: does a fresh clone plus this file produce AI output that passes our code review? Gaps found by newcomers' AI sessions are gaps in the ruleset — close them there, not in per-person folklore.

The ruleset governs *how* AI writes code for the project. The companion question — *when a human must look before AI proceeds* — is the review-checkpoint design of the next lesson.
