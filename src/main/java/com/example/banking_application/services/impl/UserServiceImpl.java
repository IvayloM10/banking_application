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
        virtualCard.setCardNumber(generateCardNumber());
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
    public void makeTransaction(TransactionDto transactionDto) {
        User sender = validateSenderPin(transactionDto);
        Account senderAccount = this.accountRepository.findByUser(sender);

        Transaction transaction = this.modelMapper.map(transactionDto, Transaction.class);
        transaction.setStatus("Waiting....");
        transaction.setAmount(transactionDto.getAmountBase());
        transaction.setSign("-");
        transaction.setDate(LocalDate.now());
        transaction.setMaker(sender);
        User receiver = getReceiverAndSetCardType(transactionDto, transaction);
        transaction.setReceiver(receiver);
       this.transactionRepository.save(transaction);

        sender.getTransactions().add(transaction);
        this.userRepository.save(sender);

        if (transactionDto.getAmountBase().compareTo(BigDecimal.valueOf(10.0)) > 0) {
            handleBranchTransaction(sender, transaction);
        } else {
            handleRegularTransaction(senderAccount, transactionDto.getCardNumber(), transaction);
        }
    }

    private User validateSenderPin(TransactionDto transactionDto) {
        Optional<User> senderAccounts = userRepository.findById(currentUser.getId());
        User sender = senderAccounts.orElseThrow(() -> new IllegalArgumentException("Sender can not found"));
        if (!sender.getCard().getPin().equals(transactionDto.getPin())) {
            throw new InvalidPinException("Invalid pin for card", transactionDto.getCardNumber());
        }
        return sender;
    }

    private User getReceiverAndSetCardType(TransactionDto transactionDto, Transaction transaction) {
        Optional<Card> receiverCard = this.cardRepository.findByCardNumber(transactionDto.getCardNumber());
        if (receiverCard.isPresent()) {
            User receiver = receiverCard.get().getCardHolder();
            transaction.setRecieverCardType("card");
            return receiver;
        }

        Optional<VirtualCard> receiverVirtualCard = this.virtualCardRepository.findByCardNumber(transactionDto.getCardNumber());
        if (receiverVirtualCard.isPresent()) {
            User receiver = receiverVirtualCard.get().getCardHolder();
            transaction.setRecieverCardType("Virtual card");
            return receiver;
        }

        throw new IllegalArgumentException("Receiver not found");
    }

    private void handleBranchTransaction(User sender, Transaction transaction) {
        Branch senderBranch = sender.getBranch();
        senderBranch.getTransaction().add(transaction);
        this.branchRepository.save(senderBranch);
        //TODO can remove it
        this.transactionRepository.save(transaction);
    }

    @Override
    public void handleRegularTransaction(Account senderAccount, String receiverCardNumber, Transaction transaction) {
        boolean cardType = receiverCardType(receiverCardNumber);
        if (cardType) {
            Optional<Card> receiverCard = this.cardRepository.findByCardNumber(receiverCardNumber);
            receiverCard.ifPresent(card -> cardReduction(senderAccount, transaction, card));
        } else {
            Optional<VirtualCard> receiverVirtualCard = this.virtualCardRepository.findByCardNumber(receiverCardNumber);
            receiverVirtualCard.ifPresent(card -> cardReduction(senderAccount, transaction, card));
        }
    }

    private boolean receiverCardType(String receiverCardNumber) {
        Optional<Card> card = this.cardRepository.findByCardNumber(receiverCardNumber);
        return card.isPresent();
    }


    @Override
    public void logout() {
        this.currentUser.setId(0L);
        this.currentUser.setUsername("guest");
        this.currentUser.setFullName("");
    }

    private void cardReduction(Account senderAccount,Transaction transaction, Card receiverCard) {

        BigDecimal convertAmount = convertAmount(transaction.getAmount(),String.valueOf(senderAccount.getCurrency()), String.valueOf(transaction.getCurrency()));
        convertAmount = convertAmount(convertAmount,String.valueOf(transaction.getCurrency()), String.valueOf(receiverCard.getCurrency()));

        User sender = senderAccount.getUser();

        userHasEnoughMoney(senderAccount, transaction);

        deductFunds(senderAccount,convertAmount);

        transactionSave(transaction.getId(), "Received", "-", sender);
        this.userRepository.save(sender);


        User cardHolder = receiverCard.getCardHolder();
        transactionSave(transaction.getId(), "Received", "+", cardHolder);
        this.userRepository.save(cardHolder);

        Account account = this.accountRepository.findByUser(cardHolder);
        account.setBalance(account.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));

         this.accountRepository.save(account);

        receiverCard.setBalance(receiverCard.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        this.cardRepository.save(receiverCard);
    };


    public void cardReduction(Account senderAccount, Transaction transaction, VirtualCard receiverCard) {

        BigDecimal convertAmount = convertAmount(transaction.getAmount(),String.valueOf(senderAccount.getCurrency()),String.valueOf(transaction.getCurrency()));
        convertAmount =  convertAmount(convertAmount,String.valueOf(transaction.getCurrency()), String.valueOf(receiverCard.getCurrency()));

       userHasEnoughMoney(senderAccount,transaction);

        User sender = senderAccount.getUser();
        transactionSave(transaction.getId(), "Received!", "-", sender);
        deductFunds(senderAccount,convertAmount);

        User cardHolder = receiverCard.getCardHolder();

        transactionSave(transaction.getId(), "Received!", "+", cardHolder);
        receiverCard.setBalance(receiverCard.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        Account recieverAccount = this.accountRepository.findByUser(cardHolder);
        recieverAccount.setBalance(recieverAccount.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        this.accountRepository.save(recieverAccount);
        this.virtualCardRepository.save(receiverCard);
    }

    private void transactionSave(Long transactionId, String status, String sign, User cardHolder) {
        Optional<Transaction> searchTransaction = this.transactionRepository.findById(transactionId);
        Transaction transaction = searchTransaction.get();
        transaction.setStatus(status);
        transaction.setSign(sign);
        cardHolder.getTransactions().add(transaction);
        this.userRepository.save(cardHolder);
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

    private void userHasEnoughMoney(Account senderAccount, Transaction transaction) {
        if( senderAccount.getBalance() < Double.parseDouble(String.valueOf(transaction.getAmount()))){
            throw new NotEnoughFundsException("I am sorry to inform you but you have no sufficient funds to execute transaction", senderAccount.getId());
        }
    }
    private BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        return exchangeRateService.convert(fromCurrency, toCurrency, amount);
    }

    private void deductFunds(Account senderAccount, BigDecimal amount) {
        Card physicalCard = this.cardRepository.findByCardHolder(senderAccount.getUser());
        VirtualCard virtualCard = this.virtualCardRepository.findByCardHolder(senderAccount.getUser());

        double physicalCardBalance = physicalCard.getBalance();
        double virtualCardBalance = virtualCard.getBalance();

        double amountAfterDeduction = Double.parseDouble(String.valueOf(amount)) - physicalCardBalance;
        if (amountAfterDeduction <= 0) {
            physicalCard.setBalance(physicalCardBalance - Double.parseDouble(String.valueOf(amount)));
            this.cardRepository.save(physicalCard);
        } else {
            physicalCard.setBalance(0);
            virtualCard.setBalance(virtualCardBalance - amountAfterDeduction);
            this.cardRepository.save(physicalCard);
            this.virtualCardRepository.save(virtualCard);
        }

        senderAccount.setBalance(senderAccount.getBalance() - Double.parseDouble(String.valueOf(amount)));
        this.accountRepository.save(senderAccount);
    }

}
