package com.retailer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({"rule", "dollars", "pointsPerDollar", "points"})
@Schema(description = "How many whole dollars of a purchase fell into one reward band.")
public class PointsBandResponse {

    private final String rule;
    private final long dollars;
    private final int pointsPerDollar;
    private final long points;

    public PointsBandResponse(String rule, long dollars, int pointsPerDollar, long points) {
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
