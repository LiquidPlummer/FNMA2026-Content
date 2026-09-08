# Lab 10 — Navigation & Routing

## Objective

So far every app we've built has been a single screen. Real applications have pages, and the URL says
which one you're looking at. Angular's **router** maps URLs to components.

We'll cover:

- The **routes array** in `app.routes.ts`, and `provideRouter()` in the app config
- **`<router-outlet />`** — the placeholder where the matched component gets rendered
- **`routerLink`** — navigating without reloading the page
- A route with a **`:id` parameter**, read with **`ActivatedRoute`**
- A **wildcard route** for URLs that match nothing else

### Where we're starting from

This project ships with three components and no routing at all:

- `home` — a static welcome message
- `course-list` — fetches `public/courses.json` with `HttpClient` and lists the courses
- `course-detail` — a placeholder; nothing renders it and there is no way to reach it

`App` stacks the first two on one page. `provideHttpClient()` is already wired up from Lab 9, and
`provideRouter(routes)` is already in `app.config.ts` — the CLI puts it there for every new app — but
`app.routes.ts` holds an empty array, so it currently does nothing.

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

Then open <http://localhost:4200>. Watch the address bar as you work — it's the thing we're actually
changing.

## Guided walkthrough

### Step 1 — Look at the two files that already exist

Open `src/app/app.config.ts`:

```ts
    provideRouter(routes)
```

That's the router being switched on for the whole app, exactly like `provideHttpClient()` next to it.
It's already there because `ng new` adds it by default.

Now open `src/app/app.routes.ts`:

```ts
export const routes: Routes = [];
```

An empty array. This is the file we'll spend most of the lab in — it's the map from URLs to
components, and it's the first place to look when navigation misbehaves.

One more thing to notice, in `src/index.html`:

```html
<base href="/">
```

That tag tells the router what the application's root URL is. Delete it and routing breaks in
confusing ways, so leave it alone — just know it's there and what it's for.

### Step 2 — Define the first two routes

In `src/app/app.routes.ts`, import the components and describe two routes:

```ts
import { Routes } from '@angular/router';
import { CourseList } from './course-list/course-list';
import { Home } from './home/home';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'courses', component: CourseList },
];
```

Each entry is an object saying "when the URL path is *this*, render *that* component."

Note that `path` has **no leading slash**. `path: '/courses'` is wrong and Angular will tell you so.
The empty string `''` is the root URL — the home page.

Save. Nothing changes on screen yet, because nothing is rendering the matched component.

### Step 3 — Add the router outlet

`<router-outlet />` is the slot where the router puts whichever component matched. Rewrite
`src/app/app.ts` to import it and drop the two components we're no longer placing by hand:

```ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
```

And `src/app/app.html`:

```html
<h1>Riverbend Course Catalog</h1>

<router-outlet />
```

Save. The home page's welcome message appears on its own. Now type <http://localhost:4200/courses> in
the address bar — the catalog replaces it.

That's the whole idea. `App` is now a shell: a heading that's always there, plus an outlet whose
contents depend on the URL. Anything you want on every page goes in `app.html` around the outlet.

### Step 4 — Navigate with `routerLink`

Typing URLs by hand gets old. Add a nav bar.

Import `RouterLink` in `src/app/app.ts`:

```ts
import { RouterLink, RouterOutlet } from '@angular/router';
```

```ts
  imports: [RouterLink, RouterOutlet],
```

And add the nav to `src/app/app.html`, above the outlet:

```html
<nav>
  <a routerLink="/">Home</a>
  <a routerLink="/courses">Catalog</a>
</nav>

<router-outlet />
```

Save and click the links. The content swaps, the URL changes, and — this is the part that matters —
**the page never reloads**. Watch the browser's reload spinner: it doesn't spin. The router swaps
components in place, so component state survives and there's no round trip to a server.

Use `routerLink`, not `href`. An `href="/courses"` would work in the sense that you'd land on the right
page, but it would throw the whole application away and boot it again from scratch.

### Step 5 — A route with a parameter

