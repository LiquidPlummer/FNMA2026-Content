# Lab — Fetching Data from the PokéAPI

We'll make real HTTP requests against a live API and turn the responses into something usable. Along the way we'll cover `fetch`, reading a JSON body, the error case that almost everyone gets wrong the first time, and how `async`/`await` relates to the `.then()` chains underneath it.

The API is [PokéAPI](https://pokeapi.co) — free, public, no key, no sign-up.

## Prerequisites

| Software | Required Version |
|---|---|
| Node.js | 24.19.0 LTS (18.0.0 or newer required — `fetch` is not built in before 18) |

No dependencies to install. From the lab directory:

```bash
node src/index.js
```

*Runs whatever is currently in `src/index.js`. `npm start` does the same thing.*

Open `src/index.js` — there's one line in it, the base URL. Everything else we add ourselves.

---

## Guided Walkthrough

### 1. Make the request and look at what comes back

Let's ask the API for a single Pokémon and log exactly what `fetch` hands us.

```js
const res = await fetch(`${BASE}/pokemon/ditto`);
console.log(res);
```

*Add this below the `BASE` line. Note the `await` — we can use it at the top level because this file is an ES module.*

Run it. We do **not** get Pokémon data. We get a `Response` object — status, headers, and a body that hasn't been read yet.

Two properties on it are worth knowing by name:

```js
console.log(res.status);   // 200
console.log(res.ok);       // true  — shorthand for "status is in the 200s"
```

*`res.ok` is the check we'll build our error handling around later.*

### 2. Read the body

The response body arrives as a stream, and turning it into an object is itself an asynchronous operation. That means a second `await`.

```js
const data = await res.json();
console.log(data.name, data.id);   // ditto 132
```

*`res.json()` returns a promise of its own — this trips up almost everyone once. Two awaits, not one.*

### 3. Pick out the parts we actually want

The response is large. Let's look at how the interesting fields are nested.

```js
console.log(data.height, data.weight, data.base_experience);
console.log(data.types.map((t) => t.type.name));
console.log(data.stats.map((s) => `${s.stat.name}: ${s.base_stat}`));
console.log(data.sprites.front_default);
```

*Types and stats are arrays of wrapper objects — the name we want is one level down, at `t.type.name` and `s.stat.name`.*

That nesting is normal for REST APIs. The wrapper exists so each entry can carry a `url` pointing at its own full resource, which we'll use in the exercises.

### 4. Wrap it in a function

Right now everything runs once, at the top level. Let's make it reusable and have it return a shape we control rather than the API's shape.

```js
async function getPokemon(name) {
  const res = await fetch(`${BASE}/pokemon/${name}`);
  const data = await res.json();
  return {
    name: data.name,
    id: data.id,
    types: data.types.map((t) => t.type.name),
    sprite: data.sprites.front_default,
  };
}

console.log(await getPokemon("pikachu"));
```

*Replace the loose lines from steps 1–3 with this. Returning our own object shape means the rest of our code doesn't depend on PokéAPI's field layout.*

### 5. The error nobody handles

Let's ask for something that doesn't exist.

```js
console.log(await getPokemon("notarealpokemon"));
```

*Try to predict what happens before running it. Does `fetch` throw? Does it return `null`?*

It does neither. We get a `SyntaxError` from `res.json()`, pointing at a line that looks perfectly fine.

Here's why:

```js
const res = await fetch(`${BASE}/pokemon/notarealpokemon`);
console.log(res.ok);      // false
console.log(res.status);  // 404
```

*`fetch` resolved successfully. A 404 is a perfectly good HTTP response — the request worked, the server just said no.*

**`fetch` only rejects when the request never completed at all** — no network, DNS failure, CORS. Any status the server actually returns, including 404 and 500, comes back as a resolved response with `ok` set to `false`.

So our function happily passed a "Not Found" error page into `res.json()`, which choked on text that isn't JSON. The error we saw was about *parsing*, three steps downstream of the real problem.

Let's fix it by checking `res.ok` before reading the body:

```js
async function getPokemon(name) {
  const res = await fetch(`${BASE}/pokemon/${name}`);
  if (!res.ok) throw new Error(`HTTP ${res.status} while looking up "${name}"`);
  const data = await res.json();
  return {
    name: data.name,
    id: data.id,
    types: data.types.map((t) => t.type.name),
    sprite: data.sprites.front_default,
  };
}
```

*One line added. Now the failure names the real problem instead of surfacing as a parse error.*

And because it throws, we can catch it normally:

```js
try {
  await getPokemon("notarealpokemon");
} catch (err) {
  console.log("Caught:", err.message);
}
```

*`try`/`catch` around `await` works exactly like it does with synchronous code.*

### 6. Asking for several at once

Let's fetch three.

```js
const names = ["ditto", "pikachu", "snorlax"];

for (const n of names) {
  console.log(await getPokemon(n));
}
```

*This works, but each request waits for the one before it to finish — even though none of them depend on each other.*

`Promise.all` starts them all and waits for the set:

```js
const all = await Promise.all(names.map(getPokemon));
console.log(all.map((p) => `${p.name} (${p.types.join("/")})`));
```

*The requests overlap instead of queueing. Watch the difference — it's noticeable even with three.*

One catch: if any single request rejects, `Promise.all` rejects and we lose the successful ones too. Add a bad name to the list and see for ourselves.

### 7. What `async`/`await` is hiding

`async`/`await` is syntax over promises. Nothing new underneath. Here's step 5's function written the older way:

```js
fetch(`${BASE}/pokemon/ditto`)
  .then((res) => {
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  })
  .then((data) => console.log(data.name))
  .catch((err) => console.log("Caught:", err.message));
```

*The same three steps: check the response, read the body, handle failure. Each `await` we wrote was a `.then()`, and our `try`/`catch` was a `.catch()`.*

Worth reading both versions side by side once. We'll see `.then()` chains in existing code for a long time yet, and recognizing that they're the same thing makes them much less intimidating.

---

## Exercises

No step-by-step from here. The API docs are at [pokeapi.co/docs/v2](https://pokeapi.co/docs/v2) — reading unfamiliar API docs is part of the exercise.

**1. A different resource.** Fetch `/type/electric` and print the names of every Pokémon of that type. Each name sits inside a wrapper object, the same way types and stats did — work out the exact path yourself before reaching for the docs.

**2. Follow the links.** Fetch `/pokemon?limit=10`. Each entry in `results` has a `url` pointing at its own full record. Use those URLs to fetch all ten and print each one's types. This is a two-level fetch: get a list, then get each item.

**3. Stop asking twice.** Add a cache so requesting the same Pokémon a second time returns the stored result without hitting the network. Prove it works — the cached call should be observably different from the first one. Then consider: what should happen if the first request failed? Should the failure be cached too?
