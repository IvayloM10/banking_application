package com.example.banking_application.services;

import com.example.banking_application.models.dtos.AddLoanDto;
import com.example.banking_application.models.entities.Loan;

import java.util.List;

public interface LoanService {
    void sendLoanForConfirmation( Long id);
    List<Loan> syncUserLoans(Long id);

    void transferMoneyToUserAccount(Long id);

    Loan getCurrentLoan(Long id);

    void rejectLoan(Long id);

    void deleteLoan(Long id);
}
