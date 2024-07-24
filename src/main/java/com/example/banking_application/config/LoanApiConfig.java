package com.example.banking_application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "loans.api")
public class LoanApiConfig {
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public LoanApiConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
}
