# Managing Context Windows

Everything an AI knows about our task — the prompt, the attached files, the ruleset, the whole conversation so far — lives in the **context window**: a finite token budget that is the model's entire working memory. Nothing outside it exists; everything inside it competes for influence. Most mid-conversation quality collapses ("it forgot what we agreed," "it's re-suggesting the thing we rejected an hour ago") aren't model failures — they're context failures, and they're ours to manage.

---

## The Budget Model

Think of context as RAM with three tenants: **standing context** (rulesets, system instructions — the fixed overhead), **task context** (the files, specs, and examples we've attached), and **conversation history** (every turn so far, including every failed attempt and dead end). The budget is large on modern models — hundreds of thousands of tokens — but three effects bite long before it's *full*:

- **Truncation:** past the limit, something gets dropped or summarized — usually the oldest turns, which is where the original requirements often live. Symptom: late-session output that contradicts early-session agreements.
- **Dilution:** attention is finite even inside the window; the model weights recent and salient content. Forty attached files don't make it forty times smarter — they bury the three lines that mattered. Middles of long contexts are measurably neglected ("lost in the middle").
- **Context rot:** the insidious one. A long session accumulates *contradictions* — the approach we tried and abandoned, the bug we fixed, the requirement that changed. All of it is still in there, still exerting pull. Symptom: previously-rejected ideas resurfacing, or fixes that resurrect earlier bugs. The conversation isn't out of memory; it's *polluted*.

---

## The Disciplines

**Curate inclusions like a reviewer's attention.** Attach the three relevant files, not the module ("just in case" attachment is dilution on purpose). Reference stable artifacts instead of pasting them repeatedly — this is *why* specs, rulesets, and golden files live in the repo: one pointer, not four re-pastes. When only part of a big file matters, excerpt it and say so.

**Reset deliberately — and more often than feels natural.** A fresh conversation with a clean brief beats a polluted marathon almost every time. Reset when: the task changes (new feature, new bug — new session), a long debugging spiral finally resolved (the spiral's wreckage is now pure rot), or output quality visibly degrades. The skill that makes resets cheap is the **handoff summary** — have the AI write its own:

```text
Before we reset: summarize this session for a fresh conversation —
the goal, the decisions made and WHY, the current state of the code,
what remains, and the dead ends (so the next session doesn't retry them).
```

*The reset ritual: distill the session's live knowledge into a brief, carry it (plus the spec and files) into a clean window, leave the rot behind.*

That summary — reviewed and corrected by us, since it inherits the usual accuracy caveats — plus the durable artifacts *is* the task state. Everything else was scaffolding.

**Put the important things where attention lives.** Beginnings and ends of the context; restated when a session runs long ("reminder of the constraints before this next step: ..."). If a mid-session instruction keeps getting dropped, that's the signal to promote it — into the current prompt's tail, or permanently into the ruleset.

**Agent modes double the stakes.** Autonomous agents burn context fast — every file read, every command output lands in the window — and a truncated agent quietly forgets its own mission. The mitigations are the same, systematized: tight task frames (small missions per run), external state (plans and progress written to files, not held in-conversation), and fresh runs per subtask rather than one epic session. The workflow designs in the Autonomy unit lean on exactly this.

---

The other half of reliable AI plumbing: when outputs feed pipelines rather than people, structure has to be *enforced*, not requested. Structured IO, next.
