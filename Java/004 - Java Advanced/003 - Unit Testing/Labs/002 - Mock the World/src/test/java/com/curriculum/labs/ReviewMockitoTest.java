package com.curriculum.labs;

import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Material for exercises 2 and 3 — leave it alone until you get there.
 */
@ExtendWith(MockitoExtension.class)
class ReviewMockitoTest {

    @Mock OrderRepository repository;
    @Mock PaymentGateway gateway;
    @InjectMocks OrderService service;

    // Exercise 2 — this test PASSES. It is also a booby trap: it re-describes
    // the current implementation call by call, so nearly any legitimate
    // refactor of checkout() will fail it while the behavior stays correct.
    // Notice what it never asserts: the receipt, or the order's new state.
    @Test
    void checkoutDoesEverythingExactlyLikeTheCurrentImplementation() {
        Order order = new Order("A-9", "cust-1", 50.0, "OPEN");
        when(repository.findById("A-9")).thenReturn(Optional.of(order));
        when(gateway.charge("cust-1", 50.0)).thenReturn(new PaymentResult("TX-1"));

        service.checkout("A-9");

        verify(repository, times(1)).findById("A-9");
        verify(gateway, times(1)).charge("cust-1", 50.0);
        verify(repository, times(1)).save(order);
        verifyNoMoreInteractions(repository, gateway);
    }

    // Exercise 3 — disabled because it BLOWS UP before the assertion is ever
    // reached. Enable it, run it, read the exception name carefully, fix the
    // one broken line, and explain in a comment what the rule is.
    @Disabled("exercise 3 — enable, read the exception, fix")
    @Test
    void chargeOfExactlyFiftyIsStubbed() {
        Order order = new Order("A-9", "cust-1", 50.0, "OPEN");
        when(repository.findById(anyString())).thenReturn(Optional.of(order));
        when(gateway.charge(anyString(), 50.0)).thenReturn(new PaymentResult("TX-1"));

        Receipt receipt = service.checkout("A-9");

        assertEquals("TX-1", receipt.transactionId());
    }
}
