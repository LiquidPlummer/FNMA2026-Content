# Spec-Driven Development

As AI handles bigger units of work, the binding constraint stops being the AI's coding ability and becomes *our ability to say precisely what we want*. **Spec-driven development** meets that constraint head-on: write a detailed specification first — behavior, constraints, interfaces, acceptance criteria — and have the AI implement *against the document*, not against a conversational vibe. The spec becomes the contract, the review standard, and the durable record. It's the plan-first habit and tests-as-spec idea from earlier units, grown into a methodology.

---

## What a Working Spec Contains

Not a requirements novel — a tight document with the sections that change implementation outcomes:

```markdown
# Spec: Invoice Late-Fee Assessment

## Goal
Automatically assess late fees on overdue invoices, idempotently,
via the nightly job.

## Behavior
- An invoice is overdue when status=ISSUED and dueDate < today (org timezone).
- Fee: 1.5% of outstanding balance, min $5, max $500, once per 30-day
  overdue period. Fees compound on balance, never on prior fees.
- Assessment appends a FEE line item and an audit event; it never
  mutates existing line items.

## Non-goals
- No notification emails (separate ticket). No retroactive assessment
  for periods before rollout.

## Interfaces
- New: LateFeePolicy.assess(Invoice, LocalDate) -> Optional<FeeAssessment>
- Job wiring in NightlyJobs; repository additions per access patterns below.

## Constraints
- Java 21 / existing Spring conventions; BigDecimal money; no new deps.
- Idempotent per (invoice, period): rerunning the job must not double-fee.

## Acceptance criteria
- [ ] overdue ISSUED invoice gets one fee per 30-day period, correct amount
- [ ] min/max clamping at $5/$500 verified at boundaries
- [ ] PAID/CANCELLED invoices never assessed
- [ ] job rerun same day: zero new fees (idempotency test)
- [ ] audit event emitted per fee with invoice id, period, amount
```

*A complete working spec: behavior with edge rules, explicit non-goals, named interfaces, hard constraints, and checkbox criteria that convert straight into tests.*

Two sections do outsized work. **Non-goals** fence off the AI's instinct to build adjacent plausible features (and fence off our own scope creep). **Acceptance criteria** are the spec's executable heart — each checkbox maps to a test (the AI TDD workflow slots in directly: generate the tests from the criteria, review, then implement), and "done" stops being a feeling.

---

## The Workflow Around the Document

1. **Draft the spec — with AI as sparring partner, before any code.** Paste the goal and ask: *"Interview me: what's ambiguous or unspecified in this feature?"* The model is excellent at finding underspecification — timezone questions, the compounding rule, the rerun case all tend to surface here, at prose prices instead of rework prices.
2. **Freeze, then implement against it.** The spec goes in the repo (or ticket), the implementation prompt becomes short — *"Implement per #file:late-fee-spec.md, criteria as tests first"* — and the conversation stops carrying the requirements (which also survives context resets, per the next unit).
3. **Review against the spec, not against plausibility.** The reviewer's question upgrades from "does this look right?" to "does this satisfy criterion 4?" — checkable by anyone, including a second AI pass: *"audit this diff against the spec; report any criterion not demonstrably met."*
4. **Amend the document, not the drift.** Discoveries mid-implementation ("dueDate is nullable for drafts!") go back into the spec as revisions — the spec stays true, and the ticket trail shows *why* it changed. A spec that silently diverges from the code is worse than no spec.

---

## Calibrating the Ceremony

Spec-driven is heavyweight by design; deploy it where weight pays: multi-day features, anything touching money/compliance/data migration, work that will be *reviewed* by others or *implemented* largely by agents, and team situations where the spec doubles as the alignment artifact. For a rename or a contained bugfix, a spec is theater — the earlier lightweight tools (a good prompt, a plan, a failing test) already cover it. The instinct being trained: **match the rigor of the ask to the cost of getting it wrong** — and when in doubt, writing the acceptance criteria alone is the 20% of the spec that delivers 80% of the value.

The spec is the fullest form of a skill that also has a per-task, lightweight form — structuring any piece of work so an AI (or a colleague) can execute it cleanly. That's task framing, next.
