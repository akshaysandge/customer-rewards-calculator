package com.retailer.rewards.validation;

import com.retailer.rewards.exception.InvalidRewardQueryException;
import java.util.regex.Pattern;

public final class CustomerIds {

    private static final Pattern CUSTOMER_ID = Pattern.compile("^C\\d{4}$");

    private CustomerIds() {
    }

    public static void requireValid(String customerId) {
        if (customerId == null || !CUSTOMER_ID.matcher(customerId).matches()) {
            throw new InvalidRewardQueryException(
                    "customerId must match C followed by 4 digits, for example C1001.");
        }
    }
}