We want a page per course, and we don't want to write six routes. A **route parameter** handles that:

```ts
  { path: 'courses/:id', component: CourseDetail },
```

Add that to `app.routes.ts`, below the `courses` route, and import `CourseDetail` at the top.

The `:id` part is a wildcard for a single path segment. `/courses/101`, `/courses/104`, and
`/courses/anything` all match it, and the router remembers what was in that position under the name
`id`.

Order matters here, though not for these two: the router matches **top to bottom and stops at the first
match**. Keep more specific paths above more general ones.

Now link to it from the catalog. In `src/app/course-list/course-list.ts`, add `RouterLink`:

```ts
import { RouterLink } from '@angular/router';
```

```ts
  imports: [AsyncPipe, CurrencyPipe, RouterLink],
```

And wrap each course title in a link, in `src/app/course-list/course-list.html`:

```html
      <h3><a [routerLink]="['/courses', course.id]">{{ course.title }}</a></h3>
```

Note the **square brackets** and the array. In Step 4 we wrote `routerLink="/courses"` — a fixed
string, no brackets. Here we need to build a URL out of a value, so it's a property binding, and the
array form lets the router join the segments for us. `['/courses', 101]` becomes `/courses/101`.

You *could* write `[routerLink]="'/courses/' + course.id"` instead, but the array form handles URL
encoding and is what you'll see in real code.

Save and click a course title. The URL becomes `/courses/101` and the placeholder detail component
renders. It doesn't know which course yet — that's next.

### Step 6 — Read the parameter with `ActivatedRoute`

`ActivatedRoute` is an injectable service describing the route that's currently active, including its
parameters.

Rewrite `src/app/course-detail/course-detail.ts`:

```ts
import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { Course } from '../course';

@Component({
  selector: 'app-course-detail',
  imports: [AsyncPipe, CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './course-detail.html',
  styleUrl: './course-detail.css',
})
export class CourseDetail {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  courseId = Number(this.route.snapshot.paramMap.get('id'));

  course$ = this.http
    .get<Course[]>('/courses.json')
    .pipe(map((courses) => courses.find((course) => course.id === this.courseId)));
}
```

Three things worth slowing down for:

- **`this.route.snapshot.paramMap.get('id')`** — `snapshot` is the route's state right now, and
  `paramMap.get('id')` pulls out the `:id` segment. The name in `get('id')` has to match the name in
  the route's `path`.
- **`Number(...)`** — URL parameters are **always strings**. `'101' === 101` is false, so without the
  conversion the `find` below would never match anything. This is the classic first bug with route
  params.
- **`map(...)`** — a new RxJS operator, and a simple one: it transforms each value the observable
  emits. Here the request emits an array of courses and `map` turns it into the single course we want.
  `catchError` from Lab 9 was an operator too; they all go inside `.pipe()`.

Now the template, `src/app/course-detail/course-detail.html`:

```html
<h2>Course detail</h2>

@if (course$ | async; as course) {
  <h3>{{ course.title }}</h3>
  <p>Taught by {{ course.instructor }}</p>
  <p>Starts {{ course.startDate | date: 'fullDate' }}</p>
  <p>Tuition: {{ course.tuition | currency }}</p>
  <p>{{ course.seatsLeft }} seats left</p>
} @else {
  <p>No course found with id {{ courseId }}.</p>
}

<a routerLink="/courses">Back to the catalog</a>
```

Save and click through from the catalog. Each course gets its own page at its own URL.

