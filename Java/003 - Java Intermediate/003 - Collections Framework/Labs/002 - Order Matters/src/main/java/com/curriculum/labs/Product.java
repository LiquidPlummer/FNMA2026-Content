package com.curriculum.labs;

/**
 * A catalog product. Deliberately not Comparable yet — Part 1 fixes that.
 */
public class Product {
    private final String sku;
    private final String name;
    private final String category;
    private final double price;

    public Product(String sku, String name, String category, double price) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return String.format("%-8s %-14s %-9s %8.2f", sku, name, category, price);
    }
}
