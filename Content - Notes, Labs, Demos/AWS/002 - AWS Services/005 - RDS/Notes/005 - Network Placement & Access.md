# Network Placement & Access

Where an RDS instance sits on the network, and what reaching it requires. Most of this is decided at creation and awkward to change afterwards.

---

## Subnet Groups

A **DB subnet group** is a named set of subnets across at least two Availability Zones. RDS places the instance in one of them, and Multi-AZ places the standby in another.

```bash
aws rds create-db-subnet-group \
  --db-subnet-group-name app-db-private \
  --db-subnet-group-description "Private subnets for application databases" \
  --subnet-ids subnet-0priv-a subnet-0priv-b subnet-0priv-c
```

*Three private subnets across three AZs. The group must span at least two AZs even for a single-AZ instance, so Multi-AZ can be enabled later.*

**Use private subnets.** A database in a public subnet is one security group mistake away from the internet, and there is no benefit — applications reach it from within the VPC.

The subnet group is chosen at creation and determines which AZs the instance can occupy.

---

## Public Accessibility

The `PubliclyAccessible` setting controls whether the instance gets a public IP and a publicly resolvable endpoint.

**Set it to false.** A publicly accessible database is exposed to continuous automated scanning, and even with a restrictive security group it is an unnecessary attack surface.

The setting can be changed after creation, though it is far simpler to get right initially.

An important detail: the RDS endpoint DNS name resolves differently depending on where the query comes from. From inside the VPC it resolves to the private address; from outside, if the instance is publicly accessible, it resolves to the public one. A connection that works from a laptop and fails from an instance — or the reverse — is often this.

---

## Security Groups

An RDS instance has a security group like any other resource. The rule follows the tiered pattern:

```
DB security group (sg-db)
  inbound: TCP 5432 from sg-app
```

*Only instances in the application's security group can connect. No CIDR ranges, so the rule survives instance replacement.*

Referencing the application's security group rather than a subnet CIDR is what keeps this correct as the application fleet scales.

---

## Reaching It From Outside the VPC

Developers often need to query a production database, and it sits in a private subnet. The options, best first:

**Session Manager port forwarding.** Forward a local port through an EC2 instance to the database, with no open ports and no bastion:

```bash
aws ssm start-session --target i-0bastion \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{"host":["orders.abc123.us-east-1.rds.amazonaws.com"],"portNumber":["5432"],"localPortNumber":["5432"]}'
```

*Connects a local port to the database through an instance in the VPC. The database stays private, nothing is exposed, and the session is logged in CloudTrail.*

**VPN or Direct Connect**, where corporate network connectivity to the VPC already exists.

**A bastion host with SSH tunnelling** — the traditional approach, requiring a host to maintain and patch.

**Making the database public** — not an option worth considering.

---

## RDS Proxy

Connection handling deserves attention, because it is where serverless and RDS meet badly.

Each database connection consumes memory, and `max_connections` scales with instance size. A Lambda function scaling to 500 concurrent executions, each opening a connection, exhausts a small instance's connection limit immediately.

**RDS Proxy** sits between applications and the database, pooling and reusing connections:

```
Lambda × 500  ──►  RDS Proxy  ──►  RDS (20 actual connections)
```

*The proxy multiplexes many client connections onto few database connections, which is what makes Lambda and RDS work together.*

It also provides faster failover — the proxy holds client connections open while the database fails over — and integrates with Secrets Manager and IAM authentication.

It is billed per vCPU of the database instance, which is a real cost, and it adds a small amount of latency. For Lambda-driven or high-concurrency workloads it is usually worth it.

---

## IAM Database Authentication

RDS supports authenticating with IAM instead of a password. The client requests a token valid for 15 minutes and uses it as the password:

```bash
TOKEN=$(aws rds generate-db-auth-token \
  --hostname orders.abc123.us-east-1.rds.amazonaws.com \
  --port 5432 --username app_user)
```

*Generates a short-lived token from IAM credentials, so no database password is stored anywhere.*

The appeal is that access follows IAM — no password to rotate, and revocation is a policy change. The constraints are a connection-rate limit that makes it unsuitable for very high connection churn, and the need for the database user to be configured for IAM authentication. It works well combined with RDS Proxy, which handles the connection pooling.

---

## Key Takeaways

- A DB subnet group spans at least two AZs and determines where the instance and its standby are placed.
- Place databases in private subnets and set `PubliclyAccessible` to false.
- The RDS endpoint resolves to a private or public address depending on where the DNS query originates.
- Reference the application's security group as the source rather than a CIDR range.
- Use Session Manager port forwarding to reach a private database, rather than a bastion or a public endpoint.
- RDS Proxy pools connections, which is what makes high-concurrency and Lambda workloads viable against RDS.
- IAM database authentication issues 15-minute tokens instead of passwords, with a connection-rate limit to consider.
