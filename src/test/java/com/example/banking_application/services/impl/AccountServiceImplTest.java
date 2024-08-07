package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository mockAccountRepository;

    private AccountServiceImpl toTest;

    @BeforeEach
    void setUp(){
        this.toTest = new AccountServiceImpl(mockAccountRepository);
    }
    @Test
    void testGetUserAccount() {
        User user = new User(); // Create a User object if needed
        Account expectedAccount = new Account();
        when(mockAccountRepository.findByUser(user)).thenReturn(expectedAccount);

        Account result = toTest.getUserAccount(user);

        assertEquals(expectedAccount, result);
        verify(mockAccountRepository, times(1)).findByUser(user);
    }

    @Test
    void testCreateNewAccount() {
        double balance = 1000.0;
        Currency currency = Currency.USD;

        // Capturing the Account object passed to save
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        Account createdAccount = toTest.createNewAccount(balance, currency);

        verify(mockAccountRepository, times(1)).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();

        assertEquals(createdAccount, savedAccount);
        assertEquals(balance, savedAccount.getBalance());
        assertEquals(currency, savedAccount.getCurrency());

    }
}
