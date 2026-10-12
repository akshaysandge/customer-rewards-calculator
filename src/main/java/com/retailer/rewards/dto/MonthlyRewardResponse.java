package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@JsonPropertyOrder({"year", "month", "monthLabel", "transactionCount", "totalSpent", "points"})
@Schema(description = "Points and spend for one calendar month inside the requested window.")
public class MonthlyRewardResponse {

    private final int year;
    private final int month;
    private final String monthLabel;
    private final int transactionCount;
    private final BigDecimal totalSpent;
    private final long points;

    public MonthlyRewardResponse(
            int year,
            int month,
            String monthLabel,
            int transactionCount,
            BigDecimal totalSpent,
            long points) {
        this.year = year;
        this.month = month;
        this.monthLabel = monthLabel;
        this.transactionCount = transactionCount;
        this.totalSpent = totalSpent;
        this.points = points;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    @JsonSerialize(using = CurrencyJsonSerializer.class)
    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public long getPoints() {
        return points;
    }
}
