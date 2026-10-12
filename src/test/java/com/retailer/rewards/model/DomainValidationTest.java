package com.retailer.rewards.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainValidationTest {

    @Test
    void rejectsAPurchaseWithTooManyDecimalPlaces() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Purchase(
                        "T10011",
                        "C1001",
                        LocalDate.parse("2026-07-04"),
                        new BigDecimal("10.999"),
                        PurchaseCategory.GROCERY,
                        "Too precise"));
        assertEquals("Purchase amount cannot have more than 2 decimal places.", exception.getMessage());
    }

    @Test
    void rejectsANegativePurchase() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Purchase(
                        "T10011",
                        "C1001",
                        LocalDate.parse("2026-07-04"),
                        new BigDecimal("-1.00"),
                        PurchaseCategory.GROCERY,
                        "Refund"));
        assertEquals("Purchase amount cannot be negative.", exception.getMessage());
    }

    @Test
    void rejectsAnInvalidCustomerEmail() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Customer("C1001", "Maya", "Chen", "not-an-email", LocalDate.parse("2024-03-15")));
        assertEquals("Email is not valid.", exception.getMessage());
    }

    @Test
    void rejectsACustomerIdThatDoesNotMatchTheProgramFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Customer("1001", "Maya", "Chen", "maya.chen@example.com", LocalDate.parse("2024-03-15")));
        assertEquals("Customer id must match C followed by 4 digits.", exception.getMessage());
    }
}
