# Lab 2 — Data Binding

## Objective

A component class and its template need to talk to each other in both directions. In this lab we'll
wire up all four ways Angular lets them do that, using a single sign-up form with one text field and
one button:

- **Interpolation** — `{{ value }}`, class to template, text only
- **Property binding** — `[value]`, `[disabled]`, class to template, into a DOM property
- **Event binding** — `(click)`, `(input)`, template back to class
- **Two-way binding** — `[(ngModel)]`, both directions at once, via `FormsModule`

We'll build the field the long way first, with a property binding *and* an event binding, and then
replace that pair with `[(ngModel)]` so you can see exactly what the two-way syntax is doing for you.

### Where we're starting from

This project ships with an `App` shell and one empty `SignupForm` component already generated and
nested — the state you'd be in right after running `ng generate component signup-form` in Lab 1. All
of your work happens inside `src/app/signup-form/`.

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

Then open <http://localhost:4200>. Leave `ng serve` running while you work.

## Guided walkthrough

### Step 1 — Interpolation: class to template

Start with the direction you already know. In `src/app/signup-form/signup-form.ts`, give the class
two properties:

```ts
export class SignupForm {
  workshopTitle = 'Intro to Angular';
  attendeeName = '';
}
```

Then replace the contents of `src/app/signup-form/signup-form.html` with:

```html
<h2>{{ workshopTitle }}</h2>

<p>Signing up as: {{ attendeeName }}</p>
```

Save. You'll see the workshop title, and "Signing up as:" with nothing after it, because
`attendeeName` is still an empty string. Our job for the rest of the lab is to get a value in there.

Interpolation only ever goes one way — the class pushes text into the template. It cannot read
anything back.

### Step 2 — Property binding: class into a DOM property

Now let's add the text field. Add this below the paragraph in `signup-form.html`:

```html
<input type="text" [value]="attendeeName" />
```

The square brackets are the important part. `[value]="attendeeName"` says "set this input element's
`value` property to whatever `attendeeName` currently holds." Without the brackets — plain
`value="attendeeName"` — the field would literally contain the eight characters `attendeeName`.

Property binding is still one-directional: class to template. Prove it to yourself. Change the
initial value in `signup-form.ts`:

```ts
attendeeName = 'Sam';
```

Save, and the box starts out containing "Sam" and the paragraph reads "Signing up as: Sam". Now type
something else into the box in the browser. The paragraph doesn't change — the class never hears
about your typing.

Set it back to an empty string before continuing:

```ts
attendeeName = '';
```

### Step 3 — More property binding: a disabled button

Let's add the button, and use another property binding to control whether it's clickable. Add this
below the input in `signup-form.html`:

```html
<button [disabled]="attendeeName.length === 0">Register</button>
```

The expression inside the quotes is evaluated as TypeScript, and its result — `true` or `false` — is
assigned to the button's `disabled` property. Since `attendeeName` is empty, the button starts out
greyed out.

Anything in the class is fair game inside a binding: properties, expressions, comparisons, and method
calls all work.

### Step 4 — Event binding: template back to the class

Time for the other direction. Events use parentheses instead of square brackets.

First, the button. Add a method to the `SignupForm` class:

```ts
export class SignupForm {
  workshopTitle = 'Intro to Angular';
  attendeeName = '';
  confirmation = '';

  register() {
    this.confirmation = `${this.attendeeName} is registered for ${this.workshopTitle}.`;
  }
}
```

Wire it to the button's click in `signup-form.html`:

```html
<button [disabled]="attendeeName.length === 0" (click)="register()">Register</button>
```

And add a line to display the confirmation, below the button:

```html
<p>{{ confirmation }}</p>
```

Save. The button is still disabled, because nothing is updating `attendeeName` yet — so let's fix
that with a second event binding.

Add another method to the class:

```ts
  updateName(event: Event) {
    this.attendeeName = (event.target as HTMLInputElement).value;
  }
```

And bind the input's `input` event to it:

```html
<input type="text" [value]="attendeeName" (input)="updateName($event)" />
```

`$event` is a special name Angular makes available inside an event binding — it's the DOM event
object itself. Because Angular types it as a generic `Event`, we have to tell TypeScript that
`event.target` is specifically an `<input>` before we can read `.value` off it. That's what the
`as HTMLInputElement` cast is doing.

Save and try it. Type in the box: the paragraph updates as you type, and the button un-greys as soon
as there's at least one character. Click **Register** and the confirmation appears.

This is a complete round trip. The class pushes `attendeeName` into the field with `[value]`, and the
field pushes edits back into the class with `(input)`.

### Step 5 — Two-way binding with `[(ngModel)]`