Try `/courses/999` in the address bar. The `find` returns `undefined`, the `@if` fails, and the `@else`
tells you so. (It also flashes that message for an instant before the data arrives, since the `async`
pipe emits `null` first — a rough edge worth noticing, and something you'd smooth over in a real app.)

Then try reloading the browser on `/courses/104`. It works — the URL alone is enough to reconstruct the
page, which is exactly what you want from routing.

### Step 7 — A wildcard route for everything else

Type <http://localhost:4200/nonsense> and look at the console. The router matched nothing and logged
an error, leaving the outlet empty.

Generate a component for that case:

```bash
ng generate component not-found
```

Give it something useful, in `src/app/not-found/not-found.ts`:

```ts
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  templateUrl: './not-found.html',
  styleUrl: './not-found.css',
})
export class NotFound {}
```

And `src/app/not-found/not-found.html`:

```html
<h2>Page not found</h2>

<p>We could not find that page.</p>

<a routerLink="/">Back to the home page</a>
```

Then add the wildcard route to `app.routes.ts`, importing `NotFound` at the top:

```ts
export const routes: Routes = [
  { path: '', component: Home },
  { path: 'courses', component: CourseList },
  { path: 'courses/:id', component: CourseDetail },
  { path: '**', component: NotFound },
];
```

`'**'` matches any URL of any depth. Because the router stops at the first match, this entry **must be
last** — put it first and every single URL renders the 404 page, including the home page.

Save and try `/nonsense` again. Add a broken link to the nav so it's easy to test:

```html
  <a routerLink="/nowhere">Broken link</a>
```

## Exercises

### Exercise 1 — An instructor section

Add a second area of the site with its own routes:

1. Create `public/instructors.json` with four instructor objects — `id`, `name`, `email`, `specialty`.
2. Generate `instructor-list` and `instructor-detail` components.
3. Route them at `/instructors` and `/instructors/:id`.
4. Add a **Faculty** link to the nav bar, and link each name in the list to its detail page.
5. On the instructor detail page, add a link back to the list.

Everything you need is in Steps 2 through 6. Do it without re-reading them if you can.

### Exercise 2 — The snapshot trap

On the course detail page, add **Previous** and **Next** links that navigate to `courseId - 1` and
`courseId + 1`.

Click Next and watch what happens. The URL changes, but the page content doesn't.

The reason is in Step 6: `snapshot` is read **once**, when the component is created — and navigating
from one detail page to another reuses the same component instance rather than making a new one, so
the code never runs again.

Fix it by reading the parameter as an observable instead. `this.route.paramMap` is an observable that
emits every time the parameter changes; combine it with `map` and the `switchMap` operator so the
request re-runs on each change. You'll need to look `switchMap` up.

Once it works, write yourself a one-line comment on when `snapshot` is safe to use and when it isn't.

### Exercise 3 — Route matching order

This one is about reading errors, not writing features.

1. Move the `'**'` route to the **top** of the routes array. Reload and note what every URL now shows.
2. Put it back at the bottom, then add `{ path: 'courses/new', component: Home }` **below** the
   `courses/:id` route. Navigate to `/courses/new` and note which component renders and why.
3. Move it above `courses/:id` and try again.

Write a short comment in `app.routes.ts` stating the rule you just demonstrated.

## Check your work

### After the guided walkthrough

With `ng serve` running:

- <http://localhost:4200/> shows the **Welcome** message and the nav bar, and the address bar shows
  just `/`.
- Clicking **Catalog** shows six courses and changes the URL to `/courses` **without the browser
  reloading**.
- Clicking a course title navigates to `/courses/101` (or whichever id) and shows that course's title,
  instructor, full start date, tuition, and seat count.
- **Back to the catalog** returns to the list.
- Reloading the browser directly on `/courses/104` shows Database Design, not an error.
- `/courses/999` shows **No course found with id 999.**
- `/nonsense` and the **Broken link** both show the **Page not found** component, and the console shows
  no router error.
- `/` still shows the home page — if it shows the 404 instead, the wildcard route isn't last.

The `<h1>` heading and nav bar should be visible on **every** one of those pages, since they live in
`app.html` outside the outlet.

### After the exercises

- The faculty section works end to end: nav link, list, per-instructor detail pages with working URLs,
  and a link back.
- Previous/Next on the course detail page changes both the URL **and** the content. If only the URL
  changes, you're still on `snapshot`.
- With the wildcard first, every URL showed the 404 page. With `courses/new` below `courses/:id`, the
  detail component rendered and tried to look up a course with the id `NaN`; moving it above fixed it.
