# Splunk — Quiz Review

This page reviews the Splunk ideas the quiz checks: what Splunk is used for, what the sourcetype tells Splunk about an event, and the commands in Splunk's search language (SPL). Each section has a short summary, a few questions to test ourselves with, and links to the notes that explain the idea in full.

This is a study guide, not an answer key. The answers to the check-yourself questions are in the linked notes.

---

## What Splunk Is Used For

Splunk is a platform for **collecting, searching, and analyzing machine data**. Machine data is what servers, applications, network devices, and security tools write about themselves as they run: logs, metrics, and events.

Machine data is produced constantly, spread across many machines, and written in many different formats. Usually nobody reads it until something breaks. Splunk **centralizes** it, so one search can cover every system at once. Teams then build reports, dashboards, and alerts on top of their searches.

Two ideas set Splunk apart from a database:

- It works like a **search engine** over timestamped records (events) that are only ever appended to. It isn't a set of tables that get updated.
- It's **schema-on-read**. Data goes in as it arrives, and structure is applied when we search it. That means any format can come in, and old data can answer new questions.

Splunk is used in three main areas:

- **IT operations**: troubleshooting failures and slowdowns across many servers.
- **Security monitoring**: as a SIEM (Security Information and Event Management) platform, detecting and investigating suspicious activity.
- **Compliance**: keeping logs for required periods and showing who accessed what.

**Check yourself**
- Without a tool like Splunk, why is it so hard to answer "Why did checkout fail at 2:15 this afternoon?"
- Give two differences between Splunk and a relational database.
- What does schema-on-read make possible, and what does it cost?

**Go deeper**
- What Splunk Is: [Machine Data](../Splunk/001%20-%20Splunk%20Intro/001%20-%20What%20Splunk%20Is/Notes/001%20-%20What%20Splunk%20Is.md#machine-data), [The Problem Splunk Solves](../Splunk/001%20-%20Splunk%20Intro/001%20-%20What%20Splunk%20Is/Notes/001%20-%20What%20Splunk%20Is.md#the-problem-splunk-solves), [A Search Engine, Not a Database](../Splunk/001%20-%20Splunk%20Intro/001%20-%20What%20Splunk%20Is/Notes/001%20-%20What%20Splunk%20Is.md#a-search-engine-not-a-database), [Where Splunk Shows Up](../Splunk/001%20-%20Splunk%20Intro/001%20-%20What%20Splunk%20Is/Notes/001%20-%20What%20Splunk%20Is.md#where-splunk-shows-up)
- [What You Build With It](../Splunk/001%20-%20Splunk%20Intro/004%20-%20What%20You%20Build%20With%20It/Notes/001%20-%20What%20You%20Build%20With%20It.md) covers reports, dashboards, and alerts

---

## host, source, and sourcetype

Every event is labeled with three pieces of metadata about where it came from. They're easy to mix up:

| Field | What it describes | Example |
|---|---|---|
| `host` | The machine the event came from | `web-01` |
| `source` | The file or input the event came from | `/var/log/nginx/access.log` |
| `sourcetype` | What **kind** of data it is: its format | `access_combined`, `syslog` |

The sourcetype matters most, because Splunk uses it to decide how to read the data:

- where one event ends and the next begins (for example, keeping a multi-line stack trace together as one event),
- where the timestamp is and how to read it,
- which fields to extract when we search.

The sourcetype is assigned at **index time**, when the data is written, so it can't be changed afterward. A wrong sourcetype shows up as events chopped into pieces, events filed under the wrong time, and fields that never appear.

**Check yourself**
- Two servers write the same kind of log to the same file path. Which of the three fields is different on their events?
- A Java stack trace shows up in Splunk as forty separate events. Which of the three fields is the likely culprit, and why?
- Why is it so much more expensive to get an index-time decision wrong than a search-time one?

**Go deeper**
- How Data Gets In: [Sourcetype](../Splunk/001%20-%20Splunk%20Intro/002%20-%20How%20Data%20Gets%20In/Notes/001%20-%20How%20Data%20Gets%20In.md#sourcetype), [Index Time vs. Search Time](../Splunk/001%20-%20Splunk%20Intro/002%20-%20How%20Data%20Gets%20In/Notes/001%20-%20How%20Data%20Gets%20In.md#index-time-vs-search-time)
- Searching with SPL: [Default Fields](../Splunk/001%20-%20Splunk%20Intro/003%20-%20Searching%20with%20SPL/Notes/001%20-%20Searching%20with%20SPL.md#default-fields)

---

## SPL: The Search Processing Language

An SPL search is a **pipeline**. It's made of stages separated by the pipe character, `|`, and each stage works on the output of the stage before it, like pipes in a Unix shell:

```spl
index=web sourcetype=access_combined status>=500 earliest=-24h
| stats count by host
| sort -count
```

- The **first stage** retrieves events from the indexes. Narrowing it by time range, index, and sourcetype is what makes a search fast.
- Each later stage filters, summarizes, or reshapes what the stage before it returned.

The commands the notes cover fall into three families:

| Family | Commands |
|---|---|
| Filtering | `search`, `where` |
| Summarizing (also called transforming) | `stats`, `top`, `timechart` |
| Shaping | `eval`, `table`, `sort`, `rename` |

SPL does some of the same jobs as SQL, but it isn't SQL. It has its own command names and its pipe syntax. For example, `stats count by host` does the work that `SELECT host, COUNT(*) ... GROUP BY host` does in SQL. SPL has well over a hundred commands in total. For the quiz, know these nine and what each one does.

**Check yourself**
- For each stage of the search above, what goes in and what comes out?
- What's the difference between `where` and `eval`?
- Which commands turn a list of events into a table of results, and why does that matter for dashboards?
- Why is searching for `status=500` more precise than searching for `500`?

**Go deeper**
- Searching with SPL: [Fields](../Splunk/001%20-%20Splunk%20Intro/003%20-%20Searching%20with%20SPL/Notes/001%20-%20Searching%20with%20SPL.md#fields), [The Pipeline Model](../Splunk/001%20-%20Splunk%20Intro/003%20-%20Searching%20with%20SPL/Notes/001%20-%20Searching%20with%20SPL.md#the-pipeline-model), [Commands Worth Recognizing](../Splunk/001%20-%20Splunk%20Intro/003%20-%20Searching%20with%20SPL/Notes/001%20-%20Searching%20with%20SPL.md#commands-worth-recognizing), [stats: The One to Remember](../Splunk/001%20-%20Splunk%20Intro/003%20-%20Searching%20with%20SPL/Notes/001%20-%20Searching%20with%20SPL.md#stats-the-one-to-remember)

---

## More Practice

The [Splunk Module Review Reading Questions](../Splunk/001%20-%20Splunk%20Intro/Review/Module%20Review%20-%20Reading%20Questions.md) follow the notes in order and go into more depth on every topic.
