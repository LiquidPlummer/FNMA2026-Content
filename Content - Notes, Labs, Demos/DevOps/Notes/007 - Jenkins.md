# Jenkins

Everything in this unit so far has described what a pipeline *does*. Jenkins is one of the tools that actually runs it.

**Jenkins is a self-hosted, open-source automation server.** It watches our repository, reacts to commits, and executes the stages we define — build, test, analyze, deploy. It's been around since 2011 (as a fork of Hudson, which dates to 2004), which makes it old by software standards. That age is the source of both its main strengths and its main frustrations: an enormous plugin ecosystem and a great deal of accumulated institutional knowledge, alongside a dated interface and real maintenance overhead.

"Self-hosted" is the defining characteristic. Unlike GitHub Actions or SonarCloud, Jenkins runs on our own servers. We install it, patch it, back it up, and manage its plugins. In exchange we get complete control — it can reach internal systems behind the firewall, and our source never leaves our infrastructure. That tradeoff is exactly why Jenkins remains common in enterprises and regulated industries even as hosted alternatives have grown.

---

## Where Jenkins Sits

Jenkins is the hub connecting the pieces we've already covered:

```
   Developer                                              Environments
      │                                                   ┌───────────┐
      │ push                                         ┌───→ │    Dev    │
      ↓                                              │    ├───────────┤
 ┌──────────┐   webhook   ┌─────────┐   deploy      ├───→ │  Staging  │
 │   Repo   │ ──────────→ │ JENKINS │ ──────────────┤    ├───────────┤
 │  (Git)   │ ←────────── │         │                └───→ │Production │
 └──────────┘   status    └─────────┘                     └───────────┘
                            │    ↑
                     analyze│    │verdict          ┌──────────────┐
                            ↓    │                 │  Artifact    │
                       ┌──────────────┐    push    │  Repository  │
                       │  SonarCloud  │  ←─────────┤              │
                       └──────────────┘            └──────────────┘
```

*Jenkins sits between the repository and the environments, coordinating analysis, artifact storage, and deployment while reporting status back to the repo.*

It's a coordinator more than a worker. Jenkins rarely compiles anything itself — it invokes Gradle or Maven to do that, calls SonarCloud for analysis, and runs deployment scripts. Its job is deciding *what runs, where, in what order, and what happens when something fails*.

---

## Jobs and Pipelines

A **job** (or **project**) is a configured unit of work in Jenkins. There are a few kinds, and the distinction matters historically.

**Freestyle jobs** are the old approach: configured entirely through the web UI by filling in forms and checking boxes. They work, and they're still around in older installations, but they have a serious problem — the configuration lives only inside Jenkins. It isn't in version control, so we can't see its history, review changes to it, or reproduce it if the server is lost. Someone edits a box in a form, the build changes, and there's no record of who or why.

**Pipeline jobs** are the modern approach, and the one to learn. The build definition is written as code in a file called `Jenkinsfile`, and that file lives **in the repository alongside the source code**.

This is the single most important idea in modern Jenkins, and it follows directly from principles we've already established:

- The pipeline is **version controlled** — full history of every change to how the project builds.
- It's **reviewed** — pipeline changes go through pull requests like any other code.
- It's **branch-aware** — a branch can modify its own pipeline, and the change merges to the mainline along with the code that needed it.
- It's **reproducible** — a fresh Jenkins server can rebuild the project by reading the repository. No server state to restore.

The `Jenkinsfile` sits at the repository root, and Jenkins reads it on every run.

---

## Pipeline Structure: Stages and Steps

Jenkins pipelines are written in a Groovy-based DSL. Declarative syntax is the recommended form — more structured, more readable, and harder to get wrong than the older scripted syntax.

The essential skeleton:

```groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Package') {
            steps {
                sh './gradlew bootJar'
                archiveArtifacts artifacts: 'build/libs/*.jar'
            }
        }
    }
}
```

*A minimal declarative pipeline: three stages running in sequence, each containing the shell commands that do the work.*

The hierarchy is worth stating plainly:

- **`pipeline`** — wraps the whole definition.
- **`agent`** — where the work executes (covered below).
- **`stages`** — the container for all stages.
- **`stage`** — a named phase, shown as a column in the UI. Stages run in order, and a failing stage stops the pipeline.
- **`steps`** — the individual commands inside a stage. `sh` runs a shell command on Unix agents; `bat` is the Windows equivalent.

