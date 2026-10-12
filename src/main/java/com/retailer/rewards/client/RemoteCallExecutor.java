package com.retailer.rewards.client;

import com.retailer.rewards.config.AsyncConfiguration;
import com.retailer.rewards.config.RewardsProperties;
import com.retailer.rewards.exception.DownstreamDataException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Runs a simulated downstream call on the fetch pool and fails it when the configured
 * timeout expires. Java 8 has no CompletableFuture timeout, so the deadline is a scheduled task.
 */
@Component
public class RemoteCallExecutor {

    private final Executor transactionFetchExecutor;
    private final ScheduledExecutorService rewardTimeoutScheduler;
    private final RemoteCallDelay remoteCallDelay;
    private final long timeoutMs;

    @Autowired
    public RemoteCallExecutor(
            @Qualifier(AsyncConfiguration.TRANSACTION_FETCH_EXECUTOR) Executor transactionFetchExecutor,
            ScheduledExecutorService rewardTimeoutScheduler,
            RemoteCallDelay remoteCallDelay,
            RewardsProperties rewardsProperties) {
        this(
                transactionFetchExecutor,
                rewardTimeoutScheduler,
                remoteCallDelay,
                rewardsProperties.getFetchTimeoutMs());
    }

    public RemoteCallExecutor(
            Executor transactionFetchExecutor,
            ScheduledExecutorService rewardTimeoutScheduler,
            RemoteCallDelay remoteCallDelay,
            long timeoutMs) {
        if (timeoutMs < 1L) {
            throw new IllegalArgumentException("Fetch timeout must be at least 1 millisecond.");
        }
        this.transactionFetchExecutor = transactionFetchExecutor;
        this.rewardTimeoutScheduler = rewardTimeoutScheduler;
        this.remoteCallDelay = remoteCallDelay;
        this.timeoutMs = timeoutMs;
    }

    public <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        CompletableFuture<T> fetch;
        try {
            fetch = CompletableFuture.supplyAsync(() -> {
                remoteCallDelay.await();
                return supplier.get();
            }, transactionFetchExecutor);
        } catch (RejectedExecutionException exception) {
            CompletableFuture<T> rejected = new CompletableFuture<T>();
            rejected.completeExceptionally(new DownstreamDataException(
                    "The reward data service is busy. Try again shortly.",
                    exception));
            return rejected;
        }

        CompletableFuture<T> timeout = new CompletableFuture<T>();
        ScheduledFuture<?> timeoutTask = rewardTimeoutScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                timeout.completeExceptionally(
                        new DownstreamDataException("The reward data service did not respond in time."));
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);

        fetch.whenComplete((result, error) -> timeoutTask.cancel(false));
        return fetch.applyToEither(timeout, value -> value);
    }
}
