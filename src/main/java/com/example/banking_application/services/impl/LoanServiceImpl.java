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
import java.util.UUID;
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
          currentLoan.setStatus("Draft");

        Branch requesterBranch = requester.getBranch();
        requesterBranch.getLoans().add(currentLoan);

        this.userRepository.save(requester);
        this.branchRepository.save(requesterBranch);
    }

    public void syncUserLoans() {
        List<LoanDto> allLoans = this.loanCrudService.getAllLoans();
        List<User> users = this.userRepository.findAll();

        for (User user : users) {
            for (LoanDto currentLoanDto : allLoans) {
                boolean loanExistsInUser = user.getLoans().stream()
                        .anyMatch(e -> e.getId().equals(currentLoanDto.getId()));

                if (!loanExistsInUser && currentLoanDto.getRequesterId().equals(user.getId())) {
                    user.getLoans().add(currentLoanDto);
//                    this.userRepository.save(user);
                }
            }
        }

//     TODO: find a way to get loans to user and then implement other logic   users.stream().forEach(user -> this.userRepository.save(user));
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
//        Loan searchedLoan = this.loanRepository.findById(id).orElse(null);
//
//        if(searchedLoan == null){
//            throw new NullPointerException("Loan not found!");
//        }
//        this.loanRepository.delete(searchedLoan);
    }
}
