package com.curriculum.labs;

/**
 * An order and its status — tracked as plain strings, for now. This class
 * is the "mess" this lab refactors into a proper enum.
 *
 * Follow the README's guided walkthrough. Each part tells you what to
 * change and points at one of the numbered markers below.
 */
public class Order {

    // [MARKER 2] Part 1: delete these five constants once OrderStatus exists.
    public static final String PENDING = "PENDING";
    public static final String PAID = "PAID";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";

    private final String id;
    private String status;   // [MARKER 3] Part 1: change this field's type to OrderStatus.

    public Order(String id) {
        this.id = id;
        this.status = PENDING;   // [MARKER 4] Part 1: becomes OrderStatus.PENDING.
    }

    public String getId() {
        return id;
    }

    public String getStatus() {   // [MARKER 5] Part 1: return type becomes OrderStatus.
        return status;
    }

    public void setStatus(String status) {   // [MARKER 6] Part 1: parameter type becomes OrderStatus.
        this.status = status;
    }
}
