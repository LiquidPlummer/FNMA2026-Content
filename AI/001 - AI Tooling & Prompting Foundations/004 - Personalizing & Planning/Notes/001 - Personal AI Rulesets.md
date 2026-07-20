# Personal AI Rulesets

By now a pattern has emerged: every session, we retype the same preferences — "Java 21," "use our ApiError record," "tests in JUnit 5, no comments narrating the obvious." A **ruleset** (custom instructions) fixes the repetition: a file of standing instructions the tool automatically includes with every request. Write the preference once; every future prompt inherits it.

---

## copilot-instructions.md

Copilot reads custom instructions from `.github/copilot-instructions.md` in the repository (and personal-scope equivalents in the IDE settings). The content is plain markdown — instructions written exactly as we'd type them in chat:

```markdown
# Copilot Instructions

## Environment
- Java 21, Spring Boot 3.3, Gradle with Groovy DSL.
- Tests: JUnit 5 + Mockito. AssertJ is NOT used here — plain Assertions.

## Code style
- Prefer records for DTOs; constructor injection, never field injection.
- Use Optional returns for lookups; never return null from a repository-facing method.
- No wildcard imports. No comments that restate the code.

## When generating tests
- One behavior per test, Arrange-Act-Assert with blank-line separation.
- Name tests as behavior statements: depositIncreasesBalance, not testDeposit.

## When unsure
- Ask before adding new dependencies.
- Match the conventions of the surrounding file over these rules if they conflict.
```

*A working instructions file: environment facts, style rules, test conventions, and escalation behavior — the brief we'd give a new team member.*

Mechanically, the file's contents are prepended (invisibly) to our prompts — it's standing context, spending context-window budget on every request. That mental model drives all the craft rules below.

---

## What Belongs in a Ruleset — and What Doesn't

**Belongs:** anything we correct *repeatedly*. Environment facts the AI can't infer (versions, frameworks, build tool). Conventions that differ from the ecosystem default (the AI already writes idiomatic Java; it needs telling that *this team* forbids AssertJ). Output preferences ("code first, brief explanation after"). Behavioral standing orders ("ask before adding dependencies").

**Doesn't belong:** task-specific detail (that's the prompt's job — a ruleset entry about invoice validation helps one task and pollutes hundreds), restatements of universal good practice ("write clean code" — noise), essays (every line costs context on every request — a bloated ruleset crowds out actual code context), and anything secret (the file is committed and travels with prompts).

The craft in one sentence: **a ruleset is for the deltas** — where our world differs from the world the model already assumes.

---

## Growing It Honestly

Start empty. Add a rule the *second* time we type the same correction in chat — that repetition is the signal. Review monthly and prune: rules that stopped mattering, rules the AI follows by default now, rules we can't remember why we added. Keep it under a screen or so of text; past that, trimming beats adding.

And test the effect: after adding a rule, run a prompt that used to produce the wrong pattern and confirm the output changed. Instructions files fail silently — a rule phrased ambiguously ("keep things simple") steers nothing, while a concrete one ("prefer a guard clause over nested if/else") visibly changes output. Concrete, checkable, few — the same properties as good code review feedback, which is roughly what a ruleset is: our standing review comments, delivered in advance.

Personal rulesets tune the AI to *us*; the team-shared version — same file, negotiated conventions, one per repo — is a later lesson (Team AI Practices). Next, though: the other half of personalizing the workflow — getting the AI to plan before it codes.
