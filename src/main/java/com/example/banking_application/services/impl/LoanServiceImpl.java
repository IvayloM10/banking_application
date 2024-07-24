package com.example.banking_application.services.impl;

import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.models.entities.*;
import com.example.banking_application.repositories.*;
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

    private CardRepository cardRepository;

    private AccountRepository accountRepository;
    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository, ModelMapper modelMapper, BranchRepository branchRepository, CardRepository cardRepository, AccountRepository accountRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.branchRepository = branchRepository;
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
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

    @Override
    public void transferMoneyToUserAccount(Long id) {
        Loan loanRequest = this.loanRepository.findById(id).orElse(null);
        if(loanRequest == null){
            throw new NullPointerException("Loan request could not be found!");
        }
        User requester = loanRequest.getRequester();
        Card requesterCard = requester.getCard();
        requesterCard.setBalance(Double.parseDouble(String.valueOf(loanRequest.getAmount())) + requesterCard.getBalance());
        this.cardRepository.save(requesterCard);
        requester.setCard(requesterCard);

        Account requesterAccount = this.accountRepository.findByUser(requester);
        requesterAccount.addIntoAccount(Double.parseDouble(String.valueOf(loanRequest.getAmount())));
        this.accountRepository.save(requesterAccount);

        requester.getLoans().add(loanRequest);

        this.userRepository.save(requester);
    }

    @Override
    public Loan getCurrentLoan(Long id) {
        return this.loanRepository.findById(id).orElse(null);
    }

    @Override
    public void rejectLoan(Long id) {
        Loan searchedLoan = this.loanRepository.findById(id).orElse(null);

        if(searchedLoan == null){
            throw new NullPointerException("Loan not found!");
        }
        this.loanRepository.delete(searchedLoan);
    }
}
