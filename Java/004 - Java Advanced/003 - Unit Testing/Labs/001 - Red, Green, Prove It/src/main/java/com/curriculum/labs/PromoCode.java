package com.curriculum.labs;

/**
 * A promotional discount code. Written by a colleague, shipped without a
 * single test — which is where you come in. The intended behavior (the spec)
 * is in the README; this class is what actually got written.
 */
public class PromoCode {

    private final String code;
    private final double percentOff;
    private final double minimumOrder;

    public PromoCode(String code, double percentOff, double minimumOrder) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (percentOff <= 0 || percentOff >= 50) {
            throw new IllegalArgumentException(
                    "percentOff must be between 0 (exclusive) and 50 (inclusive), got " + percentOff);
        }
        if (minimumOrder < 0) {
            throw new IllegalArgumentException("minimumOrder must not be negative");
        }
        this.code = code.trim().toUpperCase();
        this.percentOff = percentOff;
        this.minimumOrder = minimumOrder;
    }

    /**
     * Applies the discount to an order total and returns the discounted total.
     */
    public double apply(double orderTotal) {
        if (orderTotal <= 0) {
            throw new IllegalArgumentException("order total must be positive");
        }
        if (orderTotal <= minimumOrder) {
            throw new IllegalArgumentException(
                    "order total is below the minimum of " + minimumOrder);
        }
        return orderTotal * (1 - percentOff / 100.0);
    }

    /**
     * A full breakdown of what applying this code to an order would do.
     */
    public Discount preview(double orderTotal) {
        double finalTotal = apply(orderTotal);
        return new Discount(code, orderTotal - finalTotal, finalTotal);
    }

    public record Discount(String code, double amountOff, double finalTotal) { }

    public String getCode() {
        return code;
    }

    public double getPercentOff() {
        return percentOff;
    }

    public double getMinimumOrder() {
        return minimumOrder;
    }
}
