package com.retailer.rewards.service;

import java.util.Collections;
import java.util.List;

public final class CalculatedPoints {

    private final long points;
    private final long wholeDollarAmount;
    private final List<PointsBand> bands;

    public CalculatedPoints(long points, long wholeDollarAmount, List<PointsBand> bands) {
        this.points = points;
        this.wholeDollarAmount = wholeDollarAmount;
        this.bands = Collections.unmodifiableList(bands);
    }

    public long getPoints() {
        return points;
    }

    public long getWholeDollarAmount() {
        return wholeDollarAmount;
    }

    public List<PointsBand> getBands() {
        return bands;
    }
}
