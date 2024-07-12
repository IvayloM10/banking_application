package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    Account findByUser(User user);
}
