package com.curriculum.labs;

/**
 * Absence Lab — from null-and-hope to Optional-by-design.
 * The starter crashes. That's Part 1.
 */
public class AbsenceLab {

    public static void main(String[] args) {
        UserDirectory dir = new UserDirectory();

        User grace = dir.findByName("grace");
        System.out.println(grace.getName() + " reports to " + grace.getManager().getName());

        User ghost = dir.findByName("casper");
        System.out.println("looked up: " + ghost);
        System.out.println("emailing " + ghost.getEmail());     // boom — but why here?

        // [MARKER 1] Part 3: the four ways out of an Optional.

        // [MARKER 2] Part 4: the management-chain pipeline.

        // [MARKER 3] Part 5: the anti-pattern, refactored.
    }
}
