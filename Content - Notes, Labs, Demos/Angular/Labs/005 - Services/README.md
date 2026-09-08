# Lab 5 — Services

## Objective

Lab 3 moved data between a parent and a child. But what about two components that *aren't* related —
siblings sitting next to each other in a template, with no input or output between them?

Passing values up to a shared parent and back down again works, but it gets miserable fast. The
Angular answer is a **service**: a plain class that holds state, injected into every component that
needs it. All of them get the *same instance*, so all of them see the same data.

We'll cover:

- `@Injectable({ providedIn: 'root' })` — what makes a class injectable, and what "root" means
- `inject()` — how a component gets hold of the service
- A service holding one signal plus the two methods that change it

### Where we're starting from

This project ships with a working room-booking screen that has a deliberate bug in it, and the bug is
the whole point of the lab.

There are two sibling components under `App`:

- `booking-panel` — buttons to book and cancel a seat
- `capacity-display` — a read-out of how many seats are booked and how many are free

Each one declares its own `seatsBooked = signal(0)`. Two signals, two separate numbers, no connection
between them. Run the app and click **Book a seat** a few times: the panel's count goes up and the
capacity display sits there at zero, blissfully unaware.

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

Then open <http://localhost:4200> and click **Book a seat** three or four times so you can see the two
components disagree with each other.

## Guided walkthrough

### Step 1 — See the duplication clearly

Open `src/app/booking-panel/booking-panel.ts` and `src/app/capacity-display/capacity-display.ts` side
by side. Both declare:

```ts
  capacity = 8;
  seatsBooked = signal(0);
```

Those look like the same state, but they are two entirely separate signals living in two separate
objects. Nothing ties them together. When the panel calls `.update()`, only the panel's copy moves.

The fix isn't to wire the two components together. It's to take the state out of both of them and put
it somewhere they can share.

### Step 2 — Generate the service

```bash
ng generate service booking-service
```

You can shorten this to `ng g s booking-service`.

The CLI creates one file, `src/app/booking-service.ts`, with nothing in it but the decorator:

```ts
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class BookingService {}
```

Notice it isn't in a folder of its own the way components are, and it has no template — a service is
just a class. The only thing making it special is `@Injectable`.

`providedIn: 'root'` is the part to remember. It tells Angular: create one instance of this class for
the whole application, the first time somebody asks for it, and hand that same instance to everybody
afterwards. That single shared instance is exactly what we need.

(We named it `booking-service` rather than `booking` on purpose. The CLI would happily have made a
class called `Booking` — the current Angular style guide drops the suffix — but while you're learning,
having `Service` in the name makes it obvious which files are services and which are components.)

### Step 3 — Move the state into the service

Cut the state and the two methods out of `booking-panel.ts` and paste them into the service. In
`src/app/booking-service.ts`:

```ts
import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  capacity = 8;
  seatsBooked = signal(0);

  book() {
    this.seatsBooked.update((n) => Math.min(n + 1, this.capacity));
  }

  cancel() {
    this.seatsBooked.update((n) => Math.max(n - 1, 0));
  }
}
```

That's the entire service. It's the same signal and the same two methods we already had — they've just
moved somewhere both components can reach.

Worth pausing on: signals work in services exactly as they do in components. There's nothing
component-specific about them, which is why this move costs us nothing.

### Step 4 — Inject the service into the booking panel

Now rewrite `src/app/booking-panel/booking-panel.ts` to use the service instead of its own state:

```ts
import { Component, inject } from '@angular/core';
import { BookingService } from '../booking-service';

@Component({
  selector: 'app-booking-panel',
  imports: [],
  templateUrl: './booking-panel.html',
  styleUrl: './booking-panel.css',
})
export class BookingPanel {
  bookings = inject(BookingService);
}
```

The class is now almost empty, which is the sign we're doing this right — the component's job is to
render and handle clicks, not to own data.

`inject(BookingService)` asks Angular's injector for the instance. Because the service is
`providedIn: 'root'`, Angular either creates it (first time) or returns the one it already made.

Two rules about `inject()` that will save you time later:

- It goes in a **field initialiser** or a constructor, not inside a method. It only works during
  construction, and calling it later throws.
- Notice that `BookingService` is **not** in the component's `imports` array. That array is for
  templates — components, directives, and pipes. Services aren't used in templates, so they don't
  belong there. Putting one in is a common early mistake.

Update `src/app/booking-panel/booking-panel.html` to go through the service:

