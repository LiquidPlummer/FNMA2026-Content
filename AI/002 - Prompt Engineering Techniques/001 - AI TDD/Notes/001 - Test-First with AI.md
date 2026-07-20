# Test-First with AI

Test-driven development — write the failing test, then the code that passes it — gains a second justification in the AI era. Beyond its classic design benefits, **the test suite is the specification the AI can't argue with**: prose prompts are interpreted, but a failing test is exact, executable, and self-verifying. Test-first with AI turns "generate code and hope it's right" into "generate code until the referee says pass."

---

## The Workflow

**1. Specify as tests — with AI help drafting them.** Start from behavior, stated in prose, and have the AI expand it into test cases:

```text
I'm implementing a PromoCode class. Rules:
- codes apply a percent discount (1-100) to an order total
- expired codes throw PromoExpiredException
- codes have a minimum order amount; below it, no discount, no error
- discounts round half-up to cents

Write the JUnit 5 test class first — cover each rule plus the edge
cases you can think of. Do NOT implement PromoCode yet; let the
tests fail to compile.
```

*Behavior in, tests out — and the explicit instruction to stop before implementing, keeping the phases separate.*

**2. Review the tests like a contract — because they are one.** This is the step that carries all the weight. The AI will propose cases we listed *plus* ones we didn't (boundary: exactly the minimum amount; a 100% discount; expiry at the exact moment). Each proposal is a question about our spec — answer them deliberately. Wrong expected values get fixed *now*: an error in the tests becomes an error in the implementation, silently. The critical-review disciplines from the last unit apply at full strength here, precisely because everything downstream trusts these tests.

**3. Generate the implementation against the tests.** With tests as context, ask for code that passes them: *"Now implement PromoCode so this test class passes. Don't modify the tests."* The last sentence is load-bearing — an AI blocked by a hard test will, if allowed, "fix" the test. Test edits are ours alone.

**4. Run, feed back, repeat.** Failures go back verbatim — *"3 pass, this one fails: [output]"* — the same evidence-driven loop as AI debugging, but with the definition of done nailed to the floor. Green suite = done, by construction.

---

## Why This Combination Works So Well

Each half covers the other's weakness. The AI's weakness is *specification drift* — plausible code that solves a slightly different problem; tests eliminate the drift because "slightly different" fails. Our weakness (when hand-writing TDD) is the *tedium* of exhaustive cases; the AI generates fifteen edge-case tests in the time we'd write three, and reviewing proposed cases is faster than inventing them. The result is also regression armor for every *future* AI interaction with this code: refactors, extensions, and cross-cutting changes (a later unit) all run against the same referee.

Two cautions keep it honest. **Same-author blindness:** tests and implementation generated in one breath can share one misunderstanding — which is why the phases stay separate and the test review is human and thorough. Generating tests *after* the implementation ("write tests for this") is documentation, not TDD — it enshrines current behavior, bugs included. **Coverage theater:** AI happily produces many low-value tests (getters, framework behavior); steer with "test the business rules and boundaries, skip trivial accessors."

---

## Where It Fits

Test-first shines wherever behavior is specifiable: business logic, parsers, calculators, API contracts, bugfixes (the reproduce-as-failing-test move from the ticket workflow — the fix is done when the red test goes green). It's overkill for exploration and prototypes — spike freely, then TDD the version that stays. As the tasks we delegate get bigger through this curriculum, the tests-as-spec idea scales with them, culminating in the spec-driven workflows of a later unit — where the spec grows prose and acceptance criteria around exactly this executable core.
