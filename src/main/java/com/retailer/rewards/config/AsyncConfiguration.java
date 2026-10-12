package com.retailer.rewards.config;

import com.retailer.rewards.web.MdcTaskDecorator;
import com.retailer.rewards.web.RewardThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfiguration {

    public static final String TRANSACTION_FETCH_EXECUTOR = "transactionFetchExecutor";

    @Bean(name = TRANSACTION_FETCH_EXECUTOR)
    public ThreadPoolTaskExecutor transactionFetchExecutor(RewardsProperties rewardsProperties) {
        ThreadPoolTaskExecutor transactionFetchExecutor = new ThreadPoolTaskExecutor();
        transactionFetchExecutor.setCorePoolSize(rewardsProperties.getFetchCorePoolSize());
        transactionFetchExecutor.setMaxPoolSize(rewardsProperties.getFetchMaxPoolSize());
        transactionFetchExecutor.setQueueCapacity(rewardsProperties.getFetchQueueCapacity());
        transactionFetchExecutor.setThreadFactory(new RewardThreadFactory("txn-fetch-"));
        transactionFetchExecutor.setTaskDecorator(new MdcTaskDecorator());
        transactionFetchExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        transactionFetchExecutor.setWaitForTasksToCompleteOnShutdown(true);
        transactionFetchExecutor.setAwaitTerminationSeconds(5);
        return transactionFetchExecutor;
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService rewardTimeoutScheduler() {
        ScheduledThreadPoolExecutor timeoutScheduler = new ScheduledThreadPoolExecutor(
                1,
                new RewardThreadFactory("reward-timeout-"));
        timeoutScheduler.setRemoveOnCancelPolicy(true);
        return timeoutScheduler;
    }
}
