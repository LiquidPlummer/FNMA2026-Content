# Lab 1 — Components

## Objective

Angular apps are trees of **components**. In this lab we'll build a small page out of three of them:
the root `App` shell plus two children that we nest inside it. Along the way we'll cover
`ng generate component`, the metadata on a standalone component's `@Component` decorator
(`selector`, `templateUrl`, `styleUrl`, `imports`), how nesting works through a component's
**selector**, and how to render a class property in a template with **interpolation** — `{{ }}`.

We're deliberately not doing anything interactive yet. Everything on the page will be static text
driven by properties on our component classes. Clicks, typing, and data flow all come later.

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

Then open <http://localhost:4200>. Leave `ng serve` running while you work — it rebuilds and
refreshes the browser every time you save a file.

## Guided walkthrough

### Step 1 — Get oriented

Before we write anything, let's see what we've been handed. Open these four files and skim them:

- `src/main.ts` — the entry point. It calls `bootstrapApplication(App, appConfig)`, which tells
  Angular "start the app, and the component at the top of the tree is `App`."
- `src/index.html` — the only real HTML page in the whole app. Notice `<app-root></app-root>` in the
  body. That tag is a **selector**, and Angular replaces it with our `App` component.
- `src/app/app.ts` — the `App` component class. Right now it's empty.
- `src/app/app.html` — `App`'s template, currently a heading and a comment.

The link between `index.html` and `app.ts` is worth pausing on, because it's the same link we'll use
for every component we build. In `app.ts`, the decorator says:

```ts
selector: 'app-root',
```

That is what makes `<app-root>` in `index.html` render our component. Every component we generate
gets its own selector, and that selector is how we place it on the page.

Start `ng serve` and confirm you see **Riverbend Community College** in the browser before moving on.

### Step 2 — Render a class property with interpolation

A component is a TypeScript class plus a template. Anything that's a property on the class can be
displayed in the template using double curly braces.

In `src/app/app.ts`, add a property to the `App` class:

```ts
export class App {
  collegeName = 'Riverbend Community College';
}
```

Now in `src/app/app.html`, replace the hardcoded heading text with the property:

```html
<h1>{{ collegeName }}</h1>
```

Save. The page looks identical — but the text is now coming from the class, not from the template.
That's the whole idea of interpolation: the template is a *view* of the class's data.

Try changing `collegeName` in `app.ts` and watch the browser update on save.

### Step 3 — Generate our first child component

We'll build a header section as its own component. Run:

```bash
ng generate component course-header
```

You can shorten this to `ng g c course-header`.

The CLI creates a folder with three files in it:

```
src/app/course-header/
  course-header.ts     the class and its @Component decorator
  course-header.html   the template
  course-header.css    styles scoped to just this component
```

Open `src/app/course-header/course-header.ts`. The CLI filled in the decorator for us:

```ts
@Component({
  selector: 'app-course-header',
  imports: [],
  templateUrl: './course-header.html',
  styleUrl: './course-header.css',
})
export class CourseHeader {}
```

Four things to notice, because they're on every component you'll ever write:

- **`selector`** — the tag name we'll use to place this component. The CLI prefixed it with `app-`,
  which is the prefix configured in `angular.json`.
