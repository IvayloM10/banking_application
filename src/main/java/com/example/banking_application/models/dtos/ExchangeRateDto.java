package com.example.banking_application.models.dtos;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateDto(String base, Map<String, BigDecimal> rates) {


}
