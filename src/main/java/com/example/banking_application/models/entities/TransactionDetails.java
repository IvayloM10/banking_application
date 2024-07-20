package com.example.banking_application.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TransactionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private double amount;
    private char sign;

    private LocalDate date;

    private String currency;
    private String status;
    private String description;
}
