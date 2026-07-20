package com.curriculum.labs;

/**
 * A home-grown functional interface: one abstract method, one shape of
 * behavior (Order in, boolean out). Part 2 retires it in favor of the
 * standard kit — but it's exactly how Comparator and Runnable work.
 */
public interface OrderCheck {
    boolean test(Order order);
}
