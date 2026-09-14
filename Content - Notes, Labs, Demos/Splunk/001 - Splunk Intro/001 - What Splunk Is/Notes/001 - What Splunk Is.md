# What Splunk Is

**Splunk** is a platform for collecting, searching, and analyzing machine data. Organizations send it the records their servers, applications, network devices, and security tools produce, and Splunk makes all of it searchable in one place — then lets teams build reports, dashboards, and alerts on top.

---

## Machine Data

**Machine data** is what systems write down about themselves as they run. A web server records every request it handles. An application logs its errors and warnings. An operating system records logins and service restarts. A firewall records every connection it allows or blocks.

It comes in a few broad shapes:

- **Logs** — lines of text describing something that happened, almost always with a timestamp
- **Metrics** — numeric measurements taken at regular intervals, like CPU usage or response time
- **Events** — discrete occurrences worth recording, like a login, a deployment, or a configuration change

What these have in common is volume and neglect. Machine data piles up constantly — often gigabytes a day from a single system — and nobody reads it until something breaks. Then it's the only record of what actually happened.

---

## The Problem Splunk Solves

Machine data is scattered. The web server's logs live on the web server, the database's logs on the database host, the firewall's logs on the firewall — each in its own format, each with its own way of writing a timestamp. Answering one question, like *why did checkout fail for customers at 2:15 this afternoon?*, can mean logging into ten machines, finding the right files, and lining up timestamps by hand.

Scale that to hundreds or thousands of machines, add logs that rotate and get deleted on their own schedules, and manual investigation stops being slow and becomes impossible.

Splunk's answer is to centralize. It collects data from every source as it's written, stores it in one place, and makes it searchable together. The ten-machine investigation becomes one search.

---

## A Search Engine, Not a Database

It's tempting to think of Splunk as a database for logs. That picture will mislead us.

Splunk works more like a search engine. It takes in raw text, breaks it into individual records, and builds an index of the terms inside them, so it can find matching records across enormous volumes quickly. We don't query tables; we search for the records that match, then count, group, or chart what we found.

| | Relational database | Splunk |
|---|---|---|
| Basic unit | A row in a table | A timestamped record (an **event**) |
| Organized by | Tables and keys | Time |
| Structure | Defined before data goes in | Applied when data is searched |
| Changing data | Insert, update, delete | Append only — records aren't edited |
| Typical question | "What is the current state?" | "What happened, and when?" |

The append-only point is worth dwelling on. A log line is a record of something that happened, and that doesn't change after the fact. Splunk isn't where an application keeps its data — it's where we go to find out what the application did.

---

## Schema-on-Read

Most databases use **schema-on-write**: we define the structure first, and data must fit it before it's stored. A row with the wrong type in a column gets rejected.

Splunk uses **schema-on-read**. Data goes in as it arrives, in whatever format it's in. The structure — which part of a line is the status code, which part is the username — is worked out when we search.

That has two big payoffs:

- **Anything can come in.** A new application with an unfamiliar log format can start sending data today. Nobody has to design a table first.
- **Old data can answer new questions.** If we realize next month that some value buried in the logs matters, we can start pulling it out and searching on it immediately — across everything already stored, not just data arriving from now on.

The cost is that interpretation happens at search time, every time. Splunk doesn't force data into shape on the way in, so how useful the data is depends on the definitions teams build for it. That idea runs through every topic that follows.

---

## Where Splunk Shows Up

Splunk is used in three main areas:

- **IT operations and troubleshooting.** Finding why an application is failing or slow, tracing a problem across servers, spotting trends before they become outages.
- **Security monitoring.** Splunk is one of the most widely used **SIEM** (Security Information and Event Management) platforms. Security teams collect login activity, firewall traffic, and endpoint alerts into it to detect suspicious behavior and investigate incidents. Splunk sells a premium app built for this, **Splunk Enterprise Security (ES)**.
- **Compliance and auditing.** Regulations and standards like PCI DSS and HIPAA require organizations to keep logs for set periods and to show who accessed what. Splunk provides both the retention and a searchable record.

These overlap. The logs an operations team uses to debug an outage are the same logs a security team uses to investigate a breach, and one deployment often serves both.

---

## Names We'll Hear

- **Splunk Enterprise** — the software an organization installs and runs on its own infrastructure.
- **Splunk Cloud Platform** — the same core product, run by Splunk as a hosted service.
- **Cisco** — Splunk's parent company since 2024.
- **Elastic Stack (ELK)** — the most common comparison: Elasticsearch, Logstash, and Kibana, which tackle a similar problem. Most concepts in these notes carry over.

---

## Key Takeaways

- Machine data — logs, metrics, and events — is produced constantly, scattered across systems, and rarely read until something goes wrong.
- Splunk centralizes that data so one search can cover every system at once.
- Splunk is a search engine over timestamped, append-only records, not a database of tables.
- Schema-on-read means structure is applied at search time, so Splunk can take in any format and old data can answer new questions.
- Its three main uses are operations and troubleshooting, security monitoring (SIEM), and compliance.
