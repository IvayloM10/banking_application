package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.entities.*;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdministratorServiceImplTest {
    @Mock
    private AdministratorRepository administratorRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private TransactionRepository transactionRepository;

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

    @InjectMocks
    private AdministrationServiceImpl administrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

}
