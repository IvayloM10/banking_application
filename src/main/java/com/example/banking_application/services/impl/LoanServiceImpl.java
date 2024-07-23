package com.example.banking_application.services.impl;

import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.models.entities.Branch;
import com.example.banking_application.models.entities.Loan;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.BranchRepository;
import com.example.banking_application.repositories.LoanRepository;
import com.example.banking_application.repositories.UserRepository;
import com.example.banking_application.services.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanServiceImpl implements LoanService {
    private LoanRepository loanRepository;
    private UserRepository userRepository;

    private ModelMapper modelMapper;

    private BranchRepository branchRepository;

    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository, ModelMapper modelMapper, BranchRepository branchRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.branchRepository = branchRepository;
    }

    @Override
    public void sendLoanForConfirmation(LoanDto loanDto, Long id) {
        Optional<User> requesterOp = this.userRepository.findById(id);
        if (requesterOp.isEmpty()) {
            throw new IllegalArgumentException("Invalid transaction ID");
        }

        Loan loan = this.modelMapper.map(loanDto, Loan.class);

        User requester = requesterOp.get();
        loan.setRequester(requester);
        loan.setDate(LocalDate.now());
        loan.setAuthorized(false);
        loan.setStatus("Waiting...");
        loan.setLoanUniqueIdentifier(UUID.randomUUID().toString()); // Ensure uniqueness for every transaction, so it could be found later in the admin controller when approved or rejected


        requester.getLoans().add(loan);
        Branch requesterBranch = requester.getBranch();
        requesterBranch.getLoans().add(loan);

        this.loanRepository.save(loan);
        this.userRepository.save(requester);
        this.branchRepository.save(requesterBranch);
    }
}
