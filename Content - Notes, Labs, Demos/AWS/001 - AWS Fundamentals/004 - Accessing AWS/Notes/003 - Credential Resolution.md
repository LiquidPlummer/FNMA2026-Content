# Credential Resolution

Every AWS client — CLI or SDK — looks for credentials in a fixed order and uses the first source it finds. Knowing that order turns "why is it using the wrong account?" from a mystery into a two-minute check.

---

## The Resolution Order

Roughly, from highest precedence to lowest:

1. **Explicit parameters in code.** Credentials passed directly to an SDK client constructor.
2. **Environment variables.** `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_SESSION_TOKEN`.
3. **The credentials file.** `~/.aws/credentials`, for the selected profile.
4. **The config file.** `~/.aws/config` — including SSO and assume-role profile configuration.
5. **Container credentials.** The ECS/EKS task role, via a container-local endpoint.
6. **Instance metadata (IMDS).** The instance profile attached to an EC2 instance.

The first source that yields credentials wins, and the search stops there.

---

## Why the Order Causes Problems

The order itself is sensible. The trouble comes from sources being present without anyone remembering they are.

**Stale environment variables.** Someone exports `AWS_ACCESS_KEY_ID` for a one-off task, and every subsequent command in that shell uses it — silently overriding `--profile`, which only selects among file-based profiles.

```bash
# --profile appears to be ignored, because environment variables outrank it
env | grep AWS
unset AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_SESSION_TOKEN
```

*When a profile seems to have no effect, checking for and clearing AWS environment variables resolves it most of the time.*

**Local keys on an EC2 instance.** An instance has a perfectly good instance profile, but someone once ran `aws configure` on it and left keys in `~/.aws/credentials`. Those outrank instance metadata, so the instance uses a personal identity — which then expires, or gets deleted, and the application breaks in a way that has nothing to do with the change that triggered it.

**Expired session tokens.** Temporary credentials in environment variables produce authentication errors that look like permission problems. The distinguishing detail is in the error text: `ExpiredToken` rather than `AccessDenied`.

---

## Where Credentials Should Come From

The rule worth internalizing: **code running on AWS should never hold long-lived keys.**

| Where the code runs | Credential source |
|---|---|
| EC2 instance | Instance profile (IMDS) |
| ECS / EKS task | Task role |
| Lambda function | Execution role |
| Developer workstation | SSO, or a profile that assumes a role |
| CI/CD pipeline | OIDC federation to a role |

Every entry above the workstation line delivers **temporary, automatically rotated** credentials. Nothing needs to store a secret, nothing needs to rotate one, and there is no key to leak in a repository or a log.

Long-lived access keys are appropriate in a narrowing set of cases — mostly external systems that genuinely cannot federate. When one is unavoidable, it should be scoped narrowly and rotated on a schedule.

---

## Diagnosing

Two commands answer most credential questions:

```bash
aws sts get-caller-identity          # which identity is actually in use
aws configure list                   # which source each setting came from
```

*`get-caller-identity` names the identity; `configure list` shows where each value was resolved from — `env`, `shared-credentials-file`, `iam-role`, and so on.*

The `Type` column in `aws configure list` output is what makes it useful: it says *which* source won, which is exactly the information the resolution order determines.

---

## Key Takeaways

- Clients resolve credentials in a fixed order — explicit parameters, environment variables, credentials file, config file, container credentials, then instance metadata — and stop at the first match.
- Environment variables outrank profiles, so stale exported variables silently override `--profile`.
- Local key files on an EC2 instance override its instance profile and cause failures that look unrelated to their cause.
- Code running on AWS should use instance profiles, task roles, or execution roles, which supply temporary, auto-rotating credentials.
- `aws sts get-caller-identity` shows which identity is in use, and `aws configure list` shows which source it came from.
