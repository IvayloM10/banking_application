package com.example.banking_application.services;

import com.example.banking_application.models.dtos.AddLoanDto;
import com.example.banking_application.models.entities.Loan;

public interface LoanService {
    void sendLoanForConfirmation( Long id);
    void syncUserLoans();

    void transferMoneyToUserAccount(Long id);

    Loan getCurrentLoan(Long id);

    void rejectLoan(Long id);

}
