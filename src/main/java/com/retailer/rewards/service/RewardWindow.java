package com.retailer.rewards.service;

import java.time.LocalDate;

public final class RewardWindow {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public RewardWindow(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Reward window dates are required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Reward window start must be on or before the end.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
