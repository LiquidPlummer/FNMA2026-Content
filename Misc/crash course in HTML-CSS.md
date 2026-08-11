# Intro to HTML, CSS, and JS

A light first pass across the three core web technologies before we get into Angular. CSS won't come up much again after today, and we'll go much deeper on JS in later lessons — this is just enough to build and interact with a simple page.

## HTML: Tags, Attributes, and Nesting

An HTML **tag** marks up a piece of content — most come as a pair, an opening tag and a closing tag, wrapping the content between them: `<p>Some text</p>`. The opening tag, its closing tag, and everything in between together form an **element**.

**Attributes** live inside the opening tag as `name="value"` pairs and configure that element — `id`, `class`, `src`, `href`, and so on. Some attributes are **boolean**: just being present turns them on, no value needed (`<input disabled>`, `<input required>`).

Elements **nest** inside one another to form a tree — a `<div>` can contain a `<p>`, which can contain an `<a>`. Whatever opens last must close first (`<div><p></p></div>`, never `<div><p></div></p>`). This nesting is exactly what the CSS descendant selector (`nav a { }`) walks down.

Not every tag wraps content. **Self-closing (void) elements** — `<img>`, `<br>`, `<hr>`, `<input>`, `<meta>`, `<link>` — have no closing tag and no children, since they represent a single piece of content (an image, a line break) rather than a container.

```html
<div class="card" id="featured">
  <img src="photo.jpg" alt="A photo">
  <p>Caption text</p>
</div>
```
*A `<div>` with attributes, nesting a void `<img>` element and a `<p>` element.*

## HTML: Common Tags

