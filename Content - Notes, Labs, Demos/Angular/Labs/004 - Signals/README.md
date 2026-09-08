# Lab 4 — Signals

## Objective

This lab has no new features in it. We're going to take a working parent/child pair and rewrite it
using **signals**, Angular's reactive way of holding state. When we're done the app will behave
exactly as it does now — the point is to see the two styles side by side and understand what the
newer one buys us.

We'll cover:

- `signal()` for writable state, and reading it by **calling** it: `member()`
- `.set()` to replace a signal's value and `.update()` to derive a new value from the old one
- `computed()` for a read-only value derived from other signals
- Converting `@Input()` → `input()`, `@Output()` → `output()`, and a two-way bound property →
  `model()`

The idea underneath all of it: a signal doesn't just hold a value, it tells Angular *when the value
changed*. Angular then re-renders only what actually read that signal.

### Where we're starting from

This project ships with the finished state of Lab 3, trimmed to a single member card so the refactor
stays focused. Everything is decorator-based: a plain `member` object on the parent, an `@Input()`
and an `@Output()` on the child, and a plain `draftRole` string bound with `[(ngModel)]`.

Run it before you change anything, so you know what "unchanged behaviour" is supposed to look like.

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

Then open <http://localhost:4200>. Type a role into the field, click Apply, and watch the card and
the status line update. That's the behaviour we have to preserve.

## Guided walkthrough

### Step 1 — Turn the parent's state into signals

Start with the parent, because it owns the data. In `src/app/profile-editor/profile-editor.ts`,
import `signal` and wrap both properties:

```ts
import { Component, signal } from '@angular/core';
```

```ts
  member = signal<Member>({
    name: 'Sam Ortiz',
    email: 'sortiz@riverbend.edu',
    role: 'Student',
  });

  lastChange = signal('No changes yet.');
```

`signal(...)` takes the initial value and hands back a signal holding it. Note that we no longer need
`: Member` as a type annotation on the property — the type goes inside the angle brackets instead.
For `lastChange` we didn't even need that, because TypeScript infers `string` from the initial value.

The class won't compile yet, and that's fine — `onRoleChange` is still assigning to these properties
as if they were plain values. That's next.

### Step 2 — Write to a signal with `.set()` and `.update()`

A signal is not a variable you assign to. You change it by calling a method on it, which is exactly
what lets Angular know something happened.

Rewrite `onRoleChange` in `profile-editor.ts`:

```ts
  onRoleChange(newRole: string) {
    this.member.update((current) => ({ ...current, role: newRole }));
    this.lastChange.set(`Role changed to ${newRole}.`);
  }
}
```

Two different methods, for two different situations:

- **`.set(value)`** replaces the signal's value outright. We know the whole new status message, so we
  just set it.
- **`.update(fn)`** takes a function that receives the current value and returns the new one. We only
  want to change the member's `role`, so we spread the existing object and override that one field.

That spread — `{ ...current, role: newRole }` — is doing something important. The old code said
`this.member.role = newRole`, mutating the object in place. A signal compares the *value* it holds,
and mutating an object in place leaves the signal pointing at the very same object, so it has nothing
new to report. Producing a **new object** is what makes the change visible.

This is the single most common mistake when people first move to signals: reaching inside a signal's
value and editing it, then wondering why nothing re-rendered.

### Step 3 — Read signals in the template

A signal is a function. To get the value out, you call it — including in templates.

In `src/app/profile-editor/profile-editor.html`, add the parentheses:

```html
<h2>Profile Editor</h2>

<app-member-card [member]="member()" (roleChange)="onRoleChange($event)" />

<p>{{ lastChange() }}</p>
```

Note `[member]="member()"` — we're passing the *value* down to a child that still expects a plain
`Member`. We'll change that in the next step.

Save. The app should build and behave exactly as before. If the status line renders as something like
`function () { ... }` instead of your message, you forgot the `()`.

### Step 4 — `@Input()` becomes `input()`

Now the child. In `src/app/member-card/member-card.ts`, replace the decorator import and the input
declaration:

```ts
import { Component, EventEmitter, Output, input } from '@angular/core';
```

