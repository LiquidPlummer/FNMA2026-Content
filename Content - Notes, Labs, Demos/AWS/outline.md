# AWS — Curriculum Outline

Subject: **AWS**. Two units; topic numbering restarts inside each unit. Bullets under each topic are what that topic's content should cover.

AWS/
├── 001 - AWS Fundamentals/
└── 002 - AWS Services/

**Modality:** notes only. No labs are planned for either unit at this time; topics carry coverage bullets and nothing else.

**Standing constraint:** no marketing copy. No "fully managed, highly available, cost-effective" framing, no service-benefit lists, no comparisons written to sell. Every bullet is a mechanism, a constraint, a cost, or a decision a practitioner makes.

---

# 001 - AWS Fundamentals

## 001 - Intro to AWS
- The standard pitch, stated once: global reach, pay-as-you-go billing, no up-front capital expense, elastic capacity, high availability, managed services that remove operational work
- Where the pitch is accurate: capacity really is elastic, and there really is no hardware to buy
- Where it is conditional: high availability is something you architect and pay for, not something you receive; pay-as-you-go can cost more than owning hardware at steady load
- What AWS is structurally — a very large number of independent services sharing an account, an API, and a permissions system
- **This is the last time this material uses marketing language.** Everything after this topic describes mechanisms, constraints, costs, and decisions.

## 002 - The AWS Platform & Shared Responsibility Model
- What a cloud provider actually rents you: someone else's hardware, on demand, billed by use
- The shared responsibility model — where AWS's job ends and yours begins
- How the line moves by service type: EC2 vs. RDS vs. Lambda give you progressively less to manage and less to control
- Managed does not mean secure — what remains yours in every case
- The account as the unit of isolation and billing
- Service quotas exist and will stop you before you expect them to

## 003 - Regions & Availability Zones
- Regions as independent deployments; resources are region-scoped and most don't cross
- Availability Zones as separate failure domains within a region
- What "multi-AZ" actually buys and what it costs
- Choosing a region: latency, data residency, service availability, price differences
- Cross-region and cross-AZ data transfer as a line item
- The handful of global services and why they're the exception

## 004 - Accessing AWS — Console, CLI, SDK, API
- Everything is the same API underneath; console, CLI and SDK are three clients for it
- Installing and configuring the CLI; profiles and the credentials file
- Where credentials come from and the order they're resolved
- Region and output configuration
- Reading the CLI reference and mapping a console action to its API call
- Why console-only work doesn't reproduce and can't be reviewed

## 005 - IAM — Identities, Roles & Policies
- The four things: users, groups, roles, policies
- Policy documents — effect, action, resource, condition
- Identity-based vs. resource-based policies
- How an authorization decision is actually made; explicit deny wins
- Roles and assumed credentials — why a role beats a long-lived access key
- Instance profiles and service roles: how a resource gets permissions without a password
- Least privilege in practice, and why starting broad and narrowing rarely happens
- Root account handling and MFA

## 006 - Encryption & Auditing — KMS and CloudTrail
- Encryption at rest vs. in transit — different mechanisms, different failure modes
- KMS keys: AWS-managed vs. customer-managed, and what the difference actually buys
- Key policies as a second authorization layer alongside IAM
- Envelope encryption — why services don't ship bulk data to KMS
- Enabling encryption on S3, EBS and RDS; where the setting lives and whether it can be changed later
- CloudTrail as the record of every API call made in the account
- CloudTrail vs. CloudWatch Logs — audit trail vs. application output
- Trail configuration, log storage, and retention cost
- Answering "who deleted this resource"

## 007 - VPCs & Subnets
- The VPC as a private network you define inside a region
- CIDR blocks and sizing a network you can't easily resize later
- Subnets are AZ-scoped; public vs. private is a routing outcome, not a setting
- Route tables and the internet gateway
- NAT gateways — outbound access for private subnets, and what they cost
- Public IPs, private IPs, and elastic IPs
- VPC endpoints for reaching AWS services without traversing the internet

## 008 - Security Groups & Network Access
- Security groups as stateful, instance-level firewalls
- Allow-only rules — there is no deny
- Referencing another security group as a source, and why that beats CIDR ranges
- Network ACLs: stateless, subnet-level, and evaluated in order
- How the two layers combine on a single packet
- Diagnosing "I can't connect" — the ordered list of things to check
- Default security group behavior and why it surprises people

## 009 - Tagging & Resource Organization
- Tags as the only structure AWS gives you across services
- What tags are used for: cost allocation, automation targeting, access control
- Designing a tag scheme before resources exist, not after
- Tag-based IAM conditions
- Untagged resources as the default failure mode

