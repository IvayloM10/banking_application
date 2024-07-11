package com.example.banking_application.repositories;

import com.example.banking_application.models.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card,Long>{

}
