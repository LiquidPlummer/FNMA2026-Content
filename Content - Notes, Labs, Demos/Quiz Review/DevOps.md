# DevOps — Quiz Review

This page reviews the DevOps ideas the quiz checks: what DevOps is for, how a CI/CD pipeline is put together, and what Continuous Integration and Continuous Deployment each mean. Each section has a short summary, a few questions to test ourselves with, and links to the notes that explain the idea in full.

This is a study guide, not an answer key. The answers to the check-yourself questions are in the linked notes.

> The quiz's cloud and AWS questions (cloud computing, EC2, Security Groups, AMIs, API Gateway) are covered on the [Terraform review page](Terraform.md). The Terraform notes are the only notes in the course that work with AWS resources.

---

## What DevOps Is For

DevOps is a way of working, not a tool or a job title. The notes define it as **development and operations sharing responsibility for software from the first commit through to running in production.**

It exists to get rid of the old *handoff wall*. Development was measured on shipping changes, and operations on keeping things stable. Every change is a risk to stability, so the two goals pulled against each other. Developers threw finished work over the wall, and operations dealt with what broke. DevOps replaces that with one team that builds the software *and* keeps it running: "you build it, you run it."

The measure that matters most is **feedback speed**: how long it takes to find out whether a change is good. Almost every DevOps practice exists to shorten that loop. That includes automated tests, small changes, and monitoring in production. A mistake caught in minutes is cheap to fix. The same mistake found months later is expensive.

**Check yourself**
- Why did giving dev and ops separate goals produce a wall instead of a healthy balance?
- In one sentence, what is DevOps trying to achieve, and who is responsible for achieving it?
- Two teams ship the same bug. One finds it in four minutes, the other in three months. What makes the second case so much more expensive?

