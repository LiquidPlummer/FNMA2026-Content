# Lab 8 — Lifecycle Hooks

## Objective

Components are born, they run, and they get thrown away. Angular lets you hook into those moments by
implementing specially named methods on the class.

We'll cover the three you'll use most:

- **`ngOnInit`** — runs once, after Angular has set the component's inputs. The normal place to kick
  off work.
- **`ngOnChanges`** — runs every time an input value changes, and gets a `SimpleChanges` object
  describing what changed.
- **`ngOnDestroy`** — runs just before the component is removed. The place to clean up anything that
  would otherwise keep running.

We'll log all of them to the console so you can watch the order they fire in, then use `ngOnInit` and
`ngOnDestroy` for a real job: starting a `setInterval` and making sure it stops.

There are other hooks — `ngAfterViewInit`, `ngAfterContentInit`, `ngDoCheck` among them. They exist,
they have their uses, and they're out of scope here. The three above cover the overwhelming majority
of real code.

### Where we're starting from

This project ships with two components and no hooks in either of them:

- `session-monitor` — the **parent**. Holds the current room name, with a button to switch rooms and a
  button to show or hide the timer.
- `session-timer` — the **child**. Takes the room name as an input and displays an elapsed-seconds
  counter that currently never moves.

Note that the child uses the decorator form, `@Input() roomName`, rather than the signal `input()`
from Lab 4. That's deliberate: `ngOnChanges` and `SimpleChanges` are the decorator-era tool for
reacting to input changes, and they're clearest when the input is declared the decorator way.

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

Then open <http://localhost:4200> **with your browser's developer console open**. You'll be reading
that console for the whole lab — keep it visible next to the page.

## Guided walkthrough

### Step 1 — The constructor is not a lifecycle hook

Before adding hooks, let's establish the baseline. Add a constructor to
`src/app/session-timer/session-timer.ts`:

```ts
  constructor() {
    console.log('SessionTimer: constructor');
  }
```

Save and reload. One message appears.

The constructor runs when the object is created, which is *before* Angular has done anything Angular-y
to it — inputs aren't set yet, and the template doesn't exist. That's why we don't put startup logic
here. It's for dependency injection (`inject()`, from Lab 5) and nothing else.

### Step 2 — `ngOnInit`

Add the hook, and declare that the class implements it:

```ts
import { Component, Input, OnInit, signal } from '@angular/core';
```

```ts
export class SessionTimer implements OnInit {
```

```ts
  ngOnInit() {
    console.log('SessionTimer: ngOnInit');
  }
```

Save and reload. Now you get both messages, constructor first.

Two notes on `implements OnInit`. It's optional — Angular finds the method by name whether or not you
declare the interface. But declaring it means TypeScript will catch you if you typo `ngOnint`, which
is a bug that produces no error and no behaviour, just silence. Always declare it.

`ngOnInit` runs **once**, after Angular has set every input on the component. That last part is what
makes it the right place for startup work: unlike in the constructor, by the time `ngOnInit` runs,
`this.roomName` actually has a value. Try logging it from both places and compare.

### Step 3 — `ngOnChanges` and `SimpleChanges`

Add the third method:

```ts
import { Component, Input, OnChanges, OnInit, SimpleChanges, signal } from '@angular/core';
```

```ts
export class SessionTimer implements OnChanges, OnInit {
```

```ts
  ngOnChanges(changes: SimpleChanges) {
    const roomChange = changes['roomName'];
    console.log(
      `SessionTimer: ngOnChanges — firstChange=${roomChange.firstChange}, ` +
        `"${roomChange.previousValue}" -> "${roomChange.currentValue}"`,
    );
  }
```

Save and reload, then read the console order carefully:

```
SessionTimer: constructor
SessionTimer: ngOnChanges — firstChange=true, "undefined" -> "Study Room 204"
SessionTimer: ngOnInit
```