Stages are the unit of visibility. Naming them well is what makes a failed build diagnosable at a glance — `Unit Tests` and `Integration Tests` as separate stages immediately tell us which kind of failure we have, where a single `Test` stage would not.

### A Fuller Pipeline

Adding the pieces from earlier topics — quality gates and a deployment approval:

```groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps { sh './gradlew clean build -x test' }
        }

        stage('Unit Tests') {
            steps { sh './gradlew test' }
            post {
                always { junit 'build/test-results/test/*.xml' }
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    sh './gradlew sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy to Staging') {
            steps { sh './deploy.sh staging' }
        }

        stage('Deploy to Production') {
            when { branch 'main' }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                sh './deploy.sh production'
            }
        }
    }

    post {
        failure {
            mail to: 'team@example.com',
                 subject: "Build failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "See ${env.BUILD_URL}"
        }
    }
}
```

*A complete Continuous Delivery pipeline — automated through staging, with `input` providing the manual approval gate before production.*

Three things in there are worth calling out:

- **`post`** blocks run after a stage or the whole pipeline, with conditions like `always`, `success`, `failure`, and `unstable`. Publishing test results belongs in `always`, since we want the report especially when tests failed.
- **`when`** conditionally runs a stage. Here, production deployment only happens on the mainline branch — feature branches run everything else but can't deploy.
- **`input`** pauses the pipeline and waits for a human to click. This is the manual approval gate from topic 004, expressed in about one line. Removing it turns this into a Continuous Deployment pipeline.

---

## Agents: Where the Work Executes

An **agent** (historically called a "node" or "slave") is a machine that executes pipeline steps. Jenkins uses a **controller/agent** architecture:

- The **controller** is the Jenkins server itself. It hosts the web UI, stores configuration and build history, schedules work, and coordinates everything — but it should not run builds.
- **Agents** are separate machines connected to the controller. They check out code, compile, run tests, and do the actual work.

The separation exists for good reasons. Builds are resource-hungry and can crash a machine; keeping them off the controller protects the server everyone depends on. Builds also run arbitrary code from the repository, and we don't want that executing on the box holding all our credentials. Practically, agents also let us scale horizontally and match environments — a Windows agent for a .NET build, a Linux agent with a specific JDK for a Java build.

Declaring an agent:

```groovy
agent any                          // any available agent
agent { label 'linux && jdk17' }   // an agent with these labels
agent { docker { image 'gradle:8-jdk17' } }   // a fresh container
agent none                         // none at the top; each stage declares its own
```

*Agent declarations, from least to most specific — labels select machines with particular capabilities, and Docker agents give each build a clean, reproducible environment.*

The Docker form is worth noting because it solves an old and genuinely annoying problem. Long-lived agents accumulate state — leftover files, cached dependencies, a JDK someone upgraded — and builds start behaving differently depending on which agent they land on. A container agent starts identical every time, which is exactly the clean-checkout principle from topic 002 extended to the whole build environment.

---

## Build Triggers

A **trigger** is what causes a pipeline to run. There are three that matter.

**Commit hooks (webhooks).** The repository notifies Jenkins the moment a push happens, and the build starts within seconds. This is the right default — it's immediate and creates no wasted load. It requires the repository to be able to reach Jenkins over the network, which is the usual complication when Jenkins lives behind a corporate firewall.

**Polling.** Jenkins asks the repository "anything new?" on a schedule. It's the fallback when webhooks aren't possible. The cost is that feedback is delayed by up to the polling interval, and Jenkins is making constant requests whether or not anything changed.

```groovy
triggers { pollSCM('H/5 * * * *') }
```

*Poll the repository roughly every five minutes; `H` spreads the load so all jobs don't fire at the same instant.*

**Scheduled.** Run at a fixed time regardless of activity. Used for nightly builds — long-running test suites, security scans, or performance tests that are too slow to run on every commit.

```groovy
triggers { cron('H 2 * * *') }
```

*Run once nightly at around 2 AM, whether or not anything has changed.*

**Manual** is the fourth case, though it isn't configured: any job can be started by clicking "Build Now," and pipelines with an `input` step are effectively waiting on a manual trigger to continue.

