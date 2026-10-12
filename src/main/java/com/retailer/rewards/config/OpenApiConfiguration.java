package com.retailer.rewards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customerRewardsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Customer Rewards API")
                .version("1.0.0")
                .description("Calculates retail reward points for each purchase, then totals them by customer and month. "
                        + "A purchase earns 2 points for every whole dollar over $100 and 1 point for every whole dollar over $50 up to $100.")
                .contact(new Contact().name("Customer Rewards")));
    }
}
