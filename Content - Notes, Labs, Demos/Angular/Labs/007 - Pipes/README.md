# Lab 7 — Pipes

## Objective

The list we built in Lab 6 works, but it looks like a database dump: `2026-01-20T09:00:00` instead of
a readable date, `895` instead of a price, `intro to angular` instead of a title.

**Pipes** fix that. A pipe transforms a value on its way into the template, using the `|` character:
`{{ value | pipeName }}`. The underlying data never changes — only what the user sees.

We'll cover:

- The built-in pipes `titlecase`, `uppercase`, `date`, `currency`, and `json`
- Passing arguments to a pipe with `:`
- **Chaining** several pipes together
- Writing our own pipe with `@Pipe` and `PipeTransform`

### Where we're starting from

This project ships with the course list from Lab 6, plus two new fields on each course — `startDate`
and `tuition` — and every string stored in lowercase. The template renders all of it raw.

Run it first and look at how bad it is. That's the problem we're solving.

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

Then open <http://localhost:4200>.

## Guided walkthrough

### Step 1 — Import a pipe before you use it

Built-in pipes live in `@angular/common`, and like components, a standalone component has to list the
ones its template uses in `imports`.

Start with the two casing pipes. In `src/app/course-list/course-list.ts`, add an import line at the
top and put both pipes in the `imports` array:

```ts
import { TitleCasePipe, UpperCasePipe } from '@angular/common';
```

```ts
  imports: [TitleCasePipe, UpperCasePipe],
```

Note the naming convention: the class is `TitleCasePipe`, but the name you write in a template is
`titlecase`. That mapping is set in the pipe's own `@Pipe({ name: ... })` decorator, and we'll see it
first-hand in Step 6.

Forgetting the import is the number one pipe error. The message reads something like *"The pipe
'titlecase' could not be found"* — when you see that, come back here.

### Step 2 — `titlecase` and `uppercase`

Now use them. In `src/app/course-list/course-list.html`, change the title and instructor lines:

```html
    <h3>{{ course.title | titlecase }}</h3>
    <p>Taught by {{ course.instructor | titlecase }}</p>
```

Save. `intro to angular` becomes **Intro To Angular**, and `elena marsh` becomes **Elena Marsh**.

Add one more, on the format field. Insert this below the tuition line:

```html
    <p>Format: {{ course.format | uppercase }}</p>
```

`in-person` becomes **IN-PERSON**.

Check the class file while you're here: `CATALOG` still holds lowercase strings. Pipes only affect
rendering — nothing was mutated.

### Step 3 — `date`, and pipe arguments

The `date` pipe turns a date value into readable text. Add it to the import line and the `imports`
array:

```ts
import { DatePipe, TitleCasePipe, UpperCasePipe } from '@angular/common';
```

```ts
  imports: [DatePipe, TitleCasePipe, UpperCasePipe],
```

Then change the start-date line in the template:

```html
    <p>Starts {{ course.startDate | date }}</p>
```

Save. `2026-01-20T09:00:00` becomes **Jan 20, 2026**. Already much better — but we can be specific
about the format we want, and that's what pipe **arguments** are for.

An argument goes after a colon:

```html
    <p>Starts {{ course.startDate | date: 'fullDate' }} at {{ course.startDate | date: 'shortTime' }}</p>
```

Save. Now it reads **Starts Tuesday, January 20, 2026 at 9:00 AM**.

`'fullDate'` and `'shortTime'` are two of several named formats — `'short'`, `'medium'`, `'long'`,
`'shortDate'`, `'mediumTime'`, and others. You can also pass a custom pattern like `'dd/MM/yyyy'` or
`'EEEE'`. Try a couple and watch the output change.

Note that our `startDate` is a **string**, not a `Date` object. The `date` pipe accepts strings,
numbers, and `Date` objects, which is convenient — it means data straight off a server usually works
without conversion.

### Step 4 — `currency`

Same pattern. Add `CurrencyPipe`:

```ts
import { CurrencyPipe, DatePipe, TitleCasePipe, UpperCasePipe } from '@angular/common';
```

```ts
  imports: [CurrencyPipe, DatePipe, TitleCasePipe, UpperCasePipe],
```

And the tuition line:

```html
    <p>Tuition: {{ course.tuition | currency }}</p>
```

Save. `895` becomes **$895.00** and `640.5` becomes **$640.50** — the pipe adds the symbol, the
grouping, and the two decimal places.

Its first argument is a currency code. Add a second line below to see it:

```html
    <p>In pounds: {{ course.tuition | currency: 'GBP' }}</p>
```

