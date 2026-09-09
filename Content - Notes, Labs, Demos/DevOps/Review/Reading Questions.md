# DevOps — Reading Questions

Use these questions while working through the unit's notes. Each one points at something worth understanding well enough to talk about on the job — not a definition to memorize, but a decision or tradeoff we should be able to explain to someone else.

---

## DevOps Fundamentals

1. Development and operations were traditionally measured on different things. What were those two measures, and why did having both produce the handoff wall rather than a healthy balance?

2. "You build it, you run it" puts the team that wrote the code on call for it. What does that actually change about how the software gets designed — and why isn't good automation enough on its own?

3. Two teams introduce the same bug on the same Monday morning. One finds it in four minutes, the other in three months. Beyond the obvious, what specifically makes the second team's version of that bug so much more expensive?

4. An artifact should be built once and promoted unchanged through every environment. What exactly do we lose if we rebuild it for each environment instead — and where should environment-specific differences live?

---

## Continuous Integration

5. What is integration debt, and why do merge conflicts grow worse than linearly with the time developers spend apart?

6. A long-lived feature branch can have a completely green build. Why doesn't that tell us what we actually want to know?

7. Why is a broken mainline build treated as the whole team's top priority rather than just the responsibility of whoever broke it? What is the cost of leaving it red while work continues?

8. A test that fails randomly one run in ten is often described as worse than having no test at all. Why would that be true?

---

## Continuous Delivery and Deployment

9. Continuous Delivery and Continuous Deployment share a pipeline and differ by exactly one thing. What is it, and what should drive the choice between them?

10. Continuous Deployment removes the human approval step entirely. Why is that not a reduction in safety — and what has to be genuinely true about a team before it becomes reasonable?

11. Across the prerequisites for Continuous Deployment there's a consistent underlying mindset about failure. What is it, and how does it differ from the traditional approach of making deployments rare and heavily scrutinized?

12. What's the difference between code being *deployed* and being *released*? What does separating those two things let a team do that it otherwise couldn't?

13. Pipeline stages are ordered so the fastest, cheapest checks run first. What's the reasoning, and what would we give up by running the thorough tests earlier?

14. The manual approval gate is explicitly not there to judge quality. What questions is it actually there to answer — and what has gone wrong if the person approving is trying to decide whether the code works?

15. Under Continuous Delivery, releasing is described as becoming a business decision rather than a technical event. What changes about how an organization operates once that's true?

---

## DevOps and Agile

16. A team completes every sprint on schedule, demos working software every two weeks, and closes stories reliably — but releases quarterly. What is this costing them, and why does it undercut the point of working in sprints at all?

17. A single team can usually adopt Agile on its own but often cannot adopt DevOps on its own. Why is that, and what does it predict about how adoption tends to go?

18. What changes about a team's behavior when the Definition of Done is extended to include "running in production" and "monitoring in place"? What becomes visible that was invisible before?

---

## Code Quality and Static Analysis

19. Automated tests and static analysis both catch problems, but neither substitutes for the other. What can each one find that the other structurally cannot?

20. A ten-year-old codebase has thousands of existing issues. Why would a quality gate demanding zero total issues fail as a practice, and why does applying the gate to new code only succeed where that approach doesn't?

21. Putting a number on technical debt changes the conversation with the business. What does it change, and what are the honest limits of that number?

---

## Automating the Pipeline

22. Build work is deliberately kept off the machine that runs the automation server, and clean, disposable build environments are preferred over long-lived ones. What's the reasoning behind each?
