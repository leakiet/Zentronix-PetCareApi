package com.greenkitchen.portal.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Configuration for async processing
    // This enables @Async annotations to work
}
