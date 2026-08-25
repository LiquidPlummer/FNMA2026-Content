package com.curriculum.labs;

/**
 * Bill Split Lab — starts as one long, working {@code main} and gets
 * refactored into well-named methods piece by piece.
 *
 * Follow the README's guided walkthrough. Each part tells you which block
 * below to extract into a new method, and what to call it.
 */
public class BillSplitLab {

    // Used by the exercises at the end — a second, still-monolithic method
    // waiting for the same treatment. Don't touch it until you get there.
    static final int[] TEST_SCORES = {88, 72, 95, 64, 79, 100, 91};

    public static void main(String[] args) {
        System.out.println("=== Bill Split Lab ===");

        // ============================================================
        // [MARKER 1] Scenario 1: dinner for four.
        // Everything below (through the blank line) computes tax, tip,
        // total, and a per-person split for one bill, then prints it.
        // ============================================================
        double subtotal = 84.50;
        double taxRate = 8.25;   // percent
        double tipRate = 18;     // percent
        int diners = 4;

        double tax = subtotal * taxRate / 100.0;
        tax = Math.round(tax * 100) / 100.0;

        double tip = subtotal * tipRate / 100.0;
        tip = Math.round(tip * 100) / 100.0;

        double total = subtotal + tax + tip;
        total = Math.round(total * 100) / 100.0;

        double perPersonTotal = total / diners;
        perPersonTotal = Math.round(perPersonTotal * 100) / 100.0;

        double perPersonTip = tip / diners;
        perPersonTip = Math.round(perPersonTip * 100) / 100.0;

        System.out.println();
        System.out.println("Dinner for four");
        System.out.println("  Subtotal:   $" + subtotal);
        System.out.println("  Tax:        $" + tax);
        System.out.println("  Tip:        $" + tip);
        System.out.println("  Total:      $" + total);
        System.out.println("  Per person: $" + perPersonTotal + " (includes $" + perPersonTip + " tip)");

        // ============================================================
        // [MARKER 2] Scenario 2: solo lunch — the exact same math again,
        // copy-pasted with different numbers. This duplication is the
        // smell we're refactoring away.
        // ============================================================
        double subtotal2 = 13.75;
        double taxRate2 = 8.25;
        double tipRate2 = 20;
        int diners2 = 1;

        double tax2 = subtotal2 * taxRate2 / 100.0;
        tax2 = Math.round(tax2 * 100) / 100.0;

        double tip2 = subtotal2 * tipRate2 / 100.0;
        tip2 = Math.round(tip2 * 100) / 100.0;

        double total2 = subtotal2 + tax2 + tip2;
        total2 = Math.round(total2 * 100) / 100.0;

        double perPersonTotal2 = total2 / diners2;
        perPersonTotal2 = Math.round(perPersonTotal2 * 100) / 100.0;

        double perPersonTip2 = tip2 / diners2;
        perPersonTip2 = Math.round(perPersonTip2 * 100) / 100.0;

        System.out.println();
        System.out.println("Solo lunch");
        System.out.println("  Subtotal:   $" + subtotal2);
        System.out.println("  Tax:        $" + tax2);
        System.out.println("  Tip:        $" + tip2);
        System.out.println("  Total:      $" + total2);
        System.out.println("  Per person: $" + perPersonTotal2 + " (includes $" + perPersonTip2 + " tip)");
    }

    // [MARKER 3] Exercise-only: a second monolith, left as-is for now.
    // See the Exercises section of the README before touching this one.
    static void reportStats(int[] scores) {
        int min = scores[0];
        int max = scores[0];
        int sum = 0;
        for (int score : scores) {
            if (score < min) min = score;
            if (score > max) max = score;
            sum += score;
        }
        double average = sum / (double) scores.length;

        System.out.println();
        System.out.println("Test score report");
        System.out.println("  Count:   " + scores.length);
        System.out.println("  Min:     " + min);
        System.out.println("  Max:     " + max);
        System.out.println("  Average: " + average);
    }
}
