# Assignment: Build a Catalog SPA

## The Idea

Build a single-page application using nothing but HTML and JavaScript. The page never reloads and never navigates to another file — everything the user sees is put there, and taken away, by your JavaScript.

Your app is a **catalog**. What it catalogs is up to you: video games, movies, albums, sneakers, houseplants, hot sauces, national parks. Pick something you'll enjoy typing data for.

## What It Has To Do

**Catalog view** — a list of your items. Six or so is plenty. Each one should show enough to be recognizable, not everything you know about it.

**Detail view** — clicking an item takes you to a view showing that item's full information.

**Back** — some control on the detail view returns you to the catalog.

That's the whole app. If you get there and want more, see Going Further.

## The Rules

1. **One HTML file, one JS file.** No frameworks, no libraries, no build step.
2. **The HTML file is nearly empty.** A header, a container element for your content, a script tag. That's it. If you can read your catalog's contents by opening the HTML file in a text editor, you've done it wrong.
3. **Your data lives in JavaScript** as an array of objects. Every item on screen is built from that array.
4. **No `innerHTML`.** Build your elements with `createElement`, fill them with `textContent`, and attach them. This is the constraint that makes the assignment worth doing — without it you're writing HTML in strings and never touching the DOM API.
5. **Navigation is synthetic.** Your links and buttons don't go anywhere. They call a function that removes what's on screen and puts something else there.

## What You're Demonstrating

Everything from today, in one place:

- **Selecting** — finding your container, finding what was clicked
- **Manipulating** — creating elements, setting their content, attaching them, removing them
- **Traversing** — getting from a clicked element to the item it represents
- **Structure** — understanding that the page you see and the HTML file on disk are two different things

## Done When

- [ ] Opening the page shows the catalog
- [ ] Clicking any item shows that item's details
- [ ] The catalog is gone from the page while the detail view is up — not hidden, gone
- [ ] Back returns to the catalog
- [ ] You can do this repeatedly without the page breaking or duplicating content
- [ ] Your HTML file contains none of your catalog's actual content

## Going Further

Optional, if you finish early and want to push:

- A nav bar with a third view (About, Favorites, Random Pick)
- Sort or filter the catalog
- A search box that narrows the list as you type
- Nested data — each item has a list of something, rendered as its own loop inside the detail view
- Add-an-item form that pushes to your data array and re-renders