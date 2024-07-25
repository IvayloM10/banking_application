package com.example.banking_application.models.dtos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
//@Entity
public class LoanDto {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean isAuthorized;
    private BigDecimal amount;
    private BigDecimal returnAmount;
    private Long requesterId;
    private double rate;
    private String term;
    private LocalDate date;
    private BigDecimal monthlyPayment;
    private String status;
}
