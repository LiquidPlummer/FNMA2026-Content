# SonarCloud & SonarLint

Automated tests answer one question: *does the code do what it's supposed to do?* That's essential, and it's not the only question worth asking. Code can pass every test and still be riddled with security holes, duplicated in six places, and incomprehensible to whoever maintains it next.

**Static analysis** answers the second question — *is the code any good?* — and the Sonar tools are a common way to get that answer automatically, both while writing code and in the pipeline.

---

## Static Analysis: Finding Problems Without Running the Code

**Static analysis** examines source code without executing it. The analyzer parses the code into a structural model and then applies a large library of rules looking for known problem patterns.

The contrast with testing is the useful framing:

| | Automated Tests | Static Analysis |
|---|---|---|
| Runs the code? | Yes | No |
| Finds | Behavior that's wrong | Patterns known to be risky |
| Coverage | Only the paths the tests exercise | Every line, every time |
| Answers | "Does it work?" | "Is it well-built and safe?" |

They complement each other and neither substitutes for the other. A test suite can only find bugs on paths it actually runs; static analysis reads every line, including the error branch nobody tested. Conversely, static analysis has no idea what the code is *supposed* to do — it can tell us a null dereference is possible, but not that we calculated interest incorrectly.

Consider a concrete example:

```java
public String describe(User user) {
    String name = user.getName();
    if (user != null) {
        return "User: " + name;
    }
    return "Unknown";
}
```

*A null check that happens after the value has already been dereferenced — the analyzer flags this without ever running the method.*

Every test might pass, because no test happens to hand this method a null. The analyzer catches it immediately, because the contradiction is visible in the structure: we clearly believe `user` can be null (we check it), and we dereferenced it before checking.

---

## SonarLint — Feedback While Writing

**SonarLint** is an IDE plugin. It runs the analysis locally, on the file open in the editor, as we type — surfacing issues as squiggly underlines the same way a spell-checker does.

The value here is entirely about *when* the feedback arrives. There's a well-known progression in what it costs to fix a problem:

```
in the editor    →  seconds       (the code is in our head right now)
in code review   →  minutes       (context switch back, discussion, rework)
in the pipeline  →  tens of min   (build fails, investigate, fix, re-run)
in production    →  hours+        (incident, hotfix, deploy under pressure)
```

*The same defect costs radically more the later it's caught — which is why analysis in the editor is the highest-leverage placement.*

Catching an issue in SonarLint means it never enters a commit, never occupies a reviewer's attention, and never fails a build. It's the same principle behind the whole DevOps emphasis on fast feedback loops, applied at the smallest possible scale.

SonarLint can run standalone with its default rules, or in **connected mode**, where it binds to the team's SonarCloud project and pulls down the same rule set and quality profile the server uses. Connected mode is strongly preferable: without it, developers see one set of rules locally and the build enforces a different one, which produces exactly the confusion you'd expect.

---

## SonarCloud — Feedback on the Pull Request and the Build

**SonarCloud** is the hosted server-side counterpart. (SonarQube is the same product, self-hosted — if a team runs their own instance, everything here applies identically.)

Where SonarLint sees the file in front of one developer, SonarCloud analyzes the **entire codebase** as a pipeline stage, and it keeps history. That gives it a few things the IDE plugin can't offer:

- **Whole-project analysis**, including cross-file issues like duplication between two modules that no single developer has both of open.
- **Trends over time** — is quality improving or degrading across releases?
- **A shared, enforced standard**, rather than each developer's local configuration.
- **Pull request decoration** — analysis results posted directly onto the PR as comments and a status check, so reviewers see them alongside the diff.

That last one is the day-to-day experience for most developers. A PR is opened, the pipeline runs analysis on the changed code, and within a few minutes the PR shows a pass/fail status plus inline comments on the specific lines with problems. The reviewer's attention goes to design and correctness, and the mechanical concerns are already handled.

---

## What Gets Measured

SonarCloud sorts its findings into categories, and knowing which is which matters — they carry very different urgency.

**Bugs** — code that is demonstrably wrong and will misbehave at runtime. Null dereferences, resources never closed, conditions that can't ever be true, `equals()` implemented without `hashCode()`. These are defects, not opinions, and they're the highest priority.

**Vulnerabilities** — security weaknesses. SQL injection through string-concatenated queries, hardcoded credentials, weak cryptography, unvalidated input reaching a sensitive operation. Also defects, and often the most urgent ones.

**Security Hotspots** — a separate, softer category: security-sensitive code that *requires human review* to judge. Not "this is wrong" but "this uses cryptography / handles a file path / builds a command — someone should confirm it's used correctly here." A hotspot is a request for attention, not an accusation.

**Code Smells** — maintainability problems. The code works, but it's harder to change than it should be: methods that are too long, deeply nested conditionals, duplicated logic, dead code, cryptic names, excessive complexity. Individually minor, collectively the reason a codebase becomes miserable to work in.

