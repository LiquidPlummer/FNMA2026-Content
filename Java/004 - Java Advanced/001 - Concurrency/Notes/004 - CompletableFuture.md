# CompletableFuture

`Future.get()` blocks — so a workflow of dependent async steps degenerates into threads standing in line. **`CompletableFuture<T>`** upgrades the model: a future we can attach *callbacks* to, transforming and combining not-yet-available results into pipelines, in exactly the declarative style the Streams API applied to collections — but over *time* instead of elements.

---

## Creating and Chaining

```java
CompletableFuture<Quote> quoteF =
        CompletableFuture.supplyAsync(() -> fetchQuote("WIDGET-7"));   // runs on a pool

CompletableFuture<String> messageF = quoteF
        .thenApply(q -> q.price() * 1.08)               // transform the result (like map)
        .thenApply(total -> "Total: $" + total);        // ...again

messageF.thenAccept(System.out::println);               // consume it when ready
```

*An async pipeline: start a task, chain transformations, attach a consumer — no `get()`, nothing blocks.*

**`supplyAsync(Supplier)`** launches the work (on the common ForkJoinPool by default; pass an executor from last lesson to choose — mandatory practice for I/O work). Each **`thenApply(Function)`** returns a *new* CompletableFuture for the transformed result; **`thenAccept(Consumer)`** terminates with a side effect. The functional-interface vocabulary of Unit 3, verbatim — `Supplier`, `Function`, `Consumer` — now scheduling work instead of traversing collections.

When a step is *itself* async — its function returns a CompletableFuture — **`thenCompose`** flattens the nesting, precisely as `flatMap` did for `Optional` and streams:

```java
CompletableFuture<Order> orderF =
        fetchCustomer(id)                                // CompletableFuture<Customer>
            .thenCompose(c -> fetchOpenOrder(c));        // returns CompletableFuture<Order> — flattened
```

*`thenCompose` chains dependent async calls without producing a future-of-a-future.*

---

## Combining Independent Futures

Where CompletableFuture earns its keep: several independent slow calls, running *simultaneously*, merged when all arrive:

```java
CompletableFuture<Inventory> invF = CompletableFuture.supplyAsync(() -> checkInventory(sku), ioPool);
CompletableFuture<Price> priceF  = CompletableFuture.supplyAsync(() -> fetchPrice(sku), ioPool);

CompletableFuture<Offer> offerF =
        invF.thenCombine(priceF, (inv, price) -> makeOffer(inv, price));   // both done → combine
```

*`thenCombine`: two parallel fetches, one result — total latency is the slower call, not the sum.*

For fan-out beyond two: **`allOf(f1, f2, ...)`** completes when all do (results collected via the individual futures afterward); **`anyOf(...)`** completes with the first — racing redundant mirrors, or a value against a timeout. Speaking of which: **`orTimeout(2, TimeUnit.SECONDS)`** fails the future late, and **`completeOnTimeout(fallback, 2, SECONDS)`** substitutes a default — deadline discipline in one call.

---

## Handling Failure in the Pipeline

Exceptions flow *through* the chain: a failing stage skips subsequent `thenApply`/`thenAccept` stages until something handles the failure — try/catch semantics, async-shaped:

```java
CompletableFuture<String> safe =
        fetchQuote("WIDGET-7")
            .thenApply(q -> format(q))
            .exceptionally(ex -> {                       // catch-equivalent: supply a fallback
                log.warn("quote failed", ex);
                return "price unavailable";
            });

// Or handle both outcomes in one stage:
fetchQuote(sku).handle((quote, ex) ->
        ex == null ? format(quote) : "price unavailable");
```

*`exceptionally` is the async catch block; `handle` receives result-or-exception and decides.*

`whenComplete((result, ex) -> ...)` observes without altering the outcome — the `finally` analogue. Designed pipelines put recovery *in* the chain like this, rather than letting failures surface only when someone finally joins.

---

## Getting Out, and Keeping Perspective

Something eventually needs the value on the current thread: **`join()`** blocks like `get()` but throws unchecked (`CompletionException`) — friendlier in pipelines; in a request/response server the *framework* often accepts the CompletableFuture itself (Spring controllers can return one — Spring Web topic), so nothing of ours ever blocks.

Perspective, honestly held: CompletableFuture is Java's async composition tool, and it's verbose compared to what came after — **virtual threads** (Java 21) let plain blocking code (`var price = fetchPrice(sku);`) scale like async code, no pipeline syntax at all, and much new I/O-bound code simply writes that. CompletableFuture remains everywhere in existing codebases and stays the right tool for *genuinely parallel composition* — the `thenCombine`/`allOf` fan-out patterns — so fluency in both is the professional baseline. Either way, the bedrock from lessons 1–2 (interleaving, shared state, visibility) doesn't move.

That closes Concurrency. Next topic: Reflection — the runtime looking at itself, and the machinery beneath every framework in the rest of this unit.
