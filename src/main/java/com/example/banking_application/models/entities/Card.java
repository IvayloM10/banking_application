package com.example.banking_application.models.entities;

import com.example.banking_application.models.entities.enums.CardType;
import com.example.banking_application.models.entities.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "card_holder_id",nullable = false)
    private User cardHolder;

    @Column
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name="expiration_date",nullable = false)
    private LocalDate expirationDate;

    @Column(name="cvv", unique = true, nullable = false)
    private String cvvNumber;

    @Column(name="account_number",nullable = false,unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private CardType type;

    @Column(nullable = false)
    private double balance;

    public User getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(User cardHolder) {
        this.cardHolder = cardHolder;
    }

    private void setAccountNumberFromUser(){
       setAccountNumber(this.cardHolder.getAccount().getAccountNumber());
    }

}
