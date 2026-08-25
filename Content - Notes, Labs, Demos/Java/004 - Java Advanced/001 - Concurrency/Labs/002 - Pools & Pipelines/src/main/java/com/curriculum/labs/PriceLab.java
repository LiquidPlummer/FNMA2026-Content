package com.curriculum.labs;

/**
 * Price Lab — a best-price aggregator built three times:
 * sequentially, on a thread pool, and as a CompletableFuture pipeline.
 *
 * Follow the README's guided walkthrough. Each part is a static method below,
 * selected by a command-line argument:
 *
 *     mvn -q compile exec:java -Dexec.args="1"     (runs part1)
 *
 * The numbered [MARKER] comments show where each part's code goes.
 */
public class PriceLab {

    static final String SKU = "WIDGET-7";

    /** A vendor's answer: who quoted, and at what price. */
    record Quote(String vendor, double price) { }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: mvn -q compile exec:java -Dexec.args=\"<part>\"  where <part> is 1-3");
            return;
        }
        switch (args[0]) {
            case "1" -> part1();
            case "2" -> part2();
            case "3" -> part3();
            default -> System.out.println("Unknown part: " + args[0]);
        }
    }

    /** The smaller of two quotes — the aggregator's one business rule. */
    static Quote cheaper(Quote a, Quote b) {
        return a.price() <= b.price() ? a : b;
    }

    /** Milliseconds elapsed since a System.nanoTime() reading. */
    static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    // ------------------------------------------------------------------
    // Part 1 — the sequential baseline
    // ------------------------------------------------------------------
    static void part1() {
        System.out.println("=== Part 1: sequential ===");
        // [MARKER 1] Call the three services one after another, time it, report the best quote.
    }

    // ------------------------------------------------------------------
    // Part 2 — a fixed pool and invokeAll
    // ------------------------------------------------------------------
    static void part2() throws Exception {
        System.out.println("=== Part 2: thread pool ===");
        // [MARKER 2] Same aggregation as Callable tasks on a fixed pool.
    }

    // ------------------------------------------------------------------
    // Part 3 — the CompletableFuture pipeline
    // ------------------------------------------------------------------
    static void part3() {
        System.out.println("=== Part 3: CompletableFuture ===");
        // [MARKER 3] The async pipeline: supplyAsync, thenApply, thenCombine,
        //            orTimeout + exceptionally on the flaky partner.
    }
}
