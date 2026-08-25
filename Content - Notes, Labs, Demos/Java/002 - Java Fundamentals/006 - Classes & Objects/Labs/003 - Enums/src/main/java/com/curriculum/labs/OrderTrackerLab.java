package com.curriculum.labs;

/**
 * Driver for the Order Tracker Lab. Follow the README — each part points
 * at one of the numbered markers below.
 */
public class OrderTrackerLab {

    public static void main(String[] args) {
        System.out.println("=== Order Tracker ===");

        // [MARKER 7] Part 1: two orders — one of them hits a typo bug.
        Order order1 = new Order("ORD-1");
        Order order2 = new Order("ORD-2");

        order1.setStatus(Order.PAID);
        order1.setStatus("SHIPPPED");   // typo: three P's — compiles fine, silently wrong
        order2.setStatus(Order.PAID);

        printStatus(order1);
        printStatus(order2);

        // [MARKER 8] Part 2: parse a status string from "external input," valueOf-style.

        // [MARKER 9] Part 4: isTerminal() / label() demonstration.

        System.out.println("Done.");
    }

    // [MARKER 10] Part 3: this if/else chain becomes a switch on OrderStatus.
    private static void printStatus(Order order) {
        String label;
        if (order.getStatus().equals(Order.PENDING)) {
            label = "Awaiting payment";
        } else if (order.getStatus().equals(Order.PAID)) {
            label = "In progress";
        } else if (order.getStatus().equals(Order.SHIPPED)) {
            label = "In progress";
        } else if (order.getStatus().equals(Order.DELIVERED)) {
            label = "Complete";
        } else if (order.getStatus().equals(Order.CANCELLED)) {
            label = "Cancelled";
        } else {
            label = "Unknown status: " + order.getStatus();
        }
        System.out.println(order.getId() + ": " + label);
    }
}
