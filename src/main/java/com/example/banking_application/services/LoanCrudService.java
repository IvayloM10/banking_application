package com.example.banking_application.services;


import com.example.banking_application.models.dtos.AddLoanDto;
import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.models.entities.Loan;

import java.util.List;

public interface LoanCrudService {
    void createLoan(AddLoanDto addLoanDto);

    void updateLoan(Long id, LoanDto loanDto);

    void deleteLoan(Long id);

    LoanDto getCurrentLoan(Long id);

    List<LoanDto> getAllLoans();
}
