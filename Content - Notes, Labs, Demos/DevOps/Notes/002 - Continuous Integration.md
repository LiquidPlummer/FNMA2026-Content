# Continuous Integration

**Continuous Integration (CI)** is the practice of merging every developer's work into a shared mainline frequently — at least daily — with an automated build and test suite verifying each merge.

The name describes the problem it solves. "Integration" is the act of combining separate people's work into one coherent codebase. Done rarely, integration is painful and unpredictable. Done continuously, it stops being an event at all.

---

## The Problem: Integration Debt

Imagine four developers each working in isolation for three weeks. Every day they work, their copy of the code drifts a little further from everyone else's. On day one the differences are trivial. By week three, one person has renamed a class three other people are calling, another has restructured a module, and a third has changed a method signature used across the codebase.

Now they all merge on the same afternoon. This is what teams used to call "merge hell" or "integration week" — and it was often scheduled, because everyone knew it would take days.

The costs stack up in an unpleasant way:

- **Conflicts scale worse than linearly.** Twice the time apart is more than twice the conflict, because changes interact with each other.
- **The person resolving a conflict often didn't write either side of it,** and has to reconstruct the intent of both.
- **Nobody knows if it works until the very end.** Three weeks of work is validated for the first time on the day it's all combined.

CI attacks this directly: if we integrate every few hours, the divergence never grows large enough to be dangerous. A conflict in a change from this morning is a two-minute conversation. A conflict in a change from last month is an archaeology project.

---

## Merging Small and Often

The first discipline of CI is **small, frequent merges to a shared mainline**.

"Mainline" is the branch everyone integrates into — usually `main`. Every developer pulls from it and pushes to it regularly, so it always reflects the combined current state of the team's work.

"Frequently" has a concrete floor: **at least once per day, per developer.** If a developer goes a full day without integrating, their work has been invisible to everyone else for a full day.

This forces a habit that feels unnatural at first — breaking work into pieces small enough to integrate safely while still incomplete. A large feature becomes a sequence of small, individually safe changes: add the data model, add the service method, wire in the endpoint, enable the UI. Each one merges cleanly and leaves the mainline working, even though the feature isn't finished. (The technique for keeping unfinished work invisible to users while it's merged — feature flags — comes up in topic 003.)

---

## The Automated Build

The second discipline: **every commit to the mainline triggers an automated build.**

Automatic is the operative word. Nobody clicks a button, and nobody can forget. When a commit lands, a CI server notices and starts working within seconds.

The build compiles the code from a clean checkout — not from anyone's local machine, with its accumulated local configuration and stale artifacts. This is precisely what catches the "works on my machine" class of failure: a file that was never committed, or a dependency installed locally three months ago and never added to the build configuration. The clean build has none of that, so it fails immediately.

---

## Automated Tests as the Gate

A build that only compiles tells us the code is syntactically valid. That's a low bar. The real gate is the **automated test suite**.

Tests run as part of the build, and their result is binary: the build **passes** or it **fails**. There is no "passed with warnings" and no human interpreting the results. This is what makes CI a gate rather than a report.

For that gate to be useful, the test suite has to be:

- **Fast.** The commit-stage suite should finish in minutes. If it takes an hour, developers stop waiting for it, and feedback that arrives after we've moved on to the next task has lost most of its value.
- **Reliable.** A test that fails intermittently for no reason ("flaky") is worse than no test. Once the team learns to shrug at red builds, the gate is gone even though the automation still runs.
- **Meaningful.** Tests that pass regardless of whether the code works provide false confidence.

Speed and thoroughness pull against each other, which is why pipelines are typically layered: fast unit tests on every commit, slower integration and end-to-end tests in later stages. We'll see that layering in topic 004.

---

## A Broken Build Is the Team's Top Priority

This is the rule that decides whether a team is genuinely doing CI or just running a build server.

**When the mainline build breaks, fixing it takes precedence over new work.**

The reasoning is straightforward. The whole value of CI is knowing the mainline is in a working state. While it's broken:

- Nobody else can get a clean build, so everyone's feedback is now unreliable.
- Anyone who pulls the broken code inherits the problem.
- New commits pile on top of the failure, making it harder to identify what caused it.

In practice, teams handle this a few ways: fix forward if the cause is obvious and the fix is small, or revert the offending commit and let its author sort it out on their own branch. Reverting is not a judgment on the developer — it's just restoring the shared resource quickly. What we don't do is leave it red and keep committing.

The corollary is the standard we're holding the mainline to: **it stays releasable.** At any moment, the code on the mainline should be in a state we could build, deploy, and ship. That property is what everything in topics 003 and 004 is built on.

---

## Why Long-Lived Feature Branches Undermine CI

Branching itself is fine — most teams use short-lived branches with pull requests. The problem is *duration*.

A branch that lives for weeks is, by definition, work that is not being integrated. We can run a build on that branch and the build can be green, but that only tells us the branch works *in isolation*. It says nothing about whether it works combined with the four other branches that also haven't merged. The whole risk CI exists to eliminate has simply been relocated to the eventual merge.

The tell is a branch that needs to pull from the mainline repeatedly to "stay current" and gets more painful each time. That pain is integration debt accruing in real time.

A useful rule of thumb: a feature branch should live for **hours to a couple of days**, not weeks. If a piece of work can't be finished in that window, the fix is to slice the work smaller, not to extend the branch.

---

## What a CI Server Actually Does

The CI server is the piece of software that watches the repository and runs the pipeline. Jenkins is one such server, and we'll look at it in detail in topic 007 — but the sequence is the same regardless of tool.

1. **Detect the change.** Either the repository pushes a notification (a webhook) when a commit lands, or the server polls the repository on an interval. A webhook is faster and creates less load.
2. **Check out a clean copy.** Fresh from the repository, at the exact commit that triggered the run. No leftover state from the previous build.
3. **Resolve dependencies.** Download the libraries the project declares it needs.
4. **Compile.** Turn source into something executable. A compilation error stops the run here.
5. **Run the tests.** Execute the automated suite and collect the results.
6. **Run additional checks.** Static analysis and quality gates typically slot in around here — that's where SonarCloud lives, in topic 006.
7. **Package the artifact.** If everything passed, produce the deployable output — a `.jar`, a Docker image, a bundle.
8. **Report the result.** Update the commit status in the repository, notify the team on failure, and publish logs and test reports so the failure can be diagnosed.

Steps 1 through 6 are Continuous Integration. Step 7 is where Continuous Delivery begins, and everything past it is the subject of the next two topics.

---

## Key Takeaways

- CI is frequent merging to a shared mainline — at least daily — verified by an automated build and test suite.
- It exists to prevent integration debt: divergence between developers' work grows nonlinearly with time apart.
- Every commit triggers an automated build from a clean checkout, which catches "works on my machine" failures.
- Automated tests are the pass/fail gate; they must be fast, reliable, and meaningful to function as one.
- A broken mainline build is the team's top priority — fix forward or revert, but don't build on top of red.
- Long-lived feature branches defeat CI by deferring integration risk rather than eliminating it.
- The mainline staying releasable is the foundation that Continuous Delivery and Deployment are built on.
