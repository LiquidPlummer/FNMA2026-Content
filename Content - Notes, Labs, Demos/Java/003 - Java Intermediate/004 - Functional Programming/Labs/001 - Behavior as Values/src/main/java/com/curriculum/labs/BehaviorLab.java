package com.curriculum.labs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Behavior Lab — from anonymous-class ceremony to lambdas, method
 * references, and behavior passed as plain values.
 *
 * The three anonymous classes below are the "before" picture. Part 1
 * rewrites them; nothing else in this file is sacred either.
 */
public class BehaviorLab {

    public static void main(String[] args) {
        List<Order> orders = new ArrayList<>(Order.sample());

        // Anonymous class #1: a check
        OrderCheck bigOrder = new OrderCheck() {
            @Override
            public boolean test(Order order) {
                return order.amount() > 1000;
            }
        };

        // Anonymous class #2: an ordering
        Comparator<Order> byAmount = new Comparator<Order>() {
            @Override
            public int compare(Order a, Order b) {
                return Double.compare(a.amount(), b.amount());
            }
        };

        // Anonymous class #3: a task
        Runnable banner = new Runnable() {
            @Override
            public void run() {
                System.out.println("=== Order Report ===");
            }
        };

        banner.run();
        orders.sort(byAmount);
        for (Order o : orders) {
            if (bigOrder.test(o)) {
                System.out.println("BIG  " + o);
            } else {
                System.out.println("     " + o);
            }
        }

        // [MARKER 1] Part 2: the select(...) experiments.

        // [MARKER 2] Part 3: the standard-kit tour.

        // [MARKER 3] Part 4: composition.

        // [MARKER 4] Part 6: the capture experiment.
    }

    // [MARKER 5] Part 2: the select(...) method goes here.
}
