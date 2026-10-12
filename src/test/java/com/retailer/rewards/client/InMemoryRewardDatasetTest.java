package com.retailer.rewards.client;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Purchase;
import com.retailer.rewards.model.RewardTier;
import com.retailer.rewards.service.RewardPointsCalculator;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRewardDatasetTest {

    private final InMemoryRewardDataset rewardDataset = new InMemoryRewardDataset();
    private final RewardPointsCalculator calculator = new RewardPointsCalculator(Arrays.asList(
            new RewardTier(100L, 2),
            new RewardTier(50L, 1)));

    @Test
    void containsFiveCustomersAndTwentyPurchases() {
        assertEquals(5, rewardDataset.getCustomers().size());
        assertEquals("C1001", rewardDataset.getCustomers().get(0).getCustomerId());
        assertEquals("C1005", rewardDataset.getCustomers().get(4).getCustomerId());

        int purchaseCount = 0;
        Set<String> transactionIds = new HashSet<String>();
        for (Customer customer : rewardDataset.getCustomers()) {
            List<Purchase> purchases = rewardDataset.findPurchases(
                    customer.getCustomerId(),
                    LocalDate.parse("2026-01-01"),
                    LocalDate.parse("2026-12-31"));
            purchaseCount += purchases.size();
            for (Purchase purchase : purchases) {
                assertTrue(transactionIds.add(purchase.getTransactionId()));
                assertEquals(customer.getCustomerId(), purchase.getCustomerId());
                calculator.calculate(purchase.getAmount());
            }
        }
        assertEquals(20, purchaseCount);
    }

    @Test
    void keepsTheJuneCameraOutsideTheJulyToSeptemberWindow() {
        List<Purchase> thirdQuarter = rewardDataset.findPurchases(
                "C1005",
                LocalDate.parse("2026-07-01"),
                LocalDate.parse("2026-09-30"));
        assertEquals(1, thirdQuarter.size());
        assertEquals("T10052", thirdQuarter.get(0).getTransactionId());

        List<Purchase> includingJune = rewardDataset.findPurchases(
                "C1005",
                LocalDate.parse("2026-06-01"),
                LocalDate.parse("2026-06-30"));
        assertEquals("T10051", includingJune.get(0).getTransactionId());
        assertEquals(LocalDate.parse("2026-06-15"), includingJune.get(0).getTransactionDate());
        assertEquals(850L, calculator.calculate(includingJune.get(0).getAmount()).getPoints());
    }

    @Test
    void includesTheLastDayOfSeptemberAndExcludesTheFollowingDay() {
        List<Purchase> throughSeptember = rewardDataset.findPurchases(
                "C1004",
                LocalDate.parse("2026-09-30"),
                LocalDate.parse("2026-09-30"));
        assertEquals(1, throughSeptember.size());
        assertEquals("T10042", throughSeptember.get(0).getTransactionId());

        List<Purchase> octoberOnly = rewardDataset.findPurchases(
                "C1004",
                LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-31"));
        assertEquals("T10043", octoberOnly.get(0).getTransactionId());
        assertFalse(octoberOnly.get(0).getTransactionDate().isBefore(LocalDate.parse("2026-10-01")));
    }
}
