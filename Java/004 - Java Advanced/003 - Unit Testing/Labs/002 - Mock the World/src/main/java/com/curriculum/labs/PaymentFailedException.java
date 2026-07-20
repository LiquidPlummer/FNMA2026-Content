package com.curriculum.labs;

/** The service's own failure signal: payment could not be completed. */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
