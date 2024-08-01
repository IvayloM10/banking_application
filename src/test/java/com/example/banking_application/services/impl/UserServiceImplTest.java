package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.*;
import com.example.banking_application.models.entities.enums.CardType;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.*;
import com.example.banking_application.services.AccountService;
import com.example.banking_application.services.ExchangeRateService;
import com.example.banking_application.services.VirtualCardService;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final String PASSWORD = "pass123";

    private static final String WRONG_PASSWORD = "wrongPass123";

    private static final String ENCODED_PASSWORD = "encodedPass123";

    private static final String USERNAME = "username";

    private static final String INCORRECT_USERNAME = "incorrectUsername";

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

        // Additional assertions to verify the user has the correct card and account
        assertThat(user.getCard().getCardHolder().getUsername()).isEqualTo(card.getCardHolder().getUsername());
        assertThat(user.getAccount().getUser().getUsername()).isEqualTo(account.getUser().getUsername());
    }
}

