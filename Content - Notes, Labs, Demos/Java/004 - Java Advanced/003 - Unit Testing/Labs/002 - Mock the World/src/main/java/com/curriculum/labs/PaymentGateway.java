package com.curriculum.labs;

/**
 * Payment provider port. In production this is a slow, costly, occasionally
 * unavailable HTTP API — exactly the kind of thing unit tests must not touch.
 */
public interface PaymentGateway {

    /**
     * Charges the customer.
     * @throws GatewayTimeoutException if the provider doesn't answer in time
     */
    PaymentResult charge(String customerId, double amount);

    /**
     * Refunds a previous charge.
     * @throws GatewayTimeoutException if the provider doesn't answer in time
     */
    PaymentResult refund(String transactionId);
}
