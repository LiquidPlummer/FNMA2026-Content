package com.curriculum.labs;

import java.util.List;

import com.curriculum.labs.Order.Status;

/**
 * Two dozen orders, seeded with edge cases on purpose: cancelled rows,
 * zero amounts, repeat customers, and two orders identical in every
 * field but id.
 */
public class OrderData {

    public static List<Order> sample() {
        return List.of(
                new Order("O-01", "Ada Corp",  "NORTH", 1250.00, Status.PAID),
                new Order("O-02", "Byte Barn", "SOUTH",   89.50, Status.PAID),
                new Order("O-03", "Ada Corp",  "NORTH",  430.00, Status.PENDING),
                new Order("O-04", "Cog & Co",  "EAST",  2999.99, Status.PAID),
                new Order("O-05", "Byte Barn", "SOUTH",   15.75, Status.CANCELLED),
                new Order("O-06", "Delta Ltd", "WEST",   640.00, Status.PAID),
                new Order("O-07", "Ada Corp",  "NORTH",    0.00, Status.PENDING),
                new Order("O-08", "Eta Inc",   "EAST",   310.25, Status.PAID),
                new Order("O-09", "Cog & Co",  "EAST",   145.00, Status.CANCELLED),
                new Order("O-10", "Byte Barn", "SOUTH",  875.00, Status.PAID),
                new Order("O-11", "Delta Ltd", "WEST",    52.80, Status.PENDING),
                new Order("O-12", "Ada Corp",  "NORTH", 1250.00, Status.PAID),
                new Order("O-13", "Eta Inc",   "EAST",    99.99, Status.PAID),
                new Order("O-14", "Cog & Co",  "EAST",   410.00, Status.PENDING),
                new Order("O-15", "Delta Ltd", "WEST",  1830.00, Status.PAID),
                new Order("O-16", "Byte Barn", "SOUTH",    0.00, Status.CANCELLED),
                new Order("O-17", "Ada Corp",  "NORTH",  266.40, Status.PAID),
                new Order("O-18", "Eta Inc",   "EAST",   740.10, Status.PENDING),
                new Order("O-19", "Cog & Co",  "EAST",    33.00, Status.PAID),
                new Order("O-20", "Delta Ltd", "WEST",   505.55, Status.PAID),
                new Order("O-21", "Byte Barn", "SOUTH",  120.00, Status.PENDING),
                new Order("O-22", "Ada Corp",  "NORTH",   77.25, Status.CANCELLED),
                new Order("O-23", "Eta Inc",   "EAST",  1499.00, Status.PAID),
                new Order("O-24", "Delta Ltd", "WEST",   248.60, Status.PAID)
        );
    }
}
