package com.example.banking_application.services.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.banking_application.inits.AccountTaxDeduction;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

public class AccountTaxDeductionTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountTaxDeduction accountTaxDeduction;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Transactional
    public void testReduceAccountBalances() {
        // Arrange
        Account account1 = new Account();
        account1.setBalance(100.0);

        Account account2 = new Account();
        account2.setBalance(200.0);

        List<Account> accounts = Arrays.asList(account1, account2);

        when(accountRepository.findAll()).thenReturn(accounts);

        // Act
        accountTaxDeduction.reduceAccountBalances();

        // Assert
        assertEquals(98.5, account1.getBalance());
        assertEquals(198.5, account2.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
    }
}
