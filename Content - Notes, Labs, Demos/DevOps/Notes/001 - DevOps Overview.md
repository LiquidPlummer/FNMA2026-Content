# DevOps Overview

DevOps is a way of working, not a product we can buy or a person we can hire. When a job posting says "DevOps Engineer," what it usually means is "someone who builds and maintains the automation that makes DevOps practices possible." The practices themselves belong to the whole team.

The clearest definition: **DevOps is the practice of development and operations sharing responsibility for software from the first commit through to running in production.** Everything else — the tools, the pipelines, the culture talk — follows from that one idea.

---

## The Problem It Solves

To understand why DevOps exists, it helps to look at what came before it.

Traditionally, development and operations were separate departments with separate goals. Development was measured on **shipping features** — the more changes delivered, the better. Operations was measured on **stability** — the fewer outages, the better. Those two incentives point in opposite directions, because every change is a potential outage.

The result was a handoff wall. Developers finished their work, packaged it up, and threw it over to operations with a document explaining how to install it. Operations, who hadn't seen the code before and hadn't been consulted about how it should run, tried to deploy it. When it broke, the two sides argued:

> **Dev:** "It works on my machine."
> **Ops:** "Then we'll ship your machine."

That joke exists because the underlying problem was real and constant. The two groups had different environments, different priorities, and no shared incentive to make the other's job easier.

DevOps removes the wall. Instead of two teams optimizing for conflicting goals, we have one team responsible for the entire lifecycle — building it *and* keeping it running.

---

## Shared Ownership: "You Build It, You Run It"

This phrase, from Amazon's Werner Vogels, is the cultural core of DevOps.

If the team that writes the code is also on call for it at 2 AM, that team has a direct, personal stake in making the code observable, resilient, and easy to deploy. Logging stops being an afterthought. Deployment stops being someone else's problem. The feedback from operating the system flows straight back to the people who can change its design.

Compare that to the handoff model, where a developer never learns that the feature they shipped generates a nightly flood of alerts for someone in another department. The information exists, but it never reaches anyone who can act on it.

Shared ownership is what makes the rest of DevOps work. The automation is valuable, but automation without shared responsibility just makes a broken handoff happen faster.

---

## The Pipeline as the Backbone

The practical expression of DevOps is the **pipeline** — an automated sequence that takes a code change from a developer's commit all the way to running software.

```
commit → build → test → deploy
```

*The four fundamental stages every deployment pipeline is built from.*

Each stage does one job:

- **Commit** — a developer pushes a change to the shared repository. This is the trigger; everything after it is automatic.
- **Build** — the source code is compiled and packaged into something runnable.
- **Test** — automated tests run against what was just built. If they fail, the pipeline stops here.
- **Deploy** — the packaged result is installed into an environment where it can actually run.

Real pipelines add more stages — static analysis, security scanning, integration tests, promotion between environments — but they are all elaborations on these four. The rest of this unit is essentially a tour of what goes into each stage and how far we let the automation carry a change before a human intervenes.

The key property is that the pipeline is **automated and repeatable**. The same steps run the same way every time, whether it's Tuesday morning or the night before a holiday. A process that depends on someone remembering to run a script correctly is not a pipeline.

---

## Why Feedback Speed Is the Whole Point

If we had to reduce DevOps to a single measurement, it would be this: **how long does it take to find out whether a change is good?**

Consider two teams making the same mistake — a bug introduced on a Monday morning.

- **Team A** has an automated pipeline. The commit triggers a build, tests fail four minutes later, and the developer who wrote the change fixes it while it's still fresh in their mind. Cost: minutes.
- **Team B** batches changes into a quarterly release. The bug is found during a two-week manual test cycle in month three, mixed in with three hundred other changes. Nobody remembers writing it. Finding *which* change caused it is now its own investigation. Cost: days, possibly weeks.

Same bug. The difference in cost is entirely a function of how long the feedback took to arrive.

This is why nearly every DevOps practice is about shortening loops: automated tests give feedback in minutes instead of weeks, small changes make failures easy to attribute, and monitoring in production tells us how real users are affected rather than waiting for a support ticket. When the feedback loop is fast, mistakes stay cheap. When it's slow, mistakes compound.

---

## Core Vocabulary

These terms recur throughout the unit, so it's worth pinning them down now.

**Environment** — a place where the software runs. Teams typically maintain several: `dev` for active development, `test` or `QA` for verification, `staging` as a production-like rehearsal space, and `production` where real users are. The goal is for these to be as similar to each other as possible, so that "it worked in staging" actually means something.

**Artifact** — the packaged, deployable output of a build. A `.jar` file, a Docker image, a compiled bundle. The critical rule: an artifact is **built once and promoted unchanged** through the environments. If we rebuild between staging and production, we've deployed something we never tested.

**Release** — making a version of the software available to users. Note that this is not necessarily the same as deploying it, a distinction we'll return to in topic 003.

**Rollback** — returning to the previously working version after a bad deployment. The speed of our rollback determines how much risk we can afford to take; a team that can roll back in ninety seconds can be far bolder than one that needs an afternoon.

**Pipeline** — the automated sequence of stages a change passes through on its way to production.

---

## Key Takeaways

- DevOps is a set of practices and a culture of shared ownership, not a tool or a job title.
- It exists to dissolve the handoff wall created by dev and ops having conflicting incentives — speed versus stability.
- "You build it, you run it" puts operational feedback in the hands of the people who can act on it.
- The pipeline — commit, build, test, deploy — is the automated backbone that makes the practice real.
- The speed of the feedback loop is the point: fast feedback keeps mistakes cheap and easy to attribute.
- An artifact is built once and promoted unchanged; rebuilding between environments means shipping something untested.
