package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@JsonPropertyOrder({"period", "customerCount", "transactionCount", "totalSpent", "totalPoints", "customers"})
@Schema(description = "Reward points for every customer in the requested window.")
public class RewardsReportResponse {

    private final RewardPeriodResponse period;
    private final int customerCount;
    private final int transactionCount;
    private final BigDecimal totalSpent;
    private final long totalPoints;
    private final List<CustomerRewardResponse> customers;

    public RewardsReportResponse(
            RewardPeriodResponse period,
            int customerCount,
            int transactionCount,
            BigDecimal totalSpent,
            long totalPoints,
            List<CustomerRewardResponse> customers) {
        this.period = period;
        this.customerCount = customerCount;
        this.transactionCount = transactionCount;
        this.totalSpent = totalSpent;
        this.totalPoints = totalPoints;
        this.customers = customers;
    }

    public RewardPeriodResponse getPeriod() {
        return period;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    @JsonSerialize(using = CurrencyJsonSerializer.class)
    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public List<CustomerRewardResponse> getCustomers() {
        return customers;
    }
}
