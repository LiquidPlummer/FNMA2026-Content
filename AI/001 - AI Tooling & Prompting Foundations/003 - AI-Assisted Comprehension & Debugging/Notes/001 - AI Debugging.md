# AI Debugging

Debugging is where AI assistance pays off fastest for most developers: models have seen millions of stack traces and their resolutions, so a well-fed AI often recognizes in seconds a failure pattern we'd spend an hour reconstructing. The technique is in the *feeding* — what to include, how to iterate, and where to stop trusting.

---

## Feed the Evidence, Not the Summary

The cardinal rule: **paste the actual artifacts, don't paraphrase them.** A summary ("it throws an NPE somewhere in the service layer") strips exactly the details the model diagnoses from — line numbers, exception types, the cause chain. The full package:

```text
This test fails and I don't see why. 

The error:
[paste the COMPLETE stack trace — including every "Caused by:" section]

The failing test:
#file:InvoiceServiceTest.java

The method under test:
#file:InvoiceService.java

What I've already ruled out: the repository mock is stubbed (line 42),
and the same flow works in QuickCheckoutTest.
```

*A complete debugging prompt: full trace, both sides of the failing code, and what's already been eliminated — the same brief we'd give a colleague.*

Details that disproportionately improve the diagnosis: the **full cause chain** (the bottom `Caused by:` is usually the real story — truncating it discards the answer), the *exact* versions when the issue smells environmental ("Spring Boot 3.3, Java 21"), and **what we've already tried** — which prevents the AI from suggesting the three things we've ruled out.

---

## The Iteration Loop

AI debugging is hypothesis-driven, and we run the loop like a scientist with a fast-talking lab partner:

1. **AI proposes** a diagnosis and fix.
2. **We evaluate before applying** — does the explanation actually account for the evidence? A fix without a mechanism ("try adding @Transactional") is a guess wearing a lab coat; ask *"explain why that would cause this exact trace"* before touching code.
3. **Apply and re-run** — the test suite is the referee, not the AI's confidence.
4. **Feed back the result** — a new error is *progress*; paste it: "that fixed the NPE, now it fails with LazyInitializationException at line 60." Each round narrows the search.

Two failure modes to guard. **Plausible-fix roulette**: accepting successive suggestions without understanding any — when the third guess "works," we've learned nothing and possibly masked the bug. If two rounds haven't converged, stop and ask the AI for *instrumentation* instead of fixes: "what logging would distinguish hypothesis A from B?" **Fixing the symptom**: the AI happily silences an exception (catch-and-ignore, a null-check bandage) if we let it; insist on root cause — *"don't suppress the exception; explain why total is null in the first place."*

---

## Where AI Debugging Shines and Where It Can't See

Strong territory: recognized exception patterns (framework misconfigurations, classic NPE/concurrency/encoding traps), cryptic error messages that are famous in aggregate but new to us, build and dependency failures, "this worked before the upgrade." Weak territory: bugs whose cause lives in what the AI *can't observe* — runtime state, production data shape, race timing, an environment variable — or in business logic it has no spec for. The tell is diagnosis-by-generic-checklist. The counter is giving it observations (debugger findings, log lines, variable dumps) rather than more code, or recognizing we've left AI territory and reaching for the debugger ourselves.

One more debugging superpower with its own dedicated use: *"explain what this code actually does, line by line"* — often the bug becomes obvious to *us* mid-explanation. That comprehension mode is the next lesson.
