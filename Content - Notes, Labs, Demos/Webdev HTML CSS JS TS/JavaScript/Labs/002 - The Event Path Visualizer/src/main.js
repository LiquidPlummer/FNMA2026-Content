// main.js — we'll build this up as we go.
//
// Open index.html in a browser. No server needed, no build step.
// Reload the page after every change.

const outer = document.getElementById("outer");
const middle = document.getElementById("middle");
const inner = document.getElementById("inner");
const field = document.getElementById("field");
const logEl = document.getElementById("log");

// Appends one line to the log panel on the right.
function log(text) {
  const li = document.createElement("li");
  li.textContent = text;
  logEl.appendChild(li);
  logEl.scrollTop = logEl.scrollHeight;
}

// One listener already written for us, so we can see the shape before writing our own.
document.getElementById("clear").addEventListener("click", () => {
  logEl.replaceChildren();
});
