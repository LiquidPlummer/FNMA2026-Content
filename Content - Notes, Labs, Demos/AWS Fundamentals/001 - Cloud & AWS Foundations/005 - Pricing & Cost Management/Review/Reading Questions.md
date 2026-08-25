# Reading Questions — Pricing & Cost Management

Use these questions while you read the notes. Each one points at something worth understanding well enough to talk about on the job.

## AWS Free Tier

1. What problem is the Free Tier actually solving, beyond just "free stuff"?
2. What are the three general shapes Free Tier offers come in?
3. Why does the fact that "the Free Tier doesn't cap what we can spend" matter? What's the danger it's warning about?
4. What habits help avoid an unexpected bill while relying on the Free Tier?

## Pricing Models Overview

5. What's the fundamental trade-off between on-demand and reserved/savings-plan pricing?
6. When does committing to reserved capacity actually backfire compared to just paying on-demand rates?
7. What kind of workload is Spot pricing a good fit for, and what kind is it a poor fit for — and why?
8. How is usage-based pricing (like Lambda) fundamentally different from the other three models in what it's actually billing for?
9. Why is there no single "best" pricing model, and what does that imply about how real AWS architectures are usually priced?

## Billing & Budgets

10. What's the key limitation of the billing dashboard that budgets are specifically designed to address?
11. What's the difference between a cost budget, a usage budget, and a forecasted budget?
12. Give at least two concrete scenarios where a budget alert would catch a problem that might otherwise go unnoticed until the bill arrives.
13. Why does the *habit* of setting a budget alert matter more than picking the "correct" dollar threshold?

## Cost Explorer Basics

14. What question is Cost Explorer designed to answer that the billing dashboard alone can't?
15. Why is grouping by service usually the first move when investigating a cost spike?
16. Why does tagging discipline have to be established *before* costs accrue in order for Cost Explorer's tag-based reporting to work?
17. How does Cost Explorer's forecasting differ from just guessing — and what kind of workload is that forecast most reliable for?
18. How do budgets and Cost Explorer work together as complementary tools rather than overlapping ones?
