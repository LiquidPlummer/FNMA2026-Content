# Lab — The Event Path Visualizer

We'll build a page that shows us exactly where a DOM event goes. Three nested elements, a listener on every one of them, and a log panel that prints each stop the event makes on its way through.

Along the way we'll write listeners of several kinds, attach them with different options, take them off again, and interrupt an event partway through its journey.

## Prerequisites

| Software | Required Version |
|---|---|
| Web browser | Any current Chrome, Edge, Firefox, or Safari |
| Node.js | not needed |

No server, no build step, no dependencies. Open `src/index.html` directly in a browser — double-clicking it works.

Keep the file open in an editor beside the browser. **Reload the page after every change.**

---

## Guided Walkthrough

### 1. Look around first

Open `src/index.html`. There are three nested elements — `#outer` wraps `#middle` wraps the `#inner` button — and an empty log panel.

Now open `src/main.js`. We already have references to each element and a `log()` helper that appends a line to that panel. There's also one listener already written, on the **Clear log** button:

```js
document.getElementById("clear").addEventListener("click", () => {
  logEl.replaceChildren();
});
```

*The shape of every listener we're about to write: an element, an event type, and a function to run.*

Click **Clear log**. Nothing visible happens yet — but that listener is firing.

### 2. Our first listener, and what it receives

Let's listen on the inner button and look at what the browser hands our function.

```js
inner.addEventListener("click", (event) => {
  console.log(event);
});
```

*Add this at the bottom of `main.js`. The parameter is the event object — the browser passes it in automatically.*

Reload, click `#inner`, and expand the object in the console. It's large. Four properties carry most of what we need:

```js
inner.addEventListener("click", (event) => {
  log(`${event.type} | currentTarget: ${event.currentTarget.id} | target: ${event.target.id} | phase: ${event.eventPhase}`);
});
```

*Replace the previous listener with this one. `currentTarget` is the element we attached to; `target` is what was actually clicked.*

Click the button. One line appears. So far `target` and `currentTarget` are the same element — that changes shortly.

### 3. A describe helper, and listeners everywhere

We're about to attach a lot of listeners, so let's write the formatting once.

```js
const PHASES = { 1: "CAPTURE", 2: "TARGET ", 3: "BUBBLE " };

function describe(event) {
  return `${event.eventPhase} ${PHASES[event.eventPhase]}  on ${event.currentTarget.id.padEnd(7)} target: ${event.target.id}`;
}
```

*Turns any event into one aligned log line. `eventPhase` is a number — the lookup table gives it a readable name.*

Now attach a listener to all three elements, replacing the single one from step 2:

```js
for (const el of [outer, middle, inner]) {
  el.addEventListener("click", (event) => log(describe(event)));
}
```

*One listener per element, all doing the same thing. A `for` loop over the elements saves writing it three times.*

Reload and click `#inner`. **Three** lines, innermost first:

```
2 TARGET   on inner   target: inner
3 BUBBLE   on middle  target: inner
3 BUBBLE   on outer   target: inner
```

*We only clicked one element, but three listeners ran. `target` stays `inner` on every line while `currentTarget` changes.*

That's bubbling. The event reached the button, then travelled outward through its ancestors.

### 4. Turn on capture

`addEventListener` takes a third argument. Passing `true` asks for the **capturing** phase instead of the bubbling one.

**Before typing this, predict what the log will show.** How many lines, and in what order?

```js
for (const el of [outer, middle, inner]) {
  el.addEventListener("click", (event) => log(describe(event)), true);   // capture
  el.addEventListener("click", (event) => log(describe(event)));         // bubble
}
```

*Replace the loop from step 3. Each element now has two listeners — one for each phase.*

Reload and click `#inner`:

```
1 CAPTURE  on outer   target: inner
1 CAPTURE  on middle  target: inner
2 TARGET   on inner   target: inner
2 TARGET   on inner   target: inner
3 BUBBLE   on middle  target: inner
3 BUBBLE   on outer   target: inner
```

*Six lines. The event travels down from the outermost ancestor, reaches the target, and travels back up.*

Most people predict only the bottom half. The event was already moving through `#outer` and `#middle` before the button ever heard about it.

### 5. Reading the phases

The numbers are the browser's own constants:

