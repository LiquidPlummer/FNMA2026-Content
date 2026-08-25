# AI Task Framing

Between "quick prompt" and "full spec" lies the workhorse skill: **task framing** — structuring any unit of delegated work with the context, constraints, and success criteria it needs to come back right the first time. Where spec-driven development is a methodology for features, framing is a habit for *everything* — a thirty-second discipline applied to every non-trivial ask, whether the executor is Copilot chat, an autonomous agent, or honestly, a teammate.

---

## The Frame

Four parts, every time — the zero-shot anatomy from Unit 1, matured by everything since:

**Context** — what the executor needs to know: the *why* (intent rescues ambiguous instructions — "this feeds the mobile app, which can't handle nulls" explains a dozen small decisions), the *where* (attached files, the golden exemplar, the relevant spec section), and the *state* ("the migration already ran; the entity already has the column").

**Task** — one outcome, stated as the artifact: "a `RefundPolicy` class + tests such that criteria below pass." If the task statement needs "and" more than once, it's probably two frames.

**Constraints** — the fences: scope ("touch only these two files"), dependencies ("no new libraries"), approach when it matters ("extend the existing switch; don't introduce a strategy class yet"), and the standing rules the ruleset already carries (don't restate those — reference the deltas).

**Success criteria** — how both parties know it's done: tests green, specific behaviors demonstrated, output format satisfied. The litmus test of the whole frame: *could a competent stranger verify completion without asking us anything?* If not, the criteria are vibes.

```text
Context:  Our CSV export (#file:ExportService.java) breaks when memo
          fields contain commas — see failing sample #file:bad-row.csv.
          Exports feed the finance team's Excel import, which expects
          RFC 4180 quoting.
Task:     Fix the escaping in ExportService so all fields are RFC
          4180-compliant. 
Constraints: No new dependencies (write the quoting inline); don't
          change the export's column order or header names — finance
          macros depend on them.
Success:  New test class proves: commas, quotes, newlines, and empty
          fields round-trip; existing ExportServiceTest stays green;
          bad-row.csv now imports cleanly (describe how you verified).
```

*A complete frame in nine lines: enough context to prevent wrong turns, fences around the blast radius, and completion criteria a stranger could check.*

---

## Framing Failures, Diagnosed

Bad results trace back to the frame with surprising regularity — worth learning the failure signatures:

- **Wandering output** (solved a related-but-different problem) → context stated *what* but not *why*; the executor optimized for the wrong goal.
- **Correct-but-unusable** (right logic, wrong shape/place) → missing constraints; the fences existed only in our head.
- **"Done" that isn't** (comes back incomplete, or gold-plated) → success criteria absent or unverifiable; "make it work" is not a criterion.
- **Endless clarifying questions** (or worse, silent guesses) → context assumed knowledge the executor lacks; the frame was written from *our* seat, not theirs. The fix is the empathy check: reread the frame pretending we know nothing but what it says.

The escalation rule from the spec lesson applies downward too: when a frame's Context section starts sprawling past a screen, the task wants a spec; when a frame feels like overkill, the task was probably prompt-sized all along. Framing is the middle gear.

---

## Frames as Team Currency

The quiet payoff: a well-framed task is *transferable*. It works pasted into Copilot, handed to an autonomous agent (the Agent Autonomy unit relies on exactly this — an agent without success criteria is an agent without stop conditions), attached to a ticket for a teammate, or saved as the reproducible record of what was asked. Teams that adopt framing as their ticket-writing standard discover the AI benefits and the human benefits are the same benefit: work specified well enough to delegate is work specified well enough to *review*, *parallelize*, and *audit*. That team dimension — shared rulesets, shared checkpoints — is the rest of this unit.
