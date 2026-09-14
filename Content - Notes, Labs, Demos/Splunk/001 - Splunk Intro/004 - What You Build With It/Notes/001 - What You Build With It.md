# What You Build With It

A search answers a question once. Most of the value in a Splunk deployment comes from what gets built on top of searches, so that questions get answered repeatedly, automatically, and for people who never write SPL themselves.

---

## Saved Searches

A **saved search** is a search stored under a name so it can be re-run without being rewritten. It's the building block for everything else in this topic: reports, dashboard panels, and alerts are each a saved search with something added.

---

## Knowledge Objects

**Knowledge objects** is Splunk's umbrella term for the reusable pieces people build on top of raw data. They capture what an organization has learned about its data — what a field means, which events matter, how to label them — so that nobody has to work it out again.

The ones we'll hear about most:

- **Field extractions** — saved rules for pulling a field out of raw text, so that a field like `order_id` exists for everyone, not just the one person who figured out the pattern.
- **Event types** — a search condition saved under a name. Define once what a failed login looks like, call it `failed_login`, and everyone can search for `eventtype=failed_login`.
- **Tags** — labels attached to field values or event types. Tagging several different login formats with `authentication` lets us search by meaning rather than by each vendor's format. Splunk's **Common Information Model (CIM)** standardizes this idea with shared field names and tags, and security teams rely on it heavily.
- **Lookups** — tables that add outside information to events, covered below.
- **Saved searches**, and the reports and alerts built from them, count as knowledge objects too.

Almost all knowledge objects are applied at search time. That's why they work on data that arrived long before they were created.

Knowledge objects have owners and sharing levels: a new one starts private to the person who made it and can be shared with a team or made available to everyone. They're commonly packaged into **apps** — bundles of knowledge objects, dashboards, and configuration built for a purpose. Splunk Enterprise Security is an app. So are the **add-ons** that teach Splunk to understand a particular vendor's data, like a specific brand of firewall or a cloud provider's logs. **Splunkbase** is the public catalog where most of them are published.

---

## Lookups

A **lookup** enriches events with information that isn't in the log at all. Logs record what a machine knows: a host name, a user ID, an error code. They don't record which team owns that host, what department the user is in, or what the error code means. That information lives elsewhere — an asset inventory, a directory, a spreadsheet.

A lookup is a table, often a simple CSV file, that maps a value found in events to extra fields:

| host | team | environment |
|---|---|---|
| web-01 | Storefront | production |
| web-02 | Storefront | production |
| pay-01 | Payments | production |

With this lookup in place, every event from `pay-01` can carry `team=Payments`, and we can count errors by team instead of by server name. The stored events never change; the extra fields are added at search time.

---

## Reports and Dashboards

A **report** is a saved search whose results are meant to be looked at — a table or chart that people come back to. Reports can run on a schedule, so the results are ready when someone opens them or land in an inbox every morning.

A **dashboard** is a page of **panels**, each one a chart, table, or single number driven by its own search. Dashboards often include inputs, like a time range picker or an environment dropdown, that feed into every panel's search at once.

Dashboards are how most people in an organization experience Splunk. A manager checking the health of the checkout service is looking at the output of SPL someone else wrote, and never needs to see it.

---

## Alerts

An **alert** is a saved search that runs on its own and takes action when it finds something. Every alert has four parts:

- **A search** — the question, such as failed logins grouped by user
- **A schedule** — how often to ask it, such as every five minutes, looking back five minutes
- **A trigger condition** — when the result counts as a problem, such as any user with more than ten failures
- **An action** — what to do when it triggers: send an email, post to a chat channel, call a webhook, open a ticket

Alerts turn Splunk from something we consult into something that watches for us. The ongoing work is tuning. An alert that's too sensitive fires constantly until people learn to ignore it; one that's too strict misses the problem it was written to catch. **Throttling**, which suppresses repeat alerts for a set period after one fires, helps keep the noise down.

---

## Why This Layer Is the Point

Writing SPL is a skill that lives in one person's head. Knowledge objects are how that skill becomes something the organization owns. The field extraction someone worked out, the lookup that maps servers to teams, the alert that caught last year's outage early — each gets built once and then used by everyone, every day, including people who never write a search.

That's the real measure of a Splunk deployment. An organization that only runs one-off searches has an expensive log viewer. One that has built up its extractions, lookups, dashboards, and alerts has a system that understands its own data — and that layer is most of what people mean when they talk about "our Splunk."

---

## Key Takeaways

- A saved search is a named, re-runnable search, and it's the foundation for reports, dashboards, and alerts.
- Knowledge objects — field extractions, event types, tags, lookups, and saved searches — are the reusable layer on top of raw data, and most are applied at search time.
- Lookups enrich events with outside information, such as mapping a host to the team that owns it, without changing the stored data.
- Reports are saved searches with viewable results; dashboards are pages of panels, each driven by a search.
- An alert is a saved search plus a schedule, a trigger condition, and an action.
- Raw search is an individual skill; the saved knowledge layer is what makes a Splunk deployment valuable to an organization.
