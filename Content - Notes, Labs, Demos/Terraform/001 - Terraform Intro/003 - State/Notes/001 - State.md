# State

Terraform is declarative: we describe what should exist, and Terraform works out what to change. To work anything out, though, it needs to know what it has already built. That knowledge lives in **state**, and state is behind most of the things that go wrong with Terraform in practice.

---

## The State File

The **state file** is Terraform's record of every resource it manages. By default, it's a JSON file named `terraform.tfstate` in the folder where Terraform runs. For each resource in the configuration, it records the real object that resource corresponds to and that object's current settings.

```json
{
  "version": 4,
  "resources": [
    {
      "mode": "managed",
      "type": "aws_instance",
      "name": "app_server",
      "instances": [
        {
          "attributes": {
            "id": "i-0a1b2c3d4e5f67890",
            "instance_type": "t3.micro",
            "private_ip": "10.0.1.27"
          }
        }
      ]
    }
  ]
}
```

*A heavily trimmed state file. It records that the resource `aws_instance.app_server` is the real EC2 instance `i-0a1b2c3d4e5f67890`, along with its settings.*

That link between an address in our configuration (`aws_instance.app_server`) and a real object in the cloud (`i-0a1b2c3d4e5f67890`) is the heart of it. The configuration says what *should* exist. The state says what Terraform *built*, and what it believes currently exists.

Nobody edits this file by hand. Terraform writes it every time it makes a change.

---

## Why State Has to Exist

Picture Terraform without state. We've removed a server from our configuration. Terraform should delete the server, but which one? An AWS account might have hundreds of servers, some built by other teams, some by hand, some by other Terraform configurations. Nothing in the configuration identifies the real server that used to belong to the deleted block, because the block is gone.

State answers that question. Every time Terraform decides what to do, it works through three things:

1. **The configuration**: what we've said should exist.
2. **The state**: what Terraform built, and which real object each resource is.
3. **The real world**: Terraform asks the providers for the current details of every object recorded in state, to catch anything that has changed.

The difference between the configuration and that up-to-date picture is exactly the set of changes Terraform needs to make. A resource in configuration but not in state needs to be created. A resource in state but no longer in configuration needs to be destroyed. A resource in both, with different settings, needs to be updated.

Without state, Terraform couldn't compute any of that. With it, Terraform manages only what it built, and leaves everything else in the account alone.

---

## When Reality and State Disagree

Terraform assumes it's the only thing changing the resources it manages. When someone changes one of those resources another way, usually by hand in the console, the real world no longer matches what the configuration says. This is called **drift**.

Terraform notices drift the next time it checks, and its response is always the same: make reality match the configuration again. That's the declarative model working as designed, but the results can surprise people:

| Someone, by hand... | What Terraform sees | What Terraform proposes next time |
|---|---|---|
| Changes a server from `t3.micro` to `t3.large` to fix a slowdown | The real server differs from the configuration | Change it back to `t3.micro`, undoing the fix |
| Deletes a resource | An object in state no longer exists | Create it again |
| Creates a resource in the console | Nothing: it isn't in state | Nothing. Terraform doesn't know it exists, and it stays undocumented |

The first row is the classic story. During an incident, someone makes a quick fix in the console. Days later, a colleague runs Terraform for an unrelated change, and the fix is quietly reverted along with it. Drift is the most common source of Terraform pain because the person who made the hand change and the person who runs into the consequences are often different people, days or weeks apart.

The rule that avoids nearly all of this is simple: **once Terraform manages a resource, change it only through Terraform.** If a console fix is truly urgent, the configuration gets updated to match right afterward.

---

## Remote State

A state file on one engineer's laptop works fine, as long as only one engineer ever runs Terraform.

The moment a second engineer joins, it breaks. They clone the repository and get the configuration, but not the state file, which lives only on the first engineer's machine. Terraform on the second machine has no record of anything and concludes that nothing exists. Its plan is to create everything from scratch: duplicate resources, or a stream of errors as it collides with names that are already taken.

Committing the state file to Git looks like a fix, but it isn't. Two engineers working on different branches end up with conflicting copies, and someone running with an out-of-date copy is back in the same trouble. And as we'll see shortly, the file contains secrets that don't belong in a repository.

The real fix is **remote state**: storing the state file in a shared location that everyone's Terraform reads from and writes to. Where state is kept is controlled by a **backend** setting in the configuration:

```hcl
terraform {
  backend "s3" {
    bucket       = "acme-terraform-state"
    key          = "network/terraform.tfstate"
    region       = "us-east-1"
    use_lockfile = true
  }
}
```

*A backend setting that stores this configuration's state in an S3 bucket, at the path `network/terraform.tfstate`, with locking enabled.*

With a remote backend, every engineer and every automated pipeline works from the same, current state. Common backends include an S3 bucket on AWS, Azure Blob Storage, Google Cloud Storage, and **HCP Terraform** (formerly Terraform Cloud), HashiCorp's hosted service.

For contrast, CloudFormation doesn't make us deal with this at all. AWS keeps track of what each CloudFormation stack built, inside AWS itself. Managing state ourselves is part of the price of Terraform's flexibility.

---

## State Locking

Shared state solves one problem and creates another: two people can now run Terraform against the same state at the same moment.

If both read the state, both start making changes, and both write their results back, the second write overwrites the first. The saved state no longer describes what actually exists. Resources that were created may be missing from it, leaving them unmanaged, and the next run makes decisions based on a false picture.

**State locking** prevents this. Before Terraform changes anything, it takes a lock on the state. If a colleague is already mid-run, our run stops with an error saying the state is locked, and we wait until they finish. Only one change can be in progress at a time.

Most remote backends support locking, and it's normally switched on as part of setting up the backend (the `use_lockfile = true` line above). Older S3 setups use a separate DynamoDB table for the lock, so we'll likely see that pattern in existing configurations too.

---

## State Is Sensitive

The state file records the real values of every resource's attributes, and some of those values are secrets. If a configuration creates a database with a master password, that password is in the state file. If it generates an access key or a private key, those are in there too, stored as plain text.

Terraform can mark values as `sensitive`, which hides them from what it prints to the screen. It doesn't remove them from state.

So state gets treated like a credential store:

- **Never commit it to version control.** Terraform projects routinely list `*.tfstate` in `.gitignore`.
- **Store it encrypted.** For example, turn on encryption for the S3 bucket that holds it.
- **Restrict who can read it.** Reading state can be as good as reading every secret the configuration handles.

---

## Key Takeaways

- The state file is Terraform's record of what it built: it links each resource address in the configuration to a real object and records that object's settings.
- Terraform compares the configuration against state (updated with current reality) to work out what to create, update, or destroy. Without state, it couldn't tell which real objects are its own.
- Drift happens when someone changes managed resources outside Terraform. The next run reverts those changes, which is the most common source of Terraform pain.
- A state file on one laptop breaks as soon as a second person runs Terraform; remote state in a shared backend (like S3) fixes that.
- State locking lets only one run change the state at a time, which prevents two simultaneous applies from corrupting it.
- State contains real values, including secrets, in plain text. Keep it out of Git, encrypt it, and restrict access to it.