```ts
  member = input.required<Member>();
```

Compare that to what it replaces:

```ts
  @Input() member!: Member;
```

Three things got better. The `!` is gone, because `input.required<Member>()` tells Angular *and*
TypeScript that this input must be provided — leave the binding off the tag and the build fails
instead of blowing up at runtime. There's no decorator, so nothing special has to be imported for the
metadata to work. And the result is a signal, so anything derived from it can react to it.

(If an input is genuinely optional, you'd write `input<Member>()` for one that can be `undefined`, or
`input('Student')` to give it a default. We want this one required.)

Because `member` is now a signal, every read of it in `src/app/member-card/member-card.html` needs
parentheses:

```html
<h3>{{ member().name }}</h3>
<p>{{ member().email }}</p>
<p>Role: {{ member().role }}</p>
```

The parent template doesn't change at all. `[member]="member()"` was already passing a plain value,
and that's still what an input binding wants — the signal lives inside the child.

### Step 5 — `@Output()` becomes `output()`

Same idea on the way out. In `member-card.ts`:

```ts
import { Component, input, output } from '@angular/core';
```

```ts
  roleChange = output<string>();
```

`EventEmitter` and `Output` are no longer needed, so drop them from the import line entirely.

The `applyRole` method barely changes — `output()` still gives you an `.emit()`:

```ts
  applyRole() {
    this.roleChange.emit(this.draftRole);
    this.draftRole = '';
  }
```

And the parent's `(roleChange)="onRoleChange($event)"` binding is untouched. From the outside, an
`output()` and an `@Output()` look identical.

Save and confirm the app still works end to end.

### Step 6 — A two-way bound property becomes `model()`

The last piece is `draftRole`, the child's local editing state. Convert it with `model()`:

```ts
import { Component, input, model, output } from '@angular/core';
```

```ts
  draftRole = model('');
```

A `model()` is a writable signal *and* an input *and* an output, all in one declaration. It's what
you reach for when a value should be editable from both sides.

Because it's a writable signal, `applyRole` now reads and writes it the signal way:

```ts
  applyRole() {
    this.roleChange.emit(this.draftRole());
    this.draftRole.set('');
  }
```

In `member-card.html`, the `[(ngModel)]` binding stays exactly as it is — Angular's two-way syntax
understands writable signals and will call `.set()` for you when the user types. Only the *reads* need
parentheses:

```html
<label>
  New role:
  <input type="text" [(ngModel)]="draftRole" name="draftRole" />
</label>
<button [disabled]="draftRole().length === 0" (click)="applyRole()">Apply</button>
```

Save and check the field still works.

### Step 7 — Bind the model from the parent

So far `model()` looks like a wordier `signal()`. Here's the part that makes it different: because a
model is also an input and an output, the **parent** can two-way bind it.

Add a signal to `src/app/profile-editor/profile-editor.ts`:

```ts
  pendingRole = signal('');
```

And bind it in `profile-editor.html`:

```html
<app-member-card
  [member]="member()"
  [(draftRole)]="pendingRole"
  (roleChange)="onRoleChange($event)" />

<p>Draft in progress: {{ pendingRole() }}</p>
<p>{{ lastChange() }}</p>
```

Two details worth noticing. `[(draftRole)]="pendingRole"` passes the signal itself, **without**
parentheses — two-way binding needs to write back, so it needs the signal, not a snapshot of its
value. And this is the same banana-in-a-box syntax as `[(ngModel)]`, because `ngModel` was never
special; it's just a directive with a two-way binding.

Save and type in the child's field. The parent's "Draft in progress" line tracks every keystroke, and
clears when you click Apply — because the child called `.set('')` on a signal the parent is holding.

### Step 8 — Derive a value with `computed()`

The last signal tool: `computed()` creates a **read-only** signal whose value is calculated from other
signals. It recalculates only when something it read actually changes, and it caches the result in
between.

Add one to `member-card.ts`:

```ts
import { Component, computed, input, model, output } from '@angular/core';
```

```ts
  headline = computed(() => `${this.member().name} — ${this.member().role}`);
```

Then use it in `member-card.html`, replacing the `<h3>`:

