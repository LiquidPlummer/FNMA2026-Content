# Where Cognito Is the Wrong Tool

Cognito is capable and has real rough edges. Knowing them before committing avoids discovering them after users exist — at which point migration is genuinely difficult.

---

## Migration Is Very Hard

The most important constraint, and the reason to think carefully at the start.

**Password hashes cannot be exported from a user pool.** Not to another Cognito pool, not to another provider. This means:

- Migrating away from Cognito requires every user to reset their password, or an authentication proxy against the old pool during a transition.
- Changing a setting fixed at pool creation — sign-in alias, required attributes, username case sensitivity — means creating a new pool, with the same problem.
- Merging two pools is not really possible.

The user migration Lambda trigger handles *incoming* migration well and does nothing for outgoing. This asymmetry makes the initial pool configuration unusually consequential.

---

## Where Cognito Fits Poorly

**Highly customized sign-in experiences.** The hosted UI customizes very little. Building custom flows means implementing verification, MFA challenges, password reset, and every error path against the API — substantially more work than it first appears, and the edge cases are where it accumulates.

**Complex multi-tenancy.** A pool per tenant hits pool limits and multiplies configuration; one pool with a tenant attribute means tenant isolation is entirely the application's responsibility, and a mistake crosses tenants. Neither arrangement is comfortable, and a purpose-built B2B identity provider handles it better.

**Fine-grained authorization.** Groups are coarse labels. Per-resource permissions, roles that vary by context, or delegated administration all belong in the application, meaning Cognito supplies only authentication and the authorization system is built anyway.

**Rich user profile management.** Custom attributes are limited to 50, fixed in type at creation, and cannot be removed. Anything beyond basic identity belongs in a separate database — which many teams end up doing regardless, leaving Cognito holding only credentials.

**Immediate revocation.** Access and ID tokens cannot be revoked. A user removed from a group or disabled entirely retains a valid token until it expires. Where revocation must be immediate, the application must check current state per request, which undoes much of the benefit of stateless tokens.

**Developer experience.** The API is inconsistent, the documentation is uneven, error messages are frequently unhelpful, and behavior differs between the hosted UI and direct API use. This is a real, ongoing cost that is hard to appreciate before encountering it.

---

## Where It Fits Well

**Standard web and mobile applications** with conventional sign-up and sign-in, where the flow does not need to be distinctive.

**Applications already committed to AWS**, particularly with API Gateway — the integration is genuinely seamless and requires no code.

**Applications needing SAML federation**, where implementing SAML correctly is otherwise substantial work.

**Applications needing direct AWS access from clients**, where identity pools have no real alternative.

**Cost-sensitive applications at scale.** Pricing is competitive, with a generous free tier, and it is often meaningfully cheaper than commercial alternatives at high user counts.

---

## The Alternatives

| Option | Suits |
|---|---|
| **Auth0, Okta** | Better developer experience, richer features, higher cost |
| **Firebase Auth** | Strong mobile SDKs, simpler API |
| **Keycloak** | Self-hosted, fully controllable, operational burden |
| **Roll your own** | Almost never the right answer |

That last row deserves emphasis. Building authentication means correctly implementing password hashing, reset flows, MFA, session management, rate limiting, and account enumeration protection — and getting any of them wrong has serious consequences. A managed provider with rough edges is preferable to a bespoke system with security defects.

Note also that API Gateway's JWT authorizer accepts **any OIDC provider**, so choosing Auth0 or Okta over Cognito does not forfeit the API Gateway integration. Only identity pool functionality is Cognito-specific.

---

## Deciding

Two questions settle it in most cases:

**Is authentication a differentiating part of the product?** If the sign-in experience is part of what the product is, Cognito's constraints will be felt constantly. If it is a necessary component that should work and be forgotten, Cognito is fine.

**How much would migrating away cost?** Given that password hashes cannot be exported, this is a decision to make deliberately at the start rather than one to revisit later.

---

## Key Takeaways

- Password hashes cannot be exported, so migrating away from a user pool forces every user to reset their password.
- Settings fixed at pool creation cannot be changed without creating a new pool and facing the same migration problem.
- The hosted UI customizes very little, and building custom flows is more work than it appears.
- Multi-tenancy, fine-grained authorization, and rich user profiles all fit Cognito poorly.
- Access and ID tokens cannot be revoked, so immediate revocation requires per-request checks.
- Cognito fits standard applications on AWS, SAML federation, and clients needing direct AWS access.
- API Gateway's JWT authorizer works with any OIDC provider, so alternatives keep the integration.
- Building authentication from scratch is almost never the right choice.
