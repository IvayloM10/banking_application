package com.example.banking_application.models.dtos;

import com.example.banking_application.models.entities.enums.Currency;

public record AccountRecord(Long id, Currency currency, int accountNumber) {
}
