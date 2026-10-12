package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerProfileResponse;
import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.RewardsReportResponse;
import com.retailer.rewards.service.RewardService;
import com.retailer.rewards.service.RewardWindow;
import com.retailer.rewards.service.RewardWindowResolver;
import com.retailer.rewards.validation.CustomerIds;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Customer rewards", description = "Reward points for one customer or for every customer.")
public class RewardController {

    private final RewardWindowResolver rewardWindowResolver;
    private final RewardService rewardService;

    public RewardController(RewardWindowResolver rewardWindowResolver, RewardService rewardService) {
        this.rewardWindowResolver = rewardWindowResolver;
        this.rewardService = rewardService;
    }

    @GetMapping("/customers")
    @Operation(summary = "List customers", description = "Returns the customer profiles available to the rewards program.")
    public CompletableFuture<List<CustomerProfileResponse>> listCustomers() {
        return rewardService.listCustomers();
    }

    @GetMapping("/customers/{customerId}/rewards")
    @Operation(
            summary = "Calculate rewards for one customer",
            description = "Returns customer details, each transaction in the window, points per month, and the total. "
                    + "Pass months (default 3) or an explicit from/to range. The three-month case is months=3.")
    public CompletableFuture<CustomerRewardResponse> getCustomerRewards(
            @Parameter(description = "Customer id, for example C1001", example = "C1001")
            @PathVariable("customerId") String customerId,
            @Parameter(description = "Number of calendar months ending at asOf. Defaults to 3. Do not combine with from/to.")
            @RequestParam(value = "months", required = false) Integer months,
            @Parameter(description = "Last date of a relative window. Defaults to today. Do not combine with from/to.")
            @RequestParam(value = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @Parameter(description = "First inclusive transaction date. Requires to.")
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Last inclusive transaction date. Requires from.")
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Include each transaction. Set false for monthly totals only.")
            @RequestParam(value = "includeTransactions", defaultValue = "true") boolean includeTransactions) {
        CustomerIds.requireValid(customerId);
        RewardWindow rewardWindow = rewardWindowResolver.resolve(months, asOf, from, to);
        return rewardService.calculateForCustomer(customerId, rewardWindow, includeTransactions);
    }

    @GetMapping("/rewards")
    @Operation(
            summary = "Calculate rewards for every customer",
            description = "Same window rules as the single-customer endpoint. Each customer includes monthly points and, by default, transactions.")
    public CompletableFuture<RewardsReportResponse> getRewardsReport(
            @RequestParam(value = "months", required = false) Integer months,
            @RequestParam(value = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "includeTransactions", defaultValue = "true") boolean includeTransactions) {
        RewardWindow rewardWindow = rewardWindowResolver.resolve(months, asOf, from, to);
        return rewardService.calculateForAll(rewardWindow, includeTransactions);
    }
}
