package com.curriculum.labs;

import java.util.Optional;

/** Persistence port for orders. In production this hits a database. */
public interface OrderRepository {

    Optional<Order> findById(String orderId);

    Order save(Order order);
}
