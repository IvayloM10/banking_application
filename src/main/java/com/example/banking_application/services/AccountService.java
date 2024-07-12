package com.example.banking_application.services;

import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.User;

public interface AccountService {
    Account getUserAccount(User user);
}
