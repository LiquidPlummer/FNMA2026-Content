# Teaching by Example

An LLM's weights are frozen — it will never be *trained* on our codebase. Yet within a single conversation, it can learn our patterns almost perfectly. That capability is **in-context learning**: everything placed in the context window functions as live, temporary training data, and the model imitates what it sees there with remarkable fidelity. Few-shot prompting (Unit 1) was this idea at the scale of three examples; this lesson scales it to the codebase — deliberately curating *our own code* as the teaching material.

---

## The Codebase as Curriculum

The insight that changes daily practice: **the best specification of "how we do things here" already exists — it's the code we're proudest of.** Rather than describing our service conventions in prose (lossy, laborious), we show a finished exemplar and ask for conformance:

```text
Here is our reference implementation of a service + controller pair:
#file:PaymentService.java  #file:PaymentController.java
Note the patterns: validation placement, our ApiError usage, the
transaction boundaries, how tests name and group scenarios
(#file:PaymentServiceTest.java).

Now implement RefundService + RefundController for the attached spec,
following those patterns exactly. Where the spec forces a deviation
from the pattern, flag it rather than improvising.
```

*Exemplar-driven generation: three files teach validation style, error shape, layering, and test conventions in one shot — no prose spec could match the fidelity.*

The output lands startlingly close to house style — naming, structure, even comment tone — because imitation is the model's native operation. The craft is all in curation:

- **Choose the best exemplar, not the nearest.** The model imitates *everything*, including the exemplar's flaws — teaching from the crufty legacy service propagates the cruft. Teams get real value from designating one or two **golden files** per pattern (the blessed service, the blessed test class) precisely for this use.
- **Point at the pattern, don't just attach the file.** "Note the transaction boundaries and error shape" focuses imitation on what's intentional versus incidental — otherwise the model may faithfully copy an accident (that one file's odd import order) as if it were law.
- **One exemplar plus stated variations beats many exemplars.** Three whole services flood the context (next unit covers that budget); one golden file plus "unlike payments, refunds have no async path — omit that machinery" teaches the delta cheaply.

---

## Where Exemplar Teaching Beats Instructions

The technique dominates wherever style and structure outweigh algorithmic novelty: new components in an established architecture (the case above — most enterprise work), migrating stragglers to a new pattern ("here's a converted class, here's an unconverted one; convert it the same way" — pattern-by-diff), test conventions (show one well-built test class; every generated test after inherits the style), and configuration/pipeline files (a working YAML teaches more than a schema description). Notice the last lesson's translation work was this too — "match this file's idiom" is exemplar teaching with the target codebase as teacher.

The limits keep us honest. In-context learning is **session-scoped** — nothing persists; tomorrow's conversation starts ignorant, which is why durable patterns graduate into *rulesets* (persistent instructions) with golden files referenced from them: rules state the law, exemplars show the case law, and the pair outperforms either alone. It's **imitation, not understanding** — the model copies the transaction annotation's placement without knowing why it's on the service; when the new task differs structurally from the exemplar, imitation quietly produces cargo-cult code, which is exactly what the "flag deviations" instruction exists to surface. And it's **bounded by context** — teaching material competes with working material for window space, the economics of the Context Management unit ahead.

One pattern taught, one component generated is the retail version. The wholesale version — applying a taught pattern *across* a codebase, dozens of files at a stroke — is the next lesson.
