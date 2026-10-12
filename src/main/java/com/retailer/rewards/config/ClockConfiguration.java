package com.retailer.rewards.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfiguration {

    @Bean
    public Clock businessClock() {
        return Clock.systemDefaultZone();
    }
}
