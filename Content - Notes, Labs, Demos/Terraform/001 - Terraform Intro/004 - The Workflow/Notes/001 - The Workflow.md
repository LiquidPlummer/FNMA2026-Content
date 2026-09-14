# The Workflow

Day-to-day Terraform comes down to a small set of commands, run in a consistent order. Knowing what each one does, and what it's *for*, is most of what we need to follow a conversation about how a team uses Terraform.

```bash
terraform init      # prepare the folder: download providers, connect to state
terraform plan      # preview: what would change?
terraform apply     # make the changes
terraform destroy   # remove everything this configuration manages
```

*The four core Terraform commands, in the order they're typically used over a configuration's life.*

---

## `init`: Prepare the Working Folder

`terraform init` gets a folder ready for Terraform to work in. It's the first command run in any new copy of a configuration, and nothing else works until it has run.

It does three things:

- **Downloads providers.** Terraform reads the configuration, sees which providers it uses (AWS, GitHub, and so on), and downloads those plugins from the Terraform Registry into a hidden `.terraform` folder. It also records the exact provider versions in a lock file, `.terraform.lock.hcl`, so everyone on the team uses the same versions.
- **Connects to the backend.** If the configuration stores state remotely, `init` sets up that connection.
- **Fetches modules** the configuration calls.

`init` doesn't touch any infrastructure, so it's always safe to run. It needs running again when providers, modules, or the backend change.

---

## `plan`: The Dry Run

`terraform plan` shows what Terraform *would* do, without doing it. This is where the state topic pays off: a plan is the comparison between the configuration and the state, after Terraform has checked the real world for any changes.

```text
Terraform will perform the following actions:

  # aws_instance.app_server will be updated in-place
  ~ resource "aws_instance" "app_server" {
        id            = "i-0a1b2c3d4e5f67890"
      ~ instance_type = "t3.micro" -> "t3.small"
        # (28 unchanged attributes hidden)
    }

  # aws_s3_bucket.reports will be created
  + resource "aws_s3_bucket" "reports" {
      + arn    = (known after apply)
      + bucket = "acme-quarterly-reports"
      + id     = (known after apply)
    }

Plan: 1 to add, 1 to change, 0 to destroy.
```

*Plan output proposing two changes: resize an existing server, and create a new S3 bucket.*

Plan output is built to be skimmed. Each resource is listed by address, with a symbol showing what will happen to it:

| Symbol | Meaning |
|---|---|
| `+` | Create |
| `~` | Update in place (the resource stays, a setting changes) |
| `-` | Destroy |
| `-/+` | Replace: destroy the existing resource and create a new one |

Inside each resource, arrows (`->`) show old and new values, and `(known after apply)` marks values the cloud platform will assign once the resource exists. The summary line at the bottom gives the totals, and it's the first thing experienced engineers look at.

Nothing changes when we run `plan`. It's purely a preview.

---

## `apply`: Make It So

`terraform apply` carries out the changes. By default, it calculates a fresh plan, shows it, and waits for explicit approval:

```text
Plan: 1 to add, 1 to change, 0 to destroy.

Do you want to perform these actions?
  Terraform will perform the actions described above.
  Only 'yes' will be accepted to approve.

  Enter a value: yes

aws_s3_bucket.reports: Creating...
aws_instance.app_server: Modifying... [id=i-0a1b2c3d4e5f67890]
aws_s3_bucket.reports: Creation complete after 2s [id=acme-quarterly-reports]
aws_instance.app_server: Modifications complete after 41s [id=i-0a1b2c3d4e5f67890]

Apply complete! Resources: 1 added, 1 changed, 0 destroyed.
```

*An apply session: Terraform shows the plan, waits for `yes`, makes the changes, and reports the result.*

Terraform makes the changes in dependency order (running independent ones at the same time), updates the state as each one completes, and prints any outputs at the end. Only the exact word `yes` proceeds; anything else cancels.

A plan can also be saved to a file and applied later. That guarantees what gets applied is exactly what was reviewed, and not a plan recalculated after something else has changed.

---

## `destroy`: Tear It All Down

`terraform destroy` deletes every resource recorded in this configuration's state, in reverse dependency order. The server goes before the subnet, and the subnet before the network. Like `apply`, it shows what it will remove and waits for `yes`.

