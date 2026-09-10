# User Data & Bootstrapping

**User data** is a script passed to an instance at launch and executed during boot. It is how an instance goes from a generic AMI to a configured, running application without anyone logging in.

---

## How It Works

User data is supplied at launch and retrieved by **cloud-init** from the instance metadata service. A script beginning with a shebang is executed as root:

```bash
#!/bin/bash
dnf update -y
dnf install -y nginx
systemctl enable --now nginx
aws s3 cp s3://config-bucket/nginx.conf /etc/nginx/nginx.conf
systemctl reload nginx
```

*Runs as root during first boot. The S3 copy works without credentials because the instance profile supplies them.*

Two properties are essential:

**It runs as root**, with no interactive terminal. Anything expecting input hangs.

**By default it runs only on the first boot.** Stopping and starting an instance does not re-run it. Cloud-init records that it has run, and only explicit configuration changes that.

---

## What It Is Good For

The narrow, reliable uses:

- Fetching configuration from S3, Parameter Store, or Secrets Manager
- Registering with a service discovery system or configuration management tool
- Setting hostname and instance-specific values
- Mounting instance store or additional volumes
- Starting the application

The pattern that works well: **bake dependencies into the AMI, and use user data only for what differs per instance.** Package installation at boot is slow, and it makes launches depend on external repositories being reachable and unchanged. Under Auto Scaling, where instances launch during a traffic spike, a five-minute boot is a real problem.

---

## Debugging

The most common complaint is "my user data didn't run." It almost always did, and failed:

```bash
# Output from user data execution
sudo cat /var/log/cloud-init-output.log

# The script cloud-init actually wrote and ran
sudo cat /var/lib/cloud/instance/user-data.txt
```

*The first file holds stdout and stderr from the script; the second confirms what was received, which catches encoding and truncation problems.*

Frequent causes:

**Missing shebang.** Without `#!/bin/bash` on the first line, cloud-init does not treat it as a script.

**No error handling.** A failing command does not stop the script by default, so a later step runs against a broken state. Adding `set -euxo pipefail` makes failures loud and traceable.

**Assumed availability.** The network, DNS, or an external repository may not be ready when the script runs.

**The 16 KB limit.** User data is capped, and it is capped **before** base64 encoding. Longer scripts should be a small bootstrap that downloads the real one from S3.

**Silent success.** A script that fails leaves an instance that boots, passes status checks, and does not work. If the instance is behind a load balancer, the health check is what should catch this — which is a reason for health checks to test the application rather than just the port.

---

## Making It Idempotent

Since user data can be re-run — on a rebuilt instance, or if configured to run on every boot — writing it to be safe when repeated avoids a class of problems:

```bash
#!/bin/bash
set -euxo pipefail

if ! systemctl is-enabled nginx >/dev/null 2>&1; then
    dnf install -y nginx
    systemctl enable nginx
fi

aws s3 cp s3://config-bucket/nginx.conf /etc/nginx/nginx.conf
systemctl reload nginx || systemctl start nginx
```

*Checks before installing and handles both the reload and the initial start, so running it twice produces the same result as running it once.*

---

## Security Notes

**User data is not secret.** It is readable from instance metadata by anything running on the instance, and by anyone with `ec2:DescribeInstanceAttribute`. Credentials in user data are effectively public within the account.

Fetch secrets at runtime instead:

```bash
DB_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id prod/db/password --query SecretString --output text)
```

*Retrieves the secret using the instance profile, so nothing sensitive appears in the launch configuration, in CloudTrail, or in the metadata service.*

---

## Alternatives

For anything beyond simple bootstrapping:

- **Custom AMIs** (Packer, EC2 Image Builder) move configuration to build time, giving fast and consistent launches.
- **Systems Manager State Manager** applies and re-applies configuration on a schedule, correcting drift rather than only setting initial state.
- **Configuration management** — Ansible, Chef, Puppet — for complex or frequently changing configuration.
- **Containers**, where the image is the configuration and user data only starts the agent.

The general direction is to reduce what user data does. A long bootstrap script is a sign that configuration belongs in an image.

---

## Key Takeaways

- User data runs as root during first boot only, retrieved by cloud-init from instance metadata.
- Bake dependencies into the AMI and use user data for per-instance differences; installing packages at boot slows and destabilizes launches.
- Check `/var/log/cloud-init-output.log` when a script appears not to have run — it usually ran and failed.
- Include a shebang and `set -euxo pipefail`, since commands otherwise fail silently and later steps continue.
- User data is limited to 16 KB before encoding; longer scripts should bootstrap from S3.
- User data is not secret — fetch credentials at runtime from Secrets Manager or Parameter Store.
- Write scripts to be idempotent so re-running produces the same result.
