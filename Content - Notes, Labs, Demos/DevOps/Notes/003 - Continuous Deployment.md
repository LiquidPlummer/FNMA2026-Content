# Continuous Deployment

**Continuous Deployment (CD)** is the practice of automatically releasing to production every change that passes the pipeline. No human approves the release. If the automated checks are green, the change goes live.

This is the furthest extension of the pipeline we've been building. Continuous Integration told us a change is safe to merge; Continuous Deployment acts on that verdict without asking anyone for permission.

---

## What "Automatically" Actually Means

Consider what happens when a developer merges a one-line fix at 10:15 AM on a team practicing Continuous Deployment:

```
10:15  commit merged to mainline
10:15  pipeline triggered automatically
10:19  build + tests pass
10:21  deployed to production
10:21  live for users
```

*A single change moving from merge to production with no human intervention at any step.*

Six minutes, and no one signed off. There was no release meeting, no change ticket, no deployment window on a Thursday evening. The developer merged and went to lunch.

That can sound reckless, and done without preparation it would be. The reason mature teams do it safely is that the safety has been moved *into the pipeline* rather than sitting in a human approval step. A person glancing at a change and saying "looks fine to me" is a weak check — much weaker than a comprehensive automated test suite. Continuous Deployment doesn't remove the gate; it replaces a slow, unreliable gate with a fast, consistent one.

---

## What It Requires Before It's Safe

Continuous Deployment is not something a team adopts by flipping a switch. It's what becomes possible after a specific set of capabilities are in place. Without them, automatic deployment is just automating our mistakes into production faster.

### Genuine Test Coverage

The test suite is now the *only* thing standing between a bad change and real users. That raises the bar considerably.

We need meaningful coverage across levels — unit tests for logic, integration tests for how components fit together, end-to-end tests for critical user journeys. And "coverage" here means tests that would actually fail if the behavior broke, not a high percentage number achieved by exercising code without asserting anything about it.

Flaky tests are especially corrosive in this model. If the suite fails randomly one run in ten, the team starts re-running failed builds to get a green result — and at that moment the gate is gone.

### Monitoring and Alerting

No test suite catches everything. Some failures only appear under real production load, real data, and real user behavior. So the second requirement is that we can **detect a problem in production quickly**, without waiting for a customer to report it.

That means watching error rates, latency, throughput, and business-level signals — orders placed, logins succeeding — with alerts that fire on a meaningful deviation. In this model, monitoring isn't operational hygiene; it's the outer layer of the test strategy.

### Fast, Reliable Rollback

The third requirement follows from the second: once we've detected a problem, we need to undo it in minutes, not hours.

Fast rollback is what makes the whole approach reasonable. If reverting a bad deployment takes ninety seconds, the worst case of an undetected bug is a short window of degraded service. If reverting takes four hours of manual work, the worst case is a bad day for everyone — and no team should be deploying automatically under those conditions.

Notice the pattern across all three: because we can't prevent every failure, we invest in **detecting and recovering** from failure quickly. That's a different mindset from trying to make deployment so rare and so scrutinized that it never goes wrong.

### Small Changes

This one is implied by the CI practices in topic 002, but it matters even more here. When each deployment contains one small change, a production problem points to an obvious cause. When a deployment contains fifty changes, we're back to investigating. Small batches are what make fast diagnosis possible.

---

## Feature Flags: Separating "Deployed" from "Released"

There's an obvious tension in everything above. If every merge goes straight to production, how do we work on a feature that takes three weeks and isn't safe to show anyone yet?

The answer is to stop treating deployment and release as the same event.

- **Deployed** — the code is running in production.
- **Released** — users can actually see and use it.

A **feature flag** (or feature toggle) is a conditional that decides at runtime whether a piece of functionality is active. The code ships to production in a disabled state, and we turn it on separately.

```java
if (featureFlags.isEnabled("new-checkout-flow")) {
    return newCheckoutService.process(cart);
} else {
    return legacyCheckoutService.process(cart);
}
```

*A feature flag lets unfinished code be deployed to production safely — it's present but inactive until the flag is turned on.*

This resolves the tension neatly. The three-week feature is merged in small daily increments, each one deployed to production behind a flag that is off. Nothing is visible to users, and nothing conflicts with anyone else's work, because it's all integrated continuously.

Flags buy us more than that:

- **Gradual rollout.** Enable the feature for 1% of users, watch the monitoring, then 10%, then everyone. A problem affects a small group instead of the whole user base.
- **Instant off switch.** If something goes wrong, flip the flag off. That's faster than a rollback and doesn't require a deployment at all.
- **Release timing as a business decision.** Marketing wants the feature live Tuesday at 9 AM? The code has been in production for two weeks; someone flips a flag.

The one caution: flags accumulate. Each one is a branch in the code, and a codebase with two hundred stale flags is genuinely hard to reason about. Flags for feature rollout are meant to be temporary — once a feature is fully released and stable, the flag and the old code path should be deleted.

---

## When It's Appropriate — and When It Isn't

Continuous Deployment is a strong fit when:

- The system is a web application or service the team controls end to end.
- Deployments are low-risk and easy to reverse.
- The team has the coverage, monitoring, and rollback capability described above.
- Fast iteration has real business value.

It's the wrong choice, or simply not available, when:

**Regulatory or compliance requirements demand a documented approval.** Plenty of industries require that a named person authorize what goes into production, with an audit trail. This is common in financial services, healthcare, and government work. The requirement is legitimate and not something a team can engineer around.

**Releases have to coordinate with something outside engineering.** A marketing launch, a partner integration, a contractual go-live date, or a customer training schedule. When the timing of a release is a business commitment, it needs to be a business decision.

**The deployment isn't reversible in minutes.** Mobile apps go through app store review. Desktop software gets installed by customers. Firmware ships on hardware. When "roll it back" isn't a thing we can do, automatic release is not appropriate.

**The team isn't ready yet.** Thin test coverage, no monitoring, or a manual deployment process. This is the most common case by far, and the honest answer is to build those capabilities first.

For all of these, the alternative isn't abandoning automation — it's Continuous Delivery, where the pipeline does everything automatically up to production and stops for an explicit approval. That's the subject of the next topic. The distinction between the two is exactly one thing: whether a human presses the button.

---

## Key Takeaways

- Continuous Deployment automatically releases to production every change that passes the pipeline — no human approval step.
- It replaces a slow, unreliable human gate with a fast, consistent automated one; the safety moves into the pipeline.
- It requires genuine test coverage, production monitoring, fast rollback, and small changes before it's safe.
- The underlying mindset is investing in fast detection and recovery rather than trying to prevent every failure.
- Feature flags separate *deployed* (code is in production) from *released* (users can see it), which is what makes shipping unfinished work safe.
- Flags also enable gradual rollout and an instant off switch — but they must be cleaned up once a feature is stable.
- It's inappropriate where compliance demands documented approval, where releases coordinate with business events, or where rollback isn't fast.
