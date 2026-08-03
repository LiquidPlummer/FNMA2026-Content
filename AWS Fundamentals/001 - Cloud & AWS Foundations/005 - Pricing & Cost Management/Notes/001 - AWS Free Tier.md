# AWS Free Tier

Getting started with AWS doesn't have to mean spending money right away. AWS offers a **Free Tier** — a set of allowances that let new accounts use certain services at no cost, up to defined limits, so we can learn and experiment without financial risk.

## What the Free Tier Is For

The Free Tier exists to lower the barrier to trying AWS. Instead of reading documentation and taking pricing on faith, we can actually launch resources, break things, fix them, and get hands-on experience with real services — all without a bill showing up at the end of the month, as long as usage stays within the allowed limits.

The Free Tier generally comes in a few different shapes:

- **Always-free offers** — some limited usage of certain services that never expires, available to any account.
- **Time-limited trial offers** — short-term trials of a service, meant for evaluating something new.
- **Limits tied to account age** — some allowances are only available for a set period after an account is first created, specifically to support onboarding new users.

Exactly which services are included, and under which category, changes over time as AWS adjusts the program — so rather than memorizing a list, the important habit is checking the current Free Tier offer for a service **before** relying on it, directly from the AWS pricing documentation for that service.

## Why "Free" Still Requires Caution

The single most important thing to internalize about the Free Tier is that it has **limits**, and exceeding them means normal charges apply to the overage — the Free Tier doesn't cap what we can spend, it just discounts usage up to a threshold. It's entirely possible to use a service in good faith, exceed a limit without realizing it, and end up with an unexpected charge.

A few habits help avoid surprises:

- **Read the specific limits** for any service before assuming usage is free — limits are defined per service, not as one blanket allowance.
- **Set up billing alerts** (covered in the next couple of topics) so we're notified before small overages become a large surprise.
- **Shut down or delete resources** we're done experimenting with, rather than leaving them running indefinitely.

## Why This Matters for Learning AWS

The Free Tier is genuinely one of the best ways to build real AWS skills — reading about EC2 is not the same as launching an instance, connecting to it, and seeing how it behaves. The tier is designed to make that hands-on practice low-risk. The discipline it teaches — checking limits, watching usage, cleaning up unused resources — is also exactly the discipline that scales up to managing real production costs later, which is the focus of the rest of this lesson.
