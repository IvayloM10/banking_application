package com.example.banking_application.inits;

import com.example.banking_application.models.entities.Account;
import com.example.banking_application.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AccountTaxDeduction {

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 1 * ?")
    public void reduceAccountBalances() {
        List<Account> accounts = this.accountRepository.findAll();
        //reduce accounts every first day of the month
        for (Account account : accounts) {
            account.reduceAccount(1.5);
            this.accountRepository.save(account);
        }
    }
}
