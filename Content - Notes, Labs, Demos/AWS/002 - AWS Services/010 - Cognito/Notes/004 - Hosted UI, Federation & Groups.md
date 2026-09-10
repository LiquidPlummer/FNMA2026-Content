# Hosted UI, Federation & Groups

Three related capabilities: letting Cognito provide the sign-in screens, accepting identities from other providers, and grouping users for authorization.

---

## Hosted UI

Cognito provides hosted sign-in, sign-up, and password reset pages at a Cognito domain or a custom one:

```
https://auth.example.com/login?client_id=...&response_type=code&redirect_uri=https://app.example.com/callback
```

*The user authenticates on Cognito's pages and returns to the application with an authorization code, which is exchanged for tokens.*

**What it provides:** working sign-in, sign-up, verification, MFA, and password reset with no code; the OAuth 2.0 authorization code flow implemented correctly; and social and enterprise federation as configuration.

**What it costs:** limited customization — a logo, CSS, and little else. The pages live on a different domain, which is visible to users, and the flow involves redirects that complicate single-page application routing.

**Use the hosted UI** for internal tools, B2B applications, and anything where federation matters more than branding — and especially where SAML or social login is required, since implementing those correctly is substantial work.

**Build custom flows** where the sign-in experience is part of the product, using the Cognito API directly or through the Amplify libraries. This means implementing sign-up, verification, MFA challenges, password reset, and error handling — considerably more work than it first appears, and mostly in the edge cases.

A reasonable middle path is custom UI for username and password, with the hosted UI's redirect flow for federated providers.

---

## Federation

A user pool can accept identities from external providers, mapping them into pool users:

**Social providers** — Google, Facebook, Amazon, Apple. Configured with the provider's client ID and secret.

**SAML 2.0** — enterprise identity providers such as Okta, Azure AD, and ADFS. The standard requirement for B2B and internal applications.

**OIDC** — any OIDC-compliant provider.

The mechanism is uniform: the user authenticates with the external provider, Cognito receives the assertion, and it creates or updates a user in the pool. **The application always receives Cognito tokens**, regardless of provider — which is the main benefit, since the application implements one token format and providers are added by configuration.

**Attribute mapping** connects provider claims to pool attributes:

```
Provider claim          →  Pool attribute
email                   →  email
given_name              →  given_name
http://schemas.../group →  custom:department
```

*Mapping is configured per provider. An unmapped attribute is not available in the token.*

Two practical points. **Linking accounts across providers is awkward** — the same person signing in with Google and then with a password produces two distinct users unless account linking is configured deliberately. And **federated users are managed by their provider**; disabling someone in Okta prevents new sign-ins, while existing tokens remain valid until they expire.

---

## Groups

A **group** is a named collection of users, with an optional IAM role and a precedence value.

```bash
aws cognito-idp create-group --user-pool-id us-east-1_ABC123 \
  --group-name admins --precedence 1 \
  --role-arn arn:aws:iam::123456789012:role/CognitoAdminRole
```

*Precedence resolves which role applies when a user is in several groups — lower numbers win.*

Group membership appears in tokens as `cognito:groups`, which is how it reaches an API:

```json
{ "sub": "a1b2c3d4-...", "cognito:groups": ["admins", "beta-testers"] }
```

Two uses:

**Application authorization.** The API reads `cognito:groups` and decides. This is the common case, and it is ordinary claim-based authorization — the group is a claim like any other.

**IAM role selection via an identity pool.** The group's associated role determines which AWS credentials the identity pool issues, which is how tiered direct AWS access is implemented.

---

## What Groups Are Not

**They are not a permission system.** A group is a label. The application decides what `admins` means, and Cognito enforces nothing.

**They do not scale to fine-grained authorization.** Groups work for a handful of coarse roles. Per-resource permissions — who may edit which document — belong in the application's own data, not in Cognito.

**Membership is a claim in an issued token**, so removing someone from a group does not take effect until their token expires and is refreshed. For immediate revocation, the application must check current membership rather than trusting the token's claim.

That last point is worth weighing. Token-carried group claims avoid a per-request lookup and accept up-to-an-hour staleness. Checking membership per request is current and costs a lookup. Which is right depends on how quickly a revocation must take effect.

---

## Key Takeaways

- The hosted UI provides working sign-in, sign-up, MFA, and password reset with minimal customization available.
- Building custom flows means implementing verification, MFA challenges, reset, and error handling — more work than it appears.
- Federation supports social, SAML, and OIDC providers, and the application always receives Cognito tokens regardless of source.
- Attribute mapping is per provider, and unmapped claims are unavailable in tokens.
- Account linking across providers requires deliberate configuration, or the same person becomes two users.
- Groups appear in tokens as `cognito:groups` and can select an IAM role via an identity pool, with precedence resolving conflicts.
- Groups are labels, not permissions, and do not suit fine-grained per-resource authorization.
- Group changes take effect only when tokens refresh, so immediate revocation requires a per-request check.
