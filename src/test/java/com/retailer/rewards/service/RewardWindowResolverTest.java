package com.retailer.rewards.service;

import com.retailer.rewards.config.RewardsProperties;
import com.retailer.rewards.exception.InvalidRewardQueryException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RewardWindowResolverTest {

    private RewardWindowResolver rewardWindowResolver;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-12T15:00:00Z"), ZoneOffset.UTC);
        rewardWindowResolver = new RewardWindowResolver(new RewardsProperties(), fixedClock);
    }

    @Test
    void defaultsToThreeCalendarMonthsThroughToday() {
        RewardWindow rewardWindow = rewardWindowResolver.resolve(null, null, null, null);
        assertEquals(LocalDate.parse("2026-08-01"), rewardWindow.getStartDate());
        assertEquals(LocalDate.parse("2026-10-12"), rewardWindow.getEndDate());
    }

    @Test
    void buildsTheThreeMonthScenarioFromAnAsOfDate() {
        RewardWindow rewardWindow = rewardWindowResolver.resolve(Integer.valueOf(3), LocalDate.parse("2026-09-30"), null, null);
        assertEquals(LocalDate.parse("2026-07-01"), rewardWindow.getStartDate());
        assertEquals(LocalDate.parse("2026-09-30"), rewardWindow.getEndDate());
    }

    @Test
    void usesOnlyTheMonthThatContainsAsOfWhenMonthsIsOne() {
        RewardWindow rewardWindow = rewardWindowResolver.resolve(Integer.valueOf(1), LocalDate.parse("2026-09-15"), null, null);
        assertEquals(LocalDate.parse("2026-09-01"), rewardWindow.getStartDate());
        assertEquals(LocalDate.parse("2026-09-15"), rewardWindow.getEndDate());
    }

    @Test
    void acceptsAnExplicitInclusiveRange() {
        RewardWindow rewardWindow = rewardWindowResolver.resolve(
                null,
                null,
                LocalDate.parse("2026-07-01"),
                LocalDate.parse("2026-09-30"));
        assertEquals(LocalDate.parse("2026-07-01"), rewardWindow.getStartDate());
        assertEquals(LocalDate.parse("2026-09-30"), rewardWindow.getEndDate());
    }

    @Test
    void rejectsAMixOfRelativeAndExplicitParameters() {
        InvalidRewardQueryException exception = assertThrows(
                InvalidRewardQueryException.class,
                () -> rewardWindowResolver.resolve(
                        Integer.valueOf(3),
                        null,
                        LocalDate.parse("2026-07-01"),
                        LocalDate.parse("2026-09-30")));
        assertEquals("Use either from and to, or months and asOf. Do not combine them.", exception.getMessage());
    }

    @Test
    void rejectsAPartialExplicitRange() {
        InvalidRewardQueryException exception = assertThrows(
                InvalidRewardQueryException.class,
                () -> rewardWindowResolver.resolve(null, null, LocalDate.parse("2026-07-01"), null));
        assertEquals("Both from and to are required for an explicit date range.", exception.getMessage());
    }

    @Test
    void rejectsAStartDateAfterTheEndDate() {
        InvalidRewardQueryException exception = assertThrows(
                InvalidRewardQueryException.class,
                () -> rewardWindowResolver.resolve(
                        null,
                        null,
                        LocalDate.parse("2026-09-30"),
                        LocalDate.parse("2026-07-01")));
        assertEquals("Parameter from must be on or before to.", exception.getMessage());
    }

    @Test
    void rejectsAMonthCountOutsideTheConfiguredLimit() {
        InvalidRewardQueryException exception = assertThrows(
                InvalidRewardQueryException.class,
                () -> rewardWindowResolver.resolve(Integer.valueOf(0), null, null, null));
        assertEquals("Parameter months must be between 1 and 36.", exception.getMessage());
    }

    @Test
    void rejectsARangeLongerThanTheConfiguredMaximum() {
        InvalidRewardQueryException exception = assertThrows(
                InvalidRewardQueryException.class,
                () -> rewardWindowResolver.resolve(
                        null,
                        null,
                        LocalDate.parse("2020-01-01"),
                        LocalDate.parse("2026-09-30")));
        assertEquals("Date range cannot exceed 1126 days.", exception.getMessage());
    }
}
