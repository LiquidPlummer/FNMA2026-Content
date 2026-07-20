# AI Issue Tracking and Resolution

Tickets are where team software work actually lives — bug reports, feature requests, tech-debt items — and AI slots into that workflow at several points: understanding a ticket, reproducing it, drafting the fix, and writing everything up. The through-line of this lesson: **the ticket is a prompt-context goldmine, and the AI's outputs feed back into the ticket** — a loop that keeps both the work and its record sharp.

---

## From Ticket to Understanding

A well-formed ticket is half a debugging prompt already. Start by handing it over whole:

```text
Here's a bug ticket:

  Title: Invoice totals wrong for orders edited after a partial refund
  Steps: create order → refund one line item → edit quantity of another
         line → total shown includes the refunded item again
  Expected: refunded items stay excluded
  Environment: prod, since the 2.14 release

Given our codebase (#file:InvoiceService.java, #file:RefundService.java):
1. Which code paths are implicated?
2. What's your best hypothesis for the cause, given "since 2.14"?
3. What would reproduce this as a failing test?
```

*The triage prompt: the ticket verbatim, the likely files attached, and three questions that turn a report into an investigation plan.*

The "since the 2.14 release" detail is the kind of thing AI uses well — "what changed in these files recently?" (paste the relevant diff or PR list) often localizes the regression immediately. And question 3 is the strategic one: **a failing test that reproduces the ticket** is the ideal first artifact — it confirms our understanding of the bug, defines "fixed," and becomes the regression guard. (The AI TDD unit builds on exactly this move.)

For triage at volume — a stack of incoming tickets — AI also drafts the unglamorous parts well: suggesting severity and affected components, spotting probable duplicates ("do any of these describe the same failure?"), and flagging tickets missing reproduction steps, with a drafted comment requesting them.

---

## From Fix to Record

After the fix (via the debugging loop from the previous topic), the writeback — where AI removes the friction that makes engineers skimp on documentation:

- **Root-cause comment for the ticket:** "Summarize this bug's cause and fix for the ticket, two paragraphs, non-cryptic — future-us is the audience." Paste the diff; get a draft; correct it.
- **PR description:** cause, fix, test evidence, risk notes — generated from the diff plus the ticket, then reviewed like any AI output.
- **The follow-up tickets:** the fix exposed adjacent debt ("RefundService recalculates totals in two places"). "Draft a tech-debt ticket for consolidating total calculation — context, proposed approach, acceptance criteria." Debt that gets ticketed gets scheduled; debt that stays in our head doesn't.

*The pattern: every artifact the workflow wants — comments, descriptions, follow-ups — is a small structured-writing task, which is exactly what AI drafts fastest and we verify easiest.*

---

## The Boundaries That Keep This Honest

Three, all previews of later units. **Privacy:** production tickets contain user data — customer names, account numbers, stack traces with real values. Sanitize before prompting (the PII Guardrails lesson makes this systematic). **Verification:** an AI-drafted root-cause summary can be *fluently wrong* — committing a wrong explanation to the ticket record is worse than a terse right one, because future engineers will trust it. The draft is a draft. **Attribution:** the ticket trail is an accountability record; "the AI said so" never appears in it. We investigated, we fixed, we verified — the AI accelerated each step, and the name on the work is ours.

That last point — owning what we accept — is precisely the discipline the next lesson builds out: preventing blind acceptance.
