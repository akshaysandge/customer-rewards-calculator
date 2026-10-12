package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.retailer.rewards.model.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@JsonPropertyOrder({"customerId", "firstName", "lastName", "fullName", "email", "enrolledOn"})
@Schema(description = "Customer profile used to explain a reward result.")
public class CustomerProfileResponse {

    private final String customerId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final LocalDate enrolledOn;

    public CustomerProfileResponse(
            String customerId,
            String firstName,
            String lastName,
            String email,
            LocalDate enrolledOn) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enrolledOn = enrolledOn;
    }

    public static CustomerProfileResponse from(Customer customer) {
        return new CustomerProfileResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getEnrolledOn());
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
}
