package com.curriculum.labs;

/**
 * Thread Lab — threads, the race condition, and three ways to fix it.
 *
 * Follow the README's guided walkthrough. Each part is a static method below,
 * selected by a command-line argument:
 *
 *     mvn -q compile exec:java -Dexec.args="1"     (runs part1)
 *
 * The numbered [MARKER] comments show where each part's code goes.
 */
public class ThreadLab {

    /** How many increments each racing thread performs. */
    static final int INCREMENTS = 100_000;

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Usage: mvn -q compile exec:java -Dexec.args=\"<part>\"  where <part> is 1-4");
            return;
        }
        switch (args[0]) {
            case "1" -> part1();
            case "2" -> part2();
            case "3" -> part3();
            case "4" -> part4();
            default -> System.out.println("Unknown part: " + args[0]);
        }
    }

    // ------------------------------------------------------------------
    // Part 1 — creating threads
    // ------------------------------------------------------------------
    static void part1() throws InterruptedException {
        System.out.println("=== Part 1: two workers ===");
        System.out.println("main thread is: " + Thread.currentThread().getName());

        // [MARKER 1] Create the two worker threads here.
    }

    // ------------------------------------------------------------------
    // Part 2 — the race, observed
    // ------------------------------------------------------------------

    /** One counter, many implementations — Parts 2 and 3 all race through this. */
    interface Counter {
        void increment();
        int value();
    }

    // [MARKER 2a] BrokenCounter goes here.

    // [MARKER 2b] The race(...) harness method goes here.

    static void part2() throws InterruptedException {
        System.out.println("=== Part 2: the race ===");
        // [MARKER 2c] Three trials of the broken counter go here.
    }

    // ------------------------------------------------------------------
    // Part 3 — three fixes
    // ------------------------------------------------------------------

    // [MARKER 3a] SyncCounter goes here.

    // [MARKER 3b] AtomicCounter goes here.

    // [MARKER 3c] CountTask (the no-shared-state fix) goes here.

    static void part3() throws InterruptedException {
        System.out.println("=== Part 3: the fixes ===");
        // [MARKER 3d] Race the fixed counters here, then the confinement version.
    }

    // ------------------------------------------------------------------
    // Part 4 — visibility and volatile
    // ------------------------------------------------------------------

    // [MARKER 4a] Worker (the stop-flag loop) goes here.

    static void part4() throws InterruptedException {
        System.out.println("=== Part 4: the stop flag ===");
        // [MARKER 4b] Start the worker, flip the flag, see if it stops.
    }
}
