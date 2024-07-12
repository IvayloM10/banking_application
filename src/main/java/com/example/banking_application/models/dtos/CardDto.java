package com.example.banking_application.models.dtos;

import com.example.banking_application.models.entities.enums.CardType;
import com.example.banking_application.models.entities.enums.Currency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CardDto {
    @NotEmpty
    private String currency;
    @NotEmpty
    private String cardType;
    @NotEmpty
    @Size(min=4,max=4)
    private String pin;
    @NotEmpty
    private String confirmPin;

}