| Tag | Description | Notable Attributes |
| --- | --- | --- |
| `<html>` | Root element wrapping the whole page | `lang` |
| `<head>` | Holds metadata, not visible content | — |
| `<title>` | Sets the browser tab/title text | — |
| `<meta>` | Metadata like charset or viewport (self-closing) | `charset`, `name`, `content` |
| `<body>` | Wraps all visible page content | — |
| `<h1>`–`<h6>` | Headings, `h1` largest/most important | — |
| `<p>` | Paragraph of text | — |
| `<a>` | Hyperlink | `href`, `target` |
| `<img>` | Embeds an image (self-closing) | `src`, `alt` (required for accessibility) |
| `<div>` | Generic block-level container, no meaning of its own | `class`, `id` |
| `<span>` | Generic inline container, no meaning of its own | `class`, `id` |
| `<ul>` / `<ol>` | Unordered / ordered list | — |
| `<li>` | List item, goes inside `<ul>`/`<ol>` | — |
| `<table>` | Table container | — |
| `<tr>` / `<td>` / `<th>` | Table row / data cell / header cell | `colspan`, `rowspan` |
| `<nav>` | Semantic wrapper for navigation links | — |
| `<header>` / `<footer>` | Semantic top/bottom sections of a page or section | — |
| `<section>` / `<article>` | Semantic groupings of related content | — |
| `<form>` | Wraps inputs for submission | `action`, `method` |
| `<input>` | Single-line user input (self-closing) | `type`, `name`, `value`, `required`, `disabled` |
| `<label>` | Caption for a form control | `for` (matches the input's `id`) |
| `<button>` | Clickable button | `type` (`submit`, `button`, `reset`) |
| `<select>` / `<option>` | Dropdown and its choices | `value` (on `<option>`) |
| `<textarea>` | Multi-line text input | `rows`, `cols` |
| `<br>` | Line break (self-closing) | — |
| `<hr>` | Horizontal rule/divider (self-closing) | — |
| `<strong>` / `<em>` | Bold/important text, italic/emphasized text | — |
| `<script>` | Embeds or links JavaScript | `src` |
| `<link>` | Links external resources, e.g. a stylesheet (self-closing) | `rel`, `href` |

*A working vocabulary of tags — enough to build most simple pages without reaching for a reference.*

## HTML: Just Enough to Build a Page

A minimal page structure:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>My Page</title>
  </head>
  <body>
    <h1>Welcome</h1>
    <p>This is a paragraph.</p>
  </body>
</html>
```
*Skeleton every HTML page starts from.*

For navigating around a site, we need anchor tags and a `<nav>` to hold them:

```html
<nav>
  <a href="index.html">Home</a>
  <a href="about.html">About</a>
  <a href="#contact">Contact</a>
</nav>
```
*Links to other pages, and a same-page jump link using `#contact`.*

## HTML: Forms and `action`

A **form** collects input and can submit it as an HTTP request. The `action` attribute sets where the request goes, and `method` sets the HTTP verb (`get` or `post`).

```html
<form action="/submit" method="post">
  <label for="name">Name:</label>
  <input type="text" id="name" name="name">

  <label for="email">Email:</label>
  <input type="email" id="email" name="email">

  <button type="submit">Submit</button>
</form>
```
*Submitting this form sends a POST request to `/submit` with `name` and `email` as form data — no JavaScript required.*

This is worth demoing as-is: submit the form and watch the browser navigate to `/submit`, full page reload included. That full-page reload is exactly what we'll replace with JS later.

## CSS: Selectors

**CSS (Cascading Style Sheets)** controls how HTML elements look. A CSS rule is a **selector** paired with one or more declarations:

```css
selector {
  property: value;
}
```
*Basic anatomy of a CSS rule.*

The main selectors we'll use:

- **Element selector** — targets every instance of a tag, e.g. `p { }` targets all `<p>` elements.
- **Class selector** — targets elements with a given `class` attribute, written with a dot: `.highlight { }`.
- **ID selector** — targets a single element with a given `id`, written with a hash: `#header { }`.
- **Attribute selector** — targets elements by attribute, e.g. `input[type="text"] { }`.
- **Descendant selector** — targets elements nested inside another, e.g. `nav a { }` targets links inside a `<nav>`.
- **Pseudo-class selector** — targets an element in a particular state, e.g. `a:hover { }` or `li:first-child { }`.

```css
p { color: navy; }
.highlight { background-color: yellow; }
#header { font-size: 24px; }
input[type="text"] { border: 1px solid gray; }
nav a { text-decoration: none; }
a:hover { color: red; }
```
*One example of each selector type, with a simple style applied.*

Common style declarations worth knowing early on: `color`, `background-color`, `font-size`, `font-family`, `margin`, `padding`, `border`, and `width`/`height`.

## CSS: Three Ways to Add It to HTML

1. **Inline** — a `style` attribute directly on an element. Highest specificity, hardest to maintain.
   ```html
   <p style="color: blue;">Inline styled text</p>
   ```
2. **Internal (embedded)** — a `<style>` block in the document `<head>`. Scoped to that one page.
   ```html
   <head>
     <style>
       p { color: blue; }
     </style>
   </head>
   ```
3. **External** — a separate `.css` file linked with `<link>`. The standard approach for anything beyond a demo, since one stylesheet can style many pages.
   ```html
   <head>
     <link rel="stylesheet" href="styles.css">
   </head>
   ```
*The three ways to attach CSS to a page, in increasing order of maintainability.*

## CSS: The Box Model

Every HTML element renders as a rectangular box made of four layers, from the inside out: **content** (the text/image itself), **padding** (space between the content and the border), **border** (a line around the padding), and **margin** (space outside the border, separating the element from its neighbors).

```css
.card {
  width: 200px;
  padding: 16px;
  border: 2px solid black;
  margin: 20px;
}
```
*A `.card` whose actual rendered footprint is wider than 200px once padding and border are added — by default, `width` sets only the content layer.*

That default behavior — `width` applying to content only, with padding and border added on top — trips people up constantly, so `box-sizing: border-box` is worth knowing about early: it makes `width` include padding and border, so the box's total size matches the number you set.

```css
.card {
  box-sizing: border-box;
}
```
*With `border-box`, padding and border are carved out of the declared `width` instead of adding to it.*

## JS: A Quick First Look

JavaScript is what makes a page interactive rather than just static text and layout. Two quick ways to see it working: `console.log()` prints a value to the browser's dev tools console, and `alert()` pops up a message box on screen.

```html
<script>
  console.log('Page loaded');
  alert('Hello!');
</script>
```
*`console.log` for output only you (the developer) see; `alert` for output the user sees.*

We can also run code in response to something happening on the page — a **click**, for instance — using an event listener, the same pattern we'll use to replace the form's submit next.

```html
<button id="myButton">Click me</button>
<script>
  document.querySelector('#myButton').addEventListener('click', function () {
    console.log('Button was clicked!');
  });
</script>
```
*Selects the button by its `id`, then runs the function every time it's clicked.*

We'll get much deeper into JS — variables, functions, more event types — in a later lesson. For now, this is enough to make the form example below feel less like magic.

## JS: Replacing the Form with Event Listeners

Instead of letting the form submit and reload the page, we attach a JS **event listener** and handle the request ourselves.

```html
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
<script>
  document.querySelector('form').addEventListener('submit', function (event) {
    event.preventDefault();

    const name = document.querySelector('#name').value;
    const email = document.querySelector('#email').value;

    axios.post('/submit', { name, email })
      .then(response => console.log('Success:', response.data))
      .catch(error => console.error('Error:', error));
  });
</script>
```
*`event.preventDefault()` stops the normal form submission; Axios sends the same data as an async POST request instead, with no page reload.*

This is the pattern we'll build on going into Angular: DOM events trigger JS, and JS talks to the server directly via HTTP calls instead of relying on native form submission.