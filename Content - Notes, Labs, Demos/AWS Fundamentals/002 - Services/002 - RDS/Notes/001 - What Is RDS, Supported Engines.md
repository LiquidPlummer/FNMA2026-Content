# What Is RDS, Supported Engines

**RDS** (Relational Database Service) is AWS's managed service for running relational databases. Rather than installing and operating database software ourselves on an EC2 instance, RDS handles the operational load — provisioning, patching, backups, and (if we choose) failover — while we interact with the database mostly the same way we would with any relational database.

## What "Managed" Means Here

This is a direct, concrete example of the PaaS concept from earlier in this course: RDS sits above raw infrastructure (EC2) and below a fully custom application, managing the database engine on our behalf. Specifically, RDS takes care of:

- **Provisioning** the underlying server and storage for the database.
- **Installing and patching** the database engine software and, typically, the underlying operating system.
- **Automated backups**, including the ability to restore to a specific point in time.
- **Multi-AZ failover**, if configured, maintaining a synchronized standby copy in a second Availability Zone (directly connecting back to the Designing for AZ Failure topic).
- **Monitoring and metrics** for database health and performance.

What RDS does *not* take off our plate: schema design, query performance, the data itself, and access configuration — all squarely inside our side of the shared responsibility line, consistent with what we covered in that lesson.

## Supported Engines

RDS isn't one database technology — it's a managed wrapper around several popular relational **engines**, letting us pick the one that fits our needs (or that a team already has expertise in) while still getting RDS's managed operational benefits. The commonly supported engines include:

- **MySQL** and **MariaDB** — widely used open-source relational databases.
- **PostgreSQL** — a feature-rich open-source database, popular for its extensibility and standards compliance.
- **Microsoft SQL Server** — a common choice in organizations already invested in the Microsoft ecosystem.
- **Oracle Database** — common in enterprises with existing Oracle licensing and workloads.
- **Amazon Aurora** — AWS's own MySQL- and PostgreSQL-compatible engine, built specifically for the cloud with additional performance and availability features layered on top of the open-source engines it's compatible with.

Because RDS runs genuine, largely unmodified versions of these engines (Aurora being the interesting exception, as a purpose-built variant), applications and tools written for MySQL, PostgreSQL, and the rest generally work against RDS with little to no change — we're not learning a new database language, just a new way of operating an existing one.

## Why Choose RDS Over Installing a Database on EC2

It's entirely possible to install MySQL or PostgreSQL directly on an EC2 instance and manage it ourselves — nothing stops us. RDS exists because most of the operational work involved (patching, backup scheduling, failover configuration, monitoring) is repetitive and well-understood enough that AWS can automate it reliably. Choosing RDS over a self-managed database on EC2 is really a specific instance of the IaaS-vs-PaaS trade-off from earlier: less control over the underlying server, in exchange for AWS absorbing a large share of the ongoing operational burden. Whether that trade is worth it — and when it isn't — is exactly what the next topic digs into.
