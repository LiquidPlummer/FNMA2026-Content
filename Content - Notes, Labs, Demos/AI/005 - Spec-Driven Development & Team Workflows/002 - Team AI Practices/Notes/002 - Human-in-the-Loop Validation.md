# Human-in-the-Loop Validation

As AI takes on longer chains of work — multi-step plans, retrofit campaigns, agent-executed features — the design question shifts from "is the output good?" to "**where do humans check?**" Human-in-the-loop (HITL) validation is the deliberate placement of checkpoints: moments where work pauses for human review before proceeding. Done well, checkpoints concentrate scarce human attention where it changes outcomes; done badly, they're either absent (rubber-stamp automation) or everywhere (an approval-fatigued human clicking "yes" — which is absence with extra steps).

---

## Where Checkpoints Earn Their Cost

The economics: a checkpoint is valuable where **error is likely, expensive, or hard to reverse** — and cheap to catch early. Mapped onto the workflows we've built:

- **After the plan, before the code.** The highest-ROI checkpoint in existence (the Task Planning lesson): reviewing a ten-line plan catches wrong-direction errors at prose prices. For agent work, this is non-negotiable — plans are approved *before* execution.
- **After the tests, before the implementation.** The AI-TDD hinge: human-reviewed tests become the trusted referee, and everything after can lean on green/red instead of vigilance.
- **After each batch, not each keystroke.** Campaign-scale work (retrofits, translations) checkpoints per batch — small enough to review honestly, large enough not to drown the reviewer. Round one gets deep review; later rounds, spot-checks plus mechanical gates, *because the pattern was validated early*.
- **At the blast-radius boundaries.** Regardless of workflow: anything touching migrations, security config, public API contracts, money math, or production infrastructure stops for a human — the ruleset's "ask before" list, enforced as process. (The Autonomy unit turns this from convention into hard mechanism.)
- **Before anything leaves the building.** AI-drafted ticket comments, PR descriptions, customer-visible text: a human reads it before it ships under a human's name.

And the anti-list: checkpoints *don't* belong at steps where review is a glance-and-click (fold them into the next real checkpoint), or where a mechanical gate does the job better — compilers, tests, linters, schema validators, and conformance checks are tireless reviewers; **spend machines before spending humans**, and let the human checkpoint start from "the mechanical gates already passed."

---

## Designing a Checkpoint That Actually Validates

A checkpoint is a small interface, and it's designed like one:

```text
Checkpoint: retrofit batch approval (round 3 of 6)

Reviewer sees:
- the diff (6 files), pre-gated: build green, tests green, conformance
  check passed
- the exceptions list: 2 files skipped ("no clean seam in LegacyBilling")
- delta from approved pattern: "used warn not info in JobRunner —
  it's a retry path"

Reviewer decides: approve batch / amend pattern and redo / pull a file
out for hand-work. Recorded in the PR.
```

*A well-shaped checkpoint: pre-verified inputs, the deviations surfaced rather than buried, a bounded decision, and a paper trail.*

The design rules embedded there: the reviewer gets **the artifact plus the deviations**, never a summary alone ("the AI says it followed the pattern" is not reviewable); the decision is **bounded** (approve/amend/extract — not "thoughts?"); and the outcome is **recorded** where the team works (PR, ticket), building the audit trail governance will later want. One more, easy to miss: **checkpoints must be allowed to fail.** A checkpoint that has never rejected anything is decoration — track rejections; their absence over months means either miraculous quality or (more likely) fatigue, and fatigue is fixed by *fewer, higher-stakes* checkpoints with stronger mechanical gates beneath them, not by exhortations to pay attention.

---

## The Team Layer

HITL scales past one person: *who* reviews an AI-heavy PR needs the same clarity as any review assignment — AI-generated code gets a named human owner (the submitter — the engineer-of-record principle from Unit 1), and the reviewer knows which parts were generated and what validation already ran (a one-line "AI-assisted; plan and tests human-reviewed; batch diffs spot-checked" in the PR description sets the reviewer's depth honestly). Pairing this with the shared ruleset closes the loop: conventions live in the ruleset, judgment lives at checkpoints, and neither leaks into the other's job.

Everything here assumed conversations and diffs small enough to review. The next unit confronts the resource that quietly limits all of it — the context window — and the structured-IO techniques that make AI outputs machine-checkable at the gates.
