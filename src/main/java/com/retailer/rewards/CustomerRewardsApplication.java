package com.retailer.rewards;

import com.retailer.rewards.config.RewardsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RewardsProperties.class)
public class CustomerRewardsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerRewardsApplication.class, args);
    }
}
