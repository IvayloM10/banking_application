package com.example.banking_application.models.dtos;

import com.example.banking_application.models.entities.enums.CardType;
import com.example.banking_application.models.entities.enums.Currency;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CardDto {
    @NotEmpty
    private Currency currency;
    @NotEmpty
    private CardType cardType;

}
