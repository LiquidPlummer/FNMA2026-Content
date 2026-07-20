package com.curriculum.labs;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueriesApplication implements CommandLineRunner {

    private final InvoiceRepository invoices;

    public QueriesApplication(InvoiceRepository invoices) {
        this.invoices = invoices;
    }

    public static void main(String[] args) {
        SpringApplication.run(QueriesApplication.class, args);
    }

    @Override
    public void run(String... args) {
        seed();

        // Part 1 onward: exercise your new query methods here.
    }

    /** Seed data. Part 3 has you rework this once Customer becomes an entity. */
    private void seed() {
        Invoice a = new Invoice("Globex Corp", new BigDecimal("1200.00"), LocalDate.of(2026, 6, 1));
        a.setStatus(InvoiceStatus.PAID);
        Invoice b = new Invoice("Globex Corp", new BigDecimal("450.50"), LocalDate.of(2026, 6, 20));
        b.setStatus(InvoiceStatus.SENT);
        Invoice c = new Invoice("Initech", new BigDecimal("9800.00"), LocalDate.of(2026, 7, 2));
        c.setStatus(InvoiceStatus.SENT);
        Invoice d = new Invoice("Initech", new BigDecimal("120.00"), LocalDate.of(2026, 7, 10));
        Invoice e = new Invoice("Hooli", new BigDecimal("3300.00"), LocalDate.of(2026, 7, 12));
        e.setStatus(InvoiceStatus.PAID);

        invoices.save(a);
        invoices.save(b);
        invoices.save(c);
        invoices.save(d);
        invoices.save(e);
    }
}
