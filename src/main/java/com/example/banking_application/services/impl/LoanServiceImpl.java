package com.example.banking_application.services.impl;

import com.example.banking_application.models.dtos.AddLoanDto;
import com.example.banking_application.models.dtos.LoanDto;
import com.example.banking_application.models.entities.*;
import com.example.banking_application.repositories.*;
import com.example.banking_application.services.LoanCrudService;
import com.example.banking_application.services.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {
    private LoanRepository loanRepository;
    private UserRepository userRepository;

    private ModelMapper modelMapper;

    private BranchRepository branchRepository;

    private CardRepository cardRepository;

    private AccountRepository accountRepository;

    private LoanCrudService loanCrudService;
    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository, ModelMapper modelMapper, BranchRepository branchRepository, CardRepository cardRepository, AccountRepository accountRepository, LoanCrudService loanCrudService) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.branchRepository = branchRepository;
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.loanCrudService = loanCrudService;
    }

    @Override
    public void sendLoanForConfirmation(Long id) {
        LoanDto currentLoan = this.loanCrudService.getCurrentLoan(id);
        Optional<User> requesterOp = this.userRepository.findById(currentLoan.getRequesterId());

        if (requesterOp.isEmpty()) {
            throw new IllegalArgumentException("Invalid Loan Request ID");
        }
        User requester = requesterOp.get();
          currentLoan.setStatus("Sent");

          this.loanCrudService.updateLoan(id, currentLoan);

          Loan loan = this.modelMapper.map(currentLoan, Loan.class);
        Branch requesterBranch = requester.getBranch();
        requesterBranch.getLoans().add(loan);

        this.userRepository.save(requester);
        this.branchRepository.save(requesterBranch);
    }

    public List<Loan> syncUserLoans(Long id) {
        List<Loan> allLoans = this.loanCrudService.getAllLoans().stream().filter(loan -> loan.getRequesterId().equals(id)).map(e -> this.modelMapper.map(e, Loan.class)).toList();
        this.loanRepository.saveAllAndFlush(allLoans);
        return allLoans;
    }

    @Override
    public void transferMoneyToUserAccount(Long id) {
        Loan loanRequest = this.loanRepository.findById(id).orElse(null);
        if(loanRequest == null){
            throw new NullPointerException("Loan request could not be found!");
        }
        User requester = this.userRepository.findById(loanRequest.getRequesterId()).orElse(null);
        if(requester == null){
            throw new NullPointerException("Loan request could not be found!");
        }

        Card requesterCard = requester.getCard();
        requesterCard.setBalance(Double.parseDouble(String.valueOf(loanRequest.getAmount())) + requesterCard.getBalance());
        this.cardRepository.save(requesterCard);
        requester.setCard(requesterCard);

        Account requesterAccount = this.accountRepository.findByUser(requester);
        requesterAccount.addIntoAccount(Double.parseDouble(String.valueOf(loanRequest.getAmount())));
        this.accountRepository.save(requesterAccount);

//        requester.getLoans().add(loanRequest);

        TransactionDetails loanTransactionShowing= new TransactionDetails();
        loanTransactionShowing.setStatus("Received!");
        loanTransactionShowing.setDate(loanRequest.getDate());
        loanTransactionShowing.setDescription("Loan:" + loanRequest.getId());

        requester.getTransactions().add(loanTransactionShowing);
        this.userRepository.save(requester);
    }

    @Override
    public Loan getCurrentLoan(Long id) {
        return this.loanRepository.findById(id).orElse(null);
    }

    @Override
    public void rejectLoan(Long id) {
        this.loanCrudService.deleteLoan(id);

    }

    @Override
    public void deleteLoan(Long id) {
        this.loanRepository.deleteById(id);
        this.loanCrudService.deleteLoan(id);
    }
}
