package com.curriculum.labs;

import org.springframework.data.jpa.repository.JpaRepository;

/** The walkthrough grows this interface one method at a time. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
