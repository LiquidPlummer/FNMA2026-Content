# AWS Fundamentals — Course Outline

`Course: AWS Fundamentals`

General NoSQL theory (what NoSQL is, NoSQL vs SQL) is left out — not AWS-specific, moves into other coursework later. DynamoDB itself stays, since it's an AWS service.

## 001 - Cloud & AWS Foundations

- 001 - What Is Cloud Computing
  - 001 - Cloud vs Traditional On-Prem Infrastructure
  - 002 - Core Characteristics (On-Demand, Pay-as-You-Go, Elastic)
- 002 - Benefits of Cloud (Scalability, High Availability, Elasticity)
  - 001 - Scalability (Vertical vs Horizontal)
  - 002 - High Availability & Fault Tolerance
  - 003 - Cost Efficiency (CapEx vs OpEx)
- 003 - AWS Global Infrastructure
  - 001 - What a Region Is, How to Choose One
  - 002 - Region-Scoped vs Global Services
  - 003 - What an AZ Is, Relationship to Regions
  - 004 - Designing for AZ Failure
- 004 - Service Models & Shared Responsibility
  - 001 - Defining IaaS, PaaS, and SaaS
  - 002 - What AWS Manages vs What the Customer Manages
  - 003 - How Responsibility Shifts Across Service Types
- 005 - Pricing & Cost Management
  - 001 - AWS Free Tier
  - 002 - Pricing Models Overview
  - 003 - Billing & Budgets
  - 004 - Cost Explorer Basics
- 006 - AWS Management Console
  - 001 - Navigating the Console (Services Search, Regions, Resource Groups)
  - 002 - Guided Walkthrough: Exploring the S3 Console (Buckets, Objects, Permissions Tabs)

## 002 - Services

Brief, one-lesson-per-service survey. Deeper dives (DynamoDB, Redshift) come later as their own modules.

- 001 - S3
  - 001 - What Is S3, Buckets & Objects
  - 002 - Common Use Cases (Storage, Hosting, Backups)
- 002 - RDS
  - 001 - What Is RDS, Supported Engines
  - 002 - When to Use RDS vs Self-Managed Databases
- 003 - EC2
  - 001 - What Is EC2, Instance Types
  - 002 - Basic Provisioning Concepts
- 004 - DynamoDB
  - 001 - What Is DynamoDB
  - 002 - Where It Fits vs RDS
- 005 - Redshift
  - 001 - What Is Redshift
  - 002 - Where It Fits vs RDS/DynamoDB
- 006 - IAM
  - 001 - Users, Roles, Policies
  - 002 - Least Privilege Basics
- 007 - VPC
  - 001 - What Is a VPC, Subnets
  - 002 - Inbound vs Outbound Firewall Rules
  - 003 - Security Groups vs NACLs (Stateful vs Stateless)
- 008 - Lambda
  - 001 - What Is Serverless / Lambda
  - 002 - Common Triggers & Use Cases

---

Items above are listed as Topics per Lesson. Numbering restarts at 001 within each Module (Lessons) and within each Lesson (Topics), matching the `notes`/`labs`/`navigation` numbering convention.