package com.curriculum.labs;

/**
 * Our service layer — currently reporting failures the pre-exception way.
 * This is the class we redesign in the walkthrough.
 */
public class InventoryService {
    private final InventoryStore store = new InventoryStore();

    /**
     * Reserves stock.
     * Returns 0 = reserved, -1 = unknown SKU, -2 = not enough stock,
     * -3 = storage problem. (Yes, really. We're going to fix this.)
     */
    public int reserve(String sku, int quantity) {
        try {
            int available = store.fetchQuantity(sku);
            if (available == -1) {
                return -1;
            }
            if (available < quantity) {
                return -2;
            }
            store.decrement(sku, quantity);
            return 0;
        } catch (StorageFailure e) {
            return -3;
        }
    }
}
