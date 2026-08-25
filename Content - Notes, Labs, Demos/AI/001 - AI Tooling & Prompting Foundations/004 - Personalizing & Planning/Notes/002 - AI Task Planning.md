# AI Task Planning

Handed a multi-step task, an AI's instinct is to start typing code — and code is the most expensive place to discover a misunderstanding. **Plan-first** flips the sequence: the AI produces a step-by-step plan, we review and correct it in prose (cheap), and only then does implementation begin (expensive). For anything beyond a single-method change, this one habit prevents more waste than any other technique in this unit.

---

## Asking for the Plan

```text
I need to add soft-delete to the Invoice entity: a deletedAt timestamp,
exclusion from all standard queries, an undelete endpoint, and no data loss.

Don't write any code yet. First give me a numbered plan:
which files change, in what order, and what each change is.
Flag anything you're unsure about or any decision I need to make.
```

*The plan-first prompt: the task, then an explicit "no code yet," then the shape the plan should take — including surfacing uncertainties.*

The "don't write code yet" line matters — without it, most tools leap to implementation. The result is a reviewable artifact:

```text
Plan:
1. Add deletedAt (Instant, nullable) to Invoice + migration script
2. Replace findAll/findById usages with query methods filtering deletedAt IS NULL
   ⚠ Decision needed: should admin views see deleted invoices?
3. Change DELETE endpoint to set deletedAt instead of repo.delete
4. Add POST /invoices/{id}/restore endpoint
5. Update InvoiceRepositoryTest + controller tests
⚠ Unsure: whether reporting queries in ReportService should exclude deleted rows
```

*A reviewable plan: ordered steps, named files, and — most valuably — surfaced decisions the AI would otherwise have guessed.*

---

## Reviewing the Plan Is the Actual Work

The plan is where our engineering judgment applies at maximum leverage. The checklist:

- **Completeness** — did it miss a consumer? (Step 2's "all standard queries" — did it find the ones in the reporting module?) The AI plans from what it can see; we know what it can't.
- **The flagged decisions** — answer them *now*, in prose ("admin sees deleted; reports exclude them"). Every guess we preempt is a rework cycle saved.
- **Order and size** — steps should be independently verifiable; "step 2.5: run the tests" is a legitimate edit. A step we couldn't review on its own is two steps.
- **The step that shouldn't exist** — AI plans sometimes include unnecessary work (a new abstraction, a config change) that mirrors *typical* solutions rather than ours. Strike it; the plan is ours to edit.

Then execute **step by step, not plan-at-once**: "do step 1" → review the diff → "step 2, but use a partial index" → ... Feeding the plan back one step at a time keeps each diff small enough to actually review, and lets reality (a failing test, a surprise dependency) update the plan between steps. Agent modes that execute whole plans autonomously exist — and are exactly the scenario the Agent Autonomy unit puts guardrails around.

---

## When to Plan, When to Just Ask

Plan-first has overhead; calibrate. **Skip it** for single-file, single-concern changes — a rename, a new test, a contained bugfix. **Use it** when the task touches three or more files, crosses layers, involves a migration or contract change, or when *we're* unsure how to decompose it — the plan doubles as our own thinking aid. **Insist on it** when the task is one we'd struggle to review as a single giant diff; if we can't review the whole, we need the steps.

The plan is also a natural checkpoint to save: pasted into the ticket or PR description, it becomes the work log and the reviewer's roadmap — a habit the Spec-Driven Development unit turns into a full methodology. Before that, this unit closes with the critical-eye discipline that all of these techniques assume: working critically with AI, next topic.
