package com.curriculum.labs;

/** No order with the given id exists. */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("no such order: " + orderId);
    }
}
