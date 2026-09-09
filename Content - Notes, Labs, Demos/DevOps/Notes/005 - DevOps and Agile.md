# DevOps and Agile

Agile and DevOps get mentioned together constantly, often as if they were the same initiative or competing alternatives. They're neither. The cleanest way to hold the relationship:

> **Agile changed how work is planned. DevOps changed how it ships.**

They address adjacent halves of the same problem, they share the same underlying principles, and each is substantially weakened without the other.

---

## Two Halves of One Pipeline

Agile emerged around 2001 as a reaction to long, plan-heavy development cycles. Its target was the *front* of the process: how requirements are gathered, how work is prioritized, how teams organize, and how often we check in with the customer. Instead of specifying a year of work up front, an Agile team works in short iterations, demonstrates working software frequently, and adjusts based on what it learns.

That was a genuine improvement, and it exposed a new bottleneck.

An Agile team can finish a two-week sprint with working, demonstrated, accepted software — and that software can still sit for three months waiting for the quarterly release. The planning process got fast. The delivery process didn't.

DevOps targets the *back* of the process: build, test, deploy, operate. It picks up exactly where Agile's scope ends.

```
├──────────── Agile ────────────┤├──────────── DevOps ─────────────┤
 plan → design → build → test  →  integrate → release → operate → monitor
                                                                    │
        └───────────────── feedback ─────────────────────────────────┘
```

*Agile covers planning through development; DevOps covers integration through operation — and the monitoring feedback loops back into the next round of planning.*

Note where the feedback arrow lands. Production monitoring feeds the planning process — which is the point at which the two practices stop being adjacent and start being a single loop.

---

## Agile Without DevOps

This is the failure mode worth recognizing, because it's extremely common and teams in it often believe they're doing fine.

The symptoms:

- Sprints complete on schedule. Velocity is stable. Burndown charts look healthy.
- Sprint reviews demo working software every two weeks.
- Stories are marked Done and moved to the Done column.
- **Users have seen none of it**, because releases happen quarterly.

The team is genuinely doing Agile ceremonies correctly and still not delivering value, because "Done" and "in production" are twelve weeks apart.

Consider what that gap actually costs:

**Feedback is invalidated.** Agile's core bet is that we learn from real usage and adjust. If usage feedback arrives three months after the work, the team has built ten more sprints on top of assumptions it now discovers were wrong.

**Batch size grows.** Twelve weeks of accumulated changes release together. That's a large, risky deployment where problems are hard to attribute — exactly the situation Agile's small increments were meant to avoid, reintroduced at the release boundary.

**Finished work sits as inventory.** Completed features that aren't in production earn nothing. They're capital tied up, depreciating, and carrying risk.

There's a memorable way to put this: **a team practicing Agile without DevOps has built a fast car and left it in the garage.** The iteration speed is real; it just doesn't reach anyone.

The reverse case is worth a mention too, though it's rarer: DevOps without Agile means excellent automation for shipping work that was planned in big up-front batches. We can deploy in four minutes — a feature that was specified eleven months ago against requirements that have since changed.

---

## Shared Principles

The two practices rest on the same foundations, which is why they fit together so naturally.

**Small batches.** Agile splits requirements into small stories; DevOps splits deployments into small changes. Both are betting on the same thing: small units are easier to understand, faster to verify, and cheaper to fix when wrong. A big-bang release contradicts Agile's philosophy just as much as a big-bang requirements document does.

**Fast feedback.** Agile shortens the loop between building something and hearing from stakeholders. DevOps shortens the loop between writing code and knowing if it works — in the test suite, and then in production. Same instinct, different loops.

**Cross-functional teams.** Agile broke down the wall between business analysts, developers, and testers by putting them on one team with one goal. DevOps applies the identical move to the wall between development and operations. Both are the same argument: handoffs between specialized silos lose information and create conflicting incentives.

**Responding to change.** Agile welcomes changing requirements. DevOps is what makes acting on them cheap — a team that can deploy in minutes can change direction in a way a team with quarterly releases simply cannot, no matter how welcoming its process is.

**Working software as the measure of progress.** This is Agile's principle, but DevOps is what makes it honest. If "working software" means working on a developer's machine, the measure is soft. If it means running in production and serving users, it's real.

---

## Where Their Scope Differs

Being clear about the boundaries prevents the common mistake of treating one as a subset of the other.

**Agile owns:**
- How requirements are captured and prioritized (backlogs, stories, refinement)
- How the team organizes and plans (sprints, standups, retrospectives)
- Collaboration with the customer and stakeholders
- Adapting scope and direction as understanding improves

**DevOps owns:**
- Build and test automation
- Deployment pipelines and release mechanics
- Infrastructure, environments, and configuration
- Production monitoring, observability, and incident response
- The operational feedback returning to the team

**Neither one owns** — and this matters — the parts that only work when they're joined: whether finished work actually reaches users, and whether what we learn from production reaches the next planning cycle. Those live in the seam between them, which is why the two need to be adopted together.

One structural difference worth noting: Agile is largely a **team-level** practice, while DevOps is often **organization-level**. A single team can adopt Scrum on its own. That same team usually cannot adopt DevOps alone — shared infrastructure, deployment authority, and security or compliance policy typically involve other groups. This is a big part of why DevOps adoption tends to be slower and more political than Agile adoption.

---

## Extending the Definition of Done

The **Definition of Done** is Agile's shared agreement about what has to be true before a story counts as complete. It's also the most practical single place to connect the two practices, because it's where DevOps expectations become team policy rather than aspiration.

A typical pre-DevOps Definition of Done:

- [x] Code written
- [x] Unit tests written and passing
- [x] Code reviewed
- [x] Merged to mainline

That's a developer-centric definition. Everything on it can be true while the feature does nothing for anyone.

Extended for DevOps:

- [x] Code written
- [x] Unit tests written and passing
- [x] Code reviewed
- [x] Merged to mainline
- [x] **Pipeline green — all automated stages passed**
- [x] **Quality gate passed** (topic 006)
- [x] **Deployed to production**
- [x] **Monitoring and alerting in place for the new behavior**
- [x] **Verified working in production**

*Extending Done through to production makes deployability the team's responsibility rather than a separate department's.*

The added items change behavior in specific ways. If "deployed to production" is required for Done, then a story can't be closed by tossing it over a wall — the team owns getting it there. If "monitoring in place" is required, observability gets designed in rather than retrofitted after the first incident. And if a story can't be finished in a sprint because deployment takes six weeks, that's no longer an invisible external constraint — it shows up as unfinished work in the team's own sprint, where it becomes impossible to ignore.

That last effect is the useful one. It converts a vague organizational complaint ("releases take too long") into a visible, measured impediment that the retrospective has to deal with.

---

## Key Takeaways

- Agile changed how work is planned; DevOps changed how it ships. They cover adjacent halves of the same pipeline.
- Agile without DevOps produces finished work that sits undeployed — a fast car left in the garage.
- Both rest on the same principles: small batches, fast feedback, cross-functional teams, and responding to change.
- Agile owns planning, prioritization, and team process; DevOps owns build, deployment, infrastructure, and operations.
- Agile is typically a team-level practice; DevOps usually requires organizational change, which is why it's harder to adopt.
- Extending the Definition of Done through "running in production" is the most practical way to join the two.
- That extension makes slow deployment visible as unfinished work inside the sprint, where the team must confront it.
