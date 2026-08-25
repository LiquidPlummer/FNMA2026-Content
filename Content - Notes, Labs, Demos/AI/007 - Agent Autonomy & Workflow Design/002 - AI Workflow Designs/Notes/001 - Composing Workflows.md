# Composing Workflows

Single prompts and single agent runs top out at tasks one context window can hold and one sitting can review. Past that, the unit of design becomes the **workflow**: multiple AI steps composed — sequenced, checked, sometimes opposed to each other — with human checkpoints and mechanical gates at the joints. Everything built so far is a component: framed tasks (the steps), structured IO (the plumbing between them), stop conditions and checkpoints (the joints). This lesson is the catalog of shapes they compose into.

---

## Chains: Decompose and Pipe

The fundamental shape — a pipeline where each stage does one thing and hands a defined artifact to the next:

```text
Feature workflow (the spec-driven flow, seen as a chain):

  [draft spec] → (human approves) → [generate tests from criteria]
     → (human reviews tests) → [implement until green]
     → (gates: build/lint/coverage) → [self-review pass] → (human PR review)

Each [stage] = fresh, focused context with only its needed inputs.
Each (joint) = checkpoint or mechanical gate from earlier units.
```

*A chain: stages small enough to do well, joints strong enough to catch failures between them.*

Why chained stages beat one mega-prompt, in engineering terms: each stage gets a **clean, purpose-built context** (no rot from the drafting stage polluting implementation — the Context Management payoff), failures are **localized** (bad tests get caught at the test joint, not excavated from a finished PR), and stages are **independently reroutable** — a failed stage reruns with a corrected frame while upstream artifacts stand. The joints carry the reliability: artifacts passed between stages are files or validated structures (specs, test suites, JSON), never "the vibe of the previous conversation."

---

## Review Loops: Generator vs. Critic

The second shape sets AI against AI — one context produces, a *separate* context critiques:

```text
[implementer produces diff]
   → [reviewer context: persona "skeptical security reviewer",
      input: the diff + the spec, output: findings JSON]
   → findings exist? → [implementer addresses them] → re-review
   → clean? → human checkpoint
```

*A generator-critic loop: the reviewer sees only the artifact and the contract — none of the generator's self-justifying context.*

The separation is the mechanism: a fresh critic context has no investment in the choices made and no memory of the rationalizations — it reads the diff cold, like a real reviewer, and finds what the generator's context literally cannot see (its own assumptions). Personas sharpen the critic; structured findings make the loop drivable; **one or two iterations** is the working budget (past that, the loop polishes noise — and a critic that finds nothing on round one is a prompt problem, not perfection). Variants of the same shape: test-vs-implementation (AI-TDD is a generator-critic loop where the critic is executable), doc-vs-code consistency checks, and the spec auditor from Unit 5.

---

## Orchestration: Parallel Work, Merged Results

The third shape splits work across parallel contexts and merges: the retrofit campaign (one pattern, N batches — parallelizable because batches are independent), multi-module translations, "analyze these 12 services for the deprecated API and report per-service." The design constraints are the classic parallelism ones, AI-flavored: split along **independence lines** (shared files = merge conflicts; shared *decisions* = divergent conventions — the shared pattern/exemplar goes into *every* worker's frame), keep workers' outputs **structurally identical** (same JSON schema, same diff format) so the merge step is mechanical, and put the **integration gate** (full build + suite over the merged result) after every merge, because per-worker green does not compose into whole-system green.

---

## Designing Our Own: The Checklist

Workflows are engineering artifacts; design them like it. For each candidate workflow: **(1)** What are the stages, and is each one a task we could frame on an index card? (If not — split.) **(2)** What artifact crosses each joint, and what validates it — schema, tests, human eyes? (An unvalidated joint is where the workflow will rot.) **(3)** Where are the human checkpoints, and are they placed by blast radius (per the HITL economics) rather than habit? **(4)** What are each stage's stop conditions and budgets? **(5)** Is the whole thing *observable* — can we tell afterward what ran, what stopped, what was approved? (The audit question — which the Security and Governance units take up next, because a workflow we can't observe is a workflow we can't secure or defend.)

Start with the chain — it's the shape that covers 80% of real needs — and add critics and parallelism only when the chain's review joints or wall-clock demand them. Composition multiplies capability; the next unit makes sure it doesn't multiply exposure.
