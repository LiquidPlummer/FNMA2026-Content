# Cache Behaviors, TTLs & Invalidation

Three related controls determine what CloudFront caches, for how long, and how to remove something early.

---

## Cache Behaviors

A **cache behavior** matches a path pattern and defines how those requests are handled. Behaviors are evaluated in order, with a default behavior catching anything unmatched.

```
/api/*      →  origin: ALB           TTL: 0        forward: all headers, cookies
/static/*   →  origin: S3            TTL: 1 year   forward: nothing
/images/*   →  origin: S3            TTL: 1 day    forward: nothing
*           →  origin: S3            TTL: 5 min    forward: nothing
```

*One distribution serving an API and static assets with entirely different caching, from one domain — which also avoids CORS between them.*

Each behavior controls its origin, allowed HTTP methods, TTLs, cache key composition, compression, and whether HTTPS is required.

---

## The Cache Key

The **cache key** determines when two requests are considered the same object. By default it is the path and query string; it can also include headers and cookies.

This is where cache hit ratio is won or lost. **Every element added to the cache key multiplies the number of cached copies.**

Including `Authorization` means a separate cache entry per user — effectively no caching at all. Including a session cookie does the same. Including `User-Agent` fragments the cache across every browser version in existence.

The rule: **include in the cache key only what genuinely changes the response.** If a header does not change what the origin returns, it must not be in the key.

**Origin request policies** are the counterpart: a header can be forwarded to the origin without being part of the cache key. This is how the origin can see `User-Agent` or `CloudFront-Viewer-Country` for logging while still serving one cached copy to everyone.

---

## TTLs

Three settings interact with origin headers:

- **Minimum TTL** — the floor, regardless of origin headers
- **Default TTL** — used when the origin sends no cache headers
- **Maximum TTL** — the ceiling, regardless of origin headers

The origin's `Cache-Control: max-age` is respected within the minimum and maximum bounds. So origin headers normally control caching, and CloudFront's settings constrain them.

**Controlling caching from the origin is usually better** — cache duration then lives with the content rather than in distribution configuration.

```
Cache-Control: public, max-age=31536000, immutable   ← fingerprinted assets
Cache-Control: public, max-age=0, must-revalidate    ← index.html
Cache-Control: private, no-store                     ← per-user API responses
```

*The standard set for a modern front end: hashed assets cached indefinitely, the entry point never cached, and personalized responses not cached at all.*

---

## Invalidation

**Invalidation** removes objects from edge caches before their TTL expires:

```bash
aws cloudfront create-invalidation --distribution-id E1ABCDEF --paths "/index.html" "/api/config"
```

*Removes specific paths from all edge locations, typically taking a few minutes to complete.*

Invalidation is a workaround, not a deployment strategy. Its problems:

**It costs money.** The first 1,000 paths per month are free; beyond that each path is billed. `/*` counts as one path but invalidates everything, discarding the entire cache and sending a burst of traffic to the origin.

**It is not instant.** Propagation takes minutes, so it is not a rollback mechanism.

**It is easy to over-use.** A deployment that invalidates `/*` every time is not really caching — every release starts cold.

### Versioned filenames instead

The better approach is to make content immutable and change the URL:

```
/static/app.a1b2c3d4.js      ← content hash in the filename
/static/app.e5f6g7h8.js      ← new build, new URL
```

*A new build produces a new URL, so the old cached copy is simply never requested again. No invalidation is needed and assets can be cached for a year.*

This pairs with a short TTL on `index.html`, which references the current asset URLs. Only the small entry document needs revalidating; everything else is permanently cacheable. Every modern front-end build tool produces fingerprinted filenames for exactly this reason.

Invalidation then remains for the genuine exceptions — an urgent content correction, or a configuration file that must change immediately.

---

## Key Takeaways

- Cache behaviors match path patterns and control origin, TTLs, cache key, and methods, evaluated in order.
- The cache key decides what counts as the same object; every added element multiplies cached copies.
- Never include per-user headers or cookies in the cache key — it eliminates caching entirely.
- Use origin request policies to forward headers to the origin without adding them to the cache key.
- Minimum and maximum TTLs bound what origin `Cache-Control` headers can specify; controlling caching from the origin is usually clearer.
- Invalidation costs money beyond 1,000 paths a month, takes minutes, and is not a deployment strategy.
- Use content-hashed filenames so new builds produce new URLs, with only the entry document short-cached.
