package com.curriculum.labs;

import java.util.HashMap;
import java.util.Map;

/**
 * The (simulated) low-level storage layer. Also pretend-library code:
 * it speaks in sentinels (-1 for unknown) and checked StorageFailure.
 */
public class InventoryStore {
    private final Map<String, Integer> stock = new HashMap<>();

    public InventoryStore() {
        stock.put("WIDGET-1", 10);
        stock.put("GADGET-2", 0);
        stock.put("SPROCKET-9", 3);
    }

    /** Returns the quantity on hand, or -1 for an unknown SKU. */
    public int fetchQuantity(String sku) throws StorageFailure {
        if (sku.startsWith("X")) {
            throw new StorageFailure("read error on sector 7 for key " + sku);
        }
        return stock.getOrDefault(sku, -1);
    }

    public void decrement(String sku, int by) {
        stock.merge(sku, -by, Integer::sum);
    }
}