**Duplication** — the percentage of the codebase that is copy-pasted. Duplication is tracked separately because it's a specific and expensive failure: a bug fixed in one copy and not the other three is a classic and entirely avoidable production incident.

**Coverage** — the percentage of code exercised by tests, imported from the test run rather than computed by Sonar itself. Useful with a caveat worth stating: coverage measures which lines *ran* during tests, not whether anything was meaningfully asserted about them. High coverage with weak assertions is a comfortable illusion.

Each issue also carries a **severity** (Blocker, Critical, Major, Minor, Info), which is what quality gate rules are usually written against.

---

## Quality Gates

A **quality gate** is a set of pass/fail conditions applied to the analysis results. This is what converts Sonar from a dashboard that people occasionally look at into an actual gate in the pipeline.

The mechanism is simple: if the conditions aren't met, the gate fails, and the pipeline stage fails with it. A failed gate blocks the merge or stops the build — the same binary verdict as a failing test.

Sonar's default gate, "Sonar way," is built around a specific and important idea: **it evaluates new code only.**

A typical configuration on new code:
- No new bugs
- No new vulnerabilities
- All new security hotspots reviewed
- Maintainability rating of A
- At least 80% coverage on new lines
- No more than 3% duplication on new lines

*Conditions applied only to lines added or changed — a legacy codebase can adopt this today without a cleanup project first.*

This **Clean as You Code** approach is what makes the tool adoptable in the real world. A ten-year-old codebase might have eight thousand existing issues. A gate demanding zero total issues is unachievable, so the team would disable it and nothing would improve. A gate demanding that *new* code be clean is achievable starting immediately, and the codebase improves steadily as code gets touched.

### Failing a Build on the Gate

Wiring the gate into the pipeline is what gives it teeth. In a Jenkins pipeline (topic 007), that's a stage that runs the analysis and then waits for the server's verdict:

```groovy
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
```

*The analysis runs, then the pipeline pauses for SonarCloud's verdict; `abortPipeline: true` fails the build if the gate doesn't pass.*

The two stages are separate because analysis is asynchronous — Jenkins submits the code, SonarCloud processes it, and the second stage waits for the result to come back. Without `abortPipeline: true`, a failing gate is merely recorded, and a recorded warning that blocks nothing gets ignored within about two weeks.

---

## Technical Debt as Something Measurable

**Technical debt** is the accumulated cost of shortcuts and compromises in a codebase — the extra effort every future change requires because of decisions made earlier.

The metaphor is deliberately financial. Taking a shortcut to hit a deadline is borrowing: we get speed now and pay interest later, in the form of every subsequent change being slower. Some borrowing is sensible. Unmanaged borrowing eventually consumes all available capacity.

The chronic problem with technical debt is that it's usually argued about in feelings. A developer says the payment module is "a mess" and needs refactoring; a product owner hears an aesthetic preference competing with features that have measurable value. There's no shared unit of comparison, so the argument is unwinnable and the debt persists.

SonarCloud puts a number on it. Each rule has an estimated **remediation time**, and summing across all issues gives a total debt figure in developer-hours or days. From that comes the **technical debt ratio** — remediation cost as a percentage of the estimated cost of writing the code from scratch — which produces the maintainability rating (A through E).

That changes the conversation:

> "The payment module is a mess."

becomes

> "The payment module carries 14 days of technical debt, rated D. It's accumulated 3 days in the last quarter, and it's where 40% of our production incidents originate."

The second version can be prioritized against a feature request, because it's in the same units the rest of planning uses.

Two honest caveats. The estimates are rough — they come from generic per-rule averages, not from anything that knows our codebase. And the number counts only what the rules can see; a fundamentally wrong architecture generates no findings at all while being the most expensive debt a project can carry. The value isn't precision. It's having a consistent, trackable number that makes an invisible cost visible and lets us watch its direction over time.

---

## Key Takeaways

- Static analysis inspects code without running it, finding risky patterns on every line — complementing tests, which verify behavior only on paths they exercise.
- SonarLint gives feedback in the IDE as we type, the cheapest possible place to catch an issue; use connected mode so local rules match the server's.
- SonarCloud analyzes the whole codebase as a pipeline stage, tracks trends, and decorates pull requests with results.
- Findings are categorized as bugs, vulnerabilities, security hotspots, code smells, duplication, and coverage — bugs and vulnerabilities are defects, smells are maintainability.
- A quality gate turns analysis into a pass/fail condition that can fail the build; `abortPipeline: true` is what makes it enforce rather than report.
- Clean as You Code applies the gate to new code only, which makes the practice adoptable on an existing legacy codebase.
- Quantifying technical debt in time and a letter rating lets maintainability be prioritized alongside features instead of argued about as a feeling.
