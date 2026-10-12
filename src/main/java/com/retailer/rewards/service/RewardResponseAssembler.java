package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerProfileResponse;
import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.MonthlyRewardResponse;
import com.retailer.rewards.dto.PointsBandResponse;
import com.retailer.rewards.dto.RewardPeriodResponse;
import com.retailer.rewards.dto.RewardsReportResponse;
import com.retailer.rewards.dto.TransactionRewardResponse;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Purchase;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RewardResponseAssembler {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US);
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private final RewardPointsCalculator rewardPointsCalculator;

    public RewardResponseAssembler(RewardPointsCalculator rewardPointsCalculator) {
        this.rewardPointsCalculator = rewardPointsCalculator;
    }

    public CustomerRewardResponse assemble(
            Customer customer,
            RewardWindow rewardWindow,
            List<Purchase> purchases,
            boolean includeTransactions) {
        List<Purchase> sortedPurchases = new ArrayList<Purchase>(purchases);
        sortedPurchases.sort(Comparator.comparing(Purchase::getTransactionDate).thenComparing(Purchase::getTransactionId));

        List<TransactionRewardResponse> transactionLines = new ArrayList<TransactionRewardResponse>();
        for (Purchase purchase : sortedPurchases) {
            transactionLines.add(toTransactionLine(purchase));
        }

        List<MonthlyRewardResponse> monthlyRewards = groupByMonth(rewardWindow, transactionLines);
        long totalPoints = 0L;
        BigDecimal totalSpent = ZERO_MONEY;
        for (TransactionRewardResponse transactionLine : transactionLines) {
            totalPoints = Math.addExact(totalPoints, transactionLine.getPoints());
            totalSpent = totalSpent.add(transactionLine.getAmount());
        }

        List<TransactionRewardResponse> visibleTransactions = includeTransactions
                ? Collections.unmodifiableList(transactionLines)
                : null;

        return new CustomerRewardResponse(
                CustomerProfileResponse.from(customer),
                RewardPeriodResponse.from(rewardWindow),
                rewardPointsCalculator.programRules(),
                transactionLines.size(),
                totalSpent,
                totalPoints,
                monthlyRewards,
                visibleTransactions);
    }

    public RewardsReportResponse assembleReport(RewardWindow rewardWindow, List<CustomerRewardResponse> customerRewards) {
        int transactionCount = 0;
        long totalPoints = 0L;
        BigDecimal totalSpent = ZERO_MONEY;
        for (CustomerRewardResponse customerReward : customerRewards) {
            transactionCount += customerReward.getTransactionCount();
            totalPoints = Math.addExact(totalPoints, customerReward.getTotalPoints());
            totalSpent = totalSpent.add(customerReward.getTotalSpent());
        }
        return new RewardsReportResponse(
                RewardPeriodResponse.from(rewardWindow),
                customerRewards.size(),
                transactionCount,
                totalSpent,
                totalPoints,
                Collections.unmodifiableList(new ArrayList<CustomerRewardResponse>(customerRewards)));
    }

    private TransactionRewardResponse toTransactionLine(Purchase purchase) {
        CalculatedPoints calculatedPoints = rewardPointsCalculator.calculate(purchase.getAmount());
        List<PointsBandResponse> bands = new ArrayList<PointsBandResponse>();
        for (PointsBand pointsBand : calculatedPoints.getBands()) {
            bands.add(new PointsBandResponse(
                    pointsBand.getRule(),
                    pointsBand.getDollars(),
                    pointsBand.getPointsPerDollar(),
                    pointsBand.getPoints()));
        }
        return new TransactionRewardResponse(
                purchase.getTransactionId(),
                purchase.getTransactionDate(),
                purchase.getAmount(),
                calculatedPoints.getWholeDollarAmount(),
                purchase.getCategory(),
                purchase.getDescription(),
                calculatedPoints.getPoints(),
                Collections.unmodifiableList(bands));
    }

    private List<MonthlyRewardResponse> groupByMonth(RewardWindow rewardWindow, List<TransactionRewardResponse> transactionLines) {
        Map<YearMonth, MonthAccumulator> months = new LinkedHashMap<YearMonth, MonthAccumulator>();
        YearMonth cursor = YearMonth.from(rewardWindow.getStartDate());
        YearMonth lastMonth = YearMonth.from(rewardWindow.getEndDate());
        while (!cursor.isAfter(lastMonth)) {
            months.put(cursor, new MonthAccumulator(cursor));
            cursor = cursor.plusMonths(1);
        }

        for (TransactionRewardResponse transactionLine : transactionLines) {
            YearMonth transactionMonth = YearMonth.from(transactionLine.getTransactionDate());
            MonthAccumulator monthAccumulator = months.get(transactionMonth);
            if (monthAccumulator == null) {
                throw new IllegalStateException("Transaction " + transactionLine.getTransactionId()
                        + " falls outside the reward window.");
            }
            monthAccumulator.add(transactionLine);
        }

        List<MonthlyRewardResponse> monthlyRewards = new ArrayList<MonthlyRewardResponse>();
        for (MonthAccumulator monthAccumulator : months.values()) {
            monthlyRewards.add(monthAccumulator.toResponse());
        }
        return Collections.unmodifiableList(monthlyRewards);
    }

    private static final class MonthAccumulator {

        private final YearMonth yearMonth;
        private int transactionCount;
        private BigDecimal totalSpent = ZERO_MONEY;
        private long points;

        private MonthAccumulator(YearMonth yearMonth) {
            this.yearMonth = yearMonth;
        }

        private void add(TransactionRewardResponse transactionLine) {
            transactionCount++;
            totalSpent = totalSpent.add(transactionLine.getAmount());
            points = Math.addExact(points, transactionLine.getPoints());
        }

        private MonthlyRewardResponse toResponse() {
            return new MonthlyRewardResponse(
                    yearMonth.getYear(),
                    yearMonth.getMonthValue(),
                    MONTH_LABEL.format(yearMonth),
                    transactionCount,
                    totalSpent,
                    points);
        }
    }
}
