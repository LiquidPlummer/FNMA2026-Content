# Lab 001 — Debugging the Triage Report

A support-ticket triage report that runs without crashing and produces confidently wrong numbers. Our job is to find out why.

Six bugs are hiding in `src/triage.js`, one for each fundamental from today's session: **declarations**, **types**, **coercion**, **equality**, **scope**, and **functions**. None of them throw an error on their own. Every one of them produces a plausible-looking answer that happens to be wrong — which is exactly how these bugs behave in real code.

## Prerequisites

| Software | Required Version |
|---|---|
| Node.js | 24.19.0 LTS (18.0.0 or newer will work) |

No dependencies to install. From the lab directory:

```bash
node src/test.js
```

*Runs the grader. `npm test` does the same thing.*

## The two rules

1. **Don't edit `src/test.js`.** The tests are correct. The code they're testing is not.
2. **Every fix is one to three lines.** If we're rewriting a whole function, we've stopped diagnosing and started guessing. Back up and read the failure again.

---

## Stage 0 — Get it to run

Run the tests. Nothing runs at all:

```
STAGE 0 - triage.js will not load, so no checks can run.

  ReferenceError: Cannot access 'formatTicket' before initialization
```

*The module fails while it's still loading, so no test ever gets a chance to execute.*

The error names the problem precisely, which makes this a good warm-up: **`formatTicket` is being used on a line that runs before the line that creates it.** Open `src/triage.js` and find both — the call near the top, the definition at the bottom.

Before fixing it, it's worth asking why this is an error at all. There are other functions in this file that get called from above their definitions without complaint. The difference is in *how* `formatTicket` is declared.

There are two reasonable fixes here, and they're worth a minute of discussion with our pair:

- Move the definition above the call.
- Change how it's declared so that it hoists.

Both make the tests run. They are not equally good, and which one we prefer says something about how we want the file to read. Pick one, then run the tests again.

Six failures should appear. Now the real work starts.

---

## Stage 1 — Fix the six

Each failing check is tagged with the fundamental it depends on:

```
FAIL  [EQUALITY] count of escalated tickets
      expected: 2
      actual:   0
```

*The tag is the hint. It tells us which rule from the session the code is currently breaking, but not where.*

Work them in whatever order we like — they're fully independent, so fixing one will never fix or break another. For each one, the useful sequence is:

1. Read the expected and actual values. What *kind* of wrong is this? Off by a little, or wrong in shape?
2. Find the function the check calls, and read it out loud to our pair.
3. Predict which line is lying before changing anything.
4. Change one line. Re-run.

A few notes that will save time:

**The `actual` value is the biggest clue.** A number where we expected a number means a logic problem. A *string* where we expected a number means something coerced. `undefined` means a function didn't hand anything back. `NaN` means a conversion failed and nobody checked.

**Two of these have tempting wrong fixes** that change the number without fixing the bug. If a change makes the test pass but we can't explain *why* the original was wrong, we haven't found it yet — we've found a different bug that happens to cancel it out.

**One line in this file looks like a bug and isn't.** It breaks one of the rules from the board, deliberately and correctly. Finding it and being able to defend it is part of the lab.

When all seven checks pass, the report is correct.

---

## Exercises

Unguided from here. The tests won't help — we're writing new code, so we'll need to decide for ourselves what correct means and prove it.

**1. Count the unassigned.** Add `countUnassigned(list)`. One ticket in the data has an empty-string assignee. Write it, then write down what your check does with an assignee of `null`, `undefined`, and `"0"`. Were all three what you expected?

**2. Average hours open.** Add `averageHoursOpen(list)` returning the mean, rounded to one decimal place. Decide what it should return for an empty list, and defend the choice — `0`, `NaN`, and `null` are all defensible for different reasons.

**3. Harden the converter.** `toHours` currently accepts anything `Number()` will take. Decide what it should do with `""`, `null`, `"  "`, and `"-5"`. At least two of those convert to a number without complaint today, and at least one of them shouldn't be allowed through. Write the guard.

**4. An unknown priority.** Add a ticket with `priority: "4"` to the data. Nothing in `SLA_HOURS` covers it. Run the tests — they should all still pass, and the new ticket should never breach. Trace *which* line makes that true.

**5. Kill the closure.** Rewrite `makeRowRenderers` so it doesn't capture a loop variable at all. There's a way to build the same array where the numbering problem simply cannot happen. Once it's working, the original bug becomes impossible rather than merely fixed — that distinction is the point of the exercise.

**6. Defend the odd line.** Find the line from Stage 1 that breaks a rule on purpose. Write a one-sentence comment above it explaining why it stays. If we can't write that sentence, the line should probably change.
