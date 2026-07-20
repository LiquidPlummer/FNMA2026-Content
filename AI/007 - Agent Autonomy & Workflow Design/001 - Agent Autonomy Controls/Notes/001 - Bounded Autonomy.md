# Bounded Autonomy

An **agent** is AI that doesn't just answer — it *acts*: reads files, edits code, runs commands, iterates on results, across many steps without a prompt between each. Copilot's agent mode and tools like Claude Code operate this way, and the productivity is real — as is the new question they force: *what exactly is this thing allowed to do without us watching?* **Bounded autonomy** is the deliberate answer — an explicit division of actions into "freely," "with approval," and "never," designed before the agent runs rather than discovered after.

---

## The Three-Zone Model

Every agent action falls into a zone, and the zones are defined by **reversibility and blast radius**, not by how routine the action feels:

- **Green — autonomous.** Reversible, observable, contained: reading any file, editing working-tree source (git makes it reversible), running builds and tests, searching. This zone is *why agents are useful* — starving it with approval prompts buys no safety, only fatigue.
- **Yellow — propose, then proceed on approval.** Reversible-but-consequential or scope-expanding: adding dependencies, modifying many files at once, changing public API signatures, editing CI config, writing new migrations, git commits. The agent prepares the change and *stops*; a human look is the gate. This zone is where most judgment lives — and where the ruleset's "ask before" list becomes enforced behavior.
- **Red — never autonomous, possibly never at all.** Irreversible, external, or high-blast-radius: pushing to shared branches, deploying, touching production data or credentials, deleting untracked work, calling paid or external services, anything under `db/migration/**` that has already run. Red actions happen through the normal human process, at most *drafted* by the agent.

The zone boundaries are a team artifact — written down (the project ruleset is the natural home), reviewed like the infrastructure they are, and *tool-enforced where possible*: agent tools' permission systems (allow-listed commands, protected paths, approval prompts) turn policy into mechanism, and mechanism beats memory. A boundary that exists only as good intentions fails exactly when the agent is most confidently mid-flow. (The Hooks lesson in the Security unit shows the enforcement machinery close-up.)

---

## Sizing Autonomy to the Task — and the Evidence

Zones set the *ceiling*; the *dial* within them is set per task by the same calibration logic as review depth:

```text
Wide autonomy (long green runs, few checkpoints):
  - well-framed task, strong tests as referee, contained blast radius
  - e.g., "make the failing suite pass in module X", batch retrofits
    with an approved pattern, test generation

Narrow autonomy (short leash, plan approval, frequent checkpoints):
  - ambiguous requirements, weak test coverage, cross-cutting changes,
    unfamiliar codebase, anything near the yellow/red borders
  - e.g., "improve performance" (unbounded by nature), first run of a
    new campaign, changes in security-adjacent code
```

*The dial: autonomy earned by task clarity and verification strength — not by optimism.*

Two structural supports make wide autonomy safe rather than brave. **The frame is the leash:** an agent's real boundary is its task framing — context, constraints, success criteria (Unit 5's skill, now load-bearing). "Fix the CSV bug per this frame" bounds itself; "clean up the module" authorizes anything. **Verification is the enabler:** strong tests, mechanical gates, and structured checkpoints (the HITL lesson) are what *permit* long unsupervised runs — the agent can be left alone precisely because the referee can't be fooled. Teams that want more autonomy build better verification, not braver policies.

And one behavioral rule completes the model: agents drift — a task that starts green can *walk into* yellow ("fixing the test required a new dependency..."). Bounded autonomy therefore includes the agent's obligation to *notice and stop at the border*, which is its own design problem: stop conditions, next lesson.
