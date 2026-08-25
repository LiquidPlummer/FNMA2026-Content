package com.curriculum.labs;

import java.util.List;

/**
 * Sort Lab — natural ordering, comparators, and the sorted collections.
 */
public class SortLab {

    static void show(String heading, List<Product> products) {
        System.out.println("--- " + heading + " ---");
        products.forEach(System.out::println);
        System.out.println();
    }

    public static void main(String[] args) {
        List<Product> products = Catalog.sample();
        show("as loaded", products);

        // [MARKER 1] Part 1: natural-order sort (needs Comparable first).

        // [MARKER 2] Part 2: the three comparator views.

        // [MARKER 3] Part 3: max, and binarySearch done right and wrong.

        // [MARKER 4] Part 4: the TreeSet experiment.
    }
}
