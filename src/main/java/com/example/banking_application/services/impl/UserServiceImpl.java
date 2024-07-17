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

private ExchangeRateService exchangeRateService;

    private CurrentUser currentUser;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, CardRepository cardRepository, AccountRepository accountRepository, VirtualCardRepository virtualCardRepository, BranchRepository branchRepository, ExchangeRateService exchangeRateService, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.virtualCardRepository = virtualCardRepository;
        this.branchRepository = branchRepository;
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
        byId.setCard(card);
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(byId);
        account.setBalance(50);
        account.setCurrency(card.getCurrency());
        byId.setAccount(account);
        this.accountRepository.save(account);
        VirtualCard virtualCard = this.modelMapper.map(card, VirtualCard.class);
        virtualCard.setBalance(50);
        virtualCard.setType(CardType.valueOf(cardDetails.getCardType()));
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
        Optional<User> senderAccount = this.userRepository.findById(this.currentUser.getId());
        Account account =  senderAccount.get().getAccount();



        if( senderAccount.get().getAccount().getBalance() < Double.parseDouble(String.valueOf(transactionDto.getAmountBase()))){
            throw new NotEnoughFundsException("I am sorry to inform you but you have no sufficient funds to execute transaction", senderAccount.get().getId());
        }
    senderAccount.get().getAccount().setBalance( senderAccount.get().getAccount().getBalance() - Double.parseDouble(String.valueOf(transactionDto.getAmountBase())));
//
        Optional<Account> receiverAccountNumber = this.accountRepository.findByAccountNumber(transactionDto.getAccountNumber());
        Account receiverAccount = receiverAccountNumber.get();



        BigDecimal convertAmount = this.exchangeRateService.convert(String.valueOf(senderAccount.get().getAccount().getCurrency()), String.valueOf(transactionDto.getCurrency()), transactionDto.getAmountBase());
        if(!receiverAccount.getCurrency().equals(transactionDto.getCurrency())) {
             convertAmount = this.exchangeRateService.convert(String.valueOf(transactionDto.getCurrency()), String.valueOf(receiverAccount.getCurrency()), convertAmount);
        }
        receiverAccountNumber.get().setBalance(receiverAccountNumber.get().getBalance() + Double.parseDouble(String.valueOf(convertAmount)));


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
