package com.example.banking_application.models.entities;

import com.example.banking_application.models.entities.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;
    @ManyToOne
    @JoinColumn(name = "maker_id")
    private User maker;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
    @Column(unique = true)
    private String transactionIdentifier;

    @Column(nullable = false)
    @Positive
    private BigDecimal amount;

    @Column
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private String status;

    @Column
    private String sign;

    @Column
    private LocalDate date;


    @Column
    private String senderCardType;

    @Column
    private String recieverCardType;


    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public User getMaker() {
        return maker;
    }

    public void setMaker(User maker) {
        this.maker = maker;
    }
}