`ngOnChanges` fires **before** `ngOnInit`, which surprises most people. It makes sense once you say it
out loud: Angular sets the inputs, and setting them *is* a change, so `ngOnChanges` reports it. Only
after all inputs are in place does `ngOnInit` run.

Now click **Switch room**. You get another `ngOnChanges` — this time with `firstChange=false` and a
real `previousValue`. `ngOnInit` does not fire again, because the component wasn't re-created.

About the `changes` parameter:

- It's an object keyed by **input name**, so `changes['roomName']` is the entry for our one input. If
  a component has several inputs, only the ones that actually changed appear as keys.
- Each entry is a `SimpleChange` with three members: `previousValue`, `currentValue`, and
  `firstChange`.
- We use bracket notation rather than `changes.roomName` because `SimpleChanges` is typed with an
  index signature, and this project's `tsconfig.json` enables
  `noPropertyAccessFromIndexSignature` — which requires brackets for exactly that case.

Let's make the hook do something real. When the room changes — but not on the first run — reset the
counter:

```ts
    if (!roomChange.firstChange) {
      this.secondsElapsed.set(0);
    }
```

Put that at the bottom of `ngOnChanges`. The `firstChange` guard is the standard shape here: on the
very first call there's no previous state to react to, so you almost always want to skip it.

### Step 4 — Start something in `ngOnInit`

The elapsed counter has never moved, because nothing increments it. `ngOnInit` is where that starts.

Replace the body of `ngOnInit`:

```ts
  ngOnInit() {
    console.log('SessionTimer: ngOnInit');
    this.intervalId = setInterval(() => {
      this.secondsElapsed.update((n) => n + 1);
      console.log('SessionTimer: tick');
    }, 1000);
  }
```

And add a field to hold the interval's id, above the constructor:

```ts
  private intervalId = 0;
```

`setInterval` returns a handle we'll need later in order to stop it. Storing it on the class is the
whole reason this works.

Save and reload. The counter climbs once a second, and the console fills with `tick`. Click **Switch
room** and the counter resets to zero, courtesy of the `ngOnChanges` we wrote in Step 3.

### Step 5 — Watch it leak

Now click **Show / hide timer** to hide the component.

The timer disappears from the page. Keep watching the console.

`tick` is still logging. Once a second. Forever.

Angular removed the component from the DOM, but `setInterval` isn't part of Angular — it's a browser
timer, and nothing told the browser to stop it. The component object can't even be garbage collected,
because the callback still holds a reference to it.

Click **Show / hide timer** a few more times and watch the ticks pile up: each time the component is
re-created, `ngOnInit` starts *another* interval, and none of the old ones ever stop.

This is a real memory leak, and it's the single most common one in Angular applications. Intervals,
timeouts, `addEventListener` calls, WebSocket connections, and manual subscriptions all behave the
same way.

### Step 6 — Clean up in `ngOnDestroy`

The fix is one method:

```ts
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, signal } from '@angular/core';
```

```ts
export class SessionTimer implements OnChanges, OnInit, OnDestroy {
```

```ts
  ngOnDestroy() {
    console.log('SessionTimer: ngOnDestroy');
    clearInterval(this.intervalId);
  }
```

Save and reload — a full reload, so the leaked intervals from before are gone.

Now click **Show / hide timer**. You'll see `ngOnDestroy` in the console, and the ticks stop. Show it
again and you get a fresh `constructor` → `ngOnChanges` → `ngOnInit` sequence, with the counter back
at zero because it's a genuinely new component instance.

The rule to carry out of this lab: **anything you start in `ngOnInit`, you stop in `ngOnDestroy`.**
Treat them as a matched pair.

### Step 7 — Hook order across parent and child

One more thing to observe. Add a constructor and the same three logs to the **parent**, in
`src/app/session-monitor/session-monitor.ts`:

```ts
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
```

```ts
export class SessionMonitor implements OnInit, OnDestroy {
```

```ts
  constructor() {
    console.log('SessionMonitor: constructor');
  }

  ngOnInit() {
    console.log('SessionMonitor: ngOnInit');
  }

  ngOnDestroy() {
    console.log('SessionMonitor: ngOnDestroy');
  }
```

