package com.retailer.rewards.config;

import com.retailer.rewards.model.RewardTier;
import com.retailer.rewards.service.RewardPointsCalculator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RewardConfiguration {

    @Bean
    public RewardPointsCalculator rewardPointsCalculator(RewardsProperties rewardsProperties) {
        List<RewardTier> rewardTiers = new ArrayList<RewardTier>();
        for (RewardsProperties.TierProperties tierProperties : rewardsProperties.getTiers()) {
            rewardTiers.add(new RewardTier(
                    tierProperties.getMinimumExclusive(),
                    tierProperties.getPointsPerDollar()));
        }
        return new RewardPointsCalculator(rewardTiers);
    }

    @Component
    static class RewardSettingsValidator implements InitializingBean {

        private final RewardsProperties rewardsProperties;

        RewardSettingsValidator(RewardsProperties rewardsProperties) {
            this.rewardsProperties = rewardsProperties;
        }

        @Override
        public void afterPropertiesSet() {
            if (rewardsProperties.getDefaultMonths() > rewardsProperties.getMaxMonths()) {
                throw new IllegalStateException("rewards.default-months cannot exceed rewards.max-months.");
            }
            if (rewardsProperties.getFetchMaxPoolSize() < rewardsProperties.getFetchCorePoolSize()) {
                throw new IllegalStateException(
                        "rewards.fetch-max-pool-size cannot be lower than rewards.fetch-core-pool-size.");
            }
        }
    }
}
