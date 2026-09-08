# Lab 9 — HttpClient

## Objective

Every lab so far has invented its own data in a TypeScript file. Real apps fetch it. In this lab we'll
replace a hardcoded array with an actual HTTP GET.

We'll cover:

- `provideHttpClient()` — registering Angular's HTTP support in the app config
- Injecting `HttpClient` and calling `get<T>()` with a type parameter
- The **`async` pipe**, which subscribes to an observable for you and unsubscribes when the component
  is destroyed
- Handling a failed request with a single `catchError`

There's no server to run. The "API" is a static JSON file in `public/`, which `ng serve` hands out over
HTTP just like any other file. As far as the browser is concerned it's a real network request — you'll
see it in the Network tab — which is all we need.

### Where we're starting from

This project ships with the course list rendering a hardcoded `CATALOG` array, exactly as in earlier
labs, plus `public/courses.json` sitting there unused.

Open both. You'll notice the JSON file has **six** courses while the hardcoded array has five. That's
deliberate: when the sixth course appears on screen, you'll know for certain the data is coming from
the file and not from the array.

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

Then open <http://localhost:4200>. Open the developer tools and switch to the **Network** tab — we'll
be watching it.

## Guided walkthrough

### Step 1 — Confirm the file is being served

Before writing any code, check that our mock API exists. With `ng serve` running, visit:

<http://localhost:4200/courses.json>

You should see the raw JSON. That's what makes `public/` special: everything in it is copied to the
root of the served site, so `public/courses.json` becomes `/courses.json`. It's configured in
`angular.json` under the build target's `assets` list, if you want to see where that comes from.

Note also that `courses.json` is a plain array of objects whose fields line up exactly with the
`Course` interface in `src/app/course.ts`. That correspondence is what will let us type the request.

### Step 2 — Provide `HttpClient`

`HttpClient` is a service, but unlike the one we wrote in Lab 5 it isn't `providedIn: 'root'` by
default. You have to opt in, once, for the whole app.

In `src/app/app.config.ts`, import the provider function and add it to the `providers` array:

```ts
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(),
    provideRouter(routes)
  ]
};
```

Note the import path: `@angular/common/http`, not `@angular/common` and not `@angular/core`. It's a
separate entry point, and getting it wrong is a common first stumble.

`app.config.ts` is where app-wide setup lives. `main.ts` passes this object to
`bootstrapApplication`, and everything in `providers` becomes available to the whole injector tree.
This is the same "root" idea from Lab 5, just written out explicitly.

Nothing changes on screen yet.

### Step 3 — Inject `HttpClient` and make the request

Now rewrite `src/app/course-list/course-list.ts`. Delete the entire `CATALOG` constant — all fifty-odd
lines of it — and replace the class with this:

```ts
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { Course } from '../course';

@Component({
  selector: 'app-course-list',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './course-list.html',
  styleUrl: './course-list.css',
})
export class CourseList {
  private http = inject(HttpClient);

  courses$ = this.http.get<Course[]>('/courses.json');
}
```

`inject(HttpClient)` is the same call from Lab 5 — `HttpClient` is just another injectable service,
and `provideHttpClient()` is what made it available.

Two things about the `get` call:

- **`get<Course[]>(...)`** — the type parameter tells TypeScript what shape to expect back. This is a
  *promise you're making*, not a check Angular performs: nothing validates the response at runtime. If
  the JSON doesn't match, TypeScript will still believe you. Type it accurately.
- **`courses$`** — the trailing `$` is a widely used convention meaning "this variable holds an
  observable." It's not syntax; it's a naming habit that saves a lot of confusion.

And the crucial part: `get()` returns an **observable**, not the data and not a promise. Nothing has
been requested yet. An observable is a description of work to be done, and it does nothing until
something subscribes.

Save. The page goes blank, because the template is still looping over `courses`, which no longer
exists — you'll have a build error saying so. That's next.

### Step 4 — Subscribe with the `async` pipe

The template needs to unwrap that observable. The `async` pipe does it.

Add `AsyncPipe` to the imports in `course-list.ts`:

```ts
import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
```

```ts
  imports: [AsyncPipe, CurrencyPipe, DatePipe],
```

Then, in `src/app/course-list/course-list.html`, wrap the loop:

```html
<h2>Spring 2026</h2>

@if (courses$ | async; as courses) {
  @for (course of courses; track course.id) {
    <article>
      <h3>{{ course.title }}</h3>
      <p>Taught by {{ course.instructor }}</p>
      <p>Starts {{ course.startDate | date: 'mediumDate' }}</p>
      <p>Tuition: {{ course.tuition | currency }}</p>
      <p>{{ course.seatsLeft }} seats left</p>
    </article>
  } @empty {
    <p>No courses are listed for this term.</p>
  }
}
```

Save. Six courses appear, ending with **Testing Fundamentals** — the one that was never in the
hardcoded array. Check the Network tab and you'll see the request for `courses.json`.

There's a lot packed into `@if (courses$ | async; as courses)`, so let's take it apart:

- `courses$ | async` — the `async` pipe **subscribes** to the observable. When a value arrives, it
  hands it to the template and tells Angular to re-render.
