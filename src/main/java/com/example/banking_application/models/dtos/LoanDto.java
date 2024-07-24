package com.example.banking_application.models.dtos;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
public class LoanDto {
    private BigDecimal amount;

    private Long requesterId;
    private BigDecimal returnAmount;
    private double rate;
    private String term;
    private String currency;
    private BigDecimal monthlyPayment;
    private String pin;

    private void setRateForTransaction(){
        setRate(Double.parseDouble(String.valueOf(returnAmount.divide(amount))));
    }
}
