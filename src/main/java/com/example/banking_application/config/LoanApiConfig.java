package com.example.banking_application.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "spring.mvc.hiddenmethod.filter.loans.api")
@Component
@Getter
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
