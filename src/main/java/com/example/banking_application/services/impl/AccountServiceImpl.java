package com.example.banking_application.services.impl;

import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.AccountRepository;
import com.example.banking_application.services.AccountService;
import org.springframework.stereotype.Service;

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
}
