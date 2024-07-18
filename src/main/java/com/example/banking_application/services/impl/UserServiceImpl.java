package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.TransactionDto;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.*;
import com.example.banking_application.models.entities.enums.CardType;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.*;
import com.example.banking_application.services.ExchangeRateService;
import com.example.banking_application.services.UserService;
import com.example.banking_application.services.exceptions.InvalidPinException;
import com.example.banking_application.services.exceptions.NoSuchCardException;
import com.example.banking_application.services.exceptions.NotEnoughFundsException;
import org.modelmapper.ModelMapper;
import org.springframework.data.annotation.Transient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private ModelMapper modelMapper;
    private CardRepository cardRepository;

    private AccountRepository accountRepository;

    private VirtualCardRepository virtualCardRepository;

    private BranchRepository branchRepository;
    private TransactionRepository transactionRepository;

private ExchangeRateService exchangeRateService;

    private CurrentUser currentUser;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, CardRepository cardRepository, AccountRepository accountRepository, VirtualCardRepository virtualCardRepository, BranchRepository branchRepository, TransactionRepository transactionRepository, ExchangeRateService exchangeRateService, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.virtualCardRepository = virtualCardRepository;
        this.branchRepository = branchRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;

        this.currentUser = currentUser;
    }

    @Override
    @Transient
    public boolean register(UserRegisterDto userRegisterDto) {
        if(!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())){
            return false;
        }

        Optional<User> possibleSameUser = this.userRepository.findByUsernameOrEmail(userRegisterDto.getUsername(), userRegisterDto.getEmail());

        if(possibleSameUser.isPresent()){
            return false;
        }

        User user = modelMapper.map(userRegisterDto, User.class);
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        Branch regionBranch = this.branchRepository.findByRegion(userRegisterDto.getRegion());
        user.setBranch(regionBranch);
        this.userRepository.save(user);
        this.currentUser.setUsername(userRegisterDto.getUsername());
        return true;
    }

    @Override
    public boolean login(UserLoginDto userLoginDto) {

        Optional<User> searchedUser = this.userRepository.findByUsername(userLoginDto.getUsername());

        if(searchedUser.isEmpty()){
            return false;
        }
        User user = searchedUser.get();

        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())){
            return false;
        }

        this.currentUser = this.modelMapper.map(userLoginDto,CurrentUser.class);
        this.currentUser.setId(user.getId());
        this.currentUser.setFullName(String.join(" ",user.getFirstName(), user.getLastName()));

        return true;
    }
    @Override
    public void createCardAndAccountForUser(CardDto cardDetails) {
        User byId = this.userRepository.findByUsername(this.currentUser.getUsername()).get();
        Card card = new Card();
        card.setCardHolder(byId);
        card.setCvvNumber(generateCVV());
        card.setCardNumber(generateCardNumber());
        card.setExpirationDate(LocalDate.now());
        card.setType(CardType.valueOf(cardDetails.getCardType()));
        card.setCurrency(Currency.valueOf(cardDetails.getCurrency()));
        card.setPin(cardDetails.getPin());
        card.setBalance(50);
        byId.setCard(card);

        VirtualCard virtualCard = this.modelMapper.map(card, VirtualCard.class);
        virtualCard.setBalance(50);
        virtualCard.setType(CardType.valueOf(cardDetails.getCardType()));


        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(byId);
        account.setBalance(virtualCard.getBalance() + card.getBalance());
        account.setCurrency(card.getCurrency());

        byId.setAccount(account);
        this.accountRepository.save(account);
        this.virtualCardRepository.save(virtualCard);
        this.cardRepository.save(card);
        this.currentUser.setUsername("");
    }

    @Override
    public User getUser(String username) {
        return this.userRepository.findByUsername(username).get();
    }

    @Override
    public void makeTransaction( TransactionDto transactionDto) {
        Optional<User> senderAccounts = this.userRepository.findById(this.currentUser.getId());
        User sender = senderAccounts.get();
        if(!sender.getCard().getPin().equals(transactionDto.getPin())){
            throw new InvalidPinException("Invalid pin for card", transactionDto.getCardNumber());
        }
        Account senderAccount =  this.accountRepository.findByUser(sender);

        Transaction transaction = this.modelMapper.map(transactionDto, Transaction.class);

        transaction.setStatus("Received!");
        transaction.setSign("-");
        transaction.setDate(LocalDate.now());
        this.transactionRepository.save(transaction);
        sender.getTransactions().add(transaction);
        this.userRepository.save(sender);
        if(transactionDto.getAmountBase().compareTo(BigDecimal.valueOf(10000.0)) > 0){
            transaction.setStatus("Waiting....");
            Branch senderBranch = sender.getBranch();
                   senderBranch .getTransaction().add(transaction);
            this.branchRepository.save(senderBranch);

        }else {
            boolean cardType = receiverCardType(transactionDto);
            if(cardType){
                Optional<Card> byCardNumber = this.cardRepository.findByCardNumber(transactionDto.getCardNumber());
                cardReduction(senderAccount,transactionDto, byCardNumber.get(),transaction.getId());
            }else{
                Optional<VirtualCard> optionalVirtualCard = this.virtualCardRepository.findByCardNumber(transactionDto.getCardNumber());
                cardReduction(senderAccount,transactionDto, optionalVirtualCard.get(),transaction.getId());
            }

        }
    }

    private void cardReduction(Account senderAccount,TransactionDto transactionDto, Card receiverCard, Long transactionId) {

        BigDecimal convertAmount = this.exchangeRateService.convert(String.valueOf(senderAccount.getCurrency()), String.valueOf(transactionDto.getCurrency()), transactionDto.getAmountBase());
        convertAmount = this.exchangeRateService.convert(String.valueOf(transactionDto.getCurrency()), String.valueOf(receiverCard.getCurrency()), convertAmount);

        User sender = senderAccount.getUser();
        if( senderAccount.getBalance() < Double.parseDouble(String.valueOf(transactionDto.getAmountBase()))){
                throw new NotEnoughFundsException("I am sorry to inform you but you have no sufficient funds to execute transaction", sender.getId());
            }

            senderAccount.setBalance(senderAccount.getBalance() - Double.parseDouble(String.valueOf(convertAmount)));

        Card senderCard = this.cardRepository.findByCardHolder(sender);
        Optional<Transaction> byId = this.transactionRepository.findById(transactionId);

        Transaction transaction = byId.get();
        transaction.setStatus("Received");
        transaction.setSign("-");
        sender.getTransactions().add(transaction);
        this.userRepository.save(sender);

        senderCard.setBalance(senderCard.getBalance() - Double.parseDouble(String.valueOf(convertAmount)));

        this.accountRepository.save(senderAccount);

        User cardHolder = receiverCard.getCardHolder();
        Optional<Transaction> searchTransaction = this.transactionRepository.findById(transactionId);

        Transaction transaction1 = searchTransaction.get();
        transaction1.setStatus("Received");
        transaction1.setSign("+");
        cardHolder.getTransactions().add(transaction1);
        this.userRepository.save(cardHolder);


        Account account = this.accountRepository.findByUser(cardHolder);
        account.setBalance(account.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));

         this.accountRepository.save(account);
         this.cardRepository.save(senderCard);
        receiverCard.setBalance(receiverCard.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        this.cardRepository.save(receiverCard);

    }

    public void cardReduction(Account senderAccount, TransactionDto transactionDto, VirtualCard receiverCard, Long transactionId) {

        BigDecimal convertAmount = this.exchangeRateService.convert(String.valueOf(senderAccount.getCurrency()), String.valueOf(transactionDto.getCurrency()), transactionDto.getAmountBase());
        convertAmount = this.exchangeRateService.convert(String.valueOf(transactionDto.getCurrency()), String.valueOf(receiverCard.getCurrency()), convertAmount);

        if( senderAccount.getBalance() < Double.parseDouble(String.valueOf(transactionDto.getAmountBase()))){
            throw new NotEnoughFundsException("I am sorry to inform you but you have no sufficient funds to execute transaction", senderAccount.getUser().getId());
        }

        senderAccount.setBalance(senderAccount.getBalance() - Double.parseDouble(String.valueOf(convertAmount)));
        VirtualCard senderCard = this.virtualCardRepository.findByCardHolder(senderAccount.getUser());
        senderCard.setBalance(senderCard.getBalance() - Double.parseDouble(String.valueOf(convertAmount)));
        this.accountRepository.save(senderAccount);
        this.virtualCardRepository.save(senderCard);

        Account account = receiverCard.getCardHolder().getAccount();
        account.setBalance(account.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));

        this.accountRepository.save(account);
        receiverCard.setBalance(receiverCard.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        this.virtualCardRepository.save(receiverCard);

    }

    private boolean receiverCardType(TransactionDto transactionDto){
        Optional<Card> byCardNumber = this.cardRepository.findByCardNumber(transactionDto.getCardNumber());
        Optional<VirtualCard> optionalVirtualCard = this.virtualCardRepository.findByCardNumber(transactionDto.getCardNumber());
        if(byCardNumber.isPresent() ){
            return true;
        }

        if(optionalVirtualCard.isPresent()){
            return false;
        }

        throw new NoSuchCardException("No such card found", transactionDto.getCardNumber());
    }

    @Override
    public User getCurrentUser() {
        return this.userRepository.findByUsername(this.currentUser.getUsername()).get();
    }

    private String generateAccountNumber() {
        return "UB" + new Random().nextInt(999999);
    }

    private String generateCardNumber() {
        return "CARD" + new Random().nextInt(99999999);
    }

    private String generateCVV() {
        return String.valueOf(new Random().nextInt(999));
    }

}
