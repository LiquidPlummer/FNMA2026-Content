# Mapping Console Actions to API Calls

The skill that makes AWS documentation usable is translating between what a console screen shows and the API call underneath it. Once that mapping is automatic, the CLI reference becomes the fastest way to answer most questions.

---

## The CLI's Structure

Every CLI command has the same three-part shape:

```
aws <service> <operation> [--parameters]
```

The service is the short name (`ec2`, `s3api`, `iam`, `lambda`), and the operation is the API action converted from `PascalCase` to `kebab-case`:

| API action | CLI command |
|---|---|
| `RunInstances` | `aws ec2 run-instances` |
| `DescribeInstances` | `aws ec2 describe-instances` |
| `CreateBucket` | `aws s3api create-bucket` |
| `PutObject` | `aws s3api put-object` |
| `AttachRolePolicy` | `aws iam attach-role-policy` |

The verb prefixes are consistent enough to guess from: `describe-`/`list-`/`get-` read, `create-`/`put-`/`run-` make things, `delete-`/`terminate-` remove them, and `update-`/`modify-` change them.

**S3 is the exception worth knowing.** It has two command sets: `aws s3` offers high-level, filesystem-like commands (`cp`, `ls`, `sync`), while `aws s3api` maps one-to-one onto the actual API. Use `s3` for moving files and `s3api` when a specific API parameter is needed.

---

## Discovering Commands

`help` works at every level and is usually faster than searching the web:

```bash
aws ec2 help                      # every EC2 operation
aws ec2 describe-instances help   # parameters, filters, and examples for one operation
```

*Built-in help is generated from the same API model as the online reference, so it is always accurate for the installed version.*

---

## Finding the Call Behind a Console Screen

Three reliable techniques, in order of effort:

**Guess from the button.** Console buttons usually correspond directly to an action name. "Launch instance" is `RunInstances`; "Create bucket" is `CreateBucket`. Verify with `aws <service> help`.

**Read CloudTrail.** Perform the action in the console, then look at CloudTrail's event history. It records the exact API call, including every parameter the console supplied — often revealing defaults the console filled in without showing them.

**Open the browser's network tab.** The console's own requests are visible in developer tools, with the action name in the request payload. Useful for screens that make several calls at once.

CloudTrail is the most instructive of the three, because it shows the complete parameter set rather than only the fields the console displayed.

---

## Reading the Reference Well

Three habits make the documentation more efficient to use:

**Check whether the operation is paginated.** Many `list-` and `describe-` calls return a page at a time and a `NextToken`. The CLI handles this automatically; SDK code frequently does not, and silently processing only the first page is a common bug.

**Look up the IAM action name.** Each API operation maps to an IAM action, usually `service:ActionName` — `ec2:RunInstances`, `s3:PutObject`. Writing a policy means knowing these names. The "Actions, resources, and condition keys" page for each service lists them all.

**Use `--dry-run` and `--query`.** Many EC2 operations accept `--dry-run`, which checks permissions without doing anything. `--query` uses JMESPath to reduce large responses to the fields that matter.

```bash
aws ec2 run-instances --image-id ami-0abcd1234 --instance-type t3.micro --dry-run
aws ec2 describe-instances \
  --query "Reservations[].Instances[].[InstanceId,State.Name,InstanceType]" \
  --output table
```

*`--dry-run` verifies permission without launching anything; `--query` trims a verbose response down to three fields in a readable table.*

---

## Key Takeaways

- CLI commands follow `aws <service> <operation>`, with operations being the API action in kebab-case.
- Verb prefixes are consistent: `describe`/`list`/`get` read, `create`/`put`/`run` create, `delete`/`terminate` remove.
- `aws s3` gives filesystem-style commands; `aws s3api` maps directly to the API.
- `aws <service> <operation> help` is generated from the API model and is usually faster than searching online.
- CloudTrail shows the exact call and full parameters behind any console action.
- Check pagination, note the IAM action name for policy writing, and use `--dry-run` and `--query` while exploring.
