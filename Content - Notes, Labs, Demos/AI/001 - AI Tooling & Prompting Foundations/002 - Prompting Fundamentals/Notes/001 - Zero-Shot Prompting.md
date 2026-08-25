# Zero-Shot Prompting

A **zero-shot prompt** is a direct ask — instructions only, no examples of the desired output. It's the default mode of every chat interaction, and most day-to-day prompts should stay zero-shot: when the task is common and the instructions are clear, examples add nothing but typing. The skill is making "instructions are clear" actually true.

---

## What the Model Does with a Bare Ask

An LLM answers the question it *infers*, not the one we meant. Given an underspecified prompt, it fills the gaps with the most statistically typical interpretation — average style, average assumptions, average language choice. That's the diagnosis behind most "the AI gave me something useless" complaints: the prompt described a *category* of task and got a category-typical answer.

```text
Weak:    Write a function to validate input.

Strong:  Write a Java method that validates a US phone number string.
         Accept formats: (555) 123-4567, 555-123-4567, 5551234567.
         Return the normalized 10-digit string, or throw
         IllegalArgumentException with a message naming the problem.
         No regex libraries beyond java.util.regex. Java 21.
```

*The same request, underspecified vs. specified: the weak version forces the model to guess language, formats, error behavior, and dependencies — four coin flips we could have called.*

---

## The Anatomy of a Good Zero-Shot Prompt

Four elements cover nearly everything; not every prompt needs all four, but a disappointing result is usually missing one:

- **Task** — the verb and the object, precisely: *generate*, *refactor*, *explain*, *review* — what artifact should exist afterward?
- **Context** — what the model can't infer: the language and version, the framework, relevant types, how the code will be called. In Copilot chat, context is often *attached* rather than typed — highlight the code, reference `#file:InvoiceService.java` — which beats prose descriptions every time.
- **Constraints** — the boundaries: what to use, what to avoid, scope limits ("modify only this method"), style rules ("match the surrounding code's conventions").
- **Output shape** — what form the answer takes: "code only, no explanation," "a bulleted list of issues, worst first," "a unit test class."

```text
Task:        Refactor this method to eliminate the nested if-chains.
Context:     [highlighted: a 40-line Java method] — it's called on every
             request, so no new allocations in the hot path.
Constraints: Behavior must be identical; keep the public signature;
             use early returns, not a strategy class.
Output:      The rewritten method only.
```

*The four elements, labeled. In practice they're written as flowing prose — the labels are a checklist, not a required format.*

---

## Iteration Is Part of the Technique

Zero-shot is a dialogue opener, not a single-shot exam. A near-miss response doesn't mean starting over — it means steering: "keep the structure, but the discount rule is wrong — discounts apply *before* tax," or "same thing, but Java 11 compatible." Two refinement turns on a decent prompt beat five re-rolls of a vague one, because each turn adds context the next response builds on.

The judgment call this lesson leaves open: some tasks resist description — *"format it like this"* gestures at something an example would show instantly. When instructions grow longer and fuzzier than a demonstration would be, that's the signal to switch techniques — **few-shot prompting**, next.
