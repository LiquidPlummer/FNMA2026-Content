# Instance Profiles & VPC Placement

Two launch-time decisions that determine what an instance can do and who can reach it: the role it carries, and the subnet it sits in.

---

## Attaching an Instance Profile

An **instance profile** wraps an IAM role and attaches it to an instance, giving code on that instance temporary AWS credentials without any stored secret. The mechanism was covered in the IAM lesson; what matters here is applying it at launch.

```bash
aws ec2 run-instances \
  --image-id ami-0abc123 --instance-type t3.micro \
  --iam-instance-profile Name=app-server-profile \
  --subnet-id subnet-0private1 \
  --security-group-ids sg-0app123
```

*The instance profile is specified at launch; SDKs and the CLI on the instance then find credentials automatically with no configuration.*

Three practical points:

**The role can be changed on a running instance.** `associate-iam-instance-profile` and `replace-iam-instance-profile-association` work without a restart, though the application may cache credentials briefly.

**Permission changes apply immediately.** Editing the role's policies affects the running instance within moments — no restart, no redeploy. This is what makes revocation effective.

**An instance profile is not optional infrastructure.** An instance with no profile has no AWS access, so anything calling AWS fails with credential errors. Attaching one at launch is easier than diagnosing that later.

The rule from the IAM lesson applies directly: **an instance with an access key in a config file indicates a missing or misconfigured instance profile.**

---

## Subnet Placement

The subnet chosen at launch determines three things simultaneously:

**The Availability Zone.** Subnets are AZ-scoped, so choosing a subnet chooses a zone — which in turn determines which EBS volumes can attach and how the instance contributes to AZ resilience.

**Internet routing.** Whether the subnet's route table points `0.0.0.0/0` at an internet gateway (public) or a NAT gateway (private).

**The private IP range** the instance draws from.

None of these can be changed afterwards. **An instance cannot be moved to a different subnet.** Changing placement means launching a replacement — which is straightforward for a stateless instance behind an Auto Scaling group and awkward for a hand-built one.

---

## Public IPs, Precisely

A public IP does less than people expect. It makes an instance **addressable** from the internet; it does not make it **reachable**. Reachability requires all of:

1. A public IP or Elastic IP on the instance
2. A subnet whose route table reaches an internet gateway
3. Security group rules permitting the traffic
4. Network ACLs permitting it in both directions

Missing any one produces a connection that times out.

Equally, a public IP does not give the instance any additional capability — it can already reach the internet outbound through NAT without one.

Two behaviors worth remembering:

**Auto-assigned public IPs change.** They are released on stop and reassigned on start. Anything referencing the old address breaks. Elastic IPs are stable, and are billed hourly whether attached or not.

**Public IPv4 addresses now carry an hourly charge**, which makes assigning them by default a real cost across a large fleet.

---

## Where to Place Instances

The standard arrangement:

| Component | Subnet | Reasoning |
|---|---|---|
| Load balancers | Public | Must be internet-reachable |
| NAT gateways | Public | Need an internet gateway route |
| Application servers | Private | Reached only via the load balancer |
| Databases | Private | Reached only from the application tier |
| Bastion hosts | Public | If used at all — Session Manager removes the need |

**Application servers belong in private subnets.** They receive traffic through the load balancer, which is in a public subnet, and reach the internet outbound through NAT. There is no reason for them to be directly addressable, and being in a private subnet removes an entire category of exposure.

```
Internet ──► ALB (public subnet) ──► App instances (private subnets) ──► RDS (private)
                                            │
                                            └──► NAT gateway ──► Internet (outbound only)
```

*The application tier is unreachable from the internet while still able to reach out, which is what private subnets plus NAT provide.*

---

## Spreading Across AZs

A single instance in a single subnet is in a single AZ, and fails with it. Production capacity should span at least two — which means at least one private subnet per AZ, and an Auto Scaling group distributing instances across them.

That is the subject of the next lesson: a manually placed instance is a fixed capacity and a single point of failure, and Auto Scaling groups address both.

---

## Key Takeaways

- An instance profile attached at launch gives code on the instance temporary credentials with nothing stored.
- Roles can be attached or replaced on a running instance, and policy changes take effect immediately.
- The subnet chosen at launch fixes the Availability Zone, the internet routing, and the IP range, and cannot be changed afterwards.
- A public IP makes an instance addressable, not reachable — routing, security groups, and NACLs must also permit traffic.
- Auto-assigned public IPs change across a stop/start; Elastic IPs are stable and billed hourly even when unattached.
- Place load balancers and NAT gateways in public subnets and everything else in private subnets.
- Production capacity needs subnets in at least two Availability Zones.
