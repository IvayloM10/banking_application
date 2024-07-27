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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private ModelMapper modelMapper;
    private CardRepository cardRepository;

    private AccountRepository accountRepository;

    private VirtualCardRepository virtualCardRepository;

    @Autowired
    private final BranchRepository branchRepository;
    private TransactionRepository transactionRepository;

    private ExchangeRateService exchangeRateService;


    @Autowired
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

        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword()) || !user.getUsername().equals(userLoginDto.getUsername())){
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
        //creating a new card
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

        //getting a new virtual card
        VirtualCard virtualCard = this.modelMapper.map(card, VirtualCard.class);
        virtualCard.setCardNumber(generateCardNumber());
        virtualCard.setBalance(50);
        virtualCard.setType(CardType.valueOf(cardDetails.getCardType()));

        //creating an account
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
    public void makeTransaction(TransactionDto transactionDto, String username) {
        this.currentUser = modelMapper.map(this.userRepository.findByUsername(username).get(),CurrentUser.class);
        User sender = validateSenderPin(transactionDto);
        Account senderAccount = this.accountRepository.findByUser(sender);

        Transaction transaction = getTransaction(transactionDto, sender);

        // For amounts greater than or equal to 10,000 a validation is required from the administrator
        if (transactionDto.getAmountBase().compareTo(BigDecimal.valueOf(10.0)) >= 0) {
            handleBranchTransaction(sender, transaction);
        } else {
            handleRegularTransaction(senderAccount, transactionDto.getCardNumber(), transaction);
        }
    }

    @Transient
    private Transaction getTransaction(TransactionDto transactionDto, User sender) {
        Transaction transaction = this.modelMapper.map(transactionDto, Transaction.class);
        transaction.setStatus("Waiting....");
        transaction.setAmount(transactionDto.getAmountBase());
        transaction.setSign("-");
        transaction.setDate(LocalDate.now());
        transaction.setMaker(sender);
        transaction.setTransactionIdentifier(UUID.randomUUID().toString()); // Ensure uniqueness for every transaction, so it could be found later in the admin controller when approved or rejected


        User receiver = getReceiverAndSetCardType(transactionDto, transaction);
        transaction.setReceiver(receiver);

        return transaction;
    }

    private User validateSenderPin(TransactionDto transactionDto) {
        Optional<User> senderAccounts = this.userRepository.findById(this.currentUser.getId());
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

    @Transient
    private void handleBranchTransaction(User sender, Transaction transaction) {
        this.transactionRepository.save(transaction);
        Branch senderBranch = sender.getBranch();
        senderBranch.getTransaction().add(transaction);
        this.branchRepository.save(senderBranch);


    }

    @Override
    @Transactional
    public void handleRegularTransaction(Account senderAccount, String receiverCardNumber, Transaction transaction) {

        transaction.setStatus("Received!");


        // Ensure that the maker and receiver are set
        User maker = userRepository.findByUsername(transaction.getMaker().getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Maker not found"));
        User receiver = userRepository.findByUsername(transaction.getReceiver().getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        transaction.setMaker(maker);
        transaction.setReceiver(receiver);

        transaction = this.transactionRepository.save(transaction);

        // Sender
        Optional<User> senderOp = this.userRepository.findByUsername(transaction.getMaker().getUsername());
        if (senderOp.isPresent()) {


            TransactionDetails senderDetail = new TransactionDetails();
            senderDetail.setTransactionId(transaction.getId());
            senderDetail.setDate(transaction.getDate());
            senderDetail.setDescription(transaction.getDescription());
            senderDetail.setCurrency(String.valueOf(transaction.getCurrency()));
            senderDetail.setStatus("Received!");
            senderDetail.setUser(maker);
            senderDetail.setAmount(Double.parseDouble(String.valueOf(transaction.getAmount())));
            senderDetail.setSign('-');

            maker.getMadeTransactions().add(transaction);
            maker.getTransactions().add(senderDetail);
            this.userRepository.save(maker);
        }


        Optional<User> receiverOp = this.userRepository.findByUsername(transaction.getReceiver().getUsername());
        if (receiverOp.isPresent()) {


            TransactionDetails receiverDetail = new TransactionDetails();
            receiverDetail.setTransactionId(transaction.getId());
            receiverDetail.setDate(transaction.getDate());
            receiverDetail.setDescription(transaction.getDescription());
           receiverDetail.setCurrency(String.valueOf(transaction.getCurrency()));
           receiverDetail.setStatus("Received!");
            receiverDetail.setUser(receiver);
            receiverDetail.setAmount(Double.parseDouble(String.valueOf(transaction.getAmount())));
            receiverDetail.setSign('+');

            receiver.getReceivedTransactions().add(transaction);
            receiver.getTransactions().add(receiverDetail);
            this.userRepository.save(receiver);


            boolean cardType = receiverCardType(receiverCardNumber);
            if (cardType) {
                Optional<Card> receiverCard = this.cardRepository.findByCardNumber(receiverCardNumber);
                Transaction finalTransaction = transaction;
                receiverCard.ifPresent(card -> cardReduction(senderAccount, finalTransaction, card));
            } else {
                Optional<VirtualCard> receiverVirtualCard = this.virtualCardRepository.findByCardNumber(receiverCardNumber);
                Transaction finalTransaction1 = transaction;
                receiverVirtualCard.ifPresent(card -> cardReduction(senderAccount, finalTransaction1, card));
            }
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


@Transient
    private void cardReduction(Account senderAccount,Transaction transaction, Card receiverCard) {

        //convert the amount that is asked in
        BigDecimal convertAmount = convertAmount(transaction.getAmount(),String.valueOf(senderAccount.getCurrency()), String.valueOf(transaction.getCurrency()));

        //Get the user who sends it
    User sender = senderAccount.getUser();

    //check the user having enough money
    userHasEnoughMoney(senderAccount,convertAmount);

    //deduct the amount in the currency of the user
    deductFunds(senderAccount,convertAmount);

        convertAmount = convertAmount(convertAmount,String.valueOf(transaction.getCurrency()), String.valueOf(receiverCard.getCurrency()));

//        User sender = senderAccount.getUser();
//
//        userHasEnoughMoney(senderAccount, transaction);
//
//        deductFunds(senderAccount,convertAmount);


        this.userRepository.save(sender);

       //Get the cardholder at the receiving nd
        User cardHolder = receiverCard.getCardHolder();

        this.userRepository.save(cardHolder);

        //Update reciever account
        Account account = this.accountRepository.findByUser(cardHolder);
        account.setBalance(account.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));

         this.accountRepository.save(account);

    //update reciever card balance to reflect the amount added
    receiverCard.setBalance(receiverCard.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        this.cardRepository.save(receiverCard);
    };


    public void cardReduction(Account senderAccount, Transaction transaction, VirtualCard receiverCard) {

        // convert to the currency of the sender the amount
        BigDecimal convertAmount = convertAmount(transaction.getAmount(),String.valueOf(senderAccount.getCurrency()),String.valueOf(transaction.getCurrency()));

        //Check whether the sender has enough amount
        userHasEnoughMoney(senderAccount,convertAmount);


        User sender = senderAccount.getUser();

        this.userRepository.save(sender);
        deductFunds(senderAccount,convertAmount);

        //convert to receiver's currency
        convertAmount =  convertAmount(convertAmount,String.valueOf(transaction.getCurrency()), String.valueOf(receiverCard.getCurrency()));

//       userHasEnoughMoney(senderAccount,transaction);
//
//        User sender = senderAccount.getUser();
//
//
//
//        this.userRepository.save(sender);
//        deductFunds(senderAccount,convertAmount);

        User cardHolder = receiverCard.getCardHolder();
        this.userRepository.save(cardHolder);
        receiverCard.setBalance(receiverCard.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        Account recieverAccount = this.accountRepository.findByUser(cardHolder);
        recieverAccount.setBalance(recieverAccount.getBalance() + Double.parseDouble(String.valueOf(convertAmount)));
        this.accountRepository.save(recieverAccount);
        this.virtualCardRepository.save(receiverCard);
    }

    @Override
    public User getCurrentUser(String username) {
        return this.userRepository.findByUsername(username).get();
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

    private void userHasEnoughMoney(Account senderAccount, BigDecimal convertAmount) {
        if( senderAccount.getBalance() < Double.parseDouble(String.valueOf(convertAmount))){
            throw new NotEnoughFundsException("I am sorry to inform you but you have no sufficient funds to execute transaction", senderAccount.getId());
        }
    }
    private BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        return exchangeRateService.convert(fromCurrency, toCurrency, amount);
    }

    // method to check if the card to send the funds has the amount if not then take from the virtual card
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
          senderAccount.getUser().setCard(physicalCard);
        senderAccount.getUser().setVirtualCard(virtualCard);
        senderAccount.setBalance(senderAccount.getBalance() - Double.parseDouble(String.valueOf(amount)));
        this.accountRepository.save(senderAccount);
    }

}
