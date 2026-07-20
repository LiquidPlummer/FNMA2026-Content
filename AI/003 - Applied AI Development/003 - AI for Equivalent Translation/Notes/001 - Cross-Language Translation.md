# Cross-Language Translation

Porting code between languages and frameworks — Python utility to Java service, legacy Struts to Spring Boot, Java 8 idioms to modern Java — is historically slow, tedious, and error-prone. AI translates fluently and fast, which moves the entire difficulty to one word: **equivalent**. A translation that compiles and looks idiomatic but behaves differently on edge cases is worse than no translation, because it *passes review*. The discipline of this lesson is defining equivalence, prompting for it, and proving it.

---

## Fluent Translation ≠ Equivalent Behavior

Languages disagree in exactly the places a line-by-line translation looks clean:

- **Numerics:** Python's arbitrary-precision integers vs. Java's overflowing `int`; float division semantics (`7 / 2`); rounding modes (Java's `Math.round` half-up vs. Python's banker's rounding — a real money-code landmine).
- **Nulls and absence:** `None` vs. `null` vs. `Optional`; a missing dict key returns `None` in `.get()` but throws in Java's... unless it's a `Map.get` returning null. Every absence path needs a decision.
- **Strings and collections:** Unicode handling, default sort stability, iteration order of hash structures, negative indexing that silently becomes an exception.
- **Error philosophy:** exceptions vs. error returns vs. checked exceptions; a caught-and-defaulted error in the source may become an uncaught crash in the target.
- **Framework semantics** (for framework migrations): lifecycle timing, transaction boundaries, threading model — Struts action instantiation per-request vs. Spring singletons is a *state bug generator* if ported naively.

An AI translating line-by-line preserves the *text structure* and silently resolves these divergences to the target language's defaults — which is precisely where equivalence dies.

---

## Prompting the Translation

Brief it as a behavior-preservation task, not a rewriting task:

```text
Translate this Python module to Java 21 (#file:pricing.py).

Priorities, in order:
1. Behavioral equivalence — including edge cases: empty inputs,
   None/null paths, integer overflow, rounding. Where Python and Java
   semantics differ, FLAG the line and state which behavior you preserved.
2. Idiomatic Java — records, streams, Optional where natural; this is
   a translation, not a transliteration (no Python-shaped Java).
3. Same public behavior, but naming per Java conventions.

Before the code: list the semantic differences you had to resolve
and any behavior you could not preserve exactly.
```

*The equivalence brief: priorities ranked, divergences surfaced as a required deliverable, and idiom explicitly licensed — flagged decisions instead of silent ones.*

The divergence list is the prompt's most valuable output — it converts silent resolutions into reviewable decisions ("Python's `round(2.5)` gives 2; I used `RoundingMode.HALF_EVEN` to match — confirm this matters for billing"). The "not a transliteration" line matters in the other direction: without it, we get Java wearing a Python costume — dict-of-lists structures where a record belongs.

---

## Proving Equivalence

The proof is never inspection; it's **the same inputs producing the same outputs**, mechanized:

1. **Port the test suite first** (or write one — the AI TDD lesson's move). Source-language tests, translated, become the equivalence oracle: run them against the *new* code and every red is a divergence found cheaply. Review these translations hardest of all — a test whose *expected values* got "resolved to Java defaults" verifies the wrong behavior into place.
2. **Golden-file the gnarly parts.** For pure functions (pricing, parsing, formatting): run the *original* over a generated input corpus (seed-data techniques apply — hostile strings, boundary numbers included), capture outputs to a file, assert the Java port reproduces them exactly. This catches the rounding/overflow class nothing else does.
3. **Parallel-run when stakes justify it.** For live migrations: run old and new side by side on real traffic, compare outputs, alarm on divergence — the industrial-strength version of the same idea.
4. **Interrogate the remainder:** *"What inputs would make your translation and the original disagree?"* — the self-critique prompt, aimed at the exact risk.

Translation at scale (a whole module tree) becomes a planned, chunked workflow — translate leaf utilities first with their tests, then upward — which is task planning plus this lesson, applied per chunk. And the broader skill underneath — showing the AI existing patterns so output lands in *our* idiom — is precisely the next unit's subject: in-context learning.
