# Lab: Design the Failure

In this lab we design custom exceptions: a small domain family with meaningful messages, cause chains, and machine-readable fields — and we use it to replace an integer-sentinel error scheme in a working inventory service. The redesign also demonstrates the boundary-translation pattern: a low-level checked `StorageFailure` wrapped into our domain terms with its cause preserved.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

From this lab's folder:

```console
mvn -q compile exec:java
```

Four starter files in `src/main/java/com/curriculum/labs/`: `FailureLab` (the driver), `InventoryService` (ours — the redesign target), and `InventoryStore` + `StorageFailure` (pretend-library code we may *read but not modify*).

---

## Part 1 — What sentinels cost us

Run the starter. All five orders produce output; nothing crashes. So what's wrong? Three things, and each is worth seeing before we fix them:

1. **Failures are ignorable.** In `FailureLab`, add one line anywhere in the loop: `service.reserve(sku, qty);` — bare, return value discarded. It compiles without a murmur. A reservation that might have failed just... happened, silently. No sentinel scheme can force callers to check.
2. **The codes carry nothing.** Code `-3` means "storage problem" — *which* problem? The `StorageFailure` had a message naming the failing sector; `reserve` caught it and flattened it to an integer. The diagnosis is gone forever.
3. **The ladder is fragile.** The `-2`/`-1` meanings live in a Javadoc comment and a chain of `else if`s. Add a new failure mode and every caller needs a new branch — and nothing warns the ones that don't get it (note the starter's own "unrecognized code" fallback: an admission of defeat).

Remove the bare call. Now we replace the whole scheme.

---

## Part 2 — Build the family

We'll create three new files. First the family root — every inventory failure *is an* `InventoryException`:

```java
package com.curriculum.labs;

public class InventoryException extends RuntimeException {
    public InventoryException(String message) {
        super(message);
    }
    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

*The root: unchecked (extends `RuntimeException` — callers may handle it, nothing forces ceremony), with the two standard constructors including the cause overload.*

New file `InventoryException.java`. Why unchecked? These failures are either caller bugs (unknown SKU) or business outcomes the caller *chooses* to handle (out of stock) — the modern lean from the notes. Now the two concrete types, each carrying the SKU as data:

```java
package com.curriculum.labs;

public class UnknownSkuException extends InventoryException {
    private final String sku;

    public UnknownSkuException(String sku) {
        super("no such SKU in catalog: " + sku);
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }
}
```

*A concrete failure: the message is assembled in the constructor (every throw site gets a good one for free), and the SKU rides along as a field for handlers that need it.*

New file `UnknownSkuException.java`. Then create `OutOfStockException.java` yourself, same shape but richer: constructor `(String sku, int requested, int available)`, message like `"WIDGET-1: requested 99, only 6 available"`, and getters for all three fields.

---

## Part 3 — Redesign the service

Now `InventoryService.reserve` stops speaking integer and starts speaking domain. Replace the method with:

```java
public void reserve(String sku, int quantity) {
    int available;
    try {
        available = store.fetchQuantity(sku);
    } catch (StorageFailure e) {
        throw new InventoryException("storage unavailable while reserving " + sku, e);
    }
    if (available == -1) {
        throw new UnknownSkuException(sku);
    }
    if (available < quantity) {
        throw new OutOfStockException(sku, quantity, available);
    }
    store.decrement(sku, quantity);
}
```

*The redesigned service: `void` — success is silent, failure is thrown. The low-level checked `StorageFailure` is translated at the boundary into domain terms, **cause attached**.*

Read the translation block twice — it's the pattern of the lab. The storage layer's vocabulary (`StorageFailure`, sectors) stops here; callers above us hear only inventory vocabulary; and because the cause is chained, no diagnostic detail was lost (Part 4 proves it). Also note what the compiler now does for us: `FailureLab` no longer compiles — every call site is forced to confront the new contract. That's the opposite of the silently ignorable sentinel, and it's a *feature* of the redesign.

---

## Part 4 — Catch in domain terms

Rewrite the driver's loop body — the ladder becomes handlers, at two precisions:

```java
try {
    service.reserve(sku, qty);
    System.out.println("RESERVED  " + qty + " x " + sku);
} catch (OutOfStockException e) {
    System.out.println("BACKORDER " + e.getSku()
            + " (short by " + (e.getRequested() - e.getAvailable()) + ")");
} catch (InventoryException e) {
    System.out.println("FAILED    " + sku + ": " + e.getMessage());
}
```

*Precision where we have a distinct response (out-of-stock → backorder, using the carried fields), the family root for everything else.*

Run it. Compare each line against the sentinel version: the backorder line now *computes* from data the exception carried — impossible with code `-2`. Then make the storage case loud: temporarily change the second catch to rethrow (`throw e;`) and run again. The `XCORRUPT-7` order prints a full stack trace ending in:

```
Caused by: com.curriculum.labs.StorageFailure: read error on sector 7 for key XCORRUPT-7
```

*The chain preserved: our domain exception on top, the original low-level failure — sector number and all — underneath. Nothing was flattened.*

That `Caused by:` line is the whole argument for the cause-accepting constructor. Restore the catch when done.

---

## Exercises

1. **Design a family from a spec.** A ticketing module has these failure modes: event not found; event sold out (callers want to know how many seats *were* requested); sale not yet open (callers want the opening time); payment declined by an external gateway (the gateway throws its own checked `GatewayException`). Design and implement the exception set: family root, concrete types, fields, and — for each — one sentence on why it's checked or unchecked. Include the boundary translation for the gateway.

2. **A handler that uses the data.** Extend the driver: when `OutOfStockException` reports at least half the requested quantity available, automatically retry the reservation for the available amount (printing what happened). This is only possible because the exception carries structured fields — say so in a comment at the retry site.

3. **Critique.** A colleague proposes: `AppException` → `BusinessException` → `InventoryBusinessException` → `StockException` → `StockLevelException` → `StockLevelTooLowException`, each in its own file, most never caught specifically. Write a three-sentence review: what's wrong, what you'd keep, and the rule of thumb from the notes that decides it.

4. **The wrong parent.** Suppose `UnknownSkuException` had extended `Exception` (checked) instead. Trace the consequences outward: what changes in `reserve`'s signature, in the driver, and in any future caller three layers up? One paragraph — then state which failure modes in *this* lab, if any, would genuinely justify being checked.
