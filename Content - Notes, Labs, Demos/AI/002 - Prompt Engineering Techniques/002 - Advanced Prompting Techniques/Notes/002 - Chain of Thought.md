# Chain of Thought

**Chain-of-thought prompting** asks the model to reason step by step *before* answering: analyze, then conclude. The technique rests on a mechanical fact about LLMs — they generate one token at a time, and each generated token becomes context for the next. A model that jumps straight to an answer computes it in one leap; a model that writes out intermediate steps effectively gets scratch paper, and its conclusions are conditioned on its own visible work. For multi-step problems, that difference is large.

---

## Invoking It

The simplest forms work: *"think step by step,"* *"reason through this before answering."* The stronger forms structure the steps for the problem at hand:

```text
This method occasionally returns a stale price. Before proposing any fix:

1. List every path by which `cachedPrice` is read or written.
2. For each pair of concurrent paths, state what interleaving could occur.
3. Identify which interleaving produces a stale read.
4. Only then propose the minimal fix, and explain why it closes
   that specific interleaving.
```

*Structured chain of thought: the reasoning sequence is prescribed, and the answer is forbidden until the analysis exists to support it.*

The prescription matters more than the magic words. "Think step by step" lets the model choose its steps — fine for arithmetic-flavored problems, weak for domain analysis. Naming the steps (*enumerate paths → analyze interleavings → then fix*) forces the analysis we'd demand from a human engineer, in the order that prevents conclusion-first rationalization.

Where it earns its cost: debugging and root-cause analysis, concurrency and ordering questions, tricky business-rule implementation ("compute the proration for a mid-cycle downgrade"), design trade-offs, and anything where the direct answer has historically come back subtly wrong. Where it's waste: lookups, boilerplate, simple transformations — a step-by-step essay preceding a getter is noise. A practical modern note: reasoning-tuned models (and Copilot's deeper modes) do much of this internally by default — explicit chain-of-thought prompting matters most when the visible *structure* of the reasoning is something we want to review, steer, or keep.

---

## The Real Payoff: Auditable Reasoning

The accuracy boost is only half the value. The visible chain is a **reviewable artifact** — and reviewing reasoning catches errors that reviewing conclusions cannot:

- **Wrong premises show up early.** Step 1 says "cachedPrice is only written by refresh()" — and we know there's a second writer in the admin path. The chain is wrong at step 1; the conclusion was doomed; we caught it in seconds by reading the *first line* instead of debugging the fix.
- **Skipped cases show as gaps.** A proration analysis that never mentions the leap-year February case tells us what the answer doesn't cover — silently absent from a bare conclusion.
- **We can intervene mid-chain.** "Your step 2 is wrong — the admin path also writes. Redo from there." Correcting one step is cheaper than re-rolling the whole answer, and the revised chain stays consistent.

One honest caveat balances this: a generated chain of thought is *itself* generated text — a plausible-sounding narrative, not a guaranteed transcript of the model's actual computation. Models can produce correct answers with confabulated justifications and (worse) fluent reasoning toward wrong answers. So we review chains the way we review proofs, not testimony: check each step's *claim* against the code and the facts, not against how confident it sounds. A chain we've verified step-by-step is strong evidence; a chain we've skimmed is decoration.

---

## Composing with the Rest of the Kit

Chain of thought stacks naturally with the other techniques: a **persona** sets the lens, the **chain** structures the analysis, and (next lesson) **output constraints** shape the final answer — *"as a security reviewer [persona], trace how user input reaches this query, step by step [chain], then report findings as severity/line/fix [format]."* That three-layer shape — who, how to think, what to emit — is close to a universal template for hard prompts, and the third layer is where we go now.
