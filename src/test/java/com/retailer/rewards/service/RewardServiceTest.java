package com.retailer.rewards.service;

import com.retailer.rewards.client.CustomerProfileClient;
import com.retailer.rewards.client.TransactionHistoryClient;
import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.DownstreamDataException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Purchase;
import com.retailer.rewards.model.PurchaseCategory;
import com.retailer.rewards.model.RewardTier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    private static final LocalDate JULY_START = LocalDate.parse("2026-07-01");
    private static final LocalDate SEPTEMBER_END = LocalDate.parse("2026-09-30");

    @Mock
    private CustomerProfileClient customerProfileClient;

    @Mock
    private TransactionHistoryClient transactionHistoryClient;

    private RewardService rewardService;
    private Customer mayaChen;

    @BeforeEach
    void setUp() {
        RewardPointsCalculator calculator = new RewardPointsCalculator(Arrays.asList(
                new RewardTier(100L, 2),
                new RewardTier(50L, 1)));
        rewardService = new RewardService(
                customerProfileClient,
                transactionHistoryClient,
                new RewardResponseAssembler(calculator));
        mayaChen = new Customer("C1001", "Maya", "Chen", "maya.chen@example.com", LocalDate.parse("2024-03-15"));
    }

    @Test
    void sumsEachTransactionInsteadOfTheMonthlyTotal() throws Exception {
        when(customerProfileClient.findById("C1001"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mayaChen)));
        when(transactionHistoryClient.findByCustomerAndDateRange(eq("C1001"), eq(JULY_START), eq(LocalDate.parse("2026-07-31"))))
                .thenReturn(CompletableFuture.completedFuture(Arrays.asList(
                        purchase("T00001", "2026-07-02", "60.00", "First basket"),
                        purchase("T00002", "2026-07-03", "60.00", "Second basket"))));

        CustomerRewardResponse customerReward = rewardService
                .calculateForCustomer("C1001", new RewardWindow(JULY_START, LocalDate.parse("2026-07-31")), true)
                .get(2, TimeUnit.SECONDS);

        assertEquals(20L, customerReward.getTotalPoints());
        assertEquals(2, customerReward.getTransactionCount());
        assertEquals(1, customerReward.getMonthlyRewards().size());
        assertEquals(20L, customerReward.getMonthlyRewards().get(0).getPoints());
        assertEquals(new BigDecimal("120.00"), customerReward.getTotalSpent());
    }

    @Test
    void keepsMonthsThatHaveNoPurchases() throws Exception {
        when(customerProfileClient.findById("C1001"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mayaChen)));
        when(transactionHistoryClient.findByCustomerAndDateRange("C1001", JULY_START, SEPTEMBER_END))
                .thenReturn(CompletableFuture.completedFuture(Arrays.asList(
                        purchase("T10011", "2026-07-04", "120.00", "Wireless headphones"),
                        purchase("T10015", "2026-09-11", "200.00", "Tablet"))));

        CustomerRewardResponse customerReward = rewardService
                .calculateForCustomer("C1001", new RewardWindow(JULY_START, SEPTEMBER_END), false)
                .get(2, TimeUnit.SECONDS);

        assertEquals(340L, customerReward.getTotalPoints());
        assertEquals(2, customerReward.getTransactionCount());
        assertNull(customerReward.getTransactions());
        assertEquals(3, customerReward.getMonthlyRewards().size());
        assertEquals("July 2026", customerReward.getMonthlyRewards().get(0).getMonthLabel());
        assertEquals(90L, customerReward.getMonthlyRewards().get(0).getPoints());
        assertEquals("August 2026", customerReward.getMonthlyRewards().get(1).getMonthLabel());
        assertEquals(0L, customerReward.getMonthlyRewards().get(1).getPoints());
        assertEquals(0, customerReward.getMonthlyRewards().get(1).getTransactionCount());
        assertEquals("September 2026", customerReward.getMonthlyRewards().get(2).getMonthLabel());
        assertEquals(250L, customerReward.getMonthlyRewards().get(2).getPoints());
    }

    @Test
    void returnsZeroWhenTheCustomerHasNoPurchasesInTheWindow() throws Exception {
        Customer jonahBlake = new Customer("C1004", "Jonah", "Blake", "jonah.blake@example.com", LocalDate.parse("2026-06-01"));
        when(customerProfileClient.findById("C1004"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(jonahBlake)));
        when(transactionHistoryClient.findByCustomerAndDateRange("C1004", JULY_START, LocalDate.parse("2026-07-31")))
                .thenReturn(CompletableFuture.completedFuture(Collections.<Purchase>emptyList()));

        CustomerRewardResponse customerReward = rewardService
                .calculateForCustomer("C1004", new RewardWindow(JULY_START, LocalDate.parse("2026-07-31")), true)
                .get(2, TimeUnit.SECONDS);

        assertEquals(0L, customerReward.getTotalPoints());
        assertEquals(0, customerReward.getTransactionCount());
        assertEquals(new BigDecimal("0.00"), customerReward.getTotalSpent());
        assertTrue(customerReward.getTransactions().isEmpty());
        assertEquals("Jonah Blake", customerReward.getCustomer().getFullName());
    }

    @Test
    void failsWhenTheCustomerDoesNotExistWithoutLoadingTransactions() {
        when(customerProfileClient.findById("C9999"))
                .thenReturn(CompletableFuture.completedFuture(Optional.<Customer>empty()));

        ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> rewardService.calculateForCustomer("C9999", new RewardWindow(JULY_START, SEPTEMBER_END), true)
                        .get(2, TimeUnit.SECONDS));

        assertTrue(exception.getCause() instanceof CustomerNotFoundException);
        assertEquals("Customer C9999 was not found.", exception.getCause().getMessage());
        verify(transactionHistoryClient, never()).findByCustomerAndDateRange(any(), any(), any());
    }

    @Test
    void propagatesADownstreamFailure() {
        CompletableFuture<List<Customer>> failedLookup = new CompletableFuture<List<Customer>>();
        failedLookup.completeExceptionally(
                new DownstreamDataException("The reward data service did not respond in time."));
        when(customerProfileClient.findAll()).thenReturn(failedLookup);

        ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> rewardService.calculateForAll(new RewardWindow(JULY_START, SEPTEMBER_END), true)
                        .get(2, TimeUnit.SECONDS));

        assertTrue(causeChainContains(exception, DownstreamDataException.class));
    }

    private static Purchase purchase(String transactionId, String transactionDate, String amount, String description) {
        return new Purchase(
                transactionId,
                "C1001",
                LocalDate.parse(transactionDate),
                new BigDecimal(amount),
                PurchaseCategory.GROCERY,
                description);
    }

    private static boolean causeChainContains(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 10) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
            depth++;
        }
        return false;
    }
}
