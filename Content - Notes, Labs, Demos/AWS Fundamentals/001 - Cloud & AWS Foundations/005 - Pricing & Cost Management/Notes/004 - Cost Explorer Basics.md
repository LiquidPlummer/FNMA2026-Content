# Cost Explorer Basics

Budgets tell us *when* spending crosses a line we've drawn. **Cost Explorer** answers a different question: *why* is spending what it is, and where exactly is it coming from? It's AWS's tool for visualizing and analyzing cost and usage data over time.

## What Cost Explorer Is For

Cost Explorer takes the raw billing data AWS already collects and turns it into visual, explorable reports — charts showing spending trends over time, broken down however we choose. Instead of squinting at a flat list of line-item charges, we can ask questions like:

- Which service is driving the largest share of our spend?
- Is spending trending up, down, or flat over the past several months?
- Did a spike in cost on a particular day come from one service, or several?
- How does spending compare between two time periods — say, this month versus last month?

## Core Concepts

**Grouping and filtering** are the two main ways we shape a Cost Explorer report. We can group cost data by dimensions like service, region, or (if we've set them up) resource tags, and filter down to just the slice we care about. Grouping by service is usually the first move when investigating a cost increase — it immediately narrows "something got more expensive" down to "this specific service got more expensive."

**Tags** deserve a special mention here. AWS lets us attach custom labels (tags) to resources — for example, tagging resources by team, project, or environment (production vs. testing). If those tags are applied consistently, Cost Explorer can group spending by them, which turns a single account's bill into something that answers "how much did the marketing team's project cost this month?" without needing separate AWS accounts per team. This only works if tagging discipline is established *before* the cost history accrues — it can't retroactively tag past charges that were never tagged in the first place.

**Forecasting** is the other major capability: based on historical usage patterns, Cost Explorer can project what spending is likely to look like going forward if current trends continue. This is what powers the "forecasted" budget alerts mentioned in the previous topic — the forecast isn't magic, it's pattern continuation, so it's most reliable for workloads with fairly steady, predictable usage.

## How This Fits With Budgets

Budgets and Cost Explorer are complementary, not competing, tools:

- A **budget** is a proactive tripwire — it alerts us the moment something crosses a threshold we defined in advance.
- **Cost Explorer** is a reactive investigation tool — once we know something looks off (whether from a budget alert or just periodic review), it's how we dig in and find the actual cause.

A healthy cost-management habit uses both: budgets to catch problems early, and Cost Explorer to understand and act on them once they're found. Together, they turn "the cloud bill is unpredictable" from a real risk into something we have ongoing visibility and control over — which is really the whole point of everything covered in this lesson.
