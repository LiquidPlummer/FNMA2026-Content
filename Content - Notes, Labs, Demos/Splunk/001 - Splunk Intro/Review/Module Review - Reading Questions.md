# Splunk Intro — Reading Questions

Use these while reading the notes. They follow the notes in order, so answers show up as we read rather than sending us hunting. Each one points at something worth understanding well enough to talk about on the job.

---

## 001 - What Splunk Is

1. What counts as machine data, and why does most of it go unread until something breaks?

2. Without a tool like Splunk, what makes it so difficult to answer a single question across a fleet of servers? What does centralizing the data change about that?

3. Splunk is described as a search engine rather than a database. Give two concrete differences between the two. Why is it reasonable that Splunk data is append-only — that we never update or delete individual records?

4. What's the difference between schema-on-write and schema-on-read? What does schema-on-read make possible that schema-on-write doesn't, and what does it cost us in exchange?

5. Splunk is used for IT operations, security, and compliance. Why does a single deployment often end up serving all three at once?

6. What is a SIEM, and what makes Splunk a natural fit for that role?

---

## 002 - How Data Gets In

7. Describe what each of the three roles — forwarder, indexer, search head — is responsible for, in a sentence each. What's different about a small deployment?

8. Walk through what happens to a single line written to a log file on a server, from the moment it's written until someone can search for it.

9. What is the Universal Forwarder, and why is it built to do so little? What does that limitation buy an organization?

10. Why do deployments split data across several indexes instead of putting everything in one?

11. What does a sourcetype tell Splunk, and what does Splunk use it to decide? What goes wrong when it's set incorrectly?

12. What's the difference between index time and search time? Of these four things, which could we still change a year after the data arrived: a field extraction, an event's sourcetype, a lookup, an event's timestamp?

---

## 003 - Searching with SPL

13. What is an event, and why does Splunk always keep the original raw text alongside whatever it pulls out of it?

14. Why is the timestamp the most important thing attached to an event? What effectively happens to an event that gets stamped with the wrong time?

15. What's the difference between default fields and extracted fields? Why can a new extracted field be defined today and still work on data that arrived years ago?

16. Why is searching for `status=500` better than searching for `500`?

17. Explain the pipeline model in your own words. For the three-stage example in the notes, say what each stage receives and what it hands on.

18. Why does narrowing a search in the first stage matter so much more than filtering later in the pipeline? What three things narrow a search the most?

19. What does each of the three command families — filtering, summarizing, shaping — do, and what's one command from each?

20. Why is `stats` the one command worth remembering? What does `stats count by host` produce, and what's its SQL equivalent?

---

## 004 - What You Build With It

21. Why is the saved search described as the building block for everything else in this topic?

22. What are knowledge objects, and what problem do they solve for an organization? Why does it matter that nearly all of them are applied at search time?

23. What does a lookup let us do that the log data alone can't? Give an example of a question that a lookup makes answerable.

24. What's the difference between a report and a dashboard? Why do most people in an organization experience Splunk through dashboards rather than by writing searches?

25. What are the four parts of an alert? Why is tuning one an ongoing job rather than a one-time setup?

26. Why is the layer of saved knowledge — extractions, lookups, dashboards, alerts — described as the real measure of a Splunk deployment, rather than the ability to write good searches?
