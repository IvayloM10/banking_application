package com.example.banking_application.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Configuration
public class RestConfigurations {

    @Bean("genericRestClient")
    public RestClient genericRestClient() {
        return RestClient.create();
    }
    @Bean
    public ClientHttpRequestInterceptor requestInterceptor() {
        return new ClientHttpRequestInterceptor() {
            @Override
            public org.springframework.http.client.ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                // Add any custom logic or headers here
                System.out.println("Request URI: " + request.getURI());
                return execution.execute(request, body);
            }
        };
    }

    @Bean("loansRestClient")
    public RestClient loansRestClient(LoanApiConfig loanApiConfig,
                                       ClientHttpRequestInterceptor requestInterceptor) {
        System.out.println("Loaded base URL: " + loanApiConfig.getBaseUrl());
        return RestClient
                .builder()
                .baseUrl(loanApiConfig.getBaseUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor(requestInterceptor)
                .build();
    }
}
