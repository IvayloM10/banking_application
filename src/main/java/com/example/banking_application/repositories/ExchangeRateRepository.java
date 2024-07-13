package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate,Long> {
    Optional<ExchangeRate> findByCurrency(String currency);
}
