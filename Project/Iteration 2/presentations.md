# ERS Iteration 2 — Presentation Guidance

## Format

- Use slides (PowerPoint, Prezi, Google Slides, whatever). Don't wing it.
- Have a simple script. Print it or put it on a second monitor.
- Split the speaking across the team. Give everyone a section they own — you all built this, so you should all get to talk about it.

## What to talk about

You're presenting the experience of building this iteration. There's one headline, one thing that's new this time, and whatever else you got to.

**The migration.** You had a working Javalin app and now it's Spring Boot. If you wrote a migration summary, this is a good place to pull from it.

- What mapped over cleanly
- What didn't, and what you had to rethink
- What Spring gave you for free that you'd hand-rolled before
- What Spring took away, hid, or made harder to reason about
- JDBC to Spring Data — what got simpler, what got stranger
- Validation, logging, and tests were optional last time and aren't now. What did that change?

**Working as a group.** This part is new this iteration, and it's fair game for the presentation.

- How you split the work
- How you handled the pieces nobody could start until someone else finished
- Branch strategy, merge conflicts, the PR that ate an afternoon
- What you'd do differently next time

**Auth, if you got to it.** Spring Security was quarantined, so anyone who took this on built token handling by hand. That's worth hearing about if it's your team.

- How you generate and validate a token
- Where in the request lifecycle you check it
- How you got role enforcement working without the framework doing it for you
- What you'd do differently with the real thing available

## The demo

- Bruno, Postman, or your carried-forward UI. Any of them is fine.
- Run one workflow end to end: employee submits, manager sees it, manager resolves it, employee sees the result.
- Send a bad request and show the validation error your API returns.
- If your logging is doing something worth seeing, put the console on screen while you run the demo.
- If you implemented auth, show it: log in, get a token, use it on a protected endpoint, then hit that endpoint without it (401) and hit a manager endpoint as an employee (403).
- Have the app running and your data seeded ahead of time so you can go straight into it.
- Have one person drive the screen for the whole demo, even as the narration passes around. It keeps the transitions clean.
- Rehearse it once as a team, start to finish, with the exact requests you plan to send. That's usually where you find the surprises.
- If something breaks mid-demo, tell us what it was supposed to do and keep going. That happens to everyone and it costs you nothing.

## Code

- Skip the line-by-line walkthrough — this is a demonstration.
- A small snippet on screen is welcome when it's illustrating something:
  - This was the hard part
  - This is where the migration got weird
  - We couldn't get this working, and here's what blocked us
  - We tried this three ways before this one stuck
  - This is where we found out we'd each assumed something different
- A few lines on a slide works better than a file open in your IDE.

## Stretch goals

- If you did any, show them. One slide and a quick demo is plenty.
- If you didn't get to them, that's fine — the core requirements are the assignment.
- If something is partly working, say so up front and show the part that works. That's a perfectly good thing to present.

## Housekeeping

- It's just us in the audience.
- Every team built basically the same application. Bring coffee, try not to yawn too loudly.
- This doesn't need to be perfect, bug free, or pretty.
- We'll take a break partway through and go to lunch after.
- Don't overthink the prep. A deck, a script, and one rehearsal will carry you.