**Go deeper**
- DevOps Overview: [The Problem It Solves](../DevOps/Notes/001%20-%20DevOps%20Overview.md#the-problem-it-solves), [Shared Ownership](../DevOps/Notes/001%20-%20DevOps%20Overview.md#shared-ownership-you-build-it-you-run-it), [Why Feedback Speed Is the Whole Point](../DevOps/Notes/001%20-%20DevOps%20Overview.md#why-feedback-speed-is-the-whole-point)
- DevOps and Agile: [Two Halves of One Pipeline](../DevOps/Notes/005%20-%20DevOps%20and%20Agile.md#two-halves-of-one-pipeline)

---

## The Pipeline: From Commit to Running Software

A **pipeline** is the automated sequence a code change passes through on its way to production. Its purpose is to make the trip from a developer's commit to running software **automated and repeatable**. The same steps run the same way every time, and we find out quickly when something fails. A process that depends on someone remembering to run a script isn't a pipeline.

Every pipeline is built from four basic stages. They're listed here in **alphabetical order, not pipeline order**:

| Stage | Its job |
|---|---|
| Build | Compile the source code and package it into something runnable |
| Commit | A developer pushes a change to the shared repository. This triggers everything else |
| Deploy | Install the packaged result in an environment where it can run |
| Test | Run automated tests against what was just built. A failure stops the pipeline here |

Each stage needs what the one before it produced, so the order can be worked out from the "Its job" column alone. Work it out, then check it against the diagram in the notes.

Real pipelines add more stages, such as static analysis, security scans, integration tests, and promotion through test and staging environments. All of them are elaborations on the basic four. Two rules hold throughout:

- **Fail fast and fail cheap.** Quick checks run first, so most failures show up within minutes.
- **Build once, promote the same artifact.** The package that was tested is exactly the package that reaches production. Differences between environments come from configuration, not from rebuilding.

**Check yourself**
- Put the four stages in order, and say what each one hands to the next.
- What makes a pipeline different from a deployment script that someone runs by hand?
- Why is rebuilding the artifact for production a problem, even when the build steps are identical?

**Go deeper**
- DevOps Overview: [The Pipeline as the Backbone](../DevOps/Notes/001%20-%20DevOps%20Overview.md#the-pipeline-as-the-backbone), [Core Vocabulary](../DevOps/Notes/001%20-%20DevOps%20Overview.md#core-vocabulary)
- Continuous Delivery: [Every Build Produces a Deployable Artifact](../DevOps/Notes/004%20-%20Continuous%20Delivery.md#every-build-produces-a-deployable-artifact), [The Deployment Pipeline](../DevOps/Notes/004%20-%20Continuous%20Delivery.md#the-deployment-pipeline)
- Jenkins: [A Fuller Pipeline](../DevOps/Notes/007%20-%20Jenkins.md#a-fuller-pipeline) shows a complete pipeline written as code

---

## Continuous Integration

**Continuous Integration (CI)** means merging every developer's work into a shared mainline (usually `main`) frequently, at least once a day. An automated build and test suite checks every merge.

"Integration" means combining everyone's work into one codebase. Done rarely, it's painful: after weeks apart, changes collide in ways nobody can easily untangle, which teams used to call "merge hell." Done continuously, integration stops being an event at all. The practice rests on a few disciplines:

- **Small, frequent merges.** Branches live for hours or a couple of days, not weeks.
- **An automated build on every commit**, from a clean checkout. This catches "works on my machine" problems.
- **Automated tests as a pass/fail gate.** To work as a gate, tests have to be fast, reliable, and meaningful.
- **A broken mainline build is the team's top priority.** That keeps the mainline releasable at all times.

Everything that comes after CI builds on it. The notes list the steps a CI server works through. Everything up to and including static analysis is CI. Packaging the artifact is where Continuous Delivery begins.

**Check yourself**
- What is integration debt, and why does it grow worse than linearly the longer developers work apart?
- A feature branch has had a green build for three weeks. Why doesn't that tell us much?
- Why does CI insist on building from a clean checkout rather than on a developer's machine?

**Go deeper**
- Continuous Integration: [The Problem: Integration Debt](../DevOps/Notes/002%20-%20Continuous%20Integration.md#the-problem-integration-debt), [A Broken Build Is the Team's Top Priority](../DevOps/Notes/002%20-%20Continuous%20Integration.md#a-broken-build-is-the-teams-top-priority), [Why Long-Lived Feature Branches Undermine CI](../DevOps/Notes/002%20-%20Continuous%20Integration.md#why-long-lived-feature-branches-undermine-ci), [What a CI Server Actually Does](../DevOps/Notes/002%20-%20Continuous%20Integration.md#what-a-ci-server-actually-does)

---

## Continuous Deployment vs. Continuous Delivery

Both practices are abbreviated **CD**, which is why they get confused. They share the same machinery: a pipeline that's automated through every stage before production, where every successful build produces an artifact that could be released. They differ in exactly one respect: **whether a human approves the final step into production.**

- **Continuous Delivery** keeps the software releasable at all times. The pipeline stops at the door to production and waits for someone to approve. That approval decides *timing and business readiness*, not quality, because the automated stages have already settled quality.
- **Continuous Deployment** removes that stop. Every change that passes the pipeline goes live without anyone signing off. That's only safe once a team has:
  - test coverage good enough to be the only gate,
  - monitoring that catches problems in production quickly,
  - fast, reliable rollback,
  - small changes, so a problem points to an obvious cause.

**Feature flags** are what make Continuous Deployment workable. They separate *deployed* (the code is running in production) from *released* (users can see it).

In a Jenkins pipeline, the whole difference can come down to one `input` step. Remove it, and a Delivery pipeline becomes a Deployment pipeline.

**Check yourself**
- A bank's compliance rules require a named person to approve every production release. Which kind of CD can it practice, and why?
- Name the capabilities a team needs before releasing to production automatically is safe. What goes wrong without each one?
- How can unfinished code be running in production without users seeing it?

**Go deeper**
- Continuous Deployment: [What "Automatically" Actually Means](../DevOps/Notes/003%20-%20Continuous%20Deployment.md#what-automatically-actually-means), [What It Requires Before It's Safe](../DevOps/Notes/003%20-%20Continuous%20Deployment.md#what-it-requires-before-its-safe), [Feature Flags](../DevOps/Notes/003%20-%20Continuous%20Deployment.md#feature-flags-separating-deployed-from-released), [When It's Appropriate](../DevOps/Notes/003%20-%20Continuous%20Deployment.md#when-its-appropriate--and-when-it-isnt)
- Continuous Delivery: [The Manual Approval Gate](../DevOps/Notes/004%20-%20Continuous%20Delivery.md#the-manual-approval-gate)

---

## More Practice

[DevOps Reading Questions](../DevOps/Review/Reading%20Questions.md) has longer questions on every topic in the unit, including SonarCloud and Jenkins.
