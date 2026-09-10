# Access — Key Pairs & Session Manager

There are two ways to get a shell on an EC2 instance. The traditional one requires an open port and a private key; the modern one requires neither, and is generally the better choice.

---

## Key Pairs and SSH

A **key pair** is an SSH public/private key. AWS stores the public key and places it in the instance's `~/.ssh/authorized_keys` at first boot; we keep the private key.

```bash
aws ec2 create-key-pair --key-name training-key \
  --query "KeyMaterial" --output text > training-key.pem
chmod 400 training-key.pem
ssh -i training-key.pem ec2-user@203.0.113.42
```

*The private key is shown once, at creation, and cannot be retrieved afterwards. `chmod 400` is required — SSH refuses keys with permissive file modes.*

The default username varies by AMI: `ec2-user` on Amazon Linux, `ubuntu` on Ubuntu, `admin` on Debian.

### What SSH access requires

- A public IP or a network path from wherever we are connecting
- A security group allowing inbound port 22 from our address
- The private key file
- A subnet with a route to reach it

### Why this is awkward

**The key is a long-lived secret.** It does not expire, and it is typically copied between machines, shared within teams, and occasionally committed to repositories. Rotating it means changing `authorized_keys` on every instance.

**Port 22 has to be open to something.** Even restricted to an office range, it is an inbound path from outside. Opening it to `0.0.0.0/0` — which happens regularly — invites continuous automated attacks.

**There is no audit trail.** SSH sessions are not recorded in CloudTrail. Who connected, when, and what they ran is not visible from AWS.

**Access is not tied to IAM.** Someone who leaves the organization keeps a working key until it is removed from every instance.

**Private instances need a bastion host.** Reaching an instance with no public IP requires running, patching, and securing a jump host — infrastructure that exists only to enable SSH.

---

## Session Manager

**Session Manager**, part of AWS Systems Manager, provides shell access without any of that. The **SSM Agent** on the instance makes an *outbound* connection to the Systems Manager service, and sessions are delivered over it.

```bash
aws ssm start-session --target i-0abc123
```

*Opens a shell with no key, no open port, and no public IP required.*

The consequences are substantial:

**No inbound ports.** Security groups can have zero inbound rules. The connection is outbound from the instance.

**No public IP needed.** Instances in private subnets are reachable directly, so no bastion host is required.

**No keys to manage.** Nothing to distribute, rotate, or leak.

**Access is IAM-controlled.** Permission to start a session is an IAM policy, so removing someone's access removes it everywhere at once.

**Sessions are logged.** Start and end are recorded in CloudTrail, and full session transcripts can be sent to S3 or CloudWatch Logs — including every command typed.

### What it requires

- The **SSM Agent** installed, which is preinstalled on Amazon Linux 2023, recent Ubuntu, and Windows Server AMIs.
- An **instance profile** with `AmazonSSMManagedInstanceCore`.
- **Network access to the SSM endpoints** — either via NAT, or via interface VPC endpoints for `ssm`, `ssmmessages`, and `ec2messages`. The endpoint route is what allows a subnet with no internet access at all to still support sessions.

---

## Comparing

| | SSH | Session Manager |
|---|---|---|
| Inbound port | 22 open | None |
| Public IP | Usually required | Not required |
| Credentials | Long-lived private key | IAM |
| Audit | None in AWS | CloudTrail, plus session transcripts |
| Revocation | Edit every instance | Change one IAM policy |
| Private subnets | Needs a bastion | Direct |
| Prerequisites | Key pair | Agent, instance profile, endpoint access |

---

## Practical Guidance

**Use Session Manager by default.** It removes the open port, the key management, and the bastion host, and adds an audit trail.

**Enable session logging** to S3 or CloudWatch Logs, so there is a record of what was done on the instance.

**Port forwarding is supported**, which covers the common reason people keep SSH — reaching a database or a web UI on a private instance:

```bash
aws ssm start-session --target i-0abc123 \
  --document-name AWS-StartPortForwardingSession \
  --parameters '{"portNumber":["5432"],"localPortNumber":["5432"]}'
```

*Forwards a local port to a port on the instance, without opening anything to the network.*

**Keep a key pair for emergencies** if the SSM agent's failure would leave no way in — but keep port 22 closed, opening it only if that emergency occurs.

The broader point: needing a shell on an instance at all is worth noticing. Instances behind Auto Scaling groups should be replaceable rather than repaired, and frequent interactive access usually indicates that configuration lives on the instance rather than in an image or a template.

---

## Key Takeaways

- Key pair SSH requires an open port 22, a network path, and a long-lived private key with no AWS audit trail.
- The private key is shown only at creation and cannot be retrieved later.
- Session Manager works over an outbound connection from the SSM agent, needing no inbound port and no public IP.
- Access is governed by IAM, so revocation is a single policy change rather than editing every instance.
- Sessions appear in CloudTrail and can be fully transcribed to S3 or CloudWatch Logs.
- It requires the SSM agent, an instance profile with `AmazonSSMManagedInstanceCore`, and network access to the SSM endpoints.
- Port forwarding through Session Manager covers most remaining reasons to keep SSH open.
