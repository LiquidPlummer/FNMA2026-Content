# Tag-Based IAM Conditions

Tags can drive authorization. Instead of enumerating resource ARNs in a policy, a policy can grant access to whatever carries a particular tag — an approach usually called **attribute-based access control (ABAC)**.

---

## The Three Condition Keys

Which key to use is the part people get wrong, and the distinction is precise:

**`aws:ResourceTag/<key>`** — a tag on the **existing resource** being acted upon. Used to control access to things that already exist.

**`aws:RequestTag/<key>`** — a tag being **supplied in the request** that creates or tags a resource. Used to control what tags someone may apply.

**`aws:PrincipalTag/<key>`** — a tag on the **caller's identity**. Used to compare the caller against the resource.

```json
{
  "Effect": "Allow",
  "Action": ["ec2:StartInstances", "ec2:StopInstances"],
  "Resource": "arn:aws:ec2:*:*:instance/*",
  "Condition": {
    "StringEquals": { "aws:ResourceTag/Environment": "development" }
  }
}
```

*Permits starting and stopping any instance tagged `Environment=development`, and nothing else — no ARNs are listed.*

---

## The ABAC Pattern

The powerful version compares a principal tag to a resource tag, so one policy scales across every team:

```json
{
  "Effect": "Allow",
  "Action": ["ec2:*", "rds:*"],
  "Resource": "*",
  "Condition": {
    "StringEquals": {
      "aws:ResourceTag/Team": "${aws:PrincipalTag/Team}"
    }
  }
}
```

*Each principal may act only on resources tagged with their own team. The `${...}` substitution reads the caller's tag at evaluation time.*

One policy, unchanged, correctly scopes access for every team — including teams that do not exist yet. Adding a team means tagging their identities and their resources, with no policy edit. That is the real appeal: policies stop growing with the organization.

---

## Guarding the Tags Themselves

The pattern collapses if users can retag resources. Someone able to change `Team=data-eng` to `Team=platform` grants themselves access.

So tag-based access control requires controlling tag modification:

```json
{
  "Effect": "Deny",
  "Action": ["ec2:CreateTags", "ec2:DeleteTags"],
  "Resource": "*",
  "Condition": {
    "ForAnyValue:StringEquals": {
      "aws:TagKeys": ["Team", "Environment", "CostCenter"]
    }
  }
}
```

*Denies modifying the tags that authorization depends on, while leaving other tags editable. `aws:TagKeys` lists the keys the request touches.*

Without a deny like this, ABAC is decorative.

---

## Enforcing Tags at Creation

`aws:RequestTag` requires tags to be supplied when a resource is created:

```json
{
  "Effect": "Allow",
  "Action": "ec2:RunInstances",
  "Resource": "arn:aws:ec2:*:*:instance/*",
  "Condition": {
    "StringEquals": { "aws:RequestTag/Team": "${aws:PrincipalTag/Team}" },
    "Null": { "aws:RequestTag/Environment": "false" }
  }
}
```

*Instances may be launched only when tagged with the caller's own team and with some `Environment` value present. `Null: false` requires the tag to exist.*

This closes the loop: resources are created tagged, and access to them is governed by those tags.

---

## Practical Limits

Three constraints worth knowing before committing to ABAC:

**Not every service supports it.** Support for `aws:ResourceTag` varies by service and even by action within a service. Each service's "Actions, resources, and condition keys" documentation lists which actions support which keys. Assuming uniform support leads to policies that silently do not apply.

**Some actions have no resource to tag.** `ec2:DescribeInstances` returns many instances and cannot be conditioned on any one instance's tags. Describe and list operations generally cannot be filtered this way — the filtering has to happen client-side.

**Resource creation is multi-resource.** `ec2:RunInstances` creates an instance, volumes, and network interfaces. Tag conditions must account for each resource type, which makes launch policies longer than expected.

---

## When ABAC Is Worth It

It pays off with many teams and many resources, where per-team policies would multiply. It is overhead for a small account with a handful of resources, where naming specific ARNs is clearer and easier to audit.

A common middle path: ARN-based policies for a few sensitive resources, ABAC for the broad population of ordinary ones.

---

## Key Takeaways

- `aws:ResourceTag` matches tags on an existing resource, `aws:RequestTag` matches tags supplied in a create request, and `aws:PrincipalTag` matches tags on the caller.
- Comparing a principal tag to a resource tag produces one policy that scales to any number of teams without edits.
- ABAC requires denying modification of the tags that authorization depends on, or users can grant themselves access.
- `aws:RequestTag` with a `Null` condition enforces that resources are tagged at creation.
- Tag condition support varies by service and action, and describe/list operations generally cannot be conditioned this way.
- Multi-resource operations like `RunInstances` need tag conditions covering each resource type they create.
