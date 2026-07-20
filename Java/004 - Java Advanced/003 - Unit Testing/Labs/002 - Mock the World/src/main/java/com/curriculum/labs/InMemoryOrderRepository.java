package com.curriculum.labs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A working fake so the app runs (see Main). Your tests should NOT use it —
 * that's the whole point of the lab.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new HashMap<>();

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }
}
