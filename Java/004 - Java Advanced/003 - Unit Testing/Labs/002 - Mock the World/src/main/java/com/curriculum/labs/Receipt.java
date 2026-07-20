package com.curriculum.labs;

/** What checkout hands back to the caller. */
public record Receipt(String orderId, String transactionId, double amountCharged) { }
