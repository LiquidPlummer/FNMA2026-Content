package com.curriculum.labs;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Material for exercises 3 and 4 — do NOT fix anything here until you get there.
 *
 * The first three tests all PASS. That's the point: each one is bad in a way
 * that has nothing to do with pass/fail. Exercise 3 asks you to name each
 * smell and rewrite the test properly.
 *
 * The last test is disabled. Exercise 4 asks you to enable it and find out
 * why it fails — by hand, before you run it.
 */
class ReviewExercisesTest {

    // Smell #1 — passes today. What happens the day someone renames a record
    // component, or the day this runs on a JVM that formats doubles differently?
    // What is this test actually claiming about the *behavior* of preview()?
    @Test
    void previewProducesExpectedOutput() {
        PromoCode promo = new PromoCode("SAVE20", 20, 100);

        PromoCode.Discount discount = promo.preview(200.0);

        assertEquals("Discount[code=SAVE20, amountOff=40.0, finalTotal=160.0]",
                discount.toString());
    }

    // Smell #2 — passes today. When this fails six months from now, which of
    // its six claims broke? What is the one behavior under test here?
    @Test
    void promoCodeWorks() {
        PromoCode promo = new PromoCode(" save15 ", 15, 30);
        assertEquals("SAVE15", promo.getCode());
        assertEquals(15, promo.getPercentOff(), 0.001);
        assertEquals(30, promo.getMinimumOrder(), 0.001);
        assertEquals(85.0, promo.apply(100.0), 0.001);
        assertEquals(15.0, promo.preview(100.0).amountOff(), 0.001);
        assertThrows(IllegalArgumentException.class, () -> promo.apply(-1));
    }

    // Smell #3 — passes today. Look closely at where the expected value comes
    // from. If the formula in PromoCode.apply() were wrong, would this fail?
    @Test
    void discountMatchesFormula() {
        PromoCode promo = new PromoCode("FLASH", 12.5, 0);

        double actual = promo.apply(150.0);

        double expected = 150.0 * (1 - 12.5 / 100.0);
        assertEquals(expected, actual, 0.001);
    }

    // Exercise 4 — enable this, but before running it: compute the correct
    // answer with pencil and paper. One of you is wrong, the test or the math.
    @Disabled("exercise 4 — enable, hand-compute, and fix")
    @Test
    void twelvePercentComesOffAnEightyDollarOrder() {
        PromoCode promo = new PromoCode("SAVE12", 12, 25);

        double discounted = promo.apply(80.0);

        assertEquals(70.60, discounted, 0.01);
    }
}