Deleting everything sounds like something to avoid, and in production it almost always is. For **non-production environments**, though, it's one of Terraform's most useful features:

- **Cost control.** A test environment that's only needed during working hours, or only for one week of testing, can be destroyed when it's idle and rebuilt when it's needed. Cloud resources that don't exist don't cost anything.
- **Temporary environments.** A team can create a complete, realistic environment to test one feature, then remove it without leaving stray resources behind.
- **Proof that the code is complete.** If an environment can be destroyed and rebuilt from the configuration alone, the configuration really does describe everything. If the rebuild fails, we've found something that was set up by hand.

`destroy` only removes what's in state. Anything created outside Terraform in the same account is left untouched.

---

## The Plan as a Review Artifact

On a team, the plan is more than a preview for the person running it. It's the thing a colleague reads before anything changes, and that review process is where most of Terraform's value lies.

A typical team change looks like this:

1. An engineer edits the configuration on a branch and opens a pull request.
2. A plan is generated for that change and attached to the pull request.
3. A reviewer reads both the code change **and** the plan.
4. Once approved and merged, the change is applied.

The reviewer needs both because they answer different questions. The code change shows what changed *in the files*. The plan shows what will change *in the real world*. Usually these line up, but not always. A one-line edit can trigger the replacement of a database, and drift can cause a plan to include changes nobody wrote. The plan is the only place those differences show up before they happen.

Compare this to the console, where changes go live the moment someone clicks and nobody reviews them first. With Terraform, every infrastructure change can be reviewed, approved, and recorded before it happens, just like a code change.

---

## An Unexpected Destroy Means Stop

The most important habit in reading plans: **if a plan shows a destroy or a replacement that we didn't expect, stop.** Don't approve it and hope for the best.

```text
  # aws_instance.app_server must be replaced
-/+ resource "aws_instance" "app_server" {
      ~ ami = "ami-0123456789abcdef0" -> "ami-0fedcba9876543210" # forces replacement
      ~ id  = "i-0a1b2c3d4e5f67890" -> (known after apply)
        # (26 unchanged attributes hidden)
    }

Plan: 1 to add, 0 to change, 1 to destroy.
```

*A plan to replace a server. Changing its machine image can't be done in place, so Terraform will destroy the existing server and create a new one.*

Some settings can't be changed on an existing resource, so changing them means destroying the resource and building a new one. Terraform marks the setting responsible with `# forces replacement`. For a stateless server, that might be acceptable. For a database, the data it holds may be gone. Even for a server, replacement usually means downtime, a new ID, and a new IP address that other systems may depend on.

Common causes of a destroy nobody intended:

- **A setting that forces replacement**, like the example above.
- **A renamed resource.** Changing a local name from `web` to `app_server` looks cosmetic, but to Terraform the address `aws_instance.web` has disappeared (destroy it) and `aws_instance.app_server` has appeared (create it). Terraform has ways to record a rename so the resource is kept, but only if someone catches it first.
- **Drift.** Someone changed or deleted something by hand, and Terraform is correcting it.
- **The wrong target.** The wrong variables file or state is in use, so Terraform is comparing the configuration against a different environment than intended.

A destroy that shows up in a plan is easy to question and cheap to investigate. Once it's applied, it may not be reversible. The line `Plan: 0 to add, 0 to change, 1 to destroy` on a change that was supposed to add a tag is always worth a second look.

---

## Key Takeaways

- `init` prepares a folder by downloading providers, connecting to the state backend, and fetching modules. It never changes infrastructure.
- `plan` is a dry run: it compares the configuration against state and reality, then lists what would be created (`+`), updated (`~`), destroyed (`-`), or replaced (`-/+`).
- `apply` shows the plan, waits for `yes`, makes the changes in dependency order, and updates state.
- `destroy` removes everything in the configuration's state, which is valuable for non-production environments: it saves cost, and it proves the environment can be rebuilt from code.
- On a team, the plan is the review artifact: it shows what will change in the real world, which the code change alone can't.
- An unexpected destroy or replacement in a plan is a signal to stop and investigate, because applying it may cause downtime or data loss that can't be undone.
