package com.retailer.rewards.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> applicationDetails = new LinkedHashMap<String, Object>();
        applicationDetails.put("name", "Customer Rewards Calculator");
        applicationDetails.put("version", "1.0.0");
        applicationDetails.put("javaTarget", "1.8");
        builder.withDetail("app", applicationDetails);
    }
}
