# Sign-Up, Sign-In & User Attributes

How users get into a pool, and the configuration decisions that cannot be changed once it exists.

---

## Decisions Fixed at Creation

Several user pool settings are **permanent**. Changing them means creating a new pool and migrating every user — which is genuinely difficult, because password hashes cannot be exported.

**The sign-in alias.** Whether users sign in with a username, email, phone number, or a preferred username. Fixed at creation.

**Required attributes.** Which standard attributes must be supplied at sign-up. Cannot be changed later.

**Custom attributes.** Names, types, and mutability are fixed at creation. **Custom attributes cannot be removed**, and their maximum length cannot be increased. There is a limit of 50.

**Case sensitivity of usernames.** Fixed at creation, and a source of real support burden if chosen wrongly.

This permanence deserves emphasis because it is unusual. Most AWS configuration is adjustable; user pool schema is not.

**The practical guidance:** think through the attribute set before creating the pool, keep custom attributes minimal, and store application-specific user data in DynamoDB keyed by the Cognito `sub` rather than as pool attributes. That keeps the pool to identity and leaves everything else flexible.

---

## Attributes

**Standard attributes** follow the OIDC specification — `email`, `phone_number`, `given_name`, `family_name`, `address`, `birthdate`, and others.

**Custom attributes** are prefixed `custom:` in tokens:

```json
{
  "sub": "a1b2c3d4-...",
  "email": "user@example.com",
  "custom:tenant_id": "acme-corp",
  "cognito:groups": ["admins"]
}
```

*Custom attributes appear with their prefix. `cognito:groups` is added automatically when the user belongs to groups.*

**Mutability matters.** An attribute declared immutable cannot be changed after the user is created — appropriate for a tenant identifier that must not change, and a problem for anything that might.

---

## Sign-Up Flows

**Self-service sign-up.** Users register themselves, then verify their email or phone with a code. The standard consumer flow.

**Administrator-created users.** Sign-up is disabled and an administrator creates accounts, optionally with a temporary password. The standard flow for internal or B2B applications.

**Imported users.** A CSV import creates users without passwords; each must reset on first sign-in.

**Migration on sign-in.** A Lambda trigger authenticates against an existing system on first sign-in and creates the Cognito user transparently. This is the mechanism for migrating from another provider without forcing every user to reset their password.

---

## Sign-In and Auth Flows

Several flows exist, and choosing correctly matters for security:

**`USER_SRP_AUTH`** — Secure Remote Password. The password is never transmitted; a cryptographic proof is exchanged instead. **This is the correct choice for client applications.**

**`USER_PASSWORD_AUTH`** — the password is sent to Cognito over TLS. Simpler, and it means the password passes through the client's request. Acceptable for server-side flows and for migration, and worth avoiding in browsers where SRP is available.

**`ADMIN_USER_PASSWORD_AUTH`** — a server-side flow using AWS credentials, for backends authenticating on a user's behalf.

**`REFRESH_TOKEN_AUTH`** — exchanges a refresh token for new tokens.

The Amplify libraries use SRP by default, which is one reason to use them rather than calling the API directly.

---

## Lambda Triggers

Cognito invokes Lambda functions at points in the lifecycle, which is where customization happens:

| Trigger | Use |
|---|---|
| Pre sign-up | Auto-confirm users, restrict to an email domain |
| Post confirmation | Create a user record in the application database |
| Pre authentication | Block sign-in based on custom rules |
| Post authentication | Log sign-ins, update last-seen |
| Pre token generation | **Add or modify claims in the token** |
| User migration | Authenticate against a legacy system |
| Custom message | Customize verification emails |

**Pre token generation** is the most useful. It adds application-specific claims — a tenant ID, a role, a subscription tier — so the API receives them in the token and needs no additional lookup:

```python
def handler(event, context):
    event["response"]["claimsOverrideDetails"] = {
        "claimsToAddOrOverride": {
            "tenant_id": lookup_tenant(event["userName"]),
        }
    }
    return event
```

*Adds a claim at token issuance. The API then reads it from the validated token rather than querying a database per request.*

**Post confirmation** is the other common one — creating the application's own user record when a Cognito user is confirmed, keeping the two in step.

---

## Security Settings

**Password policy** — length and character requirements, configurable after creation.

**MFA** — off, optional, or required. SMS and TOTP are supported. Setting it to required for all users cannot be applied retroactively without disrupting existing users, so optional-with-encouragement is the usual path.

**Advanced security features** detect compromised credentials, unusual sign-in locations, and risk signals, and can block or require MFA in response. They are charged per monthly active user.

**Account recovery** — email, SMS, or both. Choosing SMS only creates a recovery problem for users who change phone numbers.

---

## Key Takeaways

- Sign-in aliases, required attributes, custom attributes, and username case sensitivity are fixed at pool creation.
- Custom attributes cannot be removed or lengthened, and are limited to 50.
- Store application-specific user data in a database keyed by `sub`, keeping the pool to identity.
- Immutable attributes cannot be changed after user creation.
- Prefer `USER_SRP_AUTH` for client applications, which never transmits the password.
- The user migration trigger allows moving from another provider without forcing password resets.
- Pre token generation adds custom claims so the API needs no per-request lookup.
- Post confirmation is the standard place to create the application's own user record.
