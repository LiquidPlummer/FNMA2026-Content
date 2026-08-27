// server.mjs — a tiny stand-in for pokeapi.co, for environments with no internet.
//
//   node server.mjs                 -> http://localhost:3000
//   PORT=8080 node server.mjs       -> pick a different port
//
// No dependencies. Everything below is Node's built-in http module.

import { createServer } from "node:http";

const PORT = Number(process.env.PORT ?? 3000);

// --- data ------------------------------------------------------------
// Shapes mirror the real PokéAPI. Ids, names and types are the real ones;
// stat numbers are illustrative fixtures, not a mirror of the live data.

const sprite = (id) =>
  `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${id}.png`;

function mon(id, name, types, height, weight, baseExp, abilities, stats) {
  return {
    id, name, height, weight,
    base_experience: baseExp,
    types: types.map((t, i) => ({ slot: i + 1, type: { name: t, url: `/api/v2/type/${t}` } })),
    abilities: abilities.map((a, i) => ({
      is_hidden: i === abilities.length - 1 && abilities.length > 1,
      slot: i + 1,
      ability: { name: a, url: `/api/v2/ability/${a}` },
    })),
    stats: ["hp", "attack", "defense", "special-attack", "special-defense", "speed"]
      .map((s, i) => ({ base_stat: stats[i], effort: 0, stat: { name: s, url: `/api/v2/stat/${s}` } })),
    sprites: { front_default: sprite(id), back_default: null, front_shiny: sprite(id) },
  };
}

const POKEMON = [
  mon(1,   "bulbasaur", ["grass", "poison"], 7,   69,  64,  ["overgrow", "chlorophyll"], [45, 49, 49, 65, 65, 45]),
  mon(4,   "charmander", ["fire"],           6,   85,  62,  ["blaze", "solar-power"],    [39, 52, 43, 60, 50, 65]),
  mon(7,   "squirtle",  ["water"],           5,   90,  63,  ["torrent", "rain-dish"],    [44, 48, 65, 50, 64, 43]),
  mon(25,  "pikachu",   ["electric"],        4,   60,  112, ["static", "lightning-rod"], [35, 55, 40, 50, 50, 90]),
  mon(39,  "jigglypuff",["normal", "fairy"], 5,   55,  95,  ["cute-charm", "friend-guard"], [115, 45, 20, 45, 25, 20]),
  mon(94,  "gengar",    ["ghost", "poison"], 15,  405, 250, ["cursed-body"],             [60, 65, 60, 130, 75, 110]),
  mon(132, "ditto",     ["normal"],          3,   40,  101, ["limber", "imposter"],      [48, 48, 48, 48, 48, 48]),
  mon(143, "snorlax",   ["normal"],          21,  4600, 189, ["immunity", "gluttony"],   [160, 110, 65, 65, 110, 30]),
  mon(150, "mewtwo",    ["psychic"],         20,  1220, 340, ["pressure", "unnerve"],    [106, 110, 90, 154, 90, 130]),
];

const TYPES = ["grass", "poison", "fire", "water", "electric", "normal", "fairy", "ghost", "psychic"];

// --- helpers ---------------------------------------------------------

const byName = new Map(POKEMON.map((p) => [p.name, p]));
const byId = new Map(POKEMON.map((p) => [String(p.id), p]));

// Rewrite every relative "/api/v2/..." url so it points back at this server.
function absolutize(value, origin) {
  if (typeof value === "string") return value.startsWith("/api/v2/") ? origin + value : value;
  if (Array.isArray(value)) return value.map((v) => absolutize(v, origin));
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, absolutize(v, origin)]));
  }
  return value;
}

// --- routes ----------------------------------------------------------

const server = createServer((req, res) => {
  const origin = `http://${req.headers.host ?? `localhost:${PORT}`}`;
  const url = new URL(req.url, origin);
  const parts = url.pathname.replace(/\/+$/, "").split("/").filter(Boolean); // ["api","v2","pokemon","ditto"]

  const cors = { "access-control-allow-origin": "*" };

  const json = (body) => {
    res.writeHead(200, { "content-type": "application/json; charset=utf-8", ...cors });
    res.end(JSON.stringify(absolutize(body, origin), null, 2));
  };

  // Plain text, exactly like the real API. This is what makes an unchecked
  // res.json() fail with a SyntaxError — keep it non-JSON on purpose.
  const notFound = () => {
    res.writeHead(404, { "content-type": "text/plain; charset=utf-8", ...cors });
    res.end("Not Found");
  };

  if (req.method === "OPTIONS") { res.writeHead(204, cors); return res.end(); }
  if (req.method !== "GET") { res.writeHead(405, cors); return res.end("Method Not Allowed"); }

  const [api, v2, resource, id] = parts;
  if (api !== "api" || v2 !== "v2") return notFound();

  // /api/v2/pokemon  and  /api/v2/pokemon/:nameOrId
  if (resource === "pokemon") {
    if (!id) {
      const limit = Number(url.searchParams.get("limit") ?? 20);
      const offset = Number(url.searchParams.get("offset") ?? 0);
      const page = POKEMON.slice(offset, offset + limit);
      const link = (o) =>
        o >= 0 && o < POKEMON.length ? `${origin}/api/v2/pokemon?offset=${o}&limit=${limit}` : null;
      return json({
        count: POKEMON.length,
        next: link(offset + limit),
        previous: link(offset - limit),
        results: page.map((p) => ({ name: p.name, url: `/api/v2/pokemon/${p.name}` })),
      });
    }
    const found = byName.get(id.toLowerCase()) ?? byId.get(id);
    return found ? json(found) : notFound();
  }

  // /api/v2/type  and  /api/v2/type/:name
  if (resource === "type") {
    if (!id) {
      return json({
        count: TYPES.length,
        next: null,
        previous: null,
        results: TYPES.map((t) => ({ name: t, url: `/api/v2/type/${t}` })),
      });
    }
    const name = id.toLowerCase();
    if (!TYPES.includes(name)) return notFound();
    return json({
      id: TYPES.indexOf(name) + 1,
      name,
      pokemon: POKEMON
        .filter((p) => p.types.some((t) => t.type.name === name))
        .map((p, i) => ({ slot: i + 1, pokemon: { name: p.name, url: `/api/v2/pokemon/${p.name}` } })),
    });
  }

  return notFound();
});

server.listen(PORT, () => {
  console.log(`PokéAPI stand-in listening on http://localhost:${PORT}`);
  console.log(`  try  http://localhost:${PORT}/api/v2/pokemon/ditto`);
  console.log(`  or   http://localhost:${PORT}/api/v2/pokemon?limit=5`);
});
