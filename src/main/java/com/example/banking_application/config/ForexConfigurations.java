package com.example.banking_application.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
@ConfigurationProperties(prefix = "forex.api")
@Getter
@Setter
@NoArgsConstructor
public class ForexConfigurations {

    private String key;
    private String url;
    private String base;


    @PostConstruct
    public void checkConfiguration() {

        verifyNotNullOrEmpty("key", key);
        verifyNotNullOrEmpty("base", base);
        verifyNotNullOrEmpty("url", url);

        if (!"USD".equals(base)) {
            throw new IllegalStateException("Sorry, but the free API does not support base, "
                    + "currencies different than USD.");
        }


    }

     static void verifyNotNullOrEmpty(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Property " + name + " cannot be empty.");
        }
    }
}