- Before the response arrives, the pipe evaluates to `null`. That's why we need the `@if`: without it,
  `@for` would be asked to loop over `null` on the first render.
- `; as courses` gives the unwrapped value a name we can use inside the block, so we don't write
  `courses$ | async` (and subscribe again) on every line.

The best part is what you don't see. Remember Lab 8's leak — anything started has to be stopped? The
`async` pipe **unsubscribes automatically** when the component is destroyed. That's precisely why we
prefer it to calling `.subscribe()` by hand: no `ngOnDestroy`, no bookkeeping, nothing to forget.

### Step 5 — See when the request actually happens

Comment out the `@if` block entirely and reload, watching the Network tab.

No request for `courses.json`.

Uncomment it and reload. The request appears.

That's the point from Step 3 made concrete: `this.http.get(...)` built an observable but didn't fetch
anything. The `async` pipe subscribing is what triggered the request. Observables are **lazy** — no
subscriber, no work.

This is also why a `get()` you never subscribe to is a silent no-op rather than an error, which is a
genuinely confusing bug the first time you hit it.

### Step 6 — Handle a failure

Right now, if the request fails, the page just sits there empty with an unhandled error in the
console. Let's catch it.

In `course-list.ts`:

```ts
import { catchError, of } from 'rxjs';
```

```ts
  courses$ = this.http.get<Course[]>('/courses.json').pipe(
    catchError((error) => {
      console.error('Could not load the catalog:', error.message);
      return of([]);
    }),
  );
```

`.pipe()` is how you attach operators to an observable. `catchError` intercepts an error and — this is
the part that trips people up — must **return another observable** to carry on with. It can't just log
and stop; something has to be emitted downstream or the `async` pipe never gets a value.

`of([])` creates an observable that immediately emits one value — here an empty array — and completes.
So on failure, the template receives `[]`, the `@for` finds nothing, and the `@empty` block renders our
"No courses" message. A failed request now degrades into an honest empty state instead of a blank page.

### Step 7 — Break it on purpose

Change the URL to something that doesn't exist:

```ts
  courses$ = this.http.get<Course[]>('/courses-typo.json').pipe(
```

Save and reload. You should see:

- **No courses are listed for this term.** on the page
- **Could not load the catalog:** followed by an error message in the console
- A **404** for `courses-typo.json` in the Network tab

Three different views of the same failure. Put the correct URL back before moving on.

## Exercises

### Exercise 1 — A second request from a service

Add a file `public/instructors.json` containing an array of instructor objects — an `id`, a `name`, an
`email`, and a `specialty`.

Then, rather than injecting `HttpClient` into a component again, put the request in a service:

1. Generate a `catalog-service`.
2. Inject `HttpClient` into it and give it a method that returns the instructors observable.
3. Generate an `instructor-list` component that injects the service and renders the result with the
   `async` pipe.
4. Place it in `App` below the course list.

This is the shape you'll see in most real Angular codebases: components render, services fetch. Note
that the service does **not** need `provideHttpClient()` again — that's app-wide and already done.

### Exercise 2 — Two async pipes, two requests

In your new instructor list, add a *second* `courses$ | async` alongside the instructors, so the
template unwraps two observables at once.

Then look at the Network tab and count the requests for `courses.json`. If you see more than you
expected, work out why — and fix it by making sure each observable is unwrapped exactly once, using the
`; as` syntax from Step 4.

Write a one-line comment recording what you found.

### Exercise 3 — A more useful failure

`of([])` is a blunt recovery. Make the failure visible to the user rather than only to the console.

Give the catalog service a method that fetches a **single** course by id — `/courses.json` won't help
here, so add `public/course-101.json` with one object in it. Render it in a new component.

Then handle its failure differently: instead of returning an empty array, return an observable
carrying a placeholder `Course` whose title reads "Course unavailable". Point the URL at a file that
doesn't exist and confirm the placeholder renders.

Think about the trade-off while you do it. Silently substituting fake data is often the wrong answer in
a real app — how would you make it obvious to a user that they're looking at a fallback rather than a
real course?

## Check your work

### After the guided walkthrough

With `ng serve` running, <http://localhost:4200> should show **six** course cards, ending with
**Testing Fundamentals** taught by Jordan Vega. If you see five and the last is *Version Control with
Git*, the hardcoded array is still in play.

Also confirm:

- `course-list.ts` contains no `CATALOG` constant and no course data of any kind.
- The Network tab shows exactly **one** request for `courses.json`, with status 200.
- Tuition renders as **$895.00** and dates as **Jan 20, 2026** — the pipes from Lab 7 work on fetched
  data exactly as they did on hardcoded data.
- There is no `.subscribe(` anywhere in your code, and no `ngOnDestroy`.

Then re-run the Step 7 check: point the URL at a nonexistent file, confirm you get the empty-state
message plus a console error and a 404, and put it back.

### After the exercises

- The instructor list renders from `instructors.json`, fetched through a service rather than directly
  in the component.
- The component that unwraps two observables makes one request per file, not several. If you saw
  duplicates before fixing it, you've learned the most important gotcha about the `async` pipe: each
  `| async` is its own subscription, and each subscription to an `HttpClient` observable is its own
  request.
- The single-course component renders a "Course unavailable" placeholder when its URL is broken, and
  the real course when it isn't.
