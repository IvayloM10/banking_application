package com.example.banking_application.services;

import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.models.entities.Loan;

public interface LoanService {
    void sendLoanForConfirmation(LoanDto loanDto, Long id);

    void transferMoneyToUserAccount(Long id);

    Loan getCurrentLoan(Long id);

    void rejectLoan(Long id);

}