That renders **£895.00**. (We're pretending the number means the same thing in both currencies, which
it doesn't — this line is here to show the argument, and it goes away in the exercises.)

### Step 5 — `json`, for when something isn't working

The `json` pipe is a debugging tool. It renders a value as formatted JSON, which is the fastest way to
see what a template is actually holding.

Add it:

```ts
import { CurrencyPipe, DatePipe, JsonPipe, TitleCasePipe, UpperCasePipe } from '@angular/common';
```

```ts
  imports: [CurrencyPipe, DatePipe, JsonPipe, TitleCasePipe, UpperCasePipe],
```

And drop this at the bottom of the `<article>`, just before the closing tag:

```html
    <pre>{{ course | json }}</pre>
```

Save. Every card now shows the raw object behind it. `<pre>` keeps the line breaks and indentation the
pipe produces.

This is a *development* tool — you'd never ship it. But when a binding shows nothing and you can't work
out why, `{{ something | json }}` usually answers the question in one step. Leave it in for now.

### Step 6 — Write a custom pipe

Built-in pipes cover the common cases. When you need a transformation specific to your app, you write
your own.

We'll build one that turns a name into initials: `elena marsh` → `e.m.`. Generate it:

```bash
ng generate pipe initials
```

That creates `src/app/initials-pipe.ts` with a skeleton:

```ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'initials',
})
export class InitialsPipe implements PipeTransform {
  transform(value: unknown, ...args: unknown[]): unknown {
    return null;
  }
}
```

Two required pieces:

- **`@Pipe({ name: 'initials' })`** — `name` is the text you'll type after the `|` in a template. It's
  entirely separate from the class name.
- **`implements PipeTransform`** — the interface that requires a `transform` method. That method takes
  the incoming value, returns the outgoing one, and that's the whole contract.

Replace the skeleton `transform` with a real implementation, and tighten the types while you're at it:

```ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'initials',
})
export class InitialsPipe implements PipeTransform {
  transform(value: string): string {
    return value
      .split(' ')
      .filter((part) => part.length > 0)
      .map((part) => part.charAt(0) + '.')
      .join('');
  }
}
```

The CLI's `unknown` types are a safe default, but narrowing them to `string` means Angular will
type-check the templates that use this pipe and tell you if you point it at a number by mistake.

Now use it. A custom pipe is imported by class, from its own file:

```ts
import { InitialsPipe } from '../initials-pipe';
```

```ts
  imports: [CurrencyPipe, DatePipe, JsonPipe, TitleCasePipe, UpperCasePipe, InitialsPipe],
```

And in the template:

```html
    <p>Taught by {{ course.instructor | titlecase }} ({{ course.instructor | initials }})</p>
```

Save. Each instructor line now ends with something like **(e.m.)**.

### Step 7 — Chain pipes together

Those initials should be capitalised. We could handle that inside the pipe, but there's already a pipe
that uppercases things — so let's just use both.

Pipes chain left to right: each one's output becomes the next one's input.

```html
    <p>Taught by {{ course.instructor | titlecase }} ({{ course.instructor | initials | uppercase }})</p>
```

Save. Now it reads **(E.M.)**.

Read that chain out loud: take `course.instructor`, reduce it to initials, then uppercase the result.
Order matters — `| uppercase | initials` would work here too, but chains where one pipe changes what
the next one sees often don't survive being reordered.

Chaining also works with arguments: `{{ course.startDate | date: 'fullDate' | uppercase }}` is
perfectly valid.

## Exercises

### Exercise 1 — A pipe for a different kind of value

Write a second custom pipe called `seat-status` that turns the `seatsLeft` number into a short phrase:
`0` becomes `Waitlist`, `1` to `5` becomes `Almost full`, and anything higher becomes `Open`.

Generate it with the CLI, type its `transform` signature properly (number in, string out), and use it
in the template. Then chain it with `uppercase`.

Once it works, notice that it does the same job as the `@if` chain from Lab 6. Delete the `@if` chain
and let the pipe handle it. Think about which version you'd rather maintain, and why the answer might
be different if each branch rendered different *markup* rather than different text.

### Exercise 2 — Clean up the display

The guided walkthrough left some scaffolding on the page. Tidy it:

1. Remove the "In pounds" line — it was only there to demonstrate an argument.
2. Remove the `{{ course | json }}` block.
3. Format the start date as day and month only, without the year, using a custom pattern rather than a
   named format. Look up the `date` pipe's format-character table in the Angular docs to work out the
   pattern.
4. Show the tuition with **no** cents — `$895` rather than `$895.00`. The `currency` pipe takes more
   than one argument; you'll need to read its documentation to find the right one and to work out what
   to pass for the arguments in between.

### Exercise 3 — A pipe on a new component

Generate a component called `receipt` and place it in `App` below the course list. Give it a
hardcoded object representing a single enrolment: a student name in lowercase, an amount paid, a
payment date, and a confirmation code.

Render it using at least four different pipes, including at least one chain of two, and at least one
pipe that takes an argument. Use `titlecase` on the name, `currency` on the amount, and `date` on the
payment date — then find one more built-in pipe in the Angular documentation that you haven't used yet
and apply it to the confirmation code.

## Check your work

### After the guided walkthrough

With `ng serve` running, the first course card at <http://localhost:4200> should read:

- **Intro To Angular** as the heading
- **Taught by Elena Marsh (E.M.)**
- **Starts Tuesday, January 20, 2026 at 9:00 AM**
- **Tuition: $895.00**
- **In pounds: £895.00**
- **Format: IN-PERSON**
- **Plenty of seats available.**
- A `<pre>` block showing the raw JSON for that course, with `"title": "intro to angular"` still in
  lowercase

That last point is the one to check carefully. The JSON still shows the original lowercase values,
proving the pipes changed only the display.

The second card should show **$640.50** for tuition, and the third — Web Accessibility — should still
say **Waitlist only.**

If you get a build error naming a pipe that "could not be found", that pipe is missing from the
component's `imports` array.

### After the exercises

- The seat-status pipe prints **WAITLIST**, **ALMOST FULL**, or **OPEN** on every card, matching what
  the `@if` chain used to say, and the `@if` chain is gone.
- The "In pounds" line and the JSON block are gone.
- Start dates read as something like **January 20** with no year, and tuition reads **$895** with no
  decimal places.
- The receipt component renders with at least four pipes applied, one of which is a chain and one of
  which takes an argument.
