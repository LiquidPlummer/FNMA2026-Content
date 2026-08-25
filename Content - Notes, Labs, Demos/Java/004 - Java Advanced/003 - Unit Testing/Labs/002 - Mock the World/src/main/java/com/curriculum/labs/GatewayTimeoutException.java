package com.curriculum.labs;

/** The payment provider didn't answer in time. */
public class GatewayTimeoutException extends RuntimeException {
    public GatewayTimeoutException(String message) {
        super(message);
    }
}
