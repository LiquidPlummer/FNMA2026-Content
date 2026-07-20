# Org Policies

Everything in this curriculum so far has been individual and team craft. The final layer is organizational: **AI governance** — the policies, approvals, audit trails, and accountability structures that let a regulated enterprise use AI development tools *defensibly*. In a financial-services environment this isn't optional overhead; it's the condition of use. The good news, and this lesson's through-line: nearly every governance requirement maps onto a practice we've already built — governance is mostly the craft, written down and made provable.

---

## Approved Tools

Organizations designate an **approved tool list** — specific products, specific license tiers, specific configurations — and the boundary is sharper than it looks: the *same* vendor's consumer product and enterprise product differ precisely in the terms that matter (training on our data excluded, retention bounded, tenant isolation, admin controls, audit APIs). Approval is a review of those terms plus security posture — which is why "Copilot Business is approved" coexists with "personal ChatGPT is not," and why routing around the list ("I'll just paste it into my personal account — it's faster") is a data-boundary violation, not a workflow choice (the leakage lesson's channels, now with policy teeth). The practitioner obligations are simple: use listed tools in listed configurations, request additions through the review path rather than adopting quietly, and treat new AI-powered features inside *already-installed* products (the IDE plugin that sprouted a cloud AI assistant) as new tools requiring the same look.

## Audit and Traceability

Regulated development must answer, after the fact: *what did AI touch, and what checked it?* The audit trail assembles from artifacts we already produce, kept deliberately:

- **Provenance in the record** — PR descriptions noting AI-assisted work and what validation ran (the HITL disclosure habit); commit trailers where the org standardizes them.
- **Decision artifacts** — specs, approved plans, checkpoint outcomes living in tickets and PRs: *who approved what, before what proceeded*.
- **Mechanical logs** — hook-based action logs for agent sessions (every tool call, every verdict — the boring log that becomes the star exhibit), plus enterprise tools' own usage/audit APIs.
- **Gate evidence** — CI records showing generated code passed the same SAST/tests/review as human code.

*The reframe that makes this light: none of these are new work — they're the workflow's existing exhaust, retained. A team doing spec-driven, checkpointed, hook-guarded AI development is generating its audit trail as a side effect.*

## Accountability

The principle threaded through every unit, now as org policy: **AI is never the accountable party.** The engineer who commits AI-drafted code owns it exactly as if typed by hand — same review standards, same on-call consequences, same name on the blame line. "The AI wrote it" has the same standing as "Stack Overflow wrote it": none. Organizationally this cashes out as: every AI-assisted change has a human author of record; every autonomous-agent action traces to the human who framed and launched the run (which is why frames, budgets, and stop reports get retained); and *degree of autonomy granted* is itself a reviewed decision — teams don't privately expand agent permissions any more than they privately grant themselves prod access. Compliance-adjacent domains (money movement, customer data, access control) carry the strictest rules — typically mandatory named-human review regardless of how the code was produced.

## Living Governance

Two closing realities keep this from being a static rulebook. **The field moves faster than policy** — new tools, new modes, new failure classes arrive quarterly, so mature orgs run governance as a *process*: a standing review body, periodic policy refresh, an intake path for practitioner proposals — and practitioners (us, after this curriculum) are the sensor network, expected to surface both opportunities and near-misses rather than improvise around the rules. **Policy floors are minimums, not targets** — the practices in these nine units routinely exceed what policy demands, and that's the correct direction of slack: teams that govern themselves well experience org governance as paperwork already done.

Which is the curriculum's closing thought in miniature: from the first Tab-complete to org-level governance, the same posture held — **the human is the engineer of record; the AI is leverage; verification is what converts leverage into trust.** Everything else was technique.
