package com.example.banking_application.utils;

import com.example.banking_application.repositories.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestData {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;


}
