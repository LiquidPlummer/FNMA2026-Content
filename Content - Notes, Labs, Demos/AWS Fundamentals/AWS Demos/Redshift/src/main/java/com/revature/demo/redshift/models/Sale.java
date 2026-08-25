package com.revature.demo.redshift.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Sale {
    private int saleId;
    private int storeId;
    private int productId;
    private LocalDate saleDate;
    private int quantity;
    private BigDecimal total;

    public Sale(int saleId, int storeId, int productId, LocalDate saleDate, int quantity, BigDecimal total) {
        this.saleId = saleId;
        this.storeId = storeId;
        this.productId = productId;
        this.saleDate = saleDate;
        this.quantity = quantity;
        this.total = total;
    }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    @Override
    public String toString() {
        return "Sale{saleId=" + saleId + ", storeId=" + storeId + ", productId=" + productId
                + ", saleDate=" + saleDate + ", quantity=" + quantity + ", total=" + total + "}";
    }
}