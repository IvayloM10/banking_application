package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator,Long> {
}
