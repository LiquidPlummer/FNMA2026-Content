# Retrofitting Concerns

A **cross-cutting concern** — logging, auth checks, error handling, caching, metrics, input validation — touches many files in the same way. Retrofitting one across an existing codebase is the work developers dread most: forty files, the same surgical change in each, attention decaying with every repetition. It's also the work AI agent modes handle best-in-class — *if* run as a disciplined campaign rather than one giant "add logging everywhere" prompt. This lesson is the campaign playbook.

---

## The Campaign Structure

**1. Define the pattern once — as an exemplar.** Straight from the last lesson: apply the concern to *one representative file by hand* (or with AI, reviewed to golden-file standard). This settles every micro-decision — log levels, what gets a key-value vs. message text, where the try/catch sits, what the cache key is — before it gets made forty slightly-different ways:

```text
Here is CustomerService before and after adding structured audit logging:
#file:CustomerService.java.orig   #file:CustomerService.java
The pattern: every public mutating method logs at info with entity type,
id, and actor; failures log at warn with the same keys plus the reason;
read methods are NOT logged.

Apply this exact pattern to InvoiceService. Do not alter any business
logic; if a method doesn't fit the pattern cleanly, skip it and list it.
```

*The before/after pair is the strongest exemplar format for retrofits — it teaches the delta itself, not just the end state.*

**2. Inventory, then batch.** Have the AI *find* the targets before changing any: "list every service class with public mutating methods not yet following this pattern" — the inventory is reviewable (and often surprising), and it converts an open-ended crawl into a checklist. Then process in **small batches** — a handful of files per round, not the whole codebase in one agentic sweep. Small batches keep each diff reviewable (the blind-acceptance bar doesn't relax because the change is repetitive — *especially* because it's repetitive, since reviewer attention decays too) and let early rounds refine the pattern before it's stamped forty times.

**3. Expect and harvest the exceptions.** The retrofit's real information is in the files that *don't* fit — the method that already logs differently, the service where "mutating" is ambiguous, the class with no clean seam. The "skip and list" instruction routes these to human judgment instead of letting the AI improvise per-file variants. Judgment calls made once, in review of round one, become instructions for round two — the campaign *learns*.

**4. Verify mechanically, per batch.** Tests green after every batch (a retrofit that changes behavior has failed by definition — "do not alter business logic" is testable). Better: add a *conformance check* where feasible — a grep/ArchUnit rule ("every @Transactional public method logs an audit event") that both validates the campaign and guards the pattern against future drift.

---

## Concern-Specific Notes

- **Logging/metrics:** the easy case — additive, low-risk, ideal first campaign. Main review risk: logging sensitive values (the AI will happily log the whole request object; the PII lesson's rules apply to log statements too).
- **Error handling:** medium risk — changing catch behavior *is* behavior change. Prefer retrofitting toward the boundary pattern (domain exceptions + one advice handler) over adding try/catch per method.
- **Auth checks:** high risk — a missed file is a vulnerability, so the *inventory* step is the security artifact; have the AI prove coverage ("list every endpoint and its auth annotation") rather than trusting the sweep.
- **Caching:** highest subtlety — invalidation, key design, and consistency are *design* work per site, not stamping. AI drafts each site; the pattern only covers mechanics.

And always ask the counter-question first: *should this concern be code-per-file at all?* Many retrofits are better served by one central mechanism — a filter, an aspect, a decorator (the Spring proxy machinery from the Java curriculum) — and "here are forty files needing X" is sometimes the smell that X belongs in one place. AI helps with that assessment too: "could this pattern be centralized instead? what would it take?"

Retrofit campaigns are the largest-scale prompting we've done — which strains the context window and begs for written task specs. Both pressures point at the next unit: spec-driven development and team workflows.
