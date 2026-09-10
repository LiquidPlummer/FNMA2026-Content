# Answering "Who Deleted This Resource?"

A concrete walkthrough, because it exercises everything in this lesson and it is a question that gets asked under pressure.

The scenario: an RDS instance named `orders-prod` no longer exists. Nobody admits to deleting it.

---

## Step 1 — Find the Event

The deletion was an API call, so it is in CloudTrail. Event history covers the last 90 days of management events and is available with no setup:

```bash
aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=EventName,AttributeValue=DeleteDBInstance \
  --start-time 2026-09-08T00:00:00Z \
  --end-time 2026-09-09T23:59:59Z
```

*Searches management events by name. `lookup-events` reads the 90-day event history, so it works even without a configured trail.*

For anything older, or for data events, the query goes against the trail's S3 bucket instead — typically through Athena, which CloudTrail can configure a table for automatically.

---

## Step 2 — Read the Identity

The `userIdentity` block is the answer, and its shape depends on how the call was made:

**An IAM user acting directly:**

```json
"userIdentity": {
  "type": "IAMUser",
  "arn": "arn:aws:iam::123456789012:user/dana",
  "userName": "dana"
}
```

*Unambiguous — a named user made the call.*

**An assumed role — the common case:**

```json
"userIdentity": {
  "type": "AssumedRole",
  "arn": "arn:aws:sts::123456789012:assumed-role/DeployRole/ci-build-4471",
  "sessionContext": {
    "sessionIssuer": {
      "type": "Role",
      "arn": "arn:aws:iam::123456789012:role/DeployRole"
    },
    "attributes": { "mfaAuthenticated": "false" }
  }
}
```

*The role is `DeployRole` and the session name is `ci-build-4471`. The session name is the only link back to the human or system that assumed the role — which is why meaningful session names matter.*

If the session name is uninformative (`default`, `session1`), the trail goes cold here. Recovering the answer means finding the earlier `AssumeRole` event and reading *its* identity:

```bash
aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=EventName,AttributeValue=AssumeRole \
  --start-time 2026-09-09T13:00:00Z --end-time 2026-09-09T15:00:00Z
```

*The `AssumeRole` event records who assumed the role, chaining the anonymous session back to an original identity.*

**An AWS service acting on our behalf:**

```json
"userIdentity": { "type": "AWSService", "invokedBy": "autoscaling.amazonaws.com" }
```

*Not a person — automation did it, which redirects the investigation toward what triggered the automation.*

---

## Step 3 — Establish the Circumstances

The rest of the event supplies context:

- **`sourceIPAddress`** — a corporate range, a home connection, or an EC2 instance's address.
- **`userAgent`** — `aws-cli/2.x`, an SDK string, `console.amazonaws.com`, or `cloudformation.amazonaws.com`. This distinguishes a manual deletion from a stack update.
- **`requestParameters`** — for `DeleteDBInstance`, whether `SkipFinalSnapshot` was true, which determines whether recovery is possible.
- **`eventTime`** — correlate with deployments, incidents, and other activity in the same window.
- **`errorCode`** — absent on success; earlier failed attempts by the same principal are often informative.

A `cloudformation.amazonaws.com` user agent reframes the question entirely: nobody deleted the database directly, a stack operation did, and the real question becomes who changed or deleted the stack.

---

## Step 4 — Recover, If Possible

For RDS, recovery depends on what was left behind:

- A final snapshot, if `SkipFinalSnapshot` was false.
- Automated backups, which are retained for the configured window even after deletion.
- Manual snapshots, which are never deleted with the instance.

Deletion protection would have prevented it outright — the API call fails while the flag is set.

---

## What This Requires in Advance

The investigation only works if groundwork was done, and every item below is set up before it is needed:

| Requirement | Why |
|---|---|
| A multi-region trail with long retention | Event history stops at 90 days and management events only |
| Meaningful role session names | Otherwise a shared role gives no attribution |
| Data events on sensitive resources | Otherwise reads and object deletions are invisible |
| Trail protected in a separate account | Otherwise logs can be altered by whoever had access |
| Alarms on high-risk events | So the question is asked in minutes, not weeks |

An alarm on `DeleteDBInstance`, `StopLogging`, and root sign-in turns this from an archaeology exercise into a notification.

---

## Key Takeaways

- Every deletion is an API call, so CloudTrail holds the answer; `lookup-events` searches the 90-day management event history without setup.
- The `userIdentity` block names the actor; for assumed roles, the session name is the only link to the human behind it.
- An uninformative session name can be chained back by finding the corresponding `AssumeRole` event.
- `userAgent` distinguishes manual console or CLI actions from automation such as CloudFormation.
- `requestParameters` often determines whether recovery is possible, such as whether a final snapshot was taken.
- The investigation depends on preparation: a long-retention trail, meaningful session names, data events where they matter, protected logs, and alarms on high-risk actions.
