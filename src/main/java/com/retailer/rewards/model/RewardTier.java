package com.retailer.rewards.model;

/**
 * One marginal reward band. Dollars strictly above {@code minimumExclusive} earn
 * {@code pointsPerDollar} until the next higher band, if one exists.
 */
public final class RewardTier {

    private final long minimumExclusive;
    private final int pointsPerDollar;

    public RewardTier(long minimumExclusive, int pointsPerDollar) {
        if (minimumExclusive < 0) {
            throw new IllegalArgumentException("Reward tier threshold cannot be negative.");
        }
        if (pointsPerDollar < 1) {
            throw new IllegalArgumentException("Reward tier must award at least 1 point per dollar.");
        }
        this.minimumExclusive = minimumExclusive;
        this.pointsPerDollar = pointsPerDollar;
    }

    public long getMinimumExclusive() {
        return minimumExclusive;
    }

    public int getPointsPerDollar() {
        return pointsPerDollar;
    }
}
