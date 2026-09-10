# VPC Endpoints

By default, an instance in a private subnet calling S3 sends that traffic out through a NAT gateway, across the public internet, and back into AWS. **VPC endpoints** keep it on the AWS network instead — which is both cheaper and more secure.

---

## The Default Path

S3, DynamoDB, SQS, and most AWS services have public endpoints. `s3.us-east-1.amazonaws.com` resolves to a public address, so calls to it are routed like any other internet traffic:

```
Private subnet instance ──► NAT gateway ──► IGW ──► internet ──► S3
```

*The default path leaves AWS's network and comes back, paying NAT processing charges each way.*

The traffic is encrypted with TLS, so this is not a confidentiality failure. But it costs NAT processing charges per gigabyte, it requires the NAT gateway to exist at all, and it means the instance's outbound access cannot be locked down without breaking AWS service calls.

---

## Gateway Endpoints

**Gateway endpoints** exist for exactly two services: **S3 and DynamoDB**. They work by adding a route to a route table.

```bash
aws ec2 create-vpc-endpoint \
  --vpc-id vpc-0abc123 \
  --service-name com.amazonaws.us-east-1.s3 \
  --route-table-ids rtb-0def456 rtb-0def789
```

*Adds a prefix-list route for S3 to the named route tables; traffic to S3 then goes directly rather than through NAT.*

Properties worth knowing:

- **Free.** No hourly charge, no per-gigabyte charge.
- **Route-based.** They add an entry to route tables and require no DNS change.
- **Region-scoped.** They reach S3 or DynamoDB in the same region only.
- **Not reachable from outside the VPC** — not over VPN or peering.

Because they are free and reduce NAT charges, an S3 gateway endpoint is close to a default for any VPC whose private subnets touch S3 at all.

---

## Interface Endpoints (PrivateLink)

**Interface endpoints** cover almost every other AWS service, plus services published by other accounts. They work differently: an elastic network interface with a private IP is created in each chosen subnet, and private DNS makes the service's normal hostname resolve to it.

```bash
aws ec2 create-vpc-endpoint \
  --vpc-id vpc-0abc123 \
  --vpc-endpoint-type Interface \
  --service-name com.amazonaws.us-east-1.secretsmanager \
  --subnet-ids subnet-0aaa subnet-0bbb \
  --security-group-ids sg-0ccc \
  --private-dns-enabled
```

*Creates an interface in two subnets. With `--private-dns-enabled`, `secretsmanager.us-east-1.amazonaws.com` resolves to the private interface, so no application change is needed.*

Properties:

- **Billed hourly per endpoint per AZ, plus per gigabyte.** Roughly $7–8/month per AZ before traffic. Enabling many endpoints across three AZs adds up.
- **Protected by security groups**, since they are network interfaces.
- **Reachable from on-premises** over VPN or Direct Connect, unlike gateway endpoints.
- **AZ-scoped**, so one per AZ is needed for resilience.

---

## Choosing

| | Gateway endpoint | Interface endpoint |
|---|---|---|
| Services | S3, DynamoDB only | Most AWS services, plus third-party |
| Cost | Free | Hourly + per GB |
| Mechanism | Route table entry | Network interface + private DNS |
| Security groups | Not applicable | Applicable |
| From on-premises | No | Yes |

The practical approach: enable gateway endpoints for S3 and DynamoDB always, since they cost nothing and save NAT charges. Add interface endpoints selectively, where the traffic volume justifies the hourly cost or where a security requirement rules out internet-path access.

---

## Endpoint Policies

Both types accept a policy restricting what can be reached through them:

```json
{
  "Statement": [{
    "Effect": "Allow",
    "Principal": "*",
    "Action": ["s3:GetObject", "s3:PutObject"],
    "Resource": "arn:aws:s3:::company-reports/*"
  }]
}
```

*Restricts this endpoint to one bucket. Combined with removing internet access from the subnet, it means the instances there can reach that bucket and nothing else in S3.*

This is a genuinely strong control. Endpoint policies plus a private subnet with no NAT gateway produce compute with no internet access at all, able to reach only specifically named AWS resources — which closes off data exfiltration paths that IAM alone does not.

---

## Key Takeaways

- Without an endpoint, calls to AWS services from private subnets traverse NAT and the public internet, incurring processing charges.
- Gateway endpoints serve S3 and DynamoDB only, work by adding route table entries, and are free.
- Interface endpoints (PrivateLink) serve most other services via a network interface and private DNS, billed hourly per AZ plus per gigabyte.
- Interface endpoints support security groups and are reachable from on-premises; gateway endpoints are neither.
- Enable S3 and DynamoDB gateway endpoints by default; add interface endpoints where volume or security requirements justify them.
- Endpoint policies restrict which resources are reachable, enabling subnets with no internet access that can still use specific AWS resources.
