package com.curriculum.labs;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Carried over from the previous lab. Part 3 of the walkthrough replaces the
 * customerName string with a real Customer relationship.
 */
@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private LocalDate issuedOn;

    protected Invoice() { }

    public Invoice(String customerName, BigDecimal total, LocalDate issuedOn) {
        this.customerName = customerName;
        this.total = total;
        this.issuedOn = issuedOn;
        this.status = InvoiceStatus.DRAFT;
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotal() { return total; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDate getIssuedOn() { return issuedOn; }

    public void setStatus(InvoiceStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Invoice[" + id + ", " + customerName + ", " + total + ", " + status + ", " + issuedOn + "]";
    }
}
