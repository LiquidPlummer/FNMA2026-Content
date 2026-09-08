# Lab 3 — Parent/Child Communication

## Objective

In Lab 2 we moved data between a class and its own template. Now we'll move data between two
*different* components: a parent that owns some state and a child that displays and edits it.

Angular gives each direction its own tool:

- **`@Input()`** — the parent passes a value *down* into the child
- **`@Output()`** with an **`EventEmitter`** — the child sends a value back *up* to the parent

We'll also put a `[(ngModel)]`-bound field inside the child, so the child has some local editing state
of its own that it only reports upward when the user is ready.

The rule this lab is really teaching is that data flows **down** and events flow **up**. A child never
reaches into its parent and changes something.

### Where we're starting from

This project ships with three pieces already in place:

- `src/app/member.ts` — a plain TypeScript interface describing the shape of a member. It isn't an
  Angular file at all, just a type we can share between components.
- `src/app/profile-editor/` — the **parent**, with one `member` object property already filled in.
- `src/app/member-card/` — the **child**, empty.

`App` already hosts `<app-profile-editor />`. All of your work happens in the profile editor and the
member card.

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

### Step 1 — Nest the child in the parent

Same two edits as Lab 1. In `src/app/profile-editor/profile-editor.ts`, import the child and add it
to `imports`:

```ts
import { MemberCard } from '../member-card/member-card';
```

```ts
  imports: [MemberCard],
```

Then in `src/app/profile-editor/profile-editor.html`, replace the comment with the tag:

```html
<h2>Profile Editor</h2>

<app-member-card />
```

Save. You should see "The member card goes here." on the page. The child is on screen, but it has no
idea the parent is holding a member object.

### Step 2 — Declare an input on the child

An `@Input()` is a property the parent is allowed to set from the template. In
`src/app/member-card/member-card.ts`:

```ts
import { Component, Input } from '@angular/core';
import { Member } from '../member';

@Component({
  selector: 'app-member-card',
  imports: [],
  templateUrl: './member-card.html',
  styleUrl: './member-card.css',
})
export class MemberCard {
  @Input() member!: Member;
}
```

Two things to note. `Input` has to be imported from `@angular/core` alongside `Component` — it's a
decorator, and decorators need importing like anything else. And the `!` after `member` is a
TypeScript signal meaning "I know this looks like it's never assigned, but trust me, it will be set
before anything reads it." Angular sets inputs from the outside, so TypeScript can't see the
assignment on its own.

Now use it in `src/app/member-card/member-card.html`:

```html
<h3>{{ member.name }}</h3>
<p>{{ member.email }}</p>
<p>Role: {{ member.role }}</p>
```

Save. The page will go blank or throw — the input is declared, but the parent still isn't passing
anything into it, so `member` is `undefined`. One more edit fixes that.

### Step 3 — Pass data down from the parent

An input is set with **property binding** — the exact same square-bracket syntax from Lab 2, except
the target is a component input rather than a DOM property.

In `src/app/profile-editor/profile-editor.html`:

```html
<app-member-card [member]="member" />
```

Read that carefully, because the repetition trips people up. The `[member]` on the left is the
child's input name. The `"member"` on the right is the parent's property. They happen to share a
name here, but they don't have to — try renaming the parent's property to `currentMember` and
updating the right-hand side to see which half is which, then change it back.

Save. Sam Ortiz's name, email, and role now render inside the child.

Data is flowing down. Now for the other direction.

### Step 4 — Add local editing state in the child

Before the child can report a change, it needs somewhere to hold the edit in progress. We'll use
`[(ngModel)]` from Lab 2 for that.

Add `FormsModule` to the child's imports and a `draftRole` property to its class, in
`src/app/member-card/member-card.ts`:

```ts
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Member } from '../member';

@Component({
  selector: 'app-member-card',
  imports: [FormsModule],
  templateUrl: './member-card.html',
  styleUrl: './member-card.css',
})
export class MemberCard {
  @Input() member!: Member;
  draftRole = '';
}
```

Then add the field and a button at the bottom of `src/app/member-card/member-card.html`:

```html
<label>
  New role:
  <input type="text" [(ngModel)]="draftRole" name="draftRole" />
</label>
<button [disabled]="draftRole.length === 0">Apply</button>
```

Save and type in the field. Nothing happens elsewhere yet, and that's correct — `draftRole` belongs
to the child alone. The parent has no visibility into it.

### Step 5 — Declare an output on the child

To send the draft upward, the child declares an `@Output()`. An output is an `EventEmitter`: the child
calls `.emit(value)` on it, and any parent listening receives that value.

In `src/app/member-card/member-card.ts`, extend the import line and add the output plus a method:

```ts
import { Component, EventEmitter, Input, Output } from '@angular/core';
```

```ts
export class MemberCard {
  @Input() member!: Member;
  @Output() roleChange = new EventEmitter<string>();
  draftRole = '';

  applyRole() {
    this.roleChange.emit(this.draftRole);
    this.draftRole = '';
  }
}
```