Nightly builds pair naturally with commit-triggered ones. Fast tests run on every commit for immediate feedback; the slow, thorough suite runs overnight. That's the same layering principle from the deployment pipeline in topic 004, applied to scheduling.

---

## Reading the Output

Most time spent with Jenkins is spent figuring out why a build failed. Knowing where to look is the practical skill.

**The stage view** is the first stop — a grid on the job page with stages as columns and recent builds as rows, each cell colored by result. It answers the first question immediately: *which stage failed?* That alone usually narrows the problem enormously. A failure in `Unit Tests` is a different investigation from a failure in `Deploy to Staging`.

**The console log** is the full, unfiltered output of the run — every command and everything it printed. It's the authoritative source and it's often thousands of lines long, which is why people find it intimidating.

A reliable method for reading it:

1. **Go to the end first.** The failure is at or near the bottom, since the pipeline stopped there. Don't read from the top.
2. **Find the `ERROR` or `FAILURE` line.** Jenkins marks the point where the build was aborted.
3. **Read *upward* from that point.** The Jenkins error is usually generic ("script returned exit code 1"). The real cause — a compiler error, a failed assertion, a missing dependency — is in the output of the tool that ran, above it.
4. **Distinguish the tool's failure from Jenkins' failure.** Gradle reporting `3 tests failed` is the actual information; Jenkins reporting a nonzero exit code is just the messenger.
5. **Check the test report if tests failed.** The published JUnit results give a much more readable view than raw console output — which test, what was expected, what was received.

Blue Ocean, an alternative Jenkins UI, presents this more pleasantly: a visual pipeline diagram where clicking the failed stage jumps straight to the relevant log section. If it's installed, it's usually the faster path.

One habit worth building: **read the log before re-running the build.** Re-running is tempting and occasionally works — which is precisely the problem, because a build that fails intermittently is a flaky test, and flaky tests quietly destroy the value of the gate (topic 002). A failure that "went away" is information, not a resolution.

---

## Where SonarCloud Fits

SonarCloud slots into the pipeline as its own stage, as seen in the fuller example above. The interaction has a subtlety worth understanding.

Analysis is **asynchronous**. Jenkins doesn't run the analysis itself — it submits the code to SonarCloud, which processes it on its own servers and takes anywhere from seconds to minutes depending on project size. So the integration is two stages:

1. **`SonarCloud Analysis`** — `withSonarQubeEnv` supplies the server URL and authentication token from Jenkins' credential store, then the build tool (`./gradlew sonar`) uploads the analysis. This stage finishes as soon as the upload completes.
2. **`Quality Gate`** — `waitForQualityGate` pauses and waits for SonarCloud to call back with its verdict. `abortPipeline: true` fails the build if the gate didn't pass, and the `timeout` wrapper prevents the pipeline from hanging forever if the callback never arrives.

Placement matters. The analysis stage goes **after** tests, because SonarCloud consumes the coverage report the test run produces — running it earlier means reporting zero coverage. And the quality gate goes **before** any deployment stage, since the entire purpose is to stop bad code from advancing.

The result is the enforcement mechanism described in topic 006: a static analysis verdict with the same authority as a failing test, applied automatically to every change.

---

## Key Takeaways

- Jenkins is a self-hosted automation server that watches the repository and executes the pipeline; self-hosting is its defining tradeoff — full control and internal network access, in exchange for maintaining it.
- It coordinates rather than works, invoking build tools, analysis services, and deployment scripts.
- Pipeline jobs defined in a `Jenkinsfile` in the repository are the modern approach — version controlled, reviewable, branch-aware, and reproducible, unlike UI-configured freestyle jobs.
- The structure is `pipeline` → `stages` → `stage` → `steps`; stages run in order, and a failing stage stops the pipeline.
- `post` handles cleanup and notification, `when` makes stages conditional, and `input` implements the manual approval gate that distinguishes Continuous Delivery from Continuous Deployment.
- Agents are the machines where work executes, kept separate from the controller for stability, security, and scale; container agents give every build a clean environment.
- Webhooks are the preferred trigger, polling the fallback, and scheduled builds handle slow suites overnight.
- To diagnose a failure: check the stage view for *which* stage, then read the console log from the bottom upward — and read it before re-running.
- SonarCloud integrates as two stages — submit the analysis after tests, then wait for the gate verdict before deploying.