```html
<h2>Book a seat</h2>

<p>You have booked {{ bookings.seatsBooked() }} of {{ bookings.capacity }} seats.</p>

<button (click)="bookings.book()" [disabled]="bookings.seatsBooked() === bookings.capacity">
  Book a seat
</button>
<button (click)="bookings.cancel()" [disabled]="bookings.seatsBooked() === 0">Cancel a seat</button>
```

Still a signal, so `seatsBooked()` still needs its parentheses. `capacity` is a plain number, so it
doesn't.

Save. The panel works exactly as before. The capacity display still doesn't — one more step.

### Step 5 — Inject the same service into the sibling

In `src/app/capacity-display/capacity-display.ts`:

```ts
import { Component, computed, inject } from '@angular/core';
import { BookingService } from '../booking-service';

@Component({
  selector: 'app-capacity-display',
  imports: [],
  templateUrl: './capacity-display.html',
  styleUrl: './capacity-display.css',
})
export class CapacityDisplay {
  bookings = inject(BookingService);

  seatsRemaining = computed(() => this.bookings.capacity - this.bookings.seatsBooked());
}
```

The `computed` stays on the component, and that's a deliberate choice. `seatsRemaining` is this
component's way of presenting the shared data — nobody else needs it. Shared state goes in the
service; a view's own derived values can stay in the view.

And `src/app/capacity-display/capacity-display.html`:

```html
<h2>Room capacity</h2>

<p>{{ bookings.seatsBooked() }} booked, {{ seatsRemaining() }} still free.</p>
```

Save and click **Book a seat**. Both components move together now.

Nothing connects them except the fact that they asked for the same service and Angular handed them the
same object. The panel calls `.update()` on the signal; the display reads that signal; Angular
re-renders the display. Neither component knows the other exists.

### Step 6 — Prove it's one instance

Worth confirming rather than taking on faith. Add a constructor log to the service, temporarily:

```ts
  constructor() {
    console.log('BookingService created');
  }
```

Reload the page with the browser console open. Two components injected the service, so if each got its
own instance you'd see the message twice. You'll see it **once**.

Delete the constructor when you've seen it.

## Exercises

### Exercise 1 — A third consumer, and a service method

Generate a component called `booking-status` and place it in `App` alongside the other two.

It should inject the same `BookingService` and display a short message describing the room's state:
"Room empty", "Seats available", or "Room full". Work that out with a `computed` on the component.

Then add a `clearAll()` method to the **service** that resets `seatsBooked` to zero, and give the new
component a button that calls it. Confirm that clicking it zeroes out the numbers in all three
components at once.

### Exercise 2 — A second, unrelated service

Services aren't only for sharing between components — they're also just a good place to put logic that
isn't about rendering.

Generate a `waitlist-service` that holds a signal for the number of people waiting, plus methods to
add and remove one. Inject it into the booking panel *alongside* `BookingService`, and show the
waitlist count there with buttons to change it.

A component can inject as many services as it needs; each is its own `inject()` call.

### Exercise 3 — Break it on purpose

Open `booking-service.ts` and change the decorator to remove the `providedIn` option entirely:

```ts
@Injectable()
```

Reload and read the error in the browser console carefully. Then, instead of putting `providedIn` back,
get the app working again by listing the service in `providers` on **each** component's `@Component`
decorator:

```ts
  providers: [BookingService],
```

It compiles and it runs — but click **Book a seat** and watch what happens to the capacity display.

Explain to yourself what changed. Then put `providedIn: 'root'` back and remove the `providers` arrays.
The lesson here is that *where* a service is provided decides how many instances exist, and "root" is
the one that means "exactly one, shared by everybody."

## Check your work

### After the guided walkthrough

With `ng serve` running, <http://localhost:4200> should show:

- A "Study Room 204" heading, a **Book a seat** section, and a **Room capacity** section.
- Clicking **Book a seat** once changes the panel to **You have booked 1 of 8 seats.** *and* the
  display to **1 booked, 7 still free.** Both move on every click.
- Clicking **Cancel a seat** moves both back down.
- The Book button greys out at 8 seats; the Cancel button greys out at 0.
- `booking-panel.ts` contains no `signal(` call at all — its only member is the `inject()` line.
- Neither component lists `BookingService` in its `imports` array.

If the two components still disagree, one of them is reading its own leftover `seatsBooked` signal
instead of the service's — check that you deleted the old declarations.

### After the exercises

- The status component's message changes on its own as seats are booked, and its **Clear** button
  resets all three components together.
- The waitlist count changes independently of the seat count — proving the two services hold separate
  state even though one component injects both.
- In Exercise 3, with `providers: [BookingService]` on each component, the two components stopped
  agreeing again. Each component-level `providers` entry creates a **new instance** for that component,
  so you were right back where the lab started. With `providedIn: 'root'` restored, they agree again.
