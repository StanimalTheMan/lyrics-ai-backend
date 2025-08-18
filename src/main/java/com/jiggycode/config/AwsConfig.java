package com.jiggycode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;

@Configuration
public class AwsConfig {

    @Bean
    public PollyClient pollyClient() {
        return PollyClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
