# Stacks, Change Sets & Rollback

The lifecycle of a stack, the mechanism for seeing what an update will do before it does it, and what happens when one fails.

---

## Stack States

A stack moves through states that name exactly what is happening:

```
CREATE_IN_PROGRESS ──► CREATE_COMPLETE
                   └─► CREATE_FAILED ──► ROLLBACK_IN_PROGRESS ──► ROLLBACK_COMPLETE

UPDATE_IN_PROGRESS ──► UPDATE_COMPLETE
                   └─► UPDATE_ROLLBACK_IN_PROGRESS ──► UPDATE_ROLLBACK_COMPLETE
                                                   └─► UPDATE_ROLLBACK_FAILED
```

Two states need specific handling:

**`ROLLBACK_COMPLETE`** after a failed creation. The stack cannot be updated from this state — it must be **deleted and recreated**. This surprises people expecting to fix the template and retry.

**`UPDATE_ROLLBACK_FAILED`** means the rollback itself failed, usually because a resource was modified outside CloudFormation or a dependency prevented reversal. Recovery is `continue-update-rollback`, optionally skipping the resources that cannot be rolled back:

```bash
aws cloudformation continue-update-rollback \
  --stack-name production-network \
  --resources-to-skip DatabaseInstance
```

*Skipped resources are left as-is, so the stack becomes consistent again with those resources needing manual reconciliation.*

---

## Change Sets

An update applied directly gives no preview. A **change set** computes what would change and waits:

```bash
aws cloudformation create-change-set \
  --stack-name production-network \
  --template-body file://network.yaml \
  --change-set-name add-nat-gateway

aws cloudformation describe-change-set \
  --stack-name production-network \
  --change-set-name add-nat-gateway
```

*Computes the changes without applying them. Executing it is a separate command, so nothing happens until it is reviewed.*

The output names each change and — critically — its **replacement** status:

| Action | Meaning |
|---|---|
| `Add` | A new resource |
| `Modify`, `Replacement: False` | Updated in place |
| `Modify`, `Replacement: True` | **Deleted and recreated** |
| `Modify`, `Replacement: Conditional` | May be replaced |
| `Remove` | Deleted |

**`Replacement: True` is what change sets are for.** Some property changes cannot be made in place — an RDS `DBInstanceIdentifier`, an EC2 `AvailabilityZone`, a DynamoDB `KeySchema`. CloudFormation handles this by creating a new resource and deleting the old one, which for a stateful resource means **data loss**.

A template change that looks innocuous can trigger replacement. Reviewing a change set before every production update is the practice that prevents this, and `Replacement: Conditional` deserves particular care because whether it replaces depends on the current state.

---

## Rollback Behavior

**On creation failure**, CloudFormation deletes everything it created by default, leaving no partial stack. `--disable-rollback` preserves the resources for inspection, which is useful when debugging why a resource failed to create.

**On update failure**, CloudFormation reverts every changed resource to its previous state. This is usually correct and occasionally problematic — a rollback can itself fail if a resource was changed outside CloudFormation.

**Rollback triggers** monitor CloudWatch alarms during and after an update, rolling back automatically if one fires:

```yaml
RollbackConfiguration:
  RollbackTriggers:
    - Arn: arn:aws:cloudwatch:us-east-1:123456789012:alarm:HighErrorRate
      Type: AWS::CloudWatch::Alarm
  MonitoringTimeInMinutes: 15
```

*Watches for 15 minutes after the update completes and rolls back if errors rise — catching a deployment that succeeded technically and broke the application.*

---

## Protecting Stacks

**Termination protection** blocks stack deletion:

```bash
aws cloudformation update-termination-protection \
  --stack-name production-network --enable-termination-protection
```

**Stack policies** protect specific resources from update or replacement:

```json
{
  "Statement": [
    { "Effect": "Allow", "Action": "Update:*", "Principal": "*", "Resource": "*" },
    { "Effect": "Deny", "Action": "Update:Replace", "Principal": "*",
      "Resource": "LogicalResourceId/DatabaseInstance" }
  ]
}
```

*Permits updates generally while preventing replacement of the database — a guard against the accidental data loss a change set would have revealed.*

Both are worth applying to production stacks.

---

## Nested Stacks and StackSets

**Nested stacks** let one stack create others, so a common pattern — a standard VPC layout — is defined once and reused as a resource type. The trade is that troubleshooting spans several stacks and updates propagate through the parent.

**StackSets** deploy one template across many accounts and regions from a single operation. This is how organization-wide baselines — logging configuration, IAM roles, security controls — are applied consistently.

---

## Key Takeaways

- A stack in `ROLLBACK_COMPLETE` after failed creation cannot be updated and must be deleted and recreated.
- `UPDATE_ROLLBACK_FAILED` is recovered with `continue-update-rollback`, optionally skipping resources.
- Change sets compute what an update would do without applying it, and executing is a separate step.
- `Replacement: True` means a resource is deleted and recreated, which for stateful resources means data loss.
- Review change sets before production updates, paying particular attention to conditional replacements.
- Creation failures roll back by default; `--disable-rollback` preserves resources for debugging.
- Rollback triggers watch CloudWatch alarms and revert an update that broke the application.
- Enable termination protection and use stack policies to prevent replacement of stateful resources.
- StackSets deploy one template across many accounts and regions.
