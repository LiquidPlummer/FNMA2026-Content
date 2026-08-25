# AI Pair Programming

There are two ways to use Copilot, and they produce very different results. **Passive mode**: type, glance at gray text, Tab, repeat — autocomplete with better guesses. **Pair programming mode**: treat the AI as an active partner — brief it, delegate deliberately, review its work, and keep a running dialogue. The tool is identical; the difference is us, and it's the difference between marginal and transformative gains.

---

## The Pairing Loop

Human pair programming has a driver (hands on keyboard) and a navigator (thinking ahead, catching issues). With AI, we're permanently the navigator — and the loop looks like:

1. **Frame** — say what we're building and why, before code: a comment, a chat message, a function signature. The AI can only work from what's in front of it.
2. **Delegate** — hand over a *scoped* piece: "generate the validation for this DTO," not "build the feature."
3. **Review** — read the draft as we would a junior developer's PR: correct? idiomatic for *this* codebase? edge cases?
4. **Refine** — respond in chat: "use our ApiError record instead of a map," "handle the null case." Iteration is conversation, not re-rolling.
5. **Own** — commit only what we fully understand. Authorship never transfers.

```text
Us:      I'm adding a rate limiter to InvoiceController — token bucket,
         per-client-id, 100 requests/minute, using our existing Clock bean.
         Sketch the class first, don't write tests yet.

Copilot: [drafts RateLimiter class]

Us:      Good shape. Two changes: make refill lazy on acquire instead of
         a scheduler, and throw our RateLimitExceededException instead
         of returning boolean.
```

*The loop in miniature: frame with constraints, review the draft, steer with specifics — like talking to a fast, well-read pair who doesn't know our conventions yet.*

---

## What to Delegate, What to Keep

A working split that holds up in practice:

- **Delegate freely:** boilerplate (DTOs, mappers, config), test scaffolding, documentation drafts, exploratory "show me three approaches" sketches, mechanical refactors.
- **Delegate with tight framing:** business logic (the AI drafts, we specify the rules precisely and verify), integrations against APIs we know well enough to check.
- **Keep:** architectural decisions, security-sensitive code paths, anything we couldn't review competently — if we can't judge the output, we can't accept it, and delegating it is abdication rather than efficiency.

Two anti-patterns bracket the skill. **Over-delegation**: accepting cascades of plausible code we half-understand — debt that comes due at debugging time, when we own code nobody wrote. **Under-delegation**: using a capable model as line-completion only, hand-typing boilerplate the AI would draft flawlessly in seconds. Both waste the partnership.

---

## Making the Partner Smarter

The AI's usefulness tracks the context it can see (a theme with its own unit later). Cheap wins available today: keep the relevant files *open* (Copilot weights open tabs heavily), write the comment or signature *before* invoking completion, name things descriptively (`calculateProratedRefund` steers generation better than `calc`), and in chat, reference code explicitly (highlight it, or use `#file` mentions) instead of describing it from memory.

And the flip side of the partnership: an AI pair is tireless and widely read, but it never pushes back the way a human partner would — it won't say "wait, why are we doing this at all?" unless asked. Prompting for that skepticism ("what's wrong with this approach?") is a technique we'll formalize in the personas lesson; cultivating our *own* skepticism is the Working Critically topic at the end of this unit.