## 010 - CloudWatch — Logs, Metrics & Alarms
- The three things CloudWatch holds: metrics, logs, events
- Log groups and log streams; how services get their logs there
- Which metrics you get for free and which require an agent
- Custom metrics and why you'd publish one
- Alarms — thresholds, states, and what an alarm can trigger
- Log retention defaults to forever, and what that costs
- Reading CloudWatch Logs Insights well enough to find one request

## 011 - The Cost Model
- What you're billed for: compute time, storage, requests, data transfer
- Data transfer as the cost that surprises people — out to internet, cross-AZ, cross-region
- On-demand vs. reserved vs. spot as a decision, not a discount table
- Free tier: what's actually free, for how long, and what silently isn't
- Reading Cost Explorer and attributing spend with tags
- Budgets and alerts before the bill, not after
- The resources that cost money while doing nothing

---

# 002 - AWS Services

## 001 - EC2
- Instances, AMIs, and instance types — what the family/size naming encodes
- The lifecycle: launch, stop, terminate, and what survives each
- EBS volumes, snapshots, and root volume persistence
- Instance store vs. EBS
- Key pairs and SSH access; Session Manager as the alternative
- User data for bootstrapping
- Attaching an instance profile so the instance can call AWS
- Placement in a VPC subnet, and what a public IP does and doesn't get you

## 002 - Load Balancing & Auto Scaling
- Why one instance is both a single point of failure and a fixed capacity ceiling
- ALB: listeners, target groups, health checks, path- and host-based routing
- ALB vs. NLB — layer 7 vs. layer 4, and when the difference matters
- Health check configuration, and how a bad one takes a working app offline
- Launch templates as the definition of what gets launched
- Auto Scaling groups: desired, minimum, maximum, and automatic replacement of failed instances
- Scaling policies — target tracking vs. step scaling
- Spreading a group across AZs
- Why instances behind a group must be stateless and disposable
- Connection draining and lifecycle hooks

## 003 - S3
- Buckets, objects, keys — and why it is not a filesystem
- Global namespace and bucket naming
- Storage classes and the retrieval trade-off
- Lifecycle rules for transition and expiration
- Versioning, and how it interacts with deletion
- Access control: bucket policies, block public access, presigned URLs
- Consistency behavior
- Static website hosting as a mechanism
- Request costs and transfer costs as distinct from storage costs

## 004 - CloudFront & Route 53
- Route 53 as DNS: hosted zones, record types, TTL
- Alias records and why they exist for AWS targets
- Routing policies: simple, weighted, latency, failover
- Health checks driving DNS failover
- CloudFront as a cache sitting in front of an origin
- Origins: S3, ALB, API Gateway
- Cache behaviors, TTLs, and invalidation
- Origin access control for keeping an S3 bucket private while serving it publicly
- HTTPS and ACM certificates, including the region constraint
- How CloudFront changes the data transfer cost picture

## 005 - RDS
- What RDS manages and what it doesn't
- Engine choice; parameter groups and where engine config actually lives
- Instance sizing and storage types
- Multi-AZ as failover, read replicas as scale — different problems
- Automated backups, snapshots, and point-in-time restore
- Placing a database in a private subnet and reaching it
- Subnet groups and security group rules for database access
- Credential handling and rotation
- Maintenance windows and forced version upgrades

## 006 - DynamoDB
- Key-value and document model; tables, items, attributes
- Partition key and sort key — how data is physically distributed
- Why query patterns are designed before the table
- Query vs. scan, and why scan is usually wrong
- Secondary indexes: local and global
- Capacity modes: on-demand vs. provisioned
- Consistency options on a read
- Item size limits and how they shape design
- When DynamoDB is the wrong choice

## 007 - Secrets Manager & Parameter Store
- The problem: credentials that have to exist somewhere, but not in code, not in environment variables, not in a repository
- Parameter Store — plain and secure string parameters, hierarchical naming
- Secrets Manager — versioned secrets and built-in rotation
- Choosing between them on cost, rotation, and size limits
- Retrieving a secret at runtime, and caching so every invocation isn't another call
- IAM permission to read the secret, KMS permission to decrypt it
- Managed rotation for RDS credentials
- What still leaks: logs, error messages, process listings

## 008 - Lambda
- The execution model: event in, function runs, environment may be reused
- Handler signature, event object, context
- Cold starts — what causes them and what actually reduces them
- Memory as the setting that also controls CPU
- Timeouts, retries, and which invocation types retry
- Execution roles and permissions
- Environment variables, and pulling secrets from Secrets Manager instead
- Packaging: zip, layers, container images
- Concurrency limits and throttling
- Reading a Lambda's logs in CloudWatch

