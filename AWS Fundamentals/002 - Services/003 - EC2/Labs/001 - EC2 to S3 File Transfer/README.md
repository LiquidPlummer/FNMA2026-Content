# EC2 to S3 File Transfer, via an IAM Role

We'll launch a Linux EC2 instance, attach an IAM role to it (rather than hardcoding credentials), SSH in, and practice moving files in both directions between the instance and an S3 bucket using the AWS CLI. This lab builds directly on the EC2 notes (AMI, instance type, key pairs, security groups) and the IAM notes (roles over long-term users, least privilege) — we won't re-explain those concepts here, just put them to work together.

This lab uses your account's **default VPC** and a single public subnet — we're deliberately keeping networking minimal here, since VPC design gets its own full lesson (and its own lab, in a later module) where it's worth doing properly. For now, the focus is entirely on the IAM role and the file transfer.

We'll drive everything from the AWS CLI, the same convention as the S3 basics lab.

## Prerequisites

| Requirement | Notes |
|---|---|
| AWS CLI v2 | 2.27 or later. Run `aws --version` to check. |
| AWS account credentials | Configured locally via `aws configure`, with permissions to manage EC2, IAM, and S3. |
| SSH client | `ssh` available on your terminal (built in on macOS/Linux; on Windows, use the built-in OpenSSH client, WSL, or Git Bash). |
| A terminal | Examples use bash syntax (`export VAR=value`). On PowerShell, use `$env:VAR = "value"` instead. |

`cd` into this lab's directory before starting — the steps below reference files under `src/` relative to this README.

Before launching anything, it's worth checking the [AWS Free Tier](https://aws.amazon.com/free/) offer for EC2 in your account, per the Free Tier notes from earlier in the course — this lab is sized to fit comfortably within a small, general-purpose instance type running for well under an hour.

## Guided Walkthrough

### 1. Create the bucket and seed it

```bash
export BUCKET_NAME=ec2-s3-lab-yourname-4821
export AWS_REGION=us-east-1
aws s3 mb s3://$BUCKET_NAME --region $AWS_REGION
aws s3 cp src/sample-data/report.txt s3://$BUCKET_NAME/report.txt
```
*Creates a fresh bucket and uploads `report.txt` into it from our local machine — this is the file we'll pull down onto the instance later.*

### 2. Create the IAM role the instance will assume

This is the important part conceptually: the instance itself will never hold an access key. Instead, it assumes a **role**, and AWS hands it short-term, auto-rotated credentials behind the scenes — exactly the pattern described in the IAM notes.

First, the **trust policy** — who's allowed to assume this role:

```bash
aws iam create-role \
  --role-name ec2-s3-lab-role \
  --assume-role-policy-document file://src/trust-policy.json
```
*`src/trust-policy.json` grants `ec2.amazonaws.com` permission to assume this role — meaning an EC2 instance can use it, nothing else can.*

Now the **permissions policy** — what the role is actually allowed to do, scoped to just our one bucket:

```bash
sed "s/BUCKET_NAME/$BUCKET_NAME/g" src/s3-access-policy.template.json > s3-access-policy.json
POLICY_ARN=$(aws iam create-policy \
  --policy-name ec2-s3-lab-policy \
  --policy-document file://s3-access-policy.json \
  --query 'Policy.Arn' --output text)
aws iam attach-role-policy --role-name ec2-s3-lab-role --policy-arn $POLICY_ARN
```
*Creates a policy granting `ListBucket`, `GetObject`, and `PutObject` on this one bucket only — not "all S3 buckets" — and attaches it to the role. This is least privilege in action: the role can do exactly what this lab needs and nothing else.*

EC2 doesn't attach roles directly — it attaches an **instance profile**, a thin wrapper around the role:

```bash
aws iam create-instance-profile --instance-profile-name ec2-s3-lab-profile
aws iam add-role-to-instance-profile \
  --instance-profile-name ec2-s3-lab-profile \
  --role-name ec2-s3-lab-role
sleep 15   # IAM's eventual consistency — give the new profile a moment to propagate
```
*Wraps our role in an instance profile, which is what we'll actually reference at launch time.*

### 3. Create a key pair and a locked-down security group

```bash
aws ec2 create-key-pair --key-name ec2-s3-lab-key \
  --query 'KeyMaterial' --output text > ec2-s3-lab-key.pem
chmod 400 ec2-s3-lab-key.pem
```
*Generates a new key pair and saves the private key locally with restricted permissions — SSH will refuse to use a key that's readable by everyone.*

```bash
SG_ID=$(aws ec2 create-security-group \
  --group-name ec2-s3-lab-sg \
  --description "SSH access for the EC2-S3 lab" \
  --query 'GroupId' --output text)

MY_IP=$(curl -s https://checkip.amazonaws.com)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID --protocol tcp --port 22 --cidr "$MY_IP/32"
```
*Creates a security group and opens inbound port 22 (SSH) — but only from our own current IP address, not the whole internet. Recall from the EC2 notes: a fresh security group denies all inbound traffic until we explicitly allow it, so this is the minimum necessary opening, not a wide-open one.*

