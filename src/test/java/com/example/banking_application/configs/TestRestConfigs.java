package com.example.banking_application.configs;

import com.example.banking_application.config.ForexConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestRestConfigs {

    @Bean
    @Primary
    public ForexConfigurations forexConfigurations() {
        ForexConfigurations configurations = new ForexConfigurations();
        configurations.setKey("test-key");
        configurations.setUrl("http://test-url.com");
        configurations.setBase("USD");
        return configurations;
    }
}
