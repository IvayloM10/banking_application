package com.example.banking_application.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AddLoanDto {
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
