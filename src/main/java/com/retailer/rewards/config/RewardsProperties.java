package com.retailer.rewards.config;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rewards")
public class RewardsProperties {

    @Min(1)
    @Max(36)
    private int defaultMonths = 3;

    @Min(1)
    @Max(120)
    private int maxMonths = 36;

    @Min(1)
    private int maxRangeDays = 1126;

    @Min(0)
    @Max(5000)
    private long simulatedLatencyMs = 80L;

    @Min(1)
    @Max(30000)
    private long fetchTimeoutMs = 2000L;

    @Min(1)
    @Max(64)
    private int fetchCorePoolSize = 4;

    @Min(1)
    @Max(64)
    private int fetchMaxPoolSize = 16;

    @Min(1)
    @Max(1000)
    private int fetchQueueCapacity = 100;

    @Valid
    @NotEmpty
    private List<TierProperties> tiers = defaultTiers();

    private static List<TierProperties> defaultTiers() {
        List<TierProperties> defaultTiers = new ArrayList<TierProperties>();
        defaultTiers.add(new TierProperties(100L, 2));
        defaultTiers.add(new TierProperties(50L, 1));
        return defaultTiers;
    }

    public int getDefaultMonths() {
        return defaultMonths;
    }

    public void setDefaultMonths(int defaultMonths) {
        this.defaultMonths = defaultMonths;
    }

    public int getMaxMonths() {
        return maxMonths;
    }

    public void setMaxMonths(int maxMonths) {
        this.maxMonths = maxMonths;
    }

    public int getMaxRangeDays() {
        return maxRangeDays;
    }

    public void setMaxRangeDays(int maxRangeDays) {
        this.maxRangeDays = maxRangeDays;
    }

    public long getSimulatedLatencyMs() {
        return simulatedLatencyMs;
    }

    public void setSimulatedLatencyMs(long simulatedLatencyMs) {
        this.simulatedLatencyMs = simulatedLatencyMs;
    }

    public long getFetchTimeoutMs() {
        return fetchTimeoutMs;
    }

    public void setFetchTimeoutMs(long fetchTimeoutMs) {
        this.fetchTimeoutMs = fetchTimeoutMs;
    }

    public int getFetchCorePoolSize() {
        return fetchCorePoolSize;
    }

    public void setFetchCorePoolSize(int fetchCorePoolSize) {
        this.fetchCorePoolSize = fetchCorePoolSize;
    }

    public int getFetchMaxPoolSize() {
        return fetchMaxPoolSize;
    }

    public void setFetchMaxPoolSize(int fetchMaxPoolSize) {
        this.fetchMaxPoolSize = fetchMaxPoolSize;
    }

    public int getFetchQueueCapacity() {
        return fetchQueueCapacity;
    }

    public void setFetchQueueCapacity(int fetchQueueCapacity) {
        this.fetchQueueCapacity = fetchQueueCapacity;
    }

    public List<TierProperties> getTiers() {
        return tiers;
    }

    public void setTiers(List<TierProperties> tiers) {
        this.tiers = tiers;
    }

    public static class TierProperties {

        @Min(0)
        @Max(100000)
        private long minimumExclusive;

        @Min(1)
        @Max(100)
        private int pointsPerDollar;

        public TierProperties() {
        }

        public TierProperties(long minimumExclusive, int pointsPerDollar) {
            this.minimumExclusive = minimumExclusive;
            this.pointsPerDollar = pointsPerDollar;
        }

        public long getMinimumExclusive() {
            return minimumExclusive;
        }

        public void setMinimumExclusive(long minimumExclusive) {
            this.minimumExclusive = minimumExclusive;
        }

        public int getPointsPerDollar() {
            return pointsPerDollar;
        }

        public void setPointsPerDollar(int pointsPerDollar) {
            this.pointsPerDollar = pointsPerDollar;
        }
    }
}
