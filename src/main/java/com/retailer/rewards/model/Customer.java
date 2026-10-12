package com.retailer.rewards.model;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class Customer {

    private static final Pattern CUSTOMER_ID = Pattern.compile("^C\\d{4}$");
    private static final Pattern PERSON_NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{0,39}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String customerId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final LocalDate enrolledOn;

    public Customer(String customerId, String firstName, String lastName, String email, LocalDate enrolledOn) {
        this.customerId = requireCustomerId(customerId);
        this.firstName = requireName(firstName, "First name");
        this.lastName = requireName(lastName, "Last name");
        this.email = requireEmail(email);
        this.enrolledOn = DomainChecks.required(enrolledOn, "Enrolled date");
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    @Override
    public String toString() {
        return "Customer{customerId='" + customerId + "'}";
    }

    private static String requireCustomerId(String customerId) {
        String normalizedCustomerId = DomainChecks.requiredText(customerId, "Customer id");
        if (!CUSTOMER_ID.matcher(normalizedCustomerId).matches()) {
            throw new IllegalArgumentException("Customer id must match C followed by 4 digits.");
        }
        return normalizedCustomerId;
    }

    private static String requireName(String name, String fieldName) {
        String normalizedName = DomainChecks.requiredText(name, fieldName);
        if (!PERSON_NAME.matcher(normalizedName).matches()) {
            throw new IllegalArgumentException(fieldName + " contains unsupported characters.");
        }
        return normalizedName;
    }

    private static String requireEmail(String email) {
        String normalizedEmail = DomainChecks.requiredText(email, "Email");
        if (normalizedEmail.length() > 80 || !EMAIL.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        return normalizedEmail;
    }
}
