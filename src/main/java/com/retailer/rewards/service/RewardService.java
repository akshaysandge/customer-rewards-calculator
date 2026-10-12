package com.retailer.rewards.service;

import com.retailer.rewards.client.CustomerProfileClient;
import com.retailer.rewards.client.TransactionHistoryClient;
import com.retailer.rewards.dto.CustomerProfileResponse;
import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.RewardsReportResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Purchase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);

    private final CustomerProfileClient customerProfileClient;
    private final TransactionHistoryClient transactionHistoryClient;
    private final RewardResponseAssembler rewardResponseAssembler;

    public RewardService(
            CustomerProfileClient customerProfileClient,
            TransactionHistoryClient transactionHistoryClient,
            RewardResponseAssembler rewardResponseAssembler) {
        this.customerProfileClient = customerProfileClient;
        this.transactionHistoryClient = transactionHistoryClient;
        this.rewardResponseAssembler = rewardResponseAssembler;
    }

    public CompletableFuture<List<CustomerProfileResponse>> listCustomers() {
        return customerProfileClient.findAll().thenApply(customers -> {
            List<Customer> sortedCustomers = sortedCopy(customers);
            List<CustomerProfileResponse> profiles = new ArrayList<CustomerProfileResponse>();
            for (Customer customer : sortedCustomers) {
                profiles.add(CustomerProfileResponse.from(customer));
            }
            log.info("Listed customers count={}", Integer.valueOf(profiles.size()));
            return Collections.unmodifiableList(profiles);
        });
    }

    public CompletableFuture<CustomerRewardResponse> calculateForCustomer(
            String customerId,
            RewardWindow rewardWindow,
            boolean includeTransactions) {
        return customerProfileClient.findById(customerId).thenCompose(customerOptional -> {
            if (!customerOptional.isPresent()) {
                CompletableFuture<CustomerRewardResponse> missingCustomer = new CompletableFuture<CustomerRewardResponse>();
                missingCustomer.completeExceptionally(new CustomerNotFoundException(customerId));
                return missingCustomer;
            }
            Customer customer = customerOptional.get();
            return transactionHistoryClient
                    .findByCustomerAndDateRange(customerId, rewardWindow.getStartDate(), rewardWindow.getEndDate())
                    .thenApply(purchases -> toCustomerReward(customer, rewardWindow, purchases, includeTransactions));
        });
    }

    public CompletableFuture<RewardsReportResponse> calculateForAll(RewardWindow rewardWindow, boolean includeTransactions) {
        return customerProfileClient.findAll().thenCompose(customers -> {
            List<Customer> sortedCustomers = sortedCopy(customers);
            if (sortedCustomers.isEmpty()) {
                RewardsReportResponse emptyReport = rewardResponseAssembler.assembleReport(
                        rewardWindow,
                        Collections.<CustomerRewardResponse>emptyList());
                return CompletableFuture.completedFuture(emptyReport);
            }

            List<CompletableFuture<CustomerRewardResponse>> rewardFutures =
                    new ArrayList<CompletableFuture<CustomerRewardResponse>>();
            for (Customer customer : sortedCustomers) {
                CompletableFuture<CustomerRewardResponse> rewardFuture = transactionHistoryClient
                        .findByCustomerAndDateRange(
                                customer.getCustomerId(),
                                rewardWindow.getStartDate(),
                                rewardWindow.getEndDate())
                        .thenApply(purchases -> toCustomerReward(customer, rewardWindow, purchases, includeTransactions));
                rewardFutures.add(rewardFuture);
            }
            return joinAll(rewardFutures).thenApply(customerRewards -> {
                RewardsReportResponse report = rewardResponseAssembler.assembleReport(rewardWindow, customerRewards);
                log.info(
                        "Calculated reward report startDate={} endDate={} customerCount={} transactionCount={} totalPoints={}",
                        rewardWindow.getStartDate(),
                        rewardWindow.getEndDate(),
                        Integer.valueOf(report.getCustomerCount()),
                        Integer.valueOf(report.getTransactionCount()),
                        Long.valueOf(report.getTotalPoints()));
                return report;
            });
        });
    }

    private CustomerRewardResponse toCustomerReward(
            Customer customer,
            RewardWindow rewardWindow,
            List<Purchase> purchases,
            boolean includeTransactions) {
        CustomerRewardResponse customerReward = rewardResponseAssembler.assemble(
                customer,
                rewardWindow,
                purchases,
                includeTransactions);
        log.info(
                "Calculated rewards customerId={} startDate={} endDate={} transactionCount={} totalPoints={}",
                customer.getCustomerId(),
                rewardWindow.getStartDate(),
                rewardWindow.getEndDate(),
                Integer.valueOf(customerReward.getTransactionCount()),
                Long.valueOf(customerReward.getTotalPoints()));
        return customerReward;
    }

    private static List<Customer> sortedCopy(List<Customer> customers) {
        List<Customer> sortedCustomers = new ArrayList<Customer>(customers);
        Collections.sort(sortedCustomers, Comparator.comparing(Customer::getCustomerId));
        return sortedCustomers;
    }

    private static <T> CompletableFuture<List<T>> joinAll(List<CompletableFuture<T>> futures) {
        CompletableFuture<?>[] futureArray = futures.toArray(new CompletableFuture<?>[futures.size()]);
        return CompletableFuture.allOf(futureArray).thenApply(ignored -> {
            List<T> results = new ArrayList<T>();
            for (CompletableFuture<T> future : futures) {
                results.add(future.join());
            }
            return results;
        });
    }
}
