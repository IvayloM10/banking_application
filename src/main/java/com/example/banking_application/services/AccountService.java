package com.example.banking_application.services;

import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.models.entities.enums.Currency;

public interface AccountService {
    Account getUserAccount(User user);

    Account createNewAccount(double balance, Currency currency);
}
