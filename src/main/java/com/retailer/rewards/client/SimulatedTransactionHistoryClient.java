package com.retailer.rewards.client;

import com.retailer.rewards.model.Purchase;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedTransactionHistoryClient implements TransactionHistoryClient {

    private static final Logger log = LoggerFactory.getLogger(SimulatedTransactionHistoryClient.class);

    private final InMemoryRewardDataset rewardDataset;
    private final RemoteCallExecutor remoteCallExecutor;

    public SimulatedTransactionHistoryClient(InMemoryRewardDataset rewardDataset, RemoteCallExecutor remoteCallExecutor) {
        this.rewardDataset = rewardDataset;
        this.remoteCallExecutor = remoteCallExecutor;
    }

    @Override
    public CompletableFuture<List<Purchase>> findByCustomerAndDateRange(
            String customerId,
            LocalDate startDate,
            LocalDate endDate) {
        return remoteCallExecutor.supply(() -> {
            List<Purchase> purchases = rewardDataset.findPurchases(customerId, startDate, endDate);
            log.debug(
                    "Fetched transactions customerId={} startDate={} endDate={} count={}",
                    customerId,
                    startDate,
                    endDate,
                    Integer.valueOf(purchases.size()));
            return purchases;
        });
    }
}
