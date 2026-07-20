# Agent Architectures

The workflow shapes from Unit 7 — chains, critic loops, orchestration — have settled into named **agentic design patterns**: recurring architectures for systems where AI components plan, act, and check each other. Like the Gang-of-Four patterns in the Java curriculum, their value is vocabulary and trade-off knowledge — recognizing which shape a problem wants, and what each shape costs. Whether we're *using* agentic tools (whose internals are exactly these patterns) or *building* automation with them, three architectures cover the working landscape.

---

## Planner–Executor

One role decomposes; another performs. The planner takes the goal and produces an explicit, ordered plan (steps, files, dependencies); executors take one step each, in fresh focused contexts, with the plan as shared state:

```text
Goal ──▶ PLANNER ──▶ plan.md (steps, per-step frames, done-criteria)
                        │  (human approves plan — the highest-ROI checkpoint)
                        ▼
             EXECUTOR runs step 1 ──▶ artifact + status ──▶ plan updated
             EXECUTOR runs step 2 ──▶ ...        (fresh context per step)
```

*The pattern behind every "plan mode": thinking and doing split into roles, with the plan as a reviewable, durable contract between them.*

Why the split beats one do-everything context: planning wants breadth (whole-task view, trade-off weighing) while executing wants depth (one step, tight context) — separating them lets each context be good at its job, keeps executor windows clean (Context Management, systematized), and puts the *human gate where leverage is highest* — between intent and action. The plan-file-as-state also gives the system crash-tolerance and auditability: any step can rerun; the plan shows what happened. Costs: latency and the risk of stale plans — reality discovered at step 3 must flow *back* into the plan (a surprise-stop, escalated to a replan), or executors march on a fiction.

## Reviewer Loops

The generator–critic shape, promoted to architecture: production and evaluation as separate roles with separate contexts — implementer/reviewer, writer/fact-checker, migrator/equivalence-checker — iterating until the critic passes or a round budget trips. The mechanism (a cold context reads the artifact against the contract, immune to the generator's rationalizations) and the disciplines (structured findings, one-two rounds, personas for the lens) carry over from Unit 7 wholesale. The architectural additions when this runs *inside* systems: the reviewer's rubric is a **versioned, evaluated artifact** (Prompt Evaluation applies — an uncalibrated judge is noise with authority), and reviewer verdicts land in structured form so the loop can be driven, measured, and audited mechanically. Trade-off: token cost roughly doubles per round — reviewer loops belong where error cost exceeds compute cost, not everywhere.

## Orchestrators

The coordination tier: an orchestrator decomposes work across *multiple* workers — parallel where independent (the retrofit campaign's batches, multi-module analyses), routed where specialized (the triage orchestrator sending schema questions to the DB-toolinged agent, code questions to the repo agent). The orchestrator holds the map, not the details: it frames sub-tasks (Task Framing, load-bearing again), collects structurally-uniform results, and runs the merge gate. The two failure modes to design against: **coordination overhead** exceeding the work (orchestration earns its keep at breadth — five-plus independent chunks — not for three sequential steps), and **divergence** among workers (the shared exemplar/convention block goes in every sub-frame; the integration gate catches what slips).

---

## Composed Systems, Same Physics

Real systems nest the patterns — an orchestrator whose workers are planner-executor pairs, each with a reviewer loop on output. What doesn't change with scale is the control physics from the last three units, which is the real lesson of this unit:

- **Every agent boundary is a trust boundary.** Autonomy zones, stop conditions, and hooks apply *per role* — a reviewer needs read-only capability; only executors need write; nothing autonomous gets red-zone powers. Multi-agent systems make least-privilege *easier*, if we let them.
- **Structured IO is the nervous system.** Roles communicate in schemas and files, never vibes — that's what makes composed systems testable and observable.
- **Human checkpoints sit at the seams** — plan approval, merge review, final PR — placed by blast radius exactly as HITL taught.
- **More agents ≠ more capability.** Each hop adds cost, latency, and a place for errors to compound; the burden of proof is on the added role. The strongest systems we'll encounter are usually the *simplest shape that verification allows* — which, one final time, means investment in tests, gates, and rubrics is what actually buys autonomy.

Architectures answer *how* agentic systems are built. The final lesson answers *under what rules* — the organizational layer: policy, audit, and accountability.
