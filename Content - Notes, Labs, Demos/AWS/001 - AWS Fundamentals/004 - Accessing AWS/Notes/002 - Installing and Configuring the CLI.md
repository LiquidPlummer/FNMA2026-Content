# Installing and Configuring the CLI

The AWS CLI needs three pieces of configuration before it can do anything: credentials, a region, and (optionally) an output format. Understanding where each lives makes credential problems much faster to diagnose.

---

## Installation

Version 2 is current; version 1 is legacy and should not be used for new work. AWS distributes v2 as a standalone installer for macOS, Windows, and Linux, so no Python installation is required.

```bash
aws --version
# aws-cli/2.15.30 Python/3.11.8 Darwin/23.4.0 exe/x86_64
```

*Confirms both that the CLI is installed and which major version — the first number should be 2.*

---

## The Two Configuration Files

Configuration lives in two files in `~/.aws/` (`%USERPROFILE%\.aws\` on Windows):

**`~/.aws/credentials`** holds secrets:

```ini
[default]
aws_access_key_id = AKIAIOSFODNN7EXAMPLE
aws_secret_access_key = wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

[training]
aws_access_key_id = AKIAI44QH8DHBEXAMPLE
aws_secret_access_key = je7MtGbClwBF/2Zp9Utk/h3yCo8nvbEXAMPLEKEY
```

*Each bracketed section is a named profile; the `default` profile is used when no profile is specified.*

**`~/.aws/config`** holds everything else:

```ini
[default]
region = us-east-1
output = json

[profile training]
region = eu-west-1
output = table
```

*Note the asymmetry: profiles are named `[training]` in the credentials file but `[profile training]` in the config file. This inconsistency is a common source of "my profile isn't working."*

Splitting the files exists so the config file, which contains no secrets, can be shared or committed while the credentials file is not.

---

## Setting It Up

`aws configure` writes both files interactively:

```bash
aws configure --profile training
# AWS Access Key ID [None]: AKIAI44QH8DHBEXAMPLE
# AWS Secret Access Key [None]: je7MtGbClwBF/2Zp9Utk/h3yCo8nvbEXAMPLEKEY
# Default region name [None]: eu-west-1
# Default output format [None]: json
```

*Creates or updates the named profile in both files; omitting `--profile` targets `default`.*

---

## Using Profiles

Select a profile per command, or for a whole shell session:

```bash
aws s3 ls --profile training              # one command
export AWS_PROFILE=training               # every command in this shell
```

*Per-command selection is safer when several accounts are in play, since an exported variable persists and is easy to forget about.*

Profiles are how a single workstation talks to several accounts — development, staging, production — without editing files between commands. Naming them after the account or environment rather than `default` and `default2` prevents a class of expensive mistakes.

### Roles in profiles

A profile can assume a role instead of holding its own keys:

```ini
[profile prod-admin]
role_arn = arn:aws:iam::123456789012:role/AdminRole
source_profile = default
region = us-east-1
mfa_serial = arn:aws:iam::111122223333:mfa/kyle
```

*The CLI uses `default`'s credentials to assume `AdminRole`, prompting for an MFA code, and caches the temporary credentials that come back.*

This is the preferred arrangement: long-lived keys exist in one place, and access to other accounts flows through roles that issue short-lived credentials.

---

## Verifying

`sts get-caller-identity` is the single most useful diagnostic command in AWS:

```bash
aws sts get-caller-identity --profile training
# {
#     "UserId": "AIDACKCEVSQ6C2EXAMPLE",
#     "Account": "123456789012",
#     "Arn": "arn:aws:iam::123456789012:user/kyle"
# }
```

*Reports which identity and account the CLI is actually using — the first thing to check when a command fails or when about to do something irreversible.*

Running it before any destructive command is a cheap habit that prevents running production commands against the wrong account.

---

## Key Takeaways

- Use AWS CLI v2; v1 is legacy.
- Credentials live in `~/.aws/credentials` and everything else in `~/.aws/config`; the config file uses `[profile name]` while the credentials file uses `[name]`.
- `aws configure --profile <name>` writes both files; profiles let one workstation address several accounts.
- Select a profile with `--profile` per command or `AWS_PROFILE` for a session.
- A profile can assume a role via `role_arn` and `source_profile`, which is preferable to storing keys per account.
- `aws sts get-caller-identity` confirms which identity is in use and should be run before anything irreversible.
