package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.retailer.rewards.model.PurchaseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonPropertyOrder({
        "transactionId",
        "transactionDate",
        "amount",
        "wholeDollarAmount",
        "category",
        "description",
        "points",
        "bands"
})
@Schema(description = "One purchase and the points it earned on its own.")
public class TransactionRewardResponse {

    private final String transactionId;
    private final LocalDate transactionDate;
    private final BigDecimal amount;
    private final long wholeDollarAmount;
    private final PurchaseCategory category;
    private final String description;
    private final long points;
    private final List<PointsBandResponse> bands;

    public TransactionRewardResponse(
            String transactionId,
            LocalDate transactionDate,
            BigDecimal amount,
            long wholeDollarAmount,
            PurchaseCategory category,
            String description,
            long points,
            List<PointsBandResponse> bands) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.wholeDollarAmount = wholeDollarAmount;
        this.category = category;
        this.description = description;
        this.points = points;
        this.bands = bands;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    @JsonSerialize(using = CurrencyJsonSerializer.class)
    public BigDecimal getAmount() {
        return amount;
    }

    public long getWholeDollarAmount() {
        return wholeDollarAmount;
    }

    public PurchaseCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public long getPoints() {
        return points;
    }

    public List<PointsBandResponse> getBands() {
        return bands;
    }
}
