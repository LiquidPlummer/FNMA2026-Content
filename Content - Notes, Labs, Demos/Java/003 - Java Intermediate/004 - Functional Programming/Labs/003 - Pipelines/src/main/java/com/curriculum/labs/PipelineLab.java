package com.curriculum.labs;

import java.util.List;

/**
 * Pipeline Lab — the Streams API over a two-dozen-order dataset.
 */
public class PipelineLab {

    public static void main(String[] args) {
        List<Order> orders = OrderData.sample();
        System.out.println("orders loaded: " + orders.size());

        // [MARKER 1] Part 1: the first pipeline, built operator by operator.

        // [MARKER 2] Part 2: the intermediate-operation tour.

        // [MARKER 3] Part 3: terminals — collect, reduce, match, find.

        // [MARKER 4] Part 4: groupingBy and joining.

        // [MARKER 5] Part 5: the loop that stays a loop.

        // [MARKER 6] Part 6: the single-use rule.
    }
}
