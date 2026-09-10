# Tags as Cross-Service Structure

A **tag** is a key-value pair attached to a resource. That is the whole mechanism — and it is the only organizing structure AWS provides that works across every service.

---

## Why Tags Carry So Much Weight

AWS has no folders. There is no project object that groups an EC2 instance, an S3 bucket, an RDS database, and a Lambda function into one thing. Resources sit in an account and a region, and that is the entire built-in hierarchy.

The available options for organizing beyond that are:

- **Separate accounts** — a strong boundary, but coarse and administratively heavy.
- **Naming conventions** — conventional only, unenforced, and unusable by any AWS feature.
- **Tags** — the only structure AWS itself understands and acts on.

That last point is what matters. Cost Explorer can group by tag. IAM policies can condition on tags. Automation can target by tag. Naming conventions can do none of this: `prod-web-01` is a string that AWS attaches no meaning to, while `Environment=production` is queryable.

---

## Mechanics

Most resources support up to **50 tags**. Keys can be up to 128 characters, values up to 256, and both are **case-sensitive** — `Environment` and `environment` are different tags, which is the most common source of gaps in tag-based reporting.

```bash
aws ec2 create-tags --resources i-0abc123 \
  --tags Key=Environment,Value=production \
         Key=Owner,Value=platform-team \
         Key=CostCenter,Value=CC-4471
```

*Tags applied after creation. Most create operations accept `--tag-specifications` so tags exist from the moment the resource does.*

Tagging at creation matters more than it appears. A resource created untagged is usually never tagged, and in the interval it produces cost records that cannot be attributed.

The `aws:` key prefix is reserved for AWS-generated tags, which cannot be edited or deleted.

---

## Where Tags Fall Short

Being honest about the limits prevents disappointment:

**Coverage is inconsistent.** Most resources support tags, but not all, and support for tagging *at creation* varies more than support for tagging at all.

**Nothing is enforced by default.** A resource can be created with no tags, and nothing objects. Enforcement requires tag policies, SCPs, or Config rules that someone sets up deliberately.

**Tags are mutable by anyone with permission.** They are metadata, not a security boundary — unless access to change them is itself restricted.

**They do not propagate.** An EBS volume created by a tagged EC2 instance does not inherit the instance's tags automatically. Snapshots do not inherit from volumes. Auto Scaling groups can propagate tags to instances, but that is a specific feature rather than general behavior — and it is why untagged volumes and snapshots accumulate in every account.

**Retroactive tagging does not fix cost data.** Cost allocation tags apply from activation forward. Tagging a resource today does not attribute last month's spend.

---

## What Makes Them Worth the Effort

Despite the limits, tags are how three important things get done:

- **Cost attribution.** Answering "what does this team spend?" requires tags; without them there is only a total.
- **Automation targeting.** Backup schedules, patch groups, and start/stop automation select resources by tag.
- **Access control.** IAM conditions can restrict actions to resources carrying a particular tag.

Each of these is covered in the following notes. The common thread is that they all depend on tags being applied consistently, which is a discipline problem more than a technical one.

---

## Key Takeaways

- A tag is a key-value pair, and tags are the only organizing structure AWS applies across all services.
- AWS has no folders or project objects; naming conventions carry no meaning that AWS can act on.
- Most resources allow 50 tags; keys and values are case-sensitive, which commonly fragments reporting.
- Tag at creation, since untagged resources tend to stay untagged and produce unattributable cost records.
- Tags are not enforced by default, are mutable, and do not propagate to derived resources like volumes and snapshots.
- Cost allocation tags apply only from activation forward, so retroactive tagging does not repair past cost data.
