package com.retailer.rewards.client;

import com.retailer.rewards.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimulatedCustomerProfileClient implements CustomerProfileClient {

    private static final Logger log = LoggerFactory.getLogger(SimulatedCustomerProfileClient.class);

    private final InMemoryRewardDataset rewardDataset;
    private final RemoteCallExecutor remoteCallExecutor;

    public SimulatedCustomerProfileClient(InMemoryRewardDataset rewardDataset, RemoteCallExecutor remoteCallExecutor) {
        this.rewardDataset = rewardDataset;
        this.remoteCallExecutor = remoteCallExecutor;
    }

    @Override
    public CompletableFuture<Optional<Customer>> findById(String customerId) {
        return remoteCallExecutor.supply(() -> {
            log.debug("Fetched customer profile customerId={}", customerId);
            return rewardDataset.findCustomer(customerId);
        });
    }

    @Override
    public CompletableFuture<List<Customer>> findAll() {
        return remoteCallExecutor.supply(() -> {
            log.debug("Fetched customer directory size={}", Integer.valueOf(rewardDataset.getCustomers().size()));
            return rewardDataset.getCustomers();
        });
    }
}
