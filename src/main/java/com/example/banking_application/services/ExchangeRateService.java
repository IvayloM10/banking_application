package com.example.banking_application.services;


import com.example.banking_application.models.dtos.ExchangeRateDto;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeRateService {

    boolean hasInitializedExRates();

    ExchangeRateDto fetchExRates();

    void updateRates(ExchangeRateDto exRatesDTO);

    BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount);
}
