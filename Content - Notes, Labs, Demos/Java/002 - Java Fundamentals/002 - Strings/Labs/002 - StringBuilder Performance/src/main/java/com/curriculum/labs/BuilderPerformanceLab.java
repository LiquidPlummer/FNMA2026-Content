package com.curriculum.labs;

/**
 * Builder Performance Lab — watch string concatenation blow up, then fix it.
 *
 * Follow the README's guided walkthrough. Each section tells you what to
 * add and points at one of the numbered markers below.
 */
public class BuilderPerformanceLab {

    static final int LINE_COUNT = 100_000;

    public static void main(String[] args) {
        System.out.println("=== Builder Performance Lab ===");

        // [MARKER 1] Part 1: build the report with += and time it.

        // [MARKER 2] Part 2: build the same report with StringBuilder and time it.

        // [MARKER 3] Part 3: the fluent API demo (insert, reverse, setLength) goes here.

        System.out.println("Done.");
    }
}
