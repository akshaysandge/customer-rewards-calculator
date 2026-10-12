package com.retailer.rewards.client;

import com.retailer.rewards.config.RewardsProperties;
import com.retailer.rewards.exception.DownstreamDataException;
import org.springframework.stereotype.Component;

@Component
public class ConfiguredRemoteCallDelay implements RemoteCallDelay {

    private final long latencyMs;

    public ConfiguredRemoteCallDelay(RewardsProperties rewardsProperties) {
        this.latencyMs = rewardsProperties.getSimulatedLatencyMs();
    }

    @Override
    public void await() {
        if (latencyMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DownstreamDataException("The reward data service call was interrupted.", exception);
        }
    }
}
