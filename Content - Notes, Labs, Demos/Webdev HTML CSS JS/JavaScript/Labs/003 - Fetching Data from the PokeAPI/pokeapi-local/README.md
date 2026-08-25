# PokéAPI Stand-In

A local replacement for `pokeapi.co`, for environments with no outbound internet.

**Dependencies: none.** One file, one import — Node's built-in `node:http`. Nothing to install, which matters here: an environment that can't reach `pokeapi.co` usually can't reach the npm registry either.

## Running it

```bash
node server.mjs
```

*Listens on port 3000. Set `PORT=8080` to change it.*

Check it's up:

```
http://localhost:3000/api/v2/pokemon/ditto
```

## Pointing the lab at it

One line in `src/index.js`:

```js
const BASE = "http://localhost:3000/api/v2";
```

*Everything else in the lab works unchanged — same paths, same response shapes.*

## What it serves

| Route | Returns |
|---|---|
| `/api/v2/pokemon` | paginated list, supports `?limit=` and `?offset=` |
| `/api/v2/pokemon/:name` | one Pokémon — also accepts a numeric id |
| `/api/v2/type` | list of types |
| `/api/v2/type/:name` | one type, with the Pokémon that have it |
| anything else | 404 |

Nine Pokémon across nine types, including two dual-typed ones so `types.map()` has more than one entry to chew on.

Two details that exist on purpose:

**The 404 body is plain text, not JSON.** That's what the real API does, and it's what makes an unchecked `res.json()` fail with a confusing `SyntaxError` — the centerpiece of the lab. Returning a JSON error here would quietly delete that lesson.

**Every `url` in a response points back at this server**, rebuilt from the incoming `Host` header. So the exercise that follows links from the list endpoint works on any port, and on any machine.

CORS is wide open, so a browser page can call it too.

## On the data

Response *shapes* mirror the live API and were verified against it. Ids, names, and types are the real ones. **Stat numbers are illustrative fixtures** — near enough for the lab, not a mirror of the real dataset. Don't use this to settle an argument about base stats.