That round trip — bind a property in, listen for changes out — is so common that Angular has a single
piece of syntax for it. It's written `[(ngModel)]`, brackets *and* parentheses, which is why it's
nicknamed "banana in a box."

`ngModel` isn't built into every component. It lives in `FormsModule`, so we have to import it. In
`src/app/signup-form/signup-form.ts`, update the import line and the `imports` array:

```ts
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-signup-form',
  imports: [FormsModule],
  templateUrl: './signup-form.html',
  styleUrl: './signup-form.css',
})
```

Now replace the input in `signup-form.html`:

```html
<input type="text" [(ngModel)]="attendeeName" name="attendeeName" />
```

The `name` attribute isn't decoration — Angular asks for it when `ngModel` is used inside a form so
the control has an identity.

Save and try it again. The behaviour is identical to Step 4, but we no longer need the `(input)`
binding or the `updateName` method — `[(ngModel)]` handles both halves.

Delete `updateName` from the class. It's dead code now, and leaving it around will only confuse you
when you come back to this file.

### Step 6 — Read the whole thing back

Your `signup-form.ts` should now look like this:

```ts
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-signup-form',
  imports: [FormsModule],
  templateUrl: './signup-form.html',
  styleUrl: './signup-form.css',
})
export class SignupForm {
  workshopTitle = 'Intro to Angular';
  attendeeName = '';
  confirmation = '';

  register() {
    this.confirmation = `${this.attendeeName} is registered for ${this.workshopTitle}.`;
  }
}
```

Four syntaxes, four jobs, and the punctuation tells you which is which:

| Syntax | Direction | Example |
|---|---|---|
| `{{ }}` | class → template, as text | `{{ attendeeName }}` |
| `[ ]` | class → template, into a property | `[disabled]="..."` |
| `( )` | template → class | `(click)="register()"` |
| `[( )]` | both | `[(ngModel)]="attendeeName"` |

## Exercises

A different small problem, the same four bindings. Work in the same `SignupForm` component.

### Exercise 1 — Seat reservation

Below the existing form, build a seat-count control:

1. Add a `seats` property initialised to `1`, and a `pricePerSeat` property set to some number.
2. Add a number input (`<input type="number">`) bound with `[(ngModel)]` to `seats`. Give it a `name`
   attribute too.
3. Interpolate the running total — seats times price — into a paragraph below the input.
4. Add a **Reserve** button that is disabled unless `seats` is at least 1 and no more than 8. You'll
   need a slightly longer expression in the `[disabled]` binding than the one we wrote in Step 3.

Watch what type comes back from a number input bound with `ngModel` — the total should read as a
number, not as two values glued together.

### Exercise 2 — A reset button, without `ngModel`

Add a **Clear** button next to **Register** that empties `attendeeName`, resets `seats` to 1, and
blanks out `confirmation`.

Write it as an event binding to a `clearForm()` method on the class. Because the field is bound with
`[(ngModel)]`, you should not have to touch the DOM at all — setting the class property is enough,
and the field will follow. Confirm that for yourself in the browser.

### Exercise 3 — Go back to the long way

Two-way binding is convenience syntax. Prove you can still write it out by hand.

Take the seat-count input from Exercise 1 and replace its `[(ngModel)]` with the Step 4 pattern
instead: a `[value]` property binding plus an `(input)` event binding to a method you write. The
behaviour in the browser should be indistinguishable from the `ngModel` version.

Remember that `event.target.value` comes back as a string even from a number input, so the total will
need a conversion somewhere to stay a real number.

## Check your work

### After the guided walkthrough

With `ng serve` running, <http://localhost:4200> should show a heading, the sign-up form, and:

- The page loads with an empty text field, an empty "Signing up as:" line, and a **greyed-out**
  Register button.
- Typing into the field updates the "Signing up as:" line **on every keystroke**, not on blur or
  Enter.
- The Register button becomes clickable as soon as the field has at least one character, and greys
  out again if you delete everything.
- Clicking Register prints something like **Sam is registered for Intro to Angular.**
- The `signup-form.ts` class contains `register()` and no longer contains `updateName()`.

If you see a template error mentioning `ngModel`, `FormsModule` isn't in the component's `imports`
array. If you see one about `name`, the input is missing its `name` attribute.

### After the exercises

- The number input reserves seats, and the running total updates as you change the count. Setting
  seats to 3 with a price of 20 should read `60`, not `320` — if you get the latter, the value coming
  back is a string and needs converting.
- The Reserve button is disabled at 0 seats and at 9 seats, and enabled everywhere between 1 and 8.
- Clicking Clear empties the text field on screen even though `clearForm()` only assigns to class
  properties.
- After Exercise 3, the seat input still works exactly as before, but `[(ngModel)]` no longer appears
  on it — just `[value]` and `(input)`.
