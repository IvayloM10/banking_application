package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator,Long> {
    List<Administrator> findAll();

    Administrator findByCurrency(Currency currency);
}
