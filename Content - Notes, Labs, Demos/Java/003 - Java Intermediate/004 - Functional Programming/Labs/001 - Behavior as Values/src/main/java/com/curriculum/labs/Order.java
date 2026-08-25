package com.curriculum.labs;

import java.util.List;

/** An order, plus sample data for the lab. */
public record Order(String id, String customer, double amount, boolean rush) {

    public static List<Order> sample() {
        return List.of(
                new Order("ORD-1", "Ada Corp", 1250.00, false),
                new Order("ORD-2", "Byte Barn", 89.50, true),
                new Order("ORD-3", "Ada Corp", 430.00, false),
                new Order("ORD-4", "Cog & Co", 2999.99, true),
                new Order("ORD-5", "Byte Barn", 15.75, false),
                new Order("ORD-6", "Delta Ltd", 640.00, true)
        );
    }
}
