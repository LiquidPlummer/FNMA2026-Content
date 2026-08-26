# Guidance: Building the Catalog SPA

Read this if you're stuck or want a sense of the shape before you start. It describes an approach — not the only one, and there's no code here on purpose. Try it your way first.

## Start With the Data

Before you write anything that touches the page, write your array. Six objects, same keys on each. Decide which keys show up on a catalog card and which are detail-only. Give every item something unique you can identify it by — an `id`.

Everything downstream gets easier when the data is settled first.

## One Place Where Content Lives

Put a single empty container element in your HTML and hand its reference to a variable at the top of your script. Every view you build gets attached inside it, and every time you navigate, that container is what gets emptied.

Having exactly one mount point is what keeps this manageable. Two or three containers and you'll lose track of what's on screen.

## A Function Per View

Think of each view as a function whose whole job is to build a chunk of DOM and put it in the container. One builds the catalog list, one builds a detail view for a given item. The detail one needs to know *which* item — that's an argument.

Somewhere above them, a small function that clears the container and then calls the right builder. That's your "navigation."

## Clearing Before You Draw

If you don't remove the old view first, the new one appends underneath it. Look up how to empty an element of its children. There's more than one way — one of them is off-limits under the assignment rules, which should narrow it down.

Do the clearing in one place, not in every view function. If you find yourself clearing in three spots, that's a sign the clearing belongs one level up.

## Getting From a Click to an Item

This is the part that trips people up.

When a card is clicked, you have an element. You need the object. The bridge between them is an identifier you stashed on the element when you built it — think about `data-*` attributes and how you read them back.

Two more things to work out:

- The thing that was actually clicked might be the card, or it might be text *inside* the card. You need to get from wherever the click landed up to the card itself. There's a method for exactly this.
- Once you have the id, you need to find the matching object in your array. That's an array method, not a DOM method.

## Where to Put Your Listeners

You have two options and both work:

- Attach a listener to each card as you build it
- Attach one listener to the container and figure out what was clicked from the event

The first is more obvious. The second is fewer listeners and doesn't need re-wiring every time you re-render. If you go with the first, remember that when you clear the container you destroy those elements and their listeners along with them — so listeners have to be attached during the build, every time.

## Build Detached, Then Attach

Create your element, set its text, add its classes, set its data attributes — *then* put it in the page. Appending first and configuring after works, but the habit of finishing an element before attaching it will serve you well.

## Common Snags

**Content stacks up instead of replacing.** You're not clearing, or you're clearing the wrong element.

**Clicking a card does nothing, but only sometimes.** You clicked a child element and your handler is looking at the wrong target.

**Back works once, then stops.** Your back button's listener was attached to an element that got destroyed on the last re-render.

**Everything shows the same item's details.** You captured a variable from the loop in a way that doesn't hold per-iteration, or you're reading the id from the wrong place.

**Nothing appears at all.** Check the console. Then check that your script runs after the container exists in the document.

## If You Want a Starting Order

1. Get the data array in place and log it
2. Render the catalog list — no clicking yet, just get the cards on screen
3. Make one card, any card, log its id when clicked
4. Make the click render a detail view
5. Add back
6. Click around until you can't break it