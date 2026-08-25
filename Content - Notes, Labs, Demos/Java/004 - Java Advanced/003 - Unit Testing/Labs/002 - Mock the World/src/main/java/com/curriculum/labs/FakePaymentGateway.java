package com.curriculum.labs;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A working fake so the app runs (see Main). Your tests should NOT use it —
 * that's the whole point of the lab.
 */
public class FakePaymentGateway implements PaymentGateway {

    private final AtomicInteger sequence = new AtomicInteger(1000);

    @Override
    public PaymentResult charge(String customerId, double amount) {
        return new PaymentResult("TX-" + sequence.incrementAndGet());
    }

    @Override
    public PaymentResult refund(String transactionId) {
        return new PaymentResult("RF-" + sequence.incrementAndGet());
    }
}
