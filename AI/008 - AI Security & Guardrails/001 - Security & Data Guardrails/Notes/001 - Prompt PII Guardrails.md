# Prompt PII Guardrails

Every prompt leaves the building. Whatever we type, paste, or attach travels to the AI provider's infrastructure — possibly logged, possibly retained per contract terms, definitely outside our perimeter. That's routine and fine for *code and technical content* under an enterprise agreement; it is categorically not fine for **PII** (names, emails, SSNs, account numbers, health and financial details) or other regulated/secret data (credentials, keys, customer records). In a financial-services environment, a customer record pasted into a chat window is a **data-handling incident** — the AI's answer quality is irrelevant. This lesson is the discipline that keeps prompts clean.

---

## Where PII Sneaks In

Nobody types a customer's SSN into Copilot on purpose. It arrives *embedded* in artifacts we handle reflexively:

- **Stack traces and logs** — the top offender: exception messages carry the values that caused the failure (`IllegalArgumentException: invalid SSN format for 078-05-...`), and production log excerpts carry whole request payloads.
- **Bug tickets** — reproduction steps quoting real customers ("when Maria Gonzalez, acct 4471..., edits her order...").
- **Real data as "sample" data** — pasting three rows from the production DB to show the AI a format; exporting a CSV to debug a parser.
- **Database query results** — "why does this query return this?" with the actual result set attached.
- **Config and .env files** — attached for a debugging session, credentials riding along. (Secrets aren't PII, but the guardrail is identical: they don't go in prompts.)
- **Screenshots** — the forgotten channel; an attached screenshot of the admin UI is a data export.

The pattern: PII enters prompts *inside evidence*, at exactly the moments — debugging pressure, ticket triage — when we're least inclined to stop and read what we're pasting.

---

## The Sanitization Habit

The rule that scales: **read every paste as an exfiltration reviewer before sending.** Concretely:

```text
Before:  IllegalStateException: refund rejected for card 4111-1111-1111-1234,
         customer maria.gonzalez@gmail.com, acct 44711092

After:   IllegalStateException: refund rejected for card [CARD-1],
         customer [EMAIL-1], acct [ACCT-1]
```

*Placeholder substitution: consistent tokens ([CARD-1], [EMAIL-1]) preserve the structure and the relationships — which is all the AI needs for diagnosis.*

The technique's key insight: **the AI almost never needs the real values.** It needs shapes, formats, and relationships — "same account appears in both events" — which consistent placeholders preserve perfectly. Same for data samples: *generate* representative data instead of copying it (the Data Seeding lesson exists partly for this reason — synthetic data with the right shape and edge cases is both safer and usually *better* than a production excerpt). For recurring log/trace work, sanitize mechanically: a small scrubbing script (or IDE snippet) that masks known patterns — emails, card numbers, our account-ID format — beats per-paste vigilance, and *ours* is the kind of team that should have one in the repo.

Three edge disciplines complete the habit. **Names in prose count** — "the bug affects John Smith's account" is PII even with no attachment. **Tickets are already contaminated** — the issue-workflow lesson's warning, operational here: sanitize *before* pasting a ticket, and better, write tickets sanitized in the first place (future prompts inherit clean sources). **When it happens anyway** — wrong paste, sent — treat it as the incident it is: report per the data-handling process, don't quietly delete the chat. The process exists because retention and logging make "unsend" fictional.

---

## The Layered Backstop

Personal habit is layer one; mature setups add mechanism, previewing the next two lessons: enterprise AI agreements (no training on our data, defined retention — the reason *approved* tools are approved), DLP-style filters that scan outbound prompts for credential and PII patterns, and **hooks** that inspect agent tool calls before execution — catching the agent that helpfully tries to read `.env` or `customers.csv` into its context. Policy defines the line, habit holds it daily, tooling catches the misses: all three layers, because each fails alone.

Clean prompts are half of AI security — the data we *send*. The other half is what comes *back* and what watches the exchange: injection, leakage, and the risks in generated code. Next lesson.
