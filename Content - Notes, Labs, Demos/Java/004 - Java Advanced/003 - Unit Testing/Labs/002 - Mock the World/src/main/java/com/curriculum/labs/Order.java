package com.curriculum.labs;

/**
 * An order moving through checkout. Status is one of: OPEN, PAID, SHIPPED,
 * CANCELLED, REFUNDED.
 */
public class Order {

    private final String id;
    private final String customerId;
    private final double total;
    private String status;
    private String transactionId;

    public Order(String id, String customerId, double total, String status) {
        this.id = id;
        this.customerId = customerId;
        this.total = total;
        this.status = status;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getTransactionId() { return transactionId; }

    public void setStatus(String status) { this.status = status; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    @Override
    public String toString() {
        return "Order[id=" + id + ", customer=" + customerId + ", total=" + total
                + ", status=" + status + ", tx=" + transactionId + "]";
    }
}