- **`templateUrl`** — points at the separate `.html` file. (A template can also be written inline
  with `template:`, but we'll always keep ours in their own file.)
- **`styleUrl`** — points at the `.css` file. Styles in there apply *only* to this component.
- **`imports`** — the list of other components, directives, and pipes this component's template is
  allowed to use. It's empty for now. We'll come back to it in Step 5.

### Step 4 — Give the header some content

Let's put two properties on the class. In `src/app/course-header/course-header.ts`:

```ts
export class CourseHeader {
  department = 'Department of Computer Science';
  term = 'Spring 2026';
}
```

Then replace everything in `src/app/course-header/course-header.html` with:

```html
<h2>{{ department }}</h2>
<p>Course listings for {{ term }}</p>
```

Save. Nothing shows up in the browser yet — and that's expected. We've *defined* a component, but we
haven't *placed* it anywhere. That's the next step.

### Step 5 — Nest the header inside the shell

Placing a child component takes two edits, and forgetting either one is the single most common
mistake at this stage.

First, `App` has to know the component exists. In `src/app/app.ts`, import the class and list it in
the `imports` array:

```ts
import { Component } from '@angular/core';
import { CourseHeader } from './course-header/course-header';

@Component({
  selector: 'app-root',
  imports: [CourseHeader],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  collegeName = 'Riverbend Community College';
}
```

Second, use the selector in `App`'s template. In `src/app/app.html`, replace the comment with the
tag:

```html
<h1>{{ collegeName }}</h1>

<app-course-header />
```

Save, and the header content appears under the college name. `<app-course-header />` is a
self-closing tag; `<app-course-header></app-course-header>` means exactly the same thing, and you'll
see both styles in the wild.

If you get a template error instead, it's almost certainly the `imports` array — Angular is telling
you it doesn't recognize the tag because the component wasn't imported.

### Step 6 — Add a second child component

Same pattern, one more time, so it sticks. Generate it:

```bash
ng generate component course-summary
```

Give the class three properties in `src/app/course-summary/course-summary.ts`:

```ts
export class CourseSummary {
  courseCount = 14;
  seatsPerCourse = 25;
  advisor = 'Dr. Elena Marsh';
}
```

And in `src/app/course-summary/course-summary.html`:

```html
<h3>At a glance</h3>
<p>{{ courseCount }} courses offered this term.</p>
<p>Total seats available: {{ courseCount * seatsPerCourse }}</p>
<p>Questions? Contact {{ advisor }}.</p>
```

Notice the third line: `{{ }}` isn't limited to bare property names. It evaluates an *expression*,
so arithmetic works, and so does joining strings together.

Now nest it. In `src/app/app.ts`, add the import and extend the `imports` array:

```ts
import { CourseSummary } from './course-summary/course-summary';
```

```ts
imports: [CourseHeader, CourseSummary],
```

And in `src/app/app.html`, add the tag below the header:

```html
<app-course-summary />
```

Save and check the browser. All three components are now on screen: the shell rendering the college
name, the header, and the summary. The component tree looks like this:

```
App
├── CourseHeader
└── CourseSummary
```

## Exercises

These are unguided. Use the same steps you just walked through, but work out the details yourself.

### Exercise 1 — An instructor spotlight

Build a *new* section of the page for a different purpose: a spotlight on one instructor.

1. Generate a component called `instructor-spotlight`.
2. Give its class at least four properties: the instructor's name, their office number, the number of
   years they've taught, and the year they were hired.
3. Write its template so it renders all four, and include at least one `{{ }}` expression that does a
   small calculation rather than just printing a property — for example, working out the current year
   from the hire year and the years taught.
4. Nest it in the `App` shell so it appears below the summary.

### Exercise 2 — Nest a component inside a component

So far every child has been nested directly in `App`, but nesting isn't limited to the root — any
component can host another.

Move `<app-course-summary />` out of `App`'s template and place it inside `CourseHeader`'s template
instead, so the tree becomes:

```
App
├── CourseHeader
│   └── CourseSummary
└── InstructorSpotlight
```

You'll have to move more than the tag to make this work. Think about which component now needs
`CourseSummary` in its `imports` array, and which one no longer does.

### Exercise 3 — Change a selector

Selectors are just names we choose. In `course-summary.ts`, change the selector from
`app-course-summary` to `app-term-overview`, then fix whatever breaks.

This one is short on purpose. The point is to see for yourself that the tag in the template and the
`selector` in the decorator are two halves of the same connection — change one and the other has to
follow.

## Check your work

### After the guided walkthrough

With `ng serve` running, <http://localhost:4200> should show, top to bottom:

- An `<h1>` reading **Riverbend Community College**
- An `<h2>` reading **Department of Computer Science**
- **Course listings for Spring 2026**
- An `<h3>` reading **At a glance**
- **14 courses offered this term.**
- **Total seats available: 350** — if you see `14 * 25` as literal text instead of `350`, your
  expression isn't inside `{{ }}`
- **Questions? Contact Dr. Elena Marsh.**

The terminal running `ng serve` should show a successful build with no errors.

Open your browser's developer tools and inspect the page. You'll see the `<app-course-header>` and
`<app-course-summary>` elements still present in the DOM, wrapping the content they rendered. That's
a good way to confirm a component is actually on the page.

### After the exercises

- The instructor spotlight section renders below the summary, with a calculated value that is clearly
  a number rather than the raw expression text.
- After Exercise 2, the "At a glance" block appears nested inside the header's output rather than as
  a sibling of it, and the app still compiles with no `imports` errors.
- After Exercise 3, the page looks exactly the same as it did before you renamed the selector. If the
  summary disappeared or the build failed, one half of the tag/selector pair is still out of date.
