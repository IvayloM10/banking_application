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
import com.example.banking_application.services.AccountService;
import com.example.banking_application.services.ExchangeRateService;
import com.example.banking_application.services.VirtualCardService;
import com.example.banking_application.services.exceptions.NotEnoughFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final String PASSWORD = "pass123";

    private static final String WRONG_PASSWORD = "wrongPass123";

    private static final String ENCODED_PASSWORD = "encodedPass123";

    private static final String USERNAME = "username";

    private static final String INCORRECT_USERNAME = "incorrectUsername";

    private static final String CARD_NUMBER = "1234";
    private static final String VIRTUAL_CARD_NUMBER = "1111";

    private UserServiceImpl toTest;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private PasswordEncoder mockPasswordEncoder;

    @Mock
    private ModelMapper mockModelMapper = new ModelMapper();


    @Mock
    private CardRepository mockCardRepository;

    @Mock
    private AccountRepository mockAccountRepository;

    @Mock
    private VirtualCardRepository mockVirtualCardRepository;

    @Mock
    private BranchRepository mockBranchRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeRateService mockExchangeRateService;
    @Mock
    private AccountService mockAccountService;
    @Mock
    private VirtualCardService mockVirtualCardService;

    @Mock
    private CurrentUser mockCurrentUser;


    @BeforeEach
    void setUp() {
        this.toTest = new UserServiceImpl(mockUserRepository, mockPasswordEncoder, mockModelMapper, mockCardRepository, mockAccountRepository, mockVirtualCardRepository, mockBranchRepository, transactionRepository, mockExchangeRateService, mockAccountService, mockVirtualCardService,mockCurrentUser);
    }

    @Test
    void testReturnFalseWhenPasswordsDoNotMatch() {
        // Arrange - a received UserRegisterDto, create one
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setPassword(PASSWORD);
        userRegisterDto.setConfirmPassword(WRONG_PASSWORD);

        // Act - call register method to register User
        boolean result = (this.toTest.register(userRegisterDto));

        // Assert - check if returns false
        assertThat(result).isFalse();
        //check for no user have been added
        verify(this.mockUserRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUserSuccessfully() {
        // Arrange - userRegisterDto to start the process
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setPassword(PASSWORD);
        userRegisterDto.setConfirmPassword(PASSWORD);
        userRegisterDto.setUsername(USERNAME);
        userRegisterDto.setEmail("email@example.com");
        userRegisterDto.setRegion("region");

        //
        User user = new User();
        Branch branch = new Branch();

        when(this.mockUserRepository.findByUsernameOrEmail(userRegisterDto.getUsername(), userRegisterDto.getEmail()))
                .thenReturn(Optional.empty());
        when(this.mockModelMapper.map(userRegisterDto, User.class)).thenReturn(user);
        when(this.mockPasswordEncoder.encode(userRegisterDto.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(this.mockBranchRepository.findByRegion(userRegisterDto.getRegion())).thenReturn(branch);

        // Act - register the user
        boolean result = this.toTest.register(userRegisterDto);

        // Assert
        //check if result is true so to send right to controller
        assertThat(result).isTrue();
        //check password is same
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
        //check you get the right branch
        assertThat(user.getBranch()).isEqualTo(branch);
        //check the repo saves the user
        verify(this.mockUserRepository).save(user);
    }

    @Test
    void testReturnFalseWhenUserDoesNotExist() {
        // Arrange - create a userLoginDto with wrong username
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setUsername(INCORRECT_USERNAME);
        userLoginDto.setPassword(PASSWORD);

        when(this.mockUserRepository.findByUsername(userLoginDto.getUsername())).thenReturn(Optional.empty());

        // Act - call login method
        boolean result = this.toTest.login(userLoginDto);

        // Assert - check if the result is false
        assertThat(result).isFalse();
    }


    @Test
    void testReturnFalseWhenPasswordIsIncorrect() {
        // Arrange - create userLoginDto so to start the login process
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setUsername(USERNAME);
        userLoginDto.setPassword(WRONG_PASSWORD);

        // create a user to we have an existing one to match the userLoginDto
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);

        when(this.mockUserRepository.findByUsername(userLoginDto.getUsername())).thenReturn(Optional.of(user));
        when(this.mockPasswordEncoder.matches(userLoginDto.getPassword(), user.getPassword())).thenReturn(false);

        // Act - call login method
        boolean result = this.toTest.login(userLoginDto);

        // Assert - check the result returns false
        assertThat(result).isFalse();
    }

    @Test
    void testReturnTrueWhenLoginIsSuccessful() {
        // Arrange - create userLoginDto to start the login process
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setUsername(USERNAME);
        userLoginDto.setPassword(PASSWORD);

        // create user with the encoded pass to have in store in the repo
        User user = new User();
        user.setId(1L);
        user.setUsername(USERNAME);
        user.setPassword(ENCODED_PASSWORD);
        user.setFirstName("Test");
        user.setLastName("Testov");


        when(this.mockUserRepository.findByUsername(userLoginDto.getUsername())).thenReturn(Optional.of(user));
        when(this.mockPasswordEncoder.matches(userLoginDto.getPassword(), user.getPassword())).thenReturn(true);

        // Adding instance of currentUser to set with the parameters
        this.mockCurrentUser = new CurrentUser();
        this.mockCurrentUser.setId(user.getId());
        this.mockCurrentUser.setFullName("Test Testov");
        this.mockCurrentUser.setUsername(userLoginDto.getUsername());

        when(this.mockModelMapper.map(userLoginDto, CurrentUser.class)).thenReturn(this.mockCurrentUser);

        // Act - call login method
        boolean result = this.toTest.login(userLoginDto);

        // Assert
        // check if the result returns true
        assertThat(result).isTrue();
        // check if currentUser has an id equal to that of the user that logs in
        assertThat(this.mockCurrentUser.getId()).isEqualTo(user.getId());
        // check if currentUser has fullName the same as the fullName of the user
        assertThat(this.mockCurrentUser.getFullName()).isEqualTo("Test Testov");

    }

    @Test
    void testCreateACardAndAccount() {
        // Arrange
        mockCurrentUser.setUsername(USERNAME);
        mockCurrentUser.setId(1L);

        // Create and set up the user
        User user = new User();
        user.setUsername(USERNAME);

        // Create the CardDto
        CardDto cardDto = new CardDto();
        cardDto.setCardType(String.valueOf(CardType.Mastercard));
        cardDto.setCurrency("USD");
        cardDto.setPin("1234");

        // Create the Card
        Card card = new Card();
        card.setCardHolder(user);
        card.setCvvNumber("123");
        card.setCardNumber("CARD456789");
        card.setExpirationDate(LocalDate.now());
        card.setType(CardType.Mastercard);
        card.setCurrency(Currency.USD);
        card.setPin("1234");
        card.setBalance(50);

        user.setCard(card);
        // Create the VirtualCard
        VirtualCard virtualCard = new VirtualCard();
        virtualCard.setCardNumber("987654");
        virtualCard.setBalance(50);
        virtualCard.setType(CardType.Mastercard);

        // Create the Account
        Account account = new Account();
        account.setBalance(100); // 50 (card) + 50 (virtual card)
        account.setCurrency(Currency.USD);
        account.setUser(user);

        user.setAccount(account);

        // Set the card to the user
        user.setCard(card);

        // Set up the mocks
        when(mockCurrentUser.getUsername()).thenReturn(USERNAME);
        Mockito.lenient().when(mockUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        Mockito.lenient().when(mockVirtualCardService.generateCardNumber()).thenReturn("987654");
        Mockito.lenient().when(mockAccountService.createNewAccount(100, Currency.USD)).thenReturn(account);
        Mockito.lenient().when(mockModelMapper.map(any(Card.class), eq(VirtualCard.class))).thenReturn(virtualCard);
        Mockito.lenient().when(mockCardRepository.save(any(Card.class))).thenReturn(card);
        Mockito.lenient().when(mockVirtualCardRepository.save(any(VirtualCard.class))).thenReturn(virtualCard);
        Mockito.lenient().when(mockAccountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        toTest.createCardAndAccountForUser(cardDto);

        // Assert
        verify(mockUserRepository).findByUsername(USERNAME);
        verify(mockVirtualCardService).generateCardNumber();
        verify(mockAccountService).createNewAccount(100, Currency.USD);
        // capture argument
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        ArgumentCaptor<VirtualCard> virtualCardCaptor = ArgumentCaptor.forClass(VirtualCard.class);
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

// Capture the arguments passed to the save method
        verify(mockCardRepository).save(cardCaptor.capture());
        verify(mockVirtualCardRepository).save(virtualCardCaptor.capture());
        verify(mockAccountRepository).save(accountCaptor.capture());

// check the list of entities
        List<Card> savedCards = cardCaptor.getAllValues();
        List<VirtualCard> savedVirtualCards = virtualCardCaptor.getAllValues();
        List<Account> savedAccounts = accountCaptor.getAllValues();

// Verify the size of each list or collection is equal to one
        assertEquals(1, savedCards.size());
        assertEquals(1, savedVirtualCards.size());
        assertEquals(1, savedAccounts.size());

        // Assertions to verify the user has the correct card and account
        assertThat(user.getCard().getCardHolder().getUsername()).isEqualTo(card.getCardHolder().getUsername());
        assertThat(user.getAccount().getUser().getUsername()).isEqualTo(account.getUser().getUsername());
    }

    @Test
    void testValidatePinSuccess(){
        // Arrange
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setPin("1234");

            mockCurrentUser.setUsername(USERNAME);
            mockCurrentUser.setId(1L);

            // Create and set up the user
            User user = new User();
            user.setId(1L);
            user.setUsername(USERNAME);


            // Create the Card
            Card card = new Card();
            card.setCardHolder(user);
            card.setCvvNumber("123");
            card.setCardNumber("CARD456789");
            card.setExpirationDate(LocalDate.now());
            card.setType(CardType.Mastercard);
            card.setCurrency(Currency.USD);
            card.setPin("1234");
            card.setBalance(50);

            user.setCard(card);

        Mockito.lenient().when(mockCurrentUser.getId()).thenReturn(1L);
        Mockito.lenient().when(mockUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        Mockito.lenient().when(mockUserRepository.findById(1L)).thenReturn(Optional.of(user));

        //Act
        User result = toTest.validateSenderPin(transactionDto);

        //Assert
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getCard().getCardNumber(), result.getCard().getCardNumber());
    }

    @Test
    void testGetReceiverAndSetCardTypeCardPresent() {
        // Arrange
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setCardNumber(CARD_NUMBER);

        Transaction transaction = new Transaction();

        User receiver = new User();
        Card card = new Card();
        card.setCardHolder(receiver);

        when(mockCardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.of(card));

        // Act
        User result = toTest.getReceiverAndSetCardType(transactionDto, transaction);

        // Assert
        assertEquals(receiver, result);
        assertEquals("card", transaction.getRecieverCardType());
    }

    @Test
    void testGetReceiverAndSetCardTypeVirtualCardPresent() {
        // Arrange
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setCardNumber(CARD_NUMBER);

        Transaction transaction = new Transaction();

        User receiver = new User();
        VirtualCard virtualCard = new VirtualCard();
        virtualCard.setCardHolder(receiver);

        when(mockCardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.empty());
        when(mockVirtualCardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.of(virtualCard));

        // Act
        User result = toTest.getReceiverAndSetCardType(transactionDto, transaction);

        // Assert
        assertEquals(receiver, result);
        assertEquals("Virtual card", transaction.getRecieverCardType());
    }

    @Test
    void testGetReceiverAndSetCardTypeReceiverNotFound() {
        //Arrange
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setCardNumber(CARD_NUMBER);

        Transaction transaction = new Transaction();

        when(mockCardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.empty());
        when(mockVirtualCardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            toTest.getReceiverAndSetCardType(transactionDto, transaction);
        });
        //Assert
        assertEquals("Receiver not found", exception.getMessage());
    }

    @Test
    void testHandleBranchTransaction() {
        //Assert
        // Create a sender
        User sender = new User();
        Branch branch = new Branch();
        sender.setBranch(branch);

        // Create a transaction
        Transaction transaction = new Transaction();

        // Act
        toTest.handleBranchTransaction(sender, transaction);

        // check the transaction repo saving it
        verify(transactionRepository, times(1)).save(transaction);

        // check containing the transaction
        assertTrue(branch.getTransaction().contains(transaction));

        // check number of saved units in branch
        verify(mockBranchRepository, times(1)).save(branch);
    }

    @Test
    void testUserHasEnoughMoneyFundsSuccess() {
        // Arrange
        Account senderAccount = new Account();
        senderAccount.setBalance(200.0);

        BigDecimal convertAmount = BigDecimal.valueOf(100.0);

        //Assert check if exception is thrwon
        assertDoesNotThrow(() -> toTest.userHasEnoughMoney(senderAccount, convertAmount));
    }

    @Test
    void testUserHasEnoughMoneyFundsUnsuccessful() {
        //Arrange
        Account senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setBalance(50.0);

        BigDecimal convertAmount = BigDecimal.valueOf(100.0);

        // check if the exception is thrown
        NotEnoughFundsException exception = assertThrows(NotEnoughFundsException.class,
                () -> toTest.userHasEnoughMoney(senderAccount, convertAmount));

        // Assert
        assertEquals("I am sorry to inform you but you have no sufficient funds to execute transaction", exception.getMessage());
        assertEquals(senderAccount.getId(), exception.getId());
    }

    @Test
    void testGetCurrentUserExists() {
        // Arrange
        String username = USERNAME;
        User expectedUser = new User();
        expectedUser.setUsername(username);

        // Mocking the repository's response
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // Execute the method
        User actualUser = toTest.getCurrentUser(username);

        // Verify the result
        assertEquals(expectedUser, actualUser);

        // Verify that the repository was called with the correct parameter
        verify(mockUserRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetCurrentUserDoesNotExist() {
        // Arrange
        String username = USERNAME;

        // make sure we get the right thing from the mockUserRepo when called
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        //Act
        assertThrows(NoSuchElementException.class, () -> toTest.getCurrentUser(username));

        // Assert
        verify(mockUserRepository, times(1)).findByUsername(username);
    }

    @Test
    void testDeductFundsEnoughFundsWithPhysicalCardBalance() {
        // Arrange
        Account senderAccount = new Account();
        senderAccount.setBalance(1000.0);

        User user = new User();
        user.setUsername("user1");
        senderAccount.setUser(user);

        Card physicalCard = new Card();
        physicalCard.setCardHolder(user);
        physicalCard.setBalance(200.0);

        VirtualCard virtualCard = new VirtualCard();
        virtualCard.setCardHolder(user);
        virtualCard.setBalance(300.0);

        BigDecimal amount = BigDecimal.valueOf(150.0);


        when(mockCardRepository.findByCardHolder(user)).thenReturn(physicalCard);
        when(mockVirtualCardRepository.findByCardHolder(user)).thenReturn(virtualCard);

        //Act
        toTest.deductFunds(senderAccount, amount);

        //Assert
        assertEquals(50.0, physicalCard.getBalance());
        assertEquals(300.0, virtualCard.getBalance()); // Unchanged
        assertEquals(850.0, senderAccount.getBalance()); // 1000 - 150

        // check if saved in repo
        verify(mockCardRepository, times(1)).save(physicalCard);
        verify(mockVirtualCardRepository, never()).save(any());
        verify(mockAccountRepository, times(1)).save(senderAccount);
    }

    @Test
    void testDeductFundsNotEnoughFundsWithPhysicalCardBalance() {
        // Arrange
        Account senderAccount = new Account();
        senderAccount.setBalance(1000.0);

        User user = new User();
        user.setUsername("user2");
        senderAccount.setUser(user);

        Card physicalCard = new Card();
        physicalCard.setCardHolder(user);
        physicalCard.setBalance(100.0);

        VirtualCard virtualCard = new VirtualCard();
        virtualCard.setCardHolder(user);
        virtualCard.setBalance(200.0);

        BigDecimal amount = BigDecimal.valueOf(150.0);

        // Mocking
        when(mockCardRepository.findByCardHolder(user)).thenReturn(physicalCard);
        when(mockVirtualCardRepository.findByCardHolder(user)).thenReturn(virtualCard);

        // Act
        toTest.deductFunds(senderAccount, amount);

        // Assert
        assertEquals(0.0, physicalCard.getBalance());
        assertEquals(150.0, virtualCard.getBalance()); // 200 - 50
        assertEquals(850.0, senderAccount.getBalance()); // 1000 - 150


        verify(mockCardRepository, times(1)).save(physicalCard);
        verify(mockVirtualCardRepository, times(1)).save(virtualCard);
        verify(mockAccountRepository, times(1)).save(senderAccount);
    }

    @Test
    void testReceiverCardTypeExists() {
        // Arrange
        Card card = new Card();
        card.setCardNumber(CARD_NUMBER);
        when(mockCardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.of(card));

        // Execute the method
        boolean result = toTest.receiverCardType(CARD_NUMBER);

        // Assertions
        assertTrue(result);

        // Verify repository interaction
        verify(mockCardRepository, times(1)).findByCardNumber(CARD_NUMBER);
    }

    @Test
    void testReceiverCardTypeDoesNotExist() {
      ;

        // Mocking
        when(mockCardRepository.findByCardNumber(CARD_NUMBER)).thenReturn(Optional.empty());

        // Act
        boolean result = toTest.receiverCardType(CARD_NUMBER);

        // Assert
        assertFalse(result);

        //check repo saved the entity
        verify(mockCardRepository, times(1)).findByCardNumber(CARD_NUMBER);
    }

    @Test
    void testConvertAmount() {
        // Arrange
        BigDecimal amount = BigDecimal.valueOf(100);
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        BigDecimal convertedAmount = BigDecimal.valueOf(85);


        when(mockExchangeRateService.convert(fromCurrency, toCurrency, amount)).thenReturn(convertedAmount);

        // Act
        BigDecimal result = toTest.convertAmount(amount, fromCurrency, toCurrency);

        // Assertions
        assertEquals(convertedAmount, result);

        verify(mockExchangeRateService, times(1)).convert(fromCurrency, toCurrency, amount);
    }



    @Test
    void testCardReductionCard() {
        //Arrange
        UserServiceImpl toTestSpy = spy(toTest);

        Account senderAccount = new Account();
        senderAccount.setBalance(1000.0);
        senderAccount.setCurrency(Currency.USD);

        User sender = new User();
        sender.setUsername("senderUser");
        senderAccount.setUser(sender);

        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(500.0));
        transaction.setCurrency(Currency.EUR);

        VirtualCard receiverVirtualCard = new VirtualCard();
        receiverVirtualCard.setCardHolder(new User());
        receiverVirtualCard.setBalance(300.0);
        receiverVirtualCard.setCurrency(Currency.EUR);

        Card receiverCard = new Card();
        receiverCard.setCardHolder(new User());
        receiverCard.setBalance(500.0);
        receiverCard.setCurrency(Currency.USD);

        Account receiverAccount = new Account();
        receiverAccount.setBalance(500.0);
        receiverAccount.setUser(receiverVirtualCard.getCardHolder());

        BigDecimal expectedConvertAmount = BigDecimal.valueOf(500.0);
        doReturn(expectedConvertAmount).when(toTestSpy).convertAmount(any(BigDecimal.class), any(String.class), any(String.class));

       //Make sure repos invoke the right entitye
        when(mockCardRepository.findByCardHolder(sender)).thenReturn(receiverCard);
        when(mockVirtualCardRepository.findByCardHolder(any(User.class))).thenReturn(receiverVirtualCard);
        when(mockAccountRepository.findByUser(receiverCard.getCardHolder())).thenReturn(receiverAccount);

        // Saving entities
        when(mockCardRepository.save(any(Card.class))).thenReturn(receiverCard);
        Mockito.lenient().when(mockVirtualCardRepository.save(any(VirtualCard.class))).thenReturn(receiverVirtualCard);
        when(mockAccountRepository.save(any(Account.class))).thenReturn(receiverAccount);

        // Act
        toTestSpy.cardReduction(senderAccount, transaction, receiverCard);

        // check repo for number of saves
        verify(mockCardRepository, times(2)).save(receiverCard);
        verify(mockAccountRepository, times(1)).save(receiverAccount);

       //Assert
        assertEquals(1000.0 - expectedConvertAmount.doubleValue(), senderAccount.getBalance());
        assertEquals(500.0 + expectedConvertAmount.doubleValue(), receiverAccount.getBalance());
    }

    @Test
    void testMakeTransactionCorrectTransactionHandlingMethod() {
        // Prepare test data
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAmountBase(BigDecimal.valueOf(11000.0));
        transactionDto.setCardNumber(CARD_NUMBER);
        transactionDto.setCurrency(String.valueOf(Currency.EUR));
        transactionDto.setPin("1111");

        User sender = new User();
        sender.setId(1L);
        sender.setUsername(USERNAME);
        sender.setBranch(new Branch());

        Account senderAccount = new Account();
        senderAccount.setUser(sender);
        senderAccount.setBalance(1000.0); // Setting the balance for the sender account
        senderAccount.setCurrency(Currency.USD);

        User receiver = new User();
        receiver.setUsername("receiverUsername");

        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(11.0));

        // Mock repository behavior
        when(mockUserRepository.findByUsername(anyString())).thenReturn(Optional.of(sender));
        Mockito.lenient().when(mockUserRepository.findById(anyLong())).thenReturn(Optional.of(sender));
        when(mockAccountRepository.findByUser(any(User.class))).thenReturn(senderAccount);
        when(mockModelMapper.map(any(User.class), eq(CurrentUser.class))).thenReturn(new CurrentUser());
        Mockito.lenient().when(mockModelMapper.map(any(TransactionDto.class), eq(Transaction.class))).thenReturn(transaction);
        Mockito.lenient().when(mockCardRepository.findByCardNumber(anyString())).thenReturn(Optional.empty());
        Mockito.lenient().when(mockVirtualCardRepository.findByCardNumber(anyString())).thenReturn(Optional.empty());

        // Spy on toTest to access private methods
        UserServiceImpl toTestSpy = spy(toTest);

        // Mock the convertAmount method to return a value within the sender's balance
        doReturn(BigDecimal.valueOf(500.0)).when(toTestSpy).convertAmount(any(BigDecimal.class), anyString(), anyString());

        doReturn(transaction).when(toTestSpy).getTransaction(any(TransactionDto.class), any(User.class));
        doReturn(sender).when(toTestSpy).validateSenderPin(any(TransactionDto.class));
        Mockito.lenient().doReturn(receiver).when(toTestSpy).getReceiverAndSetCardType(any(TransactionDto.class), any(Transaction.class));

        // Execute the method
        toTestSpy.makeTransaction(transactionDto, USERNAME);

        // Verify that handleBranchTransaction was called
        verify(toTestSpy, times(1)).handleBranchTransaction(any(User.class), any(Transaction.class));

        // Verify that handleRegularTransaction was not called
        verify(toTestSpy, never()).handleRegularTransaction(any(Account.class), anyString(), any(Transaction.class));
    }

}

