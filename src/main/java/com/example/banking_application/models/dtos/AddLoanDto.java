package com.example.banking_application.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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

}
