package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.entities.*;


import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.*;
import com.example.banking_application.services.AccountService;
import com.example.banking_application.services.LoanService;
import com.example.banking_application.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.util.Assert;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdministratorServiceImplTest {

    private static final String ADMINISTRATOR_USERNAME = "EUR";

    private static final String ADMINISTRATOR_PASSWORD = "0000";
    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private TransactionRepository mockTransactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private VirtualCardRepository virtualCardRepository;

    @Mock
    private LoanService loanService;

    @Mock
    private AccountService accountService;


    private CurrentUser currentUser;

    @InjectMocks
    private AdministrationServiceImpl administrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        administrationService.initialize();
    }


    @Test
    public void testGetCurrentUser() throws NoSuchFieldException, IllegalAccessException {
        // Arrange

        Field currentUserField = AdministrationServiceImpl.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        CurrentUser expectedUser = (CurrentUser) currentUserField.get(administrationService);

        // Act
        CurrentUser actualUser = administrationService.getCurrentUser();

        // Assert
        assertSame(expectedUser, actualUser);
    }



    @Test
    void testLoginAdminFailure() {
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setUsername("admin");
        userLoginDto.setPassword( "wrongPassword");
        Mockito.lenient().when(administratorRepository.findByUsername("admin")).thenReturn(Optional.empty());

        boolean result = administrationService.loginAdmin(userLoginDto);

        assertFalse(result);
    }


    @Test
   void testGetCurrentAdminDoesNotExist() {
        // Arrange
        String username = "nonexistentAdmin";

        //Act
        Administrator result = administrationService.getCurrentAdmin(username);

        //Assert
        assertNull(result);
    }



    @Test
    void testRemoveLoanFromView() throws NoSuchFieldException, IllegalAccessException {
        AdministrationServiceImpl toTest = spy(administrationService);
        // Arrange
        Administrator administrator = new Administrator();
        administrator.setUsername(ADMINISTRATOR_USERNAME);
        Administrator administratorSpy = spy(administrator);

        Branch branch = new Branch();
        branch.setAdministrator(administratorSpy);
        administratorSpy.setBranch(branch);

        Loan loan = new Loan();
        loan.setId(1L);
        branch.getLoans().add(loan);

        CurrentUser currentUser = new CurrentUser();
        currentUser.setUsername(ADMINISTRATOR_USERNAME);
        Field currentUserField = AdministrationServiceImpl.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        currentUserField.set(toTest, currentUser);


        doReturn(administratorSpy).when(toTest).getCurrentAdmin(ADMINISTRATOR_USERNAME);

        Mockito.lenient().when(administratorRepository.findByUsername(ADMINISTRATOR_USERNAME)).thenReturn(Optional.of(administratorSpy));

        // Act
        toTest.removeLoanFromAdminView(loan);

        // Assert
        assertFalse(branch.getLoans().contains(loan));

    }

}
