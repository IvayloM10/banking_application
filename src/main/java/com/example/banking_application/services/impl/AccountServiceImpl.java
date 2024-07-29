package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.enums.Currency;
import com.example.banking_application.repositories.AccountRepository;
import com.example.banking_application.services.AccountService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {
    private AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    @Override
    public Account getUserAccount(User user) {
        return this.accountRepository.findByUser(user);
    }

    @Override
    public Account createNewAccount(double balance, Currency currency) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(balance);
        account.setCurrency(currency);

        this.accountRepository.save(account);
        return account;
    }


    private String generateAccountNumber() {
        return "UB" + new Random().nextInt(999999);
    }
}