### 4. Launch the instance

```bash
INSTANCE_ID=$(aws ec2 run-instances \
  --image-id resolve:ssm:/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64 \
  --instance-type t3.micro \
  --key-name ec2-s3-lab-key \
  --security-group-ids $SG_ID \
  --iam-instance-profile Name=ec2-s3-lab-profile \
  --associate-public-ip-address \
  --query 'Instances[0].InstanceId' --output text)

aws ec2 wait instance-running --instance-ids $INSTANCE_ID
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID \
  --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
echo "Instance $INSTANCE_ID is running at $PUBLIC_IP"
```
*Launches a small, general-purpose Amazon Linux instance (the `resolve:ssm:...` image ID always resolves to the current Amazon Linux 2023 AMI at launch time, so we're never hardcoding a specific AMI ID that could go stale) with our security group and, critically, our IAM instance profile attached. `wait instance-running` blocks until it's actually up.*

### 5. SSH in and check identity

```bash
ssh -i ec2-s3-lab-key.pem ec2-user@$PUBLIC_IP
```
*Connects to the instance. `ec2-user` is the default login user baked into Amazon Linux AMIs.*

Once connected, confirm the AWS CLI is present (Amazon Linux 2023 ships with it) and check which identity it sees:

```bash
aws --version
aws sts get-caller-identity
```
*The identity returned here should be our `ec2-s3-lab-role` — proof the instance is authenticating as the role we attached, with no access key or secret key ever entered anywhere on this instance.*

### 6. Upload: instance → S3

Still on the instance:

```bash
echo "Hello from my EC2 instance!" > notes.txt
aws s3 cp notes.txt s3://$BUCKET_NAME/from-ec2/notes.txt
```
*(Remember `$BUCKET_NAME` won't be set in this new SSH session — export it here too, with the same value you used locally.) Creates a small file directly on the instance and uploads it to S3 under a new `from-ec2/` prefix.*

### 7. Download: S3 → instance

```bash
aws s3 cp s3://$BUCKET_NAME/report.txt ./report.txt
cat report.txt
```
*Pulls the file we seeded from our local machine back down — but this time onto the EC2 instance, using the role's credentials rather than ours.*

### 8. Verify from your local machine, then clean up

Back on your local machine (exit the SSH session with `exit`):

```bash
aws s3 ls s3://$BUCKET_NAME/from-ec2/
```
*Confirms the file the instance uploaded really did land in the bucket.*

Then tear everything down, in reverse order of creation:

```bash
aws ec2 terminate-instances --instance-ids $INSTANCE_ID
aws ec2 wait instance-terminated --instance-ids $INSTANCE_ID

aws iam remove-role-from-instance-profile --instance-profile-name ec2-s3-lab-profile --role-name ec2-s3-lab-role
aws iam delete-instance-profile --instance-profile-name ec2-s3-lab-profile
aws iam detach-role-policy --role-name ec2-s3-lab-role --policy-arn $POLICY_ARN
aws iam delete-policy --policy-arn $POLICY_ARN
aws iam delete-role --role-name ec2-s3-lab-role

aws ec2 delete-security-group --group-id $SG_ID
aws ec2 delete-key-pair --key-name ec2-s3-lab-key
rm -f ec2-s3-lab-key.pem

aws s3 rb s3://$BUCKET_NAME --force
```
*Terminates the instance, unwinds the IAM role/policy/instance profile chain, removes the security group and key pair, and empties and deletes the bucket. Order matters here — for example, the instance profile can't be deleted while still attached to a role.*

## Exercises

Relaunch a fresh instance and role (or reuse one you haven't torn down yet) for these — each builds on the least-privilege ideas from the IAM lesson.

1. **Prove read-only actually means read-only.** Edit `s3-access-policy.template.json` (or a copy of it) to remove the `s3:PutObject` action, leaving only `s3:ListBucket` and `s3:GetObject`. Update the attached policy, SSH back into the instance, and confirm that downloading still works but attempting to upload a new file now fails with an access-denied error.

2. **Scope write access to one prefix only.** Starting from the read-only policy in exercise 1, add a statement granting `s3:PutObject` — but only on `arn:aws:s3:::$BUCKET_NAME/scratch/*`, not the whole bucket. From the instance, confirm you can upload into `scratch/` but still can't upload anywhere else in the bucket.

3. **Revoke access from a running instance.** With the instance still running and connected over SSH, go back to your local machine and detach the policy from the role entirely (`aws iam detach-role-policy`). Without restarting or reconnecting to the instance, try another `aws s3 ls` from inside the existing SSH session. Notice that access is denied immediately, even though the instance was never touched — the instance was never issued permissions of its own, it was only ever borrowing the role's, checked fresh on every request.
