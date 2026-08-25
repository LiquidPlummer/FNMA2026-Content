# Evaluating Prompts

Most prompts are disposable — typed, used, gone, and "good enough" is judged by glancing at the one answer. But some prompts become *assets*: the ruleset every request inherits, the test-generation prompt the whole team reuses, the extraction prompt inside a pipeline that runs nightly. Reusable prompts deserve what reusable code gets — evaluation: deliberate assessment of quality, consistency, and failure behavior *before* we depend on them.

---

## The Two Questions

**Quality:** does the prompt produce *good* output — correct, complete, in-format, in-scope? **Consistency:** does it do so *reliably* — across runs, across varied inputs, across the model's inherent randomness? A prompt that shines once and degrades on the third variation isn't good; it's lucky. LLMs are nondeterministic — the same prompt yields different outputs run to run — so single-sample judgment is superstition. Evaluation means multiple runs, varied inputs, defined criteria.

---

## Evaluating by Hand: The Minimum Bar

For any prompt about to be reused or shared, the lightweight protocol:

**1. Write the rubric first.** Three to six checkable criteria, before looking at outputs — otherwise we grade on vibes and fluency wins:

```text
Rubric for our "generate repository test" prompt:
  □ Compiles against our actual classes (no invented methods)
  □ Uses @DataJpaTest + our naming convention (behaviorStatement style)
  □ Covers: happy path, empty result, constraint violation
  □ No testing of framework behavior (no "save assigns id" filler)
  □ Zero modifications needed to run  — count edits required as the score
```

*A rubric turns "looks good" into checkboxes — the same move as acceptance criteria on a ticket.*

**2. Run it several times, on varied inputs.** Three to five runs, including an awkward input (an entity with relationships, an empty class, a legacy monster). Consistency failures and edge-case behavior only show up here.

**3. Score, fix the worst failure, re-run.** Prompt iteration is debugging: change *one thing* (tighten a constraint, add an example of the failure case, restructure the ordering), then re-test. Changing five things at once teaches nothing about what worked — the same experimental discipline as any tuning.

**4. Version the survivors.** A prompt that passes goes into the team's prompt library / ruleset repo *with its rubric*, so the next editor can re-verify after changing it. Prompts without tests rot exactly like code without tests.

---

## Comparing Prompts: A/B on the Same Rubric

When two phrasings compete (few-shot vs. zero-shot, persona vs. plain, long vs. short), run both against the same inputs and rubric, several times each, and count. Two findings recur across almost everyone's first comparisons: **shorter, structured prompts often beat longer prose** (every sentence is a chance to mislead; labeled sections beat paragraphs), and **one good example often beats three mediocre paragraphs** (the few-shot lesson, vindicated by measurement). The point isn't these specific results — it's that measuring frequently contradicts intuition, which is why we measure.

---

## Scaling Up: AI as Evaluation Labor

Two force multipliers, both with honest caveats. **AI-assisted critique:** paste the prompt itself and ask *"Where is this ambiguous? What inputs would break it? What's underspecified?"* — models critique prompts well, and it's the cheapest first pass. **LLM-as-judge:** for high-volume pipelines, a second AI call scores each output against the rubric ("does this JSON meet these five criteria? answer per-criterion"). Powerful and standard practice — but a judge model shares the biases of the judged (fluency bias above all), so calibrate it against human-scored samples before trusting its numbers, and keep the *mechanical* checks (does it parse? does it compile? do the tests run?) as the floor no judge can talk its way past.

The graduation this lesson completes: prompts stop being incantations and become engineered artifacts — specified, tested, versioned, measured. The rest of the curriculum builds on exactly that footing, starting with applying the whole toolkit to real development domains: data, persistence, and beyond.