| `eventPhase` | Meaning |
|---|---|
| 1 | capturing — heading down toward the target |
| 2 | at the target itself |
| 3 | bubbling — heading back up |

Look again at the two identical `TARGET` lines. Both of our listeners on `#inner` fired, and **both report phase 2** — at the target, the capture/bubble distinction collapses. The listener registered with `true` still runs first, but nothing in the log tells them apart.

*That ambiguity is deliberate. Exercise 3 asks us to fix it.*

### 6. Change what we click

Click `#middle` instead of the button — anywhere in its padding, not on the button itself.

```
1 CAPTURE  on outer   target: middle
2 TARGET   on middle  target: middle
2 TARGET   on middle  target: middle
3 BUBBLE   on outer   target: middle
```

*Four lines instead of six. `#inner` is not an ancestor of `#middle`, so it isn't on the path at all.*

The path is always **the chain of ancestors above whatever was clicked** — not every element on the page.

### 7. Options: doing something once

The third argument can be an options object instead of a boolean, which lets us combine settings.

```js
inner.addEventListener("click", () => log("--- this one only fires once ---"), {
  once: true,
});
```

*Add this below the loop. `{ once: true }` runs the listener, then removes it automatically.*

Click three times. The line appears once. The other six lines keep appearing every click.

`{ capture: true }` is the object form of the boolean we passed in step 4 — the two are interchangeable, and the object form is what we reach for when combining options:

```js
outer.addEventListener("click", () => log("--- outer, capture, once ---"), {
  capture: true,
  once: true,
});
```

*Both settings on one listener. This fires during the capture phase, one time only.*

### 8. Taking a listener off

`removeEventListener` needs the same event type, the same phase, and the **same function**.

```js
const noisy = () => log("--- noisy ---");

inner.addEventListener("click", noisy);
inner.removeEventListener("click", noisy);
```

*Add this and click. "noisy" never appears — the listener was attached and removed.*

Now try the version that looks identical but isn't:

```js
inner.addEventListener("click", () => log("--- sneaky ---"));
inner.removeEventListener("click", () => log("--- sneaky ---"));
```

*Character-for-character the same text. Click anyway.*

"sneaky" still appears on every click. Those are two separate function objects that happen to have the same source — `removeEventListener` matches by identity, not by what the code looks like. **To remove a listener, we have to keep a reference to it.**

This also means a listener added with `capture: true` can't be removed without passing the same flag. The phase is part of what identifies it.

### 9. Interrupting the path

An event's journey can be cut short. Add two listeners to `#middle`, and have the first one stop the event:

```js
middle.addEventListener("click", (event) => {
  log(">>> middle stops propagation here");
  event.stopPropagation();
});
middle.addEventListener("click", () => log(">>> middle's second listener"));
```

*Add these at the bottom, then click `#inner`.*

The log stops climbing — nothing from `#outer` in the bubble phase. But `middle`'s **second** listener still ran. `stopPropagation` prevents the event from moving to the *next element*; it doesn't touch other listeners on the element we're already on.

Swap it for the stronger version:

```js
event.stopImmediatePropagation();
```

*Change the one call in the listener above. Now `middle`'s second listener doesn't run either.*

Worth noting for real code: both of these are blunt. Any listener further along the path — including ones written by someone else, or by a library — silently stops receiving the event.

---

## Exercises

No step-by-step from here.

**1. An event that doesn't bubble.** The page has a `#field` input. Attach listeners to `#outer` for `focus` and for `focusin`, then click into the field. Only one of them fires. Work out why, then find the setting that lets `#outer` observe the other one after all. (`blur` and `focusout` are the matching pair — check whether they behave the same way.)

**2. Other event types.** Wire up `dblclick`, `mouseover`, and `keydown` using the same `describe` helper. Which of them produce a full path through all three elements, and which barely register? Log enough to answer it from the panel rather than from guessing.

**3. Tell the two TARGET lines apart.** In step 5, the two phase-2 lines were indistinguishable. Change the listeners so the log makes clear which was registered for capture and which for bubble — without changing what the browser does.

**4. One listener instead of six.** Remove the per-element listeners entirely. Attach a single listener to `#outer` that still reports which element was clicked. Then add a new element inside `#middle` from the console after the page has loaded, click it, and confirm it's reported too — with no new listener attached to it.
