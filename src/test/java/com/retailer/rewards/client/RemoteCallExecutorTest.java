package com.retailer.rewards.client;

import com.retailer.rewards.exception.DownstreamDataException;
import com.retailer.rewards.model.Purchase;
import com.retailer.rewards.web.RewardThreadFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RemoteCallExecutorTest {

    @Test
    void runsTheSupplierOnTheFetchPool() throws Exception {
        ThreadPoolTaskExecutor fetchPool = fetchPool();
        ScheduledExecutorService timeoutScheduler = timeoutScheduler();
        try {
            RemoteCallDelay remoteCallDelay = mock(RemoteCallDelay.class);
            RemoteCallExecutor remoteCallExecutor = new RemoteCallExecutor(fetchPool, timeoutScheduler, remoteCallDelay, 2000L);

            String workerName = remoteCallExecutor.supply(() -> Thread.currentThread().getName()).get(2, TimeUnit.SECONDS);

            assertTrue(workerName.startsWith("txn-fetch-"));
            verify(remoteCallDelay).await();
        } finally {
            fetchPool.shutdown();
            timeoutScheduler.shutdownNow();
        }
    }

    @Test
    void returnsThePurchaseRowsAfterTheSimulatedCall() throws Exception {
        ThreadPoolTaskExecutor fetchPool = fetchPool();
        ScheduledExecutorService timeoutScheduler = timeoutScheduler();
        try {
            RemoteCallExecutor remoteCallExecutor = new RemoteCallExecutor(
                    fetchPool,
                    timeoutScheduler,
                    new RemoteCallDelay() {
                        @Override
                        public void await() {
                        }
                    },
                    2000L);
            SimulatedTransactionHistoryClient transactionHistoryClient = new SimulatedTransactionHistoryClient(
                    new InMemoryRewardDataset(),
                    remoteCallExecutor);

            List<Purchase> purchases = transactionHistoryClient
                    .findByCustomerAndDateRange("C1001", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"))
                    .get(2, TimeUnit.SECONDS);

            assertEquals(2, purchases.size());
            assertEquals("T10011", purchases.get(0).getTransactionId());
        } finally {
            fetchPool.shutdown();
            timeoutScheduler.shutdownNow();
        }
    }

    @Test
    void failsWhenTheDownstreamCallExceedsTheTimeout() throws Exception {
        ThreadPoolTaskExecutor fetchPool = fetchPool();
        ScheduledExecutorService timeoutScheduler = timeoutScheduler();
        try {
            RemoteCallDelay slowDelay = new RemoteCallDelay() {
                @Override
                public void await() {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new DownstreamDataException("The reward data service call was interrupted.", exception);
                    }
                }
            };
            RemoteCallExecutor remoteCallExecutor = new RemoteCallExecutor(fetchPool, timeoutScheduler, slowDelay, 50L);
            long startedAt = System.nanoTime();

            ExecutionException exception = assertThrows(
                    ExecutionException.class,
                    () -> remoteCallExecutor.supply(() -> "ok").get(2, TimeUnit.SECONDS));

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            assertTrue(elapsedMs < 700L);
            assertEquals(
                    "The reward data service did not respond in time.",
                    findCause(exception, DownstreamDataException.class).getMessage());
        } finally {
            fetchPool.shutdown();
            timeoutScheduler.shutdownNow();
        }
    }

    private static ThreadPoolTaskExecutor fetchPool() {
        ThreadPoolTaskExecutor fetchPool = new ThreadPoolTaskExecutor();
        fetchPool.setCorePoolSize(1);
        fetchPool.setMaxPoolSize(2);
        fetchPool.setQueueCapacity(10);
        fetchPool.setThreadFactory(new RewardThreadFactory("txn-fetch-"));
        fetchPool.initialize();
        return fetchPool;
    }

    private static ScheduledExecutorService timeoutScheduler() {
        return Executors.newSingleThreadScheduledExecutor(new RewardThreadFactory("reward-timeout-"));
    }

    private static <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 10) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
            depth++;
        }
        throw new AssertionError("Expected cause " + type.getSimpleName());
    }
}
