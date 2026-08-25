package com.curriculum.labs;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Three simulated vendor pricing services. Provided as-is — don't edit them;
 * the walkthrough's timings and failure behavior depend on these numbers.
 *
 * Each call prints which thread it runs on, sleeps to simulate network
 * latency, and returns a price with a little random jitter.
 */
public class Services {

    /** The in-house warehouse system. Steady: ~800 ms. */
    public static double warehousePrice(String sku) {
        log("warehouse", sku);
        pause(800);
        return 42.00 + jitter();
    }

    /** The retail partner API. Steady but slower: ~1000 ms. */
    public static double retailPrice(String sku) {
        log("retail   ", sku);
        pause(1000);
        return 39.99 + jitter();
    }

    /**
     * The discount partner. Cheapest when it behaves — but flaky:
     * usually ~1200 ms, yet roughly one call in three stalls for 3 seconds.
     */
    public static double partnerPrice(String sku) {
        log("partner  ", sku);
        if (ThreadLocalRandom.current().nextInt(3) == 0) {
            pause(3000);
        } else {
            pause(1200);
        }
        return 35.50 + jitter();
    }

    private static void log(String vendor, String sku) {
        System.out.printf("    [%s] %s quoting %s...%n",
                Thread.currentThread().getName(), vendor, sku);
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("quote interrupted", e);
        }
    }

    private static double jitter() {
        return ThreadLocalRandom.current().nextInt(0, 100) / 100.0;
    }
}
