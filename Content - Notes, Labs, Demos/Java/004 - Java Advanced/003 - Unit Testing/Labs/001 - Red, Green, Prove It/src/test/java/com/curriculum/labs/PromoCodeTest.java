package com.curriculum.labs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromoCodeTest {

    // A leftover from someone's quick experiment. It passes — but if it ever
    // fails, what would its name tell you? Part 6 of the walkthrough deals with it.
    @Test
    void test1() {
        assertEquals(90.0, new PromoCode("SAVE10", 10, 0).apply(100.0), 0.001);
    }

    // The walkthrough's tests go below, starting with Part 1.
}
