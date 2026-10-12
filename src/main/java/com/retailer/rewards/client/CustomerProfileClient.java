package com.retailer.rewards.client;

import com.retailer.rewards.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CustomerProfileClient {

    CompletableFuture<Optional<Customer>> findById(String customerId);

    CompletableFuture<List<Customer>> findAll();
}
