package com.example.banking_application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestConfigurations {

    @Bean("genericRestClient")
    public RestClient genericRestClient() {
        return RestClient.create();
    }


    @Bean("loansRestClient")
    public RestClient offersRestClient(LoanApiConfig loanApiConfig,
                                       ClientHttpRequestInterceptor requestInterceptor) {
        return RestClient
                .builder()
                .baseUrl(loanApiConfig.getBaseUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor(requestInterceptor)
                .build();
    }
}
