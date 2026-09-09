# Continuous Delivery

**Continuous Delivery** is the practice of keeping software in a state where it *could* be released to production at any time, with the pipeline automating every step up to that final release — which a human authorizes.

The previous topic covered Continuous Deployment, where that final step happens automatically too. Continuous Delivery is the same machinery with one deliberate difference: **the pipeline stops at the door to production and waits for someone to say go.**

Both share the abbreviation "CD," which causes endless confusion. The distinction worth remembering:

| | Continuous Delivery | Continuous Deployment |
|---|---|---|
| Pipeline automated through pre-production | Yes | Yes |
| Every build produces a releasable artifact | Yes | Yes |
| Production release | Manual approval | Automatic |
| Release is a... | business decision | technical non-event |

Continuous Delivery is the more widely applicable of the two, and it's the prerequisite for the other — a team can't deploy continuously without first being able to deliver continuously.

---

## Every Build Produces a Deployable Artifact

The foundational rule: **every successful build produces an artifact that could go to production.**

Not source code that would need assembling. Not "we'd need to run the packaging step." A finished, versioned, deployable thing — a `.jar`, a Docker image, a bundle — sitting in a repository, ready to install.

This changes the character of a release. Releasing stops being a construction project and becomes a retrieval: take artifact `1.4.7`, put it on the production servers. Nothing is built at release time, so nothing can go wrong at release time that hasn't already gone wrong somewhere we could see it.

### Build Once, Promote the Same Artifact

This deserves emphasis because it's the rule most often broken.

The artifact is built **exactly once**, at the start of the pipeline, and that identical file is what moves through every environment.

```
build  →  artifact 1.4.7  →  deploy to test
                          →  deploy to staging
                          →  deploy to production
```

*One artifact, built once, promoted unchanged through every environment — so what reaches production is bit-for-bit what was tested.*

The alternative — rebuilding for each environment — quietly destroys the value of testing. If we build separately for staging and for production, the production build has never been tested. A dependency may have resolved to a different version, a build tool may have updated, the build machine may have had a different configuration. These differences are usually harmless and occasionally catastrophic, and we'd have no way to know which.

Environment-specific differences (database URLs, credentials, endpoint addresses) belong in **configuration supplied at deploy time**, not baked into the artifact. Same artifact, different configuration.

---

## The Deployment Pipeline

The **deployment pipeline** is the sequence of stages an artifact passes through on its way to being releasable. Each stage is a progressively more demanding test of the same artifact, and each one can reject it.

A typical shape:

```
┌─────────────┐  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐  ┌────────────┐
│   Commit    │→ │ Acceptance   │→ │   Manual/   │→ │   Staging    │→ │ Production │
│   Stage     │  │    Tests     │  │  Exploratory│  │              │  │            │
├─────────────┤  ├──────────────┤  ├─────────────┤  ├──────────────┤  ├────────────┤
│ compile     │  │ integration  │  │ UAT         │  │ smoke tests  │  │  APPROVAL  │
│ unit tests  │  │ end-to-end   │  │ QA testing  │  │ perf tests   │  │  REQUIRED  │
│ static      │  │ contract     │  │ (optional)  │  │ prod-like    │  │            │
│  analysis   │  │              │  │             │  │  config      │  │            │
│ package     │  │              │  │             │  │              │  │            │
└─────────────┘  └──────────────┘  └─────────────┘  └──────────────┘  └────────────┘
   ~5 min          ~20 min            hours            ~15 min          human
```

*Stages get slower and more thorough as the artifact advances; the fastest, cheapest checks run first so failures surface early.*

The ordering principle is **fail fast and fail cheap**. Compilation errors and unit test failures are caught in the first five minutes, before we've spent thirty minutes of pipeline time on end-to-end tests. Each stage is a filter, and the artifacts that reach the later stages have already survived everything before them.

### Promotion

**Promotion** is an artifact advancing from one stage to the next. The rule is simple: an artifact is promoted only if the current stage passes completely. A failure anywhere stops that artifact's progress — it never reaches production.

Promotion between the automated stages is itself automatic. The artifact moves from commit stage to acceptance tests to staging without anyone doing anything, as long as each stage is green. The only stop is the one we put there deliberately.

### What Fails a Stage

Any stage can reject an artifact:

- **Commit stage** — compilation errors, failing unit tests, a quality gate violation from static analysis (topic 006).
- **Acceptance tests** — a component integration that works in isolation but breaks in combination; a broken API contract; an end-to-end user journey that no longer completes.
- **Staging** — a smoke test failing against production-like configuration, a performance regression, a database migration that fails against a realistic data volume.

When a stage fails, that artifact is dead. We don't patch it and push it forward — we fix the code, commit, and let a new artifact go through the pipeline from the beginning. This is important: any artifact in production has passed every stage in sequence, with no exceptions carved out for it.

---

## The Manual Approval Gate

This is the defining feature of Continuous Delivery, so it's worth being precise about what it is and isn't.

**What it isn't:** a technical step. Nothing is being built, assembled, or figured out at the gate. The artifact has passed every automated check and is sitting ready. If approved, deploying it is a scripted operation that takes minutes.

**What it is:** a decision about *timing and business readiness*. The questions at the gate are things automation genuinely cannot answer:

- Is this the right moment — are we mid-quarter-close, or is it Friday afternoon?
- Has support been briefed on the change?
- Is the marketing announcement ready to go out with it?
- Does a compliance process require a named approver on the record?
- Does the customer we promised this to expect it today or next week?

In practice the gate appears as a button in the pipeline tool. Someone with authority clicks it, the deployment stage runs, and the pipeline records who approved what and when — which is often the entire point in a regulated environment.

The crucial property is that **the gate does not gate quality.** Quality was settled by the automated stages. If the person at the gate is expected to review the code or judge whether it works, the pipeline has failed at its job and the gate has become a bottleneck pretending to be a safeguard.

---

## Releasing Becomes a Business Decision

This is the outcome worth internalizing, because it reframes what a release *is*.

Under the traditional model, releasing was a technical event: risky, slow, effortful, and therefore rare. Because it was rare, each release carried months of accumulated change, which made it riskier still — a self-reinforcing cycle. Release dates were driven by how long the engineering process took.

Under Continuous Delivery, the technical risk has been factored out. There is always a validated, deployable artifact available. Deploying it is a routine, automated, reversible operation. So the question "should we release?" stops being an engineering question at all.

Some consequences of that shift:

- **Release frequency is decoupled from engineering capability.** A team can release weekly, daily, or on demand, because the constraint is no longer technical.
- **The cost of *not* releasing becomes visible.** When shipping takes six weeks of effort, delay looks free. When the artifact has been ready since Tuesday, sitting on it is an obvious choice with an obvious cost.
- **Emergency fixes stop being scary.** The path to production is the same well-exercised path used every day, not a rarely-used manual procedure attempted under pressure.

---

## Key Takeaways

- Continuous Delivery keeps software always releasable, automating the pipeline up to production and stopping for a human approval.
- The only difference from Continuous Deployment is that final manual gate — everything before it is identical.
- Every successful build produces a deployable, versioned artifact; nothing is constructed at release time.
- The artifact is built once and promoted unchanged through every environment, with environment differences supplied as configuration.
- The deployment pipeline orders stages fast-and-cheap first, so failures surface early; a failure at any stage kills that artifact.
- The approval gate governs timing and business readiness, not quality — quality is the automated stages' job.
- The result is that releasing becomes a business decision rather than a technical event.
