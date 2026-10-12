package com.retailer.rewards.service;

import com.retailer.rewards.config.RewardsProperties;
import com.retailer.rewards.exception.InvalidRewardQueryException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class RewardWindowResolver {

    private final RewardsProperties rewardsProperties;
    private final Clock businessClock;

    public RewardWindowResolver(RewardsProperties rewardsProperties, Clock businessClock) {
        this.rewardsProperties = rewardsProperties;
        this.businessClock = businessClock;
    }

    public RewardWindow resolve(Integer months, LocalDate asOfDate, LocalDate startDate, LocalDate endDate) {
        boolean hasExplicitRange = startDate != null || endDate != null;
        boolean hasRelativeWindow = months != null || asOfDate != null;
        if (hasExplicitRange && hasRelativeWindow) {
            throw new InvalidRewardQueryException(
                    "Use either from and to, or months and asOf. Do not combine them.");
        }
        if (hasExplicitRange) {
            return explicitWindow(startDate, endDate);
        }
        return relativeWindow(months, asOfDate);
    }

    private RewardWindow explicitWindow(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidRewardQueryException("Both from and to are required for an explicit date range.");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidRewardQueryException("Parameter from must be on or before to.");
        }
        long inclusiveDaySpan = ChronoUnit.DAYS.between(startDate, endDate);
        if (inclusiveDaySpan > rewardsProperties.getMaxRangeDays()) {
            throw new InvalidRewardQueryException(
                    "Date range cannot exceed " + rewardsProperties.getMaxRangeDays() + " days.");
        }
        return new RewardWindow(startDate, endDate);
    }

    private RewardWindow relativeWindow(Integer months, LocalDate asOfDate) {
        int monthCount = months == null ? rewardsProperties.getDefaultMonths() : months.intValue();
        if (monthCount < 1 || monthCount > rewardsProperties.getMaxMonths()) {
            throw new InvalidRewardQueryException(
                    "Parameter months must be between 1 and " + rewardsProperties.getMaxMonths() + ".");
        }
        LocalDate endDate = asOfDate == null ? LocalDate.now(businessClock) : asOfDate;
        LocalDate startDate = endDate.withDayOfMonth(1).minusMonths(monthCount - 1L);
        return new RewardWindow(startDate, endDate);
    }
}
