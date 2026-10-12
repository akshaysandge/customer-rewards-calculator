package com.retailer.rewards.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class Purchase {

    private static final Pattern TRANSACTION_ID = Pattern.compile("^T\\d{5}$");
    private static final Pattern CUSTOMER_ID = Pattern.compile("^C\\d{4}$");
    private static final int MONEY_SCALE = 2;
    private static final int MAX_DESCRIPTION_LENGTH = 120;

    private final String transactionId;
    private final String customerId;
    private final LocalDate transactionDate;
    private final BigDecimal amount;
    private final PurchaseCategory category;
    private final String description;

    public Purchase(
            String transactionId,
            String customerId,
            LocalDate transactionDate,
            BigDecimal amount,
            PurchaseCategory category,
            String description) {
        this.transactionId = requireTransactionId(transactionId);
        this.customerId = requireCustomerId(customerId);
        this.transactionDate = DomainChecks.required(transactionDate, "Transaction date");
        this.amount = requireAmount(amount);
        this.category = DomainChecks.required(category, "Category");
        this.description = requireDescription(description);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PurchaseCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Purchase{transactionId='" + transactionId + "', customerId='" + customerId
                + "', transactionDate=" + transactionDate + ", amount=" + amount + "}";
    }

    private static String requireTransactionId(String transactionId) {
        String normalizedTransactionId = DomainChecks.requiredText(transactionId, "Transaction id");
        if (!TRANSACTION_ID.matcher(normalizedTransactionId).matches()) {
            throw new IllegalArgumentException("Transaction id must match T followed by 5 digits.");
        }
        return normalizedTransactionId;
    }

    private static String requireCustomerId(String customerId) {
        String normalizedCustomerId = DomainChecks.requiredText(customerId, "Customer id");
        if (!CUSTOMER_ID.matcher(normalizedCustomerId).matches()) {
            throw new IllegalArgumentException("Customer id must match C followed by 4 digits.");
        }
        return normalizedCustomerId;
    }

    private static BigDecimal requireAmount(BigDecimal amount) {
        BigDecimal purchaseAmount = DomainChecks.required(amount, "Purchase amount");
        if (purchaseAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be negative.");
        }
        if (purchaseAmount.scale() > MONEY_SCALE) {
            throw new IllegalArgumentException("Purchase amount cannot have more than 2 decimal places.");
        }
        return purchaseAmount.setScale(MONEY_SCALE);
    }

    private static String requireDescription(String description) {
        String normalizedDescription = DomainChecks.requiredText(description, "Description");
        if (normalizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
        }
        return normalizedDescription;
    }
}
