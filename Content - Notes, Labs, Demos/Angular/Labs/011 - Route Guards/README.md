# Lab 11 — Route Guards

## Objective

Lab 10 gave every page a URL. That's convenient, and it's also a problem: anyone who types the URL
gets the page. A **route guard** is a function the router runs *before* activating a route, which
decides whether the navigation is allowed to happen.

We'll cover:

- A **functional `CanActivate` guard** — what it receives, and what it's allowed to return
- `inject()` inside a guard, including `inject(Router)` for redirecting
- Wiring the guard into the routes array with `canActivate`

A word on scope before we start. The `isLoggedIn` flag in this project is a boolean somebody flips
with a button. There is no password, no server, no token, and nothing stopping a determined user. That
is fine for learning what a guard *is*, but be clear-eyed about it: **a route guard is not security.**
It controls what the UI offers, and the real enforcement always lives on the server. Guards improve the
experience; they don't protect anything.

### Where we're starting from

This project ships with the finished state of Lab 10 — four routed components, a nav bar, a `:id`
route, and a wildcard 404 — plus two additions:

- `src/app/session-service.ts` — a service holding an `isLoggedIn` signal and `signIn()` / `signOut()`
  methods.
- A signed-in / signed-out indicator in `app.html` with a button to flip it.

Run it and click the button a few times. Nothing else responds to it yet. That's what we're about to
change.

## Prerequisites

| Software | Required Version |
|---|---|
| Node.js | ^20.19.0 \|\| ^22.12.0 \|\| ^24.0.0 |
| Angular CLI | 21.1.x |
| TypeScript | >=5.9.0 <6.0.0 |
| RxJS | ^6.5.3 \|\| ^7.4.0 |

Check what you have installed:

```bash
node --version
ng version
```

If you don't have the Angular CLI, install it globally:

```bash
npm install -g @angular/cli
```

### Getting the project running

From this lab's folder:

```bash
npm install
ng serve
```

Then open <http://localhost:4200> with the developer console visible.

## Guided walkthrough

### Step 1 — Confirm the hole

Click **Catalog**, then click any course. You get the detail page.

Now click **Sign out** if you aren't already signed out, and click a course again. You still get the
detail page — the flag makes no difference to anything.

Copy the URL, say `http://localhost:4200/courses/103`, open a new tab, and paste it. Straight in.

That direct-URL case is the one people forget. Hiding a link is not the same as protecting a route,
because the URL is still there and the router will happily honour it.

### Step 2 — Generate the guard

```bash
ng generate guard auth --implements CanActivate
```

Without the `--implements` flag the CLI stops and asks you which interfaces you want; passing it keeps
the command non-interactive.

That creates `src/app/auth-guard.ts`:

```ts
import { CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  return true;
};
```

Take a moment with what that is, because it's smaller than people expect. A guard is not a class and
not a component — it's an **exported function**, typed as `CanActivateFn`. Returning `true` allows the
navigation; returning `false` blocks it. Right now it always allows.

Its two parameters:

- **`route`** — details of the route being activated, including its parameters
- **`state`** — the router state being navigated to, whose `url` property is the full URL the user
  asked for

### Step 3 — Read the session inside the guard

The guard needs to know whether the user is signed in, which means it needs `SessionService`.

Guards run inside Angular's **injection context**, which means `inject()` works in the body of the
function just as it does in a component's field initialiser. That's the main reason functional guards
replaced the old class-based ones — no constructor, no boilerplate.

```ts
import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { SessionService } from './session-service';

export const authGuard: CanActivateFn = (route, state) => {
  const session = inject(SessionService);

  return session.isLoggedIn();
};
```

Because `providedIn: 'root'` means one shared instance, this is the *same* `SessionService` the nav bar
is using. The button and the guard are looking at the same signal.

Note that `isLoggedIn()` is called with parentheses — it's a signal, and reading it means calling it.

### Step 4 — Wire it into the route

A guard does nothing until a route names it. In `src/app/app.routes.ts`, import it and add a
`canActivate` array to the detail route:

```ts
import { authGuard } from './auth-guard';
```

```ts
  { path: 'courses/:id', component: CourseDetail, canActivate: [authGuard] },
```

`canActivate` takes an **array**, because a route can have several guards. They all have to pass.

Save, make sure you're signed out, and click a course.

Nothing happens. The URL doesn't change, the page doesn't change, and the click appears to do nothing
at all.

That's `false` doing its job: the navigation was cancelled. Now click **Sign in** and try again — the
detail page opens. The guard works.

But "nothing happens" is a terrible experience. The user clicked a link and got silence. Let's fix that.

### Step 5 — Redirect instead of blocking

A guard can return a third thing besides `true` and `false`: a **`UrlTree`**, which tells the router
"don't go there, go *here* instead."

```ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session-service';

export const authGuard: CanActivateFn = (route, state) => {
  const session = inject(SessionService);
  const router = inject(Router);

  if (session.isLoggedIn()) {
    return true;
  }

  console.warn(`Blocked navigation to ${state.url} — not signed in.`);
  return router.createUrlTree(['/']);
};
```

`inject(Router)` gets the router — the same service that powers `routerLink`, available anywhere
`inject()` works. `createUrlTree(['/'])` builds a destination from path segments, the same array form
we used for `[routerLink]="['/courses', course.id]"` in Lab 10.

Save, sign out, and click a course. You land on the home page, and the console explains why.

You'll also see this written a different way in older code:

```ts
  router.navigate(['/']);
  return false;
```

It usually works, but it starts a second navigation while the first is still being cancelled, and the
two can race. Returning a `UrlTree` hands the router a single decision to act on. Prefer it.

### Step 6 — Check every way in

A guard protects the *route*, not the link, and that's the whole point. Verify it properly:

1. Signed out, click a course from the catalog → redirected home.
2. Signed out, paste `http://localhost:4200/courses/103` into the address bar → redirected home.
3. Sign in, then repeat both → the detail page opens.
4. On a detail page while signed in, click **Sign out** → note that you **stay** on the page.

That last one surprises people, and it's correct. `CanActivate` runs when a route is *entered*.
Nothing re-runs it because some state changed later. Guards are gates, not alarms — this is exactly
the kind of gap that makes the point from the top of this lab worth repeating: real enforcement lives
on the server.

### Step 7 — Confirm the guard runs before the component

Add a log to the top of the guard:

```ts
  console.log('authGuard running for', state.url);
```

And one to `CourseDetail`'s class body in `src/app/course-detail/course-detail.ts`:

```ts
  constructor() {
    console.log('CourseDetail constructed');
  }
```

Sign out and click a course. You'll see the guard's log and **no** component log — the component was
never created. Sign in and try again: guard first, then component.

That ordering matters in practice. Because a blocked route never constructs its component, a guard is
also what keeps a page from running expensive setup — an HTTP request, a timer — that the user was
never going to be allowed to see.

Remove both logs when you've seen it.

## Exercises

### Exercise 1 — Guard a second route, with the opposite logic

Add a route at `/welcome` pointing at a component you generate, and guard it so it's reachable **only
when signed out** — a signed-in user visiting it should be redirected to `/courses`.

You'll need a second guard function. Write it in its own file. Notice how little there is to it once
you know the shape: inject, check, return `true` or a `UrlTree`.

### Exercise 2 — Two guards on one route

`canActivate` takes an array because routes can have more than one guard, and all of them must pass.

Add a `termOpenGuard` that checks a second flag on `SessionService` — `registrationOpen`, which you'll
add as a signal with its own toggle button in the nav. Put **both** guards on the `courses/:id` route.

Then work out, by testing, what happens when the first guard fails: does the second one still run? Add
a `console.log` to each and find out. Write down what you observe.

### Exercise 3 — Use what the guard knows

Right now the guard throws away where the user was trying to go. Improve it.

`state.url` holds the URL that was blocked. Store it on `SessionService` before redirecting. Then, on
the home page, show a message like "Sign in to view /courses/103" whenever that value is set, with a
button that signs the user in and sends them where they were originally headed.

Two things to think about while you build it:

- The `route` parameter also carries the matched parameters. Try logging
  `route.paramMap.get('id')` from inside the guard and confirm you can see which course was requested
  — without the component ever being created.
- Should the stored URL be cleared once it's used? Decide, and make your code do what you decided.

## Check your work

### After the guided walkthrough

With `ng serve` running and the console open:

**Signed out:**

- Clicking a course title from the catalog lands you on the **home page**, not the detail page, and the
  console logs `Blocked navigation to /courses/101 — not signed in.`
- Pasting `http://localhost:4200/courses/103` directly into the address bar does the same thing.
- The catalog and home pages themselves still work normally — only the detail route is guarded.
- `/nonsense` still shows the 404 page.

**Signed in:**

- Clicking a course opens its detail page at `/courses/101`, with the title, instructor, date, tuition,
  and seat count.
- Reloading the browser on that URL keeps you there.

**Then:**

- While on a detail page and signed in, clicking **Sign out** leaves you on the page. That's expected —
  `CanActivate` only runs on entry.
- With the Step 7 logs in place, a blocked navigation logs the guard and **never** logs
  `CourseDetail constructed`.

Also check the code: `auth-guard.ts` exports a `const` typed as `CanActivateFn`, not a class, and
`app.routes.ts` lists it as `canActivate: [authGuard]` on exactly one route.

### After the exercises

- `/welcome` is reachable only when signed out, and redirects to `/courses` when signed in.
- With two guards on the detail route, you can state from your own logs whether the second guard runs
  after the first one fails.
- The home page names the URL the user was blocked from, and signing in from there takes them straight
  to it.
