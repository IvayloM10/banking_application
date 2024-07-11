package com.example.banking_application.models.entities;

import com.example.banking_application.models.entities.enums.CardType;
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

    @ManyToOne
    @JoinColumn(name = "card_holder_id",nullable = false)
    private User cardHolder;

    @Column(name="expiration_date",nullable = false)
    private LocalDate expirationDate;

    @Column(name="cvv", unique = true, nullable = false)
    private int cvvNumber;

    @Column(name="account_number",nullable = false,unique = true)
    private int accountNumber;

    @Enumerated(EnumType.STRING)
    private CardType type;


    public User getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(User cardHolder) {
        this.cardHolder = cardHolder;
    }


}
