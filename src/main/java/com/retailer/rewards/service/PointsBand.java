package com.retailer.rewards.service;

public final class PointsBand {

    private final String rule;
    private final long dollars;
    private final int pointsPerDollar;
    private final long points;

    public PointsBand(String rule, long dollars, int pointsPerDollar, long points) {
        this.rule = rule;
        this.dollars = dollars;
        this.pointsPerDollar = pointsPerDollar;
        this.points = points;
    }

    public String getRule() {
        return rule;
    }

    public long getDollars() {
        return dollars;
    }

    public int getPointsPerDollar() {
        return pointsPerDollar;
    }

    public long getPoints() {
        return points;
    }
}
