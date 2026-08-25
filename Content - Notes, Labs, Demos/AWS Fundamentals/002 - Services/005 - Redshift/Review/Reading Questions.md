# Redshift — Reading Questions

Use these questions while you read the notes. Each one points at something worth understanding well enough to talk about on the job.

## What Is Redshift

1. What kind of workload is Redshift built for, and how does that differ from what RDS and DynamoDB are built for?
2. What is columnar storage, and why does it make Redshift so much better at analytical queries than a row-oriented database?
3. RDS can technically run analytical queries too — so why use a separate service like Redshift instead of just querying RDS directly?
4. Where does the data in Redshift usually come from, and how does that connect to the idea of ETL?

## Where It Fits vs RDS/DynamoDB

5. RDS and DynamoDB have very different data models, but they're both considered OLTP tools — what's the underlying similarity in the kind of question they're answering? How is Redshift answering a fundamentally different kind of question?
6. In a typical real-world architecture, how do RDS/DynamoDB and Redshift work together? Why keep heavy analytical queries off the operational database in the first place?
7. Given a new data requirement, what's the decision framework for choosing between DynamoDB, RDS, and Redshift?
