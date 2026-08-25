package com.curriculum.labs;

import java.util.Arrays;

/**
 * Array Fundamentals Lab — a small quiz-statistics console app we build up
 * piece by piece.
 *
 * Follow the README's guided walkthrough. Each part tells you what to add
 * and points at one of the numbered markers below.
 */
public class ArrayFundamentalsLab {

    // Same roster convention as the Loops lab: -1 marks a student who was absent.
    static final int[] QUIZ_SCORES = {88, 72, -1, 95, 64, -1, 79, 100};

    public static void main(String[] args) {
        System.out.println("=== Quiz Statistics ===");

        // [MARKER 1] Part 1: declare STUDENT_NAMES (a parallel array literal) here,
        //            and print the roster alongside the scores.

        // [MARKER 2] Part 2: index reads/writes go here, including the
        //            deliberately-broken out-of-bounds read we'll fix.

        // [MARKER 3] Part 3: the length-based idioms (last element, guard check)
        //            go here, replacing the hardcoded index from Part 2.

        // [MARKER 4] Part 4: one line per iteration recipe, added as we write
        //            each method below — average, then search, then max, then curve.

        System.out.println("Done.");
    }

    // [MARKER 5] Part 4a: static double averageScore(int[] scores) goes here (aggregate).

    // [MARKER 6] Part 4b: static int indexOfFirstFailing(int[] scores) goes here (search).

    // [MARKER 7] Part 4c: static int highestScore(int[] scores) goes here (max).

    // [MARKER 8] Part 4d: static void curve(int[] scores, int points) goes here (transform).
}
