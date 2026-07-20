# AI Personas

A **persona prompt** assigns the AI a role before the task: *"Act as a security reviewer,"* *"You are a performance engineer skeptical of premature abstraction."* It reads like theater, but the mechanism is practical: a role statement shifts the model's *frame* — which concerns it prioritizes, which vocabulary it uses, which problems it goes looking for. The same code shown to "a helpful assistant" and "a pessimistic SRE" yields different reviews, and we can choose which one we need.

---

## Why a Role Changes the Output

LLMs generate text conditioned on everything in context. "Act as a security reviewer" makes security-review-shaped text more likely: the model surfaces injection risks and authorization gaps it *wouldn't mention* — not couldn't, wouldn't — under a neutral frame, because a generic assistant optimizes for balanced helpfulness, not adversarial depth. A persona is a lens selector for knowledge the model already has.

```text
Act as a senior security reviewer doing a pre-release audit.
You are thorough and unimpressed by "it's internal-only" excuses.

Review this controller (#file:UploadController.java) for:
- injection and path-traversal risks
- authorization gaps
- unsafe handling of user-supplied filenames and content types

Report findings as: severity, line, issue, concrete fix. No praise section.
```

*A working persona prompt: the role, an attitude that counters the model's agreeable default, a scoped checklist, and an output shape.*

Note the structure: the persona isn't a costume line bolted on the front — it works with the scope and output constraints. "Act as X" alone adds flavor; "act as X, focus on Y, report as Z" changes the work.

---

## The Personas That Earn Their Keep

In practice, a handful of roles cover most real use:

- **The adversarial reviewer** — security auditor, hostile QA engineer, "the person trying to break this." The single most valuable persona family, because it directly counteracts the AI's default agreeableness. *"You are a QA engineer paid per bug found — what breaks this method?"*
- **The specialist consultant** — DBA for a schema review, SRE for operational readiness, accessibility specialist for UI work. Pulls domain-specific checklists the generic frame skips.
- **The explainer at a level** — "explain as if to a strong Java dev who's never touched Kafka" (audience personas tune explanations better than "explain simply").
- **The skeptical architect** — reviews the *design*, not the code: "You've seen a hundred microservice migrations fail. Critique this plan and tell me what we're underestimating."
- **The devil's advocate on demand** — *"Argue against the approach you just recommended."* The cheapest second opinion in software; the AI switches frames and often produces the objection we needed to hear.

The common thread: personas are most valuable when we need the AI to *oppose* us — to find fault, argue back, or apply a stricter standard than the default eager-to-please assistant. For generation tasks ("act as an expert Java developer and write..."), the gain is modest — modern models already write competent code; a persona won't upgrade capability, only emphasis.

---

## Limits and Honest Expectations

A persona changes *emphasis*, not *knowledge or authority*. "Act as a lawyer" produces legal-flavored text, not legal advice; "act as a principal engineer" doesn't grant experience the model lacks — it grants confidence, which is worse if we mistake it for expertise. The blind-acceptance disciplines apply at full strength: a persona-framed review is still generated text, still needs verification, and an adversarial persona that finds *nothing* doesn't certify the code as clean — it means one lens found nothing.

Practical hygiene: keep the persona and the task consistent (a security reviewer asked to "also make it faster" muddies both jobs — run two passes with two personas instead), and drop the persona when it's done its work — a lingering "hostile reviewer" frame degrades a session that's moved on to collaborative implementation.

Personas steer *what the model attends to*. The next lesson steers *how it reasons* — prompting for explicit step-by-step thinking: chain of thought.