```html
<h3>{{ headline() }}</h3>
```

Note that we never tell `computed` what it depends on. It works that out by watching which signals get
read while the function runs — here, `member()`. Change the member's role and the headline updates on
its own.

A computed signal has no `.set()` and no `.update()`. That's the point: its value is defined entirely
by its inputs, so there's nothing to write.

### Step 9 — Look at what changed

Read both files top to bottom. The app does exactly what it did in Lab 3, but:

| Lab 3 | Lab 4 |
|---|---|
| `member: Member = {...}` | `member = signal<Member>({...})` |
| `this.member.role = x` | `this.member.update(m => ({ ...m, role: x }))` |
| `@Input() member!: Member` | `member = input.required<Member>()` |
| `@Output() x = new EventEmitter<string>()` | `x = output<string>()` |
| `draftRole = ''` | `draftRole = model('')` |
| `{{ member.name }}` | `{{ member().name }}` |

No decorators left on class members, no `!`, no `EventEmitter` import — and every read is a function
call.

## Exercises

### Exercise 1 — A computed with two dependencies

Add a `profileComplete` computed signal to the member card that is `true` only when the member has a
name, an email containing an `@`, and a role that isn't the string `'Unassigned'`.

Display the result somewhere on the card. Then, in the parent, add a button that sets the member's
role to `'Unassigned'` and confirm the displayed value flips on its own — you should not have to write
any code that updates it.

### Exercise 2 — `.update()` versus `.set()`, and the mutation trap

Add a `viewCount` signal to the parent, starting at 0, and a **View profile** button that increments
it. Write the increment with `.update()` rather than `.set()`.

Then deliberately break something, to see the failure mode from Step 2 for yourself:

1. Add a **Promote** button whose handler does `this.member().role = 'Alumnus'` — reaching into the
   signal's value and mutating it directly.
2. Click it. Note what does and doesn't appear on screen.
3. Now click **View profile**, which changes a completely unrelated signal, and watch what happens to
   the role.
4. Fix the Promote handler to use `.update()` properly.

Write yourself a one-line comment in the file explaining what you saw in step 3.

### Exercise 3 — A model on a new component

Generate a third component, `seat-counter`, with:

- A `count` declared as a `model(0)`
- Buttons inside it that increment and decrement `count` using `.update()`
- A `label` declared as a required `input<string>()`, displayed above the buttons

Place it in the profile editor, two-way bound to a signal on the parent, and display the parent's copy
of the value next to the card. Clicking the child's buttons should move the parent's number, and you
should be able to add a button *on the parent* that resets the count to zero and see the child follow.

## Check your work

### After the guided walkthrough

The app should be indistinguishable from Lab 3's finished state, plus one new line:

- The card shows **Sam Ortiz — Student** as its heading, then the email, then **Role: Student**.
- Typing in the role field updates the parent's **Draft in progress:** line on every keystroke.
- The Apply button is greyed out while the field is empty.
- Clicking Apply changes the heading to **Sam Ortiz — Alumnus** (or whatever you typed), changes the
  **Role:** line to match, sets the status line to **Role changed to Alumnus.**, clears the field, and
  clears the **Draft in progress:** line.

Then check the code itself:

- Neither `member-card.ts` nor `profile-editor.ts` imports `Input`, `Output`, or `EventEmitter`.
- There are no `@` decorators on any class member — only on the classes themselves.
- Every signal read in a template has `()` after it, *except* `[(draftRole)]="pendingRole"`, which
  passes the signal itself.

If clicking Apply changes the status line but **not** the role on the card, `onRoleChange` is mutating
the member object instead of producing a new one with `.update()`.

### After the exercises

- The profile-complete indicator flips to false the moment the role becomes `'Unassigned'`, with no
  code of yours updating it.
- The View profile counter increments on every click.
- In Exercise 2 you should have seen the mutated role **not** appear when you clicked Promote, then
  suddenly appear when you clicked View profile. If that surprised you, that's the lesson: the object
  had changed all along, but nothing told Angular to look.
- The seat counter moves in both directions — child buttons update the parent's number, and the
  parent's reset button zeroes out the child's display.
