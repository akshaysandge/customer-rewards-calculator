package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@JsonPropertyOrder({
        "customer",
        "period",
        "programRules",
        "transactionCount",
        "totalSpent",
        "totalPoints",
        "monthlyRewards",
        "transactions"
})
@Schema(description = "Reward points for one customer across the requested window.")
public class CustomerRewardResponse {

    private final CustomerProfileResponse customer;
    private final RewardPeriodResponse period;
    private final List<String> programRules;
    private final int transactionCount;
    private final BigDecimal totalSpent;
    private final long totalPoints;
    private final List<MonthlyRewardResponse> monthlyRewards;
    private final List<TransactionRewardResponse> transactions;

    public CustomerRewardResponse(
            CustomerProfileResponse customer,
            RewardPeriodResponse period,
            List<String> programRules,
            int transactionCount,
            BigDecimal totalSpent,
            long totalPoints,
            List<MonthlyRewardResponse> monthlyRewards,
            List<TransactionRewardResponse> transactions) {
        this.customer = customer;
        this.period = period;
        this.programRules = programRules;
        this.transactionCount = transactionCount;
        this.totalSpent = totalSpent;
        this.totalPoints = totalPoints;
        this.monthlyRewards = monthlyRewards;
        this.transactions = transactions;
    }

    public CustomerProfileResponse getCustomer() {
        return customer;
    }

    public RewardPeriodResponse getPeriod() {
        return period;
    }

    public List<String> getProgramRules() {
        return programRules;
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

    public List<MonthlyRewardResponse> getMonthlyRewards() {
        return monthlyRewards;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<TransactionRewardResponse> getTransactions() {
        return transactions;
    }
}
