package com.curriculum.labs;

import java.util.ArrayList;
import java.util.List;

/** Sample data. Note: two tools share a price — that's deliberate. */
public class Catalog {

    public static List<Product> sample() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("TL-300", "Torque Wrench", "tools", 89.00));
        products.add(new Product("EL-120", "Multimeter", "electrical", 45.50));
        products.add(new Product("TL-101", "Claw Hammer", "tools", 24.99));
        products.add(new Product("PL-220", "Pipe Cutter", "plumbing", 31.75));
        products.add(new Product("EL-045", "Wire Stripper", "electrical", 12.40));
        products.add(new Product("TL-205", "Socket Set", "tools", 89.00));
        products.add(new Product("PL-118", "Basin Wrench", "plumbing", 18.20));
        return products;
    }
}
