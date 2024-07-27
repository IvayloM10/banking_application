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

    @NotEmpty
    @Positive
    private BigDecimal amount;

    private Long requesterId;

    @NotEmpty
    private BigDecimal returnAmount;

    private double rate;

    @NotEmpty
    private String term;

    @NotEmpty
    @Size(min=3,max = 3)
    private String currency;

    @NotEmpty
    @Positive
    private BigDecimal monthlyPayment;

}
