package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.retailer.rewards.service.RewardWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@JsonPropertyOrder({"startDate", "endDate", "monthCount"})
@Schema(description = "Inclusive transaction window used for this calculation.")
public class RewardPeriodResponse {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final long monthCount;

    public RewardPeriodResponse(LocalDate startDate, LocalDate endDate, long monthCount) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthCount = monthCount;
    }

    public static RewardPeriodResponse from(RewardWindow rewardWindow) {
        long monthCount = ChronoUnit.MONTHS.between(
                YearMonth.from(rewardWindow.getStartDate()),
                YearMonth.from(rewardWindow.getEndDate())) + 1L;
        return new RewardPeriodResponse(rewardWindow.getStartDate(), rewardWindow.getEndDate(), monthCount);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getMonthCount() {
        return monthCount;
    }
}
