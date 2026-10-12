package com.retailer.rewards.service;

import com.retailer.rewards.model.RewardTier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardPointsCalculatorTest {

    private RewardPointsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RewardPointsCalculator(Arrays.asList(
                new RewardTier(100L, 2),
                new RewardTier(50L, 1)));
    }

    @ParameterizedTest
    @CsvSource({
            "0.00, 0, 0",
            "49.99, 0, 49",
            "50.00, 0, 50",
            "50.01, 0, 50",
            "51.00, 1, 51",
            "75.50, 25, 75",
            "100.00, 50, 100",
            "100.99, 50, 100",
            "101.00, 52, 101",
            "120.00, 90, 120",
            "120.75, 90, 120",
            "120.999, 90, 120",
            "200.00, 250, 200",
            "250.75, 350, 250",
            "500.00, 850, 500"
    })
    void awardsMarginalPointsOnWholeDollars(String amount, long expectedPoints, long expectedWholeDollars) {
        CalculatedPoints calculatedPoints = calculator.calculate(new BigDecimal(amount));
        assertEquals(expectedPoints, calculatedPoints.getPoints());
        assertEquals(expectedWholeDollars, calculatedPoints.getWholeDollarAmount());
    }

    @Test
    void splitsOneHundredTwentyDollarsIntoThePublishedBands() {
        CalculatedPoints calculatedPoints = calculator.calculate(new BigDecimal("120.00"));

        assertEquals(90L, calculatedPoints.getPoints());
        assertEquals(2, calculatedPoints.getBands().size());
        assertEquals("2 points for each whole dollar over $100", calculatedPoints.getBands().get(0).getRule());
        assertEquals(20L, calculatedPoints.getBands().get(0).getDollars());
        assertEquals(2, calculatedPoints.getBands().get(0).getPointsPerDollar());
        assertEquals(40L, calculatedPoints.getBands().get(0).getPoints());
        assertEquals("1 point for each whole dollar over $50 up to $100", calculatedPoints.getBands().get(1).getRule());
        assertEquals(50L, calculatedPoints.getBands().get(1).getDollars());
        assertEquals(1, calculatedPoints.getBands().get(1).getPointsPerDollar());
        assertEquals(50L, calculatedPoints.getBands().get(1).getPoints());
    }

    @Test
    void leavesAnEmptyBandListWhenThePurchaseEarnsNothing() {
        CalculatedPoints calculatedPoints = calculator.calculate(new BigDecimal("49.99"));
        assertTrue(calculatedPoints.getBands().isEmpty());
    }

    @Test
    void publishesTheProgramRulesInReadingOrder() {
        assertEquals(Arrays.asList(
                "1 point for each whole dollar over $50 up to $100.",
                "2 points for each whole dollar over $100.",
                "A whole dollar at or below $50 earns no points.",
                "Cents are truncated before points are calculated.",
                "Points are awarded on each transaction and are not recalculated from the monthly total."
        ), calculator.programRules());
    }

    @Test
    void rejectsANegativeAmount() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(new BigDecimal("-0.01")));
        assertEquals("Purchase amount cannot be negative.", exception.getMessage());
    }

    @Test
    void rejectsAMissingAmount() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(null));
        assertEquals("Purchase amount is required.", exception.getMessage());
    }

    @Test
    void rejectsAnAmountThatDoesNotFitInALong() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(new BigDecimal("100000000000000000000")));
        assertEquals("Purchase amount is too large to score.", exception.getMessage());
    }

    @Test
    void rejectsDuplicateThresholds() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RewardPointsCalculator(Arrays.asList(new RewardTier(50L, 1), new RewardTier(50L, 2))));
        assertEquals("Duplicate reward tier threshold: 50.", exception.getMessage());
    }

    @Test
    void rejectsAnEmptyTierList() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RewardPointsCalculator(Collections.<RewardTier>emptyList()));
        assertEquals("At least one reward tier is required.", exception.getMessage());
    }
}
