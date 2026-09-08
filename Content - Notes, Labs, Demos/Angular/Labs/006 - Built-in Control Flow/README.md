# Lab 6 — Built-in Control Flow

## Objective

Up to now every template we've written has rendered a fixed set of elements. In this lab we'll make
the template itself make decisions: repeat a block once per item, show one thing or another depending
on a value, and pick between several branches.

Angular's built-in control flow blocks all start with `@`:

- **`@for`** — repeat a block for each item, with its required **`track`** expression
- **`@empty`** — what to show when the collection has nothing in it
- **`@if`** / **`@else if`** / **`@else`** — branch on a condition
- **`@switch`** / **`@case`** / **`@default`** — branch on a value with several known options

These are template syntax, not directives. You don't import anything to use them, and they're built
into the compiler.

### A note on `*ngFor` and `*ngIf`

If you search the web for Angular examples, most of what you find will use the older syntax:
`*ngFor="let item of items"` and `*ngIf="condition"`. Those still work, but the `@`-blocks replaced
them in Angular 17 and are what you should write in new code. They're faster, they don't need imports,
and `@else` alone makes them worth the switch. We won't be using the old form in these labs — just
recognise it when you see it.

### Where we're starting from

This project ships with a `CourseList` component holding a hardcoded array of five course objects, and
a template that renders none of them. There's also a `Course` interface in `src/app/course.ts`
describing the shape of the data.

All of your work happens in `src/app/course-list/`.

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

Then open <http://localhost:4200>. You'll see the headings and nothing else — the data is there, but
the template isn't showing it yet.

## Guided walkthrough

### Step 1 — Look at the data

Open `src/app/course-list/course-list.ts`. Above the component there's a `CATALOG` constant with five
courses in it, and the class holds it in a signal:

```ts
  courses = signal<Course[]>(CATALOG);
```

Each course has an `id`, a `title`, an `instructor`, a `seatsLeft` count, and a `format`. Notice the
variety in that data: seat counts of 12, 3, 0, 24, and 1, and formats of `in-person`, `online`, and
`hybrid`. That spread is deliberate — it'll exercise every branch we write.

### Step 2 — Repeat a block with `@for`

Replace the comment in `src/app/course-list/course-list.html` with a loop:

```html
@for (course of courses(); track course.id) {
  <article>
    <h3>{{ course.title }}</h3>
    <p>Taught by {{ course.instructor }}</p>
  </article>
}
```

Save, and all five courses render.

Three parts to that first line:

- **`course of courses()`** — for each item in the collection, call it `course`. The parentheses on
  `courses()` are there because it's a signal, same as always.
- **`track course.id`** — how Angular identifies each item across re-renders.
- **The braces** — everything inside is the block that gets repeated.

`track` is **required**. Leave it off and the build fails, which is unusual for a framework and very
deliberate. Angular needs a stable identity per item so that when the array changes it can move the
existing DOM elements around instead of destroying and rebuilding all of them. Track by a unique,
stable property — an `id` is ideal.

If your data genuinely has no unique field, `track $index` is the fallback. Reach for it last, not
first: if the array ever reorders, tracking by index tells Angular the item at position 2 is still the
same item, which is exactly wrong.

### Step 3 — Branch with `@if` and `@else`

Some of these courses are nearly full and one has no seats at all. Let's say something different in
each case.

Inside the `<article>`, below the instructor line, add:

```html
    @if (course.seatsLeft > 0) {
      <p>{{ course.seatsLeft }} seats left.</p>
    } @else {
      <p>Waitlist only.</p>
    }
```

Save. Four courses show a seat count; Web Accessibility, sitting at zero, shows "Waitlist only."

The condition is any expression that evaluates to something truthy or falsy, and `@else` is optional —
an `@if` on its own is perfectly normal.

Note where `@else` goes: on the **same line** as the closing brace of the `@if` block. Put it on its
own line and Angular won't connect the two.

### Step 4 — Add a middle branch with `@else if`

Three seats left and twenty-four seats left aren't really the same situation. Add a middle case:

```html
    @if (course.seatsLeft > 5) {
      <p>Plenty of seats available.</p>
    } @else if (course.seatsLeft > 0) {
      <p>Hurry &mdash; only {{ course.seatsLeft }} seats left.</p>
    } @else {
      <p>Waitlist only.</p>
    }
```

Save. Now Intro to Angular and Database Design say "Plenty of seats available", TypeScript
Fundamentals and Version Control say "Hurry", and Web Accessibility still says "Waitlist only."

You can chain as many `@else if` branches as you like. Angular checks them top to bottom and renders
the first one that matches — exactly like an `if`/`else if` chain in TypeScript.

### Step 5 — Branch on a value with `@switch`

`@if` chains get unwieldy when you're comparing one value against a list of known options. `format` is
that kind of value, so it's a job for `@switch`.

Below the `@if` chain, still inside the `<article>`:

```html
    @switch (course.format) {
      @case ('in-person') {
        <p>Meets on campus, Tuesdays and Thursdays.</p>
      }
      @case ('online') {
        <p>Fully online, work at your own pace.</p>
      }
      @default {
        <p>Schedule to be announced.</p>
      }
    }
```

