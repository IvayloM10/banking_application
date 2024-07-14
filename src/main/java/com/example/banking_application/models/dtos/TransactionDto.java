package com.example.banking_application.models.dtos;

import com.example.banking_application.models.entities.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDto {
    @NotEmpty
    private String description;
    @NotEmpty
    private String currency;

    @Positive
    private BigDecimal amountBase;

    @NotEmpty
    private String accountNumber;
    @NotEmpty
    private String accountHolderName;
}