The parent has no inputs, so there's no `ngOnChanges` to add.

Save and reload. Read the full sequence:

```
SessionMonitor: constructor
SessionMonitor: ngOnInit
SessionTimer: constructor
SessionTimer: ngOnChanges — firstChange=true, ...
SessionTimer: ngOnInit
```

Initialisation runs **top-down**: a parent is fully initialised before its children are even created.
That's necessary, since the parent has to exist before it can supply the inputs the child needs.

Destruction runs the other way — children are destroyed before their parents. You won't see the
parent's `ngOnDestroy` in this app, because `SessionMonitor` lives as long as the page does. That's
normal; plenty of components never get destroyed at all.

## Exercises

### Exercise 1 — A different resource to clean up

`setInterval` isn't the only thing that outlives a component. Add a window event listener and clean it
up properly.

In `session-timer`, in `ngOnInit`, call `window.addEventListener('resize', ...)` with a handler that
increments a `resizeCount` signal you display on the card. Then remove the listener in `ngOnDestroy`.

To prove it works: resize the browser window a few times and watch the count. Then hide the timer,
resize again, show it, and confirm the count is back at zero and climbing normally. If you got the
cleanup wrong, you'll see the console errors that a stale handler produces.

Note that `removeEventListener` needs *the same function reference* you passed to `addEventListener`,
which means an inline arrow function won't do. Work out where to keep it.

### Exercise 2 — `ngOnChanges` with two inputs

Add a second input to the timer, `sessionLabel`, and pass it from the parent with its own button that
changes it.

Then log the **whole** `changes` object rather than one key — `console.log(changes)` — and click each
button in turn. Note which keys are present each time.

Use that to write a `ngOnChanges` that reacts differently depending on which input changed: reset the
counter when the room changes, but leave it alone when only the label changes. You'll need to check
whether a key exists before reading it.

### Exercise 3 — Prove `ngOnInit` isn't the constructor

Generate a new component, `session-banner`, with a single `@Input() title` that it displays.

Inside it, log `this.title` from **both** the constructor and `ngOnInit`. Place it in the parent with
the room name bound to its input, reload, and compare the two logged values.

Then write a one-line comment in the file explaining, in your own words, why they differ — and why
that makes the constructor a bad place to do anything that depends on an input.

## Check your work

### After the guided walkthrough

Reload <http://localhost:4200> with the console open. You should see exactly this sequence, in this
order:

```
SessionMonitor: constructor
SessionMonitor: ngOnInit
SessionTimer: constructor
SessionTimer: ngOnChanges — firstChange=true, "undefined" -> "Study Room 204"
SessionTimer: ngOnInit
```

followed by a `tick` roughly once a second, with the on-screen counter climbing to match.

Then:

- **Switch room** — the heading and the timer's title change to *Study Room 118*, the counter resets to
  0, and the console shows one `ngOnChanges` with `firstChange=false` and the previous room as
  `previousValue`. There is **no** new `constructor` or `ngOnInit`.
- **Show / hide timer** (hiding) — the console shows `SessionTimer: ngOnDestroy` and the ticks **stop
  completely**. If ticks keep coming, `clearInterval` isn't running or isn't getting the right id.
- **Show / hide timer** (showing again) — a fresh `constructor` → `ngOnChanges` → `ngOnInit`, counter
  starting from 0, and exactly **one** tick per second. If you see two or three ticks per second, an
  old interval survived.

Hide and show the timer five times, then leave it hidden for ten seconds. The console should be
completely silent.

### After the exercises

- Resizing the window moves the resize count; hiding the timer and resizing produces no console errors;
  showing it again starts the count from zero.
- With two inputs, clicking the room button and the label button produce `changes` objects with
  different keys in them, and only the room change resets the counter.
- In the banner component, the constructor logs `undefined` and `ngOnInit` logs the actual room name.
