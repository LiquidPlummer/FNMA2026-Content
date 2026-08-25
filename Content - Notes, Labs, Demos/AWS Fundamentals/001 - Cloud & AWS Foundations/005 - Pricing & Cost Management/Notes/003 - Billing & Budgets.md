# Billing & Budgets

Pay-as-you-go pricing is flexible, but that flexibility cuts both ways — without any guardrails, it's entirely possible for costs to grow quietly in the background until a bill arrives with an unpleasant surprise on it. AWS's **Billing** tools and **Budgets** feature exist specifically to keep that from happening.

## The Billing Dashboard

AWS provides a central billing dashboard where an account owner can see what they've been charged, broken down by service. This is the starting point for understanding *where* money is going — rather than seeing one lump total, we can see that, say, storage costs make up the bulk of a bill versus compute, which points us toward where to focus if we're trying to reduce spend.

Billing data is historical by nature — it reflects charges that have already accrued. That's useful for understanding trends, but it doesn't stop a cost problem before it happens, which is exactly the gap that budgets are designed to fill.

## What a Budget Is

A **budget**, in AWS terms, is a threshold we define for our own spending, paired with an alert that notifies us when actual (or forecasted) spending approaches or crosses that threshold. Rather than manually checking the billing dashboard on some regular schedule, hoping to catch a problem, a budget watches spending on our behalf and proactively tells us when something looks off.

Budgets are commonly set up in a few different shapes:

- **Cost budgets** — alert when spending crosses a dollar threshold we define.
- **Usage budgets** — alert based on usage of a particular resource (for example, hours of a certain instance type), independent of cost.
- **Forecasted budgets** — alert based on where AWS's own cost forecasting predicts spending is trending, catching a problem *before* it fully materializes rather than after.

## Why Setting a Budget Alert Is a Best Practice

The value of a budget alert isn't really about the exact number we choose — it's about having *some* early-warning system in place at all. A few reasons this matters in practice:

- **Free Tier limits can be exceeded without obvious warning.** A budget alert catches the transition from free usage to billed usage before it snowballs.
- **Runaway resources happen.** A forgotten test server left running, or a misconfigured process making far more API calls than intended, can rack up cost silently — a budget alert surfaces this quickly instead of at the end of a billing cycle.
- **Teams change.** In a shared account, a budget alert gives visibility into spending changes that might come from someone else's work, not just our own.

## The Habit to Build

The specific dollar amount or usage threshold to set is a judgment call that depends entirely on context — a personal learning account and a production business account warrant very different thresholds, and any number we picked here would be arbitrary and quickly outdated. What matters is the habit: as a first step on any new AWS account, set up at least one budget alert, even a rough one, before starting to provision resources. It costs nothing to set up and turns "I hope nothing goes wrong" into "I'll be notified if something does."
