package com.retailer.rewards.client;

import com.retailer.rewards.model.Purchase;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TransactionHistoryClient {

    CompletableFuture<List<Purchase>> findByCustomerAndDateRange(
            String customerId,
            LocalDate startDate,
            LocalDate endDate);
}
