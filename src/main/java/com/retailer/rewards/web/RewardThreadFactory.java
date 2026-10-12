package com.retailer.rewards.web;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class RewardThreadFactory implements ThreadFactory {

    private final String threadNamePrefix;
    private final AtomicInteger threadIndex = new AtomicInteger(1);

    public RewardThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread worker = new Thread(runnable, threadNamePrefix + threadIndex.getAndIncrement());
        worker.setDaemon(true);
        return worker;
    }
}