## 009 - API Gateway
- What sits between a client and a backend, and why not expose Lambda directly
- REST vs. HTTP APIs — the actual differences
- Resources, methods, and stages
- Lambda proxy integration and the request/response shape it imposes
- Request validation and mapping
- Authorizers: IAM, Cognito, Lambda
- Throttling, usage plans, and API keys
- CORS, and why it fails the way it does
- Custom domains

## 010 - Cognito
- The two halves: user pools for authentication, identity pools for AWS credentials
- Sign-up and sign-in flows; the user attributes you can't change after the pool exists
- Tokens issued — ID, access, refresh — and what each is for
- JWT validation, and whose job it is
- Hosted UI versus building the flows yourself
- Wiring a user pool authorizer into API Gateway
- Federation with external identity providers
- Groups, and mapping them to IAM roles
- Where Cognito is the wrong tool

## 011 - SQS
- Queues as decoupling — producer and consumer don't run at the same time
- Standard vs. FIFO: ordering and duplication guarantees
- Visibility timeout and why it must exceed processing time
- Polling: short vs. long, and the cost difference
- Dead-letter queues and redrive
- At-least-once delivery, and designing consumers to be idempotent
- Triggering Lambda from a queue; batch size and partial failures
- Message size limits

## 012 - SNS
- Publish/subscribe — one message, many subscribers
- Topics, subscriptions, and supported protocols
- Fanout to SQS, and why that pattern exists
- Message filtering by attribute
- Delivery retries and failure handling
- FIFO topics

## 013 - EventBridge
- Event buses, rules, and targets
- Event pattern matching — filtering before delivery rather than after
- The default bus, and what AWS already publishes to it
- Scheduled rules as cron
- Custom buses for application events
- Delivery guarantees, retries, and dead-letter configuration
- Choosing among SQS, SNS and EventBridge by what the consumer needs
- Archive and replay

## 014 - Redshift
- Columnar storage and why it changes which queries are fast
- Cluster architecture: leader and compute nodes; serverless as the alternative
- Distribution styles and sort keys — the two decisions that determine performance
- Loading data from S3 with COPY
- Redshift Spectrum for querying S3 in place
- Why it is not a transactional database
- Concurrency, workload management, and pausing to control cost

## 015 - Bedrock
- Model access as an API call; no infrastructure to run
- Available model families and what selection depends on
- Invoking a model: prompt, parameters, streaming vs. buffered response
- Token-based pricing and how cost is estimated
- Model access requests and region availability
- Knowledge bases and retrieval-augmented generation
- Guardrails
- IAM permissions for model invocation
- Where data goes and what is retained

## 016 - CloudFormation
- Declarative infrastructure: describe the end state, let the service reconcile
- Template anatomy: resources, parameters, outputs, mappings, conditions
- Stacks as the unit of creation, update, and deletion
- Change sets — seeing what an update will do before it does it
- Rollback behavior on failure
- Intrinsic functions: Ref, GetAtt, Sub
- Cross-stack references
- Drift detection when someone edits in the console
- Deletion policies and what a stack delete destroys
- Where CDK and Terraform sit relative to this

---

## Sequencing Notes

- **Topic 001 of Fundamentals quarantines the marketing.** The vendor framing is stated once, qualified, and explicitly closed off so no later topic reaches for it.
- **IAM is in Fundamentals, not Services**, even though it is a service. Every topic in the Services unit assumes a role or a policy already exists.
- **KMS and CloudTrail sit directly after IAM** — both are authorization and accountability layers, and every later service topic assumes encryption and audit are available.
- **CloudWatch and cost are Fundamentals** for the same reason: every service topic assumes learners can find logs and know what a resource is costing them.
- **VPC before security groups** — public vs. private subnets and routing have to exist before instance-level rules make sense.
- **EC2 opens the Services unit**, with load balancing and auto scaling immediately after, so the elasticity claimed in Fundamentals 001 is delivered rather than left as a slogan.
- **CloudFront and Route 53 follow S3** because static hosting is the first origin learners meet, but the topic also carries the custom-domain material API Gateway needs later.
- **RDS before DynamoDB** so the relational default is the baseline the key-value model is contrasted against.
- **Secrets Manager precedes Lambda** so that Lambda's "not in environment variables" rule has somewhere to point.
- **Lambda before API Gateway before Cognito** — each is the backend the next one fronts.
- **SQS, SNS, EventBridge run together** as one messaging arc; the EventBridge topic closes it with the three-way choice.
- **CloudFormation is last**, not in Fundamentals. It is cross-cutting by nature, but a template is unwritable until the resources it provisions are familiar.