Save. Two courses meet on campus, two are online, and Web Accessibility — the `hybrid` one — falls
through to `@default` because we never wrote a `@case` for it.

Two differences from the `switch` statement you know from TypeScript, both improvements:

- There is **no `break`**. Cases don't fall through, so there's nothing to forget.
- `@default` is optional, and if no case matches and there's no default, Angular simply renders
  nothing.

The comparison is strict equality, so `@case ('in-person')` matches only the exact string.

### Step 6 — Handle the empty case with `@empty`

Right now the loop always has data. Let's give ourselves a way to empty it.

Add two methods to `src/app/course-list/course-list.ts`:

```ts
  clearCatalog() {
    this.courses.set([]);
  }

  restoreCatalog() {
    this.courses.set(CATALOG);
  }
```

And two buttons at the top of `course-list.html`, just under the `<h2>`:

```html
<button (click)="clearCatalog()">Clear catalog</button>
<button (click)="restoreCatalog()">Restore catalog</button>
```

Save and click **Clear catalog**. Everything disappears, leaving a blank page — technically correct,
but a bad experience. Let's give the empty state something to say.

Attach an `@empty` block to the loop. It goes on the same line as the `@for` block's closing brace,
just like `@else`:

```html
} @empty {
  <p>No courses are listed for this term.</p>
}
```

Save and click **Clear catalog** again. Now you get a message instead of a void. Click **Restore
catalog** and the list comes back.

`@empty` is specific to `@for` and only renders when the collection has zero items. It saves you from
wrapping the whole loop in an `@if (courses().length > 0)`, which is what people used to have to do.

### Step 7 — Check the shape of the finished template

Your `course-list.html` should now nest like this:

```
@for                     one <article> per course
  @if / @else if / @else seat availability
  @switch / @case        format description
@empty                   message when the catalog is cleared
```

Blocks nest freely, and it's normal to have conditionals inside a loop. Keep an eye on your closing
braces — a mismatched one produces a template parse error that points at the end of the file rather
than at the real problem, so indentation is your friend here.

## Exercises

### Exercise 1 — A different list, from scratch

Generate a component called `office-hours` and place it in `App` below the course list.

Give it a hardcoded array of five appointment objects, each with an `id`, a `student` name, a `day`,
and a `status` that is one of `'confirmed'`, `'pending'`, or `'cancelled'`. Hold it in a signal.

In its template:

- Use `@for` with a proper `track` to render one block per appointment.
- Use `@switch` on `status` to show a different message for each of the three values, plus a
  `@default` branch.
- Use `@if` / `@else` to show "Today" when the day matches a `today` property on the class, and the
  day name otherwise.
- Use `@empty` to show a message when there are no appointments, with a button that empties the list.

### Exercise 2 — The `@for` context variables

Inside a `@for` block, Angular gives you more than just the item. Add `$index`, `$first`, `$last`,
`$even`, and `$count` to your vocabulary.

In the course list:

1. Number each course by displaying its position in the list. `$index` starts at zero, so you'll need
   to adjust it to read naturally.
2. Show the text "Featured" only on the first course, using `$first`.
3. Below the loop, show a line reading "5 courses listed" that uses `$count` from inside the loop —
   which means you'll have to think about where a value from inside the loop can actually be read.
   If it turns out it can't, say so in a comment and get the number a different way.

### Exercise 3 — Deliberately break `track`

Change the loop's `track course.id` to `track $index`, then add a button that calls a method reversing
the order of the courses signal.

Click it and watch the list closely — with only text in each block, it will probably look fine. Now add
an `<input type="text">` inside each `<article>`, type something different into two of them, and click
reverse again.

Note what happens to the text you typed. Then change `track` back to `course.id` and try it once more.
Write a one-line comment in the template explaining the difference. This is what `track` is for, and
it's much easier to remember once you've watched it go wrong.

## Check your work

### After the guided walkthrough

With `ng serve` running, <http://localhost:4200> should show five course blocks. Specifically:

| Course | Availability line | Format line |
|---|---|---|
| Intro to Angular | Plenty of seats available. | Meets on campus, Tuesdays and Thursdays. |
| TypeScript Fundamentals | Hurry — only 3 seats left. | Fully online, work at your own pace. |
| Web Accessibility | Waitlist only. | Schedule to be announced. |
| Database Design | Plenty of seats available. | Meets on campus, Tuesdays and Thursdays. |
| Version Control with Git | Hurry — only 1 seats left. | Fully online, work at your own pace. |

Yes, "1 seats left" reads badly. Leave it — it's a good reminder that a conditional chain only covers
the cases you actually wrote.

Also confirm:

- **Clear catalog** replaces all five blocks with **No courses are listed for this term.**
- **Restore catalog** brings all five back.
- The build succeeds. If it fails with a message about `track`, the `@for` is missing its track
  expression.

### After the exercises

- The office hours component renders five appointments with three distinct status messages, and its
  empty state works.
- Every course shows its position in the list starting at 1, and only the first one is marked
  "Featured".
- In Exercise 3, with `track $index`, reversing the list leaves the typed-in text sitting in the wrong
  rows — the DOM elements stayed put and only the data moved. With `track course.id`, the text follows
  its course to the new position.
