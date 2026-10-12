package com.retailer.rewards.controller;

import com.retailer.rewards.service.RewardPointsCalculator;
import com.retailer.rewards.web.RequestCorrelationFilter;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RewardApiTest {

    private static final String REQUEST_ID = RequestCorrelationFilter.REQUEST_ID_HEADER;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RewardPointsCalculator rewardPointsCalculator;

    @Test
    void loadsThePublishedPointRuleFromConfiguration() {
        assertEquals(90L, rewardPointsCalculator.calculate(new BigDecimal("120")).getPoints());
    }

    @Test
    void calculatesMayaChenForTheThreeMonthScenario() throws Exception {
        ResultActions response = performAsync(get("/api/v1/customers/C1001/rewards")
                .param("from", "2026-07-01")
                .param("to", "2026-09-30")
                .header(REQUEST_ID, "rewards-test-001"));

        response.andExpect(status().isOk())
                .andExpect(header().string(REQUEST_ID, "rewards-test-001"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customer.customerId").value("C1001"))
                .andExpect(jsonPath("$.customer.fullName").value("Maya Chen"))
                .andExpect(jsonPath("$.customer.email").value("maya.chen@example.com"))
                .andExpect(jsonPath("$.customer.enrolledOn").value("2024-03-15"))
                .andExpect(jsonPath("$.period.startDate").value("2026-07-01"))
                .andExpect(jsonPath("$.period.endDate").value("2026-09-30"))
                .andExpect(jsonPath("$.period.monthCount").value(3))
                .andExpect(jsonPath("$.programRules[1]").value("2 points for each whole dollar over $100."))
                .andExpect(jsonPath("$.transactionCount").value(5))
                .andExpect(jsonPath("$.totalPoints").value(415))
                .andExpect(jsonPath("$.totalSpent", closeTo(545.49, 0.001)))
                .andExpect(jsonPath("$.monthlyRewards[0].monthLabel").value("July 2026"))
                .andExpect(jsonPath("$.monthlyRewards[0].points").value(115))
                .andExpect(jsonPath("$.monthlyRewards[0].transactionCount").value(2))
                .andExpect(jsonPath("$.monthlyRewards[1].monthLabel").value("August 2026"))
                .andExpect(jsonPath("$.monthlyRewards[1].points").value(50))
                .andExpect(jsonPath("$.monthlyRewards[2].monthLabel").value("September 2026"))
                .andExpect(jsonPath("$.monthlyRewards[2].points").value(250))
                .andExpect(jsonPath("$.transactions.length()").value(5))
                .andExpect(jsonPath("$.transactions[0].transactionId").value("T10011"))
                .andExpect(jsonPath("$.transactions[0].description").value("Wireless headphones"))
                .andExpect(jsonPath("$.transactions[0].category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.transactions[0].points").value(90))
                .andExpect(jsonPath("$.transactions[0].wholeDollarAmount").value(120))
                .andExpect(jsonPath("$.transactions[0].bands[0].dollars").value(20))
                .andExpect(jsonPath("$.transactions[0].bands[0].points").value(40))
                .andExpect(jsonPath("$.transactions[0].bands[1].dollars").value(50))
                .andExpect(jsonPath("$.transactions[0].bands[1].points").value(50))
                .andExpect(jsonPath("$.transactions[1].transactionId").value("T10012"))
                .andExpect(jsonPath("$.transactions[1].wholeDollarAmount").value(75))
                .andExpect(jsonPath("$.transactions[1].points").value(25))
                .andExpect(jsonPath("$.transactions[4].transactionId").value("T10015"))
                .andExpect(content().string(containsString("\"amount\":120.00")))
                .andExpect(content().string(containsString("\"amount\":75.50")))
                .andExpect(content().string(containsString("\"totalSpent\":545.49")));
    }

    @Test
    void acceptsThreeMonthsEndingOnSeptemberThirty() throws Exception {
        performAsync(get("/api/v1/customers/C1001/rewards")
                .param("months", "3")
                .param("asOf", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.startDate").value("2026-07-01"))
                .andExpect(jsonPath("$.period.endDate").value("2026-09-30"))
                .andExpect(jsonPath("$.totalPoints").value(415));
    }

    @Test
    void omitsTransactionLinesWhenAskedForASummary() throws Exception {
        performAsync(get("/api/v1/customers/C1001/rewards")
                .param("from", "2026-07-01")
                .param("to", "2026-09-30")
                .param("includeTransactions", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(415))
                .andExpect(jsonPath("$.monthlyRewards.length()").value(3))
                .andExpect(jsonPath("$.transactions").doesNotExist());
    }

    @Test
    void returnsZeroPointsForAMonthWithNoPurchases() throws Exception {
        performAsync(get("/api/v1/customers/C1004/rewards")
                .param("from", "2026-07-01")
                .param("to", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.fullName").value("Jonah Blake"))
                .andExpect(jsonPath("$.totalPoints").value(0))
                .andExpect(jsonPath("$.transactionCount").value(0))
                .andExpect(jsonPath("$.transactions.length()").value(0))
                .andExpect(jsonPath("$.monthlyRewards[0].monthLabel").value("July 2026"))
                .andExpect(jsonPath("$.monthlyRewards[0].points").value(0));
    }

    @Test
    void truncatesCentsAtTheFiftyAndOneHundredBoundaries() throws Exception {
        performAsync(get("/api/v1/customers/C1003/rewards")
                .param("from", "2026-07-01")
                .param("to", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(50))
                .andExpect(jsonPath("$.transactions[0].transactionId").value("T10031"))
                .andExpect(jsonPath("$.transactions[0].points").value(0))
                .andExpect(jsonPath("$.transactions[1].transactionId").value("T10032"))
                .andExpect(jsonPath("$.transactions[1].amount", closeTo(50.01, 0.001)))
                .andExpect(jsonPath("$.transactions[1].wholeDollarAmount").value(50))
                .andExpect(jsonPath("$.transactions[1].points").value(0))
                .andExpect(jsonPath("$.transactions[2].transactionId").value("T10033"))
                .andExpect(jsonPath("$.transactions[2].wholeDollarAmount").value(100))
                .andExpect(jsonPath("$.transactions[2].points").value(50));
    }

    @Test
    void totalsEveryCustomerAndLeavesJuneOutsideTheWindow() throws Exception {
        performAsync(get("/api/v1/rewards")
                .param("from", "2026-07-01")
                .param("to", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCount").value(5))
                .andExpect(jsonPath("$.transactionCount").value(14))
                .andExpect(jsonPath("$.totalPoints").value(950))
                .andExpect(jsonPath("$.totalSpent", closeTo(1352.24, 0.001)))
                .andExpect(jsonPath("$.customers[0].customer.customerId").value("C1001"))
                .andExpect(jsonPath("$.customers[0].totalPoints").value(415))
                .andExpect(jsonPath("$.customers[1].totalPoints").value(350))
                .andExpect(jsonPath("$.customers[2].totalPoints").value(50))
                .andExpect(jsonPath("$.customers[3].customer.fullName").value("Jonah Blake"))
                .andExpect(jsonPath("$.customers[3].totalPoints").value(45))
                .andExpect(jsonPath("$.customers[4].customer.customerId").value("C1005"))
                .andExpect(jsonPath("$.customers[4].totalPoints").value(90))
                .andExpect(jsonPath("$.customers[4].transactionCount").value(1))
                .andExpect(jsonPath("$.customers[4].transactions[0].transactionId").value("T10052"));
    }

    @Test
    void listsCustomers() throws Exception {
        performAsync(get("/api/v1/customers").header(REQUEST_ID, "rewards-test-002"))
                .andExpect(status().isOk())
                .andExpect(header().string(REQUEST_ID, "rewards-test-002"))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].customerId").value("C1001"))
                .andExpect(jsonPath("$[0].fullName").value("Maya Chen"))
                .andExpect(jsonPath("$[4].customerId").value("C1005"));
    }

    @Test
    void returnsNotFoundForAnUnknownCustomer() throws Exception {
        performAsync(get("/api/v1/customers/C9999/rewards")
                .param("months", "3")
                .param("asOf", "2026-09-30")
                .header(REQUEST_ID, "rewards-test-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer C9999 was not found."))
                .andExpect(jsonPath("$.path").value("/api/v1/customers/C9999/rewards"))
                .andExpect(jsonPath("$.requestId").value("rewards-test-404"));
    }

    @Test
    void rejectsACustomerIdThatDoesNotMatchTheFormat() throws Exception {
        mockMvc.perform(get("/api/v1/customers/c1001/rewards").param("months", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("customerId must match C followed by 4 digits, for example C1001."));
    }

    @Test
    void rejectsCombinedWindowParameters() throws Exception {
        mockMvc.perform(get("/api/v1/customers/C1001/rewards")
                        .param("months", "3")
                        .param("from", "2026-07-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Use either from and to, or months and asOf. Do not combine them."));
    }

    @Test
    void rejectsAPartialDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/rewards").param("from", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Both from and to are required for an explicit date range."));
    }

    @Test
    void rejectsAReversedDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/rewards").param("from", "2026-09-30").param("to", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter from must be on or before to."));
    }

    @Test
    void rejectsAMonthCountOutsideTheLimit() throws Exception {
        mockMvc.perform(get("/api/v1/rewards").param("months", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter months must be between 1 and 36."));

        mockMvc.perform(get("/api/v1/rewards").param("months", "37"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter months must be between 1 and 36."));
    }

    @Test
    void rejectsMalformedQueryValues() throws Exception {
        mockMvc.perform(get("/api/v1/customers/C1001/rewards").param("from", "07-01-2026").param("to", "2026-09-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'from' must be an ISO-8601 date (yyyy-MM-dd)."));

        mockMvc.perform(get("/api/v1/rewards").param("months", "three"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'months' must be a whole number."));

        mockMvc.perform(get("/api/v1/rewards").param("includeTransactions", "maybe"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'includeTransactions' must be true or false."));
    }

    @Test
    void replacesAnUnsafeRequestId() throws Exception {
        performAsync(get("/api/v1/customers").header(REQUEST_ID, "bad id!!"))
                .andExpect(status().isOk())
                .andExpect(header().string(REQUEST_ID, not("bad id!!")))
                .andExpect(header().string(REQUEST_ID, matchesPattern("^[A-Za-z0-9\\-]{8,64}$")));
    }

    @Test
    void rejectsAnUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/api/v1/customers/C1001/rewards"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void returnsJsonForAnUnknownPath() throws Exception {
        mockMvc.perform(get("/api/v1/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No endpoint matches this request."));
    }

    @Test
    void exposesHealthAndTheOpenApiDocument() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("Customer Rewards Calculator"))
                .andExpect(jsonPath("$.app.javaTarget").value("1.8"));

        mockMvc.perform(get("/api/v1/openapi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Customer Rewards API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"));
    }

    private ResultActions performAsync(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        MvcResult asyncResult = mockMvc.perform(requestBuilder)
                .andExpect(request().asyncStarted())
                .andReturn();
        return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(asyncResult));
    }
}
