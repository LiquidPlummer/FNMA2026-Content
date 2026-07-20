# Few-Shot Prompting

A **few-shot prompt** includes worked examples — one or more input→output pairs demonstrating exactly what we want — before the actual ask. LLMs are pattern engines: show two or three instances of a transformation and the model extracts the pattern and applies it, often more faithfully than any prose description could achieve. When *show* beats *tell*, few-shot is the tool.

---

## The Mechanics

```text
Convert these legacy log calls to our structured logger.

Example 1:
  Before: log.info("User " + userId + " logged in from " + ip);
  After:  log.atInfo().addKeyValue("userId", userId)
             .addKeyValue("ip", ip).log("user login");

Example 2:
  Before: log.error("Payment failed: " + e.getMessage(), e);
  After:  log.atError().addKeyValue("reason", e.getMessage())
             .setCause(e).log("payment failed");

Now convert:
  log.warn("Retry " + attempt + " for order " + orderId + " after " + delayMs + "ms");
```

*Two demonstrations, then the real input — the model infers the full convention: fluent style, key-value extraction, lowercase terse messages, cause handling.*

Notice what the examples carried that instructions would have struggled with: the *naming* convention for keys, the *tone* of the messages, where line breaks go. Describing all that takes a paragraph per rule and still gets misread; two examples pin it down. That's the selection criterion: **few-shot earns its cost when the target has structure or style that's easier to demonstrate than specify** — output formats, house conventions, transformation patterns, edge-case handling ("look how example 2 treats the null").

---

## Choosing Examples Well

The examples *are* the specification, so they repay care:

- **Make them diverse.** Each example should teach something the others don't — a second example identical in shape to the first adds nothing; one showing the tricky variant (an exception, an empty input, a multi-line case) adds a rule. Two or three well-chosen examples is the practical sweet spot; more mostly costs context space.
- **Make them real.** Examples from *our actual codebase* teach our actual conventions — and this is precisely how we teach the AI house style without any training or fine-tuning (a theme that grows into its own unit later: In-Context Learning).
- **Keep them consistent.** The model imitates *everything* visible — formatting quirks, comment style, even mistakes. A sloppy example produces sloppy output; an example that contradicts the instructions produces coin-flips.
- **Mind the count vs. context budget.** Every example consumes context window. When examples are large (whole classes), one excellent example plus explicit notes on the variations often beats three bulky ones.

---

## Where It Shows Up in Copilot Without Us Asking

Few-shot explains a behavior every Copilot user notices: inline completions in a file full of existing patterns come out *matching those patterns*. The surrounding code is functioning as implicit examples — the model continues what it sees. That's leverage: adding one well-crafted instance of a new pattern to a file (or keeping an exemplary file open in a tab) quietly few-shots every subsequent completion. It's also a warning: a file full of legacy anti-patterns few-shots those too — the AI amplifies whatever it's steeped in.

---

## Zero or Few? The Decision in One Breath

Start **zero-shot** — clear instructions, context attached. Escalate to **few-shot** when: the output format keeps drifting from what we want, the style is house-specific, the transformation has fiddly rules, or we catch ourselves writing a third paragraph of description that one example would replace. And combine freely — instructions *plus* examples is the strongest form: prose states the rules, examples ground them, and the model gets both the law and the case history.
