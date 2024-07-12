package com.example.banking_application.models.entities;

import com.example.banking_application.models.entities.enums.CardType;
import com.example.banking_application.models.entities.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="virtual_cards")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class VirtualCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="card_type",nullable = false)
    private CardType type;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false)
     private String cardNumber;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User cardHolder;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private String pin;


}
