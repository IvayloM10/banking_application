package com.example.banking_application.services;

import com.example.banking_application.models.dtos.LoanDto;

public interface LoanService {
    void sendLoanForConfirmation(LoanDto loanDto, Long id);
}
