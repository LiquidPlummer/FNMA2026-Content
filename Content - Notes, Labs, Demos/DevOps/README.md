# DevOps — Unit Outline

A single unit, six topics. Placement under a parent subject is TBD. Bullets under each topic are what that topic's `Notes/` should cover.

---

## 001 - DevOps Overview
- What DevOps is: a set of practices and a culture, not a tool or a job title
- The problem it solves — the handoff wall between development and operations
- The pipeline as the backbone: commit → build → test → deploy
- Shared ownership: "you build it, you run it"
- Why the speed of the feedback loop is the whole point
- Core vocabulary: environment, artifact, release, rollback, pipeline

## 002 - Continuous Integration
- Merging to a shared mainline frequently, in small increments
- An automated build triggered on every commit
- Automated tests as the gate that decides pass or fail
- A broken build is the team's top priority; mainline stays releasable
- Why long-lived feature branches undermine CI
- What a CI server actually does, start to finish

## 003 - Continuous Deployment
- Every change that passes the pipeline goes to production automatically
- What it requires before it's safe: test coverage, monitoring, fast rollback
- Feature flags to separate "deployed" from "released"
- When it's appropriate, and when a business needs the gate

## 004 - Continuous Delivery
- Every successful build produces a deployable artifact
- Automated promotion through environments up to pre-production
- Releasing becomes a business decision rather than a technical event
- The manual approval gate as the defining difference from deployment
- The deployment pipeline: stages, promotion, and what fails a stage

## 005 - DevOps and Agile
- How they relate: Agile changed how work is planned, DevOps changed how it ships
- Agile without DevOps — finished work that sits undeployed
- Shared principles: small batches, fast feedback, cross-functional teams
- Where their scope differs
- Extending the Definition of Done to "running in production"

## 006 - SonarCloud & SonarLint
- Static analysis: finding problems without running the code
- SonarLint in the IDE — feedback while the code is being written
- SonarCloud in the pipeline — feedback on the pull request and the build
- What gets measured: bugs, vulnerabilities, code smells, duplication, coverage
- Quality gates, and using one to fail a build
- Technical debt as something measurable rather than a feeling

## 007 - Jenkins
- What Jenkins is: a self-hosted automation server that runs the pipeline
- Where it sits — watching the repository, reacting to commits
- Jobs and pipelines; the `Jenkinsfile` living in the repo alongside the code
- Pipeline structure: stages and steps
- Agents — where the work actually executes
- Build triggers: commit hooks, scheduled, manual
- Reading the output: console log, stage view, and finding the failing step
- Where SonarCloud fits as a pipeline stage