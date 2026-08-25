# Hallucination Mitigation (Cloud Solutions)

A **hallucination** is AI output that is confidently, fluently wrong — an API that doesn't exist, a config key that was never real, a service capability the vendor never shipped. Cloud work is the technique's natural predator habitat: hundreds of AWS/Azure/GCP services, each with sprawling APIs, permission systems, and quarterly-changing details — trained into the model at various past moments, generalized into plausible patterns. The model has seen so many `aws.something.enable=true` keys that inventing one more is effortless. This lesson is the field guide: why cloud specifically, what fabrications look like, and the verification reflexes that catch them.

---

## Why Cloud Contexts Are Worst-Case

Three properties compound. **Scale and churn:** cloud platforms are too large and too fast-moving for any training snapshot to hold accurately — services rename, SDKs re-version (v1 vs. v2 idioms blur together), features launch and deprecate monthly. **Pattern regularity:** cloud APIs are *almost* consistent — `getX`/`putX`/`listX` families, predictable config naming — which is exactly what lets a model generate the method that *should* exist by analogy but doesn't. **Deferred failure:** unlike a hallucinated Java method (compile error, seconds), a hallucinated IAM action, config key, or service behavior fails at *deploy or runtime* — sometimes silently (an ignored config key simply does nothing), in the environment where debugging is slowest and stakes are highest.

The classic specimens, worth recognizing on sight:

- **Invented SDK methods/parameters** — plausible names on real clients (`s3Client.enableAutoArchive(...)`); mixed-version chimeras that compile against neither SDK.
- **Fabricated config keys** — Spring/Terraform/CloudFormation properties that parse fine and *do nothing*; the silent killer, because nothing errors.
- **Imaginary IAM permissions** — actions that don't exist, or policies subtly broader than the named intent ("read access" that includes `s3:*`).
- **Capability fiction** — "service X natively supports Y" where Y is a roadmap item, a different service's feature, or a blog-post wish. Costliest of all when it shapes an architecture decision.
- **Stale defaults presented as current** — limits, pricing tiers, region availability recited from training-time reality.

---

## The Mitigation Stances

**1. Prompt against it.** Reduce the invitation to invent: pin versions in the ask ("AWS SDK for Java 2.25, not v1"), and — the highest-value habit — *license honesty*: **"If you're not certain this API/key/permission exists, say so rather than guessing."** Models hedge usefully when the door is opened; unprompted, they fill gaps with confidence. Follow-ups that force self-audit help too: *"Which parts of that answer are you least certain exist exactly as written?"* — the flagged items are the verification list.

**2. Verify against ground truth, proportional to feedback speed.** The rule: *the slower a fabrication would surface on its own, the faster we check it by hand.* SDK calls get caught by the compiler — cheap, let it. Config keys get checked against the official reference *before* deploy (a key the docs don't list is a fabrication until proven otherwise). Capability claims — "the service supports X" — get verified in vendor documentation *before any architecture depends on them*, full stop. IAM changes get tested with the platform's policy simulator/dry-run tooling. Tool-assisted lookups (Copilot's web-grounded modes, docs-connected MCP tools where available) shift answers from memory to retrieval — prefer them for anything version-sensitive.

**3. Design so hallucinations die early.** The structural layer: pin dependency versions so chimera code can't compile; keep infrastructure-as-code in CI with `plan`/`validate` steps so fabricated resources fail *before* apply; smoke-test cloud integrations in a sandbox account where a wrong call costs nothing; and route all AI-suggested IAM through least-privilege review — a fabricated permission that's also over-broad is two problems wearing one trench coat.

The stance to carry forward: for stable, local, compiler-checked code, AI memory is usually fine; for cloud specifics, treat every proper noun — service, method, key, permission, limit — as **a claim requiring a citation**, where the citation is current official documentation or a passing execution. That calibration instinct travels well beyond cloud, and the next topic applies it to another trust-critical task: translating code across languages while proving the behavior survived.
