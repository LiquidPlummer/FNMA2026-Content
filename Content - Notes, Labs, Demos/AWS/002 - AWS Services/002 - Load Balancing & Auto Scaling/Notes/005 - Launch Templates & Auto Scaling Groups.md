# Launch Templates & Auto Scaling Groups

A **launch template** defines what an instance looks like. An **Auto Scaling group** decides how many of them exist and where. Together they turn instances from things we create into a quantity we specify.

---

## Launch Templates

A launch template holds every parameter needed to launch an instance: AMI, instance type, key pair, security groups, instance profile, user data, block device mappings, and tags.

```bash
aws ec2 create-launch-template \
  --launch-template-name app-server \
  --launch-template-data '{
    "ImageId": "ami-0abc123",
    "InstanceType": "t3.medium",
    "IamInstanceProfile": {"Name": "app-server-profile"},
    "SecurityGroupIds": ["sg-0app123"],
    "UserData": "IyEvYmluL2Jhc2gKZG5mIHVwZGF0ZSAteQo=",
    "TagSpecifications": [{
      "ResourceType": "instance",
      "Tags": [{"Key": "Environment", "Value": "production"}]
    }]
  }'
```

*User data is base64-encoded in the template. `TagSpecifications` is what ensures launched instances are tagged — without it they are not.*

Templates are **versioned**. Each change creates a new version, and the group can reference a specific version, `$Latest`, or `$Default`. Referencing `$Latest` means the group picks up changes automatically, which is convenient and means a template edit changes production behavior without a deployment step. Pinning to a version and updating deliberately is the safer pattern.

**Launch configurations** are the deprecated predecessor — immutable, unversioned, and unsupported for newer features like mixed instance policies. Existing ones should be migrated.

---

## Auto Scaling Groups

An ASG maintains a number of instances, defined by three values:

- **Minimum** — never go below this
- **Maximum** — never go above this
- **Desired** — the current target

The group continuously works toward the desired count. If an instance becomes unhealthy or is terminated, the group launches a replacement without anyone intervening. **This automatic replacement is the main benefit**, and it applies regardless of whether any scaling policy exists — an ASG with min=max=desired=2 still provides recovery.

```bash
aws autoscaling create-auto-scaling-group \
  --auto-scaling-group-name app-servers \
  --launch-template LaunchTemplateName=app-server,Version='2' \
  --min-size 2 --max-size 10 --desired-capacity 3 \
  --vpc-zone-identifier "subnet-0priv-a,subnet-0priv-b,subnet-0priv-c" \
  --target-group-arns arn:aws:elasticloadbalancing:...:targetgroup/app/abc \
  --health-check-type ELB --health-check-grace-period 300
```

*Three instances across three AZs, using ELB health checks so a failed application is replaced. `--vpc-zone-identifier` lists the subnets, which determines the zones.*

---

## Spreading Across Availability Zones

Listing subnets in several AZs is what makes the group multi-AZ. The group balances instances across them and **rebalances automatically** when they become uneven — after an AZ recovers from an incident, for example.

Two things worth knowing:

**Maximum should account for AZ loss.** With 3 AZs and 6 instances, losing a zone leaves 4. If 6 are needed to serve peak load, the maximum must allow the group to launch 2 more in the surviving zones.

**Rebalancing terminates instances.** When restoring balance, the group may terminate a healthy instance in an over-represented zone. This is expected behavior, and it is another reason instances must be disposable.

---

## Sizing Minimum and Maximum

**Minimum** is the floor that must always exist. Setting it to 1 means an AZ failure can leave zero capacity while a replacement launches, so 2 is the practical minimum for anything that must stay available.

**Maximum** is a cost ceiling as much as a capacity limit. It caps what a traffic spike — or a scaling misconfiguration, or an attack — can spend. Setting it very high removes that protection; setting it too low means the group stops scaling exactly when it is needed. The right value is roughly double expected peak, with an alarm when the group is at maximum, since that indicates either an incident or a limit that needs revisiting.

---

## Instance Refresh

Replacing every instance in a group — for a new AMI or a template change — is done with an **instance refresh**:

```bash
aws autoscaling start-instance-refresh \
  --auto-scaling-group-name app-servers \
  --preferences '{"MinHealthyPercentage": 90, "InstanceWarmup": 300}'
```

*Replaces instances in batches while keeping at least 90% healthy, waiting 300 seconds for each new instance to warm up before continuing.*

This is a rolling deployment mechanism, and it is how AMI updates reach a running fleet without downtime. It can be paused and rolled back if health checks start failing.

---

## Mixed Instances

A group can draw from several instance types and mix on-demand with spot:

```json
{
  "InstancesDistribution": {
    "OnDemandBaseCapacity": 2,
    "OnDemandPercentageAboveBaseCapacity": 25,
    "SpotAllocationStrategy": "capacity-optimized"
  },
  "Overrides": [
    {"InstanceType": "m5.large"},
    {"InstanceType": "m5a.large"},
    {"InstanceType": "m6i.large"}
  ]
}
```

*Two on-demand instances as a guaranteed baseline, then 25% on-demand and 75% spot above it, drawing from three interchangeable types.*

Type diversity matters for spot: capacity is per type per AZ, so accepting several types substantially reduces interruptions. `capacity-optimized` selects from the pools with the most available capacity, which reduces them further.

---

## Key Takeaways

- A launch template defines instance configuration and is versioned; launch configurations are deprecated.
- Referencing `$Latest` means template edits change production immediately — pinning versions is safer.
- An Auto Scaling group maintains min, max, and desired counts, replacing failed instances automatically even with no scaling policy.
- Listing subnets across AZs makes the group multi-AZ, and it rebalances automatically, sometimes terminating healthy instances.
- Set minimum to at least 2, and size maximum to survive an AZ loss while still capping runaway cost.
- Instance refresh performs a rolling replacement of the fleet for AMI or template changes.
- Mixed instance policies combine an on-demand baseline with spot, and type diversity reduces spot interruptions.
