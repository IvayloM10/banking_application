package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.entities.*;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.*;
import com.example.banking_application.services.AccountService;
import com.example.banking_application.services.AdministrationService;
import com.example.banking_application.services.LoanService;
import com.example.banking_application.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class AdministrationServiceImpl implements AdministrationService {
    private final AdministratorRepository administratorRepository;
    private CurrentUser currentUser;

    private final ModelMapper modelMapper;
    private final BranchRepository branchRepository;

    private final TransactionRepository transactionRepository;

    private AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final UserService userService;

    private final CardRepository cardRepository;

    private final VirtualCardRepository virtualCardRepository;

    private final LoanService loanService;

    private final AccountService accountService;

    public AdministrationServiceImpl(AdministratorRepository administratorRepository, ModelMapper modelMapper, BranchRepository branchRepository, TransactionRepository transactionRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder, UserRepository userRepository, UserService userService, CardRepository cardRepository, VirtualCardRepository virtualCardRepository, LoanService loanService, AccountService accountService) {
        this.administratorRepository = administratorRepository;
        this.modelMapper = modelMapper;
        this.branchRepository = branchRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;


        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userService = userService;
        this.cardRepository = cardRepository;
        this.virtualCardRepository = virtualCardRepository;
        this.loanService = loanService;
        this.accountService = accountService;
    }

    @Override
    @Transient
    public void initialize() {
        //check if administrators are already created
        if(this.administratorRepository.findAll().size() > 0){
            return;
        }

        List<Currency> currencies = List.of(Currency.values());
        Long id = 1L;
        for (int i = 0; i < Currency.values().length; i++){
            Administrator administrator = new Administrator();
            Currency currency = currencies.get(i);
            administrator.setId(id);
            administrator.setUsername(String.join(" ", String.valueOf(currency)));
            administrator.setPassword(passwordEncoder.encode(String.join("",String.valueOf(i),String.valueOf(i),String.valueOf(i),String.valueOf(i))));
            administrator.setCurrency(currency);
            Account adminAccount = this.accountService.createNewAccount(100000, currency);

            administrator.setAccount(adminAccount);
            this.accountRepository.save(adminAccount);
            Branch branch;
            if(currency.equals(Currency.BGN)){
                branch = this.branchRepository.findByCurrency(Currency.EUR);
            }else {
                branch = this.branchRepository.findByCurrency(currency);
            }
            administrator.setBranch(branch);
            this.administratorRepository.save(administrator);

            id++;
        }
    }

    @Override
    public boolean loginAdmin(UserLoginDto userLoginDto) {
        Optional<Administrator> searchAdministrator = this.administratorRepository.findByUsername(userLoginDto.getUsername());

        if(searchAdministrator.isEmpty() || !searchAdministrator.get().getPassword().equals(userLoginDto.getPassword())){
            return false;
        }
        Administrator administrator = searchAdministrator.get();
        this.currentUser = this.modelMapper.map(userLoginDto, CurrentUser.class);
        this.currentUser.setId(administrator.getId());

        return true;
    }

    @Override
    public Administrator getCurrentAdmin(String id) {
        return this.administratorRepository.findByUsername(id).orElse(null);
    }

    @Override
    public CurrentUser getCurrentUser() {
        return this.currentUser;
    }

    @Override
    public void approveTransaction(Long id, String username) {
        getCurrentUserInfo(username);

        Optional<Transaction> searchedTransaction = this.transactionRepository.findById(id);
        if (searchedTransaction.isEmpty()) {
            throw new IllegalArgumentException("Invalid transaction ID");
        }

        Transaction transaction = searchedTransaction.get();

        //Get the user card number
        User sender = transaction.getMaker();
        User receiver = transaction.getReceiver();
        String cardNumber = "";
        if(transaction.getRecieverCardType().equals("card")){
           cardNumber = this.cardRepository.findByCardHolder(receiver).getCardNumber();
        }else if(transaction.getRecieverCardType().equals("Virtual card")){
            cardNumber = this.virtualCardRepository.findByCardHolder(receiver).getCardNumber();
        }
        Account senderAccount = this.accountService.getUserAccount(sender); //this.accountRepository.findByUser(sender);
        this.userService.handleRegularTransaction(senderAccount, cardNumber, transaction);

        transaction.setStatus("Approved!");
        this.transactionRepository.save(transaction);


        removeTransactionFromAdminView(transaction);
    }

    private void removeTransactionFromAdminView(Transaction transaction) {
        Administrator currentAdmin = getCurrentAdmin(this.currentUser.getUsername());
        Branch branch = currentAdmin.getBranch();
        List<Transaction> transactions = branch.getTransaction();

        int indexToRemove = IntStream.range(0, transactions.size())
                .filter(i -> transactions.get(i).getTransactionIdentifier().equals(transaction.getTransactionIdentifier()))
                .findFirst()
                .orElse(-1);

        if (indexToRemove != -1) {
            transactions.remove(indexToRemove);
        }

        currentAdmin.setBranch(branch);
        this.branchRepository.save(branch);
    }

    private void getCurrentUserInfo(String username) {
        this.currentUser = modelMapper.map(this.administratorRepository.findByUsername(username),CurrentUser.class);
    }

    @Override
    public void rejectTransaction(Long id,String username) {
        getCurrentUserInfo(username);
        Optional<Transaction> searchedTransaction = this.transactionRepository.findById(id);
        if (searchedTransaction.isEmpty()) {
            throw new IllegalArgumentException("Invalid transaction ID");
        }

        Transaction transaction = searchedTransaction.get();
        transaction.setStatus("Rejected");

        this.transactionRepository.save(transaction);

        User maker = transaction.getMaker();

        TransactionDetails senderDetail = new TransactionDetails();
        senderDetail.setTransactionId(transaction.getId());
        senderDetail.setDate(transaction.getDate());
        senderDetail.setDescription(transaction.getDescription());
        senderDetail.setCurrency(String.valueOf(transaction.getCurrency()));
        senderDetail.setStatus("Rejected!");
        senderDetail.setUser(maker);
        senderDetail.setAmount(Double.parseDouble(String.valueOf(transaction.getAmount())));
        senderDetail.setSign('-');
        maker.getTransactions().add(senderDetail);
        this.userRepository.save(maker);

        removeTransactionFromAdminView(transaction);
    }

    @Override
    public void approveLoan(Long id, String username) {
        getCurrentUserInfo(username);

        Loan currentLoan = this.loanService.getCurrentLoan(id);


        this.loanService.transferMoneyToUserAccount(id);
        BigDecimal amount = currentLoan.getAmount();
        Administrator currentAdmin = getCurrentAdmin(this.currentUser.getUsername());
        Account accountAdmin = currentAdmin.getAccount();
        accountAdmin.reduceAccount(Double.parseDouble(String.valueOf(amount)));
        this.administratorRepository.save(currentAdmin);
        this.accountRepository.save(accountAdmin);

        removeLoanFromAdminView( currentLoan);
    }



    @Override
    public void rejectLoan(Long id, String username) {
        getCurrentUserInfo(username);
        Loan currentLoan = this.loanService.getCurrentLoan(id);

        removeLoanFromAdminView(currentLoan);
        this.loanService.rejectLoan(id);
    }

    private void removeLoanFromAdminView(Loan currentLoan) {
        Administrator currentAdmin = getCurrentAdmin(this.currentUser.getUsername());

        Branch branch = currentAdmin.getBranch();
        List<Loan> loans = branch.getLoans();

        int indexToRemove = IntStream.range(0,loans.size())
                .filter(i ->loans.get(i).getId().equals(currentLoan.getId()))
                .findFirst()
                .orElse(-1);

        if (indexToRemove != -1) {
            loans.remove(indexToRemove);
        }

        currentAdmin.setBranch(branch);
        this.branchRepository.save(branch);
    }


}
