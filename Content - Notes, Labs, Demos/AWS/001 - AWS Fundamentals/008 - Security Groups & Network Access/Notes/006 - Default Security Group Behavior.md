# Default Security Group Behavior

Every VPC has a **default security group** that behaves differently from any group we create. The difference surprises people regularly, and it has a real security consequence.

---

## What the Default Group Does

Each VPC's default security group starts with:

- **Inbound:** allow all traffic from **itself** — any resource in the same default security group.
- **Outbound:** allow all traffic to anywhere.

That inbound rule is a self-reference, and it is the surprising part. A newly created security group has *no* inbound rules and blocks everything. The default group permits unrestricted traffic between every resource that uses it.

---

## Why That Is a Problem

Resources launched without an explicitly chosen security group get the default. In practice this means the default group accumulates unrelated workloads — a web server, a batch job, a test database, a developer's experiment — and every one of them can reach every other on every port.

```
Default security group
├── web server        ── can reach ──►  test database    (all ports)
├── test database     ── can reach ──►  batch worker     (all ports)
└── batch worker      ── can reach ──►  web server       (all ports)
```

*Membership alone grants full mutual access, so an internet-facing instance in the default group has unrestricted access to everything else in it.*

This is not a theoretical concern. The realistic scenario: a public-facing instance is compromised, and because it shares the default group with a database, the attacker reaches the database directly — no additional exploit required.

---

## Additional Quirks

**It cannot be deleted.** Every VPC has exactly one default security group, permanently. Its rules can be emptied, but the group itself remains.

**Its rules can be changed.** Removing the self-referencing inbound rule is permitted and is worth doing, since it converts the default group into something that denies inbound traffic like any other group.

**It is used implicitly.** Launching an instance without specifying a security group assigns the default. Several APIs and tools do this silently, so resources join it without anyone choosing that.

**Every VPC has its own.** They are distinct groups with the same name, which makes them easy to confuse when working across VPCs.

---

## What to Do About It

Three steps, in order of value:

**1. Strip the default group's rules.** Remove the inbound self-reference and, ideally, the outbound allow-all. A default group with no rules is harmless — anything that lands in it by accident is isolated rather than fully connected.

```bash
aws ec2 revoke-security-group-ingress \
  --group-id sg-0default123 \
  --protocol all --source-group sg-0default123
```

*Removes the self-referencing inbound rule, so resources in the default group no longer have unrestricted access to each other.*

**2. Always specify a security group explicitly.** In templates, in launch commands, in Auto Scaling launch templates. Never let the default be chosen by omission.

**3. Detect resources using it.** A Config rule or a periodic query flags anything that ended up in the default group, since that is nearly always accidental.

```bash
aws ec2 describe-instances \
  --filters "Name=instance.group-name,Values=default" \
  --query "Reservations[].Instances[].[InstanceId,SubnetId]" --output table
```

*Lists instances using a default security group — each result is worth investigating, because it indicates a group was never chosen.*

---

## The Underlying Point

The default security group is convenient for getting started and unsuitable for anything beyond that. Its permissive self-reference exists so that a beginner's instances can talk to each other without configuration — a reasonable choice for a first experiment, and a poor one for a system with a database in it.

Treating it as something to neutralize rather than use avoids the whole category of problem.

---

## Key Takeaways

- The default security group allows all traffic between its own members and all outbound traffic, unlike newly created groups which block all inbound.
- Resources launched without an explicit security group join it, so unrelated workloads accumulate with full mutual access.
- A compromised internet-facing instance in the default group can reach every other resource in it directly.
- The default group cannot be deleted, but its rules can and should be removed.
- Always specify security groups explicitly, and monitor for resources that ended up in the default group.
