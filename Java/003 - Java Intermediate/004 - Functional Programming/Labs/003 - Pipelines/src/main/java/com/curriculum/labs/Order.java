package com.curriculum.labs;

/** An order for the pipeline lab. */
public record Order(String id, String customer, String region, double amount, Status status) {

    public enum Status { PENDING, PAID, CANCELLED }
}
