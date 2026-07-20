# AI Tooling Introduction

AI coding tools have become standard equipment in professional development, and this curriculum treats them that way: not a novelty, but a skill with technique, judgment, and failure modes to master. Our primary tool throughout is **GitHub Copilot**; the concepts transfer to the rest of the landscape, which is worth a quick map before we settle in.

---

## The Tool Landscape

The tools cluster by *where they sit* in the workflow:

- **In-editor assistants** — **GitHub Copilot** (VS Code, JetBrains): inline completions as we type, a chat panel for questions and edits, and an agent mode that can carry out multi-file tasks. Because it lives where the code lives, it sees real context — open files, the project tree, compiler errors.
- **Terminal/agentic CLIs** — **Claude Code** and similar: conversation-driven agents that read the codebase, run commands and tests, and make coordinated multi-file changes. Stronger for larger autonomous tasks; the autonomy itself becomes something to manage (a later unit).
- **AI-first editors** — **Cursor**, **Windsurf**: forks of VS Code with AI integrated deeper into navigation and editing. Same concepts, tighter packaging.
- **Chat websites** — ChatGPT, Claude.ai: general-purpose, but *outside* the codebase — no project context, and pasting company code into unapproved ones is a policy violation (the Security unit covers why). Prefer the sanctioned in-editor tools.

Under every one of them is the same engine: a **large language model (LLM)** — a text predictor trained on vast code and prose, steered entirely by the text we put in front of it. That single fact explains most tool behavior: what the model can *see* (its **context**) determines what it can do, and everything we send it — instructions, code, examples — is the **prompt**. Managing both well is essentially the whole curriculum.

---

## Copilot in Practice

Three surfaces, three habits:

**Inline completions.** Copilot proposes code as we type — Tab accepts, Esc dismisses. It shines on boilerplate, obvious continuations, and patterns it can infer from surrounding code. The habit: read before accepting, always (a later lesson makes this a discipline).

**Chat.** `Ctrl+Alt+I` opens a conversation grounded in the editor: highlight code and ask "why does this throw?", request a refactor, generate a test. Chat is where most of the prompting techniques in this subject apply.

**Agent mode.** Copilot plans and executes multi-step changes — creating files, editing several places, running builds. Powerful, and exactly where critical review and bounded autonomy (later units) become non-negotiable.

```text
Prompt (in Copilot chat, with InvoiceService.java open):
  Explain what this class does, then list any thread-safety
  concerns in the methods that touch the cache field.
```

*A first realistic chat prompt: grounded in an open file, asking for explanation plus targeted analysis.*

---

## What These Tools Are Genuinely Good At — and Not

Worth calibrating expectations on day one. Strong: boilerplate and scaffolding, test generation, explaining unfamiliar code, translations between languages/formats, first drafts of almost anything text-shaped. Weak: knowing *our* business rules, recent or niche APIs (they confidently invent plausible ones — **hallucination**, with a dedicated lesson later), large-scale architectural judgment, and anything where being wrong is expensive and unverifiable.

The stance this curriculum builds toward, stated up front: **the developer remains the engineer of record.** The AI drafts; we specify, constrain, review, and own the result. Every unit that follows is a technique for doing one part of that well — starting with the shift from autocomplete-user to active collaborator, next lesson.
