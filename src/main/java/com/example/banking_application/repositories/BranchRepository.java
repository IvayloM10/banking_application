package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.Branch;
import com.example.banking_application.models.entities.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findAll();

    Branch findByCurrency(Currency currency);

    Branch findByRegion(String region);
}
