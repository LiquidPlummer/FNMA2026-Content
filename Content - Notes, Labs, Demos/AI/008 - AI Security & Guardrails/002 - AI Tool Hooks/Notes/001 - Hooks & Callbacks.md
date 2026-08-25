# Hooks & Callbacks

Rulesets *ask* the AI to behave; review *checks* afterward. **Hooks** are the third mechanism, and the only deterministic one: user-defined programs that run automatically **around an AI tool's actions** — before a file edit, before a shell command, after a change, at session events — able to observe, log, modify, or **block** what the agent does. A hook doesn't rely on the model following instructions; it intercepts the action itself. That makes hooks the enforcement layer for everything the last two units declared: autonomy zones, protected paths, PII guardrails.

---

## The Model: Events, Scripts, Verdicts

Agent tools (Claude Code's hooks being the fully-worked example; Copilot and other agent frameworks expose growing equivalents) fire events at defined moments, and our configured scripts run with the action's details as input:

- **Pre-action** (e.g., `PreToolUse`) — before a tool call executes: the hook sees the pending command/edit and returns a verdict — allow, block-with-message, or require approval. This is where guardrails live.
- **Post-action** (`PostToolUse`) — after execution: the hook sees what happened — formatting, validation, logging live here.
- **Session events** (start, stop, prompt-submit) — bookkeeping moments: load context, finalize logs, run end-of-turn checks.

```json
{
  "hooks": {
    "PreToolUse": [{
      "matcher": "Bash",
      "hooks": [{ "type": "command", "command": "python .ai/guard_bash.py" }]
    }]
  }
}
```

*A hook registration (Claude Code format): every shell command the agent attempts is first piped through our guard script, which reads the action as JSON on stdin and answers with a verdict.*

```python
# .ai/guard_bash.py — sketch of the guard's logic
action = json.load(sys.stdin)                    # {"tool":"Bash","command":"git push --force ..."}
cmd = action["tool_input"]["command"]

if BLOCKLIST.search(cmd):                        # push --force, rm -rf, curl to unknown hosts...
    verdict(deny=f"blocked by policy: {cmd}")    # agent receives the reason, can adjust course
elif touches_protected_paths(cmd):               # db/migration/**, .env, legacy/**
    verdict(ask=True)                            # escalate to the human
else:
    verdict(allow=True)
```

*The guard pattern: deterministic policy in a few dozen lines — deny the red zone, escalate the yellow, wave the green through silently.*

---

## The Standard Hook Portfolio

What teams actually deploy, mapped to the units that motivated each:

- **Autonomy enforcement** — the guard above: red-zone commands denied, yellow-zone actions escalated to approval, protected paths made *mechanically* untouchable. The zones stop being policy prose and become physics.
- **Data guardrails** — pre-action scans of outbound content: block the agent from reading `.env`/credential files into context; flag prompts or file payloads matching PII patterns (the mechanical backstop the PII lesson promised).
- **Audit logging** — every tool call appended to a log: what ran, on what, verdict, timestamp. Boring until the governance unit — then it's the answer to "what did the agent actually do?", and the observability the workflow checklist required.
- **Quality gates** — post-edit hooks running the formatter and linter on touched files, so generated code lands conforming instead of getting review comments about imports; post-turn hooks running affected tests and *feeding failures back to the agent* — tightening the verification loop without human polling.
- **Injection dampeners** — pre-action checks on content fetched from untrusted sources (the indirect-injection channel), at minimum logging what external text entered context.

Design rules carried over from everything else: hooks must be **fast** (they're on every action — a slow hook is a tax on the whole session), **deterministic and dumb** (policy lookups and pattern checks, not their own AI calls — the guard must not be injectable), **fail closed** for security hooks (script error = block, not allow), and **versioned in the repo** like the ruleset they enforce — reviewed via PR, because a hook change is a policy change.

---

## The Complete Stack, Assembled

With hooks in place, the control layers stack cleanly: **rulesets** shape what the AI intends; **frames and stop conditions** bound what it attempts; **hooks** gate what actually executes; **mechanical gates and tests** verify what got produced; **human checkpoints** judge what proceeds; **audit logs** record all of it. No layer is sufficient — the model can ignore instructions, hooks only see actions, humans fatigue — but the stack fails soft: each layer catches the others' misses. That layered posture is precisely what the final unit generalizes: agentic architectures built on these controls, and the organizational governance that makes the whole practice defensible.
