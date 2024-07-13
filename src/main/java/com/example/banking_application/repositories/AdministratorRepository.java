package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.Administrator;
import com.example.banking_application.models.entities.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator,Long> {
    List<Administrator> findAll();

    Administrator findByCurrency(Currency currency);

    Optional<Administrator> findByUsername(String username);

    Optional<Administrator> findById(Long id);
}
