package com.retailer.rewards.service;

import com.retailer.rewards.model.RewardTier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Awards points on each purchase by itself. Dollars are truncated toward zero, then split
 * across marginal bands. A $120 purchase is 2 points on the $20 over $100 plus 1 point on
 * the $50 between $50 and $100, which is 90 points. Two $60 purchases stay 10 + 10 and are
 * never combined into a $120 purchase.
 */
public final class RewardPointsCalculator {

    private final List<RewardTier> tiersDescending;
    private final Map<Long, String> ruleByThreshold;
    private final List<String> programRules;

    public RewardPointsCalculator(List<RewardTier> rewardTiers) {
        if (rewardTiers == null || rewardTiers.isEmpty()) {
            throw new IllegalArgumentException("At least one reward tier is required.");
        }
        List<RewardTier> sortedDescending = new ArrayList<RewardTier>(rewardTiers);
        Collections.sort(sortedDescending, Comparator.comparingLong(RewardTier::getMinimumExclusive).reversed());
        rejectDuplicateThresholds(sortedDescending);
        this.tiersDescending = Collections.unmodifiableList(sortedDescending);
        this.ruleByThreshold = Collections.unmodifiableMap(describeTiers(sortedDescending));
        this.programRules = buildProgramRules(sortedDescending);
    }

    public CalculatedPoints calculate(BigDecimal purchaseAmount) {
        if (purchaseAmount == null) {
            throw new IllegalArgumentException("Purchase amount is required.");
        }
        if (purchaseAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be negative.");
        }

        long wholeDollars = wholeDollars(purchaseAmount);
        long remainingDollars = wholeDollars;
        long totalPoints = 0L;
        List<PointsBand> bands = new ArrayList<PointsBand>();

        for (RewardTier rewardTier : tiersDescending) {
            if (remainingDollars <= rewardTier.getMinimumExclusive()) {
                continue;
            }
            long bandDollars = remainingDollars - rewardTier.getMinimumExclusive();
            long bandPoints = multiply(bandDollars, rewardTier.getPointsPerDollar());
            totalPoints = add(totalPoints, bandPoints);
            bands.add(new PointsBand(
                    ruleByThreshold.get(Long.valueOf(rewardTier.getMinimumExclusive())),
                    bandDollars,
                    rewardTier.getPointsPerDollar(),
                    bandPoints));
            remainingDollars = rewardTier.getMinimumExclusive();
        }

        return new CalculatedPoints(totalPoints, wholeDollars, bands);
    }

    public List<String> programRules() {
        return programRules;
    }

    private static long wholeDollars(BigDecimal purchaseAmount) {
        try {
            return purchaseAmount.setScale(0, RoundingMode.DOWN).longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Purchase amount is too large to score.", exception);
        }
    }

    private static long multiply(long dollars, int pointsPerDollar) {
        try {
            return Math.multiplyExact(dollars, pointsPerDollar);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Purchase amount is too large to score.", exception);
        }
    }

    private static long add(long totalPoints, long bandPoints) {
        try {
            return Math.addExact(totalPoints, bandPoints);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Purchase amount is too large to score.", exception);
        }
    }

    private static void rejectDuplicateThresholds(List<RewardTier> sortedDescending) {
        Long previousThreshold = null;
        for (RewardTier rewardTier : sortedDescending) {
            Long threshold = Long.valueOf(rewardTier.getMinimumExclusive());
            if (threshold.equals(previousThreshold)) {
                throw new IllegalArgumentException("Duplicate reward tier threshold: " + threshold + ".");
            }
            previousThreshold = threshold;
        }
    }

    private static Map<Long, String> describeTiers(List<RewardTier> sortedDescending) {
        List<RewardTier> ascending = new ArrayList<RewardTier>(sortedDescending);
        Collections.reverse(ascending);
        Map<Long, String> descriptions = new LinkedHashMap<Long, String>();
        for (int index = 0; index < ascending.size(); index++) {
            RewardTier rewardTier = ascending.get(index);
            RewardTier nextHigherTier = index + 1 < ascending.size() ? ascending.get(index + 1) : null;
            descriptions.put(Long.valueOf(rewardTier.getMinimumExclusive()), describe(rewardTier, nextHigherTier));
        }
        return descriptions;
    }

    private static List<String> buildProgramRules(List<RewardTier> sortedDescending) {
        List<RewardTier> ascending = new ArrayList<RewardTier>(sortedDescending);
        Collections.reverse(ascending);
        List<String> rules = new ArrayList<String>();
        for (int index = 0; index < ascending.size(); index++) {
            RewardTier rewardTier = ascending.get(index);
            RewardTier nextHigherTier = index + 1 < ascending.size() ? ascending.get(index + 1) : null;
            rules.add(describe(rewardTier, nextHigherTier) + ".");
        }
        rules.add("A whole dollar at or below $" + ascending.get(0).getMinimumExclusive() + " earns no points.");
        rules.add("Cents are truncated before points are calculated.");
        rules.add("Points are awarded on each transaction and are not recalculated from the monthly total.");
        return Collections.unmodifiableList(rules);
    }

    private static String describe(RewardTier rewardTier, RewardTier nextHigherTier) {
        String pointLabel = rewardTier.getPointsPerDollar() == 1 ? "point" : "points";
        if (nextHigherTier == null) {
            return rewardTier.getPointsPerDollar() + " " + pointLabel
                    + " for each whole dollar over $" + rewardTier.getMinimumExclusive();
        }
        return rewardTier.getPointsPerDollar() + " " + pointLabel
                + " for each whole dollar over $" + rewardTier.getMinimumExclusive()
                + " up to $" + nextHigherTier.getMinimumExclusive();
    }
}
