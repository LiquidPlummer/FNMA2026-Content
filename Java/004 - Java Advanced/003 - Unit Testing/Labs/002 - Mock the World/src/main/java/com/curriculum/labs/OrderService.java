package com.curriculum.labs;

/**
 * The class under test. It orchestrates checkout and cancellation across its
 * two dependencies — and knows them only as interfaces, which is exactly what
 * makes it testable in isolation.
 */
public class OrderService {

    private final OrderRepository repository;
    private final PaymentGateway gateway;

    public OrderService(OrderRepository repository, PaymentGateway gateway) {
        this.repository = repository;
        this.gateway = gateway;
    }

    /**
     * Charges an OPEN order, marks it PAID, persists it, and returns a receipt.
     *
     * @throws OrderNotFoundException if the order doesn't exist
     * @throws IllegalStateException  if the order isn't OPEN
     * @throws PaymentFailedException if the gateway times out (the order is left unchanged)
     */
    public Receipt checkout(String orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!"OPEN".equals(order.getStatus())) {
            throw new IllegalStateException("order " + orderId + " is not open for checkout");
        }

        try {
            PaymentResult result = gateway.charge(order.getCustomerId(), order.getTotal());
            order.setStatus("PAID");
            order.setTransactionId(result.transactionId());
            repository.save(order);
            return new Receipt(order.getId(), result.transactionId(), order.getTotal());
        } catch (GatewayTimeoutException e) {
            throw new PaymentFailedException("payment failed for order " + orderId, e);
        }
    }

    /**
     * Cancels an order. The intended behavior is spelled out in the README's
     * exercise 1 — and the tests you write there are its specification.
     */
    public void cancel(String orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if ("OPEN".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            repository.save(order);
        } else if ("PAID".equals(order.getStatus())) {
            gateway.refund(order.getTransactionId());
            order.setStatus("REFUNDED");
            repository.save(order);
        } else {
            throw new IllegalStateException("order " + orderId + " cannot be cancelled");
        }
    }
}
