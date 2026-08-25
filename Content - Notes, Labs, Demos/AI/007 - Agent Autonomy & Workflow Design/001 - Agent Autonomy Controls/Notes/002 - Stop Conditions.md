# Stop Conditions

An agent's most dangerous mode isn't acting wrongly — it's *continuing* wrongly: burning an hour spiraling on a failing test, "resolving" an ambiguity by guessing, wandering out of scope one plausible step at a time. Humans stop when confused; agents by default press on, fluently. **Stop conditions** are the explicit criteria for halting — written into the task frame so the agent knows *when to stop and surface*, and designed by us so that stopping is always cheaper than the alternative.

---

## The Standard Stop Set

Four families cover nearly everything; the phrasing goes directly in the frame:

```text
Stop and report (do not proceed) when:

1. AMBIGUITY — the spec doesn't determine a decision and the choice
   isn't reversible trivially. Don't resolve ambiguity by assumption;
   present the options and your recommendation.

2. REPEATED FAILURE — the same test/build error survives 3 distinct
   fix attempts. Stop; report the attempts and current hypothesis.
   (Do not try a 4th variation of the same idea.)

3. SCOPE BORDER — completing the task seems to require anything on
   the yellow/red list: new dependency, schema change, touching files
   outside {{scope}}, weakening a test. Stop BEFORE the border action.

4. SURPRISE — reality contradicts the frame: the "failing" test passes,
   the file to modify doesn't exist, tests unrelated to the change
   start failing. The frame is wrong somewhere; stop and say so.
```

*The four stop families — ambiguity, thrash, scope, surprise — phrased as agent instructions with the counting and borders made explicit.*

Each family counters a specific agent pathology. **Ambiguity stops** counter silent guessing (the agent that picks a timezone handling and buries it in line 200). **Failure-count stops** counter thrash — the retry spiral where attempt five is attempt two with different variable names; three *distinct* attempts is a strong default budget. **Scope stops** operationalize bounded autonomy — the border from last lesson, made self-enforcing by instruction ("stop *before* the border action" — after is too late for red-zone items). **Surprise stops** are the deepest: a contradicted assumption means the *frame* is wrong, and every further step executes a broken plan with confidence.

---

## Designing for Good Stops

**A stop is a report, not a shrug.** The value of halting is captured in what comes back — require the state dump in the frame: what was attempted, what's currently believed, the specific blocker, options with a recommendation, and *what condition triggered the stop*. A good stop report converts directly into the next, better-framed run (often via the reset-and-handoff ritual from Context Management — a stopped agent's report is a handoff summary by construction).

**Budget caps are stop conditions too.** Wall-clock, step-count, or cost limits ("stop after ~20 minutes or 40 tool calls and report progress") catch the failure modes no semantic condition anticipates — the infinite exploration, the rabbit hole. Crude, universal, essential; most agent tools support them natively, and the cap should be sized to the task frame (a bugfix does not need an afternoon).

**Calibrate sensitivity — both directions fail.** Stop-happy agents (halting on every trivial choice: "which variable name?") train us to stop reading the reports; stop-resistant agents deliver the polished, wrong, out-of-scope PR. Tune with the reversibility test: *trivially reversible + within scope = decide and note it; otherwise stop.* The "note it" half matters — decisions taken autonomously get listed in the final report, so silent-assumption risk has a floor even in the green zone.

**Stops are data.** Recurring ambiguity stops on the same theme mean the specs are thin there; recurring scope stops mean frames are drawn too tight (or the task genuinely wants a human); recurring thrash stops in one module mean the tests there are misleading. The stop log is the agent process telling us where *our* artifacts need work — read it that way.

With single agents bounded and stop-aware, the remaining move is composition: chaining focused runs into pipelines with checkpoints between them — workflow design, next.
