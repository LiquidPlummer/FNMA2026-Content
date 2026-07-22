# Bitbucket as a Repository Store — Overview Notes

## What Bitbucket Is

Bitbucket is Atlassian's Git repository hosting platform. It provides source control, pull requests, branch management, and access control, and integrates natively with other Atlassian tools (primarily Jira).

## Cloud vs. Data Center

Bitbucket has two current offerings:

- **Bitbucket Cloud** — Atlassian-hosted SaaS.
- **Bitbucket Data Center** — self-hosted, deployed on the organization's own infrastructure. This is the current on-premises option (Bitbucket Server was retired in February 2024). Data Center is the typical choice for regulated industries, air-gapped networks, and organizations with strict internal security requirements — likely what's meant by an "internal Bitbucket" instance.

## Core Concepts

- **Project** — a top-level grouping of related repositories (e.g., all repos for one product or team).
- **Repository** — a single Git repo, containing branches, commits, and history.
- **Branch** — an isolated line of development; typically a main/default branch plus feature, release, and hotfix branches.
- **Pull Request (PR)** — the mechanism for proposing, reviewing, and merging changes into a branch.
- **Fork** — a personal or team copy of a repository, used when contributors don't have direct write access to the source repo.

## Access Control & Security

Because this is a security-minded environment, permissions are enforced at multiple levels:

- **Project and repository permissions** — control who can read, write, or administer at each level.
- **Branch permissions/restrictions** — protect key branches (e.g., main) from direct pushes, force-pushes, or deletion; often require PRs to merge.
- **Merge checks** — required approvals, passing builds, and minimum reviewer counts before a PR can merge.
- **Authentication** — SSH keys or personal/app access tokens for Git operations; MFA/SSO typically enforced at the account level.
- **Audit logging** — Data Center instances typically log repository access, permission changes, and administrative actions for compliance review.
- **IP allowlisting** — internal instances often restrict access to the corporate network or VPN.

## Jira Integration

Bitbucket and Jira are commonly linked so that:

- Branches and commits can reference a Jira issue key (e.g., `PROJ-123`) to automatically link them to that issue.
- **Smart Commits** allow commit messages to transition issue status, add comments, or log work directly from Git.
- Jira issues show linked branches, commits, builds, and PRs, giving traceability from a ticket through to the code that resolved it.

## CI/CD Note

Bitbucket Cloud includes **Bitbucket Pipelines** for built-in CI/CD. Bitbucket Data Center does not include Pipelines — internal/on-prem instances typically pair Bitbucket with a separate CI/CD tool (e.g., Bamboo, Jenkins) instead.

## Practical Best Practices

- Never commit secrets, credentials, or keys — use the organization's secrets manager.
- Keep `.gitignore` current to avoid accidentally committing build artifacts or local config.
- Write descriptive commit messages, ideally referencing the related Jira issue.
- Respect branch protection rules rather than seeking workarounds — they exist for audit and stability reasons in security-sensitive environments.
- Use PRs even for small changes when working in shared/protected repos, to preserve review history.