The `<string>` on the `EventEmitter` says what kind of value this output carries. Keep it accurate —
it's what lets Angular type-check the parent's handler.

Wire the button to the method in `member-card.html`:

```html
<button [disabled]="draftRole.length === 0" (click)="applyRole()">Apply</button>
```

Save and click Apply. The field clears, so we know `applyRole()` ran, but nothing else changes — the
event is being emitted into a room with nobody in it.

### Step 6 — Listen for the event in the parent

A parent subscribes to an output with **event binding** — the same parentheses from Lab 2, except the
event name is the output's name rather than a DOM event.

Add a handler to `src/app/profile-editor/profile-editor.ts`:

```ts
export class ProfileEditor {
  member: Member = {
    name: 'Sam Ortiz',
    email: 'sortiz@riverbend.edu',
    role: 'Student',
  };

  lastChange = 'No changes yet.';

  onRoleChange(newRole: string) {
    this.member.role = newRole;
    this.lastChange = `Role changed to ${newRole}.`;
  }
}
```

And bind it in `src/app/profile-editor/profile-editor.html`:

```html
<h2>Profile Editor</h2>

<app-member-card [member]="member" (roleChange)="onRoleChange($event)" />

<p>{{ lastChange }}</p>
```

`$event` here is the value the child emitted — the string passed to `.emit()`. It is *not* a DOM
event; inside an output binding, `$event` means "whatever came out of the emitter."

Save and try the whole loop: type a new role, click Apply, and watch the child's "Role:" line update
along with the parent's status line. The child never touched the parent's data — it only announced
what happened, and the parent decided what to do about it.

### Step 7 — See the reuse

One child component can be placed more than once, each with its own input. Add a second member to
`src/app/profile-editor/profile-editor.ts`:

```ts
  colleague: Member = {
    name: 'Priya Raman',
    email: 'praman@riverbend.edu',
    role: 'Teaching Assistant',
  };
```

And a second tag in `profile-editor.html`, above the status paragraph:

```html
<app-member-card [member]="colleague" (roleChange)="onRoleChange($event)" />
```

Save. Two cards, each with its own draft field. Editing one doesn't disturb the other, because
`draftRole` is per-instance state — every placement of a component gets its own copy of the class.

You'll notice both cards report to the same handler, which always updates `member` and never
`colleague`. That's a real bug, and Exercise 2 is about fixing it.

## Exercises

### Exercise 1 — A second output

The card can report a role change. Give it a way to report something else entirely.

Add a **Send reminder** button to the member card that emits a *second* output — carrying the
member's email address rather than the draft role. In the parent, handle it by appending to a
`reminderLog` string that lists everyone who has been reminded, and interpolate that log below the
cards.

You'll be declaring a second `@Output()` with its own `EventEmitter`, and binding a second event on
each `<app-member-card>` tag. Nothing you haven't seen — but do it without copying Step 5 line by
line.

### Exercise 2 — Fix the shared handler

Right now both cards call `onRoleChange`, which always edits `member`. Change the role of the second
card and watch the wrong person's role update.

Fix it so each card updates the right member. There is more than one reasonable way to do this. One
approach is a separate handler per card; another is having the child emit enough information for a
single handler to tell the two apart. Pick one and make it work.

### Exercise 3 — Input down, no output up

Not every input needs a matching output. Add a `readOnly` boolean input to the member card. When it's
`true`, the card should still display the member's details, but the role field and Apply button
should be disabled.

Pass `true` for the colleague card and `false` for the first one, then confirm in the browser that
only one card is editable.

Think about how you'd pass a literal `true` rather than a class property. `readOnly="true"` and
`[readOnly]="true"` are not the same thing — one passes the four-character string `"true"`, the other
passes the boolean. Only one of them will type-check.

## Check your work

### After the guided walkthrough

With `ng serve` running, <http://localhost:4200> should show:

- A "Profile Editor" heading, then two member cards, then a status line.
- The first card reads **Sam Ortiz**, `sortiz@riverbend.edu`, **Role: Student**. The second reads
  **Priya Raman**, `praman@riverbend.edu`, **Role: Teaching Assistant**.
- Each card has its own text field and an Apply button that starts out greyed out.
- Typing in the first card's field does **not** change the second card's field.
- Typing "Alumnus" in the first card and clicking Apply changes that card's role line to
  **Role: Alumnus**, clears the field, and updates the status line to **Role changed to Alumnus.**
- Typing in the *second* card and clicking Apply changes the *first* card's role. That's the known
  bug from Step 7 — leave it for Exercise 2.

If the page is blank and the console shows an error about reading a property of `undefined`, the
`[member]` binding is missing from one of the tags.

### After the exercises

- Each card has a Send reminder button, and clicking it adds that member's email to a log below the
  cards without touching either role.
- Applying a role change on the second card now updates **Priya Raman's** role, and applying one on
  the first card still updates **Sam Ortiz's**.
- The second card's role field and Apply button are permanently disabled, while the first card's
  still work. Its name, email, and role still display normally.
