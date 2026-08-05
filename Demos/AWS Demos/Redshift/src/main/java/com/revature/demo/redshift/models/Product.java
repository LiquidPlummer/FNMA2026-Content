package com.revature.demo.redshift.models;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private String name;
    private String category;
    private BigDecimal price;

    public Product(int productId, String name, String category, BigDecimal price) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    @Override
    public String toString() {
        return "Product{productId=" + productId + ", name='" + name + "', category='" + category
                + "', price=" + price + "}";
    }
}