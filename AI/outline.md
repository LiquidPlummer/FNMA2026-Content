# AI Training Curriculum — Notes Outline

> **Note:** The primary AI tool for this curriculum is **GitHub Copilot**.

## AI Tooling & Prompting Foundations

### Getting Started with AI Tooling
- **AI Tooling Introduction** — Overview of tools in use (Copilot, Claude Code, Cursor, etc.).
- **AI Pair Programming** — Using AI as an active coding partner vs. autocomplete.

### Prompting Fundamentals
- **Zero-Shot Prompting** — Direct asks without examples.
- **Few-Shot Prompting** — Providing examples to guide output.

### AI-Assisted Comprehension & Debugging
- **AI Debugging** — Feeding errors/stack traces to AI, iterating on fixes.
- **AI Knowledge Lookup & Explanation** — AI to explain unfamiliar code/concepts.

### Personalizing & Planning
- **Personal AI Rulesets** — Custom instructions (e.g., copilot-instructions.md).
- **AI Task Planning** — AI breaks work into steps before coding.

### Working Critically with AI
- **AI Issue Tracking and Resolution** — AI in ticket workflows (triage, fixes from issues).
- **Preventing Blind-Acceptance** — Critically reviewing AI output.

## Prompt Engineering Techniques

### AI TDD
- **Test-First with AI** — Write/generate tests first, then implement.

### Advanced Prompting Techniques
- **AI Personas** — Assigning roles to AI ("act as a security reviewer").
- **Chain of Thought** — Prompting for step-by-step reasoning.
- **Constraints and Output Compliance** — Enforcing format/scope constraints (JSON output, style rules).

### Prompt Evaluation
- **Evaluating Prompts** — Assessing prompt quality/output consistency.

## Applied AI Development

### AI for Data & Persistence
- **AI Persistence Solutioning** — AI for data/persistence layer design (schemas, ORM, migrations).
- **AI for Data Seeding** — Generating realistic test/seed data.

### Reliability in Cloud Contexts
- **Hallucination Mitigation (Cloud Solutions)** — Catching fabricated APIs/services in cloud contexts.

### AI for Equivalent Translation
- **Cross-Language Translation** — Translating code across languages/frameworks with equivalent behavior.

## In-Context Learning & Codebase-Wide AI

### AI In-Context Learning
- **Teaching by Example** — Teaching AI patterns via examples in context (no fine-tuning).

### AI for Cross-Cutting Concerns
- **Retrofitting Concerns** — AI applied to logging, auth, error handling, caching across a codebase.

## Spec-Driven Development & Team Workflows

### Spec-Driven Development & Task Framing
- **Spec-Driven Development** — Writing detailed specs first; AI implements against them.
- **AI Task Framing** — Structuring tasks with context, constraints, and success criteria.

### Team AI Practices
- **Project-Level AI Rulesets** — Shared team/repo rulesets.
- **Human-in-the-Loop Validation** — Designing checkpoints where humans review before AI proceeds.

## Context Management & Structured IO

### AI Context Management
- **Managing Context Windows** — What to include, when to reset, avoiding context rot.

### Structured AI IO
- **Structured Inputs/Outputs** — JSON schemas, templates for reliable pipelines.

## Agent Autonomy & Workflow Design

### Agent Autonomy Controls
- **Bounded Autonomy** — Defining what AI may do unsupervised vs. what needs approval.
- **Stop Conditions** — Explicit criteria for when AI should halt (errors, ambiguity, scope limits).

### AI Workflow Designs
- **Composing Workflows** — Multi-step AI workflows (chains, review loops).

## AI Security & Guardrails

### Security & Data Guardrails
- **Prompt PII Guardrails** — Keeping personal/sensitive data out of prompts; sanitization.
- **AI Security Considerations** — Prompt injection, data leakage, risks of AI-generated code.

### AI Tool Hooks
- **Hooks & Callbacks** — Pre/post actions around AI tool calls (logging, blocking).

## Agentic Patterns & Governance

### Agentic Design Patterns
- **Agent Architectures** — Planner-executor, reviewer loops, orchestrators.

### AI Governance
- **Org Policies** — Approved tools, audit, compliance, accountability.