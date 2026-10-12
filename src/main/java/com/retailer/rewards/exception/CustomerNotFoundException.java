package com.retailer.rewards.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("Customer " + customerId + " was not found.");
    }
}
